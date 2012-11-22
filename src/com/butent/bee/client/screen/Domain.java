package com.butent.bee.client.screen;

import com.google.gwt.resources.client.ImageResource;

import com.butent.bee.client.Global;

public enum Domain {
  FAVORITE(0, Global.getImages().bookmark(), null, false),
  CALENDAR(1, Global.getImages().calendar(), null, true),
  REPORT(2, Global.getImages().report(), "Ataskaitos", false),
  ADMIN(3, Global.getImages().configure(), "Admin", true),
  WHITE_ZONE(4, null, null, false);
  
  private final int ordinal;
  private final ImageResource imageResource;
  private final String caption;
  private final boolean closable;

  private Domain(int ordinal, ImageResource imageResource, String caption, boolean closable) {
    this.ordinal = ordinal;
    this.imageResource = imageResource;
    this.caption = caption;
    this.closable = closable;
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
}
