package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.rights.Module;

public final class TradeKeeper {

  public static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(Module.TRADE.getName());
    args.addQueryItem(CommonsConstants.SERVICE, name);
    return args;
  }

  public static void register() {
    TradeUtils.registerTotalRenderer(TBL_SALE_ITEMS, VAR_TOTAL);
    TradeUtils.registerTotalRenderer(TBL_PURCHASE_ITEMS, VAR_TOTAL);
  }

  private TradeKeeper() {
  }
}
