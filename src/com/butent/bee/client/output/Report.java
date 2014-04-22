package com.butent.bee.client.output;

import com.butent.bee.client.modules.classifiers.CompanyTypeReport;
import com.butent.bee.client.modules.classifiers.CompanyUsageReport;
import com.butent.bee.client.modules.transport.AssessmentQuantityReport;
import com.butent.bee.client.modules.transport.AssessmentTurnoverReport;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

public enum Report {
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
  };
  
  private static BeeLogger logger = LogUtils.getLogger(Report.class);
  
  public static void open(String reportName) {
    Report report = parse(reportName);
    if (report != null) {
      report.open();
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
  
  private String reportName;
  private String formName;

  private Report(String reportName, String formName) {
    this.reportName = reportName;
    this.formName = formName;
  }

  public String getFormName() {
    return formName;
  }

  public void setFormName(String formName) {
    this.formName = formName;
  }

  public String getReportName() {
    return reportName;
  }

  public void open() {
    FormFactory.openForm(formName, getInterceptor());
  }

  public void open(ReportParameters parameters) {
    ReportInterceptor interceptor = getInterceptor();
    interceptor.setInitialParameters(parameters);

    FormFactory.openForm(formName, interceptor);
  }
  
  public void setReportName(String reportName) {
    this.reportName = reportName;
  }

  protected abstract ReportInterceptor getInterceptor();
}
