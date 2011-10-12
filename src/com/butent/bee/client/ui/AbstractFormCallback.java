package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;

public abstract class AbstractFormCallback implements FormFactory.FormCallback {

  public void afterCreateWidget(String name, Widget widget) {
  }

  public boolean beforeCreateWidget(String name, Element description) {
    return true;
  }

  public boolean onLoad(Element formElement) {
    return true;
  }

  public boolean onPrepareForInsert(FormView form, IsRow row) {
    return true;
  }

  public void onShow(FormPresenter presenter) {
  }

  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
  }
}
