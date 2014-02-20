package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.server.modules.commons.ExchangeUtils.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.values.WhiteSpace;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.AssessmentStatus;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssessmentForm extends PrintFormInterceptor {

  private static class PrintForm extends AbstractFormInterceptor {
    @Override
    public void beforeRefresh(FormView form, IsRow row) {
      Widget w = form.getWidgetByName("CustomerInfo");
      if (w instanceof HasWidgets) {
        CommonsUtils.getCompanyInfo(row.getLong(form.getDataIndex("Customer")), w);
      }
      w = form.getWidgetByName("ForwarderInfo");
      if (w instanceof HasWidgets) {
        CommonsUtils.getCompanyInfo(row.getLong(form.getDataIndex("Forwarder")), w);
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return null;
    }
  }

  private abstract class AssessmentGrid extends AbstractGridInterceptor {
    @Override
    public void beforeRefresh(GridPresenter presenter) {
      presenter.getDataProvider().setParentFilter(COL_ASSESSOR, getFilter());
    }

    @Override
    public Map<String, Filter> getInitialParentFilters() {
      if (currentRow == null) {
        return super.getInitialParentFilters();
      } else {
        return ImmutableMap.of(COL_ASSESSOR, getFilter());
      }
    }

    @Override
    public void onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {
      event.getColumns().add(DataUtils.getColumn(COL_ASSESSOR, gridView.getDataColumns()));
      event.getValues().add(BeeUtils.toString(currentRow.getId()));
    }

    protected Filter getFilter() {
      return Filter.equals(COL_ASSESSOR, currentRow.getId());
    }
  }

  private class ForwardersGrid extends AssessmentGrid {
    private static final String SERVICE_CARGO = "ServiceCargo";
    private static final String SERVICE_ASSESSOR = "ServiceAssessor";
    private static final String SUPPLIER = "Supplier";
    private static final String FORWARDER = "Forwarder";

    @Override
    public void afterInsertRow(IsRow result) {
      refresh();
    }

    @Override
    public void afterUpdateRow(IsRow result) {
      refresh();
    }

    @Override
    public DeleteMode beforeDeleteRow(final GridPresenter presenter, final IsRow row) {
      final long tripId = row.getLong(presenter.getGridView().getDataIndex(COL_TRIP));

      Queries.deleteRow(VIEW_EXPEDITION_TRIPS, tripId, 0, new IntCallback() {
        @Override
        public void onSuccess(Integer result) {
          RowDeleteEvent.fire(BeeKeeper.getBus(), VIEW_EXPEDITION_TRIPS, tripId);
          RowDeleteEvent.fire(BeeKeeper.getBus(), presenter.getViewName(), row.getId());
          refresh();
        }
      });
      return DeleteMode.CANCEL;
    }

    @Override
    public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
        Collection<RowInfo> selectedRows, DeleteMode defMode) {
      return DeleteMode.SINGLE;
    }

    @Override
    public void onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {
      super.onReadyForInsert(gridView, event);

      List<BeeColumn> columns = event.getColumns();
      List<String> values = event.getValues();

      columns.add(DataUtils.getColumn(SERVICE_CARGO, gridView.getDataColumns()));
      values.add(values.get(DataUtils.getColumnIndex(COL_CARGO, columns)));

      columns.add(DataUtils.getColumn(SERVICE_ASSESSOR, gridView.getDataColumns()));
      values.add(values.get(DataUtils.getColumnIndex(COL_ASSESSOR, columns)));

      if (DataUtils.getColumn(SUPPLIER, columns) == null) {
        columns.add(DataUtils.getColumn(SUPPLIER, gridView.getDataColumns()));
        values.add(values.get(DataUtils.getColumnIndex(FORWARDER, columns)));
      }
    }

    @Override
    public void onSaveChanges(GridView gridView, SaveChangesEvent event) {
      IsRow row = event.getNewRow();

      if (!DataUtils.isId(row.getLong(gridView.getDataIndex(SERVICE_CARGO)))) {
        List<BeeColumn> columns = event.getColumns();
        List<String> oldValues = event.getOldValues();
        List<String> newValues = event.getNewValues();

        columns.add(DataUtils.getColumn(SERVICE_CARGO, gridView.getDataColumns()));
        oldValues.add(null);
        newValues.add(row.getString(gridView.getDataIndex(COL_CARGO)));

        columns.add(DataUtils.getColumn(SERVICE_ASSESSOR, gridView.getDataColumns()));
        oldValues.add(null);
        newValues.add(row.getString(gridView.getDataIndex(COL_ASSESSOR)));

        if (DataUtils.getColumn(SUPPLIER, columns) == null) {
          columns.add(DataUtils.getColumn(SUPPLIER, gridView.getDataColumns()));
          oldValues.add(null);
          newValues.add(row.getString(gridView.getDataIndex(FORWARDER)));
        }
      }
    }

    private void refresh() {
      DataChangeEvent.fireRefresh(BeeKeeper.getBus(), TBL_CARGO_EXPENSES);
      refreshTotals();
    }
  }

  private class ServicesGrid extends AssessmentGrid {

    @Override
    public void afterDeleteRow(long rowId) {
      DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_ASSESSMENT_FORWARDERS);
      refreshTotals();
    }

    @Override
    public void afterInsertRow(IsRow result) {
      refreshTotals();
    }

    @Override
    public void afterUpdateCell(IsColumn column, IsRow result, boolean rowMode) {
      if (BeeUtils.inListSame(column.getId(), COL_DATE, COL_AMOUNT, COL_CURRENCY,
          COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC)) {
        DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_ASSESSMENT_FORWARDERS);
        refreshTotals();
      }
    }
  }

  private class AssessorsGrid extends AssessmentGrid {
    @Override
    public void afterDeleteRow(long rowId) {
      refreshTotals();
    }

    @Override
    public DeleteMode getDeleteMode(GridPresenter presenter, final IsRow activeRow,
        Collection<RowInfo> selectedRows, DeleteMode defMode) {

      final String view = presenter.getViewName();
      final int oldStatus = activeRow.getInteger(Data.getColumnIndex(view, COL_STATUS));

      if (AssessmentStatus.ANSWERED.is(oldStatus)) {
        Global.inputString(Localized.getConstants().trAssessmentRejection(),
            Localized.getConstants().trAssessmentRejectionReasonRequired(),
            new StringCallback() {
              @Override
              public void onSuccess(String value) {
                Queries.update(view, activeRow.getId(), activeRow.getVersion(),
                    Lists.newArrayList(Data.getColumn(view, COL_STATUS),
                        Data.getColumn(view, COL_ASSESSOR_NOTES)),
                    Lists.newArrayList(BeeUtils.toString(oldStatus),
                        activeRow.getString(Data.getColumnIndex(view, COL_ASSESSOR_NOTES))),
                    Lists.newArrayList(BeeUtils.toString(AssessmentStatus.NEW.ordinal()),
                        value), null,
                    new RowUpdateCallback(view));
              }
            });
        return DeleteMode.CANCEL;
      } else {
        return DeleteMode.SINGLE;
      }
    }

    @Override
    public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
      newRow.setValue(gridView.getDataIndex(COL_ASSESSOR_NOTES),
          currentRow.getString(getFormView().getDataIndex(COL_ASSESSOR_NOTES)));

      int status = currentRow.getInteger(getFormView().getDataIndex(COL_STATUS));

      if (AssessmentStatus.ACTIVE.is(status)) {
        newRow.setValue(gridView.getDataIndex(COL_STATUS), status);
      }
      return true;
    }

    @Override
    protected Filter getFilter() {
      if (isPrimaryRequest(currentRow)) {
        return Filter.compareId(Operator.NE, currentRow.getId());
      } else {
        return super.getFilter();
      }
    }
  }

  private class StatusUpdater implements ClickHandler {

    private final AssessmentStatus status;
    private final String preconditionError;
    private FormView formView;
    private int statusIdx;
    private int orderStatusIdx;
    private RowCallback rowCallback;

    public StatusUpdater(AssessmentStatus status, String preconditionError) {
      this.status = status;
      this.preconditionError = preconditionError;
    }

    @Override
    public void onClick(ClickEvent event) {
      if (formView == null) {
        formView = getFormView();
        statusIdx = formView.getDataIndex(COL_STATUS);
        orderStatusIdx = formView.getDataIndex("OrderStatus");
        rowCallback = new RowUpdateCallback(formView.getViewName());
      }
      if (isPrimaryRequest(currentRow) && !BeeUtils.isEmpty(preconditionError)) {
        Queries.getRowCount(TBL_CARGO_ASSESSORS,
            Filter.and(Filter.equals(COL_CARGO,
                currentRow.getLong(getFormView().getDataIndex(COL_CARGO))),
                Filter.compareId(Operator.NE, currentRow.getId()),
                Filter.isNotEqual(COL_STATUS, IntegerValue.of(status))),
            new IntCallback() {
              @Override
              public void onSuccess(Integer result) {
                if (BeeUtils.isPositive(result)) {
                  Global.showError(preconditionError);
                } else {
                  update();
                }
              }
            });
      } else {
        update();
      }
    }

    public void update() {
      Global.confirm(status.getConfirmation(), new ConfirmationCallback() {
        @Override
        public void onConfirm() {
          IsRow newRow = currentRow;

          List<BeeColumn> columns = Lists.newArrayList(DataUtils.getColumn(COL_STATUS,
              formView.getDataColumns()));
          List<String> oldValues = Lists.newArrayList(newRow.getString(statusIdx));
          List<String> newValues = Lists.newArrayList(BeeUtils.toString(status.ordinal()));

          if (isPrimaryRequest(newRow) && status.getOrderStatus() != null) {
            columns.add(DataUtils.getColumn("OrderStatus", formView.getDataColumns()));
            oldValues.add(newRow.getString(orderStatusIdx));
            newValues.add(BeeUtils.toString(status.getOrderStatus().ordinal()));
          }
          Queries.update(formView.getViewName(), newRow.getId(), newRow.getVersion(),
              columns, oldValues, newValues, formView.getChildrenForUpdate(), new RowCallback() {
                @Override
                public void onSuccess(final BeeRow result) {
                  rowCallback.onSuccess(result);

                  if (isPrimaryRequest(result)) {
                    Queries.update(TBL_CARGO_ASSESSORS,
                        Filter.and(Filter.equals(COL_CARGO,
                            result.getLong(formView.getDataIndex(COL_CARGO))),
                            Filter.compareId(Operator.NE, result.getId())),
                        COL_STATUS, IntegerValue.of(status), new IntCallback() {
                          @Override
                          public void onSuccess(Integer res) {
                            if (status.isClosable()) {
                              DataChangeEvent.fire(BeeKeeper.getBus(), formView.getViewName(),
                                  DataChangeEvent.CANCEL_RESET_REFRESH);
                            } else {
                              DataChangeEvent.fireRefresh(BeeKeeper.getBus(), TBL_CARGO_ASSESSORS);
                            }
                          }
                        });
                  }
                }
              });
        }
      });
    }
  }

  public static void doRowAction(final RowActionEvent event) {
    if (event.hasView(TBL_CARGO_ASSESSORS)) {
      RowEditor.openRow(FORM_ASSESSMENT, Data.getDataInfo("Assessments"),
          event.getRowId(), false, null, null, null);

    } else if (event.hasView(VIEW_ASSESSMENT_FORWARDERS)) {
      Global.choice(Localized.getConstants().trContractPrinting(), Localized.getConstants()
          .chooseLanguage(),
          Lists.newArrayList("LT", "RU", "EN"),
          new ChoiceCallback() {
            @Override
            public void onSuccess(int value) {
              String printView = "PrintContract";

              RowEditor.openRow(printView + (value == 0 ? "LT" : (value == 1 ? "RU" : "EN")),
                  Data.getDataInfo(printView), event.getRowId(), true, null, null, new PrintForm());
            }
          });
    }
  }

  private static void updateTotals(final FormView form, IsRow row,
      final Widget incomeTotalWidget, final Widget expenseTotalWidget, final Widget profitWidget,
      final Widget incomeWidget, final Widget expenseWidget) {

    boolean ok = false;

    for (Widget widget : new Widget[] {incomeTotalWidget, expenseTotalWidget, profitWidget,
        incomeWidget, expenseWidget}) {
      if (widget != null) {
        ok = true;
        widget.getElement().setInnerText(null);
      }
    }
    if (!ok || !DataUtils.isId(row.getId())) {
      return;
    }
    ParameterList args = TransportHandler.createArgs(SVC_GET_ASSESSMENT_TOTALS);
    args.addDataItem(COL_CARGO, row.getLong(form.getDataIndex(COL_CARGO)));
    args.addDataItem(COL_ASSESSOR, row.getId());

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(form);

        if (response.hasErrors()) {
          return;
        }
        SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());

        double incomeTotal = BeeUtils.round(BeeUtils
            .toDouble(rs.getValueByKey(COL_SERVICE, TBL_CARGO_INCOMES, VAR_TOTAL)), 2);
        double expenseTotal = BeeUtils.round(BeeUtils
            .toDouble(rs.getValueByKey(COL_SERVICE, TBL_CARGO_EXPENSES, VAR_TOTAL)), 2);
        double income = BeeUtils.round(BeeUtils
            .toDouble(rs.getValueByKey(COL_SERVICE, TBL_CARGO_INCOMES, COL_AMOUNT)), 2);
        double expense = BeeUtils.round(BeeUtils
            .toDouble(rs.getValueByKey(COL_SERVICE, TBL_CARGO_EXPENSES, COL_AMOUNT)), 2);

        if (incomeTotalWidget != null) {
          incomeTotalWidget.getElement().setInnerText(BeeUtils.toString(incomeTotal));
        }
        if (expenseTotalWidget != null) {
          expenseTotalWidget.getElement().setInnerText(BeeUtils.toString(expenseTotal));
        }
        if (profitWidget != null) {
          profitWidget.getElement()
              .setInnerText(BeeUtils.toString(BeeUtils.round(incomeTotal - expenseTotal, 2)));
        }
        if (incomeWidget != null) {
          incomeWidget.getElement()
              .setInnerText(income != 0 ? BeeUtils.parenthesize(income) : null);
        }
        if (expenseWidget != null) {
          expenseWidget.getElement()
              .setInnerText(expense != 0 ? BeeUtils.parenthesize(expense) : null);
        }
      }
    });
  }

  private IsRow currentRow;

  private final Button cmdNew = new Button(Localized.getConstants().crmRequest(),
      new StatusUpdater(AssessmentStatus.NEW, null));

  private final Button cmdLost = new Button(Localized.getConstants().trAssessmentStatusLost(),
      new StatusUpdater(AssessmentStatus.LOST, null));

  private final Button cmdAnswered = new Button(Localized.getConstants()
      .trAssessmentStatusAnswered(),
      new StatusUpdater(AssessmentStatus.ANSWERED, Localized.getConstants()
          .trAssessmentThereUnconfirmedAssessments()));

  private final Button cmdActive = new Button(Localized.getConstants()
      .trOrder(),
      new StatusUpdater(AssessmentStatus.ACTIVE, null));

  private final Button cmdCompleted = new Button(Localized.getConstants()
      .trAssessmentStatusCompleted(),
      new StatusUpdater(AssessmentStatus.COMPLETED, Localized.getConstants()
          .trAssessmentThereActiveChildrenOrders()));

  private final Button cmdCanceled = new Button(Localized.getConstants()
      .trAssessmentStatusCanceled(),
      new StatusUpdater(AssessmentStatus.CANCELED, null));

  private final List<ChildGrid> grids = Lists.newArrayList();

  @Override
  public void afterCreateWidget(final String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      AssessmentGrid interceptor = null;

      if (BeeUtils.same(name, VIEW_ASSESSMENT_FORWARDERS)) {
        interceptor = new ForwardersGrid();

      } else if (BeeUtils.inListSame(name, TBL_CARGO_INCOMES, TBL_CARGO_EXPENSES)) {
        interceptor = new ServicesGrid();

      } else if (BeeUtils.same(name, TBL_CARGO_ASSESSORS)) {
        interceptor = new AssessorsGrid();
      }
      if (interceptor != null) {
        ChildGrid grid = (ChildGrid) widget;
        grid.setGridInterceptor(interceptor);
        grids.add(grid);
      }
    } else if (widget instanceof DataSelector && BeeUtils.same(name, COL_CURRENCY)) {
      final FormView form = getFormView();
      final DataSelector selector = (DataSelector) widget;

      selector.addEditStopHandler(new EditStopEvent.Handler() {
        @Override
        public void onEditStop(EditStopEvent event) {
          if (event.isChanged() && DataUtils.isId(currentRow.getId())) {
            int idx = form.getDataIndex(COL_CURRENCY);
            String oldCurrency = form.getOldRow().getString(idx);
            String newCurrency = selector.getNormalizedValue();

            if (!BeeUtils.same(oldCurrency, newCurrency)) {
              final String viewName = form.getViewName();

              Queries.update(viewName, form.getOldRow().getId(), form.getOldRow().getVersion(),
                  Data.getColumns(viewName, Lists.newArrayList(COL_CURRENCY)),
                  Lists.newArrayList(oldCurrency), Lists.newArrayList(newCurrency), null,
                  new RowCallback() {
                    @Override
                    public void onSuccess(BeeRow row) {
                      RowUpdateEvent.fire(BeeKeeper.getBus(), viewName, row);
                      DataChangeEvent.fireRefresh(BeeKeeper.getBus(), TBL_CARGO_ASSESSORS);
                    }
                  });
            }
          }
        }
      });
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT
        && (!DataUtils.isId(currentRow.getId()) || !isPrimaryRequest(currentRow))) {
      return false;
    }
    return super.beforeAction(action, presenter);
  }

  @Override
  public void afterRefresh(final FormView form, final IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (currentRow == null) {
      return;
    }
    boolean newRecord = !DataUtils.isId(currentRow.getId());
    boolean primary = isPrimaryRequest(currentRow);
    boolean owner = Objects.equal(currentRow.getLong(form.getDataIndex(COL_ASSESSOR_MANAGER)),
        BeeKeeper.getUser().getUserId());
    int status = currentRow.getInteger(form.getDataIndex(COL_STATUS));

    String caption = AssessmentStatus.in(status,
        AssessmentStatus.ACTIVE, AssessmentStatus.COMPLETED, AssessmentStatus.CANCELED)
        ? (primary ? Localized.getConstants().trOrder() : Localized.getConstants().trChildOrder())
        : (primary ? null : Localized.getConstants().trAssessment());

    if (!BeeUtils.isEmpty(caption)) {
      header.setCaption(caption);
    }
    if (owner && !newRecord) {
      header.addCommandItem(new Button(Localized.getConstants().trWriteEmail(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Element div = Document.get().createDivElement();
          StyleUtils.setWhiteSpace(div, WhiteSpace.PRE_WRAP);
          div.setInnerHTML("\n\n---\n"
              + BeeUtils.joinWords(row.getString(form.getDataIndex("FirstName")),
                  row.getString(form.getDataIndex("LastName"))));

          Set<Long> recipient = null;
          Long addr = row.getLong(form.getDataIndex("PersonEmail"));

          if (addr == null) {
            addr = row.getLong(form.getDataIndex("CustomerEmail"));
          }
          if (addr != null) {
            recipient = Sets.newHashSet(addr);
          }
          NewMailMessage.create(recipient, null, null, null, div.getString(), null);
        }
      }));
      if (AssessmentStatus.NEW.is(status)) {
        header.addCommandItem(cmdAnswered);
      }
      if (primary) {
        if (AssessmentStatus.in(status, AssessmentStatus.NEW, AssessmentStatus.ANSWERED)) {
          header.addCommandItem(cmdLost);
        }
        if (!AssessmentStatus.NEW.is(status)) {
          header.addCommandItem(cmdNew);
        }
        if (AssessmentStatus.ANSWERED.is(status)) {
          header.addCommandItem(cmdActive);
        }
        if (AssessmentStatus.ACTIVE.is(status)) {
          header.addCommandItem(cmdCanceled);
        }
      }
      if (AssessmentStatus.ACTIVE.is(status)) {
        header.addCommandItem(cmdCompleted);
      }
    }
    boolean ok = owner
        && AssessmentStatus.in(status, AssessmentStatus.NEW, AssessmentStatus.ACTIVE);

    form.setEnabled(ok && primary);

    for (ChildGrid grid : grids) {
      grid.setEnabled(ok);
    }
    Widget orderPanel = form.getWidgetByName("AmountsPanel");

    if (orderPanel != null) {
      orderPanel.getElement().getStyle()
          .setVisibility((primary && !newRecord) ? Visibility.VISIBLE : Visibility.HIDDEN);
    }
    refreshTotals();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new PrintForm() {
      @Override
      public void beforeRefresh(FormView form, IsRow row) {
        super.beforeRefresh(form, row);
        updateTotals(form, row, form.getWidgetByName(VAR_TOTAL), null, null, null, null);
      }
    };
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentForm();
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    currentRow = row;
  }

  private boolean isPrimaryRequest(IsRow row) {
    return !DataUtils.isId(row.getLong(getFormView().getDataIndex(COL_ASSESSOR)));
  }

  private void refreshTotals() {
    final FormView form = getFormView();

    updateTotals(form, currentRow, form.getWidgetByName(VAR_INCOME + VAR_TOTAL),
        form.getWidgetByName(VAR_EXPENSE + VAR_TOTAL), form.getWidgetByName("Profit"),
        form.getWidgetByName(VAR_INCOME), form.getWidgetByName(VAR_EXPENSE));
  }
}
