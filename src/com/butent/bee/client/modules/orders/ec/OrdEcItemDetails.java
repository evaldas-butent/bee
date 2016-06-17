package com.butent.bee.client.modules.orders.ec;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcWidgetFactory;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Link;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.utils.BeeUtils;

public class OrdEcItemDetails extends Flow {

  public static final String STYLE_PRIMARY = "ItemDetails";

  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_WRAPPER = "wrapper";
  private static final String STYLE_TABLE = "table";
  private static final String STYLE_LABEL = "label";
  private static final String STYLE_VALUE = "value";
  private static final String STYLE_CURRENCY = "currency";

  private static Widget renderAddToCart(OrdEcItem item) {
    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "addToCart-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    OrdEcCartAccumulator accumulator = new OrdEcCartAccumulator(item, 1);
    accumulator.addStyleName(stylePrefix + "cart");

    container.add(accumulator);

    HtmlTable table = new HtmlTable(stylePrefix + "price-table");
    int row = 0;

    int colLabel = 0;
    int colValue = 1;
    int colCurrency = 2;

    int price = item.getPrice();

    String pfx;

    if (price > 0) {
      pfx = stylePrefix + "price-";

      table.setText(row, colLabel, Localized.dictionary().ecClientPrice(), pfx + STYLE_LABEL);
      table.setText(row, colValue, EcUtils.formatCents(price), pfx + STYLE_VALUE);
      table.setText(row, colCurrency, EcConstants.CURRENCY, pfx + STYLE_CURRENCY);

      EcStyles.markPrice(table.getRow(row));
      row++;
    }

    container.add(table);

    return container;
  }

  private static Widget renderInfo(OrdEcItem item) {
    String stylePrefix = EcStyles.name(STYLE_PRIMARY, "info-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    if (item != null) {
      Widget itemArticleWidget = EcWidgetFactory.renderField(Localized.dictionary().article(),
          item.getArticle(), stylePrefix + "itemCode");
      if (itemArticleWidget != null) {
        container.add(itemArticleWidget);
      }

      if (!BeeUtils.isEmpty(item.getDescription())) {
        Flow flow = new Flow(stylePrefix + "description");

        CustomDiv descriptionLabel = new CustomDiv();
        descriptionLabel.setHtml(Localized.dictionary().description());
        flow.add(descriptionLabel);

        CustomDiv descriptionWidget = new CustomDiv();
        descriptionWidget.setHtml(item.getDescription());
        flow.add(descriptionWidget);

        container.add(flow);
      }

      if (!BeeUtils.isEmpty(item.getLink())) {
        Flow flow = new Flow(stylePrefix + "link-container");

        CustomDiv linkLabel = new CustomDiv();
        linkLabel.setHtml(Localized.dictionary().link());
        flow.add(linkLabel);

        Link linkWidget = new Link(item.getLink(), item.getLink());
        linkWidget.addStyleName(stylePrefix + "link");
        flow.add(linkWidget);

        container.add(flow);
      }
    }

    return container;
  }

  private static Widget renderPicture(OrdEcItem item, int width, int height) {
    OrdEcItemPicture widget = new OrdEcItemPicture(item.getCaption());
    EcStyles.add(widget, STYLE_PRIMARY, "picture");
    StyleUtils.setSize(widget, width, height);

    OrdEcKeeper.setBackgroundPicture(item.getId(), widget);

    return widget;
  }

  private static Widget renderRemainders(final OrdEcItem item) {
    if (item == null || BeeUtils.isEmpty(item.getRemainder())) {
      return null;
    }

    final String stylePrefix = EcStyles.name(STYLE_PRIMARY, "remainders-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    final HtmlTable table = new HtmlTable(stylePrefix + STYLE_TABLE);

    OrdEcKeeper.ensureStockLabel(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean input) {
        Label warehouseWidget = new Label(OrdEcKeeper.getStockLabel());
        table.setWidgetAndStyle(0, 0, warehouseWidget, stylePrefix + "warehouse");

        Label stockWidget = new Label(item.getRemainder());
        table.setWidgetAndStyle(0, 1, stockWidget, stylePrefix + "stock");
      }
    });

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(stylePrefix + STYLE_WRAPPER);

    container.add(wrapper);

    return container;
  }

  private static Widget renderTabbedPages(OrdEcItem item) {
    if (item == null) {
      return null;
    }

    TabbedPages widget = new TabbedPages();

    Widget remainders = renderRemainders(item);
    if (remainders != null) {
      widget.add(remainders, Localized.dictionary().ecItemDetailsRemainders(), null, null);
    }

    return widget;
  }

  public OrdEcItemDetails(OrdEcItem item, boolean allowAddToCart) {
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

    Widget info = renderInfo(item);
    if (info != null) {
      StyleUtils.makeAbsolute(info);
      StyleUtils.setLeft(info, pictureWidth + widthMargin);
      StyleUtils.setTop(info, 0);
      StyleUtils.setHeight(info, rowHeight);

      add(info);
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

    Widget itemDataTabs = renderTabbedPages(item);

    if (itemDataTabs != null) {
      StyleUtils.setTop(itemDataTabs, rowHeight + heightMargin);
      StyleUtils.setHeight(itemDataTabs, rowHeight);

      add(itemDataTabs);
    }

  }
}
