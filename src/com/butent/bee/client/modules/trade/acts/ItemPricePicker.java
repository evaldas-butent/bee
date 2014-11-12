package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.html.builder.elements.Option;
import com.butent.bee.shared.html.builder.elements.Select;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class ItemPricePicker extends AbstractCellRenderer {

  private static boolean exported;

  private static void ensureExported() {
    if (!exported) {
      export();
      exported = true;
    }
  }

//@formatter:off
  // CHECKSTYLE:OFF
  private static native void export() /*-{
    $wnd.Bee_selectItemPrice = @ItemPricePicker::selectItemPrice(Lcom/google/gwt/core/client/JavaScriptObject;);
  }-*/;
  // CHECKSTYLE:ON
//@formatter:on

  private static void selectItemPrice(JavaScriptObject jso) {
    LogUtils.getRootLogger().debug("select", jso);
  }

  private final EnumMap<ItemPrice, Integer> priceIndexes = new EnumMap<>(ItemPrice.class);
  private final EnumMap<ItemPrice, Integer> currencyIndexes = new EnumMap<>(ItemPrice.class);

  ItemPricePicker(CellSource cellSource, List<? extends IsColumn> columns) {
    super(cellSource);

    int index;
    for (ItemPrice ip : ItemPrice.values()) {
      index = DataUtils.getColumnIndex(ip.getPriceAlias(), columns);
      if (!BeeConst.isUndef(index)) {
        priceIndexes.put(ip, index);
      }

      index = DataUtils.getColumnIndex(ip.getCurrencyAlias(), columns);
      if (!BeeConst.isUndef(index)) {
        currencyIndexes.put(ip, index);
      }
    }

    ensureExported();
  }

  @Override
  public String render(IsRow row) {
    EnumMap<ItemPrice, Double> prices = new EnumMap<>(ItemPrice.class);
    for (Map.Entry<ItemPrice, Integer> entry : priceIndexes.entrySet()) {
      Double p = row.getDouble(entry.getValue());
      if (BeeUtils.isPositive(p)) {
        prices.put(entry.getKey(), p);
      }
    }

    if (prices.isEmpty()) {
      return null;
    }

    NumberFormat format = TradeActHelper.getPriceFormat();

    Double price = getDouble(row);

    Select select = new Select();
    select.appendChild(new Option().value(BeeConst.UNDEF));

    for (Map.Entry<ItemPrice, Double> entry : prices.entrySet()) {
      ItemPrice ip = entry.getKey();
      Double x = entry.getValue();

      String text = BeeUtils.joinWords(ip.getLabel(), format.format(x));

      Option option = new Option().value(ip.ordinal()).text(text);
      if (price != null && x.equals(price)) {
        option.selected();
        price = null;
      }

      select.appendChild(option);
    }

    select.setOnChange("$wnd.Bee_selectItemPrice(this)");
    select.setOnClick("Bee_selectItemPrice(this)");

    return select.build();
  }
}
