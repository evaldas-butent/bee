package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.client.ui.HasDimensions;

public class ModalForm extends Popup {
  
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
    super(false);
    this.requiresUnload = requiresUnload;

    if (Stacking.getWidgetCount() <= 0) {
      enableGlass();
    }
    setAnimationEnabled(true);

    widget.addStyleName(getDefaultStyleName() + "-content");
    setWidget(widget);
    
    if (dimensions != null) {
      setDimensions(dimensions);
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
  protected String getDefaultStyleName() {
    return "bee-ModalForm";
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
