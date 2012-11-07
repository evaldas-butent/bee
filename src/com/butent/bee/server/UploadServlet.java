package com.butent.bee.server;

import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements upload servlet functionality.
 */

@SuppressWarnings("serial")
@MultipartConfig
public class UploadServlet extends HttpServlet {

  private static BeeLogger logger = LogUtils.getLogger(UploadServlet.class);

  @EJB
  FileStorageBean fs;
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Assert.untouchable();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    handleFileUpload(req, resp);
  }

  private void handleFileUpload(HttpServletRequest req, HttpServletResponse resp) {
    long start = System.currentTimeMillis();
    String prefix = getClass().getSimpleName() + ":";
    
    Map<String, String> parameters = HttpUtils.getHeaders(req, false);
    parameters.putAll(HttpUtils.getParameters(req, true));
    
    String fileName = parameters.get(Service.VAR_FILE_NAME);
    String mimeType = parameters.get(Service.VAR_FILE_TYPE);
    Long fileSize = BeeUtils.toLongOrNull(parameters.get(Service.VAR_FILE_SIZE));
    
    String response;
    
    if (BeeUtils.isEmpty(fileName)) {
      response = BeeUtils.joinWords(prefix, "file name not specified");
      logger.severe(response);

    } else {
      if (BeeUtils.isEmpty(mimeType)) {
        logger.warning(prefix, "mime type not specified:", fileName, "size:", fileSize);
      }

      try {
        Long fileId = fs.storeFile(req.getInputStream(), fileName, mimeType);
        if (DataUtils.isId(fileId)) {
          response = BeeUtils.toString(fileId);
          logger.info(prefix, TimeUtils.elapsedSeconds(start), "stored", fileName,
              "type", mimeType, "size", fileSize, "id", fileId);
        } else {
          response = BeeUtils.joinWords(prefix, fileName, "not stored");
          logger.warning(response);
        }

      } catch (IOException ex) {
        logger.error(ex);
        response = prefix + BeeUtils.notEmpty(ex.getMessage(), ex.getClass().getSimpleName());
      }
    }

    ContentType ctp = ContentType.TEXT;
    resp.setContentType(CommUtils.getMediaType(ctp));
    resp.setCharacterEncoding(CommUtils.getCharacterEncoding(ctp));

    try {
      PrintWriter writer = resp.getWriter();
      writer.print(response);
      writer.flush();
    } catch (IOException ex) {
      logger.error(ex);
    }
  }
}
