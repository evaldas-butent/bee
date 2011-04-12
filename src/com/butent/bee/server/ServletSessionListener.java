package com.butent.bee.server;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.utils.LogUtils;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class ServletSessionListener implements HttpSessionListener {

  private static Logger logger = Logger.getLogger(ServletSessionListener.class.getName());

  @EJB
  DispatcherBean dispatcher;

  @Override
  public void sessionCreated(HttpSessionEvent se) {
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    Object loginName = se.getSession().getAttribute(Service.VAR_LOGIN);

    if (loginName != null) {
      try {
        dispatcher.doLogout((String) loginName);
      } catch (EJBException e) {
        LogUtils.warning(logger, e.getMessage());
      }
    }
  }
}
