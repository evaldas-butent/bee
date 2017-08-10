package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.VIEW_COMPANIES;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;

import java.util.Arrays;
import java.util.List;

public abstract class CustomInvoiceForm extends InvoiceERPForm {

  @Override
  public void onStartNewRow(FormView form, IsRow row) {
    if (Data.containsColumn(form.getViewName(), COL_TRADE_CUSTOMER)) {
      Long customerId = row.getLong(form.getDataIndex(COL_TRADE_CUSTOMER));
      List<String> operationColumns = Arrays.asList(COL_TRADE_OPERATION, COL_OPERATION_NAME);

      if (DataUtils.isId(customerId)) {
        Queries.getRow(VIEW_COMPANIES, customerId, result -> {
          if (result != null) {
            operationColumns.forEach(column -> row.setValue(form.getDataIndex(column),
                result.getString(Data.getColumnIndex(VIEW_COMPANIES, column))));
            form.refreshBySource(COL_TRADE_OPERATION);
          }
        });
      }
    }
    super.onStartNewRow(form, row);
  }
}
