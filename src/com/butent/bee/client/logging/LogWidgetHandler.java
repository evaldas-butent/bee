package com.butent.bee.client.logging;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogWidgetHandler extends Handler {
  private static final String STYLENAME_DEFAULT = "bee-LogRecord";
  private static final String STYLENAME_SEPARATOR = "bee-LogSeparator";

  public static Formatter getDefaultFormatter() {
    return new LogFormatter();
  }

  public static Level getDefaultLevel() {
    return Level.ALL;
  }

  private HasWidgets container;

  private int counter = 0;

  public LogWidgetHandler() {
    super();
  }

  public LogWidgetHandler(HasWidgets cont) {
    this(cont, getDefaultFormatter(), getDefaultLevel());
  }

  public LogWidgetHandler(HasWidgets cont, Formatter format) {
    this(cont, format, getDefaultLevel());
  }

  public LogWidgetHandler(HasWidgets cont, Formatter format, Level lvl) {
    this.container = cont;
    setFormatter(format);
    setLevel(lvl);
  }

  public LogWidgetHandler(HasWidgets cont, Level lvl) {
    this(cont, getDefaultFormatter(), lvl);
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
    if (!isLoggable(record)) {
      return;
    }

    Formatter frmt = getFormatter();
    if (frmt == null) {
      return;
    }

    if (frmt instanceof LogFormatter
        && ((LogFormatter) frmt).isSeparator(record)) {
      Element elem = Document.get().createDivElement().cast();
      container.add(new Html(elem));
      elem.setClassName(STYLENAME_SEPARATOR);
      elem.scrollIntoView();

      return;
    }

    String msg = frmt.format(record);
    if (!BeeUtils.isEmpty(msg)) {
      counter++;
    }

    Element elem = Document.get().createDivElement().cast();
    elem.setInnerText(BeeUtils.concat(1, counter, msg));
    container.add(new Html(elem));
    elem.setClassName(STYLENAME_DEFAULT);
    elem.addClassName(STYLENAME_DEFAULT + BeeConst.STRING_MINUS
        + record.getLevel().getName().toLowerCase());
  }

  public void setDefaultFormatter() {
    setFormatter(getDefaultFormatter());
  }

  public void setDefaultLevel() {
    setLevel(getDefaultLevel());
  }
}
