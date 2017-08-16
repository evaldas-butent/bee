package com.butent.bee.server.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_AMOUNT;
import static com.butent.bee.shared.modules.trade.TradeConstants.TBL_PURCHASES;

import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;

@Singleton
@LocalBean
public class CustomTradeModuleBean {

  public Double calculateErpPayment(SimpleRowSet.SimpleRow payment, String table,
      SimpleRowSet debts, String idName, Long id) {
    Double paid = payment.getDouble("suma");

    if (BeeUtils.equals(table, TBL_PURCHASES)) {
      Double amount = BeeUtils.toDoubleOrNull(debts
          .getValueByKey(idName, BeeUtils.toString(id), COL_TRADE_AMOUNT));

      if (BeeUtils.isMore(paid, amount)) {
        paid = amount;
      }
    }
    return paid;
  }
}
