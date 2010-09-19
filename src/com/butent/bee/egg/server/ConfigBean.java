package com.butent.bee.egg.server;

import com.butent.bee.egg.shared.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
@Lock(LockType.READ)
public class ConfigBean {
  private static String resource = "server.properties";

  private static Logger logger = Logger.getLogger(ConfigBean.class.getName());
  private Properties properties = new Properties();

  public String getProperty(String key) {
    Assert.notEmpty(key);
    return properties.getProperty(key);
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    InputStream inp = getClass().getResourceAsStream(resource);

    if (inp == null) {
      LogUtils.warning(logger, resource, "not found");
      return;
    }

    try {
      properties.load(inp);
      inp.close();
      LogUtils.infoNow(logger, properties.size(), "properties loaded from", resource);
    } catch (IOException ex) {
      LogUtils.error(logger, ex, resource);
    }
  }

}
