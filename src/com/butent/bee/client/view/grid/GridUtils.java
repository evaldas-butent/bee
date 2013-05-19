package com.butent.bee.client.view.grid;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class GridUtils {

  private static final BeeLogger logger = LogUtils.getLogger(GridUtils.class);

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
  
  private GridUtils() {
  }
}
