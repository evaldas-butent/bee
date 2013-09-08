package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class ItemList extends Flow {

  private static final String STYLE_PRIMARY = "ItemList";
  private static final String STYLE_WAREHOUSE = "warehouse";

  private static final String STYLE_HEADER_ROW = EcStyles.name(STYLE_PRIMARY, "headerRow");
  private static final String STYLE_ITEM_ROW = EcStyles.name(STYLE_PRIMARY, "itemRow");

  private static final String STYLE_PICTURE = EcStyles.name(STYLE_PRIMARY, "picture");
  private static final String STYLE_INFO = EcStyles.name(STYLE_PRIMARY, "info");
  private static final String STYLE_STOCK_0 = EcStyles.name(STYLE_PRIMARY, "stock0");
  private static final String STYLE_STOCK_1 = EcStyles.name(STYLE_PRIMARY, "stock1");
  private static final String STYLE_STOCK_2 = EcStyles.name(STYLE_PRIMARY, "stock2");
  private static final String STYLE_STOCK_WRAPPER = EcStyles.name(STYLE_PRIMARY, "stockWrapper");
  private static final String STYLE_NO_STOCK = EcStyles.name(STYLE_PRIMARY, "noStock");
  private static final String STYLE_HAS_STOCK = EcStyles.name(STYLE_PRIMARY, "hasStock");
  private static final String STYLE_UNIT = EcStyles.name(STYLE_PRIMARY, "unit");
  private static final String STYLE_QUANTITY = EcStyles.name(STYLE_PRIMARY, "quantity");
  private static final String STYLE_LIST_PRICE = EcStyles.name(STYLE_PRIMARY, "listPrice");
  private static final String STYLE_PRICE = EcStyles.name(STYLE_PRIMARY, "price");

  private static final String STYLE_ITEM_NAME = EcStyles.name(STYLE_PRIMARY, "name");
  private static final String STYLE_ITEM_CODE = EcStyles.name(STYLE_PRIMARY, "code");
  private static final String STYLE_ITEM_ANALOGS = EcStyles.name(STYLE_PRIMARY, "analogs");

  private static final String STYLE_ITEM_BRAND = EcStyles.name(STYLE_PRIMARY, "brand");
  private static final String STYLE_ITEM_DESCRIPTION = EcStyles.name(STYLE_PRIMARY, "description");

  private static final String STYLE_LABEL = "-label";

  private static final int PAGE_SIZE = 50;

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
          params.addDataItem(EcConstants.COL_TCD_BRAND, item.getBrand());

          BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              response.notify(BeeKeeper.getScreen());

              if (!response.hasErrors()) {
                List<EcItem> items = EcKeeper.getResponseItems(response);

                if (!BeeUtils.isEmpty(items)) {
                  ItemList analogList = new ItemList(items);

                  Simple analogPanel = new Simple(analogList);
                  analogPanel.addStyleName(STYLE_ITEM_ANALOGS + "-panel");
                  
                  String caption = BeeUtils.joinWords(Localized.getConstants().ecItemAnalogs(),
                      item.getName(), item.getCode(), EcKeeper.getBrandName(item.getBrand()));
                  DialogBox dialog = DialogBox.create(caption, STYLE_ITEM_ANALOGS + "-dialog");
                  
                  dialog.setWidget(analogPanel);
                  
                  dialog.setAnimationEnabled(true);
                  dialog.setHideOnEscape(true);

                  dialog.center();
                  
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
      if (brandWidget != null) {
        panel.add(brandWidget);
      }
    }

    String description = item.getDescription();
    if (!BeeUtils.isEmpty(description)) {
      CustomDiv descriptionWidget = new CustomDiv(STYLE_ITEM_DESCRIPTION);
      descriptionWidget.setHTML(description);
      panel.add(descriptionWidget);
    }

    return panel;
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

  private static Widget renderStock(int stock, String unit) {
    Flow wrapper = new Flow(STYLE_STOCK_WRAPPER);

    String text = (stock > 0) ? BeeUtils.toString(stock) : Localized.getConstants().ecStockAsk();
    InlineLabel stockWidget = new InlineLabel(text);
    stockWidget.addStyleName((stock > 0) ? STYLE_HAS_STOCK : STYLE_NO_STOCK);
    wrapper.add(stockWidget);

    if (stock > 0 && unit != null) {
      InlineLabel unitWidget = new InlineLabel(unit);
      unitWidget.addStyleName(STYLE_UNIT);
      wrapper.add(unitWidget);
    }

    return wrapper;
  }

  private final HtmlTable table;
  private final Button moreWidget;

  private final List<EcItem> data = Lists.newArrayList();
  private final String primaryBranch;

  private final String secondaryBranch;

  private final Collection<String> primaryWarehouses;

  private final Collection<String> secondaryWarehouses;

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

    this.primaryBranch = EcKeeper.getPrimaryBranch();
    this.secondaryBranch = EcKeeper.getSecondaryBranch();

    this.primaryWarehouses = EcKeeper.getWarehouses(primaryBranch);
    this.secondaryWarehouses = EcKeeper.getWarehouses(secondaryBranch);
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
      int col = 0;

      if (items.size() > 1) {
        Label caption = new Label(BeeUtils.joinWords(Localized.getConstants().ecFoundItems(),
            BeeUtils.bracket(items.size())));
        EcStyles.add(caption, STYLE_PRIMARY, "caption");
        table.setWidget(row, col, caption);
      }

      col += 2;

      if (hasWarehouses()) {
        if (!primaryWarehouses.isEmpty()) {
          Label wrh1 = new Label(primaryBranch);
          EcStyles.add(wrh1, STYLE_PRIMARY, STYLE_WAREHOUSE);
          EcStyles.add(wrh1, STYLE_PRIMARY, STYLE_WAREHOUSE + "1");
          table.setWidget(row, col++, wrh1);
        }

        if (!secondaryWarehouses.isEmpty()) {
          Label wrh2 = new Label(secondaryBranch);
          EcStyles.add(wrh2, STYLE_PRIMARY, STYLE_WAREHOUSE);
          EcStyles.add(wrh2, STYLE_PRIMARY, STYLE_WAREHOUSE + "2");
          table.setWidget(row, col++, wrh2);
        }

      } else {
        Label wrh = new Label("S1");
        EcStyles.add(wrh, STYLE_PRIMARY, STYLE_WAREHOUSE);
        EcStyles.add(wrh, STYLE_PRIMARY, STYLE_WAREHOUSE + "0");
        table.setWidget(row, col++, wrh);
      }

      Label listPriceLabel = new Label(Localized.getConstants().ecListPrice());
      EcStyles.add(listPriceLabel, STYLE_PRIMARY, STYLE_LIST_PRICE + STYLE_LABEL);
      table.setWidget(row, col++, listPriceLabel);

      Label priceLabel = new Label(Localized.getConstants().ecClientPrice());
      EcStyles.add(priceLabel, STYLE_PRIMARY, STYLE_PRICE + STYLE_LABEL);
      table.setWidget(row, col++, priceLabel);

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

  private boolean hasWarehouses() {
    return !primaryWarehouses.isEmpty() || !secondaryWarehouses.isEmpty();
  }

  private void renderItem(int row, EcItem item, Widget pictureWidget) {
    int col = 0;
    if (pictureWidget != null) {
      table.setWidgetAndStyle(row, col, pictureWidget, STYLE_PICTURE);
    }
    col++;

    Widget info = renderInfo(item);
    if (info != null) {
      table.setWidgetAndStyle(row, col, info, STYLE_INFO);
    }
    col++;

    if (hasWarehouses()) {
      if (!primaryWarehouses.isEmpty()) {
        Widget stock1 = renderStock(item.getStock(primaryWarehouses), item.getUnit());
        if (stock1 != null) {
          table.setWidgetAndStyle(row, col++, stock1, STYLE_STOCK_1);
        }
      }

      if (!secondaryWarehouses.isEmpty()) {
        Widget stock2 = renderStock(item.getStock(secondaryWarehouses), item.getUnit());
        if (stock2 != null) {
          table.setWidgetAndStyle(row, col++, stock2, STYLE_STOCK_2);
        }
      }

    } else {
      Widget stock0 = renderStock(item.totalStock(), item.getUnit());
      if (stock0 != null) {
        table.setWidgetAndStyle(row, col++, stock0, STYLE_STOCK_0);
      }
    }

    int listPrice = item.getListPrice();
    int price = item.getPrice();

    if (listPrice > 0 && listPrice >= price) {
      Widget listPriceWidget = renderPrice(listPrice, STYLE_LIST_PRICE);
      table.setWidgetAndStyle(row, col, listPriceWidget, STYLE_LIST_PRICE);
    }
    col++;

    if (price > 0) {
      Widget priceWidget = renderPrice(price, STYLE_PRICE);
      table.setWidgetAndStyle(row, col, priceWidget, STYLE_PRICE);
    }
    col++;

    Widget accumulator = new CartAccumulator(item, 1);
    table.setWidgetAndStyle(row, col++, accumulator, STYLE_QUANTITY);

    table.getRowFormatter().addStyleName(row, STYLE_ITEM_ROW);
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
