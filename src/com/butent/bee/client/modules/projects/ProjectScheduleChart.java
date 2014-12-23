package com.butent.bee.client.modules.projects;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ProjectScheduleChart extends TimeBoard {

  public static final String SUPPLIER_KEY = "project_schedule_chart";

  public static void open(final Flow widget, long projectId) {
    ParameterList params = ProjectsKeeper.createSvcArgs(SVC_GET_PROJECT_CHART_DATA);
    params.addDataItem(VAR_PROJECT, BeeUtils.toString(projectId));

    BeeKeeper.getRpc().makePostRequest(params, getResponseCallback(getViewCallback(widget)));
  }

  private static final String STYLE_PREFIX = ProjectsKeeper.STYLE_PREFIX + "chart-";
  private static final String STYLE_STAGE_ROW = STYLE_PREFIX + "stage-row";
  private static final String STYLE_STAGE_FLOW = STYLE_PREFIX + "stage-flow";
  private static final String STYLE_STAGE_LABEL = STYLE_PREFIX + "stage-row-label";
  // private static final String STYLE_PROJECT_LABEL = STYLE_PREFIX + "project-row-label";

  private static final int DEFAULT_CHART_LEFT = 150;

  private final class ChartItem implements HasDateRange {

    private JustDate start;
    private JustDate end;
    private Long stageId;
    private String caption;
    private String color;
    private String viewName;

    @Override
    public Range<JustDate> getRange() {
      return TimeBoardHelper.getActivity(start, end);
    }

    public ChartItem(String viewName, Long stageId, String caption, JustDate start,
        JustDate end, String color) {

      this();
      this.viewName = viewName;
      this.start = start;
      this.end = end;
      this.caption = caption;
      this.color = color;
      this.stageId = stageId;

      LogUtils.getRootLogger().debug("Chart data", viewName, stageId, caption, color, this.start,
          this.end);
    }

    public Long getStageId() {
      return stageId;
    }

    public String getCaption() {
      return caption;
    }

    public String getColor() {
      return color;
    }

    public String getViewName() {
      return viewName;
    }

    private ChartItem() {
    }
  }

  private final List<ChartItem> chartItems = Lists.newArrayList();
  private final Set<String> stagePanels = new HashSet<>();
  private final Map<Integer, Long> stagesByRow = new HashMap<>();

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
    setChartLeft(DEFAULT_CHART_LEFT);
    setDayColumnWidth(25); // TODO:
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());

  }

  @Override
  protected void refresh() {
    render(true);
    // TODO:
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    stagePanels.clear();
    stagesByRow.clear();

    List<TimeBoardRowLayout> boardLayout = createLayout();

    int rc = TimeBoardRowLayout.countRows(boardLayout, 1);

    initContent(panel, rc);

    if (boardLayout.isEmpty()) {
      return;
    }

    int rowIndex = 0;
    int calendarWidth = getCalendarWidth();
    Edges margins = new Edges();
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    IdentifiableWidget stageWidget = null;
    IdentifiableWidget flowWidget = null;
    int stageStartRow = 0;
    int flowStartRow = 0;

    Long lastStage = null;
    String lastFlow = null;

    for (TimeBoardRowLayout layout : boardLayout) {
      int chartRowIndex = layout.getDataIndex();
      ChartItem dataItem = chartItems.get(chartRowIndex);

      int top = rowIndex * getRowHeight();

      if (rowIndex == 0) {
        stageWidget = createChartRowWidget(dataItem);
        stageStartRow = rowIndex;

        lastStage = dataItem.getStageId();

        // second level widgets
        flowWidget = createChartRowWidget(dataItem);
        flowStartRow = rowIndex;
        lastFlow = BeeUtils.joinItems(dataItem.getViewName(), rowIndex);

      } else {
        boolean stageChanged = !Objects.equals(lastStage, dataItem.getStageId());
        // second level changed
        boolean flowChanged = stageChanged || !Objects.equals(lastFlow,
            BeeUtils.joinItems(dataItem.getViewName(), rowIndex));

        if (stageChanged) {
          addChartRowWidget(panel, stageWidget, lastStage, stageStartRow, rowIndex - 1);
          stageWidget = createChartRowWidget(dataItem);
          stageStartRow = rowIndex;
          lastStage = dataItem.getStageId();
        }

        // if second level change
        if (flowChanged) {
          addChartRowWidget(panel, flowWidget, null, flowStartRow, rowIndex - 1);
          flowWidget = createChartRowWidget(dataItem);
          flowStartRow = rowIndex;
          lastFlow = BeeUtils.joinItems(dataItem.getViewName(), rowIndex);
        }

        if (stageChanged) {
          TimeBoardHelper.addRowSeparator(panel, top, 0, getChartLeft() + calendarWidth);
        } else if (flowChanged) {
          TimeBoardHelper.addRowSeparator(panel, top, 0, (getChartLeft() * 2) + calendarWidth);
        }
      }

      for (ChartItem item : chartItems) {
        // first level filter
        if (item.getStageId() == dataItem.stageId) {
          Widget itemWidget = createItemWidget(item);
          Rectangle rectangle = getRectangle(item.getRange(), rowIndex);
          TimeBoardHelper.apply(itemWidget, rectangle, margins);
          styleItemWidget(item, itemWidget);

          panel.add(itemWidget);
        }
      }

      int lastRow = boardLayout.size() - 1;

      if (stageWidget != null) {
        addChartRowWidget(panel, stageWidget, lastStage, stageStartRow, lastRow);
      }

      // second level
      if (flowWidget != null) {
        addChartRowWidget(panel, flowWidget, null, flowStartRow, lastRow);
      }

      rowIndex++;
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
    getCanvas().clear();
    if (response.isEmpty()) {
      return false;
    }

    if (!response.hasResponse(SimpleRowSet.class)) {
      Assert.notImplemented();
    }

    SimpleRowSet rs = SimpleRowSet.restore(response.getResponseAsString());

    int idxViewName = rs.getColumnIndex(ALS_VIEW_NAME);
    int idxStage = rs.getColumnIndex(ALS_CHART_ID);
    int idxCaption = rs.getColumnIndex(ALS_CHART_CAPTION);
    int idxStart = rs.getColumnIndex(ALS_CHART_START);
    int idxEnd = rs.getColumnIndex(ALS_CHART_END);
    int idxColor = rs.getColumnIndex(ALS_CHART_FLOW_COLOR);

    for (String[] row : rs.getRows()) {
      Long id = BeeUtils.toLong(row[idxStage]);
      JustDate start = new JustDate(BeeUtils.toInt(row[idxStart]));
      JustDate end = new JustDate(BeeUtils.toInt(row[idxEnd]));
      ChartItem ci = new ChartItem(row[idxViewName], id, row[idxCaption], start,
          end, row[idxColor]);

      chartItems.add(ci);
    }

    updateMaxRange();

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

  private void addChartRowWidget(HasWidgets panel, IdentifiableWidget widget,
      Long stageId, int firstRow, int lastRow) {
    Rectangle rectangle = TimeBoardHelper.getRectangle(0, getChartLeft(), firstRow, lastRow,
        getRowHeight());

    Edges margins = new Edges();
    margins.setRight(TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    TimeBoardHelper.apply(widget.asWidget(), rectangle, margins);

    panel.add(widget.asWidget());
    stagePanels.add(widget.getId());
    for (int row = firstRow; row <= lastRow; row++) {
      stagesByRow.put(row, stageId);
    }
  }

  private static IdentifiableWidget createChartRowWidget(ChartItem item) {
    Flow panel = new Flow(STYLE_STAGE_ROW);
    CustomDiv label = new CustomDiv(STYLE_STAGE_LABEL);
    label.setText(item.getCaption());
    panel.add(label);
    return panel;
  }

  private List<TimeBoardRowLayout> createLayout() {
    List<TimeBoardRowLayout> result = new ArrayList<>();

    Range<JustDate> range = getVisibleRange();
    List<HasDateRange> items;

    for (int i = 0; i < chartItems.size(); i++) {
      TimeBoardRowLayout layout = new TimeBoardRowLayout(i);
      items = TimeBoardHelper.getActiveItems(chartItems, range);
      layout.addItems(Long.valueOf(chartItems.get(i).getStageId()), items, range);
      result.add(layout);
    }

    return result;
  }

  private static Widget createItemWidget(ChartItem item) {
    Flow panel = new Flow(STYLE_STAGE_ROW);

    panel.addStyleName(STYLE_STAGE_FLOW);
    panel.setTitle(item.getCaption());

    if (!BeeUtils.isEmpty(item.getColor())) {
      StyleUtils.setBackgroundColor(panel, item.getColor());
    }
    return panel;
  }

  private ProjectScheduleChart() {

  }
}
