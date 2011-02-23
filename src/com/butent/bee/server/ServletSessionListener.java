package com.butent.bee.server;

import com.butent.bee.shared.BeeService;

import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class ServletSessionListener implements HttpSessionListener {

  @EJB
  DispatcherBean dispatcher;

  @Override
  public void sessionCreated(HttpSessionEvent se) {
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    Object loginName = se.getSession().getAttribute(BeeService.VAR_LOGIN);

    if (loginName != null) {
      dispatcher.doLogout((String) loginName);
    }
  }
}
