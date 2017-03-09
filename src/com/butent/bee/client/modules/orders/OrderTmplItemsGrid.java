package com.butent.bee.client.modules.orders;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.grid.column.CalculatedColumn;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.client.modules.trade.acts.ItemPricePicker;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Latch;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    if (BeeUtils.inList(columnName, COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT, COL_TRADE_VAT,
        COL_TRADE_VAT_PERC, COL_TRADE_VAT_PLUS) && editableColumn != null) {

      editableColumn.addCellValidationHandler(event -> {
        CellValidation cv = event.getCellValidation();
        IsRow row = cv.getRow();

        List<BeeColumn> cols = Lists.newArrayList(cv.getColumn());
        List<String> oldValues = Lists.newArrayList(cv.getOldValue());
        List<String> newValues = Lists.newArrayList(cv.getNewValue());

        Queries.update(getViewName(), row.getId(), row.getVersion(), cols, oldValues,
            newValues, null, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                if (OrdersKeeper.isComponent(result, VIEW_ORDER_TMPL_ITEMS)) {
                  OrdersKeeper.recalculateComplectPrice(result, VIEW_ORDER_TMPL_ITEMS,
                      COL_TEMPLATE);
                  return;
                }
                DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_ORDER_TMPL_ITEMS);
              }
            });
        return false;
      });
    } else if (column instanceof CalculatedColumn && "ItemPrices".equals(columnName)) {
      int index = DataUtils.getColumnIndex(COL_TRADE_ITEM_PRICE, dataColumns);
      CellSource cellSource = CellSource.forColumn(dataColumns.get(index), index);

      ItemPricePicker ipp = new ItemPricePicker(cellSource, dataColumns, null);
      ((HasCellRenderer) column).setRenderer(ipp);
    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  @Override
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {
    if (Objects.equals(column.getId(), COL_TRADE_ITEM_QUANTITY)) {
      if (OrdersKeeper.isComplect(getActiveRow())) {
        updateComplectItemsQuantity(BeeUtils.toDouble(oldValue), BeeUtils.toDouble(newValue));
      }
    }
    super.afterUpdateCell(column, oldValue, newValue, result, rowMode);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    IsRow parentRow = ViewHelper.getFormRow(presenter.getMainView());
    ensurePicker().show(parentRow, presenter.getMainView().getElement());

    return false;
  }

  @Override
  public DeleteMode beforeDeleteRow(GridPresenter presenter, IsRow row) {
    if (OrdersKeeper.isComplect(row)) {
      Queries.delete(VIEW_ORDER_TMPL_ITEMS, Filter.and(Filter.equals(COL_TEMPLATE,
          row.getLong(getDataIndex(COL_TEMPLATE))), Filter.equals(COL_TRADE_ITEM_PARENT,
          row.getId())), new Queries.IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              Queries.deleteRow(VIEW_ORDER_TMPL_ITEMS, row.getId(), new Queries.IntCallback() {
                @Override
                public void onSuccess(Integer result) {
                  DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_ORDER_TMPL_ITEMS);
                }
              });
            }
          });
      return DeleteMode.CANCEL;
    } else {
      return super.beforeDeleteRow(presenter, row);
    }
  }

  @Override
  public void onEditEnd(EditEndEvent event, Object source) {
    if (event.getColumn() != null) {
      if (Objects.equals(event.getColumn().getId(), COL_TRADE_ITEM_QUANTITY)) {
        String newValue = event.getNewValue();

        if (OrdersKeeper.isComplect(getActiveRow())) {
          newValue = BeeUtils.round(newValue, 0);

          if (!BeeUtils.isPositive(BeeUtils.toDouble(newValue))) {
            getGridPresenter().getGridView().notifySevere(Localized.dictionary().minValue()
                + " >= 1");
            event.consume();
            return;
          }

          event.setNewValue(newValue);
        }

        if (!BeeUtils.isPositive(BeeUtils.toDouble(newValue))) {
          getGridPresenter().getGridView().notifySevere(Localized.dictionary().minValue() + " > 0");
          event.consume();
          return;
        }

        if (isComplectTmplItemsGrid()) {
          event.consume();
          return;
        }
      }
    }

    super.onEditEnd(event, source);
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (BeeUtils.same(event.getColumnId(), "Plus")
        && OrdersKeeper.isComplect(event.getRowValue())) {
      IsRow row = event.getRowValue();

      GridPanel gridPanel = new GridPanel(GRID_ORDER_TMPL_COMPLECT_ITEMS, GridFactory.GridOptions.
          forFilter(Filter.and(Filter.equals(COL_TEMPLATE, row.getLong(getDataIndex(COL_TEMPLATE))),
              Filter.equals(COL_TRADE_ITEM_PARENT, row.getId()))), false);

      gridPanel.setGridInterceptor(new OrderTmplItemsGrid());

      StyleUtils.setWidth(gridPanel, BeeKeeper.getScreen().getWidth() * 0.9, CssUnit.DEFAULT);
      StyleUtils.setHeight(gridPanel, BeeKeeper.getScreen().getHeight() * 0.5, CssUnit.DEFAULT);

      DialogBox dialog = DialogBox.create(null);
      dialog.setWidget(gridPanel);
      dialog.setAnimationEnabled(true);
      dialog.setHideOnEscape(true);
      dialog.center();
    }

    super.onEditStart(event);
  }

  private OrderTmplItemsPicker ensurePicker() {
    if (picker == null) {
      picker = new OrderTmplItemsPicker();
      picker.addSelectionHandler(this);
    }
    return picker;
  }

  private boolean isComplectTmplItemsGrid() {
    return Objects.equals(GRID_ORDER_TMPL_COMPLECT_ITEMS, getGridView().getGridName());
  }

  private static void maybeInsertComplects(BeeRowSet rowSet, BeeRowSet complects) {
    final int parentIdx = rowSet.getColumnIndex(COL_TRADE_ITEM_PARENT);
    final int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    final int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    final int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    final int vatIdx = rowSet.getColumnIndex(COL_TRADE_VAT);

    Totalizer totalizer = new Totalizer(rowSet.getColumns());

    for (BeeRow complect : complects) {
      double price = BeeConst.DOUBLE_ZERO;
      double vat = BeeConst.DOUBLE_ZERO;

      List<BeeRow> components = DataUtils.filterRows(rowSet, COL_TRADE_ITEM_PARENT,
          complect.getLong(itemIndex));

      for (BeeRow row : components) {
        price += BeeUtils.unbox(totalizer.getTotal(row));
        vat += BeeUtils.unbox(totalizer.getVat(row));
      }

      if (price > 0) {
        complect.setValue(priceIndex, price / complect.getDouble(qtyIndex));
        complect.setValue(vatIdx, BeeUtils.round(vat, 2));
      }
    }

    if (complects.getNumberOfRows() > 0) {
      Latch latch = new Latch(complects.getNumberOfRows());
      for (BeeRow complect : complects) {
        Queries.insert(VIEW_ORDER_TMPL_ITEMS, rowSet.getColumns(), complect, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                for (BeeRow row : rowSet) {
                  if (Objects.equals(row.getLong(parentIdx), complect.getLong(itemIndex))) {
                    row.setValue(parentIdx, result.getId());
                  }
                }
                latch.decrement();
                if (latch.isOpen()) {
                  Queries.insertRows(rowSet);
                }
              }
            });
      }
    } else {
      Queries.insertRows(rowSet);
    }
  }

  private void updateComplectItemsQuantity(double oldValue, double newValue) {
    if (newValue < 1) {
      getGridPresenter().getGridView().notifySevere(Localized.dictionary().minValue() + " 1");
      return;
    }

    IsRow row = getActiveRow();
    Long template = row.getLong(getDataIndex(COL_TEMPLATE));

    Queries.getRowSet(VIEW_ORDER_TMPL_ITEMS, Collections.singletonList(COL_TRADE_ITEM_QUANTITY),
        Filter.and(Filter.equals(COL_TEMPLATE, template), Filter.equals(COL_TRADE_ITEM_PARENT,
            row.getId())), new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {

            for (BeeRow row : result) {
              double qty = BeeUtils.unbox(row.getDouble(0)) / oldValue;
              row.setValue(0, qty * newValue);
            }

            Queries.updateRows(result, new RpcCallback<RowInfoList>() {
              @Override
              public void onSuccess(RowInfoList result) {
                OrdersKeeper.recalculateComplectPrice(row.getId(), template,
                    VIEW_ORDER_TMPL_ITEMS, COL_TEMPLATE);
              }
            });
      }
    });
  }

  private void addItems(final BeeRowSet rowSet) {
    if (!DataUtils.isEmpty(rowSet) && VIEW_ITEMS.equals(rowSet.getViewName())) {
      getGridView().ensureRelId(result -> {
        FormView form = ViewHelper.getForm(getGridView());
        IsRow parentRow = (form == null) ? null : form.getActiveRow();
        Map<Long, Double> complects = new HashMap<>();

        for (BeeRow item : rowSet) {
          if (BeeUtils.isDouble(item.getPropertyDouble(PRP_QUANTITY))) {
            Integer componentsCount = item.getPropertyInteger(PROP_ITEM_COMPONENT);
            if (BeeUtils.isPositive(componentsCount)) {
              complects.put(item.getId(), item.getPropertyDouble(PRP_QUANTITY));
            }
          }
        }

        if (complects.size() > 0) {
          ParameterList params = OrdersKeeper.createSvcArgs(SVC_FILTER_COMPONENTS);
          params.addDataItem(COL_SOURCE, COL_TEMPLATE);
          params.addDataItem(COL_ITEM_COMPLECT, Codec.beeSerialize(complects));

          Long warehouse = parentRow.getLong(Data.getColumnIndex(VIEW_ORDERS, COL_WAREHOUSE));
          if (DataUtils.isId(warehouse)) {
            params.addDataItem(COL_WAREHOUSE, warehouse);
          }

          params.addDataItem(OrdersConstants.COL_ORDER, result);

          BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              if (!response.hasErrors()) {
                BeeRowSet components = BeeRowSet.restore(response.getResponseAsString());
                components.addRows(rowSet.getRows());
                addItems(parentRow, form.getDataColumns(), components);
              }
            }
          });
        } else {
          addItems(parentRow, form.getDataColumns(), rowSet);
        }
      });
    }
  }

  private void addItems(IsRow parentRow, List<BeeColumn> parentColumns, BeeRowSet items) {

    List<String> colNames = Lists.newArrayList(COL_TEMPLATE, COL_TA_ITEM, COL_TRADE_ITEM_QUANTITY,
        COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT, COL_TRADE_VAT, COL_TRADE_VAT_PERC,
        COL_TRADE_VAT_PLUS, COL_INVISIBLE_DISCOUNT, COL_TRADE_ITEM_PARENT);

    final BeeRowSet rowSet = new BeeRowSet(getViewName(), Data.getColumns(getViewName(), colNames));
    final BeeRowSet complects = new BeeRowSet(getViewName(), Data.getColumns(getViewName(),
        colNames));

    final int tmplIndex = rowSet.getColumnIndex(COL_TEMPLATE);
    final int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    final int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    final int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    final int discountIndex = rowSet.getColumnIndex(COL_TRADE_DISCOUNT);
    final int invisibleDiscountIndex = rowSet.getColumnIndex(COL_INVISIBLE_DISCOUNT);
    final int vatIdx = rowSet.getColumnIndex(COL_TRADE_VAT);
    final int vatPrcIndex = rowSet.getColumnIndex(COL_TRADE_VAT_PERC);
    final int parentIdx = rowSet.getColumnIndex(COL_TRADE_ITEM_PARENT);

    final int itemPriceIdx = items.getColumnIndex(COL_TRADE_ITEM_PRICE);
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

        if (BeeUtils.unbox(item.getBoolean(vatItemIdx))
            && !BeeUtils.isPositive(item.getPropertyInteger(PROP_ITEM_COMPONENT))) {

          if (BeeUtils.unbox(item.getInteger(vatPrcItemIdx)) > 0) {
            row.setValue(vatIdx, item.getInteger(vatPrcItemIdx));
          } else {
            row.setValue(vatIdx, item.getInteger(vatPrcDefaultIdx));
          }
          row.setValue(vatPrcIndex, true);
        }

        if (item.hasPropertyValue(COL_TRADE_ITEM_PARENT)) {
          row.setValue(parentIdx, Long.valueOf(item.getProperty(COL_TRADE_ITEM_PARENT)));
        }

        if (BeeUtils.isPositive(item.getPropertyInteger(PROP_ITEM_COMPONENT))) {
          complects.addRow(row);
        } else {
          row.setValue(priceIndex, item.getDouble(itemPriceIdx));
          rowSet.addRow(row);
        }
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
          input -> {
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
            maybeInsertComplects(rowSet, complects);
          });
    } else if (!rowSet.isEmpty()) {
      maybeInsertComplects(rowSet, complects);
    }
  }

  @Override
  public void onSelection(SelectionEvent<BeeRowSet> event) {
    addItems(event.getSelectedItem());
  }
}
