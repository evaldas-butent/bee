package com.butent.bee.client.modules.trade;

import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.modules.trade.acts.TradeActKeeper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.shared.Consumer;
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

public class SalesInvoiceForm extends PrintFormInterceptor {

  SalesInvoiceForm() {
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (BeeUtils.inListSame(name, COL_SALE_PAYER, COL_TRADE_SUPPLIER)) {
      description.setAttribute("editEnabled",
          BeeUtils.toString(!TradeActKeeper.isClientArea()).toLowerCase());
    } else if (BeeUtils.same(name, COL_TRADE_CUSTOMER)) {
      description.setAttribute("editForm", TradeActKeeper.isClientArea()
          ? ClassifierConstants.FORM_NEW_COMPANY : ClassifierConstants.FORM_COMPANY);
    }

    return super.beforeCreateWidget(name, description);
  }

  @Override
  public FormInterceptor getInstance() {
    return new SalesInvoiceForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new TradeDocumentRenderer(VIEW_SALE_ITEMS, COL_SALE) {
      @Override
      public void onLoad(FormView form) {
        TradeActKeeper.ensureSendMailPrintableForm(form);
        super.afterCreate(form);
      }
    };
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
    super.getReportParameters((defaultParameters) ->
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
        }));
  }
}
