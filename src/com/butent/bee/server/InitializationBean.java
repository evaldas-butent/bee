package com.butent.bee.server;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class InitializationBean {

  private static final String LOG_LEVEL = "LogLevel";

  @EJB
  UserServiceBean usr;
  @EJB
  ModuleHolderBean moduleBean;

  @PostConstruct
  public void init() {
    setLogLevel();

    moduleBean.initModules();

    usr.initUsers();
    usr.initRights();
  }

  private void setLogLevel() {
    String logLevel = Config.getProperty(LOG_LEVEL);
    Level level = null;

    if (BeeUtils.same(logLevel, Level.OFF.toString())) {
      level = Level.OFF;
    } else if (BeeUtils.same(logLevel, Level.SEVERE.toString())) {
      level = Level.SEVERE;
    } else if (BeeUtils.same(logLevel, Level.WARNING.toString())) {
      level = Level.WARNING;
    } else if (BeeUtils.same(logLevel, Level.INFO.toString())) {
      level = Level.INFO;
    } else if (BeeUtils.same(logLevel, Level.CONFIG.toString())) {
      level = Level.CONFIG;
    } else if (BeeUtils.same(logLevel, Level.FINE.toString())) {
      level = Level.FINE;
    } else if (BeeUtils.same(logLevel, Level.FINER.toString())) {
      level = Level.FINER;
    } else if (BeeUtils.same(logLevel, Level.FINEST.toString())) {
      level = Level.FINEST;
    } else if (BeeUtils.same(logLevel, Level.ALL.toString())) {
      level = Level.ALL;
    }
    Logger.getLogger("").setLevel(level);
  }
}
