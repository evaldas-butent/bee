package com.butent.bee.server.http;

import com.butent.bee.server.concurrency.Counter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initializes and destroys HTTP contexts.
 */
public class ContextListener implements ServletContextListener {
  private String attrCnt;

  public ContextListener() {
    super();
    attrCnt = HttpConst.ATTRIBUTE_CONTEXT_COUNTER;
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    sce.getServletContext().log(
        "context destroyed "
            + HttpUtils.counterInfo(attrCnt,
                sce.getServletContext().getAttribute(attrCnt)));
    sce.getServletContext().removeAttribute(attrCnt);
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    sce.getServletContext().setAttribute(attrCnt, new Counter());
    sce.getServletContext().log("context initialized");
  }
}
