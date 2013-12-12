package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.event.logical.VisibilityChangeEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.modules.transport.charts.CargoEvent.Type;
import com.butent.bee.client.modules.transport.charts.Filterable.FilterType;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasWidgetSupplier;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderSilverImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class ChartBase extends Flow implements Presenter, View, Printable, HandlesAllDataEvents,
    VisibilityChangeEvent.Handler, HasWidgetSupplier, HasVisibleRange, MoveEvent.Handler {

  private enum RangeMover {
    START_SLIDER, END_SLIDER, MOVER
  }

  private static final BeeLogger logger = LogUtils.getLogger(ChartBase.class);

  private static final String STYLE_PREFIX = "bee-tr-chart-";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "Container";
  private static final String STYLE_CANVAS = STYLE_PREFIX + "Canvas";

  private static final String STYLE_HEADER = STYLE_PREFIX + "Header";
  private static final String STYLE_HEADER_SPLITTER = STYLE_HEADER + "Splitter";

  private static final String STYLE_SCROLL_AREA = STYLE_PREFIX + "ScrollArea";
  private static final String STYLE_CONTENT = STYLE_PREFIX + "Content";
  private static final String STYLE_ROW_RESIZER = STYLE_CONTENT + "-row-resizer";

  private static final String STYLE_FOOTER = STYLE_PREFIX + "Footer";
  private static final String STYLE_FOOTER_SPLITTER = STYLE_FOOTER + "Splitter";

  private static final String STYLE_SELECTOR_PREFIX = STYLE_PREFIX + "Selector-";
  private static final String STYLE_SELECTOR_PANEL = STYLE_SELECTOR_PREFIX + "panel";
  private static final String STYLE_SELECTOR_STRIP = STYLE_SELECTOR_PREFIX + "strip";
  private static final String STYLE_SELECTOR_START_SLIDER = STYLE_SELECTOR_PREFIX + "startSlider";
  private static final String STYLE_SELECTOR_END_SLIDER = STYLE_SELECTOR_PREFIX + "endSlider";
  private static final String STYLE_SELECTOR_MOVER = STYLE_SELECTOR_PREFIX + "mover";

  private static final String STYLE_START_SLIDER_LABEL = STYLE_SELECTOR_START_SLIDER + "-label";
  private static final String STYLE_END_SLIDER_LABEL = STYLE_SELECTOR_END_SLIDER + "-label";

  private static final String STYLE_FILTER_LABEL = STYLE_PREFIX + "filterLabel";
  private static final String STYLE_ACTION_REMOVE_FILTER = STYLE_PREFIX + "actionRemoveFilter";

  private static final String STYLE_SHIPMENT_DAY_PREFIX = STYLE_PREFIX + "shipment-day-";
  private static final String STYLE_SHIPMENT_DAY_PANEL = STYLE_SHIPMENT_DAY_PREFIX + "panel";
  private static final String STYLE_SHIPMENT_DAY_WIDGET = STYLE_SHIPMENT_DAY_PREFIX + "widget";
  private static final String STYLE_SHIPMENT_DAY_EMPTY = STYLE_SHIPMENT_DAY_PREFIX + "empty";
  private static final String STYLE_SHIPMENT_DAY_FLAG = STYLE_SHIPMENT_DAY_PREFIX + "flag";
  private static final String STYLE_SHIPMENT_DAY_LABEL = STYLE_SHIPMENT_DAY_PREFIX + "label";

  private static final String STYLE_ITEM_PREFIX = STYLE_PREFIX + "item-";
  private static final String STYLE_ITEM_START = STYLE_ITEM_PREFIX + "start";
  private static final String STYLE_ITEM_END = STYLE_ITEM_PREFIX + "end";
  
  private final HeaderView headerView;
  private final Flow canvas;

  private final List<HandlerRegistration> registry = Lists.newArrayList();

  private boolean enabled = true;

  private final List<Color> colors = Lists.newArrayList();

  private final Multimap<Long, CargoHandling> cargoHandling = ArrayListMultimap.create();

  private BeeRowSet settings;

  private Range<JustDate> maxRange;
  private Range<JustDate> visibleRange;

  private boolean renderPending;

  private int headerHeight;
  private int footerHeight;

  private int chartLeft;
  private int chartRight;
  private int chartWidth = BeeConst.UNDEF;

  private int dayColumnWidth = BeeConst.UNDEF;
  private int rowHeight = BeeConst.UNDEF;

  private boolean showCountryFlags;
  private boolean showPlaceInfo;

  private int sliderWidth;

  private Widget startSliderLabel;
  private Widget endSliderLabel;

  private final EnumMap<RangeMover, Element> rangeMovers = Maps.newEnumMap(RangeMover.class);

  private String scrollAreaId;
  private String contentId;

  private int rowCount;

  private final Set<String> relevantDataViews = Sets.newHashSet(VIEW_ORDER_CARGO,
      VIEW_CARGO_HANDLING, VIEW_CARGO_TRIPS, VIEW_TRIP_CARGO, CommonsConstants.VIEW_COUNTRIES,
      CommonsConstants.VIEW_COLORS, CommonsConstants.VIEW_THEME_COLORS);

  private final CustomDiv filterLabel;
  private final CustomDiv removeFilter;

  private final List<ChartData> filterData = Lists.newArrayList();
  private boolean filtered;

  protected ChartBase() {
    super();
    addStyleName(STYLE_CONTAINER);

    Set<Action> enabledActions = getEnabledActions();

    this.headerView = new HeaderSilverImpl();
    headerView.create(getCaption(), false, true, EnumSet.of(UiOption.ROOT), enabledActions,
        Action.NO_ACTIONS, Action.NO_ACTIONS);

    if (BeeUtils.contains(enabledActions, Action.FILTER)) {
      this.filterLabel = new CustomDiv(STYLE_FILTER_LABEL);
      headerView.addCommandItem(filterLabel);

      this.removeFilter = new CustomDiv(STYLE_ACTION_REMOVE_FILTER);
      removeFilter.setHtml(String.valueOf(BeeConst.CHAR_TIMES));
      
      removeFilter.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleAction(Action.REMOVE_FILTER);
        }
      });
      
      removeFilter.setTitle(Action.REMOVE_FILTER.getCaption());
      removeFilter.setVisible(false);

      headerView.addCommandItem(removeFilter);

    } else {
      this.filterLabel = null;
      this.removeFilter = null;
    }

    headerView.setViewPresenter(this);
    add(headerView);

    this.canvas = new Flow();
    canvas.addStyleName(STYLE_CANVAS);
    add(canvas);
  }

  @Override
  public String getEventSource() {
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return headerView;
  }

  @Override
  public View getMainView() {
    return this;
  }

  @Override
  public Range<JustDate> getMaxRange() {
    return maxRange;
  }

  @Override
  public int getMaxSize() {
    return getChartWidth();
  }

  @Override
  public Element getPrintElement() {
    return getElement();
  }

  @Override
  public Presenter getViewPresenter() {
    return this;
  }

  @Override
  public Range<JustDate> getVisibleRange() {
    return visibleRange;
  }

  @Override
  public IdentifiableWidget getWidget() {
    return this;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public void handleAction(Action action) {
    switch (action) {
      case REFRESH:
        refresh();
        break;

      case CONFIGURE:
        editSettings();
        break;

      case CANCEL:
      case CLOSE:
        BeeKeeper.getScreen().closeWidget(this);
        break;

      case PRINT:
        Printer.print(this);
        break;

      case FILTER:
        FilterHelper.openDialog(filterData, new FilterHelper.DialogCallback() {
          @Override
          public void onClear() {
            resetFilter(FilterType.TENTATIVE);
          }

          @Override
          public void onFilter() {
            setFiltered(persistFilter());
            refreshFilterInfo();
            render(false);
          }

          @Override
          public void onSelectionChange(HasWidgets dataContainer) {
            filter(FilterType.TENTATIVE);
            FilterHelper.enableData(getFilterData(), prepareFilterData(FilterType.TENTATIVE),
                dataContainer);
          }
        });
        break;

      case REMOVE_FILTER:
        clearFilter();
        render(false);
        break;

      default:
        logger.warning(getCaption(), action, "not implemented");
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (isDataEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (isDataEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onMove(MoveEvent event) {
    if (!(event.getSource() instanceof Mover)) {
      return;
    }

    int delta = event.getDeltaX();
    Element source = ((Mover) event.getSource()).getElement();

    RangeMover sourceType = null;
    for (Map.Entry<RangeMover, Element> entry : rangeMovers.entrySet()) {
      if (entry.getValue().getId().equals(source.getId())) {
        sourceType = entry.getKey();
        break;
      }
    }
    if (sourceType == null) {
      logger.warning("range mover not found:", source);
      return;
    }

    int itemAreaWidth = getChartWidth();

    double dayWidth = (double) itemAreaWidth / ChartHelper.getSize(getMaxRange());
    int maxWidth = BeeUtils.round(dayWidth * getMaxSize());

    int startPos = StyleUtils.getLeft(rangeMovers.get(RangeMover.START_SLIDER));
    int endPos = StyleUtils.getLeft(rangeMovers.get(RangeMover.END_SLIDER)) - getSliderWidth();

    boolean hasMover = rangeMovers.containsKey(RangeMover.MOVER);

    boolean changed = false;

    if (delta != 0) {
      switch (sourceType) {
        case START_SLIDER:
          int newStart = BeeUtils.clamp(startPos + delta, 0, endPos - 1);
          if (maxWidth > 0 && endPos - newStart > maxWidth) {
            newStart = endPos - maxWidth;
          }

          if (newStart != startPos) {
            startPos = newStart;

            StyleUtils.setLeft(source, startPos);
            if (hasMover) {
              StyleUtils.setLeft(rangeMovers.get(RangeMover.MOVER), startPos + getSliderWidth());
              StyleUtils.setWidth(rangeMovers.get(RangeMover.MOVER), endPos - startPos);
            }

            changed = true;
          }
          break;

        case END_SLIDER:
          int newEnd = BeeUtils.clamp(endPos + delta, startPos + 1, itemAreaWidth);
          if (maxWidth > 0 && newEnd - startPos > maxWidth) {
            newEnd = startPos + maxWidth;
          }

          if (newEnd != endPos) {
            endPos = newEnd;

            StyleUtils.setLeft(source, endPos + getSliderWidth());
            if (hasMover) {
              StyleUtils.setWidth(rangeMovers.get(RangeMover.MOVER), endPos - startPos);
            }

            changed = true;
          }
          break;

        case MOVER:
          int dx = BeeUtils.clamp(delta, -startPos, itemAreaWidth - endPos);
          if (dx != 0) {
            startPos += dx;
            endPos += dx;

            StyleUtils.setLeft(rangeMovers.get(RangeMover.START_SLIDER), startPos);
            StyleUtils.setLeft(rangeMovers.get(RangeMover.END_SLIDER), endPos + getSliderWidth());
            StyleUtils.setLeft(source, startPos + getSliderWidth());

            changed = true;
          }
          break;
      }
    }

    if (changed || event.isFinished()) {

      if (event.isFinished()) {
        getStartSliderLabel().setVisible(false);
        getEndSliderLabel().setVisible(false);

        JustDate min = getMaxRange().lowerEndpoint();
        JustDate max = getMaxRange().upperEndpoint();

        JustDate start = TimeUtils.clamp(ChartHelper.getDate(min, startPos, dayWidth), min, max);
        JustDate end = TimeUtils.clamp(TimeUtils.previousDay(ChartHelper.getDate(min, endPos,
            dayWidth)), start, max);

        if (!setVisibleRange(start, end)) {
          JustDate firstVisible = getVisibleRange().lowerEndpoint();
          JustDate lastVisible = getVisibleRange().upperEndpoint();

          int p1 = ChartHelper.getPosition(min, firstVisible, dayWidth);
          p1 = BeeUtils.clamp(p1, 0, itemAreaWidth - getSliderWidth());

          int p2 = ChartHelper.getPosition(min, TimeUtils.nextDay(lastVisible), dayWidth);
          p2 = BeeUtils.clamp(p2, startPos + getSliderWidth(), itemAreaWidth);

          if (p1 != startPos || p2 != endPos) {
            StyleUtils.setLeft(rangeMovers.get(RangeMover.START_SLIDER), p1);
            StyleUtils.setLeft(rangeMovers.get(RangeMover.END_SLIDER), p2 + getSliderWidth());

            if (hasMover) {
              StyleUtils.setLeft(rangeMovers.get(RangeMover.MOVER), p1 + getSliderWidth());
              StyleUtils.setWidth(rangeMovers.get(RangeMover.MOVER), p2 - p1);
            }
          }
        }

      } else {
        JustDate min = getMaxRange().lowerEndpoint();
        JustDate max = TimeUtils.nextDay(getMaxRange().upperEndpoint());

        JustDate start = TimeUtils.clamp(ChartHelper.getDate(min, startPos, dayWidth), min, max);
        JustDate end = TimeUtils.clamp(ChartHelper.getDate(min, endPos, dayWidth), start, max);

        String startLabel = start.toString();
        String endLabel = end.toString();

        getStartSliderLabel().setVisible(true);
        getEndSliderLabel().setVisible(true);

        getStartSliderLabel().getElement().setInnerText(startLabel);
        getEndSliderLabel().getElement().setInnerText(endLabel);

        int startWidth = getStartSliderLabel().getOffsetWidth();
        int endWidth = getStartSliderLabel().getOffsetWidth();

        int canvasWidth = getCanvasWidth();
        int panelLeft = getChartLeft() - getSliderWidth();

        int startLeft = startPos - (startWidth - getSliderWidth()) / 2;
        startLeft = BeeUtils.clamp(startLeft, -panelLeft,
            canvasWidth - panelLeft - startWidth - endWidth);
        StyleUtils.setLeft(getStartSliderLabel(), panelLeft + startLeft);

        int endLeft = endPos - (endWidth - getSliderWidth()) / 2;
        endLeft = BeeUtils.clamp(endLeft, startLeft + startWidth,
            canvasWidth - panelLeft - endWidth);
        StyleUtils.setLeft(getEndSliderLabel(), panelLeft + endLeft);
      }
    }
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (isDataEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    boolean ok;
    String id = source.getId();

    if (getId().equals(id)) {
      int adjustment = getPrintHeightAdjustment();
      if (adjustment != 0) {
        StyleUtils.setHeight(target, source.getClientHeight() + adjustment);
      }
      ok = true;

    } else if (headerView.asWidget().getElement().isOrHasChild(source)) {
      if (StyleUtils.hasClassName(source, STYLE_ACTION_REMOVE_FILTER)) {
        ok = false;
      } else {
        ok = headerView.onPrint(source, target);
      }

    } else {
      ok = true;
    }

    return ok;
  }

  @Override
  public void onResize() {
    render(false);
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (isDataEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (isDataEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (isDataEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onViewUnload() {
  }

  @Override
  public void onVisibilityChange(VisibilityChangeEvent event) {
    if (event.isVisible() && DomUtils.isOrHasAncestor(getElement(), event.getId())) {
      if (isRenderPending()) {
        render(false);
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setEventSource(String eventSource) {
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
  }

  @Override
  public boolean setVisibleRange(JustDate start, JustDate end) {
    if (start != null && end != null && TimeUtils.isLeq(start, end)) {

      Range<JustDate> range = Range.closed(start, end);
      if (getMaxSize() > 0 && ChartHelper.getSize(range) > getMaxSize()) {
        range = Range.closed(start, TimeUtils.nextDay(start, getMaxSize() - 1));
      }

      if (!range.equals(getVisibleRange())) {
        setVisibleRange(range);
        render(false);
        return true;
      }
    }
    return false;
  }

  protected void addContentHandlers(Widget content) {
    Binder.addDoubleClickHandler(content, new DoubleClickHandler() {
      @Override
      public void onDoubleClick(DoubleClickEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (x >= getChartLeft() && getVisibleRange() != null && getDayColumnWidth() > 0) {
          JustDate date = TimeUtils.nextDay(getVisibleRange().lowerEndpoint(),
              (x - getChartLeft()) / getDayColumnWidth());

          if (getVisibleRange().contains(date)) {
            onDoubleClickChart(y / getRowHeight(), date);
          }
        }
      }
    });
  }

  protected void addRelevantDataViews(String... viewNames) {
    if (viewNames != null) {
      for (String viewName : viewNames) {
        if (!BeeUtils.isEmpty(viewName)) {
          relevantDataViews.add(viewName);
        }
      }
    }
  }

  protected void adjustWidths() {
    int chartColumnCount = ChartHelper.getSize(getVisibleRange());
    if (chartColumnCount > 0) {
      setDayColumnWidth(Math.max(getChartWidth() / chartColumnCount, 1));
    }
  }

  protected void bindOpener(HasClickHandlers widget, final String viewName, final Long id) {
    if (widget != null && id != null) {
      widget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          openDataRow(event, viewName, id);
        }
      });
    }
  }

  protected int clampFooterHeight(int height, int canvasHeight) {
    return BeeUtils.clamp(height, 3, BeeUtils.clamp(canvasHeight / 2, 3, 1000));
  }

  protected int clampHeaderHeight(int height, int canvasHeight) {
    return BeeUtils.clamp(height, 1, BeeUtils.clamp(canvasHeight / 5, 1, 100));
  }

  protected int clampRowHeight(int height, int canvasHeight) {
    int defMax = (getScrollAreaHeight(canvasHeight) - ChartHelper.DEFAULT_MOVER_HEIGHT) / 2;
    return BeeUtils.clamp(height, 2, BeeUtils.clamp(defMax, 2, 1000));
  }

  protected void clearFilter() {
    resetFilter(FilterType.TENTATIVE);
    resetFilter(FilterType.PERSISTENT);

    setFiltered(false);

    for (ChartData data : filterData) {
      if (data != null) {
        data.enableAll();
        data.deselectAll();
      }
    }

    refreshFilterInfo();
  }

  protected Widget createShipmentDayPanel(Multimap<Long, CargoEvent> dayEvents,
      String parentTitle) {

    Flow panel = new Flow();
    panel.addStyleName(STYLE_SHIPMENT_DAY_PANEL);

    Set<Long> countryIds = dayEvents.keySet();
    Size size = ChartHelper.splitRectangle(getDayColumnWidth(), getRowHeight(), countryIds.size());

    if (size != null) {
      for (Long countryId : countryIds) {
        Widget widget = createShipmentDayWidget(countryId, dayEvents.get(countryId), parentTitle);
        StyleUtils.setSize(widget, size.getWidth(), size.getHeight());

        panel.add(widget);
      }
    }

    return panel;
  }

  protected void editSettings() {
    if (BeeUtils.isEmpty(getSettingsFormName()) || DataUtils.isEmpty(getSettings())) {
      return;
    }

    BeeRow oldSettings = getSettings().getRow(0);
    final Long oldTheme = getColorTheme(oldSettings);

    RowEditor.openRow(getSettingsFormName(), getSettings().getViewName(), oldSettings, true,
        new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            if (result != null) {
              getSettings().clearRows();
              getSettings().addRow(DataUtils.cloneRow(result));

              if (BeeUtils.isEmpty(getThemeColumnName())) {
                render(false);

              } else {
                Long newTheme = getColorTheme(result);
                if (Objects.equal(oldTheme, newTheme)) {
                  render(false);
                } else {
                  updateColorTheme(newTheme);
                }
              }
            }
          }
        });
  }

  protected void extendMaxRange(JustDate start, JustDate end) {
    if (start == null && end == null) {
      return;
    }

    JustDate lower;
    JustDate upper;

    if (getMaxRange() == null) {
      lower = BeeUtils.min(start, end);
      upper = BeeUtils.max(start, end);
    } else {
      lower = BeeUtils.min(getMaxRange().lowerEndpoint(), start);
      upper = BeeUtils.max(getMaxRange().upperEndpoint(), end);
    }

    setMaxRange(Range.closed(lower, upper));
  }

  protected abstract boolean filter(FilterType filterType);

  protected void finalizeContent(ComplexPanel panel) {
    renderMovers(panel, getContentHeight());
    renderRowResizer(panel);
  }

  protected Color findColor(Long id) {
    if (id == null) {
      return null;
    }

    for (Color color : colors) {
      if (Objects.equal(color.getId(), id)) {
        return color;
      }
    }
    return null;
  }

  protected int getCalendarWidth() {
    return ChartHelper.getSize(getVisibleRange()) * getDayColumnWidth();
  }

  protected Flow getCanvas() {
    return canvas;
  }

  protected int getCanvasHeight() {
    return canvas.getElement().getClientHeight();
  }

  protected int getCanvasWidth() {
    return canvas.getElement().getClientWidth();
  }

  protected Collection<CargoHandling> getCargoHandling(Long cargoId) {
    return cargoHandling.get(cargoId);
  }

  protected Pair<JustDate, JustDate> getCargoHandlingSpan(Long cargoId) {
    JustDate minLoad = null;
    JustDate maxUnload = null;

    if (hasCargoHandling(cargoId)) {
      for (CargoHandling ch : getCargoHandling(cargoId)) {
        minLoad = BeeUtils.min(minLoad, ch.getLoadingDate());
        maxUnload = BeeUtils.max(maxUnload, ch.getUnloadingDate());
      }
    }

    return Pair.of(minLoad, maxUnload);
  }

  protected abstract Collection<? extends HasDateRange> getChartItems();

  protected int getChartLeft() {
    return chartLeft;
  }

  protected int getChartRight() {
    return chartRight;
  }

  protected int getChartWidth() {
    return chartWidth;
  }

  protected Color getColor(Long colorSource) {
    if (colorSource == null) {
      return null;
    }

    int index = ChartHelper.getColorIndex(colorSource, colors.size());
    if (BeeUtils.isIndex(colors, index)) {
      return colors.get(index);
    } else {
      return null;
    }
  }

  protected Element getContent() {
    if (BeeUtils.isEmpty(getContentId())) {
      return null;
    } else {
      return Document.get().getElementById(getContentId());
    }
  }

  protected String getContentId() {
    return contentId;
  }

  protected abstract String getDataService();

  protected int getDayColumnWidth() {
    return dayColumnWidth;
  }

  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.FILTER, Action.REFRESH, Action.ADD, Action.CONFIGURE);
  }

  protected Widget getEndSliderLabel() {
    return endSliderLabel;
  }

  protected List<ChartData> getFilterData() {
    return filterData;
  }

  protected int getFooterHeight() {
    return footerHeight;
  }

  protected abstract String getFooterHeightColumnName();

  protected int getFooterSplitterSize() {
    return ChartHelper.DEFAULT_MOVER_HEIGHT;
  }

  protected int getHeaderHeight() {
    return headerHeight;
  }

  protected abstract String getHeaderHeightColumnName();

  protected int getHeaderSplitterSize() {
    return ChartHelper.DEFAULT_MOVER_HEIGHT;
  }

  protected int getLastResizableColumnMaxLeft(int minLeft) {
    int maxLeft = minLeft + ChartHelper.MAX_RESIZABLE_COLUMN_WIDTH;
    if (getChartWidth() > 0) {
      maxLeft = Math.min(maxLeft, getChartLeft() + getChartWidth() / 2);
    }
    return maxLeft;
  }

  protected int getPrintHeightAdjustment() {
    if (BeeUtils.anyEmpty(getScrollAreaId(), getContentId())) {
      return 0;
    }

    Element scrollArea = getScrollArea();
    Element content = Document.get().getElementById(getContentId());
    if (scrollArea == null || content == null) {
      return 0;
    }
    return content.getClientHeight() - scrollArea.getClientHeight();
  }

  protected Rectangle getRectangle(Range<JustDate> range, int row) {
    return getRectangle(range, row, row);
  }

  protected Rectangle getRectangle(Range<JustDate> range, int firstRow, int lastRow) {
    JustDate start = TimeUtils.clamp(range.lowerEndpoint(), getVisibleRange().lowerEndpoint(),
        getVisibleRange().upperEndpoint());
    JustDate end = TimeUtils.clamp(range.upperEndpoint(), getVisibleRange().lowerEndpoint(),
        getVisibleRange().upperEndpoint());

    int left = getChartLeft()
        + TimeUtils.dayDiff(getVisibleRange().lowerEndpoint(), start) * getDayColumnWidth();
    int width = (TimeUtils.dayDiff(start, end) + 1) * getDayColumnWidth();

    return ChartHelper.getRectangle(left, width, firstRow, lastRow, getRowHeight());
  }
  
  protected int getRelativeLeft(Range<JustDate> parent, JustDate date) {
    return TimeUtils.dayDiff(parent.lowerEndpoint(), date) * getDayColumnWidth();
  }

  protected int getRowHeight() {
    return rowHeight;
  }

  protected abstract String getRowHeightColumnName();

  protected Element getScrollArea() {
    if (BeeUtils.isEmpty(getScrollAreaId())) {
      return null;
    } else {
      return Document.get().getElementById(getScrollAreaId());
    }
  }

  protected int getScrollAreaHeight(int canvasHeight) {
    return canvasHeight - getHeaderHeight() - getHeaderSplitterSize() - getFooterHeight()
        - getFooterSplitterSize();
  }

  protected String getScrollAreaId() {
    return scrollAreaId;
  }

  protected BeeRowSet getSettings() {
    return settings;
  }

  protected abstract String getSettingsFormName();

  protected abstract String getShowCountryFlagsColumnName();

  protected abstract String getShowPlaceInfoColumnName();

  protected int getSliderWidth() {
    return sliderWidth;
  }

  protected Widget getStartSliderLabel() {
    return startSliderLabel;
  }

  protected abstract String getStripOpacityColumnName();

  protected abstract String getThemeColumnName();

  protected boolean hasCargoHandling(Long cargoId) {
    return cargoId != null && cargoHandling.containsKey(cargoId);
  }

  protected void initContent(ComplexPanel panel, int rc) {
    setRowCount(rc);

    if (rc > 0) {
      int height = getContentHeight();
      StyleUtils.setHeight(panel, height + getRowResizerHeight());

      ChartHelper.renderDayColumns(panel, getVisibleRange(), getChartLeft(), getDayColumnWidth(),
          height);
    } else {
      StyleUtils.clearHeight(panel);
    }
  }

  protected abstract void initData(Map<String, String> properties);

  protected boolean isDataEventRelevant(DataEvent event) {
    return event != null && relevantDataViews.contains(event.getViewName());
  }

  protected boolean isFiltered() {
    return filtered;
  }

  protected boolean isItemVisible(Filterable item) {
    return item != null && (!isFiltered() || item.matched(FilterType.PERSISTENT));
  }

  protected void onCreate(ResponseObject response, Callback<IdentifiableWidget> callback) {
    if (setData(response)) {
      callback.onSuccess(this);
    } else {
      callback.onFailure(getCaption(), Localized.getConstants().nothingFound());
    }
  }

  /**
   * @param row
   * @param date
   */
  protected void onDoubleClickChart(int row, JustDate date) {
  }

  protected void onFooterSplitterMove(MoveEvent event) {
    int delta = event.getDeltaY();

    Element splitter = ((Mover) event.getSource()).getElement();
    int oldBottom = StyleUtils.getBottom(splitter);

    int canvasHeight = getCanvasHeight();
    int newBottom = Math.min(clampFooterHeight(oldBottom - delta, canvasHeight),
        canvasHeight - getHeaderHeight() - getHeaderSplitterSize() - getRowHeight());

    if (newBottom != oldBottom) {
      StyleUtils.setBottom(splitter, newBottom);
      StyleUtils.setBottom(getScrollArea(), newBottom + getFooterSplitterSize());
    }

    if (event.isFinished() && updateSetting(getFooterHeightColumnName(), newBottom)) {
      render(false);
    }
  }

  protected void onHeaderSplitterMove(MoveEvent event) {
    int delta = event.getDeltaY();

    Element splitter = ((Mover) event.getSource()).getElement();
    int oldTop = StyleUtils.getTop(splitter);

    int canvasHeight = getCanvasHeight();
    int newTop = Math.min(clampHeaderHeight(oldTop + delta, canvasHeight),
        canvasHeight - getFooterHeight() - getFooterSplitterSize() - getRowHeight());

    if (newTop != oldTop) {
      StyleUtils.setTop(splitter, newTop);
      StyleUtils.setTop(getScrollArea(), newTop + getHeaderSplitterSize());
    }

    if (event.isFinished() && updateSetting(getHeaderHeightColumnName(), newTop)) {
      render(false);
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    EventUtils.clearRegistry(registry);
    registry.addAll(register());

    render(true);
  }

  protected void onRowResizerMove(MoveEvent event) {
    int delta = event.getDeltaY();
    Element resizer = ((Mover) event.getSource()).getElement();

    int oldTop = StyleUtils.getTop(resizer);
    int newTop = Math.max(oldTop + delta, getRowCount() * 2);

    if (newTop != oldTop) {
      StyleUtils.setHeight(getContent(), newTop + getRowResizerHeight());
      StyleUtils.setTop(resizer, newTop);
    }

    if (event.isFinished()) {
      int rh = BeeUtils.round((double) newTop / getRowCount());
      if (rh != getRowHeight()) {
        updateSetting(getRowHeightColumnName(), rh);
      }

      render(false);
    }
  }

  @Override
  protected void onUnload() {
    EventUtils.clearRegistry(registry);

    super.onUnload();
  }

  protected void openDataRow(HasNativeEvent event, String viewName, Long rowId) {
    RowEditor.openRow(viewName, rowId, !EventUtils.hasModifierKey(event.getNativeEvent()), null);
  }

  protected abstract boolean persistFilter();

  protected abstract void prepareChart(Size canvasSize);

  protected void prepareDefaults(Size canvasSize) {
    setChartRight(DomUtils.getScrollBarWidth());

    int hh = ChartHelper.getPixels(getSettings(), getHeaderHeightColumnName(), 20);
    setHeaderHeight(clampHeaderHeight(hh, canvasSize.getHeight()));

    int fh = ChartHelper.getPixels(getSettings(), getFooterHeightColumnName(), 30);
    setFooterHeight(clampFooterHeight(fh, canvasSize.getHeight()));

    int rh = ChartHelper.getPixels(getSettings(), getRowHeightColumnName(), 20);
    setRowHeight(clampRowHeight(rh, canvasSize.getHeight()));

    setShowCountryFlags(ChartHelper.getBoolean(getSettings(), getShowCountryFlagsColumnName()));
    setShowPlaceInfo(ChartHelper.getBoolean(getSettings(), getShowPlaceInfoColumnName()));
  }

  protected abstract List<ChartData> prepareFilterData(FilterType filterType);

  protected void prepareFooter() {
    if (getChartWidth() > 0) {
      setSliderWidth(BeeUtils.clamp(ChartHelper.DEFAULT_MOVER_WIDTH,
          1, Math.min(getChartLeft(), getChartRight())));
    } else {
      setSliderWidth(0);
    }
  }

  protected void refresh() {
    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(getDataService()),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (setData(response)) {
              render(false);
            }
          }
        });
  }

  protected List<HandlerRegistration> register() {
    List<HandlerRegistration> result = Lists.newArrayList();

    result.add(VisibilityChangeEvent.register(this));
    result.addAll(BeeKeeper.getBus().registerDataHandler(this, false));

    return result;
  }

  protected void render(boolean updateRange) {
    canvas.clear();
    if (getMaxRange() == null) {
      return;
    }

    int width = getCanvasWidth();
    int height = getCanvasHeight();
    if (width < 30 || height < 30) {
      setRenderPending(true);
      return;
    }

    Size canvasSize = new Size(width, height);

    prepareDefaults(canvasSize);
    prepareChart(canvasSize);

    if (updateRange || getVisibleRange() == null) {
      setVisibleRange(ChartHelper.getDefaultRange(getMaxRange(), getChartWidth(),
          getDayColumnWidth()));
    } else {
      checkVisibleRange();
    }

    adjustWidths();

    renderHeader();
    renderHeaderSplitter();

    Flow content = new Flow();
    content.addStyleName(STYLE_CONTENT);

    renderContent(content);
    finalizeContent(content);

    addContentHandlers(content);

    Simple scroll = new Simple();
    scroll.addStyleName(STYLE_SCROLL_AREA);
    StyleUtils.setTop(scroll, getHeaderHeight() + getHeaderSplitterSize());
    StyleUtils.setBottom(scroll, getFooterHeight() + getFooterSplitterSize());

    scroll.setWidget(content);

    setContentId(content.getId());
    setScrollAreaId(scroll.getId());

    canvas.add(scroll);

    renderFooterSplitter();

    prepareFooter();
    renderFooter(getChartItems());

    setRenderPending(false);
  }

  protected void renderCargoShipment(HasWidgets panel, OrderCargo cargo, String parentTitle) {
    if (panel == null || cargo == null) {
      return;
    }

    Range<JustDate> range = ChartHelper.normalizedIntersection(cargo.getRange(), getVisibleRange());
    if (range == null) {
      return;
    }

    Multimap<JustDate, CargoEvent> cargoLayout = splitCargoByDate(cargo, range);
    if (cargoLayout.isEmpty()) {
      return;
    }

    for (JustDate date : cargoLayout.keySet()) {
      Multimap<Long, CargoEvent> dayLayout = CargoEvent.splitByCountry(cargoLayout.get(date));
      if (!dayLayout.isEmpty()) {
        Widget dayWidget = createShipmentDayPanel(dayLayout, parentTitle);

        StyleUtils.setLeft(dayWidget, getRelativeLeft(range, date));
        StyleUtils.setWidth(dayWidget, getDayColumnWidth());

        panel.add(dayWidget);
      }
    }
  }

  protected abstract void renderContent(ComplexPanel panel);

  protected void renderDayLabels(HasWidgets panel) {
    ChartHelper.renderDayLabels(panel, getVisibleRange(), getChartLeft(), getDayColumnWidth(),
        getHeaderHeight());
  }

  protected void renderFooter(Collection<? extends HasDateRange> chartItems) {
    if (getFooterHeight() <= 0) {
      return;
    }

    Flow footer = new Flow();
    footer.addStyleName(STYLE_FOOTER);
    StyleUtils.setHeight(footer, getFooterHeight());

    renderMaxRange(footer);
    renderRangeSelector(footer, chartItems);

    canvas.add(footer);

    renderSliderLabels();
  }

  protected void renderFooterSplitter() {
    if (getFooterSplitterSize() > 0) {
      Mover mover = ChartHelper.createVerticalMover();

      mover.addStyleName(STYLE_FOOTER_SPLITTER);
      StyleUtils.setBottom(mover, Math.max(getFooterHeight(), 0));

      mover.addMoveHandler(new MoveEvent.Handler() {
        @Override
        public void onMove(MoveEvent event) {
          onFooterSplitterMove(event);
        }
      });

      canvas.add(mover);
    }
  }

  protected void renderHeader() {
    if (getHeaderHeight() > 0) {
      Flow header = new Flow();
      header.addStyleName(STYLE_HEADER);
      StyleUtils.setHeight(header, getHeaderHeight());

      renderVisibleRange(header);
      renderDayLabels(header);

      canvas.add(header);
    }
  }

  protected void renderHeaderSplitter() {
    if (getHeaderSplitterSize() > 0) {
      Mover mover = ChartHelper.createVerticalMover();

      mover.addStyleName(STYLE_HEADER_SPLITTER);
      StyleUtils.setTop(mover, Math.max(getHeaderHeight(), 0));

      mover.addMoveHandler(new MoveEvent.Handler() {
        @Override
        public void onMove(MoveEvent event) {
          onHeaderSplitterMove(event);
        }
      });

      canvas.add(mover);
    }
  }

  protected void renderMaxRange(HasWidgets panel) {
    ChartHelper.renderMaxRange(getMaxRange(), panel, getChartLeft() - getSliderWidth(),
        getFooterHeight());
  }

  protected abstract void renderMovers(ComplexPanel panel, int height);

  protected void renderRangeSelector(HasWidgets panel,
      Collection<? extends HasDateRange> chartItems) {

    Flow selector = new Flow();
    selector.addStyleName(STYLE_SELECTOR_PANEL);

    StyleUtils.setLeft(selector, getChartLeft() - getSliderWidth());
    StyleUtils.setWidth(selector, getChartWidth() + getSliderWidth() * 2);

    renderSelector(selector, chartItems);
    panel.add(selector);
  }

  protected void renderRowResizer(ComplexPanel panel) {
    if (getRowCount() > 0) {
      Mover mover = ChartHelper.createVerticalMover();
      mover.addStyleName(STYLE_ROW_RESIZER);

      StyleUtils.setWidth(mover, getChartLeft() + getCalendarWidth());
      StyleUtils.setTop(mover, getContentHeight());

      mover.addMoveHandler(new MoveEvent.Handler() {
        @Override
        public void onMove(MoveEvent event) {
          onRowResizerMove(event);
        }
      });

      panel.add(mover);
    }
  }

  protected void renderSelector(HasWidgets panel, Collection<? extends HasDateRange> chartItems) {
    if (getMaxRange() == null || chartItems.isEmpty()) {
      return;
    }

    int itemAreaLeft = getSliderWidth();
    int itemAreaWidth = getChartWidth();

    int height = getFooterHeight();

    if (itemAreaWidth <= 0 || height <= 0) {
      return;
    }

    JustDate firstDate = getMaxRange().lowerEndpoint();
    JustDate lastDate = getMaxRange().upperEndpoint();

    double dayWidth = (double) itemAreaWidth / ChartHelper.getSize(getMaxRange());

    int itemAreaHeight = height;

    int maxRows;
    if (itemAreaHeight <= 10) {
      maxRows = itemAreaHeight;
    } else if (itemAreaHeight <= 20) {
      maxRows = itemAreaHeight / 2;
    } else {
      maxRows = itemAreaHeight / 3;
    }

    List<List<HasDateRange>> rows = doStripLayout(chartItems, maxRows);

    int rc = rows.size();
    int itemHeight = itemAreaHeight / rc;

    Double stripOpacity = BeeUtils.isEmpty(getStripOpacityColumnName())
        ? null : ChartHelper.getOpacity(getSettings(), getStripOpacityColumnName());

    for (int row = 0; row < rc; row++) {
      for (HasDateRange item : rows.get(row)) {

        Widget itemStrip = new CustomDiv(STYLE_SELECTOR_STRIP);
        setItemWidgetColor(item, itemStrip);

        if (stripOpacity != null) {
          StyleUtils.setOpacity(itemStrip, stripOpacity);
        }

        StyleUtils.setTop(itemStrip, row * itemHeight);
        StyleUtils.setHeight(itemStrip, itemHeight);

        JustDate start = TimeUtils.clamp(item.getRange().lowerEndpoint(), firstDate, lastDate);
        JustDate end = TimeUtils.clamp(item.getRange().upperEndpoint(), firstDate, lastDate);

        int left = BeeUtils.round(TimeUtils.dayDiff(firstDate, start) * dayWidth);
        StyleUtils.setLeft(itemStrip, itemAreaLeft + BeeUtils.clamp(left, 0, itemAreaWidth - 1));

        int w = BeeUtils.round((TimeUtils.dayDiff(start, end) + 1) * dayWidth);
        StyleUtils.setWidth(itemStrip, BeeUtils.clamp(w, 1, itemAreaWidth - left));

        panel.add(itemStrip);
      }
    }

    JustDate firstVisible = getVisibleRange().lowerEndpoint();
    JustDate lastVisible = getVisibleRange().upperEndpoint();

    int startPos = ChartHelper.getPosition(firstDate, firstVisible, dayWidth);
    startPos = BeeUtils.clamp(startPos, 0, itemAreaWidth - getSliderWidth());

    int endPos = ChartHelper.getPosition(firstDate, TimeUtils.nextDay(lastVisible), dayWidth);
    endPos = BeeUtils.clamp(endPos, startPos + getSliderWidth(), itemAreaWidth);

    rangeMovers.clear();

    if (getSliderWidth() > 0) {
      Mover startSlider = new Mover(STYLE_SELECTOR_START_SLIDER, Orientation.HORIZONTAL);
      StyleUtils.setLeft(startSlider, startPos);
      StyleUtils.setWidth(startSlider, getSliderWidth());

      startSlider.setTitle(firstVisible.toString());

      startSlider.addMoveHandler(this);
      rangeMovers.put(RangeMover.START_SLIDER, startSlider.getElement());

      panel.add(startSlider);

      Mover endSlider = new Mover(STYLE_SELECTOR_END_SLIDER, Orientation.HORIZONTAL);
      StyleUtils.setLeft(endSlider, endPos + getSliderWidth());
      StyleUtils.setWidth(endSlider, getSliderWidth());

      endSlider.setTitle(TimeUtils.nextDay(lastVisible).toString());

      endSlider.addMoveHandler(this);
      rangeMovers.put(RangeMover.END_SLIDER, endSlider.getElement());

      panel.add(endSlider);

      if (startPos < endPos) {
        Mover mover = new Mover(STYLE_SELECTOR_MOVER, Orientation.HORIZONTAL);
        StyleUtils.setLeft(mover, startPos + getSliderWidth());
        StyleUtils.setWidth(mover, endPos - startPos);

        mover.setTitle(ChartHelper.getRangeLabel(firstVisible, lastVisible));

        mover.addMoveHandler(this);
        rangeMovers.put(RangeMover.MOVER, mover.getElement());

        panel.add(mover);
      }
    }
  }

  protected void renderSliderLabels() {
    Label startLabel = new Label();
    startLabel.addStyleName(STYLE_START_SLIDER_LABEL);
    StyleUtils.setBottom(startLabel, getFooterHeight());
    startLabel.setVisible(false);

    setStartSliderLabel(startLabel);
    canvas.add(startLabel);

    Label endLabel = new Label();
    endLabel.addStyleName(STYLE_END_SLIDER_LABEL);
    StyleUtils.setBottom(endLabel, getFooterHeight());
    endLabel.setVisible(false);

    setEndSliderLabel(endLabel);
    canvas.add(endLabel);
  }

  protected void renderTrip(HasWidgets panel, String title,
      Collection<? extends OrderCargo> cargos, Range<JustDate> range, String styleVoid) {

    List<Range<JustDate>> voidRanges;

    if (BeeUtils.isEmpty(cargos)) {
      voidRanges = Lists.newArrayList();
      voidRanges.add(range);

    } else {
      Multimap<JustDate, CargoEvent> tripLayout = splitTripByDate(cargos, range);
      Set<JustDate> eventDates = tripLayout.keySet();

      for (JustDate date : eventDates) {
        Multimap<Long, CargoEvent> dayLayout = CargoEvent.splitByCountry(tripLayout.get(date));
        if (!dayLayout.isEmpty()) {
          Widget dayWidget = createShipmentDayPanel(dayLayout, title);

          StyleUtils.setLeft(dayWidget, getRelativeLeft(range, date));
          StyleUtils.setWidth(dayWidget, getDayColumnWidth());

          panel.add(dayWidget);
        }
      }

      voidRanges = Trip.getVoidRanges(range, eventDates, cargos);
    }

    for (Range<JustDate> voidRange : voidRanges) {
      Widget voidWidget = new CustomDiv(styleVoid);

      StyleUtils.setLeft(voidWidget, getRelativeLeft(range, voidRange.lowerEndpoint()));
      StyleUtils.setWidth(voidWidget, ChartHelper.getSize(voidRange) * getDayColumnWidth());

      panel.add(voidWidget);
    }
  }

  protected void renderVisibleRange(HasWidgets panel) {
    ChartHelper.renderVisibleRange(this, panel, getChartLeft(), getHeaderHeight());
  }

  protected abstract void resetFilter(FilterType filterType);

  protected void setChartLeft(int chartLeft) {
    this.chartLeft = chartLeft;
  }

  protected void setChartRight(int chartRight) {
    this.chartRight = chartRight;
  }

  protected void setChartWidth(int chartWidth) {
    this.chartWidth = chartWidth;
  }

  protected boolean setData(ResponseObject response) {
    if (!Queries.checkResponse(getCaption(), null, response, BeeRowSet.class, null)) {
      return false;
    }

    BeeRowSet rowSet = BeeRowSet.restore((String) response.getResponse());
    setSettings(rowSet);

    String serialized = rowSet.getTableProperty(PROP_COUNTRIES);
    if (!BeeUtils.isEmpty(serialized)) {
      Places.setCountries(BeeRowSet.restore(serialized));
    }

    serialized = rowSet.getTableProperty(PROP_COLORS);
    if (!BeeUtils.isEmpty(serialized)) {
      restoreColors(serialized);
    }

    cargoHandling.clear();
    serialized = rowSet.getTableProperty(PROP_CARGO_HANDLING);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      for (SimpleRow row : srs) {
        cargoHandling.put(row.getLong(COL_CARGO), new CargoHandling(row));
      }
    }

    initData(rowSet.getTableProperties());
    updateMaxRange();

    updateFilterData();

    return true;
  }

  protected void setDayColumnWidth(int dayColumnWidth) {
    this.dayColumnWidth = dayColumnWidth;
  }

  protected void setEndSliderLabel(Widget endSliderLabel) {
    this.endSliderLabel = endSliderLabel;
  }

  protected void setFiltered(boolean filtered) {
    this.filtered = filtered;
  }

  protected void setFooterHeight(int footerHeight) {
    this.footerHeight = footerHeight;
  }

  protected void setHeaderHeight(int headerHeight) {
    this.headerHeight = headerHeight;
  }

  protected void setItemWidgetColor(HasDateRange item, Widget widget) {
    if (item instanceof HasColorSource) {
      Color color = getColor(((HasColorSource) item).getColorSource());
      if (color != null) {
        UiHelper.setColor(widget, color);
      }
    }
  }

  protected void setRowHeight(int rowHeight) {
    this.rowHeight = rowHeight;
  }

  protected void setSliderWidth(int sliderWidth) {
    this.sliderWidth = sliderWidth;
  }

  protected void setStartSliderLabel(Widget startSliderLabel) {
    this.startSliderLabel = startSliderLabel;
  }

  protected void setVisibleRange(Range<JustDate> visibleRange) {
    this.visibleRange = visibleRange;
  }

  protected Multimap<JustDate, CargoEvent> splitCargoByDate(OrderCargo cargo,
      Range<JustDate> range) {

    Multimap<JustDate, CargoEvent> result = ArrayListMultimap.create();
    if (cargo == null || range == null || range.isEmpty()) {
      return result;
    }

    if (cargo.getLoadingDate() != null && range.contains(cargo.getLoadingDate())) {
      result.put(cargo.getLoadingDate(), new CargoEvent(cargo, true));
    }

    if (cargo.getUnloadingDate() != null && range.contains(cargo.getUnloadingDate())) {
      result.put(cargo.getUnloadingDate(), new CargoEvent(cargo, false));
    }

    if (hasCargoHandling(cargo.getCargoId())) {
      for (CargoHandling ch : getCargoHandling(cargo.getCargoId())) {
        if (ch.getLoadingDate() != null && range.contains(ch.getLoadingDate())) {
          result.put(ch.getLoadingDate(), new CargoEvent(cargo, ch, true));
        }

        if (ch.getUnloadingDate() != null && range.contains(ch.getUnloadingDate())) {
          result.put(ch.getUnloadingDate(), new CargoEvent(cargo, ch, false));
        }
      }
    }

    return result;
  }

  protected Multimap<JustDate, CargoEvent> splitTripByDate(Collection<? extends OrderCargo> cargos,
      Range<JustDate> range) {

    Multimap<JustDate, CargoEvent> result = ArrayListMultimap.create();
    if (BeeUtils.isEmpty(cargos)) {
      return result;
    }

    for (OrderCargo cargo : cargos) {
      result.putAll(splitCargoByDate(cargo, range));
    }

    return result;
  }

  protected void styleItemWidget(HasDateRange item, Widget widget) {
    if (getVisibleRange().contains(item.getRange().lowerEndpoint())) {
      widget.addStyleName(STYLE_ITEM_START);
    }
    if (getVisibleRange().contains(item.getRange().upperEndpoint())) {
      widget.addStyleName(STYLE_ITEM_END);
    }
  }

  protected void updateMaxRange() {
    setMaxRange(getChartItems());
  }

  protected boolean updateSetting(String colName, int value) {
    List<String> colNames = Lists.newArrayList(colName);
    List<String> values = Lists.newArrayList(BeeUtils.toString(value));

    return updateSettings(colNames, values);
  }

  protected boolean updateSettings(List<String> colNames, List<String> values) {
    if (DataUtils.isEmpty(getSettings())) {
      logger.severe("update settings: rowSet isempty");
      return false;
    }

    List<Integer> indexes = Lists.newArrayList();

    for (String colName : colNames) {
      int index = getSettings().getColumnIndex(colName);

      if (BeeConst.isUndef(index)) {
        logger.severe(getSettings().getViewName(), colName, "column not found");
        return false;
      } else {
        indexes.add(index);
      }
    }

    List<BeeColumn> columns = Lists.newArrayList();
    List<String> oldValues = Lists.newArrayList();
    List<String> newValues = Lists.newArrayList();

    BeeRow row = getSettings().getRow(0);

    for (int i = 0; i < indexes.size(); i++) {
      int index = indexes.get(i);

      String oldValue = row.getString(index);
      String newValue = values.get(i);

      if (!BeeUtils.equalsTrim(oldValue, newValue)) {
        row.setValue(index, newValue);

        columns.add(getSettings().getColumn(index));
        oldValues.add(oldValue);
        newValues.add(newValue);
      }
    }

    if (columns.isEmpty()) {
      return false;
    }

    Queries.update(getSettings().getViewName(), row.getId(), row.getVersion(), columns, oldValues,
        newValues, null, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            if (result != null) {
              getSettings().clearRows();
              getSettings().addRow(DataUtils.cloneRow(result));
            }
          }
        });

    return true;
  }

  protected boolean updateSettings(String col1, int val1, String col2, int val2) {
    List<String> colNames = Lists.newArrayList(col1, col2);
    List<String> values = Lists.newArrayList(BeeUtils.toString(val1), BeeUtils.toString(val2));

    return updateSettings(colNames, values);
  }

  private void checkVisibleRange() {
    if (!getMaxRange().encloses(getVisibleRange())) {
      int visibleDays = ChartHelper.getSize(getVisibleRange());

      if (visibleDays <= 0 || visibleDays >= ChartHelper.getSize(getMaxRange())) {
        setVisibleRange(ChartHelper.normalizedCopyOf(getMaxRange()));
      } else {
        JustDate lower;
        JustDate upper;

        if (TimeUtils.isLess(getVisibleRange().lowerEndpoint(), getMaxRange().lowerEndpoint())) {
          lower = JustDate.copyOf(getMaxRange().lowerEndpoint());
          upper = TimeUtils.nextDay(lower, visibleDays - 1);
        } else {
          upper = JustDate.copyOf(getMaxRange().upperEndpoint());
          lower = TimeUtils.nextDay(upper, 1 - visibleDays);
        }

        setVisibleRange(Range.closed(lower, upper));
      }
    }
  }

  private Widget createShipmentDayWidget(Long countryId, Collection<CargoEvent> events,
      String parentTitle) {

    Flow widget = new Flow();
    widget.addStyleName(STYLE_SHIPMENT_DAY_WIDGET);

    String flag = showCountryFlags() ? Places.getCountryFlag(countryId) : null;

    if (!BeeUtils.isEmpty(flag)) {
      widget.addStyleName(STYLE_SHIPMENT_DAY_FLAG);
      StyleUtils.setBackgroundImage(widget, flag);
    }

    if (!BeeUtils.isEmpty(events)) {
      if (showPlaceInfo()) {
        List<String> info = Lists.newArrayList();

        if (BeeUtils.isEmpty(flag) && DataUtils.isId(countryId)) {
          String countryLabel = Places.getCountryLabel(countryId);
          if (!BeeUtils.isEmpty(countryLabel)) {
            info.add(countryLabel);
          }
        }

        for (CargoEvent event : events) {
          String place = event.getPlace();
          if (!BeeUtils.isEmpty(place) && !BeeUtils.containsSame(info, place)) {
            info.add(place);
          }

          String terminal = event.getTerminal();
          if (!BeeUtils.isEmpty(terminal) && BeeUtils.containsSame(info, terminal)) {
            info.add(terminal);
          }
        }

        if (!info.isEmpty()) {
          CustomDiv label = new CustomDiv(STYLE_SHIPMENT_DAY_LABEL);
          label.setHtml(BeeUtils.join(BeeConst.STRING_SPACE, info));

          widget.add(label);
        }
      }

      List<String> title = Lists.newArrayList();

      Multimap<OrderCargo, CargoEvent> eventsByCargo = LinkedListMultimap.create();
      for (CargoEvent event : events) {
        eventsByCargo.put(event.getCargo(), event);
      }

      for (OrderCargo cargo : eventsByCargo.keySet()) {
        Map<CargoHandling, EnumSet<CargoEvent.Type>> handlingEvents = Maps.newHashMap();

        for (CargoEvent event : eventsByCargo.get(cargo)) {
          if (event.isHandlingEvent()) {
            CargoEvent.Type eventType = event.isLoading()
                ? CargoEvent.Type.LOADING : CargoEvent.Type.UNLOADING;

            if (handlingEvents.containsKey(event.getHandling())) {
              handlingEvents.get(event.getHandling()).add(eventType);
            } else {
              handlingEvents.put(event.getHandling(), EnumSet.of(eventType));
            }
          }
        }

        if (!title.isEmpty()) {
          title.add(BeeConst.STRING_NBSP);
        }
        title.add(cargo.getTitle());

        if (!handlingEvents.isEmpty()) {
          title.add(BeeConst.STRING_NBSP);

          for (Map.Entry<CargoHandling, EnumSet<Type>> entry : handlingEvents.entrySet()) {
            String chLoading = entry.getValue().contains(CargoEvent.Type.LOADING)
                ? Places.getLoadingInfo(entry.getKey()) : null;
            String chUnloading = entry.getValue().contains(CargoEvent.Type.UNLOADING)
                ? Places.getUnloadingInfo(entry.getKey()) : null;

            title.add(entry.getKey().getTitle(chLoading, chUnloading));
          }
        }
      }

      if (!BeeUtils.isEmpty(parentTitle)) {
        title.add(BeeConst.STRING_NBSP);
        title.add(parentTitle);
      }

      if (!title.isEmpty()) {
        widget.setTitle(BeeUtils.join(BeeConst.STRING_EOL, title));
      }
    }

    if (widget.isEmpty() && BeeUtils.isEmpty(flag)) {
      widget.addStyleName(STYLE_SHIPMENT_DAY_EMPTY);
    }

    return widget;
  }

  private static List<List<HasDateRange>> doStripLayout(
      Collection<? extends HasDateRange> chartItems, int maxRows) {

    List<List<HasDateRange>> rows = Lists.newArrayList();

    for (HasDateRange item : chartItems) {
      int row = BeeConst.UNDEF;
      for (int i = 0; i < rows.size(); i++) {
        if (!BeeUtils.intersects(rows.get(i), item.getRange())) {
          row = i;
          break;
        }
      }

      if (BeeConst.isUndef(row)) {
        if (rows.size() < maxRows) {
          row = rows.size();

          List<HasDateRange> rowItems = Lists.newArrayList();
          rows.add(rowItems);

        } else {
          row = BeeUtils.randomInt(0, rows.size());
        }
      }

      rows.get(row).add(item);
    }

    return rows;
  }

  private Long getColorTheme(BeeRow row) {
    if (row == null || BeeUtils.isEmpty(getThemeColumnName()) || DataUtils.isEmpty(getSettings())) {
      return null;
    } else {
      return row.getLong(getSettings().getColumnIndex(getThemeColumnName()));
    }
  }

  private int getContentHeight() {
    return getRowCount() * getRowHeight();
  }

  private int getRowCount() {
    return rowCount;
  }

  private int getRowResizerHeight() {
    return (getRowCount() > 0) ? ChartHelper.DEFAULT_MOVER_HEIGHT : 0;
  }

  private boolean isRenderPending() {
    return renderPending;
  }

  private void refreshFilterInfo() {
    if (isFiltered()) {
      List<String> selection = Lists.newArrayList();
      for (ChartData data : filterData) {
        Collection<String> selectedNames = data.getSelectedNames();
        if (!selectedNames.isEmpty()) {
          selection.addAll(selectedNames);
        }
      }

      if (!selection.isEmpty()) {
        filterLabel.getElement().setInnerText(BeeUtils.join(BeeConst.STRING_COMMA, selection));
        removeFilter.setVisible(true);
        return;
      }
    }

    filterLabel.getElement().setInnerText(BeeConst.STRING_EMPTY);
    removeFilter.setVisible(false);
  }

  private void restoreColors(String serialized) {
    String[] arr = Codec.beeDeserializeCollection(serialized);
    if (arr != null && arr.length > 0) {
      colors.clear();

      for (String s : arr) {
        Color color = Color.restore(s);
        if (color != null) {
          colors.add(color);
        }
      }
    }
  }

  private void setContentId(String contentId) {
    this.contentId = contentId;
  }

  private void setMaxRange(Collection<? extends HasDateRange> items) {
    JustDate min = TimeUtils.today(-1);
    JustDate max = TimeUtils.startOfNextMonth(min);
    if (min.getDom() > 1) {
      TimeUtils.addDay(max, min.getDom() - 1);
    }

    setMaxRange(ChartHelper.getSpan(items, min, max));
  }

  private void setMaxRange(Range<JustDate> maxRange) {
    this.maxRange = maxRange;
  }

  private void setRenderPending(boolean renderPending) {
    this.renderPending = renderPending;
  }

  private void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  private void setScrollAreaId(String scrollAreaId) {
    this.scrollAreaId = scrollAreaId;
  }

  private void setSettings(BeeRowSet settings) {
    this.settings = settings;
  }

  private void setShowCountryFlags(boolean showCountryFlags) {
    this.showCountryFlags = showCountryFlags;
  }

  private void setShowPlaceInfo(boolean showPlaceInfo) {
    this.showPlaceInfo = showPlaceInfo;
  }

  private boolean showCountryFlags() {
    return showCountryFlags;
  }

  private boolean showPlaceInfo() {
    return showPlaceInfo;
  }

  private void updateColorTheme(Long theme) {
    ParameterList args = TransportHandler.createArgs(SVC_GET_COLORS);
    if (theme != null) {
      args.addQueryItem(VAR_ID, theme);
    }

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        restoreColors((String) response.getResponse());
        render(false);
      }
    });
  }

  private void updateFilterData() {
    List<ChartData> newData = FilterHelper.notEmptyData(prepareFilterData(null));
    if (newData != null) {
      for (ChartData cd : newData) {
        cd.prepare();
      }
    }

    boolean wasFiltered = isFiltered();

    if (BeeUtils.isEmpty(newData)) {
      filterData.clear();
      if (wasFiltered) {
        clearFilter();
      }

    } else if (filterData.isEmpty()) {
      filterData.addAll(newData);

    } else {
      if (wasFiltered) {
        for (ChartData ocd : filterData) {
          ChartData ncd = FilterHelper.getDataByType(newData, ocd.getType());

          if (ncd != null) {
            Collection<String> selectedNames = ocd.getSelectedNames();
            for (String name : selectedNames) {
              ncd.setSelected(name, true);
            }
          }
        }
      }

      filterData.clear();
      filterData.addAll(newData);

      if (wasFiltered) {
        setFiltered(filter(FilterType.TENTATIVE));

        if (isFiltered()) {
          FilterHelper.enableData(getFilterData(), prepareFilterData(FilterType.TENTATIVE), null);

          persistFilter();
          refreshFilterInfo();
          
        } else {
          clearFilter();
        }
      }
    }
  }
}
