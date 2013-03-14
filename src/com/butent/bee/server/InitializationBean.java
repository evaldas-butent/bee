package com.butent.bee.server;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.logging.LogbackFactory;
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
  ModuleHolderBean moduleBean;
  @EJB
  UserServiceBean usr;

  @PostConstruct
  public void init() {
    LogUtils.setLoggerFactory(new LogbackFactory());
    Config.init();

    moduleBean.initModules();

    usr.initUsers();
    usr.initRights();
  }
}
