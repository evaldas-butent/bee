package com.butent.bee.egg.client.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.butent.bee.egg.client.widget.BeeHtml;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasWidgets;

public class LogWidgetHandler extends Handler {
  private HasWidgets container;
  private int counter = 0;

  public LogWidgetHandler() {
    super();
  }

  public LogWidgetHandler(HasWidgets cont, Formatter format, Level lvl) {
    this.container = cont;
    setFormatter(format);
    setLevel(lvl);
  }

  public LogWidgetHandler(HasWidgets cont, Formatter format) {
    this(cont, format, getDefaultLevel());
  }

  public LogWidgetHandler(HasWidgets cont, Level lvl) {
    this(cont, getDefaultFormatter(), lvl);
  }

  public LogWidgetHandler(HasWidgets cont) {
    this(cont, getDefaultFormatter(), getDefaultLevel());
  }

  public static Formatter getDefaultFormatter() {
    return new LogFormatter();
  }

  public void setDefaultFormatter() {
    setFormatter(getDefaultFormatter());
  }

  public static Level getDefaultLevel() {
    return Level.ALL;
  }

  public void setDefaultLevel() {
    setLevel(getDefaultLevel());
  }

  public void clear() {
    container.clear();
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  @Override
  public void publish(LogRecord record) {
    if (!isLoggable(record))
      return;

    Formatter frmt = getFormatter();
    if (frmt == null)
      return;

    if (frmt instanceof LogFormatter
        && ((LogFormatter) frmt).isSeparator(record)) {
      container.add(new BeeHtml(Document.get().createHRElement()));
      return;
    }

    String msg = frmt.format(record);
    if (!BeeUtils.isEmpty(msg))
      counter++;
    container.add(new BeeLabel(BeeUtils.concat(1, counter, msg)));
  }
}
