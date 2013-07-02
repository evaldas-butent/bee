package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.widget.CustomSpan;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ItemList extends Simple {

  private static final String STYLE_PRIMARY = "ItemList";
  private static final String STYLE_WAREHOUSE = "warehouse";

  private static final String STYLE_HEADER_ROW = EcStyles.name(STYLE_PRIMARY, "headerRow");
  private static final String STYLE_ITEM_ROW = EcStyles.name(STYLE_PRIMARY, "itemRow");

  private static final String STYLE_PICTURE = EcStyles.name(STYLE_PRIMARY, "picture");
  private static final String STYLE_INFO = EcStyles.name(STYLE_PRIMARY, "info");
  private static final String STYLE_STOCK_1 = EcStyles.name(STYLE_PRIMARY, "stock1");
  private static final String STYLE_STOCK_2 = EcStyles.name(STYLE_PRIMARY, "stock2");
  private static final String STYLE_QUANTITY = EcStyles.name(STYLE_PRIMARY, "quantity");
  private static final String STYLE_LIST_PRICE = EcStyles.name(STYLE_PRIMARY, "listPrice");
  private static final String STYLE_PRICE = EcStyles.name(STYLE_PRIMARY, "price");
  private static final String STYLE_CART = EcStyles.name(STYLE_PRIMARY, "cart");

  private static final String STYLE_ITEM_NAME = EcStyles.name(STYLE_PRIMARY, "name");
  private static final String STYLE_ITEM_CODE = EcStyles.name(STYLE_PRIMARY, "code");
  private static final String STYLE_ITEM_SUPPLIER = EcStyles.name(STYLE_PRIMARY, "supplier");
  private static final String STYLE_ITEM_ANALOGS = EcStyles.name(STYLE_PRIMARY, "analogs");

  private static final String STYLE_ITEM_MANUFACTURER =
      EcStyles.name(STYLE_PRIMARY, "manufacturer");

  private static final String STYLE_INFO_CONTAINER = "-container";
  private static final String STYLE_LABEL = "-label";

  private static final int COL_PICTURE = 0;
  private static final int COL_INFO = 1;
  private static final int COL_STOCK_1 = 2;
  private static final int COL_STOCK_2 = 3;
  private static final int COL_LIST_PRICE = 4;
  private static final int COL_PRICE = 5;
  private static final int COL_QUANTITY = 6;

  private final HtmlTable table;

  public ItemList() {
    super();
    addStyleName(EcStyles.name(STYLE_PRIMARY));

    this.table = new HtmlTable();
    EcStyles.add(table, STYLE_PRIMARY, "table");
    setWidget(table);
  }

  public ItemList(List<EcItem> items) {
    this();
    render(items);
  }

  public void render(List<EcItem> items) {
    if (!table.isEmpty()) {
      table.clear();
    }

    if (!BeeUtils.isEmpty(items)) {
      int row = 0;

      if (items.size() > 1) {
        Label caption = new Label(BeeUtils.joinWords(Localized.constants.ecFoundItems(),
            BeeUtils.bracket(items.size())));
        EcStyles.add(caption, STYLE_PRIMARY, "caption");
        table.setWidget(row, COL_PICTURE, caption);
      }

      Label wrh1 = new Label("S1");
      EcStyles.add(wrh1, STYLE_PRIMARY, STYLE_WAREHOUSE);
      EcStyles.add(wrh1, STYLE_PRIMARY, STYLE_WAREHOUSE + "1");
      table.setWidget(row, COL_STOCK_1, wrh1);

      Label wrh2 = new Label("S2");
      EcStyles.add(wrh2, STYLE_PRIMARY, STYLE_WAREHOUSE);
      EcStyles.add(wrh2, STYLE_PRIMARY, STYLE_WAREHOUSE + "2");
      table.setWidget(row, COL_STOCK_2, wrh2);
      
      Label listPriceLabel = new Label(Localized.constants.ecListPrice());
      EcStyles.add(listPriceLabel, STYLE_PRIMARY, STYLE_LIST_PRICE + STYLE_LABEL);
      table.setWidget(row, COL_LIST_PRICE, listPriceLabel);
      
      Label priceLabel = new Label(Localized.constants.ecClientPrice());
      EcStyles.add(priceLabel, STYLE_PRIMARY, STYLE_PRICE + STYLE_LABEL);
      table.setWidget(row, COL_PRICE, priceLabel);

      table.getRowFormatter().addStyleName(row, STYLE_HEADER_ROW);

      row++;
      for (EcItem item : items) {
        if (row > 500) {
          break;
        }
        renderItem(row++, item);
      }
    }
  }

  private void fillTree(Multimap<String, Pair<String, String>> data, String parent,
      TreeItem tree) {

    if (data.containsKey(parent)) {
      for (Pair<String, String> info : data.get(parent)) {
        TreeItem item = new TreeItem(info.getB());
        tree.addItem(item);
        fillTree(data, info.getA(), item);
      }
    }
  }

  private Widget renderInfo(final EcItem item) {
    Flow panel = new Flow();

    String name = item.getName();
    if (!BeeUtils.isEmpty(name)) {
      Label itemName = new Label(name);
      itemName.addStyleName(STYLE_ITEM_NAME);
      
      String categoryNames = BeeUtils.join(BeeConst.STRING_EOL, EcKeeper.getCategoryNames(item));
      if (!BeeUtils.isEmpty(categoryNames)) {
        itemName.setTitle(categoryNames);
      }
      
      panel.add(itemName);
    }

    String code = item.getCode();
    if (!BeeUtils.isEmpty(code)) {
      Flow codeContainer = new Flow(STYLE_ITEM_CODE + STYLE_INFO_CONTAINER);

      CustomSpan codeLabel = new CustomSpan(STYLE_ITEM_CODE + STYLE_LABEL);
      codeLabel.setText(Localized.constants.ecItemCode());
      codeContainer.add(codeLabel);

      CustomSpan itemCode = new CustomSpan(STYLE_ITEM_CODE);
      itemCode.setText(code);
      codeContainer.add(itemCode);

      panel.add(codeContainer);
    }

    String supplier = item.getSupplier();
    if (!BeeUtils.isEmpty(supplier)) {
      Flow supplierContainer = new Flow(STYLE_ITEM_SUPPLIER + STYLE_INFO_CONTAINER);

      CustomSpan supplierLabel = new CustomSpan(STYLE_ITEM_SUPPLIER + STYLE_LABEL);
      supplierLabel.setText(Localized.constants.ecItemSupplier());
      supplierContainer.add(supplierLabel);

      CustomSpan itemSupplier = new CustomSpan(STYLE_ITEM_SUPPLIER);
      itemSupplier.setText(supplier);
      supplierContainer.add(itemSupplier);

      panel.add(supplierContainer);
    }

    if (item.hasAnalogs()) {
      InternalLink analogs = new InternalLink(Localized.constants.ecItemAnalogs());
      analogs.addStyleName(STYLE_ITEM_ANALOGS);
      panel.add(analogs);

      analogs.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          ParameterList params = EcKeeper.createArgs("ITEM_INFO");
          params.addDataItem("ID", item.getId());

          BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              response.notify(BeeKeeper.getScreen());

              if (!response.hasErrors()) {
                Multimap<String, Pair<String, String>> subTree = HashMultimap.create();

                for (SimpleRow row : SimpleRowSet.restore((String) response.getResponse())) {
                  subTree.put(row.getValue("ParentID"),
                      Pair.of(row.getValue("CategoryID"), row.getValue("CategoryName")));
                }
                Tree tree = new Tree();

                TreeItem root = new TreeItem("Kategorijos");
                tree.addItem(root);
                fillTree(subTree, null, root);
                root.setOpenRecursive(true, false);

                Global.getMsgBoxen().showWidget(tree);
              }
            }
          });
        }
      });
    }

    String manufacturer = item.getManufacturer();
    if (!BeeUtils.isEmpty(manufacturer)) {
      Flow manufacturerContainer = new Flow(STYLE_ITEM_MANUFACTURER + STYLE_INFO_CONTAINER);

      CustomSpan manufacturerLabel = new CustomSpan(STYLE_ITEM_MANUFACTURER + STYLE_LABEL);
      manufacturerLabel.setText(Localized.constants.ecItemManufacturer());
      manufacturerContainer.add(manufacturerLabel);

      CustomSpan itemManufacturer = new CustomSpan(STYLE_ITEM_MANUFACTURER);
      itemManufacturer.setText(manufacturer);
      manufacturerContainer.add(itemManufacturer);

      panel.add(manufacturerContainer);
    }

    return panel;
  }

  private void renderItem(int row, EcItem item) {
    Widget picture = renderPicture();
    if (picture != null) {
      table.setWidgetAndStyle(row, COL_PICTURE, picture, STYLE_PICTURE);
    }
    Widget info = renderInfo(item);
    if (info != null) {
      table.setWidgetAndStyle(row, COL_INFO, info, STYLE_INFO);
    }

    Widget stock1 = renderStock(item.getStock1());
    if (stock1 != null) {
      table.setWidgetAndStyle(row, COL_STOCK_1, stock1, STYLE_STOCK_1);
    }
    Widget stock2 = renderStock(item.getStock2());
    if (stock2 != null) {
      table.setWidgetAndStyle(row, COL_STOCK_2, stock2, STYLE_STOCK_2);
    }
    
    int listPrice = item.getListPrice();
    int price = item.getPrice();
    
    if (listPrice > price) {
      Widget listPriceWidget = renderPrice(listPrice, STYLE_LIST_PRICE);
      table.setWidgetAndStyle(row, COL_LIST_PRICE, listPriceWidget, STYLE_LIST_PRICE);
    }
    if (price > 0) {
      Widget priceWidget = renderPrice(price, STYLE_PRICE);
      table.setWidgetAndStyle(row, COL_PRICE, priceWidget, STYLE_PRICE);
    }

    Widget qty = renderQuantity(item, 1);
    if (qty != null) {
      table.setWidgetAndStyle(row, COL_QUANTITY, qty, STYLE_QUANTITY);
    }

    table.getRowFormatter().addStyleName(row, STYLE_ITEM_ROW);
  }

  private Widget renderPicture() {
    return EcUtils.randomPicture(30, 100);
  }

  private Widget renderPrice(int price, String style) {
    String stylePrefix = style + "-";

    Flow panel = new Flow();
    
    InlineLabel value = new InlineLabel(EcUtils.renderCents(price));
    value.addStyleName(stylePrefix + "value");
    panel.add(value);

    InlineLabel currency = new InlineLabel(EcConstants.CURRENCY);
    currency.addStyleName(stylePrefix + "currency");
    panel.add(currency);

    return panel;
  }
  
  private Widget renderQuantity(final EcItem item, int quantity) {
    String stylePrefix = STYLE_QUANTITY + "-";

    Horizontal panel = new Horizontal();
    final InputInteger input = new InputInteger(quantity);
    input.addStyleName(stylePrefix + "input");
    panel.add(input);

    Flow spin = new Flow(stylePrefix + "spin");

    Image plus = new Image(Global.getImages().silverPlus());
    plus.addStyleName(stylePrefix + "plus");

    plus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = Math.max(input.getIntValue() + 1, 1);
        input.setValue(value);
      }
    });
    spin.add(plus);

    Image minus = new Image(Global.getImages().silverMinus());
    minus.addStyleName(stylePrefix + "minus");

    minus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = Math.max(input.getIntValue() - 1, 0);
        input.setValue(value);
      }
    });
    spin.add(minus);

    panel.add(spin);

    Image cart = new Image("images/shoppingcart_add.png");
    cart.setAlt("cart");
    cart.addStyleName(STYLE_CART);

    cart.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = input.getIntValue();
        if (value > 0) {
          EcKeeper.addToCart(item, value);
          input.setValue(0);
        }
      }
    });

    panel.add(cart);

    return panel;
  }

  private Widget renderStock(int stock) {
    String text = (stock > 0) ? BeeUtils.toString(stock) : Localized.constants.ecStockAsk();
    return new Label(text);
  }
}
