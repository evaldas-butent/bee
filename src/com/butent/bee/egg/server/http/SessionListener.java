package com.butent.bee.egg.server.http;

import com.butent.bee.egg.server.concurrency.Counter;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {
  private String attrCnt = null;

  public SessionListener() {
    super();
    attrCnt = HttpConst.ATTRIBUTE_SESSION_COUNTER;
  }

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    se.getSession().setAttribute(attrCnt, new Counter());
    se.getSession().getServletContext().log("session created");
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    se.getSession().getServletContext().log(
        "session destroyed "
            + HttpUtils.counterInfo(attrCnt,
                se.getSession().getAttribute(attrCnt)));
    se.getSession().removeAttribute(attrCnt);
  }

}
