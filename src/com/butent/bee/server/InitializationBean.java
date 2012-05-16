package com.butent.bee.server;

import com.google.common.eventbus.Subscribe;

import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEvent.ViewModifyEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.modules.ModuleHolderBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class InitializationBean {

  @EJB
  ModuleHolderBean moduleBean;
  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void invalidateUserCache(ViewModifyEvent event) {
        if (usr.isUserTable(event.getViewName()) && event.isAfter()) {
          usr.invalidateCache();
        }
      }
    });
    moduleBean.initModules();
  }
}
