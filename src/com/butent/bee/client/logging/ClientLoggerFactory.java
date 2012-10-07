package com.butent.bee.client.logging;

import com.butent.bee.client.Settings;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerFactory;
import com.butent.bee.shared.logging.LogLevel;

import java.util.logging.Level;

public class ClientLoggerFactory implements BeeLoggerFactory {
  
  private final ClientLogger logger;

  public ClientLoggerFactory() {
    this.logger = new ClientLogger(BeeConst.STRING_EMPTY);
    
    LogLevel logLevel = Settings.getLogLevel();
    Level level = (logLevel == null) ? Level.FINEST : logLevel.getLevel();

    logger.addHandler(new PanelHandler(level));
  }

  @Override
  public BeeLogger createLogger(String name) {
    return logger;
  }

  @Override
  public BeeLogger getLogger(String name) {
    return logger;
  }
}
