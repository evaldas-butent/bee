package com.butent.bee.egg.client;

import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.egg.client.logging.LogArea;
import com.butent.bee.egg.client.logging.LogFormatter;
import com.butent.bee.egg.client.logging.LogWidgetHandler;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BeeLog implements BeeModule {
  private Logger logger = null;
  private LogArea area = null;

  public BeeLog() {
    super();

    setLogger(Logger.getLogger(BeeConst.STRING_EMPTY));
    setArea(new LogArea());
    addArea(getArea());

    setLevel(Level.FINEST);
  }

  public void addSeparator() {
    getLogger().log(LogFormatter.LOG_SEPARATOR_LEVEL,
        LogFormatter.LOG_SEPARATOR_TAG);
  }

  public void clear() {
    getArea().clear();
  }

  public void end() {
  }

  public void finish(BeeDuration dur, Object... obj) {
    Assert.notNull(dur);

    dur.finish();
    if (obj.length > 0) {
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

  public String getName() {
    return getClass().getName();
  }

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

  public void info(Object... obj) {
    LogUtils.info(getLogger(), obj);
  }

  public void init() {
  }

  public void setArea(LogArea area) {
    this.area = area;
  }

  public void setLevel(Level lvl) {
    getLogger().setLevel(lvl);
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void severe(Object... obj) {
    LogUtils.severe(getLogger(), obj);
  }

  public void stack() {
    Throwable err = new Throwable();
    err.fillInStackTrace();
    LogUtils.stack(getLogger(), err);
  }

  public void start() {
  }

  public void warning(Object... obj) {
    LogUtils.warning(getLogger(), obj);
  }

  private void addArea(HasWidgets p) {
    if (p != null) {
      getLogger().addHandler(new LogWidgetHandler(p));
    }
  }

}
