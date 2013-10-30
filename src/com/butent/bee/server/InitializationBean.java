package com.butent.bee.server;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.logging.LogbackFactory;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;

import java.util.Locale;

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
    
    Locale locale = Localizations.getDefaultLocale();

    Localized.setConstants(Localizations.getConstants(locale));
    Localized.setMessages(Localizations.getMessages(locale));

    Localized.setDictionary(Localizations.getDictionary(locale));

    moduleBean.initModules();

    usr.initUsers();
    usr.initRights();
  }
}
