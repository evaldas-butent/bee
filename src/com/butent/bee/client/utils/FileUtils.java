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
import com.butent.bee.client.communication.RpcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

import elemental.events.ProgressEvent;

import elemental.events.Event;
import elemental.events.EventListener;

import elemental.xml.XMLHttpRequest;

import elemental.html.Window;

import elemental.client.Browser;

import elemental.js.dom.JsClipboard;

import elemental.html.File;

import elemental.html.FileList;

public class FileUtils {

  private static final BeeLogger logger = LogUtils.getLogger(FileUtils.class);

  private static final String UPLOAD_URL = "upload";
  
  private static final long MIN_FILE_SIZE_FOR_PROGRESS = 100000; 

  public static List<FileInfo> getFileInfo(FileList fileList) {
    List<FileInfo> result = Lists.newArrayList();
    if (fileList == null) {
      return result;
    }

    for (int i = 0; i < fileList.length(); i++) {
      result.add(new FileInfo(fileList.item(i)));
    }
    return result;
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

  public static void upload(FileInfo fileInfo, final Callback<Long> callback) {
    Assert.notNull(fileInfo);
    Assert.notNull(callback);
    
    final String fileName = BeeUtils.notEmpty(fileInfo.getCaption(), fileInfo.getName());
    final String fileType = fileInfo.getType(); 
    final long fileSize = fileInfo.getSize();

    Map<String, String> parameters = Maps.newHashMap();
    parameters.put(Service.VAR_FILE_NAME, fileName);
    parameters.put(Service.VAR_FILE_TYPE, fileType);
    parameters.put(Service.VAR_FILE_SIZE, BeeUtils.toString(fileSize));
    
    final String progressId = (fileSize > MIN_FILE_SIZE_FOR_PROGRESS)
        ? BeeKeeper.getScreen().createProgress(fileName, fileSize) : null;

    String url = RpcUtils.addQueryString(GWT.getModuleBaseURL() + UPLOAD_URL,
        RpcUtils.buildQueryString(parameters, true));

    final XMLHttpRequest xhr = createXhr();
    xhr.open(RequestBuilder.POST.toString(), url, true);

    String sid = BeeKeeper.getUser().getSessionId();
    if (!BeeUtils.isEmpty(sid)) {
      xhr.setRequestHeader(Service.RPC_VAR_SID, sid);
    }

    final long start = System.currentTimeMillis();

    xhr.setOnload(new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        if (progressId != null) {
          BeeKeeper.getScreen().closeProgress(progressId);
        }

        if (xhr.getStatus() == Response.SC_OK) {
          String response = xhr.getResponseText();

          if (BeeUtils.isLong(response)) {
            long fileId = BeeUtils.toLong(response);

            logger.info(TimeUtils.elapsedSeconds(start), "uploaded", fileName);
            logger.info("type:", fileType, "size:", fileSize, "id:", fileId);
            logger.addSeparator();
            
            callback.onSuccess(fileId);

          } else {
            String msg = BeeUtils.joinWords("upload", fileName, "response:",
                response);
            logger.warning(msg);
            callback.onFailure(msg);
          }

        } else {
          String msg = BeeUtils.joinWords("upload", fileName, "response status:",
              BeeUtils.bracket(xhr.getStatus()), xhr.getStatusText());
          logger.severe(msg);
          callback.onFailure(msg);
        }
      }
    });

    if (progressId != null) {
      xhr.getUpload().setOnprogress(new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          BeeKeeper.getScreen().updateProgress(progressId, ((ProgressEvent) evt).getLoaded());
        }
      });
    }

    xhr.send(fileInfo.getFile());
  }

  static native double getLastModifiedMillis(File file) /*-{
    return file.lastModifiedDate.getTime();
  }-*/;

  private static XMLHttpRequest createXhr() {
    return createXhr(Browser.getWindow());
  }

  private static native XMLHttpRequest createXhr(Window window) /*-{
    return new window.XMLHttpRequest();
  }-*/;

  private FileUtils() {
  }
}
