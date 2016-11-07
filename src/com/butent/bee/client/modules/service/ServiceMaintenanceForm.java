package com.butent.bee.client.modules.service;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ModificationEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ServiceMaintenanceForm extends PrintFormInterceptor implements SelectorEvent.Handler {

  private static final BeeLogger logger = LogUtils.getLogger(ServiceMaintenanceForm.class);

  private static final String WIDGET_MAINTENANCE_COMMENTS = "MaintenanceComments";
  private static final int NEW_MAINTENANCE_DATA_LOAD_PROCESS_COUNT = 7;

  private static final String STYLE_PROGRESS_CONTAINER =
      BeeConst.CSS_CLASS_PREFIX + "Grid-ProgressContainer";
  private static final String STYLE_PROGRESS_BAR =
      BeeConst.CSS_CLASS_PREFIX + "Grid-ProgressBar";


  private final MaintenanceEventsHandler eventsHandler = new MaintenanceEventsHandler();
  private Flow maintenanceComments;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof DataSelector && BeeUtils.same(name, COL_TYPE)) {
      ((DataSelector) widget).addSelectorHandler(this);

    } else if (widget instanceof Flow && BeeUtils.same(name, WIDGET_MAINTENANCE_COMMENTS)) {
      maintenanceComments = (Flow) widget;
      maintenanceComments.clear();
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    super.afterInsertRow(result, forced);

    fillDataByStateProcessSettings(result, null);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    drawComments(row);

    updateStateDataSelector(false);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ServiceMaintenanceForm();
  }

  public Flow getMaintenanceComments() {
    return maintenanceComments;
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isChanged() && BeeUtils.same(event.getRelatedViewName(), VIEW_MAINTENANCE_TYPES)) {

      updateStateDataSelector(true);
    }
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    super.onSaveChanges(listener, event);

    if (event.getColumns().contains(Data.getColumn(getViewName(),
        AdministrationConstants.COL_STATE))) {
      fillDataByStateProcessSettings(event.getNewRow(), event.getOldRow());
    }
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    super.onStartNewRow(form, oldRow, newRow);

    form.addStyleName(STYLE_PROGRESS_CONTAINER);
    form.addStyleName(STYLE_PROGRESS_BAR);

    final Consumer<Pair<String, String>> consumer = new Consumer<Pair<String, String>>() {
      Set<Pair<String, String>> values = new HashSet<>();
      int processCount;

      @Override
      public void accept(Pair<String, String> columnAndValue) {
        processCount++;

        if (columnAndValue != null) {
          values.add(columnAndValue);
        }

        if (processCount == NEW_MAINTENANCE_DATA_LOAD_PROCESS_COUNT) {
          values.forEach(value ->
              newRow.setValue(Data.getColumnIndex(getViewName(), value.getA()), value.getB()));

          form.removeStyleName(STYLE_PROGRESS_CONTAINER);
          form.removeStyleName(STYLE_PROGRESS_BAR);

          getFormView().refresh(false, false);
        }
      }
    };

    Global.getRelationParameter(PRM_DEFAULT_MAINTENANCE_TYPE, (prefixId, prefix) -> {
      Filter roleFilter = Filter.in(AdministrationConstants.COL_ROLE,
          AdministrationConstants.VIEW_USER_ROLES, AdministrationConstants.COL_ROLE,
          Filter.equals(AdministrationConstants.COL_USER, BeeKeeper.getUser().getUserId()));
      Filter stateFilter;

      if (DataUtils.isId(prefixId)) {
        consumer.accept(Pair.of(COL_TYPE, BeeUtils.toString(prefixId)));
        consumer.accept(Pair.of(ALS_MAINTENANCE_TYPE_NAME, prefix));

        stateFilter = Filter.and(Filter.equals(COL_MAINTENANCE_TYPE, prefixId),
            Filter.isPositive(COL_INITIAL), roleFilter);

      } else {
        consumeProcess(consumer, 2);
        stateFilter = Filter.and(Filter.isPositive(COL_INITIAL), roleFilter);
      }

      Queries.getRowSet(TBL_STATE_PROCESS, null, stateFilter, new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          if (!DataUtils.isEmpty(result)) {
            IsRow processRow = result.getRow(0);
            consumer.accept(Pair.of(AdministrationConstants.COL_STATE, processRow.
                    getString(Data.getColumnIndex(TBL_STATE_PROCESS, COL_MAINTENANCE_STATE))));

            consumer.accept(Pair.of(ALS_STATE_NAME, processRow.
                getString(Data.getColumnIndex(TBL_STATE_PROCESS, ALS_MAINTENANCE_STATE_NAME))));

          } else {
            consumeProcess(consumer, 2);
          }
        }

        @Override
        public void onFailure(String... reason) {
          consumeProcess(consumer, 2);
        }
      });
    });

    Global.getRelationParameter(PRM_DEFAULT_WARRANTY_TYPE, (prefixId, prefix) -> {
      if (DataUtils.isId(prefixId)) {
        consumer.accept(Pair.of(COL_WARRANTY_TYPE, BeeUtils.toString(prefixId)));
        consumer.accept(Pair.of(ALS_WARRANTY_TYPE_NAME, prefix));
      } else {
        consumeProcess(consumer, 2);
      }
    });

    Filter personFilter = Filter.in(Data.getIdColumn(VIEW_COMPANY_PERSONS),
        AdministrationConstants.TBL_USERS, COL_COMPANY_PERSON,
        Filter.compareId(BeeKeeper.getUser().getUserId()));

    Queries.getRowSet(VIEW_COMPANY_PERSONS, null, personFilter, new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            if (!DataUtils.isEmpty(result)) {
              IsRow personRow = result.getRow(0);
              consumer.accept(Pair.of(ALS_CREATOR_DEPARTMENT_NAME, personRow.
                  getString(Data.getColumnIndex(VIEW_COMPANY_PERSONS, ALS_DEPARTMENT_NAME))));

            } else {
              consumeProcess(consumer, 1);
            }
          }

          @Override
          public void onFailure(String... reason) {
            consumeProcess(consumer, 1);
          }
        });
  }

  private void consumeProcess(Consumer consumer, int processCount) {
    for (int i = 0; i < processCount; i++) {
      consumer.accept(null);
    }
  }

  private void createStateChangeComment(IsRow row, IsRow stateProcessRow) {
    List<BeeColumn> columns = Data.getColumns(TBL_MAINTENANCE_COMMENTS,
        Lists.newArrayList(COL_SERVICE_MAINTENANCE, COL_EVENT_NOTE));
    List<String> values = Lists.newArrayList(Long.toString(row.getId()),
        row.getString(Data.getColumnIndex(getViewName(), ALS_STATE_NAME)));

    if (stateProcessRow != null) {
      String notifyValue = stateProcessRow.
          getString(Data.getColumnIndex(TBL_STATE_PROCESS, COL_NOTIFY_CUSTOMER));
      if (BeeUtils.toBoolean(notifyValue)) {
        columns.add(Data.getColumn(TBL_MAINTENANCE_COMMENTS, COL_SHOW_CUSTOMER));
        values.add(notifyValue);
      }

      String commentValue = stateProcessRow.
          getString(Data.getColumnIndex(TBL_STATE_PROCESS, COL_MESSAGE));
      if (!BeeUtils.isEmpty(commentValue)) {
        columns.add(Data.getColumn(TBL_MAINTENANCE_COMMENTS, COL_COMMENT));
        values.add(commentValue);
      }
    }

    Queries.insert(TBL_MAINTENANCE_COMMENTS, columns, values);
  }

  private void drawComments(IsRow row) {
    final Flow comments = getMaintenanceComments();

    if (comments == null) {
      logger.warning("Widget of project comments not found");
      return;
    }

    if (eventsHandler == null) {
      logger.warning("Events handler not initialized");
      return;
    }

    comments.clear();

    if (!DataUtils.isId(row.getId())) {
      return;
    }

    eventsHandler.create(comments, row.getId());
    eventsHandler.setMaintenanceRow(row);
  }

  private void fillDataByStateProcessSettings(IsRow row, IsRow oldRow) {
    Long stateId = row.
        getLong(Data.getColumnIndex(getViewName(), AdministrationConstants.COL_STATE));
    Filter roleFilter = Filter.in(AdministrationConstants.COL_ROLE,
        AdministrationConstants.VIEW_USER_ROLES, AdministrationConstants.COL_ROLE,
        Filter.equals(AdministrationConstants.COL_USER, BeeKeeper.getUser().getUserId()));

    Filter filter = Filter.and(Filter.equals(COL_MAINTENANCE_STATE, stateId), roleFilter);

    Queries.getRowSet(TBL_STATE_PROCESS, null, filter, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (!DataUtils.isEmpty(result)) {
          IsRow stateProcessRow = result.getRow(0);
          String oldValue;

          if (oldRow != null) {
            oldValue = oldRow.getString(Data.getColumnIndex(getViewName(), COL_ENDING_DATE));
          } else {
            oldValue = null;
          }

          if (BeeUtils.toBoolean(stateProcessRow.getString(result.getColumnIndex(COL_FINITE)))) {
            Queries.updateAndFire(getViewName(), row.getId(), row.getVersion(), COL_ENDING_DATE,
                oldValue, BeeUtils.toString(System.currentTimeMillis()),
                ModificationEvent.Kind.UPDATE_ROW);

          } else if (!BeeUtils.isEmpty(oldValue)) {
            Queries.updateAndFire(getViewName(), row.getId(), row.getVersion(), COL_ENDING_DATE,
                oldValue, null, ModificationEvent.Kind.UPDATE_ROW);
          }

          createStateChangeComment(row, stateProcessRow);

        } else {
          createStateChangeComment(row, null);
        }
      }
    });
  }

  private void updateStateDataSelector(boolean clearValue) {
    Widget stateWidget = getFormView().getWidgetByName(AdministrationConstants.COL_STATE);
    if (stateWidget instanceof DataSelector) {
      Filter stateFilter = DataUtils.isNewRow(getActiveRow())
          ? Filter.isPositive(COL_INITIAL) : Filter.isNull(COL_INITIAL);
      Filter roleFilter = Filter.in(AdministrationConstants.COL_ROLE,
          AdministrationConstants.VIEW_USER_ROLES, AdministrationConstants.COL_ROLE,
          Filter.equals(AdministrationConstants.COL_USER, BeeKeeper.getUser().getUserId()));
      Filter typFilter = Filter.equals(COL_MAINTENANCE_TYPE,
          getActiveRow().getLong(Data.getColumnIndex(getViewName(), COL_TYPE)));

      ((DataSelector) stateWidget).setAdditionalFilter(
          Filter.in(Data.getIdColumn(VIEW_MAINTENANCE_STATES), TBL_STATE_PROCESS,
              COL_MAINTENANCE_STATE, Filter.and(stateFilter, roleFilter, typFilter)));

      if (clearValue) {
        ((DataSelector) stateWidget).clearValue();
        getFormView().getActiveRow().clearCell(Data.getColumnIndex(getViewName(),
            AdministrationConstants.COL_STATE));
        getFormView().getActiveRow().clearCell(Data.getColumnIndex(getViewName(), ALS_STATE_NAME));
        getFormView().refreshBySource(AdministrationConstants.COL_STATE);
      }
    }
  }
}