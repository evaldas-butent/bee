package com.butent.bee.egg.server.datasource;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public abstract class DataSourceServlet extends HttpServlet implements DataTableGenerator {

  @Override
  public Capabilities getCapabilities() {
    return Capabilities.NONE;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    DataSourceHelper.executeDataSourceServletFlow(req, resp, this, isRestrictedAccessMode());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    doGet(req, resp);
  }

  protected boolean isRestrictedAccessMode() {
    return true;
  }
}
