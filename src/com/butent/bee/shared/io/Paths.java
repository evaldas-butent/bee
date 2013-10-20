package com.butent.bee.shared.io;

import com.google.common.collect.Lists;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.List;

public final class Paths {

  public static final String IMAGE_DIR = "images";
  
  public static final String FILE_ICON_DIR = "fileicons";
  public static final String FLAG_DIR = "flags";
  public static final String PHOTO_DIR = "photo";

  private static final String CSS_DIR = "css";
  private static final String CSS_EXT = "css";

  private static final String JS_DIR = "js";
  private static final String JS_EXT = "js";
  
  private static final String SEGMENT_SEPARATOR = "/";
  
  public static String buildPath(String first, String second, String... rest) {
    List<String> segments = Lists.newArrayList(first, second);
    if (rest != null) {
      segments.addAll(Arrays.asList(rest));
    }
    return BeeUtils.join(SEGMENT_SEPARATOR, segments);
  }
  
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
      return buildPath(dir, FileNameUtils.defaultExtension(name, ext));
    }
  }
  
  private Paths() {
  }
}
