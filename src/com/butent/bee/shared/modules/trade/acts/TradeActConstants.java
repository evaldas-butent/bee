package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.utils.EnumUtils;

public final class TradeActConstants {

  public static final String VIEW_TRADE_ACTS = "TradeActs";

  public static void register() {
    EnumUtils.register(TradeActKind.class);
  }

  private TradeActConstants() {
  }
}
