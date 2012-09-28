package com.butent.bee.server;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.logging.ServerLoggerFactory;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.shared.logging.LogUtils;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class InitializationBean {

  @EJB
  UserServiceBean usr;
  @EJB
  ModuleHolderBean moduleBean;

  @PostConstruct
  public void init() {
    LogUtils.setLoggerFactory(new ServerLoggerFactory());
    Config.init();

    moduleBean.initModules();

    usr.initUsers();
    usr.initRights();
  }
}
