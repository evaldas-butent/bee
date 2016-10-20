package com.butent.bee.client.modules.finance;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.finance.FinanceConstants;

class TradeFinancialRecordsGrid extends FinanceGrid {

  TradeFinancialRecordsGrid() {
    super();
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeFinancialRecordsGrid();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (presenter != null && presenter.getHeader() != null
        && BeeKeeper.getUser().canCreateData(FinanceConstants.VIEW_FINANCIAL_RECORDS)) {

      Button post = new Button(Localized.dictionary().finPostAction());
      presenter.getHeader().addCommandItem(post);
    }

    super.afterCreatePresenter(presenter);
  }
}
