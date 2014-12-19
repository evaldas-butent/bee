package com.butent.bee.client.modules.projects;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.timeboard.TimeBoard;
import com.butent.bee.client.timeboard.TimeBoardHelper;
import com.butent.bee.client.timeboard.TimeBoardRowLayout;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

final class ProjectScheduleChart extends TimeBoard {

  public static final String SUPPLIER_KEY = "project_schedule_chart";

  public static void open(final Flow widget, long projectId) {
    ParameterList params = ProjectsKeeper.createSvcArgs(SVC_GET_PROJECT_CHART_DATA);
    params.addDataItem(VAR_PROJECT, BeeUtils.toString(projectId));

    BeeKeeper.getRpc().makePostRequest(params, getResponseCallback(getViewCallback(widget)));
  }

  private final class ChartItem implements HasDateRange {

    private JustDate start;
    private JustDate end;
    private long stageId;
    private String stageName;

    @Override
    public Range<JustDate> getRange() {
      return TimeBoardHelper.getActivity(start, end);
    }

    public ChartItem(String viewName, long stageId, String stageName, JustDate prjStart,
        JustDate prjEnd, JustDate stageStart, JustDate stageEnd, String color) {

      this();

      switch (viewName) {
        case VIEW_PROJECT_STAGES:
          this.start = stageStart;
          this.end = stageEnd;
          this.stageId = stageId;
          this.stageName = stageName;
          break;

        default:
          this.start = prjStart;
          this.end = prjEnd;
          this.stageId = stageId;
          this.stageName = stageName;
          break;
      }

      LogUtils.getRootLogger().debug("Chart data", viewName, stageId, stageName, color);
    }

    @SuppressWarnings("unused")
    public long getStageId() {
      return stageId;
    }

    public String getStageName() {
      return stageName;
    }

    private ChartItem() {
    }
  }

  private final List<ChartItem> chartItems = Lists.newArrayList();

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
    return chartItems;
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
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());
    setDayColumnWidth(25);
  }

  @Override
  protected void refresh() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void renderContent(ComplexPanel panel) {

    List<TimeBoardRowLayout> boardLayout = createLayout();

    int rc = TimeBoardRowLayout.countRows(boardLayout, 1);

    initContent(panel, rc);

    int rowIndex = 0;
    int calendarWidth = getCalendarWidth();

    for (TimeBoardRowLayout layout : boardLayout) {
      int chartRowIndex = layout.getDataIndex();

      int top = rowIndex * getRowHeight();
      int size = layout.getSize(1);
      int lastRow = rowIndex + size - 1;

      if (rowIndex > 0) {
        TimeBoardHelper.addRowSeparator(panel, top, 0, getChartLeft() + calendarWidth);
      }

      IdentifiableWidget chartRowWidget = createChartRowWidget(chartItems.get(chartRowIndex));
      addChartRowWidget(panel, chartRowWidget, rowIndex, lastRow);

      rowIndex += size;
    }

  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
    Mover mover = TimeBoardHelper.createHorizontalMover();
    StyleUtils.setLeft(mover, getChartLeft() - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(mover, height);

    panel.add(mover);
  }

  @Override
  protected boolean setData(ResponseObject response) {
    if (response.isEmpty()) {
      return false;
    }

    if (!response.hasResponse(SimpleRowSet.class)) {
      Assert.notImplemented();
    }

    SimpleRowSet rs = SimpleRowSet.restore(response.getResponseAsString());

    int idxViewName = rs.getColumnIndex(ALS_VIEW_NAME);
    int idxStage = rs.getColumnIndex(COL_PROJECT_STAGE);
    int idxStageName = rs.getColumnIndex(ALS_STAGE_NAME);
    int idxProjectStart = rs.getColumnIndex(ALS_PROJECT_START_DATE);
    int idxProjectEnd = rs.getColumnIndex(ALS_PROJECT_END_DATE);
    int idxStageStart = rs.getColumnIndex(ALS_STAGE_START);
    int idxStageEnd = rs.getColumnIndex(ALS_STAGE_END);
    int idxColor = rs.getColumnIndex(ALS_CHART_FLOW_COLOR);

    for (String[] row : rs.getRows()) {
      long stageId = BeeUtils.toLong(row[idxStage]);
      JustDate prjStart = new JustDate(BeeUtils.toLong(row[idxProjectStart]));
      JustDate prjEnd = new JustDate(BeeUtils.toLong(row[idxProjectEnd]));
      JustDate stageStart = new JustDate(BeeUtils.toLong(row[idxStageStart]));
      JustDate stageEnd = new JustDate(BeeUtils.toLong(row[idxStageEnd]));
      ChartItem ci = new ChartItem(row[idxViewName], stageId, row[idxStageName], prjStart,
          prjEnd, stageStart, stageEnd, row[idxColor]);

      chartItems.add(ci);
    }

    updateMaxRange();

    return true;
  }



  private static IdentifiableWidget createChartRowWidget(ChartItem item) {
    Flow panel = new Flow(); // TODO: ADD style name

    CustomDiv label = new CustomDiv(); // TODO : Add style name
    label.setText(item.getStageName());
    panel.add(label);

    return panel;
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

  private void addChartRowWidget(HasWidgets panel, IdentifiableWidget widget,
      int firstRow, int lastRow) {
    Rectangle rectangle = TimeBoardHelper.getRectangle(0, 150, firstRow, lastRow,
        getRowHeight()); // TODO: default stage width

    Edges margins = new Edges();
    margins.setRight(TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    TimeBoardHelper.apply(widget.asWidget(), rectangle, margins);

    panel.add(widget.asWidget());
  }

  private List<TimeBoardRowLayout> createLayout() {
    List<TimeBoardRowLayout> result = new ArrayList<>();

    Range<JustDate> range = getVisibleRange();
    List<HasDateRange> items;

    for (int i = 0; i < chartItems.size(); i++) {
      TimeBoardRowLayout layout = new TimeBoardRowLayout(i);
      items = TimeBoardHelper.getActiveItems(chartItems, range);
      layout.addItems(Long.valueOf(chartItems.get(i).stageId), items, range);

      result.add(layout);
    }

    return result;
  }

  private ProjectScheduleChart() {

  }
}
