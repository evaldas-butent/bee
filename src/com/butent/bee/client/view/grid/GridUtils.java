package com.butent.bee.client.view.grid;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CustomProperties;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.rights.RightsUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

final class GridUtils {

  private static final BeeLogger logger = LogUtils.getLogger(GridUtils.class);

  static boolean containsColumn(Collection<ColumnDescription> columnDescriptions, String id) {
    return getColumnDescription(columnDescriptions, id) != null;
  }

  static ColumnDescription getColumnDescription(Collection<ColumnDescription> columnDescriptions,
      String id) {
    if (!BeeUtils.isEmpty(columnDescriptions)) {
      for (ColumnDescription columnDescription : columnDescriptions) {
        if (columnDescription.is(id)) {
          return columnDescription;
        }
      }
    }
    return null;
  }

  static ColumnInfo getColumnInfo(Collection<ColumnInfo> columns, String id) {
    if (!BeeUtils.isEmpty(columns)) {
      for (ColumnInfo columnInfo : columns) {
        if (columnInfo.is(id)) {
          return columnInfo;
        }
      }
    }
    return null;
  }

  static int getColumnIndex(List<ColumnInfo> columns, String id) {
    if (!BeeUtils.isEmpty(columns)) {
      for (int i = 0; i < columns.size(); i++) {
        if (columns.get(i).is(id)) {
          return i;
        }
      }
    }
    return BeeConst.UNDEF;
  }

  static int getIndex(List<String> names, String name) {
    int index = names.indexOf(name);
    if (index < 0) {
      logger.severe("name not found:", name);
    }
    return index;
  }

  static String normalizeValue(String value) {
    return BeeUtils.isEmpty(value) ? null : value.trim();
  }

  static void updateProperties(IsRow target, IsRow source) {
    if (BeeUtils.isEmpty(target.getProperties())) {
      if (!BeeUtils.isEmpty(source.getProperties())) {
        target.setProperties(source.getProperties().copy());
      }

    } else {
      CustomProperties retain = new CustomProperties();
      for (Map.Entry<String, String> property : target.getProperties().entrySet()) {
        if (RightsUtils.isStateRoleAlias(property.getKey())) {
          retain.put(property.getKey(), property.getValue());
        }
      }

      if (retain.isEmpty() && BeeUtils.isEmpty(source.getProperties())) {
        target.setProperties(null);

      } else {
        target.getProperties().clear();
        if (!retain.isEmpty()) {
          target.getProperties().putAll(retain);
        }

        if (!BeeUtils.isEmpty(source.getProperties())) {
          target.getProperties().putAll(source.getProperties());
        }
      }
    }
  }

  private GridUtils() {
  }
}
