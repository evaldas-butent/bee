package com.butent.bee.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;

import java.util.List;

public abstract class AbstractFormInterceptor implements FormFactory.FormInterceptor {

  private FormView formView;

  @Override
  public void afterAction(Action action, Presenter presenter) {
  }

  @Override
  public void afterCreate(FormView form) {
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
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
  public IdentifiableWidget createCustomWidget(String name, Element description) {
    return null;
  }

  @Override
  public IsRow getActiveRow() {
    return (getFormView() == null) ? null : getFormView().getActiveRow();
  }

  @Override
  public long getActiveRowId() {
    return (getFormView() == null) ? BeeConst.UNDEF : getFormView().getActiveRowId();
  }

  @Override
  public Domain getDomain() {
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
  public HeaderView getHeaderView() {
    return (getFormView() == null || getFormView().getViewPresenter() == null) ? null 
        : getFormView().getViewPresenter().getHeader();
  }

  @Override
  public abstract FormInterceptor getInstance();

  @Override
  public AbstractCellRenderer getRenderer(WidgetDescription widgetDescription) {
    return null;
  }

  @Override
  public BeeRowSet getRowSet() {
    return null;
  }

  @Override
  public String getSupplierKey() {
    return null;
  }

  @Override
  public String getViewName() {
    return (getFormView() == null) ? null : getFormView().getViewName();
  }

  @Override
  public boolean hasFooter(int rowCount) {
    return true;
  }

  @Override
  public void onClose(List<String> messages, IsRow oldRow, IsRow newRow) {
  }

  @Override
  public void onReadyForInsert(ReadyForInsertEvent event) {
  }

  @Override
  public void onSaveChanges(SaveChangesEvent event) {
  }

  @Override
  public void onSetActiveRow(IsRow row) {
  }

  @Override
  public void onShow(Presenter presenter) {
  }

  @Override
  public void onStart(FormView form) {
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand) {
    return true;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
  }

  @Override
  public void onStateChange(State state) {
  }

  @Override
  public void setFormView(FormView formView) {
    this.formView = formView;
  }
}
