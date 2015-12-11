package com.butent.bee.server.rest;

import javax.json.JsonObject;
import javax.ws.rs.Path;

@Path("taskfiles")
public class TaskFilesWorker extends CrudWorker {

  @Override
  public RestResponse delete(Long id, Long version) {
    return RestResponse.forbidden();
  }

  @Override
  protected String getViewName() {
    return "RestTaskFiles";
  }

  @Override
  public RestResponse update(Long id, Long version, JsonObject data) {
    return RestResponse.forbidden();
  }
}
