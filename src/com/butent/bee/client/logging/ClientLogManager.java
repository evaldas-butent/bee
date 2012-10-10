package com.butent.bee.client.logging;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Settings;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerFactory;
import com.butent.bee.shared.logging.LogLevel;

import java.util.logging.Level;

public class ClientLogManager implements BeeLoggerFactory {
  
  private static final ClientLogger rootLogger = createLogger();
  
  public static void clearPanel() {
    getPanelHandler().clear();
  }

  public static Widget getLogPanel() {
    return getPanelHandler().getPanel();
  }

  public static void setPanelSize(int size) {
    getPanelHandler().resize(size);
  }
  
  public static void setPanelVisible(boolean visible) {
    getPanelHandler().setVisible(visible);
  }

  private static ClientLogger createLogger() {
    ClientLogger logger = new ClientLogger(BeeConst.STRING_EMPTY);
    
    LogLevel logLevel = Settings.getLogLevel();
    Level level = (logLevel == null) ? Level.FINEST : logLevel.getLevel();

    logger.addHandler(new PanelHandler(level));
    return logger;
  }

  private static PanelHandler getPanelHandler() {
    return rootLogger.getPanelHandler();
  }
  
  public ClientLogManager() {
  }

  @Override
  public BeeLogger getLogger(String name) {
    return rootLogger;
  }
}
