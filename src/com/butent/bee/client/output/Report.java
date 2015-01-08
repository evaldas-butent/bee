package com.butent.bee.client.output;

import com.butent.bee.client.Callback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.classifiers.CompanyTypeReport;
import com.butent.bee.client.modules.classifiers.CompanyUsageReport;
import com.butent.bee.client.modules.trade.acts.TradeActItemsByCompanyReport;
import com.butent.bee.client.modules.trade.acts.TradeActServicesReport;
import com.butent.bee.client.modules.trade.acts.TradeActStockReport;
import com.butent.bee.client.modules.trade.acts.TradeActTransferReport;
import com.butent.bee.client.modules.transport.AssessmentQuantityReport;
import com.butent.bee.client.modules.transport.AssessmentTurnoverReport;
import com.butent.bee.client.modules.transport.TransportTripProfitReport;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.ui.HasWidgetSupplier;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Report implements HasWidgetSupplier {
  COMPANY_TYPES("CompanyTypes", "CompanyRelationTypeReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new CompanyTypeReport();
    }
  },
  COMPANY_USAGE("CompanyUsage", "CompanyUsageReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new CompanyUsageReport();
    }
  },

  ASSESSMENT_QUANTITY("AssessmentQuantity", "AssessmentQuantityReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new AssessmentQuantityReport();
    }
  },
  ASSESSMENT_TURNOVER("AssessmentTurnover", "AssessmentTurnoverReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new AssessmentTurnoverReport();
    }
  },

  TRADE_ACT_ITEMS_BY_COMPANY("TradeActItemsByCompany", "TradeActItemsByCompanyReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActItemsByCompanyReport();
    }
  },
  TRADE_ACT_STOCK("TradeActStock", "TradeActStockReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActStockReport();
    }
  },
  TRADE_ACT_SERVICES("TradeActServices", "TradeActServicesReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActServicesReport();
    }
  },
  TRADE_ACT_TRANSFER("TradeActTransfer", "TradeActTransferReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActTransferReport();
    }
  },

  TRANSPORT_TRIP_PROFIT("TransportTripProfit", "TransportTripProfitReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TransportTripProfitReport();
    }

    @Override
    public List<ReportItem> getItems() {
      LocalizableConstants loc = Localized.getConstants();

      return Arrays.asList(
          new ReportTextItem(TransportConstants.COL_TRIP,
              Data.getColumnLabel(TransportConstants.TBL_TRIP_COSTS, TransportConstants.COL_TRIP)),
          new ReportTextItem(TransportConstants.COL_TRIP_NO,
              Data.getColumnLabel(TransportConstants.TBL_TRIPS, TransportConstants.COL_TRIP_NO)),
          new ReportDateItem(TransportConstants.COL_TRIP_DATE_FROM, loc.dateFrom()),
          new ReportDateTimeItem(TransportConstants.COL_TRIP_DATE_TO, loc.dateTo()),
          new ReportTextItem(TransportConstants.COL_VEHICLE,
              Data.getColumnLabel(TransportConstants.TBL_TRIPS, TransportConstants.COL_VEHICLE)),
          new ReportTextItem(TransportConstants.COL_TRAILER,
              Data.getColumnLabel(TransportConstants.TBL_TRIPS, TransportConstants.COL_TRAILER)),
          new ReportNumericItem("Kilometers", null, 0),
          new ReportNumericItem("FuelCosts", null, 2),
          new ReportNumericItem("DailyCosts", null, 2),
          new ReportNumericItem("RoadCosts", null, 2),
          new ReportNumericItem("OtherCosts", null, 2),
          new ReportNumericItem("Incomes", null, 2));
    }
  };

  private static BeeLogger logger = LogUtils.getLogger(Report.class);

  public static void open(String reportName) {
    Report report = parse(reportName);
    if (report != null) {
      report.open();
    }
  }

  public static void open(String reportName, final ViewCallback callback) {
    Assert.notNull(callback);

    final Report report = parse(reportName);
    if (report != null) {
      FormFactory.getFormDescription(report.getFormName(), new Callback<FormDescription>() {
        @Override
        public void onSuccess(FormDescription result) {
          FormFactory.openForm(result, report.getInterceptor(),
              ViewFactory.getPresenterCallback(callback));
        }
      });
    }
  }

  public static Report parse(String input) {
    for (Report report : values()) {
      if (BeeUtils.same(report.reportName, input)) {
        return report;
      }
    }

    logger.severe("report not recognized:", input);
    return null;
  }

  private final String reportName;
  private final String formName;

  private Report(String reportName, String formName) {
    this.reportName = reportName;
    this.formName = formName;
  }

  public String getFormName() {
    return formName;
  }

  public List<ReportItem> getItems() {
    return new ArrayList<>();
  }

  public String getReportName() {
    return reportName;
  }

  @Override
  public String getSupplierKey() {
    return ViewFactory.SupplierKind.REPORT.getKey(reportName);
  }

  public void open() {
    FormFactory.openForm(formName, getInterceptor());
  }

  public void open(ReportParameters parameters) {
    ReportInterceptor interceptor = getInterceptor();
    interceptor.setInitialParameters(parameters);

    FormFactory.openForm(formName, interceptor);
  }

  protected abstract ReportInterceptor getInterceptor();
}
