package com.butent.bee.server;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Destroys user sessions after configured amount of idle time.
 */

@WebListener
public class ServletSessionListener implements HttpSessionListener {

  private static BeeLogger logger = LogUtils.getLogger(ServletSessionListener.class);

  @EJB
  DispatcherBean dispatcher;

  @Override
  public void sessionCreated(HttpSessionEvent se) {
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    Object loginName = se.getSession().getAttribute(Service.VAR_USER);

    if (loginName != null) {
      try {
        dispatcher.doLogout((String) loginName);
      } catch (EJBException e) {
        logger.warning(e.getMessage());
      }
    }
  }
}
