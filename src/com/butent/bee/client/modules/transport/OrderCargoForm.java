package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_CURRENCY;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.transport.TransportHandler.Profit;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.IntegerLabel;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

class OrderCargoForm extends AbstractFormInterceptor implements SelectorEvent.Handler {

  static void preload(final ScheduledCommand command) {
    Global.getParameter(PRM_CARGO_TYPE, new Consumer<String>() {
      @Override
      public void accept(String input) {
        if (DataUtils.isId(input)) {
          Queries.getRow(VIEW_CARGO_TYPES, BeeUtils.toLong(input), new RowCallback() {
            @Override
            public void onFailure(String... reason) {
              super.onFailure(reason);
              defaultCargoType = null;
              command.execute();
            }

            @Override
            public void onSuccess(BeeRow result) {
              defaultCargoType = result;
              command.execute();
            }
          });

        } else {
          defaultCargoType = null;
          command.execute();
        }
      }
    });
  }

  private static IsRow defaultCargoType;
  private final Set<Long> expensesChangeQueue = new HashSet<>();
  private ListBox insuranceCertificate;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_CURRENCY) && widget instanceof DataSelector) {
      final DataSelector selector = (DataSelector) widget;

      selector.addEditStopHandler(event -> {
        if (event.isChanged()) {
          refresh(BeeUtils.toLongOrNull(selector.getNormalizedValue()));
        }
      });
    } else if (widget instanceof ChildGrid) {
      switch (name) {
        case TBL_CARGO_INCOMES:
          final FormView form = getFormView();

          ((ChildGrid) widget).setGridInterceptor(new TransportVatGridInterceptor() {
            @Override
            public void afterCreateEditor(String source, Editor editor, boolean embedded) {
              if ((BeeUtils.same(source, COL_CARGO_INCOME) || BeeUtils.same(source, COL_SERVICE))
                  && editor instanceof DataSelector) {

                ((DataSelector) editor).addSelectorHandler(OrderCargoForm.this);
              } else if (BeeUtils.same(source,
                  COL_INSURANCE_CERTIFICATE) && editor instanceof ListBox) {
                insuranceCertificate = (ListBox) editor;
              }

              super.afterCreateEditor(source, editor, embedded);
            }

            @Override
            public void afterDeleteRow(long rowId) {
              refresh(form.getLongValue(COL_CURRENCY));
            }

            @Override
            public void afterInsertRow(IsRow result) {

              if (BeeUtils.same(getViewName(), TBL_CARGO_INCOMES)) {
                createPercentExpense(result);
              }

              refresh(form.getLongValue(COL_CURRENCY));
            }

            @Override
            public void afterUpdateCell(IsColumn column, String oldValue, String newValue,
                IsRow result, boolean rowMode) {
              if (BeeUtils.inListSame(column.getId(), COL_DATE, COL_AMOUNT, COL_CURRENCY,
                  COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC)) {
                refresh(form.getLongValue(COL_CURRENCY));
              }
            }

            @Override
            public GridInterceptor getInstance() {
              return null;
            }

            @Override
            public void onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {
              super.onReadyForInsert(gridView, event);

              IsRow row = gridView.getActiveRow();
              Double servicePercent = row.getDouble(gridView.getDataIndex(COL_SERVICE_PERCENT));

              if (!BeeUtils.isPositive(servicePercent)) {
                return;
              }

              FormView pForm = ViewHelper.getForm(gridView.asWidget());
              Double cargoValue = pForm.getDoubleValue(COL_CARGO_VALUE);

              if (BeeUtils.isPositive(cargoValue)) {
                return;
              }

              pForm.notifySevere(Localized.dictionary().cargoValue(),
                  Localized.dictionary().valueRequired());

              event.consume();
            }

            private void createPercentExpense(IsRow gridRow) {
              GridView grid = getGridView();
              if (grid == null) {
                return;
              }

              double servicePercent = BeeUtils.unbox(
                  gridRow.getDouble(grid.getDataIndex(COL_SERVICE_PERCENT)));

              if (!BeeUtils.isPositive(servicePercent)) {
                return;
              }

              IsRow formRow = form.getActiveRow();

              double cargoValue = BeeUtils.unbox(
                  formRow.getDouble(form.getDataIndex(COL_CARGO_VALUE)));


              double expenseSum = getExpenseSum(cargoValue, servicePercent);

              final DataInfo expensesView = Data.getDataInfo(TBL_CARGO_EXPENSES);
              BeeRow expenseRow = RowFactory.createEmptyRow(expensesView, true);

              Long currency =
                  BeeUtils.nvl(formRow.getLong(form.getDataIndex(COL_CARGO_VALUE_CURRENCY)),
                      gridRow.getLong(grid.getDataIndex(COL_CURRENCY)));

              expenseRow.setValue(expensesView.getColumnIndex(COL_SERVICE),
                  gridRow.getValue(grid.getDataIndex(COL_SERVICE)));
              expenseRow.setValue(expensesView.getColumnIndex(COL_AMOUNT), expenseSum);
              expenseRow.setValue(expensesView.getColumnIndex(COL_TRADE_VAT_PLUS),
                  gridRow.getValue(grid.getDataIndex(COL_TRADE_VAT_PLUS)));
              expenseRow.setValue(expensesView.getColumnIndex(COL_TRADE_VAT),
                  gridRow.getValue(grid.getDataIndex(COL_TRADE_VAT)));
              expenseRow.setValue(expensesView.getColumnIndex(COL_TRADE_VAT_PERC),
                  gridRow.getValue(grid.getDataIndex(COL_TRADE_VAT_PERC)));
              expenseRow.setValue(expensesView.getColumnIndex(COL_CARGO),
                  gridRow.getValue(grid.getDataIndex(COL_CARGO)));
              expenseRow.setValue(expensesView.getColumnIndex(COL_DATE),
                  gridRow.getValue(grid.getDataIndex(COL_DATE)));
              expenseRow.setValue(expensesView.getColumnIndex(COL_COSTS_SUPPLIER),
                  gridRow.getValue(grid.getDataIndex(COL_SERVICE_INSURER)));
              expenseRow.setValue(expensesView.getColumnIndex(COL_CARGO_INCOME),
                  gridRow.getId());
              expenseRow.setValue(expensesView.getColumnIndex(COL_CURRENCY), currency);

              Queries.insert(expensesView.getViewName(), expensesView.getColumns(), expenseRow,
                  new RowCallback() {
                    @Override
                    public void onSuccess(BeeRow result) {
                      RowInsertEvent.fire(BeeKeeper.getBus(), expensesView.getViewName(), result,
                          form.getId());
                      refresh(form.getLongValue(COL_CURRENCY));
                    }
                  });
            }

          });
          break;

        case TBL_CARGO_EXPENSES:
          ((ChildGrid) widget).setGridInterceptor(new TransportVatGridInterceptor() {
            @Override
            public void afterCreateEditor(String source, Editor editor, boolean embedded) {
              if (BeeUtils.same(source, COL_CARGO_INCOME) && editor instanceof DataSelector) {
                ((DataSelector) editor).addSelectorHandler(OrderCargoForm.this);
              }
              super.afterCreateEditor(source, editor, embedded);
            }

            @Override
            public ColumnDescription beforeCreateColumn(GridView gridView,
                ColumnDescription descr) {
              if (!TransportHandler.bindExpensesToIncomes()
                  && Objects.equals(descr.getId(), COL_CARGO_INCOME)) {
                return null;
              }
              return super.beforeCreateColumn(gridView, descr);
            }

            @Override
            public GridInterceptor getInstance() {
              return null;
            }
          });
          break;

        case TBL_CARGO_LOADING:
        case TBL_CARGO_UNLOADING:
          ((ChildGrid) widget).addReadyHandler(re -> {
            GridView gridView = ViewHelper.getChildGrid(getFormView(), name);

            if (gridView != null) {
              gridView.getGrid().addMutationHandler(mu -> refreshKilometers());
            }
          });
          break;

        case VIEW_CARGO_TRIPS:
          ((ChildGrid) widget).setGridInterceptor(new CargoTripsGrid());
          break;

        case VIEW_TRIP_COSTS:
          ((ChildGrid) widget).setGridInterceptor(new TripCostsGrid(true));
          break;
      }
    }
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    refresh(row.getLong(form.getDataIndex(COL_CURRENCY)));
    refreshKilometers();

    Widget cmrWidget = form.getWidgetBySource(COL_CARGO_CMR);
    if (cmrWidget instanceof DataSelector) {
      Filter filter;

      if (DataUtils.hasId(row)) {
        filter = Filter.in(Data.getIdColumn(DocumentConstants.VIEW_DOCUMENTS),
            DocumentConstants.VIEW_RELATED_DOCUMENTS, DocumentConstants.COL_DOCUMENT,
            Filter.equals(COL_CARGO, row.getId()));
      } else {
        filter = Filter.isFalse();
      }

      ((DataSelector) cmrWidget).setAdditionalFilter(filter);
    }
  }

  @Override
  public void afterUpdateRow(IsRow row) {
    super.afterUpdateRow(row);

    if (!expensesChangeQueue.contains(row.getId())) {
      return;
    }

    expensesChangeQueue.remove(row.getId());

    if (!DataUtils.isId(row.getId())) {
      return;
    }

    final double cargoValue = BeeUtils.unbox(row.getDouble(getDataIndex(COL_CARGO_VALUE)));
    final Long valueCurrency = row.getLong(getDataIndex(COL_CARGO_VALUE_CURRENCY));

    Queries.getRowSet(TBL_CARGO_EXPENSES,
        Lists.newArrayList(COL_AMOUNT, COL_CURRENCY, COL_SERVICE_PERCENT,
            ALS_CARGO_INCOME_CURRENCY), Filter.and(Filter.equals(COL_CARGO, row.getId()),
            Filter.isNull(COL_PURCHASE), Filter.notNull(COL_SERVICE_PERCENT)),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet expenses) {
            if (expenses.isEmpty()) {
              return;
            }

            BeeRowSet updExpenses =
                new BeeRowSet(TBL_CARGO_EXPENSES, Data.getColumns(TBL_CARGO_EXPENSES,
                    Lists.newArrayList(COL_AMOUNT, COL_CURRENCY)));

            for (IsRow expense : expenses) {
              double percent = BeeUtils.unbox(
                  expense.getDouble(expenses.getColumnIndex(COL_SERVICE_PERCENT)));

              double amount = getExpenseSum(cargoValue, percent);

              Long incomeCurrency = expense.getLong(expenses.getColumnIndex(
                  ALS_CARGO_INCOME_CURRENCY));

              Long currency = BeeUtils.nvl(valueCurrency, incomeCurrency);

              updExpenses.addRow(expense.getId(), expense.getVersion(),
                  new String[]{BeeUtils.toString(amount), BeeUtils.toString(currency)});
            }

            Queries.updateRows(updExpenses);
          }
        });
  }

  @Override
  public FormInterceptor getInstance() {
    return new OrderCargoForm();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), TBL_CARGO_INCOMES) && event.isOpened()) {
      event.getSelector().setAdditionalFilter(Filter.equals(COL_CARGO, getActiveRowId()));
    } else if (BeeUtils.containsSame(event.getRelatedViewName(), TBL_SERVICES)) {
      DataInfo servicesView = Data.getDataInfo(TBL_SERVICES);
      IsRow row = event.getRelatedRow();

      if (row == null) {
        return;
      }

      Double servicePercent = row.getDouble(servicesView.getColumnIndex(COL_SERVICE_PERCENT));

      if (!BeeUtils.isPositive(servicePercent)) {
        if (insuranceCertificate != null) {
          insuranceCertificate.setEnabled(false);
        }

        return;
      } else if (insuranceCertificate != null) {
        insuranceCertificate.setEnabled(true);
      }

      Double cargoValue = getFormView().getDoubleValue(COL_CARGO_VALUE);

      if (BeeUtils.isPositive(cargoValue)) {
        return;
      }

      getFormView().notifySevere(Localized.dictionary().cargoValue(),
          Localized.dictionary().valueRequired());
    }
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    super.onSaveChanges(listener, event);
    IsRow oldRow = event.getOldRow();
    IsRow row = event.getNewRow();
    FormView form = getFormView();

    double oldCargoValue = BeeUtils.unbox(oldRow.getDouble(form.getDataIndex(COL_CARGO_VALUE)));
    long oldCargoCurrency = BeeUtils.unbox(
        oldRow.getLong(form.getDataIndex(COL_CARGO_VALUE_CURRENCY)));

    double cargoValue = BeeUtils.unbox(row.getDouble(form.getDataIndex(COL_CARGO_VALUE)));
    long cargoCurrency = BeeUtils.unbox(row.getLong(form.getDataIndex(COL_CARGO_VALUE_CURRENCY)));

    if (oldCargoValue == cargoValue && oldCargoCurrency == cargoCurrency) {
      return;
    }

    expensesChangeQueue.add(row.getId());
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (Data.isViewEditable(VIEW_CARGO_INVOICES)) {
      header.addCommandItem(new InvoiceCreator(VIEW_CARGO_SALES,
          Filter.equals(COL_CARGO, row.getId())));
    }
    header.addCommandItem(new Profit(COL_CARGO, BeeUtils.toString(row.getId())));

    return true;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    form.getViewPresenter().getHeader().clearCommandPanel();

    if (defaultCargoType != null) {
      RelationUtils.updateRow(Data.getDataInfo(form.getViewName()), COL_CARGO_TYPE, newRow,
          Data.getDataInfo(VIEW_CARGO_TYPES), defaultCargoType, true);
    }

    GridView gridView = getGridView();
    if (gridView != null) {
      FormView parentForm = ViewHelper.getForm(gridView.asWidget());

      if (parentForm != null && BeeUtils.same(parentForm.getFormName(), FORM_ORDER)
                                            && Data.isNull(form.getViewName(), newRow, COL_ORDER)) {
        IsRow parentRow = parentForm.getActiveRow();

        if (parentRow != null) {
          int idxParCmp = parentForm.getDataIndex(COL_CUSTOMER);
          int idxParCmpName = parentForm.getDataIndex(COL_CUSTOMER_NAME);
          int idxParCmpTypeName = parentForm.getDataIndex(ALS_CUSTOMER_TYPE_NAME);

          if (!BeeConst.isUndef(idxParCmp) && !BeeConst.isUndef(idxParCmpName)) {
            newRow.setValue(
                Data.getColumnIndex(form.getViewName(), COL_CUSTOMER),
                parentRow.getLong(idxParCmp));
            newRow.setValue(
                Data.getColumnIndex(form.getViewName(), COL_CUSTOMER_NAME),
                BeeConst.isUndef(idxParCmpTypeName)
                    ? parentRow.getString(idxParCmpName)
                    : BeeUtils.joinWords(parentRow.getString(idxParCmpName),
                    parentRow.getString(idxParCmpTypeName)));
          }
        }
      }
    }
  }

  private static double getExpenseSum(double val, double percents) {
    return BeeUtils.round(val * percents / 100.0, 2);
  }

  private void refresh(Long currency) {
    final FormView form = getFormView();
    final Widget widget = form.getWidgetByName(COL_AMOUNT);

    if (widget != null) {
      widget.getElement().setInnerText(null);

      if (!DataUtils.isId(getActiveRow().getId())) {
        return;
      }
      ParameterList args = TransportHandler.createArgs(SVC_GET_CARGO_TOTAL);
      args.addDataItem(COL_CARGO, getActiveRow().getId());

      if (DataUtils.isId(currency)) {
        args.addDataItem(COL_CURRENCY, currency);
      }
      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(form);

          if (response.hasErrors()) {
            return;
          }
          widget.getElement().setInnerText(response.getResponseAsString());
        }
      });
      ChildGrid grid = (ChildGrid) getWidgetByName(VIEW_CARGO_TRIPS);

      if (Objects.nonNull(grid) && Objects.nonNull(grid.getPresenter())) {
        grid.getPresenter().refresh(false, false);
      }
    }
  }

  private void refreshKilometers() {
    Integer emptyKm = null;
    Integer loadedKm = null;

    for (String view : new String[] {TBL_CARGO_LOADING, TBL_CARGO_UNLOADING}) {
      GridView grid = ViewHelper.getChildGrid(getFormView(), view);

      if (grid != null && !grid.isEmpty()) {
        List<? extends IsRow> childRows = grid.getRowData();
        int emptyKmIndex = grid.getDataIndex(COL_EMPTY_KILOMETERS);
        int loadedKmIndex = grid.getDataIndex(COL_LOADED_KILOMETERS);

        int unplannedManagerKmIndex = grid.getDataIndex(COL_UNPLANNED_MANAGER_KM);
        int unplannedDriverKmIndex = grid.getDataIndex(COL_UNPLANNED_DRIVER_KM);

        for (IsRow childRow : childRows) {
          Integer v = childRow.getInteger(emptyKmIndex);
          if (v != null) {
            if (emptyKm == null) {
              emptyKm = v;
            } else {
              emptyKm += v;
            }
          }

          v = childRow.getInteger(loadedKmIndex);
          if (v != null) {
            if (loadedKm == null) {
              loadedKm = v;
            } else {
              loadedKm += v;
            }
          }

          v = childRow.getInteger(unplannedManagerKmIndex);
          if (v != null) {
            if (loadedKm == null) {
              loadedKm = v;
            } else {
              loadedKm += v;
            }
          }

          v = childRow.getInteger(unplannedDriverKmIndex);
          if (v != null) {
            if (loadedKm == null) {
              loadedKm = v;
            } else {
              loadedKm += v;
            }
          }
        }
      }
    }
    Widget widget = getFormView().getWidgetByName("TotalEmpty");
    if (widget instanceof IntegerLabel) {
      ((IntegerLabel) widget).setValue(emptyKm);
    }
    widget = getFormView().getWidgetByName("TotalLoaded");
    if (widget instanceof IntegerLabel) {
      ((IntegerLabel) widget).setValue(loadedKm);
    }
  }
}