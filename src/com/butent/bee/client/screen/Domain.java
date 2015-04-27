package com.butent.bee.client.screen;

import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public enum Domain implements HasCaption {
  NEWS(FontAwesome.RSS, Localized.getConstants().domainNews(), false, false),
  FAVORITES(FontAwesome.STAR_O, null, false, false),
  WORKSPACES(FontAwesome.NEWSPAPER_O, Localized.getConstants().workspaces(), false, false),
  REPORTS(FontAwesome.FILE_TEXT_O, Localized.getConstants().reports(), false, false),
  CALENDAR(FontAwesome.CALENDAR, null, true, true),
  MAIL(FontAwesome.ENVELOPE_O, null, true, true),
  ONLINE(FontAwesome.USERS, Localized.getConstants().domainOnline(), false, false),
  ROOMS(FontAwesome.COMMENTS_O, Localized.getConstants().domainRooms(), false, false),
  ADMIN(FontAwesome.MAGIC, "Admin", false, true);

  private final FontAwesome icon;
  private final String caption;

  private final boolean closable;
  private final boolean removable;

  private Domain(FontAwesome icon, String caption, boolean closable, boolean removable) {
    this.icon = icon;
    this.caption = caption;

    this.closable = closable;
    this.removable = removable;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  FontAwesome getIcon() {
    return icon;
  }

  boolean isClosable() {
    return closable;
  }

  boolean isRemovable() {
    return removable;
  }
}
