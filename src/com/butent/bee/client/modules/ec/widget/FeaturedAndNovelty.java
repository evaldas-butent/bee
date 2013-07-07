package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
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

    int total = items.size();
    int featuredCount = BeeUtils.randomInt(0, total + 1);
    int noveltyCount = total - featuredCount;

    if (featuredCount > 0) {
      Label featuredLabel = new Label(Localized.getConstants().ecFeaturedItems());
      EcStyles.add(featuredLabel, STYLE_FEATURED, STYLE_LABEL);
      add(featuredLabel);

      Flow featuredContainer = new Flow();
      EcStyles.add(featuredContainer, STYLE_FEATURED, STYLE_CONTAINER);
      
      Horizontal featuredTable = new Horizontal();
      EcStyles.add(featuredTable, STYLE_FEATURED, STYLE_TABLE);
      
      String banner = Localized.getConstants().ecFeaturedBanner();
      for (int i = 0; i < featuredCount; i++) {
        featuredTable.add(renderItem(items.get(i), STYLE_FEATURED, banner));
      }

      featuredContainer.add(featuredTable);
      add(featuredContainer);
    }

    if (noveltyCount > 0) {
      Label noveltyLabel = new Label(Localized.getConstants().ecNoveltyItems());
      EcStyles.add(noveltyLabel, STYLE_NOVELTY, STYLE_LABEL);
      add(noveltyLabel);

      Flow noveltyContainer = new Flow();
      EcStyles.add(noveltyContainer, STYLE_NOVELTY, STYLE_CONTAINER);

      Horizontal noveltyTable = new Horizontal();
      EcStyles.add(noveltyTable, STYLE_NOVELTY, STYLE_TABLE);
      
      String banner = Localized.getConstants().ecNoveltyBanner();
      for (int i = featuredCount; i < total; i++) {
        noveltyTable.add(renderItem(items.get(i), STYLE_NOVELTY, banner));
      }

      noveltyContainer.add(noveltyTable);
      add(noveltyContainer);
    }
  }
  
  private static Widget renderItem(EcItem item, String primaryStyle, String bannerText) {
    Flow panel = new Flow(EcStyles.name(primaryStyle, STYLE_ITEM));
    
    if (!BeeUtils.isEmpty(bannerText)) {
      Label banner = new Label(bannerText);
      EcStyles.add(banner, primaryStyle, STYLE_BANNER);
      panel.add(banner);
    }
    
    Widget picture = EcUtils.randomPicture(30, 100);
    if (picture != null) {
      EcStyles.add(picture, primaryStyle, STYLE_PICTURE);
      panel.add(picture);
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
      String priceInfo = BeeUtils.joinWords(Localized.getConstants().price() 
          + BeeConst.STRING_COLON, EcUtils.renderCents(price), EcConstants.CURRENCY);
      Label itemPrice = new Label(priceInfo);
      EcStyles.add(itemPrice, primaryStyle, STYLE_PRICE);
      panel.add(itemPrice);
    }
    
    Button details = new Button(Localized.getConstants().ecShowDetails());
    EcStyles.add(details, primaryStyle, STYLE_DRILL);
    panel.add(details);
    
    return panel;
  }
}
