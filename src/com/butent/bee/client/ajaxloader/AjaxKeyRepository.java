package com.butent.bee.client.ajaxloader;

import com.google.gwt.user.client.Window.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains core information about available external APIs in the system.
 */

public class AjaxKeyRepository {
  private static Map<String, String> keys = new HashMap<String, String>();

  static {
    keys.put("localhost:8080",
        "ABQIAAAAG8LzhtshQCjpSshU_uJjmxTwM0brOpm-All5BF6PoaKBxRWWERTZER2lJ4GnsG8nvhKLOQ20degaEQ");
    keys.put("127.0.0.1:8080",
        "ABQIAAAAG8LzhtshQCjpSshU_uJjmxTBfUk9TZrBRaIteybtnU2KziHEpRQvhPNTjo7DMczjrRD3yBPRJ_BSQQ");
  }

  public static String getKey() {
    return keys.get(Location.getHost());
  }

  public static String getKey(String location) {
    return keys.get(location);
  }

  public static Map<String, String> getKeys() {
    return keys;
  }

  public static void putKey(String location, String ajaxApiKey) {
    keys.put(location, ajaxApiKey);
  }

  private AjaxKeyRepository() {
  }
}
