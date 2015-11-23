package com.butent.bee.server.rest;

import javax.ws.rs.Path;

@Path("companypersons")
public class CompanyPersonsWorker extends CRUDWorker {

  @Override
  protected String getViewName() {
    return "RestCompanyPersons";
  }
}
