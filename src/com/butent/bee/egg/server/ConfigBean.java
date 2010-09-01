package com.butent.bee.egg.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.butent.bee.egg.shared.utils.BeeUtils;

@Singleton
@Startup
@Lock(LockType.READ)
public class ConfigBean {
  private static final String PROPERTIES_FILE = "/com/butent/bee/egg/server/server.properties";

  private Properties properties = new Properties();
  private static Logger logger = Logger.getLogger(ConfigBean.class.getName());

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    InputStream inp = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(PROPERTIES_FILE);

    if (inp == null) {
      logger.warning(BeeUtils.concat(1, PROPERTIES_FILE, "not found",
          new Date()));
      return;
    }

    try {
      properties.load(inp);
      inp.close();
      logger.info(BeeUtils.concat(1, properties.size(),
          "properties loaded from", PROPERTIES_FILE));
    } catch (IOException ex) {
      logger.log(Level.SEVERE, PROPERTIES_FILE, ex);
    }
  }

  public String getProperty(String key) {
    Assert.notEmpty(key);
    return properties.getProperty(key);
  }

}
