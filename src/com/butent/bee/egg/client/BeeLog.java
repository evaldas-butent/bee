package com.butent.bee.egg.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.butent.bee.egg.client.logging.LogArea;
import com.butent.bee.egg.client.logging.LogFormatter;
import com.butent.bee.egg.client.logging.LogWidgetHandler;
import com.butent.bee.egg.client.utils.BeeDuration;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.LogUtils;

import com.google.gwt.user.client.ui.HasWidgets;

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

  public Logger getLogger() {
    return logger;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public LogArea getArea() {
    return area;
  }

  public void setArea(LogArea area) {
    this.area = area;
  }

  public void setLevel(Level lvl) {
    getLogger().setLevel(lvl);
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

  public void init() {
  }

  public void start() {
  }

  public void end() {
  }

  public void info(String msg) {
    LogUtils.info(getLogger(), msg);
  }

  public void info(Object... obj) {
    LogUtils.info(getLogger(), obj);
  }

  public void warning(Object... obj) {
    LogUtils.warning(getLogger(), obj);
  }

  public void severe(Object... obj) {
    LogUtils.severe(getLogger(), obj);
  }
  
  public void addSeparator() {
    getLogger().log(LogFormatter.LOG_SEPARATOR_LEVEL,
        LogFormatter.LOG_SEPARATOR_TAG);
  }

  public void finish(BeeDuration dur, Object... obj) {
    Assert.notNull(dur);

    dur.finish();
    if (obj.length > 0)
      info(dur, obj);
    else
      info(dur.toString());
  }
  
  public void clear() {
    getArea().clear();
  }
  
  public void stack() {
    Throwable err = new Throwable();
    err.fillInStackTrace();
    LogUtils.stack(getLogger(), err);
  }

  private void addArea(HasWidgets p) {
    if (p != null)
      getLogger().addHandler(new LogWidgetHandler(p));
  }

}
