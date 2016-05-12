package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.YearMonth;

class ObjectEarningsForm extends AbstractFormInterceptor {

  ObjectEarningsForm() {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    Integer year = getIntegerValue(COL_EARNINGS_YEAR);
    Integer month = getIntegerValue(COL_EARNINGS_MONTH);

    String message = PayrollHelper.format(YearMonth.of(year, month));
    form.getViewPresenter().getHeader().setMessage(message);

    super.afterRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ObjectEarningsForm();
  }
}
