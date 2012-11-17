package com.butent.bee.client.logging;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Settings;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.BeeConst;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Manages content of log information showing user interface component.
 */

public class PanelHandler extends Handler implements HasVisibility {

  private static final String STYLENAME_DEFAULT = "bee-LogRecord";
  private static final String STYLENAME_SEPARATOR = "bee-LogSeparator";

  private final Flow panel;

  private final int capacity;

  private int hiddenSize = BeeConst.UNDEF;

  public PanelHandler(Flow panel, Formatter formatter, Level level) {
    this.panel = panel;
    this.capacity = Settings.getLogCapacity();

    setFormatter(formatter);
    if (level != null) {
      setLevel(level);
    }
  }

  public PanelHandler(Level level) {
    this(new LogPanel(), new LogFormatter(), level);
  }

  public void clear() {
    panel.clear();
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  public Flow getPanel() {
    return panel;
  }

  public boolean isEmpty() {
    return panel.isEmpty();
  }

  @Override
  public boolean isVisible() {
    return getSize() > 0;
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

    if (capacity > 0 && panel.getWidgetCount() >= capacity) {
      panel.remove(0);
    }

    if (frmt instanceof LogFormatter && ((LogFormatter) frmt).isSeparator(record)) {
      Element elem = Document.get().createDivElement().cast();
      panel.add(new Html(elem));
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

    panel.add(new Html(elem));
  }

  public void resize(int size) {
    Widget parent = getPanel().getParent();
    if (parent instanceof Split) {
      if (size <= 0) {
        hiddenSize = getSize();
      } else {
        hiddenSize = BeeConst.UNDEF;
      }

      ((Split) parent).setWidgetSize(getPanel(), size);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      if (hiddenSize > 0) {
        resize(hiddenSize);
      }
    } else {
      resize(0);
    }
  }

  private int getSize() {
    Widget parent = getPanel().getParent();
    return (parent instanceof Split) ? ((Split) parent).getWidgetSize(getPanel()) : BeeConst.UNDEF;
  }
}
