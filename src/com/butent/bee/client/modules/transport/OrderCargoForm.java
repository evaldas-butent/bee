package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_CURRENCY;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.transport.TransportHandler.Profit;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
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
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Set;

class OrderCargoForm extends AbstractFormInterceptor implements SelectorEvent.Handler {

  private FaLabel copyAction;

  static void preload(Runnable command) {
    Long typeId = Global.getParameterRelation(PRM_CARGO_TYPE);

    if (DataUtils.isId(typeId)) {
      Queries.getRow(VIEW_CARGO_TYPES, typeId, new RowCallback() {
        @Override
        public void onFailure(String... reason) {
          super.onFailure(reason);
          defaultCargoType = null;
          command.run();
        }

        @Override
        public void onSuccess(BeeRow result) {
          defaultCargoType = result;
          command.run();
        }
      });
    } else {
      defaultCargoType = null;
      command.run();
    }
  }

  private static IsRow defaultCargoType;
  private final Set<Long> expensesChangeQueue = new HashSet<>();
  private ListBox insuranceCertificate;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      switch (name) {
        case TBL_CARGO_INCOMES:
          ((ChildGrid) widget).setGridInterceptor(new CargoIncomesGrid() {
            @Override
            public boolean previewModify(Set<Long> rowIds) {
              if (super.previewModify(rowIds)) {
                Data.refreshLocal(TBL_CARGO_TRIPS);
                return true;
              }
              return false;
            }

            @Override
            public void afterCreateEditor(String source, Editor editor, boolean embedded) {
              if ((BeeUtils.same(source, COL_CARGO_INCOME) || BeeUtils.same(source, COL_SERVICE))
                  && editor instanceof DataSelector) {

                ((DataSelector) editor).addSelectorHandler(OrderCargoForm.this);

              } else if (BeeUtils.same(source, COL_INSURANCE_CERTIFICATE)
                  && editor instanceof ListBox) {
                insuranceCertificate = (ListBox) editor;
              }
              super.afterCreateEditor(source, editor, embedded);
            }

            @Override
            public void afterInsertRow(IsRow result) {
              if (BeeUtils.same(getViewName(), TBL_CARGO_INCOMES)) {
                createPercentExpense(result);
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
              FormView form = getFormView();

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
                  new RowInsertCallback(expensesView.getViewName(), form.getId()));
            }
          });
          break;

        case TBL_CARGO_EXPENSES:
          ((ChildGrid) widget).setGridInterceptor(new CargoExpensesGrid());
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
                  new String[] {BeeUtils.toString(amount), BeeUtils.toString(currency)});
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
    if (BeeUtils.containsSame(event.getRelatedViewName(), TBL_SERVICES)) {
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

    if (!DataUtils.isNewRow(row)) {
      header.addCommandItem(getCopyAction());
    }

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

  private IdentifiableWidget getCopyAction() {
    if (copyAction == null) {
      copyAction = new FaLabel(FontAwesome.COPY);
      copyAction.setTitle(Localized.dictionary().actionCopy());

      copyAction.addClickHandler(clickEvent -> {
        final Long orderId = getLongValue(COL_ORDER);

        if (DataUtils.isId(orderId)) {
          TransportUtils.copyOrderWithCargos(orderId,
              Filter.compareId(getActiveRowId()), (newOrderId, newCargos) ->
                  RowEditor.open(getViewName(), BeeUtils.peek(newCargos).getId(), Opener.MODAL));
        }
      });
    }
    return copyAction;
  }
}