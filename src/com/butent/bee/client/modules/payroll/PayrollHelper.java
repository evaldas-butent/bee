package com.butent.bee.client.modules.payroll;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;

final class PayrollHelper {

  static String format(YearMonth ym) {
    if (ym == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinWords(ym.getYear(), Format.renderMonthFullStandalone(ym).toLowerCase());
    }
  }

  private PayrollHelper() {
  }
}
