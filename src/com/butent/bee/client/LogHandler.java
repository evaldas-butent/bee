package com.butent.bee.client;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Split;
import com.butent.bee.client.logging.LogArea;
import com.butent.bee.client.logging.LogFormatter;
import com.butent.bee.client.logging.LogWidgetHandler;
import com.butent.bee.client.utils.Duration;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages appearance and content of the logger object.
 */
public class LogHandler implements Module {

  private final Logger logger;
  private final LogArea area;

  private int hiddenSize = BeeConst.UNDEF;

  public LogHandler() {
    super();

    this.logger = Logger.getLogger(BeeConst.STRING_EMPTY);
    this.area = new LogArea();

    this.logger.addHandler(new LogWidgetHandler(this.area));

    setLevel(Level.FINEST);
  }

  public void addSeparator() {
    getLogger().log(LogFormatter.LOG_SEPARATOR_LEVEL, LogFormatter.LOG_SEPARATOR_TAG);
  }

  public void clear() {
    getArea().clear();
  }

  public void debug(Object... obj) {
    getLogger().info(BeeUtils.joinWords(obj));
  }

  public void debugCollection(Collection<?> collection) {
    if (collection != null) {
      for (Object obj : collection) {
        getLogger().info((String) obj);
      }
    }
  }

  public void debugMap(Map<?, ?> map) {
    if (map != null) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        debug(entry.getKey(), entry.getValue());
      }
    }
  }

  public void debugWithSeparator(Object... obj) {
    debug(obj);
    addSeparator();
  }

  @Override
  public void end() {
  }

  public void finish(Duration dur, Object... obj) {
    Assert.notNull(dur);

    dur.finish();
    if (obj != null && obj.length > 0) {
      info(dur.toLog(), obj);
    } else {
      info(dur.toLog());
    }
  }

  public LogArea getArea() {
    return area;
  }

  public Logger getLogger() {
    return logger;
  }

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public int getSize() {
    Widget parent = getArea().getParent();
    return (parent instanceof Split) ? ((Split) parent).getWidgetSize(getArea()) : BeeConst.UNDEF;
  }

  public void hide() {
    resize(0);
  }

  public void info(Object... obj) {
    getLogger().info(BeeUtils.joinWords(obj));
  }

  @Override
  public void init() {
  }

  public boolean isEmpty() {
    return getArea().isEmpty();
  }

  public void log(Level level, Object... obj) {
    getLogger().log(level, BeeUtils.joinWords(obj));
  }

  public void resize(int size) {
    if (getArea() == null) {
      return;
    }

    Widget parent = getArea().getParent();
    if (parent instanceof Split) {
      if (size <= 0) {
        hiddenSize = getSize();
      } else {
        hiddenSize = BeeConst.UNDEF;
      }

      ((Split) parent).setWidgetSize(getArea(), size);
    }
  }

  public void setLevel(Level lvl) {
    getLogger().setLevel(lvl);
  }

  public void severe(Object... obj) {
    getLogger().severe(BeeUtils.joinWords(obj));
  }

  public void show() {
    if (hiddenSize > 0) {
      resize(hiddenSize);
    }
  }

  public void stack() {
    Throwable err = new Throwable();
    err.fillInStackTrace();
    getLogger().severe(err.toString());
  }

  @Override
  public void start() {
  }

  public void warning(Object... obj) {
    getLogger().warning(BeeUtils.joinWords(obj));
  }
}
