package com.butent.bee.server.modules.tasks;

import com.butent.bee.server.rest.EntryPoint;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path(EntryPoint.ENTRY)
public class RestfulTaskBuilder {

  @POST
  @Path("request")
  public String entry(@FormParam("name") String companyName,
      @FormParam("code") String companyCode, @FormParam("message") String message) {

    return BeeUtils.joinWords(companyName, companyCode, message);
  }
}
