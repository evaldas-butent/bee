package com.butent.bee.client.layout;

import com.google.common.collect.Lists;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.dom.Rulers;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.ui.Orientation;

import java.util.List;

public class Layout {

  public enum Alignment {
    BEGIN, END, STRETCH;
  }

  public interface AnimationCallback {

    void onAnimationComplete();

    void onLayout(Layer layer, double progress);
  }

  class Layer {
    final Element container, child;
    Object userObject;

    boolean setLeft;
    boolean setRight;
    boolean setTop;
    boolean setBottom;
    boolean setWidth;
    boolean setHeight;
    
    boolean setTargetLeft = true;
    boolean setTargetRight = true;
    boolean setTargetTop = true;
    boolean setTargetBottom = true;
    boolean setTargetWidth;
    boolean setTargetHeight;
    
    CssUnit leftUnit;
    CssUnit topUnit;
    CssUnit rightUnit;
    CssUnit bottomUnit;
    CssUnit widthUnit;
    CssUnit heightUnit;
    
    CssUnit targetLeftUnit = CssUnit.PX;
    CssUnit targetTopUnit = CssUnit.PX;
    CssUnit targetRightUnit = CssUnit.PX;
    CssUnit targetBottomUnit = CssUnit.PX;
    CssUnit targetWidthUnit;
    CssUnit targetHeightUnit;
    
    double left;
    double top;
    double right;
    double bottom;
    double width;
    double height;
    
    double sourceLeft;
    double sourceTop;
    double sourceRight;
    double sourceBottom;
    double sourceWidth;
    double sourceHeight;
    
    double targetLeft;
    double targetTop;
    double targetRight;
    double targetBottom;
    double targetWidth;
    double  targetHeight;

    Alignment hPos = Alignment.STRETCH;
    Alignment vPos = Alignment.STRETCH;

    boolean visible = true;

    Layer(Element container, Element child, Object userObject) {
      this.container = container;
      this.child = child;
      this.userObject = userObject;
    }

    Element getContainerElement() {
      return container;
    }

    Object getUserObject() {
      return this.userObject;
    }

    void setBottomHeight(double bottom, CssUnit bottomUnit, double height, CssUnit heightUnit) {
      this.setTargetBottom = true;
      this.setTargetHeight = true;
      this.setTargetTop = false;
      
      this.targetBottom = bottom;
      this.targetHeight = height;
      this.targetBottomUnit = bottomUnit;
      this.targetHeightUnit = heightUnit;
    }

    void setChildHorizontalPosition(Alignment position) {
      this.hPos = position;
    }

    void setChildVerticalPosition(Alignment position) {
      this.vPos = position;
    }

    void setLeftRight(double left, CssUnit leftUnit, double right, CssUnit rightUnit) {
      this.setTargetLeft = true;
      this.setTargetRight = true;
      this.setTargetWidth = false;
      
      this.targetLeft = left;
      this.targetRight = right;
      this.targetLeftUnit = leftUnit;
      this.targetRightUnit = rightUnit;
    }

    void setLeftWidth(double left, CssUnit leftUnit, double width, CssUnit widthUnit) {
      this.setTargetLeft = true;
      this.setTargetWidth = true;
      this.setTargetRight = false;
      
      this.targetLeft = left;
      this.targetWidth = width;
      this.targetLeftUnit = leftUnit;
      this.targetWidthUnit = widthUnit;
    }

    void setRightWidth(double right, CssUnit rightUnit, double width, CssUnit widthUnit) {
      this.setTargetRight = true;
      this.setTargetWidth = true;
      this.setTargetLeft = false;
      
      this.targetRight = right;
      this.targetWidth = width;
      this.targetRightUnit = rightUnit;
      this.targetWidthUnit = widthUnit;
    }

    void setTopBottom(double top, CssUnit topUnit, double bottom, CssUnit bottomUnit) {
      this.setTargetTop = true;
      this.setTargetBottom = true;
      this.setTargetHeight = false;
      
      this.targetTop = top;
      this.targetBottom = bottom;
      this.targetTopUnit = topUnit;
      this.targetBottomUnit = bottomUnit;
    }

    void setTopHeight(double top, CssUnit topUnit, double height, CssUnit heightUnit) {
      this.setTargetTop = true;
      this.setTargetHeight = true;
      this.setTargetBottom = false;
      
      this.targetTop = top;
      this.targetHeight = height;
      this.targetTopUnit = topUnit;
      this.targetHeightUnit = heightUnit;
    }

    void setVisible(boolean visible) {
      this.visible = visible;
    }
  }

  private final LayoutImpl impl = new LayoutImpl();

  private final List<Layer> layers = Lists.newArrayList();
  private final Element parentElem;
  
  private Animation animation = null;

  Layout(Element parent) {
    this.parentElem = parent;
    impl.initParent(parent);
  }

  Layer attachChild(Element child) {
    return attachChild(child, null);
  }

  Layer attachChild(Element child, Element before) {
    return attachChild(child, before, null);
  }

  Layer attachChild(Element child, Element before, Object userObject) {
    Element container = impl.attachChild(parentElem, child, before);
    Layer layer = new Layer(container, child, userObject);
    layers.add(layer);
    return layer;
  }

  Layer attachChild(Element child, Object userObject) {
    return attachChild(child, null, userObject);
  }

  void fillParent() {
    impl.fillParent(parentElem);
  }

  void layout() {
    layout(0);
  }

  void layout(int duration) {
    layout(duration, null);
  }

  void layout(int duration, final AnimationCallback callback) {
    if (animation != null) {
      animation.cancel();
    }

    if (duration == 0) {
      for (Layer l : layers) {
        l.left = l.sourceLeft = l.targetLeft;
        l.top = l.sourceTop = l.targetTop;
        l.right = l.sourceRight = l.targetRight;
        l.bottom = l.sourceBottom = l.targetBottom;
        l.width = l.sourceWidth = l.targetWidth;
        l.height = l.sourceHeight = l.targetHeight;

        l.setLeft = l.setTargetLeft;
        l.setTop = l.setTargetTop;
        l.setRight = l.setTargetRight;
        l.setBottom = l.setTargetBottom;
        l.setWidth = l.setTargetWidth;
        l.setHeight = l.setTargetHeight;

        l.leftUnit = l.targetLeftUnit;
        l.topUnit = l.targetTopUnit;
        l.rightUnit = l.targetRightUnit;
        l.bottomUnit = l.targetBottomUnit;
        l.widthUnit = l.targetWidthUnit;
        l.heightUnit = l.targetHeightUnit;

        impl.layout(l);
      }

      if (callback != null) {
        callback.onAnimationComplete();
      }
      return;
    }

    int parentWidth = parentElem.getClientWidth();
    int parentHeight = parentElem.getClientHeight();
    for (Layer l : layers) {
      adjustHorizontalConstraints(parentWidth, l);
      adjustVerticalConstraints(parentHeight, l);
    }

    animation = new Animation() {
      @Override
      protected void onCancel() {
        onComplete();
      }

      @Override
      protected void onComplete() {
        animation = null;
        layout();
        if (callback != null) {
          callback.onAnimationComplete();
        }
      }

      @Override
      protected void onUpdate(double progress) {
        for (Layer l : layers) {
          if (l.setTargetLeft) {
            l.left = l.sourceLeft + (l.targetLeft - l.sourceLeft) * progress;
          }
          if (l.setTargetRight) {
            l.right = l.sourceRight + (l.targetRight - l.sourceRight) * progress;
          }
          
          if (l.setTargetTop) {
            l.top = l.sourceTop + (l.targetTop - l.sourceTop) * progress;
          }
          if (l.setTargetBottom) {
            l.bottom = l.sourceBottom + (l.targetBottom - l.sourceBottom) * progress;
          }

          if (l.setTargetWidth) {
            l.width = l.sourceWidth + (l.targetWidth - l.sourceWidth) * progress;
          }
          if (l.setTargetHeight) {
            l.height = l.sourceHeight + (l.targetHeight - l.sourceHeight) * progress;
          }

          impl.layout(l);
          if (callback != null) {
            callback.onLayout(l, progress);
          }
        }
      }
    };

    animation.run(duration, parentElem);
  }

  void removeChild(Layer layer) {
    impl.removeChild(layer.container, layer.child);
    layers.remove(layer);
  }

  private void adjustHorizontalConstraints(int parentWidth, Layer l) {
    Orientation orientation = Orientation.HORIZONTAL;
    
    double leftPx = l.left * getUnitSize(l.leftUnit, orientation);
    double rightPx = l.right * getUnitSize(l.rightUnit, orientation);
    double widthPx = l.width * getUnitSize(l.widthUnit, orientation);

    if (l.setLeft && !l.setTargetLeft) {
      l.setLeft = false;

      if (!l.setWidth) {
        l.setTargetWidth = true;
        l.sourceWidth = (parentWidth - (leftPx + rightPx)) 
            / getUnitSize(l.targetWidthUnit, orientation);
      } else {
        l.setTargetRight = true;
        l.sourceRight = (parentWidth - (leftPx + widthPx)) 
            / getUnitSize(l.targetRightUnit, orientation);
      }

    } else if (l.setWidth && !l.setTargetWidth) {
      l.setWidth = false;

      if (!l.setLeft) {
        l.setTargetLeft = true;
        l.sourceLeft = (parentWidth - (rightPx + widthPx)) 
            / getUnitSize(l.targetLeftUnit, orientation);
      } else {
        l.setTargetRight = true;
        l.sourceRight = (parentWidth - (leftPx + widthPx)) 
            / getUnitSize(l.targetRightUnit, orientation);
      }

    } else if (l.setRight && !l.setTargetRight) {
      l.setRight = false;

      if (!l.setWidth) {
        l.setTargetWidth = true;
        l.sourceWidth = (parentWidth - (leftPx + rightPx)) 
            / getUnitSize(l.targetWidthUnit, orientation);
      } else {
        l.setTargetLeft = true;
        l.sourceLeft = (parentWidth - (rightPx + widthPx)) 
            / getUnitSize(l.targetLeftUnit, orientation);
      }
    }

    l.setLeft = l.setTargetLeft;
    l.setRight = l.setTargetRight;
    l.setWidth = l.setTargetWidth;

    l.leftUnit = l.targetLeftUnit;
    l.rightUnit = l.targetRightUnit;
    l.widthUnit = l.targetWidthUnit;
  }

  private void adjustVerticalConstraints(int parentHeight, Layer l) {
    Orientation orientation = Orientation.VERTICAL;
    
    double topPx = l.top * getUnitSize(l.topUnit, orientation);
    double bottomPx = l.bottom * getUnitSize(l.bottomUnit, orientation);
    double heightPx = l.height * getUnitSize(l.heightUnit, orientation);

    if (l.setTop && !l.setTargetTop) {
      l.setTop = false;

      if (!l.setHeight) {
        l.setTargetHeight = true;
        l.sourceHeight = (parentHeight - (topPx + bottomPx)) 
            / getUnitSize(l.targetHeightUnit, orientation);
      } else {
        l.setTargetBottom = true;
        l.sourceBottom = (parentHeight - (topPx + heightPx))
            / getUnitSize(l.targetBottomUnit, orientation);
      }

    } else if (l.setHeight && !l.setTargetHeight) {
      l.setHeight = false;

      if (!l.setTop) {
        l.setTargetTop = true;
        l.sourceTop = (parentHeight - (bottomPx + heightPx)) 
            / getUnitSize(l.targetTopUnit, orientation);
      } else {
        l.setTargetBottom = true;
        l.sourceBottom = (parentHeight - (topPx + heightPx))
            / getUnitSize(l.targetBottomUnit, orientation);
      }

    } else if (l.setBottom && !l.setTargetBottom) {
      l.setBottom = false;

      if (!l.setHeight) {
        l.setTargetHeight = true;
        l.sourceHeight = (parentHeight - (topPx + bottomPx))
            / getUnitSize(l.targetHeightUnit, orientation);
      } else {
        l.setTargetTop = true;
        l.sourceTop = (parentHeight - (bottomPx + heightPx))
            / getUnitSize(l.targetTopUnit, orientation);
      }
    }

    l.setTop = l.setTargetTop;
    l.setBottom = l.setTargetBottom;
    l.setHeight = l.setTargetHeight;

    l.topUnit = l.targetTopUnit;
    l.bottomUnit = l.targetBottomUnit;
    l.heightUnit = l.targetHeightUnit;
  }

  private double getUnitSize(CssUnit unit, Orientation orientation) {
    if (unit == null) {
      return 1; 
    } else {
      return Rulers.getUnitSize(parentElem, unit, orientation);
    }
  }
}
