package com.butent.bee.client.modules.orders;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.grid.column.CalculatedColumn;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.client.modules.trade.TotalRenderer;
import com.butent.bee.client.modules.trade.acts.ItemPricePicker;
import com.butent.bee.client.modules.trade.acts.QuantityReader;
import com.butent.bee.client.modules.transport.InvoiceCreator;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.orders.OrdersConstants.OrdersStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderItemsGrid extends AbstractGridInterceptor implements SelectionHandler<BeeRowSet> {

  Long orderForm;
  private OrderItemsPicker picker;
  private Flow invoice = new Flow();

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    presenter.getHeader().addCommandItem(invoice);
    super.afterCreatePresenter(presenter);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    IsRow parentRow = ViewHelper.getFormRow(presenter.getMainView());
    ensurePicker().show(parentRow, presenter.getMainView().getElement());

    return false;
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (BeeUtils.inList(columnName, COL_RESERVED_REMAINDER, COL_TRADE_ITEM_QUANTITY)
        && editableColumn != null) {
      editableColumn.addCellValidationHandler(new CellValidateEvent.Handler() {

        @Override
        public Boolean validateCell(CellValidateEvent event) {
          if (event.isCellValidation() && event.isPostValidation()) {
            CellValidation cv = event.getCellValidation();
            IsRow row = cv.getRow();
            Double freeRem = BeeUtils.toDouble(row.getProperty(PRP_FREE_REMAINDER));
            Double qty =
                row.getDouble(Data.getColumnIndex(VIEW_ORDER_ITEMS, COL_TRADE_ITEM_QUANTITY));
            Double newValue = BeeUtils.toDouble(cv.getNewValue());
            Double oldValue = BeeUtils.toDouble(cv.getOldValue());

            switch (event.getColumnId()) {
              case COL_RESERVED_REMAINDER:

                if (freeRem == 0) {
                  if (newValue > oldValue) {
                    getGridPresenter().getGridView().notifySevere(
                        Localized.getConstants().ordResNotIncrease());
                    return false;
                  }

                } else if (newValue < 0) {
                  getGridPresenter().getGridView().notifySevere(
                      Localized.getConstants().minValue() + " 0");
                  return false;

                } else if (newValue > qty || newValue > freeRem) {
                  getGridPresenter().getGridView().notifySevere(
                      Localized.getConstants().ordResQtyIsTooBig());
                  return false;
                }

                break;

              case COL_TRADE_ITEM_QUANTITY:

                if (newValue < 1) {
                  getGridPresenter().getGridView().notifySevere(
                      Localized.getConstants().minValue() + " 1");
                  return false;
                }

                int updIndex = Data.getColumnIndex(VIEW_ORDER_ITEMS, COL_RESERVED_REMAINDER);
                Double updValue =
                    row.getDouble(updIndex) == null ? 0 : row.getDouble(updIndex);

                BeeColumn updColumn = Data.getColumn(VIEW_ORDER_ITEMS, COL_RESERVED_REMAINDER);

                if (newValue - oldValue <= freeRem && freeRem != 0) {
                  updValue += newValue - oldValue;
                } else if (newValue < updValue) {
                  updValue = newValue;
                } else {
                  updValue += freeRem;
                }

                if (updValue < 0) {
                  updValue = newValue;
                }

                List<BeeColumn> cols = Lists.newArrayList(cv.getColumn(), updColumn);
                List<String> oldValues = Lists.newArrayList(cv.getOldValue(),
                    row.getString(updIndex));
                List<String> newValues = Lists.newArrayList(cv.getNewValue(),
                    BeeUtils.toString(updValue));

                Queries.update(getViewName(), row.getId(), row.getVersion(), cols, oldValues,
                    newValues,
                    null, new RowUpdateCallback(getViewName()));
                return null;
            }

          }
          return true;
        }
      });
    } else if (column instanceof CalculatedColumn) {
      if ("ItemPrices".equals(columnName)) {

        int index = DataUtils.getColumnIndex(COL_TRADE_ITEM_PRICE, dataColumns);
        CellSource cellSource = CellSource.forColumn(dataColumns.get(index), index);

        ItemPricePicker ipp = new ItemPricePicker(cellSource, dataColumns, null);
        ((HasCellRenderer) column).setRenderer(ipp);

      } else {
        AbstractCellRenderer renderer = ((CalculatedColumn) column).getRenderer();
        if (renderer instanceof TotalRenderer) {
          configureRenderer(dataColumns, (TotalRenderer) renderer);
        }
      }
    }

    if (footer != null && footer.getRowEvaluator() instanceof TotalRenderer) {
      configureRenderer(dataColumns, (TotalRenderer) footer.getRowEvaluator());
    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  @Override
  public GridInterceptor getInstance() {
    return new OrderItemsGrid();
  }

  @Override
  public void onSelection(SelectionEvent<BeeRowSet> event) {
    addItems(event.getSelectedItem());
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    orderForm = event.getRowId();

    invoice.clear();

    if (DataUtils.isId(orderForm)) {
      boolean isOrder =
          (Data.getInteger(event.getViewName(), event.getRow(), COL_ORDERS_STATUS)
          == OrdersStatus.APPROVED
              .ordinal()) ? true : false;
      if (isOrder) {
        invoice.add(new InvoiceCreator(VIEW_ORDER_SALES, Filter.equals(COL_ORDER, orderForm)));
      }
    }

    super.onParentRow(event);
  }

  private static void configureRenderer(List<? extends IsColumn> dataColumns,
      TotalRenderer renderer) {

    int index = DataUtils.getColumnIndex(COL_TRADE_ITEM_QUANTITY, dataColumns);

    QuantityReader quantityReader = new QuantityReader(index);

    renderer.getTotalizer().setQuantityFunction(quantityReader);
  }

  private Double getDefaultDiscount() {
    IsRow row = getGridView().getActiveRow();
    if (row == null) {
      row = BeeUtils.getLast(getGridView().getRowData());
    }

    return (row == null) ? null : row.getDouble(getDataIndex(COL_TRADE_DISCOUNT));
  }

  private OrderItemsPicker ensurePicker() {
    if (picker == null) {
      picker = new OrderItemsPicker();
      picker.addSelectionHandler(this);
    }
    return picker;
  }

  private void addItems(final BeeRowSet rowSet) {
    if (!DataUtils.isEmpty(rowSet) && VIEW_ITEMS.equals(rowSet.getViewName())) {
      getGridView().ensureRelId(new IdCallback() {
        @Override
        public void onSuccess(Long result) {
          FormView form = ViewHelper.getForm(getGridView());
          IsRow parentRow = (form == null) ? null : form.getActiveRow();

          if (DataUtils.idEquals(parentRow, result)) {
            ItemPrice itemPrice = null;

            String ip = rowSet.getTableProperty(PRP_ITEM_PRICE);
            if (BeeUtils.isDigit(ip)) {
              itemPrice = EnumUtils.getEnumByIndex(ItemPrice.class, ip);
            }

            addItems(parentRow, form.getDataColumns(), itemPrice, getDefaultDiscount(), rowSet);
          }
        }
      });
    }
  }

  private void addItems(IsRow parentRow, List<BeeColumn> parentColumns, ItemPrice defPrice,
      Double discount, BeeRowSet items) {

    List<String> colNames = Lists.newArrayList(COL_ORDER, COL_TA_ITEM,
        COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT, COL_RESERVED_REMAINDER);
    final BeeRowSet rowSet = new BeeRowSet(getViewName(), Data.getColumns(getViewName(), colNames));

    final int ordIndex = rowSet.getColumnIndex(COL_ORDER);
    final int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    final int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    final int resRemIndex = rowSet.getColumnIndex(COL_RESERVED_REMAINDER);
    final int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    final int discountIndex = rowSet.getColumnIndex(COL_TRADE_DISCOUNT);

    Map<Long, Double> quantities = new HashMap<>();
    Map<Long, ItemPrice> priceNames = new HashMap<>();

    for (BeeRow item : items) {
      Double qty = BeeUtils.toDoubleOrNull(item.getProperty(PRP_QUANTITY));

      if (BeeUtils.isDouble(qty)) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());

        row.setValue(ordIndex, parentRow.getId());
        row.setValue(itemIndex, item.getId());

        row.setValue(qtyIndex, qty);
        row.setValue(resRemIndex, qty);

        quantities.put(item.getId(), qty);

        ItemPrice itemPrice = defPrice;

        String ip = item.getProperty(PRP_ITEM_PRICE);
        if (BeeUtils.isDigit(ip)) {
          itemPrice = EnumUtils.getEnumByIndex(ItemPrice.class, ip);
        }

        if (itemPrice != null) {
          Double price = item.getDouble(items.getColumnIndex(itemPrice.getPriceColumn()));
          if (BeeUtils.isDouble(price)) {
            row.setValue(priceIndex, Data.round(getViewName(), COL_TRADE_ITEM_PRICE, price));
          }

          priceNames.put(item.getId(), itemPrice);
        }

        if (BeeUtils.nonZero(discount)) {
          row.setValue(discountIndex, discount);
        }

        rowSet.addRow(row);
      }
    }

    Long company = DataUtils.getLong(parentColumns, parentRow, COL_COMPANY);

    if (!rowSet.isEmpty() && DataUtils.isId(company)) {
      Map<String, Long> options = new HashMap<>();

      options.put(COL_DISCOUNT_COMPANY, company);

      Long warehouse = DataUtils.getLong(parentColumns, parentRow, COL_WAREHOUSE);
      if (DataUtils.isId(warehouse)) {
        options.put(COL_DISCOUNT_WAREHOUSE, warehouse);
      }

      DateTime startDate = DataUtils.getDateTime(parentColumns, parentRow, "StartDate");
      if (startDate != null) {
        options.put(Service.VAR_TIME, startDate.getTime());
      }

      ClassifierKeeper.getPricesAndDiscounts(options, quantities.keySet(), quantities, priceNames,
          new Consumer<Map<Long, Pair<Double, Double>>>() {
            @Override
            public void accept(Map<Long, Pair<Double, Double>> input) {
              for (BeeRow row : rowSet) {
                Pair<Double, Double> pair = input.get(row.getLong(itemIndex));

                if (pair != null) {
                  Double price = pair.getA();
                  Double percent = pair.getB();
                  if (BeeUtils.isPositive(price)) {
                    row.setValue(priceIndex,
                        Data.round(getViewName(), COL_TRADE_ITEM_PRICE, price));
                  }

                  if (BeeUtils.isDouble(percent)) {
                    if (BeeUtils.nonZero(percent)) {
                      row.setValue(discountIndex, percent);
                    } else {
                      row.clearCell(discountIndex);
                    }
                  }
                }
              }

              Queries.insertRows(rowSet);
            }
          });

    } else if (!rowSet.isEmpty()) {
      Queries.insertRows(rowSet);
    }
  }
}
