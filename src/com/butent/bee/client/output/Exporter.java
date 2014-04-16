package com.butent.bee.client.output;

import com.butent.bee.shared.export.XWorkbook;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.NameUtils;

public final class Exporter {

  private static final BeeLogger logger = LogUtils.getLogger(Exporter.class);

  public static void toExcel(XWorkbook wb) {
    if (wb == null || wb.isEmpty()) {
      logger.warning(NameUtils.getClassName(Exporter.class), "workbook is empty");
    }
  }

  private Exporter() {
  }
}
