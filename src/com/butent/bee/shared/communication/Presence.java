package com.butent.bee.shared.communication;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public enum Presence implements HasCaption {
  ONLINE(FontAwesome.CHECK_CIRCLE, "online", Colors.PALEGREEN) {
    @Override
    public String getCaption() {
      return Localized.dictionary().presenceOnline();
    }
  },

  IDLE(FontAwesome.COFFEE, "idle", Colors.LIGHTSKYBLUE) {
    @Override
    public String getCaption() {
      return Localized.dictionary().presenceIdle();
    }
  },

  AWAY(FontAwesome.BICYCLE, "away", Colors.LIGHTYELLOW) {
    @Override
    public String getCaption() {
      return Localized.dictionary().presenceAway();
    }
  },

  OFFLINE(FontAwesome.MINUS_CIRCLE, "offline", Colors.PINK) {
    @Override
    public String getCaption() {
      return Localized.dictionary().presenceOffline();
    }
  };

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "Presence-";

  private final FontAwesome icon;

  private final String styleSuffix;
  private final String background;

  Presence(FontAwesome icon, String styleSuffix, String background) {
    this.icon = icon;

    this.styleSuffix = styleSuffix;
    this.background = background;
  }

  public String getBackground() {
    return background;
  }

  public FontAwesome getIcon() {
    return icon;
  }

  @Override
  public abstract String getCaption();

  public String getStyleName() {
    return STYLE_PREFIX + styleSuffix;
  }

  public String getStyleSuffix() {
    return styleSuffix;
  }
}
