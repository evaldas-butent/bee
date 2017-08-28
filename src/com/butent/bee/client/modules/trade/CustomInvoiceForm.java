package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.VIEW_COMPANIES;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.view.DataInfo;

import java.util.stream.Stream;

public abstract class CustomInvoiceForm extends InvoiceERPForm {

  @Override
  public void onStartNewRow(FormView form, IsRow row) {
    if (Data.containsColumn(form.getViewName(), COL_TRADE_CUSTOMER)) {
      Long customerId = row.getLong(form.getDataIndex(COL_TRADE_CUSTOMER));

      if (DataUtils.isId(customerId)) {
        Queries.getRow(VIEW_COMPANIES, customerId, result -> {
          if (result != null) {
            DataInfo targetInfo = Data.getDataInfo(form.getViewName());
            DataInfo sourceInfo = Data.getDataInfo(VIEW_COMPANIES);

            Stream.of(COL_TRADE_OPERATION, COL_TRADE_WAREHOUSE_FROM).forEach(col ->
                RelationUtils.copyWithDescendants(sourceInfo, col, result, targetInfo, col, row));

            form.refresh();
          }
        });
      }
    }
    super.onStartNewRow(form, row);
  }
}
