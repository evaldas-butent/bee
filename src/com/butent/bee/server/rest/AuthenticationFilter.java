package com.butent.bee.server.rest;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.rest.annotations.Authorized;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.IOException;
import java.util.Objects;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

  private static BeeLogger logger = LogUtils.getLogger(AuthenticationFilter.class);

  @Context
  HttpServletRequest req;
  @Context
  ResourceInfo info;
  @EJB
  UserServiceBean usr;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    Trusted trusted = info.getResourceMethod().getAnnotation(Trusted.class);

    if (Objects.nonNull(trusted) && (BeeUtils.isEmpty(trusted.secret())
        || Objects.equals(requestContext.getHeaderString("secret"),
        Codec.md5(TimeUtils.year() + trusted.secret() + TimeUtils.month())))) {
      return;
    }
    String[] split = BeeUtils.split(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION), ' ');
    String user = ArrayUtils.getQuietly(split, 0);
    String password = ArrayUtils.getQuietly(split, 1);

    boolean ok = BeeUtils.allNotEmpty(user, password);

    if (ok) {
      ok = login(req, user, password);

    } else if ((info.getResourceMethod().isAnnotationPresent(Authorized.class)
        || info.getResourceClass().isAnnotationPresent(Authorized.class))
        && Objects.nonNull(req.getUserPrincipal())) {
      return;
    }
    if (ok && (!usr.authenticateUser(user, password) || usr.isBlocked(user))) {
      try {
        req.logout();
      } catch (ServletException e) {
        logger.error(e);
      }
      ok = false;
    }
    if (!ok) {
      HttpSession session = req.getSession(false);

      if (session != null) {
        session.invalidate();
      }
      logger.warning(requestContext.getUriInfo().getRequestUri(), ArrayUtils.joinWords(split));
      throw new NotAuthorizedException("B-NOVO");
    }
  }

  public static boolean login(HttpServletRequest request, String user, String password) {
    if (Objects.nonNull(request.getUserPrincipal())) {
      try {
        request.logout();
      } catch (ServletException e) {
        logger.error(e);
      }
    }
    boolean ok = BeeUtils.allNotEmpty(user, password);

    if (ok) {
      try {
        request.login(user, user);
      } catch (ServletException e1) {
        try {
          request.login(user, password);
        } catch (ServletException e2) {
          logger.error(e2);
          ok = false;
        }
      }
    }
    return ok;
  }
}
