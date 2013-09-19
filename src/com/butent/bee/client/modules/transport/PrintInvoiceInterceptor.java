package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class PrintInvoiceInterceptor extends AbstractFormInterceptor {

  List<Element> totals = Lists.newArrayList();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.startsSame(name, "TotalInWords")) {
      totals.add(widget.getElement());
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    for (String company : new String[] {COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER}) {
      Widget widget = form.getWidgetByName(company);

      if (widget instanceof HasWidgets) {
        Long companyId = row.getLong(form.getDataIndex(company));

        if (!DataUtils.isId(companyId)) {
          companyId = BeeKeeper.getUser().getUserData().getCompany();
        }
        if (BeeUtils.same(company, COL_SALE_PAYER)) {
          final Widget payerCaption = form.getWidgetByName("PayerCaption");

          if (payerCaption != null) {
            StyleUtils.setVisible(payerCaption, DataUtils.isId(companyId));
          }
        }
        CommonsUtils.getCompanyInfo(companyId, (HasWidgets) widget);
      }
    }
    Widget widget = form.getWidgetByName("InvoiceDetails");

    if (widget instanceof HtmlTable) {
      TradeUtils.getDocumentItems(getFormView().getViewName(), row.getId(), (HtmlTable) widget);
    }
    for (Element total : totals) {
      TradeUtils.getTotalInWords(row.getDouble(form.getDataIndex(COL_TRADE_AMOUNT)),
          row.getString(form.getDataIndex("CurrencyName")),
          row.getString(form.getDataIndex("MinorName")), total);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintInvoiceInterceptor();
  }
}
