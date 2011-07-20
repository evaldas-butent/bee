package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Enables to manage a inline panel which contains only one child component.
 */

public class SimpleInline extends Panel implements HasOneWidget, HasId {

  private Widget widget;

  public SimpleInline() {
    this(DOM.createSpan());
  }

  public SimpleInline(Element elem) {
    setElement(elem);
    init();
  }

  public SimpleInline(Widget child) {
    this();
    setWidget(child);
  }

  @Override
  public void add(Widget w) {
    Assert.isNull(getWidget(), "Simple Inline Panel can only contain one child widget");
    setWidget(w);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "simple-inline";
  }

  public Widget getWidget() {
    return widget;
  }

  public Iterator<Widget> iterator() {
    return new Iterator<Widget>() {
      boolean hasElement = widget != null;
      Widget returned = null;

      public boolean hasNext() {
        return hasElement;
      }

      public Widget next() {
        if (!hasElement || (widget == null)) {
          throw new NoSuchElementException();
        }
        hasElement = false;
        return (returned = widget);
      }

      public void remove() {
        if (returned != null) {
          SimpleInline.this.remove(returned);
        }
      }
    };
  }

  @Override
  public boolean remove(Widget w) {
    if (widget != w) {
      return false;
    }

    try {
      orphan(w);
    } finally {
      getElement().removeChild(w.getElement());
      widget = null;
    }
    return true;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setWidget(IsWidget w) {
    setWidget(asWidgetOrNull(w));
  }

  public void setWidget(Widget w) {
    if (w == widget) {
      return;
    }

    if (w != null) {
      w.removeFromParent();
    }
    if (widget != null) {
      remove(widget);
    }

    widget = w;

    if (w != null) {
      DOM.appendChild(getElement(), widget.getElement());
      adopt(w);
    }
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
