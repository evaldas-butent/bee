package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class TradeActItemPicker extends Flow implements HasSelectionHandlers<BeeRowSet> {

  private static final BeeLogger logger = LogUtils.getLogger(TradeActItemPicker.class);

  private static final String STYLE_PREFIX = TradeActKeeper.STYLE_PREFIX + "picker-";

  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_SAVE = STYLE_PREFIX + "save";
  private static final String STYLE_CLOSE = STYLE_PREFIX + "close";

  private static final String STYLE_HEADER_ROW = STYLE_PREFIX + "header";
  private static final String STYLE_ITEM_ROW = STYLE_PREFIX + "item";

  private static final String STYLE_ID_PREFIX = STYLE_PREFIX + "id-";
  private static final String STYLE_TYPE_PREFIX = STYLE_PREFIX + "type-";
  private static final String STYLE_GROUP_PREFIX = STYLE_PREFIX + "group-";
  private static final String STYLE_NAME_PREFIX = STYLE_PREFIX + "name-";
  private static final String STYLE_ARTICLE_PREFIX = STYLE_PREFIX + "article-";
  private static final String STYLE_PRICE_PREFIX = STYLE_PREFIX + "price-";
  private static final String STYLE_STOCK_PREFIX = STYLE_PREFIX + "stock-";
  private static final String STYLE_QTY_PREFIX = STYLE_PREFIX + "qty-";

  private static final String STYLE_HEADER_CELL_SUFFIX = "label";
  private static final String STYLE_CELL_SUFFIX = "cell";

  private static final String STYLE_QTY_INPUT = STYLE_QTY_PREFIX + "input";

  private static final List<String> SEARCH_COLUMNS = Lists.newArrayList(COL_ITEM_NAME,
      COL_ITEM_ARTICLE, COL_ITEM_TYPE, COL_ITEM_GROUP);

  private IsRow lastTaRow;
  private BeeRowSet items;

  private final List<Long> warehouses = new ArrayList<>();
  private final Map<Long, String> warehouseLabels = new HashMap<>();

  private final Flow itemPanel = new Flow(STYLE_PREFIX + "item-panel");

  TradeActItemPicker() {
    super(STYLE_PREFIX + "container");

    add(createSearch());
    add(itemPanel);

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
        prepareWarehouses(extractWarehouses(result));

        renderItems();
        openDialog();
      }
    });
  }

  private void openDialog() {
    final DialogBox dialog = DialogBox.withoutCloseBox(Localized.getConstants().goods(),
        STYLE_DIALOG);

    Image save = new Image(Global.getImages().silverSave());
    save.addStyleName(STYLE_SAVE);

    save.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      }
    });

    dialog.addAction(Action.SAVE, save);

    Image close = new Image(Global.getImages().silverClose());
    close.addStyleName(STYLE_CLOSE);

    close.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.close();
      }
    });

    dialog.addAction(Action.CLOSE, close);

    dialog.setWidget(this);
    dialog.center();
  }

  private static Collection<Long> extractWarehouses(BeeRowSet rowSet) {
    if (DataUtils.isEmpty(rowSet)) {
      return Collections.emptySet();
    } else {
      return DataUtils.parseIdSet(rowSet.getTableProperty(TBL_WAREHOUSES));
    }
  }

  private void prepareWarehouses(Collection<Long> ids) {
    warehouses.clear();
    warehouseLabels.clear();

    if (!BeeUtils.isEmpty(ids)) {
      Map<Long, String> codes = TradeActKeeper.getWarehouseCodes(ids);
      if (codes.size() < ids.size()) {
        for (Long id : ids) {
          if (DataUtils.isId(id) && !codes.containsKey(id)) {
            codes.put(id, BeeUtils.toString(id));
          }
        }
      }

      if (codes.size() == 1) {
        warehouses.addAll(codes.keySet());
        warehouseLabels.putAll(codes);

      } else if (codes.size() > 1) {
        TreeMap<String, Long> sorted = new TreeMap<>();
        for (Map.Entry<Long, String> entry : codes.entrySet()) {
          sorted.put(entry.getValue(), entry.getKey());
        }

        for (Map.Entry<String, Long> entry : sorted.entrySet()) {
          warehouses.add(entry.getValue());
          warehouseLabels.put(entry.getValue(), entry.getKey());
        }
      }
    }
  }

  private static Widget createSearch() {
    Flow panel = new Flow(STYLE_PREFIX + "search-panel");

    final ListBox searchBy = new ListBox();
    searchBy.addStyleName(STYLE_PREFIX + "search-by");

    searchBy.addItem(BeeConst.STRING_EMPTY, BeeConst.STRING_ASTERISK);
    for (String column : SEARCH_COLUMNS) {
      searchBy.addItem(Data.getColumnLabel(VIEW_ITEMS, column), column);
    }
    searchBy.addItem(Localized.getConstants().captionId(), COL_ITEM);

    panel.add(searchBy);

    final InputText searchBox = new InputText();
    DomUtils.setSearch(searchBox);
    searchBox.addStyleName(STYLE_PREFIX + "search-box");

    searchBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          String query = BeeUtils.trim(searchBox.getValue());
          if (!BeeUtils.isEmpty(query)) {
            doSearch(searchBy.getValue(), query);
          }
        }
      }
    });

    panel.add(searchBox);

    FaLabel searchCommand = new FaLabel(FontAwesome.SEARCH, STYLE_PREFIX + "search-command");

    searchCommand.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch(searchBy.getValue(), searchBox.getValue());
      }
    });

    panel.add(searchCommand);

    return panel;
  }

  private static void doSearch(String by, String query) {
    logger.debug(by, query);
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

    Long warehouse = TradeActKeeper.getWarehouseFrom(VIEW_TRADE_ACTS, lastTaRow);
    if (DataUtils.isId(warehouse)) {
      params.addDataItem(COL_WAREHOUSE, warehouse);
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

    HtmlTable table = new HtmlTable(STYLE_PREFIX + "item-table");

    int r = 0;
    int c = 0;

    table.setText(r, c++, Localized.getConstants().captionId(),
        STYLE_ID_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().type(),
        STYLE_TYPE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().group(),
        STYLE_GROUP_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().name(),
        STYLE_NAME_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().article(),
        STYLE_ARTICLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    for (ItemPrice ip : ItemPrice.values()) {
      table.setText(r, c++, ip.getCaption(), STYLE_PRICE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    }

    for (Long warehouse : warehouses) {
      table.setText(r, c++, warehouseLabels.get(warehouse),
          STYLE_STOCK_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    }

    table.setText(r, c++, Localized.getConstants().quantity(),
        STYLE_QTY_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.getRowFormatter().addStyleName(r, STYLE_HEADER_ROW);

    List<Integer> typeIndexes = Lists.newArrayList(items.getColumnIndex(ALS_PARENT_TYPE_NAME),
        items.getColumnIndex(ALS_ITEM_TYPE_NAME));
    List<Integer> groupIndexes = Lists.newArrayList(items.getColumnIndex(ALS_PARENT_GROUP_NAME),
        items.getColumnIndex(ALS_ITEM_GROUP_NAME));

    int nameIndex = items.getColumnIndex(COL_ITEM_NAME);
    int articleIndex = items.getColumnIndex(COL_ITEM_ARTICLE);

    EnumMap<ItemPrice, Integer> priceIndexes = new EnumMap<>(ItemPrice.class);
    EnumMap<ItemPrice, Integer> currencyIndexes = new EnumMap<>(ItemPrice.class);

    for (ItemPrice ip : ItemPrice.values()) {
      priceIndexes.put(ip, items.getColumnIndex(ip.getPriceColumn()));
      currencyIndexes.put(ip, items.getColumnIndex(ip.getCurrencyNameAlias()));
    }

    r++;
    for (BeeRow item : items) {
      c = 0;

      table.setText(r, c++, BeeUtils.toString(item.getId()),
          STYLE_ID_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, DataUtils.join(items.getColumns(), item, typeIndexes,
          BeeConst.STRING_EOL), STYLE_TYPE_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, DataUtils.join(items.getColumns(), item, groupIndexes,
          BeeConst.STRING_EOL), STYLE_GROUP_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, item.getString(nameIndex),
          STYLE_NAME_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, item.getString(articleIndex),
          STYLE_ARTICLE_PREFIX + STYLE_CELL_SUFFIX);

      for (ItemPrice ip : ItemPrice.values()) {
        String html = renderPrice(item, priceIndexes.get(ip), currencyIndexes.get(ip));
        if (html != null) {
          table.setHtml(r, c, html, STYLE_PRICE_PREFIX + STYLE_CELL_SUFFIX);
        }
        c++;
      }

      if (!warehouses.isEmpty() && !BeeUtils.isEmpty(item.getProperties())) {
        for (Map.Entry<String, String> entry : item.getProperties().entrySet()) {
          if (BeeUtils.isPrefix(entry.getKey(), PRP_WAREHOUSE_PREFIX)) {
            Long warehouse = BeeUtils.toLongOrNull(BeeUtils.removePrefix(entry.getKey(),
                PRP_WAREHOUSE_PREFIX));

            if (DataUtils.isId(warehouse) && warehouses.contains(warehouse)) {
              table.setHtml(r, c + warehouses.indexOf(warehouse),
                  renderStock(warehouse, entry.getValue()), STYLE_STOCK_PREFIX + STYLE_CELL_SUFFIX);
            }
          }
        }
      }

      c += warehouses.size();

      table.setWidget(r, c, renderQty(), STYLE_STOCK_PREFIX + STYLE_CELL_SUFFIX);

      table.getRowFormatter().addStyleName(r, STYLE_ITEM_ROW);
      r++;
    }

    itemPanel.add(table);
  }

  private static String renderPrice(BeeRow item, int priceIndex, int currencyIndex) {
    Double price = item.getDouble(priceIndex);
    if (BeeUtils.isDouble(price)) {
      return BeeUtils.joinWords(BeeUtils.toString(price), item.getString(currencyIndex));
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  @SuppressWarnings("unused")
  private static String renderStock(long warehouse, String value) {
    return value;
  }

  private static Widget renderQty() {
    InputNumber input = new InputNumber();

    input.setMinValue(BeeConst.STRING_ZERO);
    input.setScale(Data.getColumnPrecision(VIEW_TRADE_ACT_ITEMS,
        TradeConstants.COL_TRADE_ITEM_QUANTITY));

    input.addStyleName(STYLE_QTY_INPUT);

    return input;
  }

  private void selectRow(int index, double qty) {
    BeeRow row = DataUtils.cloneRow(items.getRow(index - 1));
    row.setProperty(PRP_QUANTITY, BeeUtils.toString(qty));

    BeeRowSet selection = new BeeRowSet(items.getViewName(), items.getColumns());
    selection.addRow(row);

    items.removeRow(index);
    renderItems();

    SelectionEvent.fire(this, selection);
  }
}
