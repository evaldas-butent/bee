package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.utils.EnumUtils;

public final class TradeActConstants {

  public static final String VIEW_TRADE_ACTS = "TradeActs";
  public static final String VIEW_TRADE_ACT_TEMPLATES = "TradeActTemplates";

  public static final String COL_TA_KIND = "Kind";
  public static final String COL_TA_OPERATION = "Operation";
  public static final String COL_TA_STATUS = "Status";

  public static final String GRID_TRADE_ACTS = "TradeActs";
  public static final String GRID_TRADE_ACT_TEMPLATES = "TradeActTemplates";

  public static void register() {
    EnumUtils.register(TradeActKind.class);
    EnumUtils.register(TradeActTimeUnit.class);
  }

  private TradeActConstants() {
  }
}
