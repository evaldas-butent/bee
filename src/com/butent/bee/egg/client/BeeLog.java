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
  private Logger logger;
  private LogArea area;

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

  @Override
  public void init() {
  }

  @Override
  public void start() {
  }

  @Override
  public void end() {
  }

  private void addArea(HasWidgets p) {
    if (p != null)
      getLogger().addHandler(new LogWidgetHandler(p));
  }

  public void log(String msg) {
    LogUtils.info(getLogger(), msg);
  }

  public void log(Object... obj) {
    LogUtils.info(getLogger(), obj);
  }

  public void addSeparator() {
    getLogger().log(LogFormatter.LOG_SEPARATOR_LEVEL,
        LogFormatter.LOG_SEPARATOR_TAG);
  }

  public void finish(BeeDuration dur, Object... obj) {
    Assert.notNull(dur);

    dur.finish();
    if (obj.length > 0)
      log(dur, obj);
    else
      log(dur.toString());
  }

}
