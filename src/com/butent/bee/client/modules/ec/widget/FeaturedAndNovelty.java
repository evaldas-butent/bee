package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class FeaturedAndNovelty extends Flow {

  private static final String STYLE_FEATURED = "Featured";
  private static final String STYLE_NOVELTY = "Novelty";

  private static final String STYLE_LABEL = "label";
  private static final String STYLE_CONTAINER = "container";
  private static final String STYLE_ITEM = "item";

  public FeaturedAndNovelty(List<EcItem> items) {
    super(EcStyles.name("FeaturedAndNovelty"));

    int total = items.size();
    int featuredCount = BeeUtils.randomInt(0, total + 1);
    int noveltyCount = total - featuredCount;

    if (featuredCount > 0) {
      Label featuredLabel = new Label(Localized.constants.ecFeaturedItems());
      EcStyles.add(featuredLabel, STYLE_FEATURED, STYLE_LABEL);
      add(featuredLabel);

      Flow featuredContainer = new Flow();
      EcStyles.add(featuredContainer, STYLE_FEATURED, STYLE_CONTAINER);

      for (int i = 0; i < featuredCount; i++) {
        Widget item = EcUtils.randomPicture(60, 200);
        EcStyles.add(item, STYLE_FEATURED, STYLE_ITEM);
        featuredContainer.add(item);
      }

      add(featuredContainer);
    }

    if (noveltyCount > 0) {
      Label noveltyLabel = new Label(Localized.constants.ecNoveltyItems());
      EcStyles.add(noveltyLabel, STYLE_NOVELTY, STYLE_LABEL);
      add(noveltyLabel);

      Flow noveltyContainer = new Flow();
      EcStyles.add(noveltyContainer, STYLE_NOVELTY, STYLE_CONTAINER);

      for (int i = 0; i < noveltyCount; i++) {
        Widget item = EcUtils.randomPicture(30, 120);
        EcStyles.add(item, STYLE_NOVELTY, STYLE_ITEM);
        noveltyContainer.add(item);
      }

      add(noveltyContainer);
    }
  }
}
