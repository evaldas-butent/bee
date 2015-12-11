package com.butent.bee.server.rest;

import javax.ws.rs.Path;

@Path("taskusers")
public class TaskUsersWorker extends CrudWorker {

  @Override
  protected String getViewName() {
    return "RestTaskUsers";
  }
}
