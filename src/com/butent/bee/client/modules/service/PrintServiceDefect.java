package com.butent.bee.client.modules.service;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;

public class PrintServiceDefect extends AbstractFormInterceptor {

  private static void getCompanyInfo(FormView form, IsRow row, String source) {
    Long company = row.getLong(form.getDataIndex(source));
    Widget widget = form.getWidgetByName(source);

    if (DataUtils.isId(company) && widget != null) {
      ClassifierUtils.getCompanyInfo(company, widget);
    }
  }

  PrintServiceDefect() {
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (form != null && row != null) {
      getCompanyInfo(form, row, COL_DEFECT_SUPPLIER);
      getCompanyInfo(form, row, COL_SERVICE_CUSTOMER);
    }

    // TradeUtils.getDocumentItems(getViewName(), row.getId(), invoiceDetails);
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintServiceDefect();
  }
}
