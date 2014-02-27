package com.butent.bee.shared.rights;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import com.butent.bee.shared.utils.BeeUtils;

public final class RightsUtils {

  private static final String NAME_SEPARATOR = ".";

  public static final Joiner JOINER = Joiner.on(NAME_SEPARATOR).skipNulls();
  public static final Splitter SPLITTER = Splitter.on(NAME_SEPARATOR);

  public static String buildName(String parent, String child) {
    if (BeeUtils.isEmpty(parent)) {
      return normalizeName(child);
    } else {
      return JOINER.join(normalizeName(parent), normalizeName(child));
    }
  }

  public static String normalizeName(String name) {
    return BeeUtils.trim(name);
  }

  private RightsUtils() {
  }
}