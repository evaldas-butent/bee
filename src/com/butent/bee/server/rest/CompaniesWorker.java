package com.butent.bee.server.rest;

import javax.ws.rs.Path;

@Path("companies")
public class CompaniesWorker extends CRUDtmpWorker {

  @Override
  protected String getViewName() {
    return "RestCompanies";
  }
}
