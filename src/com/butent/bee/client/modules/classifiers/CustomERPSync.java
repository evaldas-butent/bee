package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

abstract class CustomERPSync extends AbstractFormInterceptor {

  private InputDateTime erpLastEvent;

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    super.afterCreateEditableWidget(editableWidget, widget);

    if (widget instanceof InputDateTime && editableWidget.hasSource("ERPLastEvent")) {
      erpLastEvent = (InputDateTime) widget;
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (erpLastEvent == null) {
      super.beforeRefresh(form, row);
      return;
    }

    boolean hasUrl = !BeeUtils.isEmpty(Data.getString(form.getViewName(), row,
        "ERPAddress"));

    erpLastEvent.setEnabled(!hasUrl);
    erpLastEvent.setStyleName(StyleUtils.NAME_DISABLED, hasUrl);

    if (!hasUrl) {
      super.beforeRefresh(form, row);
      return;
    }

    Queries.getRowSet(AdministrationConstants.TBL_EVENT_HISTORY,
        Lists.newArrayList(AdministrationConstants.COL_EVENT_STARTED,
            AdministrationConstants.COL_EVENT_ENDED),
        Filter.equals(AdministrationConstants.COL_EVENT,
            PayrollConstants.PRM_ERP_SYNC_HOURS_VITARESTA),
        new Order(Data.getIdColumn(AdministrationConstants.TBL_EVENT_HISTORY), false), 0, 1,
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            if (erpLastEvent == null) {
              return;
            }

            boolean enabled = DataUtils.isEmpty(result)
                || result.getDateTime(0, AdministrationConstants.COL_EVENT_ENDED) != null;

            erpLastEvent.setEnabled(enabled);
            erpLastEvent.setStyleName(StyleUtils.NAME_DISABLED, !enabled);

            if (!enabled) {
              DateTime started = result.getDateTime(0, AdministrationConstants.COL_EVENT_STARTED);
              if (started != null) {
                started.setMillis(0);
              }

              String msg = BeeUtils.joinWords(Localized.dictionary().dataImport(),
                  BeeUtils.bracket(Localized.dictionary().dateFromShort() + " "
                      + started != null ? started.toCompactString() : ""));

              erpLastEvent.setTitle(msg);
            } else {
              erpLastEvent.setTitle("");
            }
          }
        });

    super.beforeRefresh(form, row);
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    super.onSaveChanges(listener, event);

    IsRow oldRow = getFormView().getOldRow();

    if (DataUtils.contains(event.getColumns(), "ERPLastEvent")
        && !Data.isNull(getViewName(), oldRow, "ERPAddress")) {
      event.consume();

      Queries.getRow(getViewName(), oldRow.getId(), new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          String newVal = Data.getString(getViewName(), result,
              "ERPLastEvent");
          if (!BeeUtils.equals(newVal, Data.getString(getViewName(), oldRow, "ERPLastEvent"))) {

            Data.setValue(getViewName(), oldRow, "ERPLastEvent", newVal);
            getFormView().updateCell("ERPLastEvent", newVal);
            getFormView().refreshBySource("ERPLastEvent");
            getFormView().notifyWarning(PayrollConstants.PRM_ERP_SYNC_HOURS_VITARESTA,
                Localized.dictionary().dataImport(), Localized.dictionary().recordIsInUse(oldRow
                    .getId()));
          } else {
            listener.fireEvent(event);
          }
        }
      });
    }
  }
}
