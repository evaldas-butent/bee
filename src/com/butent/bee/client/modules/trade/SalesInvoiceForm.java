package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class SalesInvoiceForm extends PrintFormInterceptor {

  SalesInvoiceForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new SalesInvoiceForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new TradeDocumentRenderer(VIEW_SALE_ITEMS, COL_SALE);
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    Queries.getRowSet(VIEW_SALE_ITEMS, null, Filter.equals(COL_SALE, getActiveRowId()),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            dataConsumer.accept(new BeeRowSet[] {result});
          }
        });
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();

    for (String col : Arrays.asList(COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER)) {
      Long id = getLongValue(col);

      if (DataUtils.isId(id)) {
        companies.put(col, id);
      }
    }
    if (!companies.containsKey(COL_TRADE_SUPPLIER)) {
      companies.put(COL_TRADE_SUPPLIER, BeeKeeper.getUser().getCompany());
    }
    super.getReportParameters((defaultParameters) -> {
      Queries.getRowSet(ClassifierConstants.VIEW_COMPANIES, null, Filter.idIn(companies.values()),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              for (BeeRow row : result) {
                for (Map.Entry<String, Long> entry : companies.entrySet()) {
                  if (Objects.equals(row.getId(), entry.getValue())) {
                    for (BeeColumn column : result.getColumns()) {
                      String value = DataUtils.getString(result, row, column.getId());

                      if (!BeeUtils.isEmpty(value)) {
                        defaultParameters.put(entry.getKey() + column.getId(), value);
                      }
                    }
                  }
                }
              }
              parametersConsumer.accept(defaultParameters);
            }
          });
    });
  }
}
