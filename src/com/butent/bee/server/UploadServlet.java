package com.butent.bee.server;

import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements upload servlet functionality.
 */

@WebServlet(urlPatterns = "/upload")
@SuppressWarnings("serial")
@MultipartConfig
public class UploadServlet extends LoginServlet {

  private static BeeLogger logger = LogUtils.getLogger(UploadServlet.class);

  @EJB
  FileStorageBean fs;

  private static ResponseObject uploadTempFile(HttpServletRequest req,
      Map<String, String> parameters) {

    File tmp;
    try {
      tmp = File.createTempFile("bee_", null);
      tmp.deleteOnExit();
    } catch (IOException e) {
      logger.error(e);
      return ResponseObject.error(e.getMessage());
    }
    long start = System.currentTimeMillis();
    String fileName = parameters.get(Service.VAR_FILE_NAME);
    Long fileSize = BeeUtils.toLongOrNull(parameters.get(Service.VAR_FILE_SIZE));

    ResponseObject response;
    OutputStream out = null;

    try {
      InputStream is = req.getInputStream();
      out = new FileOutputStream(tmp);

      byte[] buffer = new byte[8192];
      int bytesRead;

      while ((bytesRead = is.read(buffer)) > 0) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush();
      response = ResponseObject.response(tmp.getAbsolutePath());

      logger.info(UploadServlet.class.getSimpleName(), TimeUtils.elapsedSeconds(start),
          "stored", fileName, "size", fileSize, "file", response.getResponse());

    } catch (IOException e) {
      logger.error(e);
      response = ResponseObject.error(e.getMessage());

    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          logger.error(e);
        }
      }
    }
    return response;
  }

  @Override
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    Map<String, String> parameters = HttpUtils.getHeaders(req, false);
    parameters.putAll(HttpUtils.getParameters(req, true));

    ResponseObject response;

    String service = parameters.get(Service.RPC_VAR_SVC);
    if (BeeUtils.same(service, Service.UPLOAD_FILE)) {
      response = uploadFile(req, parameters);
    } else if (BeeUtils.same(service, Service.UPLOAD_TEMP_FILE)) {
      response = uploadTempFile(req, parameters);
    } else if (BeeUtils.same(service, Service.UPLOAD_PHOTO)) {
      response = ResponseObject.response(uploadPhoto(req, parameters));
    } else if (BeeUtils.same(service, Service.DELETE_PHOTO)) {
      response = ResponseObject.response(deletePhoto(parameters));
    } else {
      response = ResponseObject.error(BeeUtils.joinWords(getClass().getSimpleName(), service,
          "service not recognized"));
      logger.severe((Object[]) response.getErrors());
    }

    ContentType ctp = ContentType.TEXT;
    resp.setContentType(CommUtils.getMediaType(ctp));
    resp.setCharacterEncoding(CommUtils.getCharacterEncoding(ctp));

    try {
      PrintWriter writer = resp.getWriter();
      writer.print(Codec.beeSerialize(response));
      writer.flush();
    } catch (IOException ex) {
      logger.error(ex);
    }
  }

  private String deletePhoto(Map<String, String> parameters) {
    String fileName = parameters.get(Service.VAR_FILE_NAME);

    if (BeeUtils.isEmpty(fileName)) {
      String message = "photo file name not specified";
      logger.severe(message);
      return message;

    } else if (fs.photoExists(fileName)) {
      boolean deleted = fs.deletePhoto(fileName);

      if (deleted) {
        logger.info("photo deleted:", fileName);
        return fileName.trim();
      } else {
        String message = "cannot delete existing photo: " + fileName;
        logger.severe(message);
        return message;
      }

    } else {
      String message = "photo not found: " + fileName;
      logger.warning(message);
      return message;
    }
  }

  private ResponseObject uploadFile(HttpServletRequest req, Map<String, String> parameters) {
    long start = System.currentTimeMillis();
    String prefix = "file upload:";

    String fileName = parameters.get(Service.VAR_FILE_NAME);
    String mimeType = parameters.get(Service.VAR_FILE_TYPE);
    Long fileSize = BeeUtils.toLongOrNull(parameters.get(Service.VAR_FILE_SIZE));

    ResponseObject response;

    if (BeeUtils.isEmpty(fileName)) {
      response = ResponseObject.error(prefix, "file name not specified");

    } else {
      if (BeeUtils.isEmpty(mimeType)) {
        logger.warning(prefix, "mime type not specified:", fileName, "size:", fileSize);
      }
      try {
        Long fileId = fs.storeFile(req.getInputStream(), fileName, mimeType);
        if (DataUtils.isId(fileId)) {
          response = ResponseObject.response(fileId);
          logger.info(prefix, TimeUtils.elapsedSeconds(start), "stored", fileName,
              "type", mimeType, "size", fileSize, "id", fileId);
        } else {
          response = ResponseObject.error(prefix, fileName, "not stored");
        }
      } catch (IOException ex) {
        response = ResponseObject.error(prefix + BeeUtils.notEmpty(ex.getMessage(),
            ex.getClass().getSimpleName()));
      }
    }
    if (response.hasErrors()) {
      logger.severe((Object[]) response.getErrors());
    }
    return response;
  }

  private String uploadPhoto(HttpServletRequest req, Map<String, String> parameters) {
    String fileName = parameters.get(Service.VAR_FILE_NAME);
    if (BeeUtils.isEmpty(fileName)) {
      String message = "photo file name not specified";
      logger.severe(message);
      return message;
    }

    String oldPhoto = parameters.get(Service.VAR_OLD_VALUE);
    if (!BeeUtils.isEmpty(oldPhoto)) {
      boolean deleted = fs.deletePhoto(oldPhoto);
      if (deleted) {
        logger.info("old photo deleted:", oldPhoto);
      } else {
        logger.warning("cannot delete old photo:", oldPhoto);
      }
    }

    if (fs.photoExists(fileName)) {
      boolean deleted = fs.deletePhoto(fileName);
      if (deleted) {
        logger.info("existing photo deleted:", fileName);
      } else {
        String message = "cannot delete existing photo: " + fileName;
        logger.severe(message);
        return message;
      }
    }

    long start = System.currentTimeMillis();
    String response;

    try {
      boolean stored = fs.storePhoto(req.getInputStream(), fileName);
      if (stored) {
        logger.info(TimeUtils.elapsedSeconds(start), "stored photo:", fileName);
        response = fileName.trim();
      } else {
        response = BeeUtils.joinWords("photo not stored:", fileName);
      }

    } catch (IOException ex) {
      logger.error(ex);
      response = BeeUtils.notEmpty(ex.getMessage(), ex.getClass().getSimpleName());
    }

    return response;
  }
}
