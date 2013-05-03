package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class PrintCargoInvoiceForm extends AbstractFormInterceptor {

  List<HasWidgets> totals = Lists.newArrayList();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.startsSame(name, "TotalInWords") && widget instanceof HasWidgets) {
      totals.add((HasWidgets) widget);
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    Widget widget = form.getWidgetByName("SupplierInfo");
    if (widget instanceof HasWidgets) {
      Long supplier = row.getLong(form.getDataIndex("Supplier"));

      if (!DataUtils.isId(supplier)) {
        supplier = BeeKeeper.getUser().getUserData().getCompany();
      }
      CommonsUtils.getCompanyInfo(supplier, (HasWidgets) widget);
    }
    widget = form.getWidgetByName("CustomerInfo");
    if (widget instanceof HasWidgets) {
      CommonsUtils.getCompanyInfo(row.getLong(form.getDataIndex("Customer")), (HasWidgets) widget);
    }
    widget = form.getWidgetByName("InvoiceDetails");
    if (widget instanceof HasWidgets) {
      TradeUtils.getSaleItems(row.getId(), (HasWidgets) widget, null);
    }
    for (HasWidgets total : totals) {
      TradeUtils.getTotalInWords(row.getDouble(form.getDataIndex(COL_AMOUNT)),
          row.getString(form.getDataIndex("CurrencyName")),
          row.getString(form.getDataIndex("MinorName")), total,
          total instanceof HasOptions ? ((HasOptions) total).getOptions() : null);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintCargoInvoiceForm();
  }
}
