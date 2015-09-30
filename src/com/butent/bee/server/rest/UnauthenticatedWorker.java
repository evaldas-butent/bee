package com.butent.bee.server.rest;

import com.butent.bee.shared.utils.BeeUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path(EntryPoint.ENTRY)
public class UnauthenticatedWorker {

  @GET
  @Path("licence/{licence}")
  public String entry(@PathParam("licence") String licence) {
    return BeeUtils.joinWords("Path:", licence);
  }
}
