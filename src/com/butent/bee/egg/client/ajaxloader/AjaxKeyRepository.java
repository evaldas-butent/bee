package com.butent.bee.egg.client.ajaxloader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window.Location;

public class AjaxKeyRepository {
  private static AjaxKeyConstants keys = GWT.create(AjaxKeyConstants.class);

  public static String getKey() {
    return keys.ajaxKeys().get(Location.getHost());
  }

  public static String getKey(String location) {
    return keys.ajaxKeys().get(location);
  }

  public static void putKey(String location, String ajaxApiKey) {
    keys.ajaxKeys().put(location, ajaxApiKey);
  }

  public static void setKeys(AjaxKeyConstants keys) {
    AjaxKeyRepository.keys = keys;
  }

  private AjaxKeyRepository() {
  }
}
