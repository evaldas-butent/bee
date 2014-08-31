package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class TradeActItemsGrid extends AbstractGridInterceptor implements
    SelectionHandler<BeeRowSet> {

  private TradeActItemPicker picker;

  TradeActItemsGrid() {
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    Button command = new Button(Localized.getConstants().actionImport());
    command.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        onImport();
      }
    });

    presenter.getHeader().addCommandItem(command);

    super.afterCreatePresenter(presenter);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    IsRow parentRow = UiHelper.getFormRow(presenter.getMainView());
    if (parentRow != null) {
      ensurePicker().show(parentRow, presenter.getMainView().getElement());
    }

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActItemsGrid();
  }

  @Override
  public void onSelection(SelectionEvent<BeeRowSet> event) {
    final BeeRowSet rowSet = event.getSelectedItem();

    if (!DataUtils.isEmpty(rowSet) && VIEW_ITEMS.equals(rowSet.getViewName())) {
      getGridView().ensureRelId(new IdCallback() {
        @Override
        public void onSuccess(Long result) {
          IsRow parentRow = UiHelper.getFormRow(getGridView());

          if (DataUtils.idEquals(parentRow, result)) {
            ItemPrice itemPrice = TradeActKeeper.getItemPrice(VIEW_TRADE_ACTS, parentRow);

            DateTime date = Data.getDateTime(VIEW_TRADE_ACTS, parentRow, COL_TA_DATE);
            Long currency = Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CURRENCY);

            addItems(parentRow, date, currency, itemPrice, getDefaultDiscount(), rowSet);
          }
        }
      });
    }
  }

  private void addItems(IsRow parentRow, DateTime date, Long currency, ItemPrice itemPrice,
      Double discount, BeeRowSet items) {

    List<String> colNames = Lists.newArrayList(COL_TRADE_ACT, COL_TA_ITEM,
        COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT);
    BeeRowSet rowSet = new BeeRowSet(getViewName(), Data.getColumns(getViewName(), colNames));

    int actIndex = rowSet.getColumnIndex(COL_TRADE_ACT);
    int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    int discountIndex = rowSet.getColumnIndex(COL_TRADE_DISCOUNT);

    for (BeeRow item : items) {
      Double qty = BeeUtils.toDoubleOrNull(item.getProperty(PRP_QUANTITY));

      if (BeeUtils.isDouble(qty)) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());

        row.setValue(actIndex, parentRow.getId());
        row.setValue(itemIndex, item.getId());

        row.setValue(qtyIndex, qty);

        if (itemPrice != null) {
          Double price = item.getDouble(items.getColumnIndex(itemPrice.getPriceColumn()));

          if (BeeUtils.isDouble(price)) {
            if (DataUtils.isId(currency)) {
              Long ic = item.getLong(items.getColumnIndex(itemPrice.getCurrencyColumn()));
              if (DataUtils.isId(ic) && !currency.equals(ic)) {
                price = Money.exchange(ic, currency, price, date);
              }
            }

            row.setValue(priceIndex, Data.round(getViewName(), COL_TRADE_ITEM_PRICE, price));
          }
        }

        if (BeeUtils.nonZero(discount)) {
          row.setValue(discountIndex, discount);
        }

        rowSet.addRow(row);
      }
    }

    if (!rowSet.isEmpty()) {
      Queries.insertRows(rowSet);
    }
  }

  private TradeActItemPicker ensurePicker() {
    if (picker == null) {
      picker = new TradeActItemPicker();
      picker.addSelectionHandler(this);
    }

    return picker;
  }

  private Double getDefaultDiscount() {
    IsRow row = getGridView().getActiveRow();
    if (row == null) {
      row = BeeUtils.getLast(getGridView().getRowData());
    }

    return (row == null) ? null : row.getDouble(getDataIndex(COL_TRADE_DISCOUNT));
  }

  private void onImport() {

  }
}
