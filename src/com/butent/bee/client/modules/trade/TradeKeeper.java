package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;

public final class TradeKeeper {

  public static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(TRADE_MODULE);
    args.addQueryItem(TRADE_METHOD, name);
    return args;
  }

  public static void register() {
    TradeUtils.registerTotalRenderer(TBL_SALE_ITEMS, VAR_TOTAL);
    TradeUtils.registerTotalRenderer(TBL_PURCHASE_ITEMS, VAR_TOTAL);
  }

  private TradeKeeper() {
  }
}
