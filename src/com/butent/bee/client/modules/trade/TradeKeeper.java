package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;

public final class TradeKeeper {

  public static void register() {
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(TRADE_MODULE);
    args.addQueryItem(TRADE_METHOD, name);
    return args;
  }

  private TradeKeeper() {
  }
}
