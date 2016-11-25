package com.butent.bee.client.modules.service;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_STATE;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.data.event.RowUpdateEvent;

public class MaintenanceCommentForm extends AbstractFormInterceptor {

  private static final String WIDGET_ITEM_LABEL_NAME = "ItemLabel";
  private static final String WIDGET_TERM_LABEL_NAME = "TermLabel";
  private static final String WIDGET_WARRANTY_LABEL_NAME = "WarrantyLabel";

  private final IsRow serviceMaintenance;
  private IsRow stateProcessRow;

  MaintenanceCommentForm(IsRow serviceMaintenance) {
    this.serviceMaintenance = serviceMaintenance;
  }

  public MaintenanceCommentForm(IsRow maintenanceRow, IsRow stateProcessRow) {
    super();
    this.serviceMaintenance = maintenanceRow;
    this.stateProcessRow = stateProcessRow;
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    super.afterInsertRow(result, forced);

    FormView form = getFormView();

    if (form != null) {
      Widget warrantyWidget = form.getWidgetByName(COL_WARRANTY);

      if (warrantyWidget instanceof InputDateTime) {
        String warrantyDateTimeValue = ((InputDateTime) warrantyWidget).getNormalizedValue();

        if (!BeeUtils.isEmpty(warrantyDateTimeValue)) {
          result.setProperty(COL_WARRANTY_VALID_TO, warrantyDateTimeValue);
        }
      }
    }
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    super.onSaveChanges(listener, event);

    Long serviceMaintenanceId = event.getNewRow()
        .getLong(Data.getColumnIndex(getViewName(), COL_SERVICE_MAINTENANCE));

    if (event.getColumns().contains(Data.getColumn(getViewName(), COL_TERM))
        && DataUtils.isId(serviceMaintenanceId)) {
      Queries.getRow(TBL_SERVICE_MAINTENANCE, serviceMaintenanceId, new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          if (result != null) {
            RowUpdateEvent.fire(BeeKeeper.getBus(), TBL_SERVICE_MAINTENANCE, result);
          }
        }
      });
    }
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand) {
    updateTermVisibility(form, row);

    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    super.onStartNewRow(form, oldRow, newRow);

    if (serviceMaintenance != null) {
      newRow.setValue(Data.getColumnIndex(form.getViewName(), COL_SERVICE_MAINTENANCE),
          serviceMaintenance.getId());

      if (stateProcessRow != null) {
        newRow.setValue(Data.getColumnIndex(getViewName(), COL_MAINTENANCE_STATE),
            serviceMaintenance.getLong(Data.getColumnIndex(TBL_SERVICE_MAINTENANCE, COL_STATE)));

        newRow.setValue(Data.getColumnIndex(getViewName(), COL_EVENT_NOTE), serviceMaintenance
            .getString(Data.getColumnIndex(TBL_SERVICE_MAINTENANCE, ALS_STATE_NAME)));
        form.refreshBySource(COL_EVENT_NOTE);

        setWidgetsVisibility(BeeUtils.isTrue(stateProcessRow
            .getBoolean(Data.getColumnIndex(TBL_STATE_PROCESS, COL_TERM))),
            form.getWidgetBySource(COL_TERM),
            form.getWidgetByName(WIDGET_TERM_LABEL_NAME));

        Long itemId = stateProcessRow.getLong(Data.getColumnIndex(TBL_STATE_PROCESS,
            COL_MAINTENANCE_ITEM));

        boolean visibleItem = DataUtils.isId(itemId);
        setWidgetsVisibility(DataUtils.isId(itemId), form.getWidgetByName(WIDGET_ITEM_LABEL_NAME),
            form.getWidgetBySource(COL_MAINTENANCE_ITEM), form.getWidgetBySource(COL_ITEM_PRICE),
            form.getWidgetBySource(COL_ITEM_CURRENCY));

        if (visibleItem) {
          newRow.setValue(Data.getColumnIndex(getViewName(), COL_MAINTENANCE_ITEM),
              BeeUtils.toString(itemId));
          newRow.setValue(Data.getColumnIndex(getViewName(), ALS_MAINTENANCE_ITEM_NAME),
              stateProcessRow.getString(Data.getColumnIndex(TBL_STATE_PROCESS,
                  ALS_MAINTENANCE_ITEM_NAME)));
          form.refreshBySource(COL_MAINTENANCE_ITEM);
        }

        String commentValue = stateProcessRow
            .getString(Data.getColumnIndex(TBL_STATE_PROCESS, COL_MESSAGE));
        newRow.setValue(Data.getColumnIndex(getViewName(), COL_COMMENT), commentValue);
        form.refreshBySource(COL_COMMENT);

        Boolean notifyCustomer = stateProcessRow
            .getBoolean(Data.getColumnIndex(TBL_STATE_PROCESS, COL_NOTIFY_CUSTOMER));

        if (BeeUtils.isTrue(notifyCustomer)) {
          newRow.setValue(Data.getColumnIndex(getViewName(), COL_CUSTOMER_SENT), notifyCustomer);
          form.refreshBySource(COL_CUSTOMER_SENT);
        }

        Boolean showCustomerValue = stateProcessRow
            .getBoolean(Data.getColumnIndex(TBL_STATE_PROCESS, COL_SHOW_CUSTOMER));

        if (BeeUtils.isTrue(showCustomerValue)) {
          newRow.setValue(Data.getColumnIndex(getViewName(), COL_SHOW_CUSTOMER), showCustomerValue);
          form.refreshBySource(COL_SHOW_CUSTOMER);
        }

        boolean isWarrantyVisible = BeeUtils.toBoolean(stateProcessRow
            .getString(Data.getColumnIndex(TBL_STATE_PROCESS, COL_WARRANTY)));
        Widget warrantyWidget = form.getWidgetByName(COL_WARRANTY);
        setWidgetsVisibility(isWarrantyVisible,
            form.getWidgetByName(WIDGET_WARRANTY_LABEL_NAME), warrantyWidget);

        if (warrantyWidget instanceof InputDateTime) {
          ((InputDateTime) warrantyWidget).setNullable(!isWarrantyVisible);
        }

      } else {
        updateTermVisibility(form, newRow);
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new MaintenanceCommentForm(serviceMaintenance);
  }

  private static void setWidgetsVisibility(boolean visible, Widget... widget) {
    for (Widget element : widget) {
      if (element != null) {
        element.setVisible(visible);
      }
    }
  }

  private void updateTermVisibility(FormView form, IsRow row) {
    Long commentsStateId = row.getLong(Data.getColumnIndex(getViewName(), COL_MAINTENANCE_STATE));
    setWidgetsVisibility(DataUtils.isId(commentsStateId),
        form.getWidgetBySource(COL_TERM), form.getWidgetByName(WIDGET_TERM_LABEL_NAME));
  }
}