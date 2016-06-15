package com.butent.bee.client.modules.orders.ec;

import com.butent.bee.client.Settings;

public final class OrdEcKeeper {

  public static void register() {
  }

  public static boolean showGlobalSearch() {
    return Settings.getBoolean("showGlobalSearch");
  }

  private OrdEcKeeper() {
  }
}
