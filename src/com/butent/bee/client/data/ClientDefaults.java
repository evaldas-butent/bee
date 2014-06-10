package com.butent.bee.client.data;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.data.Defaults;

public class ClientDefaults extends Defaults {

  private static Long currency;
  private static String currencyName;

  @Override
  public Object getValue(DefaultExpression defExpr, Object defValue) {
    Object value = null;

    if (defExpr == null) {
      value = defValue;
    } else {
      switch (defExpr) {
        case CURRENT_USER:
          value = BeeKeeper.getUser().getUserId();
          break;

        case MAIN_CURRENCY:
          value = getCurrency();
          break;

        case NEXT_NUMBER:
          break;

        default:
          value = super.getValue(defExpr, defValue);
          break;
      }
    }
    return value;
  }

  public static Long getCurrency() {
    return currency;
  }

  public static String getCurrencyName() {
    return currencyName;
  }

  public static void setCurrency(Long currency) {
    ClientDefaults.currency = currency;
  }

  public static void setCurrencyName(String currencyName) {
    ClientDefaults.currencyName = currencyName;
  }
}
