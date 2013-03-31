package com.butent.bee.client.modules.transport.charts;

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
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.DndDiv;
import com.butent.bee.client.widget.Mover;
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

  private static class DriverTrip implements HasDateRange {

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
    public Range<JustDate> getRange() {
      return range;
    }
  }

  static final String SUPPLIER_KEY = "driver_time_board";
  private static final String DATA_SERVICE = SVC_GET_DTB_DATA;

  private static final String STYLE_PREFIX = "bee-tr-dtb-";

  private static final String STYLE_DRIVER_PREFIX = STYLE_PREFIX + "Driver-";
  private static final String STYLE_DRIVER_ROW_SEPARATOR = STYLE_DRIVER_PREFIX + "row-sep";
  private static final String STYLE_DRIVER_PANEL = STYLE_DRIVER_PREFIX + "panel";
  private static final String STYLE_DRIVER_LABEL = STYLE_DRIVER_PREFIX + "label";
  private static final String STYLE_DRIVER_OVERLAP = STYLE_DRIVER_PREFIX + "overlap";
  private static final String STYLE_DRIVER_DRAG = STYLE_DRIVER_PREFIX + "drag";

  private static final String STYLE_TRIP_PREFIX = STYLE_PREFIX + "Trip-";
  private static final String STYLE_TRIP_PANEL = STYLE_TRIP_PREFIX + "panel";

  private static final String STYLE_ABSENCE_PREFIX = STYLE_PREFIX + "Absence-";
  private static final String STYLE_ABSENCE_PANEL = STYLE_ABSENCE_PREFIX + "panel";
  private static final String STYLE_ABSENCE_LABEL = STYLE_ABSENCE_PREFIX + "label";

  private static final String STYLE_INACTIVE = STYLE_PREFIX + "Inactive";
  private static final String STYLE_OVERLAP = STYLE_PREFIX + "Overlap";

  static void open(final Callback<IdentifiableWidget> callback) {
    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(DATA_SERVICE),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            DriverTimeBoard dtb = new DriverTimeBoard();
            dtb.onCreate(response, callback);
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
        VIEW_TRIPS, VIEW_VEHICLES, VIEW_ORDER_CARGO, VIEW_CARGO_TRIPS, VIEW_TRIP_CARGO,
        CommonsConstants.VIEW_COLORS);
  }

  @Override
  public String getCaption() {
    return Global.CONSTANTS.driverTimeBoard();
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
  public void handleAction(Action action) {
    if (Action.ADD.equals(action)) {
      RowFactory.createRow(VIEW_DRIVERS);
    } else {
      super.handleAction(action);
    }
  }

  @Override
  protected Collection<? extends HasDateRange> getChartItems() {
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
    return EnumSet.of(Action.REFRESH, Action.ADD, Action.CONFIGURE);
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
  protected String getRowHeightColumnName() {
    return COL_DTB_PIXELS_PER_ROW;
  }

  @Override
  protected String getSettingsFormName() {
    return FORM_DTB_SETTINGS;
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
  protected Collection<? extends HasDateRange> initItems(SimpleRowSet data) {
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
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    List<ChartRowLayout> driverLayout = doLayout();

    int rc = ChartRowLayout.countRows(driverLayout, 1);
    initContent(panel, rc);

    if (driverLayout.isEmpty()) {
      return;
    }

    int calendarWidth = getCalendarWidth();

    Double opacity = ChartHelper.getOpacity(getSettings(), COL_DTB_ITEM_OPACITY);

    Edges margins = new Edges();
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    Widget driverWidget;
    Widget offWidget;
    Widget itemWidget;
    Widget overlapWidget;

    int rowIndex = 0;
    for (ChartRowLayout layout : driverLayout) {

      int size = layout.getSize(1);
      int top = rowIndex * getRowHeight();

      if (rowIndex > 0) {
        ChartHelper.addRowSeparator(panel, STYLE_DRIVER_ROW_SEPARATOR, top, 0,
            getChartLeft() + calendarWidth);
      }

      driverWidget = createDriverWidget(drivers.get(layout.getDataIndex()), layout.hasOverlap());
      addDriverWidget(panel, driverWidget, rowIndex, rowIndex + size - 1);

      for (int i = 1; i < size; i++) {
        ChartHelper.addRowSeparator(panel, top + getRowHeight() * i, getChartLeft(), calendarWidth);
      }

      for (HasDateRange item : layout.getInactivity()) {
        offWidget = new CustomDiv(STYLE_INACTIVE);

        Rectangle rectangle = getRectangle(item.getRange(), rowIndex, rowIndex + size - 1);
        ChartHelper.apply(offWidget, rectangle, margins);

        panel.add(offWidget);
      }

      for (int i = 0; i < layout.getRows().size(); i++) {
        for (HasDateRange item : layout.getRows().get(i).getRowItems()) {

          if (item instanceof DriverTrip) {
            itemWidget = createTripWidget((DriverTrip) item);
          } else if (item instanceof Absence) {
            itemWidget = createAbsenceWidget((Absence) item);
          } else {
            itemWidget = null;
          }

          if (itemWidget != null) {
            Rectangle rectangle = getRectangle(item.getRange(), rowIndex + i);
            ChartHelper.apply(itemWidget, rectangle, margins);
            if (opacity != null) {
              StyleUtils.setOpacity(itemWidget, opacity);
            }

            panel.add(itemWidget);
          }

          Set<Range<JustDate>> overlap = layout.getOverlap(item.getRange());

          for (Range<JustDate> over : overlap) {
            overlapWidget = new CustomDiv(STYLE_OVERLAP);

            Rectangle rectangle = getRectangle(over, rowIndex + i);
            ChartHelper.apply(overlapWidget, rectangle, margins);

            panel.add(overlapWidget);
          }
        }
      }

      rowIndex += size;
    }
  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
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

  private void addDriverWidget(HasWidgets panel, Widget widget, int firstRow, int lastRow) {
    Rectangle rectangle = ChartHelper.getRectangle(0, getDriverWidth(), firstRow, lastRow,
        getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget, rectangle, margins);
    panel.add(widget);
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
      Range<JustDate> range = ChartHelper.normalizedIntersection(da.getRange(), getVisibleRange());
      int dayCount = ChartHelper.getSize(range);

      int dayWidth = getDayColumnWidth();
      int panelWidth = dayCount * dayWidth;

      Size labelSize = Rulers.getLineSize(null, da.label.trim(), false);
      int labelWidth = labelSize.getWidth();
      int labelTop = Math.max((getRowHeight() - labelSize.getHeight()) / 2, 0);

      int incr = (labelWidth % dayWidth > 0) ? 1 : 0;
      int step = Math.min(panelWidth, (labelWidth / dayWidth + incr) * dayWidth);

      if (step > 0) {
        for (int x = 0; x <= panelWidth - step; x += step) {
          CustomDiv label = new CustomDiv(STYLE_ABSENCE_LABEL);
          label.setText(da.label);

          StyleUtils.setLeft(label, x);
          StyleUtils.setWidth(label, step);
          StyleUtils.setTop(label, labelTop);

          panel.add(label);
        }
      }
    }

    return panel;
  }

  private Widget createDriverWidget(Driver driver, boolean hasOverlap) {
    Simple panel = new Simple();
    panel.addStyleName(STYLE_DRIVER_PANEL);
    if (hasOverlap) {
      panel.addStyleName(STYLE_DRIVER_OVERLAP);
    }

    final Long driverId = driver.getId();

    DndDiv widget = new DndDiv(STYLE_DRIVER_LABEL);
    widget.setText(driver.getItemName());

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_DRIVERS, driverId);
      }
    });

    DndHelper.makeSource(widget, DATA_TYPE_DRIVER, driver, STYLE_DRIVER_DRAG, true);

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

  private List<ChartRowLayout> doLayout() {
    List<ChartRowLayout> result = Lists.newArrayList();
    Range<JustDate> range = getVisibleRange();

    for (int driverIndex = 0; driverIndex < drivers.size(); driverIndex++) {
      Driver driver = drivers.get(driverIndex);
      Long driverId = driver.getId();

      if (ChartHelper.isActive(driver, range)) {
        ChartRowLayout layout = new ChartRowLayout(driverIndex);

        layout.addItems(driverId, getTrips(driverId, range), range);
        layout.addItems(driverId, getAbsence(driverId, range), range);

        layout.addInactivity(ChartHelper.getInactivity(driver, range), range);

        result.add(layout);
      }
    }

    return result;
  }

  private List<HasDateRange> getAbsence(long driverId, Range<JustDate> range) {
    List<HasDateRange> absence = Lists.newArrayList();

    if (driverAbsence.containsKey(driverId)) {
      absence.addAll(ChartHelper.getActiveItems(driverAbsence.get(driverId), range));
    }
    return absence;
  }

  private int getDriverWidth() {
    return driverWidth;
  }

  private List<HasDateRange> getTrips(long driverId, Range<JustDate> range) {
    List<HasDateRange> trips = Lists.newArrayList();

    if (driverTrips.containsKey(driverId)) {
      trips.addAll(ChartHelper.getActiveItems(driverTrips.get(driverId), range));
    }
    return trips;
  }

  private void onDriverResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft = getLastResizableColumnMaxLeft(0);
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

  private void setDriverWidth(int driverWidth) {
    this.driverWidth = driverWidth;
  }
}
