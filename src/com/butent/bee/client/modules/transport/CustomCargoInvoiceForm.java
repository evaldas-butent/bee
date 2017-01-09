package com.butent.bee.client.modules.transport;

import com.butent.bee.client.modules.trade.InvoiceForm;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class CustomCargoInvoiceForm extends InvoiceForm {

  public CustomCargoInvoiceForm() {
    super(null);
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }

  @Override
  protected void print(String report) {
    getReportParameters(parameters -> {
      String[] arr = BeeUtils.split(report, BeeConst.CHAR_ASTERISK);
      String rep = arr[1];
      String type = ArrayUtils.getQuietly(arr, 0);

      parameters.put(AdministrationConstants.COL_FILE_TYPE, type);
      getReportData(data -> ReportUtils.showReport(rep, getReportCallback(), parameters, data));
    });
  }
}
