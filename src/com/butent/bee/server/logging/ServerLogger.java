package com.butent.bee.server.logging;

import com.butent.bee.server.Config;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils.LogLevel;
import com.butent.bee.shared.utils.BeeUtils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.util.Properties;

public class ServerLogger implements BeeLogger {

  private static final String LOG4J_PROPERTIES = "log4j.properties";
  private static boolean loadedConfiguration = false;
  private static boolean busy = false;

  public static BeeLogger create(String name) {
    if (busy) {
      return null;
    }
    return new ServerLogger(name);
  }

  private final Logger logger;

  public ServerLogger(String name) {
    if (!loadedConfiguration) {
      busy = true;

      Properties props = Config.loadProperties(LOG4J_PROPERTIES);

      if (props != null) {
        props.setProperty("log_dir", new File(Config.USER_DIR, "logs").getPath());
        PropertyConfigurator.configure(props);
      } else {
        BasicConfigurator.configure();
      }
      loadedConfiguration = true;
      busy = false;
    }
    logger = Logger.getLogger(name);
  }

  @Override
  public void debug(Object... messages) {
    if (logger.isEnabledFor(Level.DEBUG)) {
      logger.debug(BeeUtils.joinWords(messages));
    }
  }

  @Override
  public void error(Object... messages) {
    if (logger.isEnabledFor(Level.ERROR)) {
      logger.error(BeeUtils.joinWords(messages));
    }
  }

  @Override
  public void error(Throwable ex, Object... messages) {
    if (logger.isEnabledFor(Level.ERROR)) {
      logger.error(BeeUtils.joinWords(messages), ex);
    }
  }

  @Override
  public void info(Object... messages) {
    if (logger.isEnabledFor(Level.INFO)) {
      logger.info(BeeUtils.joinWords(messages));
    }
  }

  @Override
  public void log(LogLevel level, Object... messages) {
    switch (level) {
      case DEBUG:
        debug(messages);
        break;
      case ERROR:
        error(messages);
        break;
      case INFO:
        info(messages);
        break;
      case WARNING:
        warning(messages);
        break;
    }
  }

  @Override
  public void warning(Object... messages) {
    if (logger.isEnabledFor(Level.WARN)) {
      logger.warn(BeeUtils.joinWords(messages));
    }
  }
}
