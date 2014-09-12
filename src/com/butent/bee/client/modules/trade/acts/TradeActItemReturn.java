package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

final class TradeActItemReturn {

  private static final String STYLE_PREFIX = TradeActKeeper.STYLE_PREFIX + "return-items-";

  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_SAVE = STYLE_PREFIX + "save";
  private static final String STYLE_CLOSE = STYLE_PREFIX + "close";

  private static final String STYLE_WRAPPER = STYLE_PREFIX + "wrapper";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private static final String STYLE_HEADER_ROW = STYLE_PREFIX + "header";
  private static final String STYLE_ITEM_ROW = STYLE_PREFIX + "item";
  private static final String STYLE_SELECTED_ROW = STYLE_PREFIX + "selected";

  private static final String STYLE_ID_PREFIX = STYLE_PREFIX + "id-";
  private static final String STYLE_NAME_PREFIX = STYLE_PREFIX + "name-";
  private static final String STYLE_ARTICLE_PREFIX = STYLE_PREFIX + "article-";
  private static final String STYLE_PRICE_PREFIX = STYLE_PREFIX + "price-";

  private static final String STYLE_QTY_PREFIX = STYLE_PREFIX + "qty-";

  private static final String STYLE_INPUT_PREFIX = STYLE_PREFIX + "input-";
  private static final String STYLE_INPUT_WIDGET = STYLE_INPUT_PREFIX + "widget";

  private static final String STYLE_HEADER_CELL_SUFFIX = "label";
  private static final String STYLE_CELL_SUFFIX = "cell";

  static void show(String caption, final BeeRowSet parentItems,
      final Consumer<BeeRowSet> consumer) {

    final HtmlTable table = new HtmlTable(STYLE_TABLE);

    int r = 0;
    int c = 0;

    table.setText(r, c++, Localized.getConstants().captionId(),
        STYLE_ID_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().name(),
        STYLE_NAME_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().article(),
        STYLE_ARTICLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().price(),
        STYLE_PRICE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().quantity(),
        STYLE_QTY_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().taQuantityReturn(),
        STYLE_INPUT_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.getRowFormatter().addStyleName(r, STYLE_HEADER_ROW);

    int itemIndex = parentItems.getColumnIndex(COL_TA_ITEM);
    int nameIndex = parentItems.getColumnIndex(ALS_ITEM_NAME);
    int articleIndex = parentItems.getColumnIndex(COL_ITEM_ARTICLE);
    int priceIndex = parentItems.getColumnIndex(COL_TRADE_ITEM_PRICE);
    int qtyIndex = parentItems.getColumnIndex(COL_TRADE_ITEM_QUANTITY);

    BeeColumn qtyColumn = parentItems.getColumn(qtyIndex);

    r++;
    for (BeeRow p : parentItems) {
      c = 0;

      table.setText(r, c++, p.getString(itemIndex), STYLE_ID_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, p.getString(nameIndex), STYLE_NAME_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, p.getString(articleIndex), STYLE_ARTICLE_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, p.getString(priceIndex), STYLE_PRICE_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, p.getString(qtyIndex), STYLE_QTY_PREFIX + STYLE_CELL_SUFFIX);

      table.setWidget(r, c++, renderInput(qtyColumn), STYLE_INPUT_PREFIX + STYLE_CELL_SUFFIX);

      table.getRowFormatter().addStyleName(r, STYLE_ITEM_ROW);
      DomUtils.setDataIndex(table.getRow(r), p.getId());

      r++;
    }

    final DialogBox dialog = DialogBox.withoutCloseBox(caption, STYLE_DIALOG);

    Image save = new Image(Global.getImages().silverSave());
    save.addStyleName(STYLE_SAVE);

    save.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Map<Long, Double> quantities = getQuantities(table);
        if (!quantities.isEmpty()) {
          selectItems(parentItems, quantities, consumer);
        }

        dialog.close();
      }
    });

    dialog.addAction(Action.SAVE, save);

    Image close = new Image(Global.getImages().silverClose());
    close.addStyleName(STYLE_CLOSE);

    close.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final Map<Long, Double> quantities = getQuantities(table);

        if (quantities.isEmpty()) {
          dialog.close();

        } else {
          Global.decide(Localized.getConstants().goods(),
              Lists.newArrayList(Localized.getConstants().taSaveSelectedItems()),
              new DecisionCallback() {
                @Override
                public void onConfirm() {
                  selectItems(parentItems, quantities, consumer);
                  dialog.close();
                }

                @Override
                public void onDeny() {
                  dialog.close();
                }
              }, DialogConstants.DECISION_YES);
        }
      }
    });

    dialog.addAction(Action.CLOSE, close);

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(STYLE_WRAPPER);

    dialog.setWidget(wrapper);
    dialog.center();
  }

  private static Map<Long, Double> getQuantities(Widget parent) {
    Map<Long, Double> result = new HashMap<>();

    Collection<InputNumber> inputs = UiHelper.getChildren(parent, InputNumber.class);

    for (InputNumber input : inputs) {
      Double qty = input.getNumber();

      if (BeeUtils.isPositive(qty)) {
        long id = DomUtils.getDataIndexLong(DomUtils.getParentRow(input.getElement(), false));
        if (DataUtils.isId(id)) {
          result.put(id, qty);
        }
      }
    }

    return result;
  }

  private static void onQuantityChange(Element source, Double qty) {
    TableRowElement row = DomUtils.getParentRow(source, true);

    if (row != null) {
      if (BeeUtils.isPositive(qty)) {
        row.addClassName(STYLE_SELECTED_ROW);
      } else {
        row.removeClassName(STYLE_SELECTED_ROW);
      }
    }
  }

  private static Widget renderInput(BeeColumn column) {
    InputNumber input = new InputNumber();
    input.addStyleName(STYLE_INPUT_WIDGET);

    input.setMinValue(BeeConst.STRING_ZERO);

    if (column != null) {
      input.setMaxValue(DataUtils.getMaxValue(column));
      input.setScale(column.getScale());
      input.setMaxLength(UiHelper.getMaxLength(column));
    }

    input.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (event.getSource() instanceof InputNumber) {
          InputNumber w = (InputNumber) event.getSource();
          onQuantityChange(w.getElement(), w.getNumber());
        }
      }
    });

    return input;
  }

  private static void selectItems(BeeRowSet input, Map<Long, Double> quantities,
      Consumer<BeeRowSet> consumer) {

    BeeRowSet selection = new BeeRowSet(input.getViewName(), input.getColumns());
    int index = input.getColumnIndex(COL_TRADE_ITEM_QUANTITY);

    for (BeeRow item : input) {
      if (quantities.containsKey(item.getId())) {
        BeeRow row = DataUtils.cloneRow(item);
        row.setValue(index, quantities.get(item.getId()));

        selection.addRow(row);
      }
    }

    if (!selection.isEmpty()) {
      consumer.accept(selection);
    }
  }

  private TradeActItemReturn() {
  }
}
