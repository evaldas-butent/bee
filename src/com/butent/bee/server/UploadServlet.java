package com.butent.bee.server;

import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
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

@SuppressWarnings("serial")
@WebServlet(name = "UploadServlet", urlPatterns = {"/bee/upload/*"})
@MultipartConfig
public class UploadServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger(UploadServlet.class.getName());

  @EJB
  UiServiceBean uiBean;
  
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
    String formName = HttpUtils.readPart(req, Service.VAR_FORM_NAME);
    String design = HttpUtils.readPart(req, Service.VAR_FILE_NAME);
    
    ResponseObject response = uiBean.importForm(formName, BeeUtils.trim(design));
    Assert.notNull(response);
    
    resp.setContentType("text/html;charset=UTF-8");
    
    try {
      PrintWriter writer = resp.getWriter();    

      if (response.hasErrors()) {
        for (String message : response.getErrors()) {
          writer.println(message);
        }
      } else if (response.hasResponse(String.class)) {
        writer.print((String) response.getResponse());
      } else {
        writer.println("unknown response type");
      }
      
      writer.flush();    
    } catch (IOException ex) {
      LogUtils.stack(logger, ex);
    }
  }
}
