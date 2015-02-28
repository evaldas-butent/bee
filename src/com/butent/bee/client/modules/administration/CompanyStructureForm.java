package com.butent.bee.client.modules.administration;

import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.ui.Action;

import java.util.ArrayList;
import java.util.List;

class CompanyStructureForm extends AbstractFormInterceptor implements HandlesAllDataEvents {

  private final List<HandlerRegistration> handlerRegistry = new ArrayList<>();

  CompanyStructureForm() {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case ADD:
        RowFactory.createRow(VIEW_DEPARTMENTS, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            refresh();
          }
        });
        return false;

      case REFRESH:
        refresh();
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CompanyStructureForm();
  }

  @Override
  public void onLoad(FormView form) {
    if (handlerRegistry.isEmpty()) {
      handlerRegistry.addAll(BeeKeeper.getBus().registerDataHandler(this, false));
    }

    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    EventUtils.clearRegistry(handlerRegistry);
    super.onUnload(form);
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  private static boolean isEventRelevant(DataEvent event) {
    return event.hasView(VIEW_DEPARTMENTS) || event.hasView(VIEW_DEPARTMENT_EMPLOYEES);
  }

  private void refresh() {
  }
}
