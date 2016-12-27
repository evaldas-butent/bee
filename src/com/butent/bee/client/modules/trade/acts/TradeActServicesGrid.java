package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;
import java.util.Objects;

public class TradeActServicesGrid extends AbstractGridInterceptor implements
    SelectionHandler<BeeRowSet> {

  private static final String STYLE_COMMAND_RECALCULATE_PRICES = TradeActKeeper.STYLE_PREFIX
      + "command-recalculate-prices";

  private static double getItemTotal(GridView gridView) {
    double total = BeeConst.DOUBLE_ZERO;
    GridView items = ViewHelper.getSiblingGrid(gridView.asWidget(), GRID_TRADE_ACT_ITEMS);

    if (items != null && !items.isEmpty()) {
      Totalizer totalizer = new Totalizer(items.getDataColumns());

      int qtyIndex = items.getDataIndex(COL_TRADE_ITEM_QUANTITY);
      totalizer.setQuantityFunction(new QuantityReader(qtyIndex));

      for (IsRow row : items.getRowData()) {
        Double amount = totalizer.getTotal(row);
        if (BeeUtils.isDouble(amount)) {
          total += amount;
        }
      }
    }

    return total;
  }

  private TradeActServicePicker picker;
  private Button commandRecalculate;

  TradeActServicesGrid() {
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    GridView gridView = presenter.getGridView();

    if (gridView != null && !gridView.isReadOnly()) {
      commandRecalculate = new Button(Localized.dictionary().taRecalculatePrices());
      commandRecalculate.addStyleName(STYLE_COMMAND_RECALCULATE_PRICES);

      commandRecalculate.addClickHandler(event -> maybeRecalculatePrices());

      presenter.getHeader().addCommandItem(commandRecalculate);
    }
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    if (copy) {
      return true;
    }
    final IsRow parentRow = ViewHelper.getFormRow(presenter.getMainView());

    if (parentRow != null) {
      if (DataUtils.isId(Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CONTINUOUS))) {
        getGridView().notifySevere(Localized.dictionary().actionCanNotBeExecuted());
        return false;
      }
      ensurePicker().show(parentRow, presenter.getMainView().getElement());
    }

    return false;
  }

  @Override
  public void beforeRender(GridView gridView, RenderingEvent event) {
    super.beforeRender(gridView, event);

    IsRow parentRow = ViewHelper.getFormRow(gridView);

    if (commandRecalculate != null) {
      commandRecalculate.setVisible(parentRow != null
          && !DataUtils.isId(Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CONTINUOUS)));
    }
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    FormView parentForm = null;
    IsRow parentRow = null;

    if (gridView != null) {
      parentForm = ViewHelper.getForm(gridView.asWidget());
    }

    if (parentForm != null) {
      parentRow = parentForm.getActiveRow();
    }

    if (parentRow != null
        && BeeUtils.same(parentForm.getFormName(), FORM_TRADE_ACT)) {

      int idxDate = gridView.getDataIndex(COL_TA_SERVICE_FROM);
      int idxParentDate = parentForm.getDataIndex(COL_TA_DATE);

      if (!BeeConst.isUndef(idxDate) && !BeeConst.isUndef(idxParentDate)) {
        newRow.setValue(idxDate, parentRow.getDateTime(idxParentDate).getDate());
      }
    }
    return true;
  }

  @Override
  public void onSelection(SelectionEvent<BeeRowSet> event) {
    addItems(event.getSelectedItem());
  }

  @Override
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {

    if (COL_TA_SERVICE_TARIFF.equalsIgnoreCase(column.getId())
        && BeeUtils.isPositiveDouble(newValue)) {

      GridView gridView = getGridView();
      double total = getItemTotal(gridView);

      if (BeeUtils.isPositive(total)) {
        IsRow row = gridView.getGrid().getRowById(result.getId());

        String oldPrice = (row == null)
            ? null : row.getString(getDataIndex(COL_TRADE_ITEM_PRICE));
        JustDate dateTo = (row == null) ? null : row.getDate(getDataIndex(COL_TA_SERVICE_TO));

        Double price = calculatePrice(BeeUtils.toDoubleOrNull(oldPrice), dateTo, total,
            BeeUtils.toDouble(newValue));

        if (BeeUtils.isPositive(price)) {
          updatePrice(result.getId(), result.getVersion(), oldPrice, price);
        }
      }
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActServicesGrid();
  }

  private void addItems(final BeeRowSet rowSet) {
    if (!DataUtils.isEmpty(rowSet) && VIEW_ITEMS.equals(rowSet.getViewName())) {
      getGridView().ensureRelId(result -> {
        IsRow parentRow = ViewHelper.getFormRow(getGridView());

        if (DataUtils.idEquals(parentRow, result)) {
          ItemPrice itemPrice = TradeActKeeper.getItemPrice(VIEW_TRADE_ACTS, parentRow);

          String ip = rowSet.getTableProperty(PRP_ITEM_PRICE);
          if (BeeUtils.isDigit(ip)) {
            itemPrice = EnumUtils.getEnumByIndex(ItemPrice.class, ip);
          }

          DateTime date = Data.getDateTime(VIEW_TRADE_ACTS, parentRow, COL_TA_DATE);
          Long currency = Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CURRENCY);

          addItems(parentRow, date, currency, itemPrice, rowSet);
        }
      });
    }
  }

  private void addItems(IsRow parentRow, DateTime date, Long currency, ItemPrice defPrice,
      BeeRowSet items) {

    List<String> colNames =
        Lists.newArrayList(COL_TRADE_ACT, COL_TA_ITEM,
            COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_TRADE_VAT,
            COL_TRADE_VAT_PERC, COL_TA_SERVICE_TO, COL_TA_SERVICE_FROM, COL_TA_SERVICE_FROM,
            COL_TA_SERVICE_TARIFF, COL_TRADE_DISCOUNT, COL_TA_SERVICE_DAYS, COL_TA_SERVICE_MIN);
    BeeRowSet rowSet = new BeeRowSet(getViewName(), Data.getColumns(getViewName(), colNames));

    int actIndex = rowSet.getColumnIndex(COL_TRADE_ACT);
    int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    int vatIndex = rowSet.getColumnIndex(COL_TRADE_VAT);
    int vatPercIndex = rowSet.getColumnIndex(COL_TRADE_VAT_PERC);
    int dateFrom = rowSet.getColumnIndex(COL_TA_SERVICE_FROM);
    int dateTo = rowSet.getColumnIndex(COL_TA_SERVICE_TO);
    int tariff = rowSet.getColumnIndex(COL_TA_SERVICE_TARIFF);
    int discount = rowSet.getColumnIndex(COL_TRADE_DISCOUNT);
    int serviceDays = rowSet.getColumnIndex(COL_TA_SERVICE_DAYS);
    int serviceMinTerm = rowSet.getColumnIndex(COL_TA_SERVICE_MIN);

    for (BeeRow item : items) {
      Double qty = BeeUtils.toDoubleOrNull(item.getProperty(PRP_QUANTITY));

      if (BeeUtils.isDouble(qty)) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());

        row.setValue(actIndex, parentRow.getId());
        row.setValue(itemIndex, item.getId());

        row.setValue(qtyIndex, qty);

        row.setValue(vatIndex, item.getValue(Data.getColumnIndex(VIEW_ITEMS, COL_ITEM_VAT_PERC)));
        row.setValue(vatPercIndex, item.getBoolean(Data.getColumnIndex(VIEW_ITEMS, COL_ITEM_VAT)));
        row.setValue(dateFrom, picker.getDatesFrom().get(item.getId()));
        row.setValue(dateTo, picker.getDatesTo().get(item.getId()));
        row.setValue(tariff, picker.getTariffs().get(item.getId()));
        row.setValue(discount, picker.getDiscounts().get(item.getId()));
        row.setValue(serviceDays, item.getString(Data.getColumnIndex(VIEW_ITEMS,
            COL_TA_SERVICE_DAYS)));
        row.setValue(serviceMinTerm, item.getString(Data.getColumnIndex(VIEW_ITEMS,
            COL_TA_SERVICE_MIN)));

        ItemPrice itemPrice = defPrice;

        String ip = item.getProperty(PRP_ITEM_PRICE);
        if (BeeUtils.isDigit(ip)) {
          itemPrice = EnumUtils.getEnumByIndex(ItemPrice.class, ip);
        }

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

        rowSet.addRow(row);
      }
    }

    if (!rowSet.isEmpty()) {
      Queries.insertRows(rowSet);
    }
  }

  private Double calculatePrice(Double defPrice, JustDate dateTo, Double itemTotal, Double tariff) {
    Integer scale = Data.getColumnScale(getViewName(), COL_TRADE_ITEM_PRICE);
    return TradeActUtils.calculateServicePrice(defPrice, dateTo, itemTotal, tariff, scale);
  }

  private TradeActItemPicker ensurePicker() {
    if (picker == null) {
      picker = new TradeActServicePicker();
      picker.addSelectionHandler(this);
    }

    return picker;
  }

  private void maybeRecalculatePrices() {
    GridView gridView = getGridView();
    if (gridView == null || gridView.isEmpty()) {
      return;
    }

    double total = getItemTotal(gridView);

    if (!BeeUtils.isPositive(total)) {
      gridView.notifyWarning(Data.getViewCaption(VIEW_TRADE_ACT_ITEMS),
          Localized.dictionary().noData());
      return;
    }

    int toIndex = getDataIndex(COL_TA_SERVICE_TO);
    int timeUnitIdx = getDataIndex(COL_TRADE_TIME_UNIT);
    int tariffIndex = getDataIndex(COL_TA_SERVICE_TARIFF);
    int priceIndex = getDataIndex(COL_TRADE_ITEM_PRICE);

    int count = 0;

    for (IsRow row : gridView.getRowData()) {
      Double tariff = row.getDouble(tariffIndex);

      if (BeeUtils.isPositive(tariff)) {
        Double price =
            calculatePrice(row.getDouble(priceIndex), row.getDate(toIndex), total, tariff);
        updatePrice(row.getId(), row.getVersion(), row.getString(priceIndex), price);

        count++;
      } else if (row.getInteger(timeUnitIdx) != null) {
        double t = BeeUtils.unbox(row.getDouble(priceIndex)) * 100 / total;
        updateTariff(row.getId(), row.getVersion(), row.getString(tariffIndex), t);
        count++;
      }
    }

    if (count > 0) {
      gridView.notifyInfo(Localized.dictionary().taRecalculatedPrices(count));
    } else {
      gridView.notifyWarning(Data.getColumnLabel(getViewName(), COL_TA_SERVICE_TARIFF),
          Localized.dictionary().noData());
    }
  }

  private boolean updatePrice(long rowId, long version, String oldValue, Double newPrice) {
    String newValue;
    if (BeeUtils.isDouble(newPrice)) {
      Integer scale = Data.getColumnScale(getViewName(), COL_TRADE_ITEM_PRICE);
      newValue = BeeUtils.toString(newPrice, scale);
    } else {
      newValue = null;
    }

    if (Objects.equals(oldValue, newValue)) {
      return false;

    } else {
      Queries.updateCellAndFire(getViewName(), rowId, version, COL_TRADE_ITEM_PRICE,
          oldValue, newValue);

      return true;
    }
  }

  private boolean updateTariff(long rowId, long version, String oldValue, Double newTariff) {
    String newValue;
    if (BeeUtils.isDouble(newTariff)) {
      Integer scale = Data.getColumnScale(getViewName(), COL_TA_SERVICE_TARIFF);
      newValue = BeeUtils.toString(newTariff, scale);
    } else {
      newValue = null;
    }

    if (Objects.equals(oldValue, newValue)) {
      return false;

    } else {
      Queries.updateCellAndFire(getViewName(), rowId, version, COL_TA_SERVICE_TARIFF,
          oldValue, newValue);

      return true;
    }
  }
}
