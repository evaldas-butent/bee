package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.html.Tags;
import com.butent.bee.shared.utils.BeeUtils;

public class ModalForm extends Popup {
  
  private static final String STYLE_NAME = "bee-ModalForm";
  
  private final boolean requiresUnload; 
  
  private boolean wasAttached;
  private boolean pendingUnload;

  public ModalForm(Widget widget, HasDimensions dimensions, boolean requiresUnload) {
    super(OutsideClick.IGNORE, STYLE_NAME);
    this.requiresUnload = requiresUnload;
    
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
      Element el = Element.as(target);
      return header.asWidget().getElement().isOrHasChild(el)
          && BeeUtils.inListSame(el.getTagName(), Tags.DIV, Tags.SPAN);
    } else {
      return false;
    }
  }
  
  private void setDimensions(HasDimensions dimensions) {
    double v;
    CssUnit u;

    if (dimensions.getWidthValue() != null) {
      v = dimensions.getWidthValue();
      u = Dimensions.normalizeUnit(dimensions.getWidthUnit());
      if (CssUnit.PCT.equals(u)) {
        v = Window.getClientWidth() * v / 100;
        u = CssUnit.PX;
      }
    } else {
      v = Window.getClientWidth() / 2;
      u = CssUnit.PX;
    }
    if (v > 0) {
      StyleUtils.setWidth(getElement(), v, u);
    }

    if (dimensions.getHeightValue() != null) {
      v = dimensions.getHeightValue();
      u = Dimensions.normalizeUnit(dimensions.getHeightUnit());
      if (CssUnit.PCT.equals(u)) {
        v = Window.getClientHeight() * v / 100;
        u = CssUnit.PX;
      }
    } else {
      v = Window.getClientHeight() / 2;
      u = CssUnit.PX;
    }
    if (v > 0) {
      StyleUtils.setHeight(getElement(), v, u);
    }
  }
}
