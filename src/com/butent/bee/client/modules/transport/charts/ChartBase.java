package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.event.logical.VisibilityChangeEvent;
import com.butent.bee.client.images.Flags;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasWidgetSupplier;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderSilverImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
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

  protected interface HasColorSource {
    Long getColorSource();
  }

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

  private static final String STYLE_ACTION_FILTER = STYLE_PREFIX + "actionFilter";
  private static final String STYLE_FILTER_LABEL = STYLE_PREFIX + "filterLabel";
  private static final String STYLE_ACTION_REMOVE_FILTER = STYLE_PREFIX + "actionRemoveFilter";

  private static final Color DEFAULT_ITEM_COLOR = new Color("yellow", "black");

  private final HeaderView headerView;
  private final Flow canvas;

  private final List<HandlerRegistration> registry = Lists.newArrayList();

  private boolean enabled = true;

  private final List<Color> colors = Lists.newArrayList();

  private BeeRowSet settings = null;

  private BeeRowSet countries = null;
  private int countryCodeIndex = BeeConst.UNDEF;
  private int countryNameIndex = BeeConst.UNDEF;

  private Range<JustDate> maxRange = null;
  private Range<JustDate> visibleRange = null;

  private boolean renderPending = false;

  private int headerHeight = 0;
  private int footerHeight = 0;

  private int chartLeft = 0;
  private int chartRight = 0;
  private int chartWidth = BeeConst.UNDEF;

  private int dayColumnWidth = BeeConst.UNDEF;
  private int rowHeight = BeeConst.UNDEF;

  private int sliderWidth = 0;

  private Widget startSliderLabel = null;
  private Widget endSliderLabel = null;

  private final EnumMap<RangeMover, Element> rangeMovers = Maps.newEnumMap(RangeMover.class);

  private String scrollAreaId = null;
  private String contentId = null;

  private int rowCount = 0;

  private final Set<String> relevantDataViews = Sets.newHashSet();

  private final List<ChartData> filterData = Lists.newArrayList();
  private final List<Integer> filteredIndexes = Lists.newArrayList();

  private final CustomDiv filterLabel;
  private final BeeImage removeFilter;

  protected ChartBase() {
    super();
    addStyleName(STYLE_CONTAINER);

    Set<Action> enabledActions = getEnabledActions();

    this.headerView = new HeaderSilverImpl();
    headerView.create(getCaption(), false, true, EnumSet.of(UiOption.ROOT), enabledActions,
        Action.NO_ACTIONS);

    if (BeeUtils.contains(enabledActions, Action.FILTER)) {
      BeeButton filter = new BeeButton("Filtras", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleAction(Action.FILTER);
        }
      });
      filter.addStyleName(STYLE_ACTION_FILTER);

      headerView.addCommandItem(filter);

      this.filterLabel = new CustomDiv(STYLE_FILTER_LABEL);
      headerView.addCommandItem(filterLabel);

      this.removeFilter = new BeeImage(Global.getImages().closeSmall(), new ScheduledCommand() {
        @Override
        public void execute() {
          handleAction(Action.REMOVE_FILTER);
        }
      });
      removeFilter.addStyleName(STYLE_ACTION_REMOVE_FILTER);
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

      case CLOSE:
        BeeKeeper.getScreen().closeWidget(this);
        break;

      case PRINT:
        Printer.print(this);
        break;

      case FILTER:
        FilterHelper.openDialog(filterData, new FilterHelper.DialogCallback() {
          @Override
          public void onSuccess(DialogBox result) {
            boolean ok = filter(result);
            if (ok) {
              refreshFilterInfo();
            } else {
              BeeKeeper.getScreen().notifyWarning("Nieko nerasta");
            }
          }
        });
        break;

      case REMOVE_FILTER:
        clearFilter();
        break;

      default:
        logger.info(getCaption(), action, "not implemented");
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
      ok = headerView.onPrint(source, target);

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

  protected void adjustWidths() {
    int chartColumnCount = ChartHelper.getSize(getVisibleRange());
    if (chartColumnCount > 0) {
      setDayColumnWidth(Math.max(getChartWidth() / chartColumnCount, 1));
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
    if (isFiltered()) {
      filteredIndexes.clear();

      updateMaxRange();
      render(true);
    }

    filterLabel.getElement().setInnerText(BeeConst.STRING_EMPTY);
    removeFilter.setVisible(false);

    for (ChartData data : filterData) {
      data.setSelected(false);
    }
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

  protected boolean filter(DialogBox dialog) {
    dialog.close();
    return false;
  }

  protected void finalizeContent(ComplexPanel panel) {
    renderMovers(panel, getContentHeight());
    renderRowResizer(panel);
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

  protected Color getColor(Long id) {
    if (id == null) {
      return null;
    }

    int index = ChartHelper.getColorIndex(id, colors.size());
    if (BeeUtils.isIndex(colors, index)) {
      return colors.get(index);
    } else {
      return DEFAULT_ITEM_COLOR;
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

  protected String getCountryFlag(Long countryId) {
    if (countryId == null || DataUtils.isEmpty(getCountries())) {
      return null;
    }
    
    BeeRow row = getCountries().getRowById(countryId);
    if (row == null) {
      return null;
    }

    String code = row.getString(getCountryCodeIndex());
    if (BeeUtils.isEmpty(code)) {
      return null;
    } else {
      return Flags.get(code);
    }
  }
  
  protected String getCountryLabel(Long countryId) {
    if (countryId == null || DataUtils.isEmpty(getCountries())) {
      return null;
    }

    BeeRow row = getCountries().getRowById(countryId);
    if (row == null) {
      return null;
    }

    String label = row.getString(getCountryCodeIndex());

    if (BeeUtils.isEmpty(label)) {
      return BeeUtils.trim(row.getString(getCountryNameIndex()));
    } else {
      return BeeUtils.trim(label).toUpperCase();
    }
  }

  protected abstract String getDataService();

  protected int getDayColumnWidth() {
    return dayColumnWidth;
  }

  protected abstract Set<Action> getEnabledActions();

  protected Widget getEndSliderLabel() {
    return endSliderLabel;
  }

  protected List<ChartData> getFilterData() {
    return filterData;
  }

  protected List<Integer> getFilteredIndexes() {
    return filteredIndexes;
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

  protected String getLoadingInfo(HasShipmentInfo item) {
    if (item == null) {
      return null;
    } else {
      return BeeUtils.joinWords(item.getLoadingDate(), getPlaceInfo(item.getLoadingCountry(),
          item.getLoadingPlace(), item.getLoadingTerminal()));
    }
  }

  protected String getPlaceInfo(Long countryId, String placeName, String terminal) {
    String countryLabel = getCountryLabel(countryId);

    if (BeeUtils.isEmpty(countryLabel) || BeeUtils.containsSame(placeName, countryLabel)
        || BeeUtils.containsSame(terminal, countryLabel)) {
      return BeeUtils.joinNoDuplicates(BeeConst.STRING_SPACE, placeName, terminal);
    } else {
      return BeeUtils.joinNoDuplicates(BeeConst.STRING_SPACE, countryLabel, placeName, terminal);
    }
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

  protected int getSliderWidth() {
    return sliderWidth;
  }

  protected Widget getStartSliderLabel() {
    return startSliderLabel;
  }

  protected abstract String getStripOpacityColumnName();

  protected abstract String getThemeColumnName();

  protected String getUnloadingInfo(HasShipmentInfo item) {
    if (item == null) {
      return null;
    } else {
      return BeeUtils.joinWords(item.getUnloadingDate(), getPlaceInfo(item.getUnloadingCountry(),
          item.getUnloadingPlace(), item.getUnloadingTerminal()));
    }
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

  /**
   * @param rowSet
   */
  protected void initData(BeeRowSet rowSet) {
  }

  protected List<ChartData> initFilter() {
    return Lists.newArrayList();
  }

  protected abstract Collection<? extends HasDateRange> initItems(SimpleRowSet data);

  protected boolean isDataEventRelevant(DataEvent event) {
    return event != null && relevantDataViews.contains(event.getViewName());
  }

  protected boolean isFiltered() {
    return !filteredIndexes.isEmpty();
  }

  protected void onCreate(ResponseObject response, Callback<IdentifiableWidget> callback) {
    if (setData(response)) {
      callback.onSuccess(this);
    } else {
      callback.onFailure(getCaption(), "negavo duomenų iš serverio", Global.CONSTANTS.sorry());
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

  protected abstract void prepareChart(Size canvasSize);

  protected void prepareDefaults(Size canvasSize) {
    setChartRight(DomUtils.getScrollBarWidth());

    int hh = ChartHelper.getPixels(getSettings(), getHeaderHeightColumnName(), 20);
    setHeaderHeight(clampHeaderHeight(hh, canvasSize.getHeight()));

    int fh = ChartHelper.getPixels(getSettings(), getFooterHeightColumnName(), 30);
    setFooterHeight(clampFooterHeight(fh, canvasSize.getHeight()));

    int rh = ChartHelper.getPixels(getSettings(), getRowHeightColumnName(), 20);
    setRowHeight(clampRowHeight(rh, canvasSize.getHeight()));
  }

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
        if (item instanceof HasColorSource) {
          setItemWidgetColor((HasColorSource) item, itemStrip);
        }
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
    BeeLabel startLabel = new BeeLabel();
    startLabel.addStyleName(STYLE_START_SLIDER_LABEL);
    StyleUtils.setBottom(startLabel, getFooterHeight());
    startLabel.setVisible(false);

    setStartSliderLabel(startLabel);
    canvas.add(startLabel);

    BeeLabel endLabel = new BeeLabel();
    endLabel.addStyleName(STYLE_END_SLIDER_LABEL);
    StyleUtils.setBottom(endLabel, getFooterHeight());
    endLabel.setVisible(false);

    setEndSliderLabel(endLabel);
    canvas.add(endLabel);
  }

  protected void renderVisibleRange(HasWidgets panel) {
    ChartHelper.renderVisibleRange(this, panel, getChartLeft(), getHeaderHeight());
  }

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
      setCountries(BeeRowSet.restore(serialized));
      setCountryCodeIndex(getCountries().getColumnIndex(CommonsConstants.COL_CODE));
      setCountryNameIndex(getCountries().getColumnIndex(CommonsConstants.COL_NAME));
    }

    serialized = rowSet.getTableProperty(PROP_COLORS);
    if (!BeeUtils.isEmpty(serialized)) {
      restoreColors(serialized);
    }

    serialized = rowSet.getTableProperty(PROP_DATA);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet data = SimpleRowSet.restore(serialized);

      Collection<? extends HasDateRange> items = initItems(data);
      if (items != null) {
        logger.debug(getCaption(), "loaded", items.size(), "items");
        setMaxRange(items);
      }
    }

    initData(rowSet);

    updateFilterData(initFilter());

    return true;
  }

  protected void setDayColumnWidth(int dayColumnWidth) {
    this.dayColumnWidth = dayColumnWidth;
  }

  protected void setEndSliderLabel(Widget endSliderLabel) {
    this.endSliderLabel = endSliderLabel;
  }

  protected void setFooterHeight(int footerHeight) {
    this.footerHeight = footerHeight;
  }

  protected void setHeaderHeight(int headerHeight) {
    this.headerHeight = headerHeight;
  }

  protected void setItemWidgetColor(HasColorSource item, Widget widget) {
    Color color = getColor(item.getColorSource());
    if (color == null) {
      return;
    }

    if (!BeeUtils.isEmpty(color.getBackground())) {
      StyleUtils.setBackgroundColor(widget, color.getBackground());
    }
    if (!BeeUtils.isEmpty(color.getForeground())) {
      StyleUtils.setColor(widget, color.getForeground());
    }
  }

  protected void setRelevantDataViews(String... viewNames) {
    relevantDataViews.clear();

    if (viewNames != null) {
      for (String viewName : viewNames) {
        if (!BeeUtils.isEmpty(viewName)) {
          relevantDataViews.add(viewName);
        }
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

  protected void updateFilteredIndexes(List<Integer> indexes) {
    if (!BeeUtils.sameElements(indexes, filteredIndexes)) {
      filteredIndexes.clear();
      filteredIndexes.addAll(indexes);

      updateMaxRange();
      render(true);
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
        newValues, new RowCallback() {
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

  private List<List<HasDateRange>> doStripLayout(Collection<? extends HasDateRange> chartItems,
      int maxRows) {
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

  private BeeRowSet getCountries() {
    return countries;
  }

  private int getCountryCodeIndex() {
    return countryCodeIndex;
  }

  private int getCountryNameIndex() {
    return countryNameIndex;
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
    List<String> selection = Lists.newArrayList();
    for (ChartData data : filterData) {
      Collection<String> selectedNames = data.getSelectedNames();
      if (!selectedNames.isEmpty() && selectedNames.size() < data.size()) {
        selection.addAll(selectedNames);
      }
    }

    if (selection.isEmpty()) {
      filterLabel.getElement().setInnerText(BeeConst.STRING_EMPTY);
      removeFilter.setVisible(false);
    } else {
      filterLabel.getElement().setInnerText(BeeUtils.join(BeeConst.STRING_COMMA, selection));
      removeFilter.setVisible(true);
    }
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

  private void setCountries(BeeRowSet countries) {
    this.countries = countries;
  }

  private void setCountryCodeIndex(int countryCodeIndex) {
    this.countryCodeIndex = countryCodeIndex;
  }

  private void setCountryNameIndex(int countryNameIndex) {
    this.countryNameIndex = countryNameIndex;
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
  
  private void updateColorTheme(Long theme) {
    ParameterList args = TransportHandler.createArgs(SVC_GET_COLORS);
    if (theme != null) {
      args.addQueryItem(VAR_THEME_ID, theme);
    }

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        restoreColors((String) response.getResponse());
        render(false);
      }
    });
  }

  private void updateFilterData(List<ChartData> newData) {
    if (BeeUtils.isEmpty(newData)) {
      filterData.clear();

    } else if (filterData.isEmpty()) {
      filterData.addAll(newData);

    } else {
      for (ChartData ocd : filterData) {
        Collection<String> selectedNames = ocd.getSelectedNames();
        if (selectedNames.isEmpty()) {
          break;
        }

        for (ChartData ncd : newData) {
          if (ncd.getType().equals(ocd.getType())) {
            if (!ncd.isEmpty()) {
              for (String name : selectedNames) {
                ncd.setSelected(name, true);
              }
            }
            break;
          }
        }
      }

      filterData.clear();
      filterData.addAll(newData);
    }
  }
}
