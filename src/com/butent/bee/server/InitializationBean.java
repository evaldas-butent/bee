package com.butent.bee.server;

import com.butent.bee.server.communication.ChatBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.logging.LogbackFactory;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.ui.UiHolderBean;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
  @EJB
  ParamHolderBean prm;
  @EJB
  ChatBean chat;
  @EJB
  UiHolderBean ui;

  @PostConstruct
  public void init() {
    Config.setInitialized(false);

    LogUtils.setLoggerFactory(new LogbackFactory());
    Config.init();

    Locale locale = Localizations.getDefaultLocale();

    Localized.setConstants(Localizations.getConstants(locale));
    Localized.setMessages(Localizations.getMessages(locale));
    Localized.setDictionary(Localizations.getDictionary(locale));

    sys.init();

    prm.init();
    moduleBean.getModules().forEach((moduleName) -> prm.refreshParameters(moduleName));

    Map<String, String> props = prm.getMap(AdministrationConstants.PRM_SERVER_PROPERTIES);

    if (!BeeUtils.isEmpty(props)) {
      props.forEach((prop, value) -> Config.setProperty(prop, value));
    }
    sys.initViews();
    chat.init();

    moduleBean.initModules();

    usr.initRights();
    usr.initUsers();
    usr.initIpFilters();

    ui.init();

    Config.setInitialized(true);
  }

  @PreDestroy
  public void stop() {
    LogUtils.stopLogger();
  }
}
