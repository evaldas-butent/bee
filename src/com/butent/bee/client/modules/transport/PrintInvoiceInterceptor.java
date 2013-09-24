package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class PrintInvoiceInterceptor extends AbstractFormInterceptor {

  Map<String, Widget> companies = Maps.newHashMap();
  HtmlTable invoiceDetails;
  List<Widget> totals = Lists.newArrayList();

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
      Long id = form.getActiveRow().getLong(form.getDataIndex(name));

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      CommonsUtils.getCompanyInfo(id, companies.get(name));
    }
    if (invoiceDetails != null) {
      TradeUtils.getDocumentItems(getFormView().getViewName(), row.getId(), invoiceDetails);
    }
    for (Widget total : totals) {
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
