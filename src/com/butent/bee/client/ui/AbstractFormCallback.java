package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;

public class AbstractFormCallback implements FormFactory.FormCallback {

  public void afterAction(Action action, FormPresenter presenter) {
  }

  public void afterCreateWidget(String name, Widget widget) {
  }

  public void afterRefresh(FormView form, IsRow row) {
  }

  public boolean beforeAction(Action action, FormPresenter presenter) {
    return true;
  }

  public boolean beforeCreateWidget(String name, Element description) {
    return true;
  }
  
  public void beforeRefresh(FormView form, IsRow row) {
  }
  
  public Widget createCustomWidget(String name, Element description) {
    return null;
  }

  public FormCallback getInstance() {
    return null;
  }

  public boolean onLoad(Element formElement) {
    return true;
  }

  public boolean onPrepareForInsert(FormView form, DataView dataView, IsRow row) {
    return true;
  }

  public void onShow(Presenter presenter) {
  }

  public void onStartEdit(FormView form, IsRow row) {
  }
  
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
  }
}
