package com.butent.bee.client.modules.orders.ec;

import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcWidgetFactory;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Link;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.function.Consumer;

public class OrdEcItemDetails extends Flow {

  public static final String STYLE_PRIMARY = "ItemDetails";

  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_WRAPPER = "wrapper";
  private static final String STYLE_TABLE = "table";
  private static final String STYLE_LABEL = "label";
  private static final String STYLE_VALUE = "value";
  private static final String STYLE_CURRENCY = "currency";
  private static final String STYLE_ITEM_GALLERY = BeeConst.CSS_CLASS_PREFIX + "Gallery-";
  private static final String STYLE_ITEM_GALLERY_IMAGE = STYLE_ITEM_GALLERY + "image";
  private static final String STYLE_ITEM_GALLERY_PICTURE = STYLE_ITEM_GALLERY + "picture";
  private static final String STYLE_ITEM_GALLERY_PANEL = STYLE_ITEM_GALLERY_PICTURE + "-panel";
  private static final String STYLE_OPEN = EcStyles.name("ItemPicture-") + "open";

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
        Flow descContainer = new Flow(stylePrefix + "description-" + STYLE_CONTAINER);

        CustomDiv descriptionLabel = new CustomDiv();
        descriptionLabel.setHtml(Localized.dictionary().description());
        descContainer.add(descriptionLabel);

        CustomDiv descriptionWidget = new CustomDiv(stylePrefix + "description");
        descriptionWidget.setHtml(item.getDescription());
        descContainer.add(descriptionWidget);

        container.add(descContainer);
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

  private static void renderDocuments(Long itemId, Consumer<Multimap<String, Pair<String,
      String>>> consumer) {
    OrdEcKeeper.getDocuments(itemId, consumer);
  }

  private static Widget renderPicture(OrdEcItem item, int width, int height) {
    OrdEcItemPicture widget = new OrdEcItemPicture(item.getCaption());
    EcStyles.add(widget, STYLE_PRIMARY, "picture");
    StyleUtils.setSize(widget, width, height);

    OrdEcKeeper.setBackgroundPicture(item.getId(), widget);

    return widget;
  }

  private static Widget renderPictures(OrdEcItem item) {
    Flow panel = new Flow(STYLE_ITEM_GALLERY_PANEL);
    List<String> pictures = OrdEcKeeper.getPictures().getIfPresent(item.getId());

    if (pictures != null) {
      if (pictures.size() > 0) {
        for (String picture : pictures) {
          Flow flow = new Flow(STYLE_ITEM_GALLERY_PICTURE);
          Image image = new Image(picture);
          image.addStyleName(STYLE_ITEM_GALLERY_IMAGE);

          image.addClickHandler(event -> {
            Image biggerImg = new Image(image.getUrl());
            biggerImg.addStyleName(STYLE_OPEN);
            Global.showModalWidget(biggerImg);
          });

          flow.add(image);
          panel.add(flow);
        }
      }
    }
    return panel;
  }

  private static Widget renderRemainders(final OrdEcItem item) {
    if (item == null || BeeUtils.isEmpty(item.getRemainder())) {
      return null;
    }

    final String stylePrefix = EcStyles.name(STYLE_PRIMARY, "remainders-");
    Flow container = new Flow(stylePrefix + STYLE_CONTAINER);

    final HtmlTable table = new HtmlTable(stylePrefix + STYLE_TABLE);

    OrdEcKeeper.ensureStockLabel(input -> {
      Label warehouseWidget = new Label(OrdEcKeeper.getStockLabel());
      table.setWidgetAndStyle(0, 0, warehouseWidget, stylePrefix + "warehouse");

      Label stockWidget = new Label(item.getRemainder());
      table.setWidgetAndStyle(0, 1, stockWidget, stylePrefix + "stock");
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
      widget.add(remainders, Localized.dictionary().ecItemDetailsRemainders(), null, null, null);
    }

    Widget pictures = renderPictures(item);
    if (pictures != null) {
      widget.add(pictures, Localized.dictionary().pictures(), null, null, null);
    }

    renderDocuments(item.getId(), input -> {
      if (input.size() > 0) {
        for (String key : input.keySet()) {
          final String stylePrefix = EcStyles.name(STYLE_PRIMARY, "documents-");
          Flow container = new Flow(stylePrefix + STYLE_CONTAINER);
          for (Pair<String, String> pair : input.get(key)) {
            Flow flow = new Flow();
            Link link = new Link(pair.getA(), FileUtils.getUrl(Long.valueOf(pair.getB()),
                pair.getA()));
            flow.add(link);
            container.add(flow);
          }
          widget.add(container, key, null, null, null);
        }
      }
    });

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
