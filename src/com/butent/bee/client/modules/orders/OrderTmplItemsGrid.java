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
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.grid.column.CalculatedColumn;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.client.modules.trade.TotalRenderer;
import com.butent.bee.client.modules.trade.acts.ItemPricePicker;
import com.butent.bee.client.modules.trade.acts.QuantityReader;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OrderTmplItemsGrid extends AbstractGridInterceptor implements
    SelectionHandler<BeeRowSet> {

  private OrderTmplItemsPicker picker;

  @Override
  public GridInterceptor getInstance() {
    return new OrderTmplItemsGrid();
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (column instanceof CalculatedColumn) {
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
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    IsRow parentRow = ViewHelper.getFormRow(presenter.getMainView());
    ensurePicker().show(parentRow, presenter.getMainView().getElement());

    return false;
  }

  private OrderTmplItemsPicker ensurePicker() {
    if (picker == null) {
      picker = new OrderTmplItemsPicker();
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
            addItems(parentRow, form.getDataColumns(), rowSet);
          }
        }
      });
    }
  }

  private void addItems(IsRow parentRow, List<BeeColumn> parentColumns, BeeRowSet items) {

    List<String> colNames = Lists.newArrayList(COL_TEMPLATE, COL_TA_ITEM, COL_TRADE_ITEM_QUANTITY,
        COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT, COL_TRADE_VAT, COL_TRADE_VAT_PERC,
        COL_INVISIBLE_DISCOUNT);

    final BeeRowSet rowSet = new BeeRowSet(getViewName(), Data.getColumns(getViewName(), colNames));

    final int tmplIndex = rowSet.getColumnIndex(COL_TEMPLATE);
    final int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    final int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    final int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    final int discountIndex = rowSet.getColumnIndex(COL_TRADE_DISCOUNT);
    final int invisibleDiscountIndex = rowSet.getColumnIndex(COL_INVISIBLE_DISCOUNT);
    final int vatIdx = rowSet.getColumnIndex(COL_TRADE_VAT);
    final int vatPrcIndex = rowSet.getColumnIndex(COL_TRADE_VAT_PERC);

    final int vatPrcItemIdx = items.getColumnIndex(COL_TRADE_VAT_PERC);
    final int vatPrcDefaultIdx = items.getNumberOfColumns() - 1;
    final int vatItemIdx = items.getColumnIndex(COL_TRADE_VAT);

    Map<Long, Double> quantities = new HashMap<>();
    Map<Long, ItemPrice> priceNames = new HashMap<>();

    for (BeeRow item : items) {
      Double qty = BeeUtils.toDoubleOrNull(item.getProperty(PRP_QUANTITY));

      if (BeeUtils.isDouble(qty)) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());

        row.setValue(tmplIndex, parentRow.getId());
        row.setValue(itemIndex, item.getId());

        row.setValue(qtyIndex, qty);

        quantities.put(item.getId(), qty);

        if (BeeUtils.unbox(item.getBoolean(vatItemIdx))) {

          if (BeeUtils.unbox(item.getInteger(vatPrcItemIdx)) > 0) {
            row.setValue(vatIdx, item.getInteger(vatPrcItemIdx));
          } else {
            row.setValue(vatIdx, item.getInteger(vatPrcDefaultIdx));
          }
          row.setValue(vatPrcIndex, true);
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
                      row.setValue(invisibleDiscountIndex, percent);
                    } else {
                      row.clearCell(discountIndex);
                      row.setValue(invisibleDiscountIndex, 0);
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

  private static void configureRenderer(List<? extends IsColumn> dataColumns,
      TotalRenderer renderer) {

    int index = DataUtils.getColumnIndex(COL_TRADE_ITEM_QUANTITY, dataColumns);

    QuantityReader quantityReader = new QuantityReader(index);

    renderer.getTotalizer().setQuantityFunction(quantityReader);
  }

  @Override
  public void onSelection(SelectionEvent<BeeRowSet> event) {
    addItems(event.getSelectedItem());
  }
}
