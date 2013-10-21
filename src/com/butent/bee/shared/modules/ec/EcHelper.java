package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

public final class EcHelper {

  public static String renderCents(int cents) {
    if (cents >= 0) {
      String s = BeeUtils.toLeadingZeroes(cents, 3);
      int len = s.length();
      return s.substring(0, len - 2) + BeeConst.STRING_POINT + s.substring(len - 2);
    } else {
      return BeeConst.STRING_MINUS + renderCents(-cents);
    }
  }

  private EcHelper() {
  }
}
