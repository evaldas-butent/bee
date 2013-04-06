package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Size;

public class ElementSize {

  private enum Origin {
    CLIENT {
      @Override
      int getHeight(Element element) {
        return element.getClientHeight();
      }

      @Override
      int getWidth(Element element) {
        return element.getClientWidth();
      }
    },
    OFFSET {
      @Override
      int getHeight(Element element) {
        return element.getOffsetHeight();
      }

      @Override
      int getWidth(Element element) {
        return element.getOffsetWidth();
      }
    },
    SCROLL {
      @Override
      int getHeight(Element element) {
        return element.getScrollHeight();
      }

      @Override
      int getWidth(Element element) {
        return element.getScrollWidth();
      }
    };

    abstract int getHeight(Element element);

    abstract int getWidth(Element element);
  }

  public static void copyScroll(Element source, Element target) {
    Assert.notNull(source);
    Assert.notNull(target);

    forScroll(source).applyTo(target);
  }

  public static void copyWithAdjustment(Element source, Element target, Element adjustment) {
    Assert.notNull(source);
    Assert.notNull(target);
    Assert.notNull(adjustment);

    Size delta = forScroll(adjustment).getSize().minus(forClient(adjustment).getSize());
    StyleUtils.setSize(target, forClient(source).getSize().plus(delta));
  }

  public static ElementSize forClient(Element element) {
    return new ElementSize(Origin.CLIENT, Assert.notNull(element));
  }

  public static ElementSize forClient(UIObject obj) {
    return forClient(Assert.notNull(obj).getElement());
  }

  public static ElementSize forOffset(Element element) {
    return new ElementSize(Origin.OFFSET, Assert.notNull(element));
  }

  public static ElementSize forOffset(UIObject obj) {
    return forOffset(Assert.notNull(obj).getElement());
  }

  public static ElementSize forScroll(Element element) {
    return new ElementSize(Origin.SCROLL, Assert.notNull(element));
  }

  public static ElementSize forScroll(UIObject obj) {
    return forScroll(Assert.notNull(obj).getElement());
  }

  private final Origin origin;

  private final int width;
  private final int height;

  private ElementSize(Origin origin, Element element) {
    this.origin = origin;

    this.width = origin.getWidth(element);
    this.height = origin.getHeight(element);
  }

  public void applyTo(Element element) {
    StyleUtils.setSize(element, getSize());
  }

  public Size getSize() {
    return new Size(width, height);
  }

  public boolean sameHeight(Element element) {
    return height == origin.getHeight(Assert.notNull(element));
  }

  public boolean sameHeight(UIObject obj) {
    return sameHeight(Assert.notNull(obj).getElement());
  }

  public boolean sameWidth(Element element) {
    return width == origin.getWidth(Assert.notNull(element));
  }

  public boolean sameWidth(UIObject obj) {
    return sameWidth(Assert.notNull(obj).getElement());
  }
}
