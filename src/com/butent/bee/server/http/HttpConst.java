package com.butent.bee.server.http;

import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains constant references to attribute counters.
 */

public final class HttpConst {

  public static final String ATTRIBUTE_REQUEST_COUNTER = "request_counter";
  public static final String ATTRIBUTE_CONTEXT_COUNTER = "context_counter";
  public static final String ATTRIBUTE_SESSION_COUNTER = "session_counter";

  public static final String PARAM_USER = "user";
  public static final String PARAM_PASSWORD = "password";

  public static final String PARAM_LOCALE = "locale";
  public static final String PARAM_UI = "ui";

  public static final String PARAM_REGISTER = "register";
  public static final String PARAM_QUERY = "query";
  
  private static final String CSS_DIR = "css";
  private static final String CSS_EXT = "css";

  private static final String JS_DIR = "js";
  private static final String JS_EXT = "js";
  
  public static String getScriptPath(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return null;
    } else {
      return normalizePath(JS_DIR, fileName, JS_EXT);
    }
  }

  public static String getStyleSheetPath(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return null;
    } else {
      return normalizePath(CSS_DIR, fileName, CSS_EXT);
    }
  }
  
  private static String normalizePath(String dir, String name, String ext) {
    if (FileNameUtils.hasSeparator(name)) {
      return FileNameUtils.defaultExtension(name, ext);
    } else {
      return dir + String.valueOf(FileNameUtils.UNIX_SEPARATOR)
          + FileNameUtils.defaultExtension(name, ext);
    }
  }
  
  private HttpConst() {
  }
}
