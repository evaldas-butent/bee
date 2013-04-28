package com.butent.bee.client.modules.crm;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

public class TasksReportsInterceptor extends AbstractFormInterceptor {

  public static enum ReportType {
    TYPE_HOURS, COMPANY_TIMES, USERS_HOURS
  };

  public static final ReportType DEFAULT_REPORT_TYPE = ReportType.TYPE_HOURS;
  private static final String WIDGET_DATE_FROM_NAME = "dateFrom";
  private static final String WIDGET_DATE_TILL_NAME = "dateTill";
  private static final String WIDGET_FILTER_NAME = "Filter";
  private static final String WIDGET_CLEAR_FILTER_NAME = "ClearFilter";
  private static final String WIDGET_REPORT_TABLE_NAME = "reportTable";
  private static final String WIDGET_USER_NAME = "User";
  private static final String WIDGET_COMPANY_NAME = "Company";
  private static final String WIDGET_DURATION_TYPE_NAME = "DurationType";

  private ReportType reportType;

  private static class ClearReportsFilter implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      final FormView form = UiHelper.getForm((Widget) event.getSource());

      InputDate fromDate = (InputDate) form.getWidgetByName(WIDGET_DATE_FROM_NAME);

      if (fromDate != null) {
        fromDate.clearValue();
      }

      InputDate tillDate = (InputDate) form.getWidgetByName(WIDGET_DATE_TILL_NAME);

      if (tillDate != null) {
        tillDate.clearValue();
      }

      MultiSelector userId = (MultiSelector) form.getWidgetByName(WIDGET_USER_NAME);

      if (userId != null) {
        userId.clearValue();
      }

      MultiSelector companyId = (MultiSelector) form.getWidgetByName(WIDGET_COMPANY_NAME);

      if (companyId != null) {
        companyId.clearValue();
      }

      MultiSelector durationTId = (MultiSelector) form.getWidgetByName(WIDGET_DURATION_TYPE_NAME);

      if (durationTId != null) {
        durationTId.clearValue();
      }
    }
  }

  private static class ReportsFilter implements ClickHandler {

    private ReportType reportType;

    private ReportsFilter(ReportType rt) {
      this.reportType = rt;
    }

    @Override
    public void onClick(ClickEvent event) {
      final FormView form = UiHelper.getForm((Widget) event.getSource());
      ParameterList params = null;

      switch (reportType) {
        case TYPE_HOURS:
          params = CrmKeeper.createTaskRequestParameters(CrmConstants.SVC_TASKS_REPORTS_TYPE_HOURS);
          break;
        case COMPANY_TIMES:
          params =
              CrmKeeper.createTaskRequestParameters(CrmConstants.SVC_TASKS_REPORTS_COMPANY_TIMES);
          break;
        case USERS_HOURS:
          params =
              CrmKeeper.createTaskRequestParameters(CrmConstants.SVC_TASKS_REPORTS_USERS_HOURS);
          break;
        default:
          params = CrmKeeper.createTaskRequestParameters(CrmConstants.SVC_TASKS_REPORTS_TYPE_HOURS);
          break;
      }

      InputDate fromDate = (InputDate) form.getWidgetByName(WIDGET_DATE_FROM_NAME);

      if (fromDate != null) {
        if (!BeeUtils.isEmpty(fromDate.getValue())) {

          params.addQueryItem(CrmConstants.VAR_TASK_DURATION_DATE_FROM,
              fromDate.getDate().getTime());
        }
      }

      InputDate tillDate = (InputDate) form.getWidgetByName(WIDGET_DATE_TILL_NAME);

      if (tillDate != null) {
        if (!BeeUtils.isEmpty(tillDate.getValue())) {
          params.addQueryItem(CrmConstants.VAR_TASK_DURATION_DATE_TO, tillDate.getDate().getTime());
        }
      }

      MultiSelector userId = (MultiSelector) form.getWidgetByName(WIDGET_USER_NAME);

      if (userId != null) {
        if (!BeeUtils.isEmpty(userId.getValue())) {
          params.addQueryItem(CrmConstants.VAR_TASK_PUBLISHER, userId.getValue().trim());
        }
      }

      MultiSelector companyId = (MultiSelector) form.getWidgetByName(WIDGET_COMPANY_NAME);

      if (companyId != null) {
        if (!BeeUtils.isEmpty(companyId.getValue())) {
          params.addQueryItem(CrmConstants.VAR_TASK_COMPANY, companyId.getValue().trim());
        }
      }

      MultiSelector durationTId = (MultiSelector) form.getWidgetByName(WIDGET_DURATION_TYPE_NAME);

      if (durationTId != null) {
        if (!BeeUtils.isEmpty(durationTId.getValue())) {
          params.addQueryItem(CrmConstants.VAR_TASK_DURATION_TYPE, durationTId.getValue().trim());
        }
      }

      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

        @Override
        public void onResponse(ResponseObject response) {
          SimpleRowSet durationTimesSet = null;
          Assert.notNull(response);

          if (response.hasResponse(SimpleRowSet.class)) {
            durationTimesSet = SimpleRowSet.restore((String) response.getResponse());
            if (!durationTimesSet.isEmpty()) {
              doReport(form, durationTimesSet);
            }
          } else {
            if (response.hasErrors()) {
              LogUtils.getLogger(TasksReportsInterceptor.class).debug(
                  (Object[]) response.getErrors());
            }
            if (response.hasNotifications()) {
              LogUtils.getLogger(TasksReportsInterceptor.class).debug(
                  (Object[]) response.getNotifications());
            }
          }
        }

        private void doReport(FormView formView, SimpleRowSet rowSet) {
          FlowPanel reportPanel = (FlowPanel) formView.getWidgetByName(WIDGET_REPORT_TABLE_NAME);
          reportPanel.clear();

          int gridRows = rowSet.getNumberOfRows();
          int gridCols = rowSet.getNumberOfColumns();
          Grid g = new Grid(gridRows, gridCols);

          g.setStyleName("bee-HtmlTable bee-crm-taskDuration-display");
          for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols; j++) {
              if (i == 0) {
                g.getCellFormatter().addStyleName(i, j, "bee-crm-taskDuration-colLabel");
              }
              if (i == gridRows - 1) {
                g.getCellFormatter().addStyleName(i, j, "bee-crm-taskDuration-rowTotal");
              }
              g.setText(i, j, rowSet.getValue(i, j));
            }
          }
          reportPanel.add(g);
        }
      });
    }
  }

  public TasksReportsInterceptor() {
    super();
    this.reportType = DEFAULT_REPORT_TYPE;
  }

  public TasksReportsInterceptor(ReportType rt) {
    this();
    this.reportType = rt;
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, WIDGET_FILTER_NAME) && (widget instanceof HasClickHandlers)) {
      ((HasClickHandlers) widget)
          .addClickHandler(new ReportsFilter(this.reportType));
    }

    if (BeeUtils.same(name, WIDGET_DATE_FROM_NAME) && widget instanceof InputDate) {
      InputDate dateFrom = (InputDate) widget;
      DateTime thMonth = new DateTime();
      thMonth.setDom(1);
      dateFrom.setDate(thMonth);
    }
    if (BeeUtils.same(name, WIDGET_DATE_TILL_NAME) && widget instanceof InputDate) {
      InputDate dateFrom = (InputDate) widget;
      DateTime thMonth = new DateTime();
      dateFrom.setDate(thMonth);
    }
    if (BeeUtils.same(name, WIDGET_CLEAR_FILTER_NAME) && (widget instanceof HasClickHandlers)) {
      ((HasClickHandlers) widget)
          .addClickHandler(new ClearReportsFilter());
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TasksReportsInterceptor();
  }

  @Override
  public void onShow(Presenter presenter) {
  }

  @Override
  public void onStart(FormView form) {
    Widget widget = form.getWidgetByName(WIDGET_FILTER_NAME);
    HeaderView header = form.getViewPresenter().getHeader();
    String reportCaption;

    switch (reportType) {
      case TYPE_HOURS:
        reportCaption = Global.CONSTANTS.hoursByTypes();
        break;
      case COMPANY_TIMES:
        reportCaption = Global.CONSTANTS.hoursByCompanies();
        break;
      case USERS_HOURS:
        reportCaption = Global.CONSTANTS.hoursByUsers();
        break;
      default:
        reportCaption = Global.CONSTANTS.hoursByTypes();
        break;
    }

    header.setCaption(reportCaption);

    if (widget instanceof BeeButton) {
      BeeButton btn = (BeeButton) widget;
      btn.click();
    }
  }
}
