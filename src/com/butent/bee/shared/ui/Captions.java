package com.butent.bee.shared.ui;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

public final class Captions {

  public static boolean isCaption(String caption) {
    return !BeeUtils.isEmpty(caption) && !BeeConst.STRING_MINUS.equals(caption);
  }

  private Captions() {
  }
}
