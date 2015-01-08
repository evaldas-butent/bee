package com.butent.bee.client.output;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

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
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.client.view.form.interceptor.ExtendedReportInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.ui.HasWidgetSupplier;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Report implements HasWidgetSupplier {
  COMPANY_TYPES(ModuleAndSub.of(Module.CLASSIFIERS), "CompanyTypes", "CompanyRelationTypeReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new CompanyTypeReport();
    }
  },
  COMPANY_USAGE(ModuleAndSub.of(Module.CLASSIFIERS), "CompanyUsage", "CompanyUsageReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new CompanyUsageReport();
    }
  },

  ASSESSMENT_QUANTITY(ModuleAndSub.of(Module.TRANSPORT), "AssessmentQuantity",
      "AssessmentQuantityReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new AssessmentQuantityReport();
    }
  },
  ASSESSMENT_TURNOVER(ModuleAndSub.of(Module.TRANSPORT), "AssessmentTurnover",
      "AssessmentTurnoverReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new AssessmentTurnoverReport();
    }
  },

  TRADE_ACT_ITEMS_BY_COMPANY(ModuleAndSub.of(Module.TRADE, SubModule.ACTS),
      "TradeActItemsByCompany", "TradeActItemsByCompanyReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActItemsByCompanyReport();
    }
  },
  TRADE_ACT_STOCK(ModuleAndSub.of(Module.TRADE, SubModule.ACTS), "TradeActStock",
      "TradeActStockReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActStockReport();
    }
  },
  TRADE_ACT_SERVICES(ModuleAndSub.of(Module.TRADE, SubModule.ACTS), "TradeActServices",
      "TradeActServicesReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActServicesReport();
    }
  },
  TRADE_ACT_TRANSFER(ModuleAndSub.of(Module.TRADE, SubModule.ACTS), "TradeActTransfer",
      "TradeActTransferReport") {
    @Override
    protected ReportInterceptor getInterceptor() {
      return new TradeActTransferReport();
    }
  },

  TRANSPORT_TRIP_PROFIT(ModuleAndSub.of(Module.TRANSPORT),
      TransportConstants.SVC_TRIP_PROFIT_REPORT) {

    @Override
    public Multimap<String, ReportItem> getDefaults() {
      Map<String, ReportItem> items = new HashMap<>();

      for (ReportItem item : getItems()) {
        items.put(item.getName(), item);
      }
      Multimap<String, ReportItem> map = LinkedListMultimap.create();

      for (String item : new String[] {TransportConstants.COL_TRIP, TransportConstants.COL_TRIP_NO,
          TransportConstants.COL_TRIP_DATE_FROM, TransportConstants.COL_TRIP_DATE_TO,
          TransportConstants.COL_TRAILER}) {
        map.put("ROWS", items.get(item).create());
      }
      map.put("ROW_GROUP", items.get(TransportConstants.COL_VEHICLE).create());

      for (String item : new String[] {"Kilometers", "FuelCosts", "Incomes"}) {
        map.put("COLUMNS", items.get(item).create().enableCalculation());
      }
      return map;
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
          new ReportNumericItem("Kilometers", "Kilometrai", 0),
          new ReportNumericItem("FuelCosts", "Kuro išl.", 2),
          new ReportNumericItem("DailyCosts", "Dienpinigių išl.", 2),
          new ReportNumericItem("RoadCosts", "Kelių išl.", 2),
          new ReportNumericItem("OtherCosts", "Kitos išl.", 2),
          new ReportNumericItem("Incomes", "Pajamos", 2));
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

  private final ModuleAndSub module;
  private final String reportName;
  private final String formName;

  private Report(ModuleAndSub module, String reportName) {
    this(module, reportName, "ExtendedReport");
  }

  private Report(ModuleAndSub module, String reportName, String formName) {
    this.module = Assert.notNull(module);
    this.reportName = reportName;
    this.formName = formName;
  }

  public String getFormName() {
    return formName;
  }

  public Multimap<String, ReportItem> getDefaults() {
    return null;
  }

  public List<ReportItem> getItems() {
    return new ArrayList<>();
  }

  public ModuleAndSub getModuleAndSub() {
    return module;
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

  protected ReportInterceptor getInterceptor() {
    return new ExtendedReportInterceptor(this);
  }
}
