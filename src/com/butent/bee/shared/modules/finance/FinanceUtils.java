package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public final class FinanceUtils {

  public static boolean isValidEntry(Long debit, Long credit) {
    return DataUtils.isId(debit) && DataUtils.isId(credit) && !Objects.equals(debit, credit);
  }

  public static boolean isValidEntry(DateTime dt, Long debit, Long credit,
      Double amount, Long currency) {

    return dt != null && isValidEntry(debit, credit)
        && BeeUtils.nonZero(amount) && DataUtils.isId(currency);
  }

  private FinanceUtils() {
  }
}
