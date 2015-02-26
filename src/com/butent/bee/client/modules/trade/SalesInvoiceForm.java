package com.butent.bee.client.modules.trade;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;

public class SalesInvoiceForm extends AbstractFormInterceptor {

  SalesInvoiceForm() {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      IsRow row = getActiveRow();

      if (DataUtils.hasId(row)) {
        DataInfo dataInfo = Data.getDataInfo(getFormView().getViewName());
        TradeDocumentRenderer renderer = new TradeDocumentRenderer(TradeConstants.VIEW_SALE_ITEMS,
            TradeConstants.COL_SALE);

        RowEditor.openForm(TradeConstants.FORM_PRINT_SALES_INVOICE, dataInfo, row, Opener.MODAL,
            null, renderer);
      }

      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new SalesInvoiceForm();
  }
}
