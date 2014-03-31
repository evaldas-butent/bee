package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.view.ViewHelper;

public class ModalForm extends Popup {

  private static final String STYLE_NAME = "bee-ModalForm";

  private static final double MAX_WIDTH_FACTOR = 0.95;
  private static final double MAX_HEIGHT_FACTOR = 0.95;

  private final boolean requiresUnload;

  private boolean wasAttached;
  private boolean pendingUnload;

  public ModalForm(Widget widget, HasDimensions dimensions, boolean requiresUnload) {
    super(OutsideClick.IGNORE, STYLE_NAME);
    this.requiresUnload = requiresUnload;

    widget.addStyleName(STYLE_NAME + "-content");
    setWidget(widget);

    if (dimensions != null) {
      setDimensions(dimensions, MAX_WIDTH_FACTOR, MAX_HEIGHT_FACTOR);
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
    return isViewHeaderEvent(event);
  }
}
