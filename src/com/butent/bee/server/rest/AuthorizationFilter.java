package com.butent.bee.server.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@PreMatching
@Provider
public class AuthorizationFilter implements ContainerRequestFilter {

  @Context
  HttpServletRequest req;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (req.getUserPrincipal() == null) {
      throw new NotAuthorizedException("B-NOVO");
    }
  }
}
