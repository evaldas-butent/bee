package com.butent.bee.client.view.form;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.ReadyEvent;
import com.butent.bee.client.event.logical.ReadyEvent.Handler;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.View;

public class FormAndHeader extends Complex implements View {

  private Presenter viewPresenter;

  private boolean enabled = true;

  public FormAndHeader() {
    super();
  }

  @Override
  public HandlerRegistration addReadyHandler(Handler handler) {
    return addHandler(handler, ReadyEvent.getType());
  }

  @Override
  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled != isEnabled()) {
      this.enabled = enabled;
      DomUtils.enableChildren(this, enabled);
    }
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;

    for (Widget child : this) {
      if (child instanceof View && ((View) child).getViewPresenter() == null) {
        ((View) child).setViewPresenter(viewPresenter);
      }
    }
  }
}
