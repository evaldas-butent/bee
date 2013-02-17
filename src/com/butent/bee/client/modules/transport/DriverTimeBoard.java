package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DriverTimeBoard extends ChartBase {

  private static class Absence implements HasDateRange {

    private final String name;
    private final String label;

    private final String background;
    private final String foreground;

    private final String notes;

    private final Range<JustDate> range;

    private Absence(JustDate dateFrom, JustDate dateTo, String name, String label,
        String background, String foreground, String notes) {
      super();

      this.name = name;
      this.label = label;

      this.background = background;
      this.foreground = foreground;

      this.notes = notes;

      this.range = Range.closed(dateFrom, BeeUtils.max(dateFrom, dateTo));
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }
  }

  private static class Driver implements HasDateRange {
    private static final JustDate DEFAULT_START_DATE = new JustDate(TimeUtils.year() - 10, 1, 1);
    private static final JustDate DEFAULT_END_DATE = TimeUtils.endOfYear(TimeUtils.year(), 10);

    private final Long id;
    private final String name;

    private final JustDate startDate;
    private final JustDate endDate;

    private final Range<JustDate> range;

    private Driver(Long driverId, String firstName, String lastName, JustDate startDate,
        JustDate endDate) {
      super();
      this.id = driverId;
      this.name = BeeUtils.joinWords(firstName, lastName);

      this.startDate = startDate;
      this.endDate = endDate;

      JustDate start = BeeUtils.nvl(startDate, DEFAULT_START_DATE);
      JustDate end = BeeUtils.nvl(endDate, DEFAULT_END_DATE);

      this.range = Range.closed(start, BeeUtils.max(start, end));
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }
  }

  private static class DriverTrip implements ChartItem {

    private final JustDate driverDateFrom;
    private final JustDate driverDateTo;

    private final Long tripId;
    private final DateTime tripDate;
    private final String tripNo;

    private final JustDate tripPlannedEndDate;
    private final JustDate tripDateFrom;
    private final JustDate tripDateTo;

    private final String vehicleNumber;
    private final String trailerNumber;

    private final JustDate loadingDate;
    private final JustDate unloadingDate;

    private final Range<JustDate> range;

    private DriverTrip(SimpleRow row) {
      super();

      this.driverDateFrom = row.getDate(ALS_TRIP_DRIVER_FROM);
      this.driverDateTo = row.getDate(ALS_TRIP_DRIVER_TO);

      this.tripId = row.getLong(COL_TRIP);
      this.tripDate = row.getDateTime(ALS_TRIP_DATE);
      this.tripNo = row.getValue(COL_TRIP_NO);

      this.tripPlannedEndDate = row.getDate(COL_TRIP_PLANNED_END_DATE);
      this.tripDateFrom = row.getDate(ALS_TRIP_DATE_FROM);
      this.tripDateTo = row.getDate(ALS_TRIP_DATE_TO);

      this.vehicleNumber = row.getValue(ALS_VEHICLE_NUMBER);
      this.trailerNumber = row.getValue(ALS_TRAILER_NUMBER);

      this.loadingDate = BeeUtils.min(row.getDate(loadingColumnAlias(COL_PLACE_DATE)),
          row.getDate(defaultLoadingColumnAlias(COL_PLACE_DATE)));
      this.unloadingDate = BeeUtils.max(row.getDate(unloadingColumnAlias(COL_PLACE_DATE)),
          row.getDate(defaultUnloadingColumnAlias(COL_PLACE_DATE)));

      JustDate start = BeeUtils.nvl(driverDateFrom, tripDateFrom, loadingDate, tripDate.getDate());
      JustDate end = BeeUtils.nvl(driverDateTo, tripDateTo, tripPlannedEndDate, unloadingDate);

      this.range = Range.closed(start, BeeUtils.max(start, end));
    }

    @Override
    public Long getColorSource() {
      return null;
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }
  }

  private static class LayoutRow {
    private final int driverIndex;
    private final List<HasDateRange> items;

    private LayoutRow(int driverIndex, List<HasDateRange> items) {
      this.driverIndex = driverIndex;
      this.items = items;
    }
  }

  static final String SUPPLIER_KEY = "driver_time_board";
  private static final String DATA_SERVICE = SVC_GET_DTB_DATA;

  private static final String STYLE_PREFIX = "bee-tr-dtb-";

  private static final String STYLE_DRIVER_PREFIX = STYLE_PREFIX + "Driver-";
  private static final String STYLE_DRIVER_ROW_SEPARATOR = STYLE_DRIVER_PREFIX + "row-sep";
  private static final String STYLE_DRIVER_PANEL = STYLE_DRIVER_PREFIX + "panel";
  private static final String STYLE_DRIVER_LABEL = STYLE_DRIVER_PREFIX + "label";

  private static final String STYLE_TRIP_PREFIX = STYLE_PREFIX + "Trip-";
  private static final String STYLE_TRIP_PANEL = STYLE_TRIP_PREFIX + "panel";

  private static final String STYLE_ABSENCE_PREFIX = STYLE_PREFIX + "Absence-";
  private static final String STYLE_ABSENCE_PANEL = STYLE_ABSENCE_PREFIX + "panel";
  private static final String STYLE_ABSENCE_LABEL = STYLE_ABSENCE_PREFIX + "label";

  static void open(final Callback<IdentifiableWidget> callback) {
    Assert.notNull(callback);

    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(DATA_SERVICE),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            DriverTimeBoard dtb = new DriverTimeBoard();
            if (dtb.setData(response)) {
              callback.onSuccess(dtb);
            } else {
              callback.onFailure(dtb.getCaption(), "negavo duomenų iš serverio",
                  Global.CONSTANTS.sorry());
            }
          }
        });
  }

  private final List<Driver> drivers = Lists.newArrayList();

  private final Map<Long, List<DriverTrip>> driverTrips = Maps.newHashMap();
  private final Map<Long, List<Absence>> driverAbsence = Maps.newHashMap();

  private int driverWidth = BeeConst.UNDEF;

  private DriverTimeBoard() {
    super();
    addStyleName(STYLE_PREFIX + "View");

    setRelevantDataViews(VIEW_DRIVERS, VIEW_DRIVER_ABSENCE, VIEW_ABSENCE_TYPES, VIEW_TRIP_DRIVERS,
        VIEW_TRIPS, VIEW_VEHICLES, VIEW_CARGO, VIEW_CARGO_TRIPS, VIEW_TRIP_CARGO,
        CommonsConstants.VIEW_COLORS);
  }

  @Override
  public String getCaption() {
    return "Vairuotojų užimtumas";
  }

  @Override
  public String getIdPrefix() {
    return "tr-dtb";
  }

  @Override
  public String getSupplierKey() {
    return SUPPLIER_KEY;
  }

  @Override
  protected String getBarHeightColumnName() {
    return COL_DTB_BAR_HEIGHT;
  }

  @Override
  protected Collection<? extends ChartItem> getChartItems() {
    List<DriverTrip> result = Lists.newArrayList();

    for (List<DriverTrip> trips : driverTrips.values()) {
      result.addAll(trips);
    }

    return result;
  }

  @Override
  protected String getDataService() {
    return DATA_SERVICE;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.CONFIGURE);
  }

  @Override
  protected String getFooterHeightColumnName() {
    return COL_DTB_FOOTER_HEIGHT;
  }

  @Override
  protected String getHeaderHeightColumnName() {
    return COL_DTB_HEADER_HEIGHT;
  }

  @Override
  protected String getSettingsFormName() {
    return FORM_DTB_SETTINGS;
  }

  @Override
  protected String getSliderWidthColumnName() {
    return COL_DTB_SLIDER_WIDTH;
  }

  @Override
  protected String getStripOpacityColumnName() {
    return COL_DTB_STRIP_OPACITY;
  }

  @Override
  protected String getThemeColumnName() {
    return null;
  }

  @Override
  protected void initData(BeeRowSet rowSet) {
    drivers.clear();
    driverAbsence.clear();

    if (rowSet == null) {
      return;
    }

    String serialized = rowSet.getTableProperty(PROP_DRIVERS);
    if (!BeeUtils.isEmpty(serialized)) {
      BeeRowSet brs = BeeRowSet.restore(serialized);

      if (!DataUtils.isEmpty(brs)) {
        int firstNameIndex = brs.getColumnIndex(CommonsConstants.COL_FIRST_NAME);
        int lastNameIndex = brs.getColumnIndex(CommonsConstants.COL_LAST_NAME);

        int startDateIndex = brs.getColumnIndex(COL_DRIVER_START_DATE);
        int endDateIndex = brs.getColumnIndex(COL_DRIVER_END_DATE);

        for (BeeRow row : brs.getRows()) {
          drivers.add(new Driver(row.getId(),
              row.getString(firstNameIndex), row.getString(lastNameIndex),
              row.getDate(startDateIndex), row.getDate(endDateIndex)));
        }
      }
    }

    serialized = rowSet.getTableProperty(PROP_ABSENCE);
    if (!BeeUtils.isEmpty(serialized)) {
      BeeRowSet brs = BeeRowSet.restore(serialized);

      if (!DataUtils.isEmpty(brs)) {
        int driverIndex = brs.getColumnIndex(COL_DRIVER);

        int dateFromIndex = brs.getColumnIndex(COL_ABSENCE_FROM);
        int dateToIndex = brs.getColumnIndex(COL_ABSENCE_TO);

        int nameIndex = brs.getColumnIndex(ALS_ABSENCE_NAME);
        int labelIndex = brs.getColumnIndex(ALS_ABSENCE_LABEL);

        int bgIndex = brs.getColumnIndex(CommonsConstants.COL_BACKGROUND);
        int fgIndex = brs.getColumnIndex(CommonsConstants.COL_FOREGROUND);

        int notesIndex = brs.getColumnIndex(COL_ABSENCE_NOTES);

        JustDate min = null;
        JustDate max = null;

        for (BeeRow row : brs.getRows()) {
          Long driver = row.getLong(driverIndex);
          JustDate dateFrom = row.getDate(dateFromIndex);
          if (!DataUtils.isId(driver) || dateFrom == null) {
            continue;
          }

          Absence da = new Absence(dateFrom, row.getDate(dateToIndex),
              row.getString(nameIndex), row.getString(labelIndex),
              row.getString(bgIndex), row.getString(fgIndex), row.getString(notesIndex));

          if (driverAbsence.containsKey(driver)) {
            driverAbsence.get(driver).add(da);
          } else {
            driverAbsence.put(driver, Lists.newArrayList(da));
          }

          if (min == null || TimeUtils.isLess(da.getRange().lowerEndpoint(), min)) {
            min = da.getRange().lowerEndpoint();
          }

          if (max == null || TimeUtils.isMore(da.getRange().upperEndpoint(), max)) {
            max = da.getRange().upperEndpoint();
          }
        }

        extendMaxRange(min, max);
      }
    }
  }

  @Override
  protected Collection<? extends ChartItem> initItems(SimpleRowSet data) {
    driverTrips.clear();

    for (SimpleRow row : data) {
      Long driver = row.getLong(COL_DRIVER);

      if (DataUtils.isId(driver)) {
        DriverTrip dt = new DriverTrip(row);

        if (driverTrips.containsKey(driver)) {
          driverTrips.get(driver).add(dt);
        } else {
          driverTrips.put(driver, Lists.newArrayList(dt));
        }
      }
    }

    return getChartItems();
  }

  @Override
  protected void prepareChart(Size canvasSize) {
    setDriverWidth(ChartHelper.getPixels(getSettings(), COL_DTB_PIXELS_PER_DRIVER, 140,
        ChartHelper.DEFAULT_MOVER_WIDTH + 1, canvasSize.getWidth() / 3));

    setChartLeft(getDriverWidth());
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());

    setDayColumnWidth(ChartHelper.getPixels(getSettings(), COL_DTB_PIXELS_PER_DAY, 20,
        1, getChartWidth()));

    setRowHeight(ChartHelper.getPixels(getSettings(), COL_DTB_PIXELS_PER_ROW, 20,
        1, getScrollAreaHeight(canvasSize.getHeight()) / 2));
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    List<LayoutRow> layoutRows = doLayout();
    if (layoutRows.isEmpty()) {
      return;
    }

    int height = layoutRows.size() * getRowHeight();
    StyleUtils.setHeight(panel, height);

    ChartHelper.renderDayColumns(panel, getVisibleRange(), getChartLeft(), getDayColumnWidth(),
        height);

    JustDate firstDate = getVisibleRange().lowerEndpoint();
    JustDate lastDate = getVisibleRange().upperEndpoint();

    int calendarWidth = getCalendarWidth();

    int lastDriverIndex = BeeConst.UNDEF;

    IdentifiableWidget driverWidget = null;
    int driverStartRow = 0;

    Double itemOpacity = ChartHelper.getOpacity(getSettings(), COL_DTB_ITEM_OPACITY);

    for (int row = 0; row < layoutRows.size(); row++) {
      int driverIndex = layoutRows.get(row).driverIndex;
      List<HasDateRange> rowItems = layoutRows.get(row).items;

      int top = row * getRowHeight();

      if (row == 0) {
        lastDriverIndex = driverIndex;

        driverWidget = createDriverWidget(drivers.get(driverIndex));
        driverStartRow = row;

      } else if (driverIndex == lastDriverIndex) {
        ChartHelper.addRowSeparator(panel, top, getChartLeft(), calendarWidth);

      } else {
        lastDriverIndex = driverIndex;

        addDriverWidget(panel, driverWidget, driverStartRow, row - 1);

        driverWidget = createDriverWidget(drivers.get(driverIndex));
        driverStartRow = row;

        ChartHelper.addRowSeparator(panel, STYLE_DRIVER_ROW_SEPARATOR, top, 0,
            getDriverWidth() + calendarWidth);
      }

      for (HasDateRange item : rowItems) {
        JustDate start = TimeUtils.clamp(item.getRange().lowerEndpoint(), firstDate, lastDate);
        JustDate end = TimeUtils.clamp(item.getRange().upperEndpoint(), firstDate, lastDate);

        int left = getChartLeft() + TimeUtils.dayDiff(firstDate, start) * getDayColumnWidth();
        int width = (TimeUtils.dayDiff(start, end) + 1) * getDayColumnWidth();

        Rectangle rectangle = new Rectangle(left, top, width,
            getRowHeight() - ChartHelper.ROW_SEPARATOR_HEIGHT);

        Widget itemWidget;

        if (item instanceof DriverTrip) {
          itemWidget = createTripWidget((DriverTrip) item);
        } else if (item instanceof Absence) {
          itemWidget = createAbsenceWidget((Absence) item);
        } else {
          itemWidget = null;
        }

        if (itemWidget != null) {
          rectangle.applyTo(itemWidget);
          if (itemOpacity != null) {
            StyleUtils.setOpacity(itemWidget, itemOpacity);
          }

          panel.add(itemWidget);
        }
      }
    }

    if (driverWidget != null) {
      addDriverWidget(panel, driverWidget, driverStartRow, layoutRows.size() - 1);
    }

    ChartHelper.addBottomSeparator(panel, height, 0, getChartLeft() + calendarWidth);

    renderMovers(panel, height);
  }

  private void addDriverWidget(HasWidgets panel, IdentifiableWidget widget,
      int firstRow, int lastRow) {

    Rectangle rectangle = ChartHelper.getLegendRectangle(0, getDriverWidth(),
        firstRow, lastRow, getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget.asWidget(), rectangle, margins);
    panel.add(widget.asWidget());
  }

  private String buildTripTitle(DriverTrip item) {
    return ChartHelper.buildTitle("Reiso Nr.", item.tripNo,
        "Vilkikas", item.vehicleNumber,
        "Puspriekabė", item.trailerNumber);
  }

  private Widget createAbsenceWidget(Absence da) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_ABSENCE_PANEL);

    panel.setTitle(BeeUtils.buildLines(ChartHelper.getRangeLabel(da.getRange()), da.name,
        da.notes));

    if (!BeeUtils.isEmpty(da.background)) {
      StyleUtils.setBackgroundColor(panel, da.background);
    }
    if (!BeeUtils.isEmpty(da.foreground)) {
      StyleUtils.setColor(panel, da.foreground);
    }

    if (!BeeUtils.isEmpty(da.label)) {
      BeeLabel label = new BeeLabel(da.label);
      label.addStyleName(STYLE_ABSENCE_LABEL);
      panel.add(label);
    }

    return panel;
  }

  private IdentifiableWidget createDriverWidget(Driver driver) {
    Simple panel = new Simple();
    panel.addStyleName(STYLE_DRIVER_PANEL);

    final Long driverId = driver.id;

    BeeLabel widget = new BeeLabel(driver.name);
    widget.addStyleName(STYLE_DRIVER_LABEL);

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_DRIVERS, driverId);
      }
    });

    panel.add(widget);

    return panel;
  }

  private Widget createTripWidget(DriverTrip item) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_TRIP_PANEL);

    panel.setTitle(buildTripTitle(item));

    final Long tripId = item.tripId;

    panel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_TRIPS, tripId);
      }
    });

    return panel;
  }

  private List<LayoutRow> doLayout() {
    List<LayoutRow> rows = Lists.newArrayList();

    for (int driverIndex = 0; driverIndex < drivers.size(); driverIndex++) {
      Long driverId = drivers.get(driverIndex).id;

      List<HasDateRange> items = Lists.newArrayList();

      if (driverTrips.containsKey(driverId)) {
        for (DriverTrip dtItem : driverTrips.get(driverId)) {
          if (BeeUtils.intersects(getVisibleRange(), dtItem.getRange())) {
            if (ChartHelper.intersects(items, dtItem.getRange())) {
              rows.add(new LayoutRow(driverIndex, Lists.newArrayList(items)));
              items.clear();
            }

            items.add(dtItem);
          }
        }
      }

      if (driverAbsence.containsKey(driverId)) {
        for (Absence daItem : driverAbsence.get(driverId)) {
          if (BeeUtils.intersects(getVisibleRange(), daItem.getRange())) {
            if (ChartHelper.intersects(items, daItem.getRange())) {
              rows.add(new LayoutRow(driverIndex, Lists.newArrayList(items)));
              items.clear();
            }

            items.add(daItem);
          }
        }
      }

      if (!items.isEmpty()) {
        rows.add(new LayoutRow(driverIndex, Lists.newArrayList(items)));
      }
    }
    return rows;
  }

  private int getDriverWidth() {
    return driverWidth;
  }

  private void onDriverResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft = 300;
    if (getChartWidth() > 0) {
      maxLeft = Math.min(maxLeft, getChartLeft() + getChartWidth() / 2);
    }

    int newLeft = BeeUtils.clamp(oldLeft + delta, 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);
      }

      int px = newLeft + ChartHelper.DEFAULT_MOVER_WIDTH;
      if (event.isFinished() && updateSetting(COL_DTB_PIXELS_PER_DRIVER, px)) {
        setDriverWidth(px);
        render(false);
      }
    }
  }

  private void renderMovers(HasWidgets panel, int height) {
    Mover driverMover = ChartHelper.createHorizontalMover();
    StyleUtils.setLeft(driverMover, getChartLeft() - ChartHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(driverMover, height);

    driverMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onDriverResize(event);
      }
    });

    panel.add(driverMover);
  }

  private void setDriverWidth(int driverWidth) {
    this.driverWidth = driverWidth;
  }
}
