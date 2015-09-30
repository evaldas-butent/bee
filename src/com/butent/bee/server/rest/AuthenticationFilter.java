package com.butent.bee.server.rest;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@PreMatching
@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

  private static BeeLogger logger = LogUtils.getLogger(AuthenticationFilter.class);

  @Context
  HttpServletRequest req;
  @EJB
  UserServiceBean usr;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (BeeUtils.startsWith(requestContext.getUriInfo().getPath(), EntryPoint.ENTRY)) {
      return;
    }
    String user = requestContext.getHeaderString("usr");
    String password = requestContext.getHeaderString("pwd");
    boolean ok = BeeUtils.allNotEmpty(user, password);

    if (ok) {
      try {
        req.login(user, user);
      } catch (ServletException e1) {
        try {
          logger.info(user, "login failed, trying with password...");
          req.login(user, password);
        } catch (ServletException e2) {
          logger.error(e2);
          ok = false;
        }
      }
      req.getSession().invalidate();
    }
    if (ok && !usr.authenticateUser(user, Codec.encodePassword(password))) {
      try {
        req.logout();
      } catch (ServletException e) {
        logger.error(e);
      }
      ok = false;
    }
    if (!ok) {
      throw new NotAuthorizedException("B-NOVO");
    }
  }
}
