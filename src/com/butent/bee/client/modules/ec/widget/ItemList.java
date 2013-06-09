package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcItem;
import com.butent.bee.client.modules.ec.EcScreen;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.widget.CustomSpan;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ItemList extends Flow {

  private static final String STYLE_PRIMARY = "ItemList";
  private static final String STYLE_WAREHOUSE = "warehouse";

  private static final String STYLE_HEADER_ROW = EcStyles.name(STYLE_PRIMARY, "headerRow");
  private static final String STYLE_ITEM_ROW = EcStyles.name(STYLE_PRIMARY, "itemRow");

  private static final String STYLE_PICTURE = EcStyles.name(STYLE_PRIMARY, "picture");
  private static final String STYLE_INFO = EcStyles.name(STYLE_PRIMARY, "info");
  private static final String STYLE_STOCK_1 = EcStyles.name(STYLE_PRIMARY, "stock1");
  private static final String STYLE_STOCK_2 = EcStyles.name(STYLE_PRIMARY, "stock2");
  private static final String STYLE_QUANTITY = EcStyles.name(STYLE_PRIMARY, "quantity");
  private static final String STYLE_CART = EcStyles.name(STYLE_PRIMARY, "cart");

  private static final String STYLE_ITEM_NAME = EcStyles.name(STYLE_PRIMARY, "name");
  private static final String STYLE_ITEM_CODE = EcStyles.name(STYLE_PRIMARY, "code");
  private static final String STYLE_ITEM_SUPPLIER = EcStyles.name(STYLE_PRIMARY, "supplier");
  private static final String STYLE_ITEM_ANALOGS = EcStyles.name(STYLE_PRIMARY, "analogs");

  private static final int COL_PICTURE = 0;
  private static final int COL_INFO = 1;
  private static final int COL_STOCK_1 = 2;
  private static final int COL_STOCK_2 = 3;
  private static final int COL_QUANTITY = 4;
  private static final int COL_CART = 5;
  
  private final HtmlTable table;

  public ItemList() {
    super(EcStyles.name(STYLE_PRIMARY));
    
    this.table = new HtmlTable();
    EcStyles.add(table, STYLE_PRIMARY, "table");
    add(table);
  }
  
  public void render(List<EcItem> items) {
    if (BeeUtils.isEmpty(items)) {
      if (!table.isEmpty()) {
        table.clear();
      }

    } else {
      if (!table.isEmpty()) {
        table.clear();
      }

      int row = 0;
      
      if (items.size() > 1) {
        Label caption = new Label(Localized.constants.ecFoundItems());
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
      
      table.getRowFormatter().addStyleName(row, STYLE_HEADER_ROW);
      
      row++;
      for (EcItem item : items) {
        renderItem(row++, item);
      }
    }
  }
  
  private Widget renderCart() {
    Image image = new Image("images/shoppingcart_add.png");
    image.setAlt("cart");
    
    image.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (BeeKeeper.getScreen() instanceof EcScreen) {
          ((EcScreen) BeeKeeper.getScreen()).addToCart(1);
        }
      }
    });

    return image;
  }

  private Widget renderInfo(EcItem item) {
    Flow panel = new Flow();

    String name = item.getName();
    if (!BeeUtils.isEmpty(name)) {
      Label itemName = new Label(name);
      itemName.addStyleName(STYLE_ITEM_NAME);
      panel.add(itemName);
    }
    
    String code = item.getCode();
    if (!BeeUtils.isEmpty(code)) {
      Flow codeContainer = new Flow(STYLE_ITEM_CODE + "Container");

      CustomSpan codeLabel = new CustomSpan(STYLE_ITEM_CODE + "Label");
      codeLabel.setText(Localized.constants.ecItemCode());
      codeContainer.add(codeLabel);

      CustomSpan itemCode = new CustomSpan(STYLE_ITEM_CODE);
      itemCode.setText(code);
      codeContainer.add(itemCode);
      
      panel.add(codeContainer);
    }
    
    String supplier = item.getSupplier();
    if (!BeeUtils.isEmpty(supplier)) {
      Flow supplierContainer = new Flow(STYLE_ITEM_SUPPLIER + "Container");

      CustomSpan supplierLabel = new CustomSpan(STYLE_ITEM_SUPPLIER + "Label");
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

    Widget qty = renderQuantity(item.getQuantity());
    if (qty != null) {
      table.setWidgetAndStyle(row, COL_QUANTITY, qty, STYLE_QUANTITY);
    }
    Widget cart = renderCart();
    if (cart != null) {
      table.setWidgetAndStyle(row, COL_CART, cart, STYLE_CART);
    }
    
    table.getRowFormatter().addStyleName(row, STYLE_ITEM_ROW);
  }
  
  private Widget renderPicture() {
    return EcUtils.randomPicture(30, 100);
  }
  
  private Widget renderQuantity(int quantity) {
    InputSpinner spinner = new InputSpinner();
    spinner.setValue(quantity);
    spinner.setMinValue("1");
    
    return spinner;
  }
  
  private Widget renderStock(int stock) {
    String text = (stock > 0) ? BeeUtils.toString(stock) : Localized.constants.ecStockAsk();
    return new Label(text);
  }
}
