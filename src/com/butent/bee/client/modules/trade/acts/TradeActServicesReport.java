package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportParameters;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;

public class TradeActServicesReport extends ReportInterceptor {

  public TradeActServicesReport() {
    super();
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeActServicesReport();
  }

  @Override
  protected void clearFilter() {
  }

  @Override
  protected void doReport() {
  }

  @Override
  protected String getBookmarkLabel() {
    return null;
  }

  @Override
  protected Report getReport() {
    return Report.TRADE_ACT_SERVICES;
  }

  @Override
  protected ReportParameters getReportParameters() {
    ReportParameters parameters = new ReportParameters();
    return parameters;
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    return false;
  }
}
