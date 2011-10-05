package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.presenter.FormPresenter;

public abstract class AbstractFormCallback implements FormFactory.FormCallback {
  public abstract void afterCreateWidget(String name, Widget widget);

  public boolean beforeCreateWidget(String name, Element description) {
    return true;
  }

  public boolean onLoad(Element formElement) {
    return true;
  }

  public void onShow(FormPresenter presenter) {
  }
}
