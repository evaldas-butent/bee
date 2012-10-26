package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;

public abstract class AbstractFormCallback implements FormFactory.FormCallback {
  
  private FormView formView;

  @Override
  public void afterAction(Action action, FormPresenter presenter) {
  }

  @Override
  public void afterCreate(FormView form) {
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget) {
  }

  @Override
  public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
  }

  @Override
  public boolean beforeAction(Action action, FormPresenter presenter) {
    return true;
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    return true;
  }
  
  @Override
  public void beforeRefresh(FormView form, IsRow row) {
  }
  
  @Override
  public Widget createCustomWidget(String name, Element description) {
    return null;
  }

  @Override
  public FormView getFormView() {
    return formView;
  }
  
  @Override
  public GridView getGridView() {
    if (getFormView() != null && getFormView().getViewPresenter() instanceof HasGridView) {
      return ((HasGridView) getFormView().getViewPresenter()).getGridView();
    } else {
      return null;
    }
  }

  @Override
  public abstract FormCallback getInstance();

  @Override
  public AbstractCellRenderer getRenderer(WidgetDescription widgetDescription) {
    return null;
  }

  @Override
  public BeeRowSet getRowSet() {
    return null;
  }

  @Override
  public boolean hasFooter(int rowCount) {
    return true;
  }

  @Override
  public boolean onReadyForInsert(ReadyForInsertEvent event) {
    return true;
  }

  @Override
  public void onSetActiveRow(IsRow row) {
  }

  @Override
  public void onShow(FormPresenter presenter) {
  }

  @Override
  public void onStart(FormView form) {
  }
  
  @Override
  public void onStartEdit(FormView form, IsRow row) {
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
  }

  @Override
  public void setFormView(FormView formView) {
    this.formView = formView;
  }
}
