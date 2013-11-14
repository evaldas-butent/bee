package com.butent.bee.shared.ui;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

public final class Captions {

  private static final Table<String, String, String> COLUMN_KEYS = HashBasedTable.create();

  public static String getCaption(HasCaption source) {
    return (source == null) ? null : source.getCaption();
  }

  public static String getColumnKey(String viewName, String columnId) {
    return COLUMN_KEYS.get(viewName, columnId);
  }

  public static Table<String, String, String> getColumnKeys() {
    return COLUMN_KEYS;
  }

  public static String getValueCaption(String viewName, String columnId, int index) {
    return EnumUtils.getCaption(getColumnKey(viewName, columnId), index);
  }

  public static boolean isCaption(String caption) {
    return !BeeUtils.isEmpty(caption) && !BeeConst.STRING_MINUS.equals(caption);
  }

  public static boolean isColumnRegistered(String viewName, String columnId) {
    return COLUMN_KEYS.contains(viewName, columnId);
  }

  public static void registerColumn(String viewName, String columnId, String key) {
    COLUMN_KEYS.put(viewName, columnId, key);
  }

  private Captions() {
  }
}
