package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
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
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.style.StyleUtils.ScrollBars;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcItem;
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
  private static final String STYLE_NO_STOCK = EcStyles.name(STYLE_PRIMARY, "noStock");
  private static final String STYLE_HAS_STOCK = EcStyles.name(STYLE_PRIMARY, "hasStock");
  private static final String STYLE_QUANTITY = EcStyles.name(STYLE_PRIMARY, "quantity");
  private static final String STYLE_LIST_PRICE = EcStyles.name(STYLE_PRIMARY, "listPrice");
  private static final String STYLE_PRICE = EcStyles.name(STYLE_PRIMARY, "price");

  private static final String STYLE_ITEM_NAME = EcStyles.name(STYLE_PRIMARY, "name");
  private static final String STYLE_ITEM_CODE = EcStyles.name(STYLE_PRIMARY, "code");
  private static final String STYLE_ITEM_ANALOGS = EcStyles.name(STYLE_PRIMARY, "analogs");

  private static final String STYLE_ITEM_BRAND = EcStyles.name(STYLE_PRIMARY, "brand");

  private static final String STYLE_LABEL = "-label";

  private static final int COL_PICTURE = 0;
  private static final int COL_INFO = 1;
  private static final int COL_STOCK_1 = 2;
  private static final int COL_STOCK_2 = 3;
  private static final int COL_LIST_PRICE = 4;
  private static final int COL_PRICE = 5;
  private static final int COL_QUANTITY = 6;

  private static final int PAGE_SIZE = 50;

  private final HtmlTable table;
  private final Button moreWidget;

  private final List<EcItem> data = Lists.newArrayList();

  public ItemList(List<EcItem> items) {
    this();
    render(items);
  }

  private ItemList() {
    super();
    addStyleName(EcStyles.name(STYLE_PRIMARY));

    this.table = new HtmlTable();
    EcStyles.add(table, STYLE_PRIMARY, "table");
    add(table);

    this.moreWidget = new Button(Localized.getConstants().ecMoreItems());
    EcStyles.add(moreWidget, STYLE_PRIMARY, "more");
    moreWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showMoreItems();
      }
    });
    add(moreWidget);
  }

  public void render(List<EcItem> items) {
    if (!table.isEmpty()) {
      table.clear();
    }
    StyleUtils.hideDisplay(moreWidget);

    if (!data.isEmpty()) {
      data.clear();
    }

    if (!BeeUtils.isEmpty(items)) {
      data.addAll(items);
      int row = 0;

      if (items.size() > 1) {
        Label caption = new Label(BeeUtils.joinWords(Localized.getConstants().ecFoundItems(),
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

      Label listPriceLabel = new Label(Localized.getConstants().ecListPrice());
      EcStyles.add(listPriceLabel, STYLE_PRIMARY, STYLE_LIST_PRICE + STYLE_LABEL);
      table.setWidget(row, COL_LIST_PRICE, listPriceLabel);

      Label priceLabel = new Label(Localized.getConstants().ecClientPrice());
      EcStyles.add(priceLabel, STYLE_PRIMARY, STYLE_PRICE + STYLE_LABEL);
      table.setWidget(row, COL_PRICE, priceLabel);

      table.getRowFormatter().addStyleName(row, STYLE_HEADER_ROW);

      Multimap<Long, ItemPicture> pictureWidgets = ArrayListMultimap.create();

      int pageSize = (items.size() > PAGE_SIZE * 3 / 2) ? PAGE_SIZE : items.size();

      row++;
      for (EcItem item : items) {
        if (row > pageSize) {
          break;
        }

        ItemPicture pictureWidget = new ItemPicture();
        renderItem(row++, item, pictureWidget);

        pictureWidgets.put(item.getArticleId(), pictureWidget);
      }

      if (pageSize < items.size()) {
        StyleUtils.unhideDisplay(moreWidget);
      }

      if (!pictureWidgets.isEmpty()) {
        EcKeeper.setBackgroundPictures(pictureWidgets);
      }
    }
  }

  private static Widget renderInfo(final EcItem item) {
    Flow panel = new Flow();

    String name = item.getName();
    if (!BeeUtils.isEmpty(name)) {
      Label itemName = new Label(name);
      itemName.addStyleName(STYLE_ITEM_NAME);

      String categoryNames = BeeUtils.join(BeeConst.STRING_EOL, EcKeeper.getCategoryNames(item));
      if (!BeeUtils.isEmpty(categoryNames)) {
        itemName.setTitle(categoryNames);
      }

      itemName.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          EcKeeper.openItem(item, true);
        }
      });

      panel.add(itemName);
    }

    String code = item.getCode();
    if (!BeeUtils.isEmpty(code)) {
      Widget codeWidget = EcUtils.renderField(Localized.getConstants().ecItemCode(), code,
          STYLE_ITEM_CODE);
      panel.add(codeWidget);
      codeWidget.setTitle(BeeUtils.joinWords("ArticleID:", item.getArticleId()));
    }

    if (item.hasAnalogs()) {
      InternalLink analogs = new InternalLink(Localized.getConstants().ecItemAnalogs());
      analogs.addStyleName(STYLE_ITEM_ANALOGS);
      panel.add(analogs);

      analogs.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          ParameterList params = EcKeeper.createArgs(EcConstants.SVC_GET_ITEM_ANALOGS);
          params.addDataItem(EcConstants.COL_TCD_ARTICLE, item.getArticleId());
          params.addDataItem(EcConstants.COL_TCD_ARTICLE_NR, item.getCode());
          params.addDataItem(EcConstants.COL_TCD_BRAND_NAME,
              EcKeeper.getBrandName(item.getBrand()));

          BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              response.notify(BeeKeeper.getScreen());

              if (!response.hasErrors()) {
                List<EcItem> items = EcKeeper.getResponseItems(response);

                if (!BeeUtils.isEmpty(items)) {
                  ItemPanel itemPanel = new ItemPanel();
                  itemPanel.setHeight("600px");
                  StyleUtils.setOverflow(itemPanel.getElement(), ScrollBars.VERTICAL, "auto");
                  EcKeeper.renderItems(itemPanel, items);
                  Global.getMsgBoxen().showWidget(itemPanel);
                } else {
                  BeeKeeper.getScreen().notifyWarning(Localized.getConstants().noData());
                }
              }
            }
          });
        }
      });
    }

    Long brand = item.getBrand();
    if (brand != null) {
      Widget brandWidget = EcUtils.renderField(Localized.getConstants().ecItemBrand(),
          EcKeeper.getBrandName(brand), STYLE_ITEM_BRAND);
      panel.add(brandWidget);
    }

    return panel;
  }

  private void renderItem(int row, EcItem item, Widget pictureWidget) {
    if (pictureWidget != null) {
      table.setWidgetAndStyle(row, COL_PICTURE, pictureWidget, STYLE_PICTURE);
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

    if (listPrice > 0 && listPrice >= price) {
      Widget listPriceWidget = renderPrice(listPrice, STYLE_LIST_PRICE);
      table.setWidgetAndStyle(row, COL_LIST_PRICE, listPriceWidget, STYLE_LIST_PRICE);
    }
    if (price > 0) {
      Widget priceWidget = renderPrice(price, STYLE_PRICE);
      table.setWidgetAndStyle(row, COL_PRICE, priceWidget, STYLE_PRICE);
    }

    Widget accumulator = new CartAccumulator(item, 1);
    table.setWidgetAndStyle(row, COL_QUANTITY, accumulator, STYLE_QUANTITY);

    table.getRowFormatter().addStyleName(row, STYLE_ITEM_ROW);
  }

  private static Widget renderPrice(int price, String style) {
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

  private static Widget renderStock(int stock) {
    String text = (stock > 0) ? BeeUtils.toString(stock) : Localized.getConstants().ecStockAsk();
    Label widget = new Label(text);
    widget.addStyleName((stock > 0) ? STYLE_HAS_STOCK : STYLE_NO_STOCK);
    return widget;
  }

  private void showMoreItems() {
    int pageStart = table.getRowCount() - 1;
    int more = data.size() - pageStart;

    if (more > 0) {
      Multimap<Long, ItemPicture> pictureWidgets = ArrayListMultimap.create();

      int pageSize = (more > PAGE_SIZE * 3 / 2) ? PAGE_SIZE : more;

      for (int i = pageStart; i < pageStart + pageSize; i++) {
        EcItem item = data.get(i);
        ItemPicture pictureWidget = new ItemPicture();

        renderItem(i + 1, item, pictureWidget);

        pictureWidgets.put(item.getArticleId(), pictureWidget);
      }

      if (pageSize >= more) {
        StyleUtils.hideDisplay(moreWidget);
      }

      if (!pictureWidgets.isEmpty()) {
        EcKeeper.setBackgroundPictures(pictureWidgets);
      }
    }
  }
}
