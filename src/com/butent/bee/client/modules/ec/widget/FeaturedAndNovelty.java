package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class FeaturedAndNovelty extends Flow {

  private static final String STYLE_FEATURED = "Featured";
  private static final String STYLE_NOVELTY = "Novelty";

  private static final String STYLE_LABEL = "label";
  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_TABLE = "table";
  private static final String STYLE_ITEM = "item";

  private static final String STYLE_BANNER = "banner";
  private static final String STYLE_PICTURE = "picture";
  private static final String STYLE_NAME = "name";
  private static final String STYLE_CODE = "code";
  private static final String STYLE_PRICE = "price";
  private static final String STYLE_DRILL = "drill";
  
  public FeaturedAndNovelty(List<EcItem> items) {
    super(EcStyles.name("FeaturedAndNovelty"));
    
    Horizontal featuredTable = null;
    Horizontal noveltyTable = null;

    Multimap<Long, ItemPicture> pictureWidgets = ArrayListMultimap.create();
    
    for (EcItem item : items) {
      ItemPicture pictureWidget = new ItemPicture();
      pictureWidgets.put(item.getArticleId(), pictureWidget);

      if (item.isFeatured()) {
        if (featuredTable == null) {
          Label featuredLabel = new Label(Localized.getConstants().ecFeaturedItems());
          EcStyles.add(featuredLabel, STYLE_FEATURED, STYLE_LABEL);
          add(featuredLabel);

          Flow featuredContainer = new Flow();
          EcStyles.add(featuredContainer, STYLE_FEATURED, STYLE_CONTAINER);
          
          featuredTable = new Horizontal();
          EcStyles.add(featuredTable, STYLE_FEATURED, STYLE_TABLE);

          featuredContainer.add(featuredTable);
          add(featuredContainer);
        }

        featuredTable.add(renderItem(item, STYLE_FEATURED,
            Localized.getConstants().ecFeaturedBanner(), pictureWidget));

      } else {
        if (noveltyTable == null) {
          Label noveltyLabel = new Label(Localized.getConstants().ecNoveltyItems());
          EcStyles.add(noveltyLabel, STYLE_NOVELTY, STYLE_LABEL);
          add(noveltyLabel);
          
          Flow noveltyContainer = new Flow();
          EcStyles.add(noveltyContainer, STYLE_NOVELTY, STYLE_CONTAINER);
          
          noveltyTable = new Horizontal();
          EcStyles.add(noveltyTable, STYLE_NOVELTY, STYLE_TABLE);
          
          noveltyContainer.add(noveltyTable);
          add(noveltyContainer);
        }

        noveltyTable.add(renderItem(item, STYLE_NOVELTY, 
            Localized.getConstants().ecNoveltyBanner(), pictureWidget));
      }
    }

    if (!pictureWidgets.isEmpty()) {
      EcKeeper.setBackgroundPictures(pictureWidgets);
    }
  }
  
  private static Widget renderItem(final EcItem item, String primaryStyle, String bannerText,
      Widget pictureWidget) {

    Flow panel = new Flow(EcStyles.name(primaryStyle, STYLE_ITEM));
    
    if (!BeeUtils.isEmpty(bannerText)) {
      Label banner = new Label(bannerText);
      EcStyles.add(banner, primaryStyle, STYLE_BANNER);
      panel.add(banner);
    }
    
    if (pictureWidget != null) {
      EcStyles.add(pictureWidget, primaryStyle, STYLE_PICTURE);
      panel.add(pictureWidget);
    }
    
    String name = item.getName();
    if (!BeeUtils.isEmpty(name)) {
      Label itemName = new Label(name);
      EcStyles.add(itemName, primaryStyle, STYLE_NAME);
      panel.add(itemName);
    }
      
    String code = item.getCode();
    if (!BeeUtils.isEmpty(code)) {
      Label itemCode = new Label(code);
      EcStyles.add(itemCode, primaryStyle, STYLE_CODE);
      panel.add(itemCode);
    }
    
    int price = item.getPrice();
    if (price > 0) {
      String priceInfo = BeeUtils.joinWords(Localized.getConstants().ecItemPrice() 
          + BeeConst.STRING_COLON, EcUtils.renderCents(price), EcConstants.CURRENCY);
      Label itemPrice = new Label(priceInfo);
      EcStyles.add(itemPrice, primaryStyle, STYLE_PRICE);
      panel.add(itemPrice);
    }
    
    Button details = new Button(Localized.getConstants().ecShowDetails());
    EcStyles.add(details, primaryStyle, STYLE_DRILL);
    details.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        EcKeeper.openItem(item, true);
      }
    });

    panel.add(details);
    
    return panel;
  }
}
