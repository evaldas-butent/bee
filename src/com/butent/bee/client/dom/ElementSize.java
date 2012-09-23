package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.shared.Assert;

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
