package com.butent.bee.shared.ui;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Captions {

  private static final BeeLogger logger = LogUtils.getLogger(Captions.class);

  private static final Map<String, Class<? extends Enum<?>>> CLASSES = Maps.newHashMap();

  private static final Table<String, String, String> COLUMN_KEYS = HashBasedTable.create();

  public static String getCaption(Class<? extends Enum<?>> clazz, int index) {
    Assert.notNull(clazz);
    if (!BeeUtils.isOrdinal(clazz, index)) {
      return null;
    }

    Enum<?> constant = clazz.getEnumConstants()[index];
    if (constant instanceof HasCaption) {
      return ((HasCaption) constant).getCaption();
    } else {
      return BeeUtils.proper(constant);
    }
  }

  public static String getCaption(HasCaption source) {
    return (source == null) ? null : source.getCaption();
  }
  
  public static String getCaption(String key, int index) {
    if (BeeUtils.isEmpty(key)) {
      logger.severe("Caption key not specified");
      return null;
    }

    List<String> list = getCaptions(key);
    if (!BeeUtils.isIndex(list, index)) {
      logger.severe("cannot get caption: key", key, "index", index);
      return null;
    } else {
      return list.get(index);
    }
  }

  public static List<String> getCaptions(Class<? extends Enum<?>> clazz) {
    Assert.notNull(clazz);
    List<String> result = Lists.newArrayList();

    for (Enum<?> constant : clazz.getEnumConstants()) {
      if (constant instanceof HasCaption) {
        result.add(((HasCaption) constant).getCaption());
      } else {
        result.add(BeeUtils.proper(constant));
      }
    }
    return result;
  }

  public static List<String> getCaptions(String key) {
    Assert.notEmpty(key);
    Class<? extends Enum<?>> clazz = CLASSES.get(BeeUtils.normalize(key));

    if (clazz == null) {
      logger.severe("Captions not registered: " + key);
      return null;
    } else {
      return getCaptions(clazz);
    }
  }

  public static String getColumnKey(String viewName, String columnid) {
    return COLUMN_KEYS.get(viewName, columnid);
  }

  public static Table<String, String, String> getColumnKeys() {
    return COLUMN_KEYS;
  }

  public static Set<String> getRegisteredKeys() {
    return CLASSES.keySet();
  }

  public static String getValueCaption(String viewName, String columnid, int index) {
    return getCaption(getColumnKey(viewName, columnid), index);
  }

  public static boolean isColumnRegistered(String viewName, String columnid) {
    return COLUMN_KEYS.contains(viewName, columnid);
  }

  public static <E extends Enum<?> & HasCaption> String register(Class<E> clazz) {
    Assert.notNull(clazz);
    return register(NameUtils.getClassName(clazz), clazz);
  }

  public static <E extends Enum<?> & HasCaption> String register(String key, Class<E> clazz) {
    Assert.notEmpty(key);
    Assert.notNull(clazz);

    String normalized = BeeUtils.normalize(key);
    CLASSES.put(normalized, clazz);

    return normalized;
  }

  public static void registerColumn(String viewName, String columnid, String key) {
    COLUMN_KEYS.put(viewName, columnid, key);
  }

  private Captions() {
  }
}
