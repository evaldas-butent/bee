package com.butent.bee.client.decorator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Iterator;

public class DecoratedWidget extends Panel {

  private final Widget widget;

  public DecoratedWidget(Widget widget, Element element) {
    this.widget = widget;
    setElement(element);
    adopt(widget);
  }

  public Widget getWidget() {
    return widget;
  }

  @Override
  public Iterator<Widget> iterator() {
    return new Iterator<Widget>() {
      boolean first = true;

      public boolean hasNext() {
        return first;
      }

      public Widget next() {
        if (!first) {
          return null;
        }
        first = false;
        return widget;
      }

      public void remove() {
      }
    };
  }

  @Override
  public boolean remove(Widget child) {
    return false;
  }
}
