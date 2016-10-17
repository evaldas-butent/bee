package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.style.ColorStyleProvider;
import com.butent.bee.client.style.ConditionalStyle;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.rights.Module;

public final class FinanceKeeper {

  public static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.FINANCE, method);
  }

  public static void register() {
    MenuService.FINANCE_CONFIGURATION.setHandler(parameters -> openConfiguration());

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

  private static void openConfiguration() {
    Queries.getRowSet(VIEW_FINANCE_CONFIGURATION, null, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (DataUtils.isEmpty(result)) {
          RowFactory.createRow(VIEW_FINANCE_CONFIGURATION);
        } else {
          RowEditor.open(VIEW_FINANCE_CONFIGURATION, result.getRow(0), Opener.modeless());
        }
      }
    });
  }

  private FinanceKeeper() {
  }
}
