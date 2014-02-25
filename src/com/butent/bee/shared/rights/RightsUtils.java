package com.butent.bee.shared.rights;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public final class RightsUtils {

  private static final String NAME_SEPARATOR = ".";

  public static String buildModuleName(Module module) {
    Assert.notNull(module);
    return normalizeName(module.getName());
  }

  public static String buildModuleName(Module module, SubModule subModule) {
    String mod = buildModuleName(module);

    if (subModule != null) {
      mod += NAME_SEPARATOR + normalizeName(subModule.getName());
    }
    return mod;
  }

  public static String buildName(String parent, String child) {
    if (BeeUtils.isEmpty(parent)) {
      return normalizeName(child);
    } else {
      return normalizeName(parent) + NAME_SEPARATOR + normalizeName(child);
    }
  }

  public static String normalizeName(String name) {
    return BeeUtils.trim(name);
  }

  private RightsUtils() {
  }
}