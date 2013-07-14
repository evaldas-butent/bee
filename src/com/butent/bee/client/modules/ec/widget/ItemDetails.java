package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.ArticleBrand;
import com.butent.bee.shared.modules.ec.ArticleCriteria;
import com.butent.bee.shared.modules.ec.ArticleRemainder;
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

  private static Widget renderBrands(EcItemInfo info) {
    if (info == null || info.getBrands().size() <= 1) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "brands-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    Label caption = new Label(Localized.getConstants().ecItemDetailsBrands());
    caption.addStyleName(stylePrefix + STYLE_LABEL);
    container.add(caption);

    HtmlTable table = new HtmlTable(stylePrefix + STYLE_TABLE);

    int row = 0;
    int col;

    for (ArticleBrand ab : info.getBrands()) {
      col = 0;

      table.setText(row, col++, ab.getBrand());
      table.setText(row, col++, ab.getAnalogNr());

      row++;
    }

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + STYLE_WRAPPER);

    container.add(wrapper);

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
      }

      Widget supplierWidget = EcUtils.renderField(Localized.getConstants().ecItemSupplier(),
          item.getSupplier(), stylePrefix + "supplier");
      if (supplierWidget != null) {
        container.add(supplierWidget);
      }

      Widget supplierCodeWidget =
          EcUtils.renderField(Localized.getConstants().ecItemSupplierCode(),
              item.getSupplierCode(), stylePrefix + "supplierCode");
      if (supplierCodeWidget != null) {
        container.add(supplierCodeWidget);
      }

      Widget manufacturerWidget =
          EcUtils.renderField(Localized.getConstants().ecItemManufacturer(),
              item.getManufacturer(), stylePrefix + "manufacturer");
      if (manufacturerWidget != null) {
        container.add(manufacturerWidget);
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

  private static Widget renderPicture(int width, int height) {
    int max = Math.min(width, height);
    int min = max / 2;

    Widget picture = EcUtils.randomPicture(min, max);
    EcStyles.add(picture, STYLE_PRIMARY, "picture");
    return picture;
  }

  private static Widget renderRemainders(EcItemInfo info) {
    if (info == null || BeeUtils.isEmpty(info.getRemainders())) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "remainders-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    Label caption = new Label(Localized.getConstants().ecItemDetailsRemainders());
    caption.addStyleName(stylePrefix + STYLE_LABEL);
    container.add(caption);

    HtmlTable table = new HtmlTable(stylePrefix + STYLE_TABLE);

    int row = 0;
    for (ArticleRemainder ar : info.getRemainders()) {
      Label warehouseWidget = new Label(ar.getWarehouse());
      table.setWidgetAndStyle(row, 0, warehouseWidget, stylePrefix + "warehouse");

      String remainderText = BeeUtils.isPositive(ar.getRemainder())
          ? BeeUtils.toString(ar.getRemainder()) : Localized.getConstants().ecStockAsk();
      Label stockWidget = new Label(remainderText);
      table.setWidgetAndStyle(row, 1, stockWidget, stylePrefix + "stock");

      row++;
    }

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + STYLE_WRAPPER);

    container.add(wrapper);

    return container;
  }

  public ItemDetails(EcItem item, EcItemInfo info, boolean allowAddToCart) {
    super(EcStyles.name(STYLE_PRIMARY, "panel"));

    int width = BeeKeeper.getScreen().getWidth() * 3 / 4;
    int height = BeeKeeper.getScreen().getHeight() * 3 / 4;
    if (width < 50 || height < 50) {
      return;
    }
    StyleUtils.setSize(this, width, height);

    int widthMargin = 10;
    int heightMargin = 10;

    int rowHeight = (height - heightMargin) / 2;

    int pictureWidth = Math.min(200, width / 3);

    Widget picture = renderPicture(pictureWidth, rowHeight);
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

    Widget remainders = renderRemainders(info);
    Widget brands = renderBrands(info);
    Widget carTypes = renderCarTypes(info);

    int remaindersWidth = 0;
    int brandsWidth = 0;

    if (remainders != null) {
      StyleUtils.makeAbsolute(remainders);
      StyleUtils.setLeft(remainders, 0);
      StyleUtils.setTop(remainders, rowHeight + heightMargin);
      StyleUtils.setHeight(remainders, rowHeight);

      if (brands != null || carTypes != null) {
        if (brands == null || carTypes == null) {
          remaindersWidth = width / 4;
        } else {
          remaindersWidth = width / 5;
        }

        StyleUtils.setWidth(remainders, remaindersWidth);
      }

      add(remainders);
    }

    if (brands != null) {
      StyleUtils.makeAbsolute(brands);
      StyleUtils.setLeft(brands, remaindersWidth);
      StyleUtils.setTop(brands, rowHeight + heightMargin);
      StyleUtils.setHeight(brands, rowHeight);

      if (carTypes != null) {
        brandsWidth = width / 4;
        StyleUtils.setWidth(brands, brandsWidth);
      }

      add(brands);
    }

    if (carTypes != null) {
      StyleUtils.makeAbsolute(carTypes);
      StyleUtils.setLeft(carTypes, remaindersWidth + brandsWidth);
      StyleUtils.setTop(carTypes, rowHeight + heightMargin);
      StyleUtils.setHeight(carTypes, rowHeight);

      if (remaindersWidth + brandsWidth > 0) {
        StyleUtils.setWidth(carTypes, width - remaindersWidth - brandsWidth);
      }

      add(carTypes);
    }
  }
}
