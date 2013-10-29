package com.butent.bee.server;

import com.butent.bee.server.http.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RootServlet extends LoginServlet {

  @Override
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    HttpUtils.sendResponse(resp, doDefault(req, null));
  }
}
