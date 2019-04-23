package com.butent.bee.client.modules.calendar.view;

import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.*;
import com.butent.bee.client.event.logical.DataReceivedEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.trade.acts.TradeActForm;
import com.butent.bee.client.modules.trade.acts.TradeActServicesGrid;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_USER;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.projects.ProjectConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_SERIES_NAME;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_SUPPLIER;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.Relations;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.modules.trade.acts.TradeActKeeper;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AppointmentForm extends AbstractFormInterceptor implements ClickHandler {

  private DataSelector prjSelector;
  private static final String NEW_ACT_LABEL = "NewActLabel";
  private static final String NEW_ACT_SERVICE_LABEL = "NewActServiceLabel";

  private class RelationsHandler implements SelectorEvent.Handler {

    @Override
    public void onDataSelector(SelectorEvent event) {
      final String viewName = event.getRelatedViewName();

      if (event.isNewRow()) {
        if (Objects.equals(viewName, VIEW_TASKS)) {
          event.consume();

          final String formName = event.getNewRowFormName();
          final BeeRow row = event.getNewRow();
          final DataSelector selector = event.getSelector();

          Long projectId = getLongValue(COL_PROJECT);
          if (DataUtils.isId(projectId)) {
            String project = getStringValue(ALS_PROJECT_NAME);

            Data.setValue(TaskConstants.VIEW_TASKS, row, COL_PROJECT, projectId);
            Data.setValue(TaskConstants.VIEW_TASKS, row, ALS_PROJECT_NAME, project);
          }

          Long companyId = getLongValue(COL_COMPANY);
          if (DataUtils.isId(companyId)) {
            String company = getStringValue(ALS_COMPANY_NAME);

            Data.setValue(VIEW_TASKS, row, COL_COMPANY, companyId);
            Data.setValue(VIEW_TASKS, row, ALS_COMPANY_NAME, company);
          }
          RowFactory.createRelatedRow(formName, row, selector);

        } else if (Objects.equals(viewName, TBL_TRADE_ACTS)) {
          event.consume();
          createTradeAct(event.getNewRowFormName(), event.getNewRow(), event.getSelector());
        }
      } else {
        Filter filter = null;
        Long user = BeeKeeper.getUser().getUserId();

        if (Objects.equals(viewName, VIEW_TASKS)) {
          filter = Filter.or(Filter.equals(COL_EXECUTOR, user), Filter.equals(COL_OWNER, user),
              Filter.in(COL_TASK_ID, VIEW_TASK_USERS, COL_TASK, Filter.equals(COL_USER, user)));
        } else if (Objects.equals(viewName, VIEW_PROJECTS)) {
          filter = Filter.in(Data.getIdColumn(VIEW_PROJECTS), VIEW_PROJECT_USERS, COL_PROJECT,
              Filter.equals(COL_USER, user));
        }

        event.getSelector().setAdditionalFilter(filter);
      }
    }
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (BeeUtils.same(editableWidget.getColumnId(), COL_PROJECT)) {
      prjSelector = (DataSelector) widget;
      prjSelector.addSelectorHandler(event -> {
        Filter filter = Filter.in(Data.getIdColumn(VIEW_PROJECTS), VIEW_PROJECT_USERS, COL_PROJECT,
            Filter.equals(COL_USER, BeeKeeper.getUser().getUserId()));

        event.getSelector().setAdditionalFilter(filter);
      });
    }

    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof Relations) {
      ((Relations) widget).setSelectorHandler(new RelationsHandler());
    } else if (BeeUtils.same(NEW_ACT_LABEL, name)) {
      ((FaLabel) widget).addClickHandler(this);
    } else if (BeeUtils.same(NEW_ACT_SERVICE_LABEL, name)) {
      ((FaLabel) widget).addClickHandler(clickEvent -> {

        Long tradeActID = getLongValue(COL_TRADE_ACT);

        if (BeeUtils.isPositive(tradeActID)) {
          handleAndRefreshAppointment(tradeActID);
        } else {
          getFormView().notifySevere(Localized.dictionary().fieldRequired(Localized.dictionary().tradeAct()));
        }
      });
    } else if (BeeUtils.same(name, COL_TRADE_ACT_SERVICE) && widget instanceof UnboundSelector) {
        ((UnboundSelector) widget).addSelectorHandler(this::setTradeActServiceFilter);
      }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    updateTradeActService(null);

    super.beforeRefresh(form, row);
  }

  private void handleAndRefreshAppointment(Long tradeActID) {
    if (!DataUtils.isNewRow(getActiveRow())) {

      Queries.getRow(VIEW_TRADE_ACTS, tradeActID, new RowCallback() {
        @Override
        public void onSuccess(BeeRow row) {
          row.setProperty(CalendarConstants.COL_APPOINTMENT, getActiveRowId());

          RowEditor.openForm(FORM_TRADE_ACT, Data.getDataInfo(VIEW_TRADE_ACTS), row, null, new TradeActForm(){

            @Override
            public void afterCreateWidget(String name, IdentifiableWidget widget, FormFactory.WidgetDescriptionCallback callback) {

              if(BeeUtils.same(name, VIEW_TRADE_ACT_SERVICES) && widget instanceof ChildGrid) {
                ((ChildGrid) widget).setGridInterceptor(new TradeActServicesGrid(){

                  @Override
                  public void onDataReceived(DataReceivedEvent event) {
                    Long appointmentID = AppointmentForm.this.getFormView().getActiveRowId();

                    if (DataUtils.isId(appointmentID) && event.getRows() != null && event.getRows().size() > 0) {
                      IsRow serviceRow = event.getRows().stream()
                        .filter(row -> Objects.equals(Data.getLong(VIEW_TRADE_ACT_SERVICES, row,
                          CalendarConstants.COL_APPOINTMENT), appointmentID))
                        .findFirst().orElse(null);

                      if (serviceRow != null) {
                        updateTradeActService(serviceRow.getId());
                      }
                    }
                  }
                });
              }
              super.afterCreateWidget(name, widget, callback);
            }
          });
        }
      });
    }
  }
  @Override
  public FormInterceptor getInstance() {
    return new AppointmentForm();
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    DataSelector tradeAct = (DataSelector) getFormView().getWidgetBySource(COL_TRADE_ACT);
    DataInfo dataInfo = tradeAct.getOracle().getDataInfo();

    createTradeAct(tradeAct.getNewRowForm(), RowFactory.createEmptyRow(dataInfo, true),
        (DataSelector) getFormView().getWidgetBySource(COL_TRADE_ACT));
  }

  public DataSelector getProjectSelector() {
    return prjSelector;
  }

  public static void createTradeAct(String formName, BeeRow row, DataSelector selector) {
    List<TradeActKind> kinds = Arrays.asList(TradeActKind.SALE, TradeActKind.TENDER,
        TradeActKind.PURCHASE, TradeActKind.WRITE_OFF, TradeActKind.RESERVE,
        TradeActKind.RENT_PROJECT);

    Global.choice(Localized.dictionary().tradeActNew(), null,
        kinds.stream().map(HasLocalizedCaption::getCaption).collect(Collectors.toList()),
        value -> TradeActKeeper.ensureChache(() -> {
          TradeActKeeper.prepareNewTradeAct(row, kinds.get(value));
          RowFactory.createRelatedRow(formName, row, selector);
        }));
  }

  private static void clearTradeAct(IsRow row) {
    Data.clearCell(CalendarConstants.VIEW_APPOINTMENTS, row, COL_TRADE_ACT);
    Data.clearCell(CalendarConstants.VIEW_APPOINTMENTS, row, "TradeNumber");
    Data.clearCell(CalendarConstants.VIEW_APPOINTMENTS, row, COL_SERIES_NAME);
    Data.clearCell(CalendarConstants.VIEW_APPOINTMENTS, row, "TradeCompanyName");
  }

  private void updateSupplier(BeeRow serviceRow) {
    Long supplier = serviceRow == null ? null : Data.getLong(VIEW_TRADE_ACT_SERVICES, serviceRow, COL_TRADE_SUPPLIER);
    ((UnboundSelector) getFormView().getWidgetByName("Suppliers")).setValue(supplier, true);
  }

  private void updateTradeActService(Long id) {
    Long tradeActServiceID = id == null ? Data.getLong(CalendarConstants.VIEW_APPOINTMENTS, getActiveRow(), COL_TRADE_ACT_SERVICE) : id;
    UnboundSelector serviceSelector = (UnboundSelector) getFormView().getWidgetByName(COL_TRADE_ACT_SERVICE);

    if (serviceSelector != null) {
      serviceSelector.setValue(tradeActServiceID, true);
    }
  }

  private static void setTradeAct(IsRow activeRow, BeeRow serviceRow, Long newTradeActID) {
    Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, activeRow, COL_TRADE_ACT, newTradeActID);

    Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, activeRow, "TradeNumber",
      Data.getString(VIEW_TRADE_ACT_SERVICES, serviceRow, "TradeNumber"));

    Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, activeRow, "TradeCompanyName",
      Data.getString(VIEW_TRADE_ACT_SERVICES, serviceRow, "TradeCompanyName"));

    Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, activeRow, COL_SERIES_NAME,
      Data.getString(VIEW_TRADE_ACT_SERVICES, serviceRow, "TradeSeriesName"));
  }

  private void setTradeActServiceFilter(SelectorEvent event) {
    Long tradeActID = getLongValue(COL_TRADE_ACT);
    Filter filter = BeeUtils.isPositive(tradeActID) ? Filter.equals(COL_TRADE_ACT, tradeActID) : null;

    if (event.isOpened()) {
      event.getSelector().getOracle().setAdditionalFilter(filter, true);
    } else if (event.isChanged()) {
      refreshAppointmentBasedOnService(event.getRelatedRow(), tradeActID);
    }
  }

  private void refreshAppointmentBasedOnService(BeeRow serviceRow, Long tradeActID) {
    if (serviceRow != null) {

    Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, getActiveRow(), COL_COST_AMOUNT,
      Data.getDouble(VIEW_TRADE_ACT_SERVICES, serviceRow, COL_COST_AMOUNT));

    Data.setValue(CalendarConstants.VIEW_APPOINTMENTS, getActiveRow(), COL_DATE_FROM,
      Data.getDate(VIEW_TRADE_ACT_SERVICES, serviceRow, COL_DATE_FROM));

    Long newTradeActID = Data.getLong(VIEW_TRADE_ACT_SERVICES, serviceRow, COL_TRADE_ACT);
    if (BeeUtils.isPositive(newTradeActID) && !Objects.equals(tradeActID, newTradeActID)) {
      setTradeAct(getActiveRow(), serviceRow, newTradeActID);
    }

    getFormView().refreshBySource(COL_COST_AMOUNT);
    getFormView().refreshBySource(COL_DATE_FROM);
    getFormView().refreshBySource(COL_TRADE_ACT);

    updateSupplier(serviceRow);

    } else {
      clearTradeAct(getActiveRow());
      getFormView().refreshBySource(COL_TRADE_ACT);
    }
  }
}