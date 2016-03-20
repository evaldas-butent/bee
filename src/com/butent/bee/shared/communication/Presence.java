package com.butent.bee.shared.communication;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public enum Presence implements HasCaption {
  ONLINE(FontAwesome.CHECK_CIRCLE, Localized.dictionary().presenceOnline(), "online",
      Colors.PALEGREEN),
  IDLE(FontAwesome.COFFEE, Localized.dictionary().presenceIdle(), "idle", Colors.LIGHTSKYBLUE),
  AWAY(FontAwesome.BICYCLE, Localized.dictionary().presenceAway(), "away", Colors.LIGHTYELLOW),
  OFFLINE(FontAwesome.MINUS_CIRCLE, Localized.dictionary().presenceOffline(), "offline",
      Colors.PINK);

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "Presence-";

  private final FontAwesome icon;
  private final String caption;

  private final String styleSuffix;
  private final String background;

  Presence(FontAwesome icon, String caption, String styleSuffix, String background) {
    this.icon = icon;
    this.caption = caption;

    this.styleSuffix = styleSuffix;
    this.background = background;
  }

  public String getBackground() {
    return background;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public FontAwesome getIcon() {
    return icon;
  }

  public String getStyleName() {
    return STYLE_PREFIX + styleSuffix;
  }

  public String getStyleSuffix() {
    return styleSuffix;
  }
}
