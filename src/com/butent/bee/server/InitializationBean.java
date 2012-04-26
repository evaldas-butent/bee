package com.butent.bee.server;

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

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    moduleBean.initModules();
  }
}
