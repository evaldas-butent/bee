package com.butent.bee.server;

import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.IOException;
import java.io.PrintWriter;

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

  private ResponseObject dispatch(String service) {
    ResponseObject responseObject;

    String msg = BeeUtils.concat(1, service, "service not recognized");
    logger.warning(msg);
    responseObject = ResponseObject.error(msg);

    return responseObject;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doService(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doService(req, resp);
  }

  private void doService(HttpServletRequest req, HttpServletResponse resp) {
    long start = System.currentTimeMillis();
    String service = HttpUtils.readPart(req, Service.NAME_SERVICE);
    String prefix = getClass().getSimpleName() + ":";

    ResponseObject responseObject;
    if (BeeUtils.isEmpty(service)) {
      String msg = BeeUtils.concat(1, prefix, "service name not specified");
      logger.error(msg);
      responseObject = ResponseObject.error(msg);
    } else {
      logger.info(prefix, "request", service);
      responseObject = dispatch(service);
      if (responseObject == null) {
        String msg = BeeUtils.concat(1, prefix, service, "response empty");
        logger.warning(msg);
        responseObject = ResponseObject.warning(msg);
      }
    }

    ContentType ctp = CommUtils.formResponseContentType;
    resp.setContentType(CommUtils.getMediaType(ctp));
    resp.setCharacterEncoding(CommUtils.getCharacterEncoding(ctp));

    String content = CommUtils.prepareContent(ctp, responseObject.serialize());

    logger.info(prefix, BeeUtils.elapsedSeconds(start), "response",
        resp.getContentType(), content.length());

    try {
      PrintWriter writer = resp.getWriter();
      writer.print(content);
      writer.flush();
    } catch (IOException ex) {
      LogUtils.stack(logger, ex);
    }
  }
}
