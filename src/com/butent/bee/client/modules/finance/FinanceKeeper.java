package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.style.ColorStyleProvider;
import com.butent.bee.client.style.ConditionalStyle;
import com.butent.bee.shared.rights.Module;

public final class FinanceKeeper {

  public static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.FINANCE, method);
  }

  public static void register() {
    ConditionalStyle.registerGridColumnStyleProvider(GRID_FINANCIAL_RECORDS, COL_FIN_JOURNAL,
        ColorStyleProvider.create(VIEW_FINANCIAL_RECORDS,
            ALS_JOURNAL_BACKGROUND, ALS_JOURNAL_FOREGROUND));

    ConditionalStyle.registerGridColumnStyleProvider(GRID_FINANCIAL_RECORDS, COL_FIN_DEBIT,
        ColorStyleProvider.create(VIEW_FINANCIAL_RECORDS,
            ALS_DEBIT_BACKGROUND, ALS_DEBIT_FOREGROUND));
    ConditionalStyle.registerGridColumnStyleProvider(GRID_FINANCIAL_RECORDS, COL_FIN_CREDIT,
        ColorStyleProvider.create(VIEW_FINANCIAL_RECORDS,
            ALS_CREDIT_BACKGROUND, ALS_CREDIT_FOREGROUND));
  }

  private FinanceKeeper() {
  }
}
