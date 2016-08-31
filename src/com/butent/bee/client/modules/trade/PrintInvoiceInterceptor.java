package com.butent.bee.client.modules.trade;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.ALS_CURRENCY_NAME;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrintInvoiceInterceptor extends AbstractFormInterceptor {

  Map<String, Widget> companies = new HashMap<>();
  HtmlTable invoiceDetails;
  List<Widget> totals = new ArrayList<>();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.inListSame(name, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER)) {
      companies.put(name, widget.asWidget());

    } else if (BeeUtils.same(name, "InvoiceDetails") && widget instanceof HtmlTable) {
      invoiceDetails = (HtmlTable) widget;

    } else if (BeeUtils.startsSame(name, "TotalInWords")) {
      totals.add(widget.asWidget());
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    for (String name : companies.keySet()) {
      Long id = form.getLongValue(name);

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      ClassifierUtils.getCompanyInfo(id, companies.get(name));
    }
    final Widget bankAccounts = form.getWidgetByName(COL_BANK_ACCOUNT);

    if (bankAccounts != null) {
      bankAccounts.getElement().setInnerHTML(null);
      Long customer = BeeUtils.nvl(getLongValue(COL_PAYER), getLongValue(COL_CUSTOMER));

      if (DataUtils.isId(customer)) {
        Queries.getRowSet("CompanyPayAccounts", Collections.singletonList("Account"),
            Filter.equals(COL_COMPANY, customer),
            new Queries.RowSetCallback() {
              @Override
              public void onSuccess(BeeRowSet result) {
                renderBankAccounts(bankAccounts, BeeUtils.nvl(getLongValue(COL_TRADE_SUPPLIER),
                    BeeKeeper.getUser().getUserData().getCompany()), result.getDistinctLongs(0));
              }
            });
      }
    }
    if (invoiceDetails != null) {
      TradeUtils.getDocumentItems(getViewName(), row.getId(),
          form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME), invoiceDetails);
    }
    for (Widget total : totals) {
      TradeUtils.getTotalInWords(form.getDoubleValue(COL_TRADE_AMOUNT),
          form.getLongValue(COL_TRADE_CURRENCY), total);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintInvoiceInterceptor();
  }

  private static void renderBankAccounts(final Widget widget, Long supplier, Set<Long> ids) {
    Queries.getRowSet(TBL_COMPANY_BANK_ACCOUNTS, Arrays.asList(ALS_CURRENCY_NAME, COL_BANK_ACCOUNT,
        ALS_BANK_NAME, COL_ADDRESS, COL_BANK_CODE, COL_SWIFT_CODE),
        Filter.and(Filter.equals(COL_COMPANY, supplier), Filter.idIn(ids)),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            Flow flow = new Flow();

            for (int i = 0; i < result.getNumberOfRows(); i++) {
              Flow item = new Flow(BeeUtils.isEmpty(result.getString(i, ALS_CURRENCY_NAME))
                  ? null : "correspondent");

              for (int j = 0; j < result.getNumberOfColumns(); j++) {
                String text = result.getString(i, j);

                if (!BeeUtils.isEmpty(text)) {
                  Span span = new Span();
                  span.getElement().setClassName(result.getColumnId(j));
                  span.getElement().setInnerText(text);
                  item.add(span);
                }
              }
              if (!item.isEmpty()) {
                flow.add(item);
              }
            }
            widget.getElement().setInnerHTML(flow.getElement().getString());
          }
        });
  }
}
