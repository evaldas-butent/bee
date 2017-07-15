package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.classifiers.ItemsPicker;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

class TradeActItemPicker extends ItemsPicker {
  private static final String STYLE_STOCK_PREFIX = STYLE_PREFIX + "stock-";
  private static final String STYLE_STOCK_POSITIVE = STYLE_STOCK_PREFIX + "positive";

  @Override
  public void getItems(Filter filter, final RowSetCallback callback) {
    ParameterList params = TradeActKeeper.createArgs(SVC_GET_ITEMS_FOR_SELECTION);

    if (DataUtils.hasId(getLastRow())) {
      params.addDataItem(COL_TRADE_ACT, getLastRow().getId());
    }

    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, getLastRow());
    if (kind != null) {
      params.addDataItem(COL_TA_KIND, kind.ordinal());
    }

    if (DataUtils.isId(getWarehouseFrom())) {
      params.addDataItem(ClassifierConstants.COL_WAREHOUSE, getWarehouseFrom());
    }

    Filter defFilter = getDefaultItemFilter();

    if (filter != null) {
      if (defFilter != null) {
        defFilter = Filter.and(defFilter, filter);
      } else {
        defFilter = filter;
      }
    }

    if (defFilter != null) {
      params.addDataItem(Service.VAR_VIEW_WHERE, defFilter.serialize());
    }

    params.addDataItem(Service.VAR_TABLE, getSource());

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          callback.onSuccess(BeeRowSet.restore(response.getResponseAsString()));
        } else {
          getSpinner().setStyleName(STYLE_SEARCH_SPINNER_LOADING, false);
          BeeKeeper.getScreen().notifyWarning(Localized.dictionary().nothingFound());
        }
      }
    });
  }

  @Override
  public Long getWarehouseFrom(IsRow row) {
    return TradeActKeeper.getWarehouseFrom(VIEW_TRADE_ACTS, row);
  }

  @Override
  public boolean setIsOrder(IsRow row) {
    return false;
  }

  @Override
  protected String getCaption() {
    return Localized.dictionary().goods();
  }

  @Override
  protected void renderItems(Map<Long, Double> quantities, Map<Long, String> warehouses,
      Flow panel,
      BeeRowSet itemList) {
    super.renderItems(quantities, warehouses, panel, itemList);

    HtmlTable table = null;

    for (int i = 0; i < panel.getWidgetCount(); i++) {
      Widget w = panel.getWidget(i);
      if (w instanceof HtmlTable && StyleUtils.hasClassName(w.getElement(), STYLE_ITEM_TABLE)) {
        table = (HtmlTable) w;
        break;
      }
    }

    if (table == null) {
      return;
    }

    // HtmlTable newTable = new HtmlTable();

    int r = 0;
    int c = table.getCellCount(r);

    if (table.getWidget(r, c - 1) != null) {
      table.setWidget(r, c, table.getWidget(r, c - 1));
    } else {
      table.setHtml(r, c, table.getCellFormatter().getElement(r, c - 1).getInnerHTML());
    }

    getVisibleTableCols().add(c - 1, ClassifierConstants.COL_EXTERNAL_STOCK);

    table.setText(r, c - 1, Localized.maybeTranslate(itemList.getColumnLabel(
        ClassifierConstants.COL_EXTERNAL_STOCK)));

    r++;
    for (int i = r; i < table.getRowCount(); i++) {
      c = table.getCellCount(i);

      Long itemId = BeeUtils.toLongOrNull(table.getCellFormatter().getElement(i,
          getVisibleTableCols().indexOf(DataUtils.ID_TAG)).getInnerText());

      if (!DataUtils.isId(itemId)) {
        continue;
      }

      if (!itemList.getRowIds().contains(itemId)) {
        continue;
      }

      table.setWidget(i, c, table.getWidget(i, c - 1));

      table.setText(i, c - 1, itemList.getString(itemList.getRowIndex(itemId),
          ClassifierConstants.COL_EXTERNAL_STOCK), STYLE_STOCK_POSITIVE);
    }
  }

  protected Filter getDefaultItemFilter() {
    return Filter.isNull(ClassifierConstants.COL_ITEM_IS_SERVICE);
  }

  protected String getSource() {
    return TBL_TRADE_ACT_ITEMS;
  }
}
