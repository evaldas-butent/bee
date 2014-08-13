package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.menu.MenuHandler;
import com.butent.bee.shared.menu.MenuService;

public final class TradeActKeeper {

  public static void register() {
    MenuService.TRADE_ACT_NEW.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        LogUtils.getRootLogger().warning(parameters, "not yet");
      }
    });

    MenuService.TRADE_ACT_LIST.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        LogUtils.getRootLogger().warning(parameters, "not yet");
      }
    });
  }

  private TradeActKeeper() {
  }
}
