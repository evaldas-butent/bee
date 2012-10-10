package com.butent.bee.server.logging;

import com.butent.bee.server.Config;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.BeeLoggerWrapper;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

public class ServerLogger implements BeeLogger {

  private static final String FQCN = BeeLoggerWrapper.class.getName();
  private static final String LOG4J_PROPERTIES = "log4j.xml";
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
      File cfg = new File(Config.USER_DIR, LOG4J_PROPERTIES);

      if (!FileUtils.isInputFile(cfg)) {
        cfg = new File(Config.CONFIG_DIR, LOG4J_PROPERTIES);
      }
      Document xmlProps = XmlUtils.fromFileName(cfg.getPath());

      if (xmlProps != null) {
        NodeList appenders = xmlProps.getElementsByTagName("appender");

        for (int i = 0; i < appenders.getLength(); i++) {
          NodeList childs = appenders.item(i).getChildNodes();

          for (int j = 0; j < childs.getLength(); j++) {
            Node child = childs.item(j);

            if (BeeUtils.same(child.getLocalName(), "param") && child.hasAttributes()) {
              Node prm = child.getAttributes().getNamedItem("name");

              if (prm != null && BeeUtils.same(prm.getNodeValue(), "file")) {
                prm = child.getAttributes().getNamedItem("value");
                prm.setNodeValue(new File(Config.LOG_DIR, prm.getNodeValue()).getPath());
                break;
              }
            }
          }
        }

        DOMConfigurator.configure(xmlProps.getDocumentElement());
      } else {
        BasicConfigurator.configure();
      }
      loadedConfiguration = true;
      busy = false;
    }
    logger = Logger.getLogger(name);
  }

  @Override
  public void addSeparator() {
  }

  @Override
  public void debug(Object... messages) {
    logInternal(Level.DEBUG, null, messages);
  }

  @Override
  public void error(Throwable ex, Object... messages) {
    logInternal(Level.ERROR, ex, messages);
  }

  @Override
  public void info(Object... messages) {
    logInternal(Level.INFO, null, messages);
  }

  @Override
  public void log(LogLevel level, Object... messages) {
    switch (level) {
      case DEBUG:
        debug(messages);
        break;
      case ERROR:
        severe(messages);
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
  public void severe(Object... messages) {
    logInternal(Level.ERROR, null, messages);
  }

  @Override
  public void warning(Object... messages) {
    logInternal(Level.WARN, null, messages);
  }

  private void logInternal(Level level, Throwable ex, Object... messages) {
    if (logger.isEnabledFor(level)) {
      logger.log(FQCN, level, ArrayUtils.joinWords(messages), ex);
    }
  }
}
