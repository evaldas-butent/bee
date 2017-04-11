package com.butent.bee.client.modules.trade.reports;

import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportParameters;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;

public class TradeMovementOfGoodsReport extends ReportInterceptor {

  public TradeMovementOfGoodsReport() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeMovementOfGoodsReport();
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
    return Report.TRADE_MOVEMENT_OF_GOODS;
  }

  @Override
  protected ReportParameters getReportParameters() {
    return null;
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    return true;
  }
}
