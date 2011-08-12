package com.butent.bee.server;

import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements upload servlet functionality.
 */

@SuppressWarnings("serial")
@WebServlet(name = "UploadServlet", urlPatterns = {"/bee/upload"})
@MultipartConfig
public class UploadServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger(UploadServlet.class.getName());

  @EJB
  UiServiceBean uiBean;

  private ResponseObject dispatch(String service, HttpServletRequest req) {
    ResponseObject responseObject;

    if (BeeUtils.same(service, Service.IMPORT_FORM)) {
      String formName = HttpUtils.readPart(req, Service.VAR_FORM_NAME);
      String design = HttpUtils.readPart(req, Service.VAR_FILE_NAME);
      responseObject = uiBean.importForm(formName, BeeUtils.trim(design));
    } else {
      String msg = BeeUtils.concat(1, service, "service not recognized");
      LogUtils.warning(logger, msg);
      responseObject = ResponseObject.error(msg);
    }

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
      LogUtils.severe(logger, msg);
      responseObject = ResponseObject.error(msg);
    } else {
      LogUtils.infoNow(logger, prefix, "request", service);
      responseObject = dispatch(service, req);
      if (responseObject == null) {
        String msg = BeeUtils.concat(1, prefix, service, "response empty");
        LogUtils.warning(logger, msg);
        responseObject = ResponseObject.warning(msg);
      }
    }

    ContentType ctp = CommUtils.formResponseContentType;
    resp.setContentType(CommUtils.getMediaType(ctp));
    resp.setCharacterEncoding(CommUtils.getCharacterEncoding(ctp));

    String content = CommUtils.prepareContent(ctp, responseObject.serialize());

    LogUtils.infoNow(logger, prefix, BeeUtils.elapsedSeconds(start), "response",
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
