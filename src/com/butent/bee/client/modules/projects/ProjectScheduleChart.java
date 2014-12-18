package com.butent.bee.client.modules.projects;

import com.google.gwt.user.client.ui.ComplexPanel;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.timeboard.TimeBoard;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

final class ProjectScheduleChart extends TimeBoard {

  public static final String SUPPLIER_KEY = "project_schedule_chart";

  public static void open(final Flow widget, long projectId) {
    ParameterList params = ProjectsKeeper.createSvcArgs(SVC_GET_PROJECT_CHART_DATA);
    params.addDataItem(VAR_PROJECT, BeeUtils.toString(projectId));

    BeeKeeper.getRpc().makePostRequest(params, getResponseCallback(getViewCallback(widget)));
  }

  @Override
  public String getCaption() {
    return Localized.getConstants().prjSchedule();
  }

  @Override
  public String getSupplierKey() {
    return SUPPLIER_KEY;
  }

  @Override
  protected void editSettings() {
  }

  @Override
  protected Collection<? extends HasDateRange> getChartItems() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.PRINT);
  }

  @Override
  protected String getFooterHeightColumnName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getHeaderHeightColumnName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getRowHeightColumnName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getStripOpacityColumnName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void handleAction(Action action) {
    if (action.equals(Action.CLOSE)) {
      return;
    }
    super.handleAction(action);
  }

  @Override
  protected boolean isDataEventRelevant(DataEvent event) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected void prepareChart(Size canvasSize) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void refresh() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  protected boolean setData(ResponseObject response) {
    // TODO Auto-generated method stub
    return true;
  }

  private static ResponseCallback getResponseCallback(final ViewCallback viewCallback) {
    return new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        ProjectScheduleChart chart = new ProjectScheduleChart();
        chart.onCreate(response, viewCallback);
      }
    };
  }

  private static ViewCallback getViewCallback(final Flow widget) {
    return new ViewCallback() {

      @Override
      public void onSuccess(View result) {
        widget.add(result);
      }
    };
  }

  private ProjectScheduleChart() {

  }
}
