package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

class TradeActItemPicker extends Flow implements HasSelectionHandlers<BeeRowSet> {

  private IsRow lastTaRow;
  private BeeRowSet items;

  private final Flow itemPanel = new Flow();

  TradeActItemPicker() {
    itemPanel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        TableRowElement rowElement =
            DomUtils.getParentRow(EventUtils.getEventTargetElement(event), true);
        if (rowElement != null) {
          selectRow(rowElement.getRowIndex(), 1.0);
        }
      }
    });
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<BeeRowSet> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  void show(IsRow taRow) {
    lastTaRow = DataUtils.cloneRow(taRow);

    getItems(new RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        items = result;
        renderItems();

        Global.showModalWidget(Localized.getConstants().goods(), itemPanel);
      }
    });
  }

  private void getItems(final RowSetCallback callback) {
    ParameterList params = TradeActKeeper.createArgs(SVC_GET_ITEMS_FOR_SELECTION);

    if (DataUtils.hasId(lastTaRow)) {
      params.addDataItem(COL_TRADE_ACT, lastTaRow.getId());
    }

    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, lastTaRow);
    if (kind != null) {
      params.addDataItem(COL_TA_KIND, kind.ordinal());
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          callback.onSuccess(BeeRowSet.restore(response.getResponseAsString()));
        } else {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().nothingFound());
        }
      }
    });
  }

  private void renderItems() {
    itemPanel.clear();

    HtmlTable table = new HtmlTable();

    int nameIndex = items.getColumnIndex(COL_ITEM_NAME);
    int articleIndex = items.getColumnIndex(COL_ITEM_ARTICLE);

    int r = 0;
    int c = 0;

    for (BeeRow row : items) {
      c = 0;

      table.setText(r, c++, BeeUtils.toString(row.getId()));

      table.setText(r, c++, row.getString(nameIndex));
      table.setText(r, c++, row.getString(articleIndex));

      if (!BeeUtils.isEmpty(row.getProperties())) {
        for (Map.Entry<String, String> entry : row.getProperties().entrySet()) {
          table.setText(r, c++, entry.getKey() + "=" + entry.getValue());
        }
      }

      r++;
    }

    itemPanel.add(table);
  }

  private void selectRow(int index, double qty) {
    BeeRow row = DataUtils.cloneRow(items.getRow(index));
    row.setProperty(PRP_QUANTITY, BeeUtils.toString(qty));

    BeeRowSet selection = new BeeRowSet(items.getViewName(), items.getColumns());
    selection.addRow(row);

    items.removeRow(index);
    renderItems();

    SelectionEvent.fire(this, selection);
  }
}
