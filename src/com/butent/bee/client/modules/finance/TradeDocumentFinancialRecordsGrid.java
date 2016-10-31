package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;

class TradeDocumentFinancialRecordsGrid extends FinanceGrid {

  TradeDocumentFinancialRecordsGrid() {
    super();
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeDocumentFinancialRecordsGrid();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (presenter != null && presenter.getHeader() != null
        && BeeKeeper.getUser().canCreateData(VIEW_FINANCIAL_RECORDS)) {

      Button post = new Button(Localized.dictionary().finPostAction(), event -> post());
      presenter.getHeader().addCommandItem(post);
    }

    super.afterCreatePresenter(presenter);
  }

  private void post() {
    Long docId = ViewHelper.getParentRowId(ViewHelper.asWidget(getGridView()),
        TradeConstants.VIEW_TRADE_DOCUMENTS);

    if (DataUtils.isId(docId)) {
      ParameterList params = FinanceKeeper.createArgs(SVC_POST_TRADE_DOCUMENT);
      params.addQueryItem(Service.VAR_ID, docId);

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          GridView gridView = getGridView();

          if (gridView != null && response != null) {
            response.notify(gridView);

            if (response.hasResponse() && getGridPresenter() != null) {
              getGridPresenter().refresh(false, true);
            }
          }
        }
      });
    }
  }
}
