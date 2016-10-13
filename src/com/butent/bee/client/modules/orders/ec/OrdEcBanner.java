package com.butent.bee.client.modules.orders.ec;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.utils.BrowsingContext;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class OrdEcBanner extends Flow {

  private static final String STYLE_BANNER = "Banner";
  private static final String STYLE_SUFFIX_CONTAINER = "container";
  private static final String STYLE_SUFFIX_PICTURE = "picture";
  private static final String STYLE_SUFFIX_LINK = "link";
  private static final String STYLE_SUFFIX_NO_LINK = "noLink";

  private static Widget renderBanner(String picture, Integer width, Integer height,
      final String link) {

    Image widget = new Image(picture);
    EcStyles.add(widget, STYLE_BANNER, STYLE_SUFFIX_PICTURE);

    if (BeeUtils.isPositive(width)) {
      StyleUtils.setWidth(widget, width);
    }
    if (BeeUtils.isPositive(height)) {
      StyleUtils.setHeight(widget, height);
    }

    if (BeeUtils.isEmpty(link)) {
      EcStyles.add(widget, STYLE_BANNER, STYLE_SUFFIX_NO_LINK);

    } else {
      widget.setTitle(link);
      EcStyles.add(widget, STYLE_BANNER, STYLE_SUFFIX_LINK);

      widget.addClickHandler(event -> BrowsingContext.open(link));
    }

    return widget;
  }

  public OrdEcBanner(BeeRowSet banners) {
    super(EcStyles.name("Promo"));

    if (!DataUtils.isEmpty(banners)) {
      Flow bannerContainer = new Flow(EcStyles.name(STYLE_BANNER, STYLE_SUFFIX_CONTAINER));

      int pictureIndex = banners.getColumnIndex(EcConstants.COL_BANNER_PICTURE);
      int widthIndex = banners.getColumnIndex(EcConstants.COL_BANNER_WIDTH);
      int heightIndex = banners.getColumnIndex(EcConstants.COL_BANNER_HEIGHT);
      int linkIndex = banners.getColumnIndex(EcConstants.COL_BANNER_LINK);

      for (BeeRow banner : banners.getRows()) {
        bannerContainer.add(renderBanner(banner.getString(pictureIndex),
            banner.getInteger(widthIndex), banner.getInteger(heightIndex),
            banner.getString(linkIndex)));
      }

      add(bannerContainer);
    }
  }
}