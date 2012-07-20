package com.butent.bee.client.logging;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.Settings;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.BeeConst;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Manages content of log information showing user interface component.
 */

public class LogWidgetHandler extends Handler {

  private static final String STYLENAME_DEFAULT = "bee-LogRecord";
  private static final String STYLENAME_SEPARATOR = "bee-LogSeparator";

  public static Formatter getDefaultFormatter() {
    return new LogFormatter();
  }

  public static Level getDefaultLevel() {
    return Level.ALL;
  }

  private final HasIndexedWidgets container;
  
  private final int capacity;

  public LogWidgetHandler(HasIndexedWidgets container) {
    this(container, getDefaultFormatter(), getDefaultLevel());
  }

  public LogWidgetHandler(HasIndexedWidgets container, Formatter formatter, Level level) {
    this.container = container;
    this.capacity = Settings.getLogCapacity(); 

    setFormatter(formatter);
    setLevel(level);
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
    
    if (capacity > 0 && container.getWidgetCount() >= capacity) {
      container.remove(0);
    }

    if (frmt instanceof LogFormatter && ((LogFormatter) frmt).isSeparator(record)) {
      Element elem = Document.get().createDivElement().cast();
      container.add(new Html(elem));
      elem.setClassName(STYLENAME_SEPARATOR);
      elem.scrollIntoView();

      return;
    }

    String msg = frmt.format(record);

    Element elem = Document.get().createDivElement().cast();
    elem.setInnerText(msg);
    elem.setClassName(STYLENAME_DEFAULT);
    elem.addClassName(STYLENAME_DEFAULT + BeeConst.STRING_MINUS
        + record.getLevel().getName().toLowerCase());

    container.add(new Html(elem));
  }

  public void setDefaultFormatter() {
    setFormatter(getDefaultFormatter());
  }

  public void setDefaultLevel() {
    setLevel(getDefaultLevel());
  }
}
