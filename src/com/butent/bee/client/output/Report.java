package com.butent.bee.client.output;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

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
import com.butent.bee.client.output.ReportItem.Function;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.client.view.form.interceptor.ExtendedReportInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.modules.projects.ProjectPriority;
import com.butent.bee.shared.modules.projects.ProjectStatus;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
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
        map.put(PROP_ROWS, items.get(item).create());
      }
      map.put(PROP_ROW_GROUP, items.get(TransportConstants.COL_VEHICLE).create());

      for (String item : new String[] {"Kilometers", "FuelCosts", "Incomes"}) {
        map.put(PROP_COLUMNS, items.get(item).create().enableCalculation());
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
  },

  PROJECT_REPORT(ModuleAndSub.of(Module.PROJECTS), ProjectConstants.SVC_PROJECT_REPORT) {

    @Override
    public Multimap<String, ReportItem> getDefaults() {
      Map<String, ReportItem> items = new HashMap<>();

      for (ReportItem item : getItems()) {
        items.put(item.getName(), item);
      }
      Multimap<String, ReportItem> map = LinkedListMultimap.create();

      for (String item : new String[] {
          ProjectConstants.COL_PROJECT_NAME,
          ClassifierConstants.ALS_COMPANY_NAME,
          ProjectConstants.COL_PROJECT_OWNER,
          ProjectConstants.COL_PROJECT_PRIORITY,
          ProjectConstants.COL_PROJECT_START_DATE,
          ProjectConstants.COL_PROJECT_END_DATE,
          ProjectConstants.ALS_PROJECT_OVERDUE,
          ProjectConstants.COL_PROGRESS,
          ProjectConstants.COL_EXPECTED_DURATION,
          ProjectConstants.COL_PROJECT_TIME_UNIT
      }) {

        map.put(PROP_ROWS, items.get(item).create());
      }

      map.put(Report.PROP_ROW_GROUP, items.get(ProjectConstants.COL_PROJECT_NAME).create());

      return map;
    }

    @Override
    public List<ReportItem> getItems() {
      LocalizableConstants loc = Localized.getConstants();

      return Arrays.asList(
          new ReportTextItem(ProjectConstants.COL_PROJECT_NAME, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_NAME)),
          new ReportTextItem(ClassifierConstants.ALS_COMPANY_NAME, loc.client()),
          new ReportTextItem(ProjectConstants.COL_PROJECT_OWNER, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_OWNER)),
          new ReportNumericItem(ProjectConstants.COL_PROJECT, loc.project(), 0),
          new ReportEnumItem<>(ProjectConstants.COL_PROJECT_STATUS, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_STATUS),
              ProjectStatus.class),
          new ReportTextItem(ProjectConstants.COL_PROJECT_TYPE, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_TYPE)),
          new ReportEnumItem<>(ProjectConstants.COL_PROJECT_PRIORITY, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_PRIORITY),
              ProjectPriority.class),
          new ReportDateItem(ProjectConstants.COL_PROJECT_START_DATE, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_START_DATE)),
          new ReportDateItem(ProjectConstants.COL_PROJECT_END_DATE, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_END_DATE)),
          new ReportNumericItem(ProjectConstants.ALS_PROJECT_OVERDUE, BeeUtils.join(
              BeeConst.STRING_COMMA, loc.prjOverdue(), loc.unitDaysShort()), 0),
          new ReportNumericItem(ProjectConstants.COL_PROGRESS, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROGRESS), 2),

          new ReportNumericItem(ProjectConstants.COL_EXPECTED_DURATION, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_EXPECTED_DURATION), 2),

          new ReportTextItem(ProjectConstants.COL_PROJECT_TIME_UNIT, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_TIME_UNIT)),

          new ReportNumericItem(TaskConstants.COL_ACTUAL_DURATION, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_ACTUAL_TASKS_DURATION), 2),

          new ReportNumericItem(ProjectConstants.ALS_ACTUAL_TIME_DIFFERENCE, loc.timeDifference(),
              2),

          new ReportNumericItem(ProjectConstants.COL_PROJECT_PRICE, Data.getColumnLabel(
              ProjectConstants.VIEW_PROJECTS, ProjectConstants.COL_PROJECT_PRICE), 2),

          new ReportNumericItem(TaskConstants.COL_ACTUAL_EXPENSES, Data.getColumnLabel(
              TaskConstants.VIEW_TASKS, TaskConstants.COL_ACTUAL_EXPENSES), 2),

          new ReportNumericItem(TaskConstants.COL_TASK, loc.crmTasks(), 0),
          new ReportEnumItem<>(ProjectConstants.ALS_TASK_STATUS, Data.getColumnLabel(
              TaskConstants.VIEW_TASKS, TaskConstants.COL_STATUS),
              ProjectPriority.class)
          );
    }
  },

  INCOME_INVOICES_REPORT(ModuleAndSub.of(Module.TRANSPORT),
      TransportConstants.SVC_INCOME_INVOICES_REPORT) {

    @Override
    public Multimap<String, ReportItem> getDefaults() {
      Map<String, ReportItem> items = new HashMap<>();

      for (ReportItem item : getItems()) {
        items.put(item.getName(), item);
      }
      Multimap<String, ReportItem> map = LinkedListMultimap.create();

      for (String item : new String[] {TransportConstants.COL_ASSESSMENT,
          TransportConstants.COL_SERVICE_NAME, TransportConstants.COL_ORDER_MANAGER,
          TradeConstants.COL_TRADE_INVOICE_NO, TradeConstants.COL_TRADE_CUSTOMER,
          TradeConstants.COL_SALE + TransportConstants.COL_ORDER_MANAGER}) {
        map.put("ROWS", items.get(item).create());
      }
      map.put("ROW_GROUP", items.get(AdministrationConstants.COL_DEPARTMENT_NAME).create());

      for (String item : new String[] {VAR_EXPENSE + COL_SERVICE_NAME,
          VAR_EXPENSE + COL_TRADE_INVOICE_NO}) {
        map.put("COLUMNS", items.get(item).create().enableCalculation().setFunction(Function.LIST));
      }
      for (String item : new String[] {TransportConstants.VAR_INCOME,
          TransportConstants.VAR_EXPENSE, "Profit"}) {
        map.put("COLUMNS", items.get(item).create().enableCalculation());
      }
      return map;
    }

    @Override
    public List<ReportItem> getItems() {
      LocalizableConstants loc = Localized.getConstants();

      return Arrays.asList(
          new ReportTextItem(TransportConstants.COL_ASSESSMENT, "Užsakymo Nr."),
          new ReportTextItem(AdministrationConstants.COL_DEPARTMENT_NAME,
              Data.getColumnLabel(AdministrationConstants.TBL_DEPARTMENTS,
                  AdministrationConstants.COL_DEPARTMENT_NAME)),
          new ReportTextItem(TransportConstants.COL_SERVICE_NAME,
              Data.getColumnLabel(TransportConstants.TBL_SERVICES, "Name")),
          new ReportDateTimeItem(TradeConstants.COL_TRADE_DATE, loc.date()),
          new ReportTextItem(TradeConstants.COL_SALE + TransportConstants.COL_ORDER_MANAGER,
              "Sąskaitą išrašė"),
          new ReportTextItem(TransportConstants.COL_ORDER_MANAGER, loc.manager()),
          new ReportTextItem(TradeConstants.COL_TRADE_INVOICE_NO,
              Data.getColumnLabel(TradeConstants.TBL_SALES, TradeConstants.COL_TRADE_INVOICE_NO)),
          new ReportTextItem(TradeConstants.COL_TRADE_CUSTOMER, loc.customer()),
          new ReportTextItem(VAR_EXPENSE + COL_SERVICE_NAME, "Sąnaudų paslauga"),
          new ReportTextItem(VAR_EXPENSE + COL_TRADE_INVOICE_NO, "Sąnaudų sąskaitos Nr."),
          new ReportNumericItem(TransportConstants.VAR_INCOME, loc.income(), 2),
          new ReportNumericItem(TransportConstants.VAR_EXPENSE, "Sąnaudos", 2),
          new ReportNumericItem("Profit", loc.profit(), 2));
    }
  };

  public static final String PROP_ROWS = "ROWS";
  public static final String PROP_ROW_GROUP = "ROW_GROUP";
  public static final String PROP_COLUMNS = "COLUMNS";
  public static final String PROP_COLUMN_GROUP = "COLUMN_GROUP";

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

  private final ModuleAndSub moduleAndSub;
  private final String reportName;
  private final String formName;

  private Report(ModuleAndSub module, String reportName) {
    this(module, reportName, "ExtendedReport");
  }

  private Report(ModuleAndSub moduleAndSub, String reportName, String formName) {
    this.moduleAndSub = Assert.notNull(moduleAndSub);
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
    return moduleAndSub;
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
