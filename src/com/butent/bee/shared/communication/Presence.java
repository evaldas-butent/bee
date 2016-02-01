package com.butent.bee.shared.communication;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public enum Presence implements HasCaption {
  ONLINE(FontAwesome.CHECK_CIRCLE, Localized.getConstants().presenceOnline(), "online"),
  IDLE(FontAwesome.COFFEE, Localized.getConstants().presenceIdle(), "idle"),
  AWAY(FontAwesome.BICYCLE, Localized.getConstants().presenceAway(), "away"),
  OFFLINE(FontAwesome.MINUS_CIRCLE, Localized.getConstants().presenceOffline(), "offline");

  private final FontAwesome icon;
  private final String caption;

  private final String styleSuffix;

  Presence(FontAwesome icon, String caption, String styleSuffix) {
    this.icon = icon;
    this.caption = caption;
    this.styleSuffix = styleSuffix;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public FontAwesome getIcon() {
    return icon;
  }

  public String getStyleName() {
    return BeeConst.CSS_CLASS_PREFIX + "Presence-" + styleSuffix;
  }

  public String getStyleSuffix() {
    return styleSuffix;
  }
}
