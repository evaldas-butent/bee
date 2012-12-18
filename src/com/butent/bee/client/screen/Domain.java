package com.butent.bee.client.screen;

import com.google.gwt.resources.client.ImageResource;

import com.butent.bee.client.Global;

public enum Domain {
  FAVORITE(0, Global.getImages().bookmark(), null, false, false),
  CALENDAR(1, Global.getImages().calendar(), null, true, true),
  REPORT(2, Global.getImages().report(), "Ataskaitos", false, false),
  MAIL(3, Global.getImages().plane(), null, true, true),
  ADMIN(4, Global.getImages().configure(), "Admin", false, true);

  private final int ordinal;
  private final ImageResource imageResource;
  private final String caption;

  private final boolean closable;
  private final boolean removable;

  private Domain(int ordinal, ImageResource imageResource, String caption, boolean closable,
      boolean removable) {
    this.ordinal = ordinal;
    this.imageResource = imageResource;
    this.caption = caption;

    this.closable = closable;
    this.removable = removable;
  }

  String getCaption() {
    return caption;
  }

  ImageResource getImageResource() {
    return imageResource;
  }

  int getOrdinal() {
    return ordinal;
  }

  boolean isClosable() {
    return closable;
  }

  boolean isRemovable() {
    return removable;
  }
}
