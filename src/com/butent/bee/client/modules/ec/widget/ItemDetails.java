package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.ArticleCriteria;
import com.butent.bee.shared.modules.ec.ArticleSupplier;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.modules.ec.EcItemInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ItemDetails extends Flow {

  public static final String STYLE_PRIMARY = "ItemDetails";

  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_WRAPPER = "wrapper";
  private static final String STYLE_TABLE = "table";

  private static Widget renderAddToCart(EcItem item) {
    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "addToCart-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    int price = item.getPrice();

    CartAccumulator accumulator = new CartAccumulator(item, 1);
    accumulator.addStyleName(stylePrefix + "cart");

    container.add(accumulator);

    if (price > 0) {
      String priceInfo = BeeUtils.joinWords(Localized.getConstants().ecItemPrice()
          + BeeConst.STRING_COLON, EcUtils.renderCents(price), EcConstants.CURRENCY);
      Label itemPrice = new Label(priceInfo);
      itemPrice.addStyleName(stylePrefix + "price");

      container.add(itemPrice);
    }

    return container;
  }

  private static Widget renderAnalogs(EcItem item) {
    if (!item.hasAnalogs()) {
      return null;
    }

    ParameterList params = EcKeeper.createArgs(EcConstants.SVC_GET_ITEM_ANALOGS);
    params.addDataItem(EcConstants.COL_TCD_ARTICLE, item.getArticleId());
    params.addDataItem(EcConstants.COL_TCD_ARTICLE_NR, item.getCode());
    params.addDataItem(EcConstants.COL_TCD_BRAND, item.getBrand());
    
    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "itemAnalogs-");
    final Flow container = new Flow(stylePrefix + STYLE_CONTAINER);
    final Flow wrapper = new Flow(stylePrefix + STYLE_WRAPPER);
    
    final String styleAnalogInfo = stylePrefix + "info";

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          return;
        }
        List<EcItem> items = EcKeeper.getResponseItems(response);
        
        for (EcItem analog : items) {
          Widget infoLabel = renderAnalogItem(analog);
          if (infoLabel != null) {
            infoLabel.addStyleName(styleAnalogInfo);
            wrapper.add(infoLabel);
          }
        }

        container.add(wrapper);
      }
    });

    return container;
  }
  
  private static Widget renderAnalogItem(final EcItem item) {

    if (item == null) {
      return null;
    }

    String code = BeeUtils.isEmpty(item.getCode()) ? "" : item.getCode();
    String name = BeeUtils.isEmpty(item.getName()) ? "" : item.getName();
    String brand = EcKeeper.getBrandName(item.getBrand());
    brand = BeeUtils.isEmpty(brand) ? "" : brand;
    
    String labelText = BeeUtils.join(" ", code, name, brand);

    if (BeeUtils.isEmpty(labelText)) {
      return null;
    }

    Label content = new Label(labelText);

    content.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        EcKeeper.openItem(item, true);
      }
    });

    return content;
  }

  private static Widget renderCarTypes(EcItemInfo info) {
    if (info == null || BeeUtils.isEmpty(info.getCarTypes())) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "carTypes-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    Flow wrapper = new Flow(stylePrefix + STYLE_WRAPPER);

    String styleCarInfo = stylePrefix + "info";

    for (EcCarType ect : info.getCarTypes()) {
      Label infoLabel = new Label(ect.getInfo());
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
      
      if (!BeeUtils.isEmpty(item.getDescription())) {
        CustomDiv descriptionWidget = new CustomDiv(stylePrefix + "description");
        descriptionWidget.setHtml(item.getDescription());
        container.add(descriptionWidget);
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
    if (item == null || BeeUtils.isEmpty(item.getSuppliers())) {
      return null;
    }

    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "suppliers-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    HtmlTable table = new HtmlTable(stylePrefix + STYLE_TABLE);

    int row = 0;
    int col;

    for (ArticleSupplier as : item.getSuppliers()) {
      col = 0;

      table.setHtml(row, col++, BeeUtils.toString(as.getRealPrice()));

      table.setHtml(row, col++, as.getSupplier().name());
      table.setHtml(row, col++, as.getSupplierId());

      row++;
    }

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + STYLE_WRAPPER);

    container.add(wrapper);

    return container;
  }

  private static Widget renderTabbedPages(EcItem item, EcItemInfo info) {
    if (item == null || info == null) {
      return null;
    }

    TabbedPages widget = new TabbedPages();
    Widget remainders = renderRemainders(item);
    Widget oeNumbers = renderOeNumbers(info);
    Widget suppliers = renderSuppliers(item);
    Widget carTypes = renderCarTypes(info);
    Widget analogs = renderAnalogs(item);

    if (remainders != null) {
      StyleUtils.makeAbsolute(remainders);
      StyleUtils.setLeft(remainders, 0);

      widget.add(remainders, Localized.getConstants().ecItemDetailsRemainders());
    }

    if (oeNumbers != null) {
      StyleUtils.makeAbsolute(oeNumbers);

      widget.add(oeNumbers, Localized.getConstants().ecItemDetailsOeNumbers());
    }

    if (suppliers != null) {
      StyleUtils.makeAbsolute(suppliers);
      StyleUtils.setLeft(suppliers, 0);

      widget.add(suppliers, Localized.getConstants().ecItemDetailsSuppliers());
    }

    if (carTypes != null) {
      StyleUtils.makeAbsolute(carTypes);

      widget.add(carTypes, Localized.getConstants().ecItemDetailsCarTypes());
    }

    if (analogs != null) {
      StyleUtils.makeAbsolute(analogs);

      widget.add(analogs, Localized.getConstants().ecItemAnalogs());
    }

    return widget;
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
        StyleUtils.setTop(addToCart, heightMargin);

        add(addToCart);
      }
    }

    Widget itemDataTabs = renderTabbedPages(item, info);

    int top2 = rowHeight + heightMargin;

    if (itemDataTabs != null) {

      StyleUtils.setTop(itemDataTabs, top2);
      StyleUtils.setHeight(itemDataTabs, rowHeight);

      add(itemDataTabs);
    }
  }
}
