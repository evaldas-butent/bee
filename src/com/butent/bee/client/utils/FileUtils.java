package com.butent.bee.client.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.ProgressEvent;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.html.Window;
import elemental.js.dom.JsClipboard;
import elemental.xml.XMLHttpRequest;

public final class FileUtils {

  private static final BeeLogger logger = LogUtils.getLogger(FileUtils.class);

  private static final String OPEN_URL = "file";
  private static final String UPLOAD_URL = "upload";

  private static final long MIN_FILE_SIZE_FOR_PROGRESS = 100000;

  public static void deletePhoto(final String photoFileName, final Callback<String> callback) {
    Assert.notEmpty(photoFileName);

    Map<String, String> parameters = createParameters(Service.DELETE_PHOTO, photoFileName);

    final XMLHttpRequest xhr = createXhr();
    xhr.open(RequestBuilder.POST.toString(), getUploadUrl(parameters), true);

    addSessionId(xhr);

    xhr.setOnload(new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        if (xhr.getStatus() == Response.SC_OK) {
          String response = xhr.getResponseText();

          if (BeeUtils.same(response, photoFileName)) {
            logger.info("deleted photo", photoFileName);
            logger.addSeparator();

            if (callback != null) {
              callback.onSuccess(photoFileName);
            }

          } else {
            String msg = BeeUtils.joinWords("delete", photoFileName, "response:", response);
            logger.warning(msg);
            if (callback != null) {
              callback.onFailure(msg);
            }
          }

        } else {
          String msg = BeeUtils.joinWords("delete", photoFileName, "response status:",
              BeeUtils.bracket(xhr.getStatus()), xhr.getStatusText());
          logger.severe(msg);
          if (callback != null) {
            callback.onFailure(msg);
          }
        }
      }
    });

    xhr.send();
  }

  public static String generatePhotoFileName(String originalFileName) {
    String name = BeeUtils.join(BeeConst.STRING_UNDER, BeeUtils.randomString(6),
        System.currentTimeMillis());
    String ext = FileNameUtils.getExtension(originalFileName);

    return BeeUtils.isEmpty(ext) ? name : FileNameUtils.addExtension(name, ext);
  }

  public static FileList getFiles(NativeEvent event) {
    Assert.notNull(event);

    DataTransfer dataTransfer = event.getDataTransfer();
    if (dataTransfer == null) {
      return null;
    }

    JsClipboard clipboard = dataTransfer.cast();
    return clipboard.getFiles();
  }

  public static List<NewFileInfo> getNewFileInfos(FileList fileList) {
    List<NewFileInfo> result = Lists.newArrayList();
    if (fileList == null) {
      return result;
    }

    for (int i = 0; i < fileList.length(); i++) {
      result.add(new NewFileInfo(fileList.item(i)));
    }
    return result;
  }

  public static String getUrl(String fileName, Long fileId) {
    Map<String, String> parameters = Maps.newHashMap();
    parameters.put(Service.VAR_FILE_ID, BeeUtils.toString(fileId));
    parameters.put(Service.VAR_FILE_NAME, fileName);

    return CommUtils.addQueryString(GWT.getHostPageBaseURL() + OPEN_URL,
        CommUtils.buildQueryString(parameters, true));
  }

  public static void readAsDataURL(File file, final Consumer<String> consumer) {
    final FileReader reader = Browser.getWindow().newFileReader();

    reader.setOnload(new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        consumer.accept((String) reader.getResult());
      }
    });

    reader.readAsDataURL(file);
  }

  public static String sizeToText(Long size) {
    long sz = BeeUtils.unbox(size);
    String prfx = "MB";
    long c = 1;

    for (int i = 1; i < 3; i++) {
      if (sz < c * 1024) {
        prfx = (i == 1) ? "B" : "KB";
        break;
      }
      c *= 1024;
    }
    if (c == 1) {
      return sz + prfx;
    }
    return Math.round(sz * 10d / c) / 10d + prfx;
  }

  public static void uploadFile(NewFileInfo fileInfo, final Callback<Long> callback) {
    final String fileName = BeeUtils.notEmpty(fileInfo.getCaption(), fileInfo.getName());
    final String fileType = fileInfo.getType();
    final long fileSize = fileInfo.getSize();

    final long start = System.currentTimeMillis();

    upload(Service.UPLOAD_FILE, fileInfo, new Callback<String>() {
      @Override
      public void onSuccess(String response) {
        if (BeeUtils.isLong(response)) {
          long fileId = BeeUtils.toLong(response);

          logger.info(TimeUtils.elapsedSeconds(start), "uploaded", fileName);
          logger.info("type:", fileType, "size:", fileSize, "id:", fileId);
          logger.addSeparator();

          callback.onSuccess(fileId);

        } else {
          String msg = BeeUtils.joinWords("upload", fileName, "response:", response);
          logger.warning(msg);
          callback.onFailure(msg);
        }
      }

      @Override
      public void onFailure(String... reason) {
        callback.onFailure(reason);
      }
    });
  }

  public static void uploadPhoto(NewFileInfo fileInfo, final String photoFileName, String oldPhoto,
      final Callback<String> callback) {

    Assert.notNull(fileInfo);
    Assert.notEmpty(photoFileName);
    Assert.notNull(callback);

    final String originalFileName = fileInfo.getName();
    final long fileSize = fileInfo.getSize();

    Map<String, String> parameters = createParameters(Service.UPLOAD_PHOTO, photoFileName);

    parameters.put(Service.VAR_FILE_SIZE, BeeUtils.toString(fileSize));
    if (!BeeUtils.isEmpty(oldPhoto)) {
      parameters.put(Service.VAR_OLD_VALUE, oldPhoto.trim());
    }

    final String progressId = maybeCreateProgress(originalFileName, fileSize);

    final XMLHttpRequest xhr = createXhr();
    xhr.open(RequestBuilder.POST.toString(), getUploadUrl(parameters), true);

    addSessionId(xhr);

    final long start = System.currentTimeMillis();

    xhr.setOnload(new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        if (progressId != null) {
          BeeKeeper.getScreen().closeProgress(progressId);
        }

        if (xhr.getStatus() == Response.SC_OK) {
          String response = xhr.getResponseText();

          if (BeeUtils.same(response, photoFileName)) {
            logger.info(TimeUtils.elapsedSeconds(start), originalFileName, "size:", fileSize);
            logger.info("uploaded as:", photoFileName);
            logger.addSeparator();

            callback.onSuccess(photoFileName);

          } else {
            String msg = BeeUtils.joinWords("upload", originalFileName, "response:", response);
            logger.warning(msg);
            callback.onFailure(msg);
          }

        } else {
          String msg = BeeUtils.joinWords("upload", originalFileName, "response status:",
              BeeUtils.bracket(xhr.getStatus()), xhr.getStatusText());
          logger.severe(msg);
          callback.onFailure(msg);
        }
      }
    });

    addProgressListener(xhr, progressId);
    xhr.send(fileInfo.getFile());
  }

  public static void uploadTempFile(NewFileInfo fileInfo, final Callback<String> callback) {
    final String fileName = BeeUtils.notEmpty(fileInfo.getCaption(), fileInfo.getName());
    final String fileType = fileInfo.getType();
    final long fileSize = fileInfo.getSize();

    final long start = System.currentTimeMillis();

    upload(Service.UPLOAD_TEMP_FILE, fileInfo, new Callback<String>() {
      @Override
      public void onSuccess(String response) {
        logger.info(TimeUtils.elapsedSeconds(start), "uploaded", fileName);
        logger.info("type:", fileType, "size:", fileSize, "file:", response);
        logger.addSeparator();

        callback.onSuccess(response);
      }

      @Override
      public void onFailure(String... reason) {
        callback.onFailure(reason);
      }
    });
  }

  static native double getLastModifiedMillis(File file) /*-{
    return file.lastModifiedDate.getTime();
  }-*/;

  private static void addProgressListener(XMLHttpRequest xhr, final String progressId) {
    if (progressId != null) {
      xhr.getUpload().setOnprogress(new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          BeeKeeper.getScreen().updateProgress(progressId, ((ProgressEvent) evt).getLoaded());
        }
      });
    }
  }

  private static void addSessionId(XMLHttpRequest xhr) {
    String sid = BeeKeeper.getUser().getSessionId();
    if (!BeeUtils.isEmpty(sid)) {
      xhr.setRequestHeader(Service.RPC_VAR_SID, sid);
    }
  }

  private static Map<String, String> createParameters(String service, String fileName) {
    Map<String, String> parameters = Maps.newHashMap();

    parameters.put(Service.RPC_VAR_SVC, service);
    parameters.put(Service.VAR_FILE_NAME, fileName);

    return parameters;
  }

  private static XMLHttpRequest createXhr() {
    return createXhr(Browser.getWindow());
  }

  private static XMLHttpRequest createXhr(Window window) {
    return window.newXMLHttpRequest();
  }

  private static String getUploadUrl(Map<String, String> parameters) {
    return CommUtils.addQueryString(GWT.getHostPageBaseURL() + UPLOAD_URL,
        CommUtils.buildQueryString(parameters, true));
  }

  private static String maybeCreateProgress(String caption, long size) {
    return (size > MIN_FILE_SIZE_FOR_PROGRESS)
        ? BeeKeeper.getScreen().createProgress(caption, (double) size, null) : null;
  }

  private static void upload(String srv, NewFileInfo fileInfo, final Callback<String> callback) {
    Assert.notEmpty(srv);
    Assert.notNull(fileInfo);
    Assert.notNull(callback);

    final String fileName = BeeUtils.notEmpty(fileInfo.getCaption(), fileInfo.getName());
    final String fileType = fileInfo.getType();
    final long fileSize = fileInfo.getSize();

    Map<String, String> parameters = createParameters(srv, fileName);

    parameters.put(Service.VAR_FILE_TYPE, fileType);
    parameters.put(Service.VAR_FILE_SIZE, BeeUtils.toString(fileSize));

    final String progressId = maybeCreateProgress(fileName, fileSize);

    final XMLHttpRequest xhr = createXhr();
    xhr.open(RequestBuilder.POST.toString(), getUploadUrl(parameters), true);

    addSessionId(xhr);

    xhr.setOnload(new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        if (progressId != null) {
          BeeKeeper.getScreen().closeProgress(progressId);
        }
        String msg = BeeUtils.joinWords("upload", fileName, "response status:");

        if (xhr.getStatus() == Response.SC_OK) {
          ResponseObject resp = ResponseObject.restore(xhr.getResponseText());

          if (!resp.hasErrors()) {
            callback.onSuccess(resp.getResponseAsString());
            return;
          }
          msg = BeeUtils.joinWords(msg, resp.getErrors());
        } else {
          msg = BeeUtils.joinWords(msg, BeeUtils.bracket(xhr.getStatus()), xhr.getStatusText());
        }
        logger.severe(msg);
        callback.onFailure(msg);
      }
    });
    addProgressListener(xhr, progressId);
    xhr.send(fileInfo.getFile());
  }

  private FileUtils() {
  }
}
