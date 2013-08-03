package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.ArticleCriteria;
import com.butent.bee.shared.modules.ec.ArticleSupplier;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.modules.ec.EcItemInfo;
import com.butent.bee.shared.utils.BeeUtils;

public class ItemDetails extends Flow {

  public static final String STYLE_PRIMARY = "ItemDetails";

  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_LABEL = "label";
  private static final String STYLE_WRAPPER = "wrapper";
  private static final String STYLE_TABLE = "table";

  private static Widget renderAddToCart(EcItem item) {
    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "addToCart-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    int price = item.getPrice();
    if (price > 0) {
      String priceInfo = BeeUtils.joinWords(Localized.getConstants().price()
          + BeeConst.STRING_COLON, EcUtils.renderCents(price), EcConstants.CURRENCY);
      Label itemPrice = new Label(priceInfo);
      itemPrice.addStyleName(stylePrefix + "price");

      container.add(itemPrice);
    }

    CartAccumulator accumulator = new CartAccumulator(item, 1);
    accumulator.addStyleName(stylePrefix + "cart");

    container.add(accumulator);

    return container;
  }

  private static Widget renderCarTypes(EcItemInfo info) {
    if (info == null || BeeUtils.isEmpty(info.getCarTypes())) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "carTypes-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    Label caption = new Label(Localized.getConstants().ecItemDetailsCarTypes());
    caption.addStyleName(stylePrefix + STYLE_LABEL);
    container.add(caption);

    Flow wrapper = new Flow(stylePrefix + STYLE_WRAPPER);

    String styleCarInfo = stylePrefix + "info";

    for (EcCarType ect : info.getCarTypes()) {
      String carInfo = BeeUtils.joinItems(ect.getManufacturer(), ect.getModelName(),
          ect.getTypeName(), EcUtils.renderProduced(ect.getProducedFrom(), ect.getProducedTo()),
          ect.getPower());

      Label infoLabel = new Label(carInfo);
      infoLabel.addStyleName(styleCarInfo);

      wrapper.add(infoLabel);
    }

    container.add(wrapper);

    return container;
  }

  private static Widget renderInfo(EcItem item, EcItemInfo info) {
    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "info-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    if (item != null) {
      Widget itemCodeWidget = EcUtils.renderField(Localized.getConstants().ecItemCode(),
          item.getCode(), stylePrefix + "itemCode");
      if (itemCodeWidget != null) {
        container.add(itemCodeWidget);
        itemCodeWidget.setTitle(BeeUtils.joinWords("ArticleID:", item.getArticleId()));
      }

      if (item.getBrand() != null) {
        Widget brandWidget = EcUtils.renderField(Localized.getConstants().ecItemBrand(),
            EcKeeper.getBrandName(item.getBrand()), stylePrefix + "brand");
        if (brandWidget != null) {
          container.add(brandWidget);
        }
      }
    }

    if (info != null && !BeeUtils.isEmpty(info.getCriteria())) {
      String styleCriteria = stylePrefix + "criteria-";
      HtmlTable table = new HtmlTable(styleCriteria + STYLE_TABLE);

      int row = 0;
      for (ArticleCriteria ac : info.getCriteria()) {
        Label nameWidget = new Label(ac.getName());
        table.setWidgetAndStyle(row, 0, nameWidget, styleCriteria + "name");

        if (!BeeUtils.isEmpty(ac.getValue())) {
          Label valueWidget = new Label(ac.getValue());
          table.setWidgetAndStyle(row, 1, valueWidget, styleCriteria + "value");
        }

        row++;
      }

      Simple wrapper = new Simple(table);
      wrapper.addStyleName(styleCriteria + STYLE_WRAPPER);

      container.add(wrapper);
    }

    return container;
  }

  private static Widget renderOeNumbers(EcItemInfo info) {
    if (info == null || BeeUtils.isEmpty(info.getOeNumbers())) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "oeNumbers-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    Label caption = new Label(Localized.getConstants().ecItemDetailsOeNumbers());
    caption.addStyleName(stylePrefix + STYLE_LABEL);
    container.add(caption);

    Flow wrapper = new Flow(stylePrefix + STYLE_WRAPPER);

    String styleNumber = stylePrefix + "number";

    for (String oen : info.getOeNumbers()) {
      Label numberLabel = new Label(oen);
      numberLabel.addStyleName(styleNumber);

      wrapper.add(numberLabel);
    }

    container.add(wrapper);

    return container;
  }

  private static Widget renderPicture(EcItem item, int width, int height) {
    ItemPicture widget = new ItemPicture();
    EcStyles.add(widget, STYLE_PRIMARY, "picture");
    StyleUtils.setSize(widget, width, height);

    EcKeeper.setBackgroundPicture(item.getArticleId(), widget);

    return widget;
  }

  private static Widget renderRemainders(EcItem item) {
    if (item == null || BeeUtils.isEmpty(item.getSuppliers())) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "remainders-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    Label caption = new Label(Localized.getConstants().ecItemDetailsRemainders());
    caption.addStyleName(stylePrefix + STYLE_LABEL);
    container.add(caption);

    HtmlTable table = new HtmlTable(stylePrefix + STYLE_TABLE);

    int row = 0;
    for (ArticleSupplier as : item.getSuppliers()) {
      for (String warehouse : as.getRemainders().keySet()) {
        Label warehouseWidget = new Label(warehouse);
        table.setWidgetAndStyle(row, 0, warehouseWidget, stylePrefix + "warehouse");
        double remainder = BeeUtils.toDouble(as.getRemainders().get(warehouse));

        String remainderText = BeeUtils.isPositive(remainder)
            ? BeeUtils.toString(remainder) : Localized.getConstants().ecStockAsk();
        Label stockWidget = new Label(remainderText);
        table.setWidgetAndStyle(row, 1, stockWidget, stylePrefix + "stock");

        row++;
      }
    }

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + STYLE_WRAPPER);

    container.add(wrapper);

    return container;
  }

  private static Widget renderSuppliers(EcItem item) {
    if (item == null || item.getSuppliers().size() <= 1) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "suppliers-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    Label caption = new Label(Localized.getConstants().ecItemDetailsSuppliers());
    caption.addStyleName(stylePrefix + STYLE_LABEL);
    container.add(caption);

    HtmlTable table = new HtmlTable(stylePrefix + STYLE_TABLE);

    int row = 0;
    int col;

    for (ArticleSupplier as : item.getSuppliers()) {
      col = 0;

      table.setText(row, col++, BeeUtils.toString(as.getRealPrice()));

      table.setText(row, col++, as.getSupplier().name());
      table.setText(row, col++, as.getSupplierId());

      row++;
    }

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + STYLE_WRAPPER);

    container.add(wrapper);

    return container;
  }

  public ItemDetails(EcItem item, EcItemInfo info, boolean allowAddToCart) {
    super(EcStyles.name(STYLE_PRIMARY, "panel"));

    int screenWidth = BeeKeeper.getScreen().getWidth();
    int screenHeight = BeeKeeper.getScreen().getHeight();
    if (screenWidth < 100 || screenHeight < 100) {
      return;
    }

    int width = BeeUtils.resize(screenWidth, 100, 1600, 100, 1200);
    int height = BeeUtils.resize(screenHeight, 100, 1600, 100, 1200);
    StyleUtils.setSize(this, width, height);

    int widthMargin = BeeUtils.resize(width, 0, 1000, 0, 20);
    int heightMargin = BeeUtils.resize(height, 0, 1000, 0, 20);

    int rowHeight = (height - heightMargin) / 2;
    int pictureWidth = Math.min(300, width / 3);

    Widget picture = renderPicture(item, pictureWidth, rowHeight);
    if (picture != null) {
      add(picture);
    }

    Widget criteria = renderInfo(item, info);
    if (criteria != null) {
      StyleUtils.makeAbsolute(criteria);
      StyleUtils.setLeft(criteria, pictureWidth + widthMargin);
      StyleUtils.setTop(criteria, 0);
      StyleUtils.setHeight(criteria, rowHeight);

      add(criteria);
    }

    if (allowAddToCart) {
      Widget addToCart = renderAddToCart(item);
      if (addToCart != null) {
        StyleUtils.makeAbsolute(addToCart);
        StyleUtils.setRight(addToCart, 0);
        StyleUtils.setTop(addToCart, 0);

        add(addToCart);
      }
    }

    Widget remainders = renderRemainders(item);
    Widget oeNumbers = renderOeNumbers(info);
    Widget suppliers = renderSuppliers(item);
    Widget carTypes = renderCarTypes(info);

    int remaindersWidth = Math.min(width / 5, 160);
    int oeNumbersWidth = Math.min(width / 6, 140);
    int brandsWidth = Math.max(remaindersWidth + oeNumbersWidth, width / 3);
    int carTypesWidth = width - brandsWidth - widthMargin;

    int top2 = rowHeight + heightMargin;

    int h3 = (suppliers == null) ? 0 : rowHeight / 2;
    int h2 = rowHeight - h3;

    if (remainders != null) {
      StyleUtils.makeAbsolute(remainders);
      StyleUtils.setLeft(remainders, 0);
      StyleUtils.setWidth(remainders, remaindersWidth);
      StyleUtils.setTop(remainders, top2);
      StyleUtils.setHeight(remainders, h2);

      add(remainders);
    }

    if (oeNumbers != null) {
      StyleUtils.makeAbsolute(oeNumbers);
      StyleUtils.setLeft(oeNumbers, remaindersWidth);
      StyleUtils.setWidth(oeNumbers, oeNumbersWidth);
      StyleUtils.setTop(oeNumbers, top2);
      StyleUtils.setHeight(oeNumbers, h2);

      add(oeNumbers);
    }

    if (suppliers != null) {
      StyleUtils.makeAbsolute(suppliers);
      StyleUtils.setLeft(suppliers, 0);
      StyleUtils.setWidth(suppliers, brandsWidth);
      StyleUtils.setTop(suppliers, top2 + h2 + heightMargin);
      StyleUtils.setHeight(suppliers, h3 - heightMargin);

      add(suppliers);
    }

    if (carTypes != null) {
      StyleUtils.makeAbsolute(carTypes);
      StyleUtils.setLeft(carTypes, brandsWidth + widthMargin);
      StyleUtils.setWidth(carTypes, carTypesWidth);
      StyleUtils.setTop(carTypes, top2);
      StyleUtils.setHeight(carTypes, rowHeight);

      add(carTypes);
    }
  }
}
