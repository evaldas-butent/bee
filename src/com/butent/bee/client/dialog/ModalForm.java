package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;

public class ModalForm extends Popup {
  
  private static final String STYLE_NAME = "bee-ModalForm";
  
  private final boolean requiresUnload; 
  
  private boolean wasAttached = false;
  private boolean pendingUnload = false;

  public ModalForm(Widget widget) {
    this(widget, null);
  }

  public ModalForm(Widget widget, HasDimensions dimensions) {
    this(widget, dimensions, false);
  }
  
  public ModalForm(Widget widget, HasDimensions dimensions, boolean requiresUnload) {
    super(false, STYLE_NAME);
    this.requiresUnload = requiresUnload;

    if (Stacking.getWidgetCount() <= 0) {
      enableGlass();
    }
    setAnimationEnabled(true);

    widget.addStyleName(STYLE_NAME + "-content");
    setWidget(widget);
    
    if (dimensions != null) {
      setDimensions(dimensions);
    }
    if (ViewHelper.hasHeader(widget)) {
      enableDragging();
    }
  }

  public void close() {
    hide();
  }

  @Override
  public String getIdPrefix() {
    return "modal-form";
  }

  public void open() {
    center();
  }

  public void unload() {
    if (wasAttached) {
      pendingUnload = true;
      if (isShowing()) {
        close();
      } else {
        doDetachChildren();
      }
    }
  }
  
  @Override
  protected void doAttachChildren() {
    if (!requiresUnload || !wasAttached) {
      super.doAttachChildren();
      wasAttached = true;
    }
  }
  
  @Override
  protected void doDetachChildren() {
    if (!requiresUnload || pendingUnload) {
      super.doDetachChildren();
      wasAttached = false;
      pendingUnload = false;
    }
  }

  @Override
  protected boolean isCaptionEvent(NativeEvent event) {
    HeaderView header = ViewHelper.getHeader(getWidget());
    if (header == null) {
      return false;
    }

    EventTarget target = event.getEventTarget();
    if (Element.is(target)) {
      return header.asWidget().getElement().isOrHasChild(Element.as(target));
    } else {
      return false;
    }
  }

  private void setDimensions(HasDimensions dimensions) {
    double v;
    Unit u;

    if (dimensions.getWidthValue() != null) {
      v = dimensions.getWidthValue();
      u = Dimensions.normalizeUnit(dimensions.getWidthUnit());
      if (Unit.PCT.equals(u)) {
        v = Window.getClientWidth() * v / 100;
        u = Unit.PX;
      }
    } else {
      v = Window.getClientWidth() / 2;
      u = Unit.PX;
    }
    if (v > 0) {
      getElement().getStyle().setWidth(v, u);
    }

    if (dimensions.getHeightValue() != null) {
      v = dimensions.getHeightValue();
      u = Dimensions.normalizeUnit(dimensions.getHeightUnit());
      if (Unit.PCT.equals(u)) {
        v = Window.getClientHeight() * v / 100;
        u = Unit.PX;
      }
    } else {
      v = Window.getClientHeight() / 2;
      u = Unit.PX;
    }
    if (v > 0) {
      getElement().getStyle().setHeight(v, u);
    }
  }
}
