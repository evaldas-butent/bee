package com.butent.bee.client.modules.transport;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
abstract class VehicleTimeBoard extends ChartBase {

  private static class CargoHandling implements HasDateRange {
    private final Long cargoId;

    private final JustDate loadingDate;
    private final Long loadingCountry;
    private final String loadingPlace;
    private final String loadingTerminal;

    private final JustDate unloadingDate;
    private final Long unloadingCountry;
    private final String unloadingPlace;
    private final String unloadingTerminal;

    private final String notes;

    private final Range<JustDate> range;

    private CargoHandling(SimpleRow row) {
      this.cargoId = row.getLong(COL_CARGO);

      this.loadingDate = row.getDate(loadingColumnAlias(COL_PLACE_DATE));
      this.loadingCountry = row.getLong(loadingColumnAlias(COL_COUNTRY));
      this.loadingPlace = row.getValue(loadingColumnAlias(COL_PLACE_NAME));
      this.loadingTerminal = row.getValue(loadingColumnAlias(COL_TERMINAL));

      this.unloadingDate = row.getDate(unloadingColumnAlias(COL_PLACE_DATE));
      this.unloadingCountry = row.getLong(unloadingColumnAlias(COL_COUNTRY));
      this.unloadingPlace = row.getValue(unloadingColumnAlias(COL_PLACE_NAME));
      this.unloadingTerminal = row.getValue(unloadingColumnAlias(COL_TERMINAL));

      this.notes = row.getValue(COL_CARGO_HANDLING_NOTES);

      this.range = ChartHelper.getActivity(this.loadingDate, this.unloadingDate);
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }
  }

  private static class Freight implements HasDateRange, HasColorSource {
    private final Long tripId;

    private final Long cargoTripId;
    private final Long cargoTripVersion;

    private final Long cargoId;
    private final String cargoDescription;

    private final JustDate loadingDate;
    private final Long loadingCountry;
    private final String loadingPlace;
    private final String loadingTerminal;

    private final JustDate unloadingDate;
    private final Long unloadingCountry;
    private final String unloadingPlace;
    private final String unloadingTerminal;

    private final String orderNo;
    private final String customerName;

    private final Range<JustDate> range;

    private Freight(SimpleRow row) {
      this.tripId = row.getLong(COL_TRIP_ID);

      this.cargoTripId = row.getLong(COL_CARGO_TRIP_ID);
      this.cargoTripVersion = row.getLong(ALS_CARGO_TRIP_VERSION);

      this.cargoId = row.getLong(COL_CARGO);
      this.cargoDescription = row.getValue(COL_DESCRIPTION);

      this.loadingDate = BeeUtils.nvl(row.getDate(loadingColumnAlias(COL_PLACE_DATE)),
          row.getDate(defaultLoadingColumnAlias(COL_PLACE_DATE)));
      this.loadingCountry = BeeUtils.nvl(row.getLong(loadingColumnAlias(COL_COUNTRY)),
          row.getLong(defaultLoadingColumnAlias(COL_COUNTRY)));
      this.loadingPlace = BeeUtils.nvl(row.getValue(loadingColumnAlias(COL_PLACE_NAME)),
          row.getValue(defaultLoadingColumnAlias(COL_PLACE_NAME)));
      this.loadingTerminal = BeeUtils.nvl(row.getValue(loadingColumnAlias(COL_TERMINAL)),
          row.getValue(defaultLoadingColumnAlias(COL_TERMINAL)));

      this.unloadingDate = BeeUtils.nvl(row.getDate(unloadingColumnAlias(COL_PLACE_DATE)),
          row.getDate(defaultUnloadingColumnAlias(COL_PLACE_DATE)));
      this.unloadingCountry = BeeUtils.nvl(row.getLong(unloadingColumnAlias(COL_COUNTRY)),
          row.getLong(defaultUnloadingColumnAlias(COL_COUNTRY)));
      this.unloadingPlace = BeeUtils.nvl(row.getValue(unloadingColumnAlias(COL_PLACE_NAME)),
          row.getValue(defaultUnloadingColumnAlias(COL_PLACE_NAME)));
      this.unloadingTerminal = BeeUtils.nvl(row.getValue(unloadingColumnAlias(COL_TERMINAL)),
          row.getValue(defaultUnloadingColumnAlias(COL_TERMINAL)));

      this.orderNo = row.getValue(COL_ORDER_NO);
      this.customerName = row.getValue(COL_CUSTOMER_NAME);

      this.range = ChartHelper.getActivity(this.loadingDate, this.unloadingDate);
    }

    @Override
    public Long getColorSource() {
      return tripId;
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }
  }

  private static class Trip implements HasDateRange, HasColorSource {
    private final Long tripId;
    private final String tripNo;

    private final DateTime date;
    private final JustDate plannedEndDate;
    private final JustDate dateFrom;
    private final JustDate dateTo;

    private final Long truckId;
    private final String truckNumber;
    private final Long trailerId;
    private final String trailerNumber;

    private final String notes;

    private final Range<JustDate> range;

    private Trip(SimpleRow row) {
      this.tripId = row.getLong(COL_TRIP_ID);
      this.tripNo = row.getValue(COL_TRIP_NO);

      this.date = row.getDateTime(COL_TRIP_DATE);
      this.plannedEndDate = row.getDate(COL_TRIP_PLANNED_END_DATE);
      this.dateFrom = row.getDate(COL_TRIP_DATE_FROM);
      this.dateTo = row.getDate(COL_TRIP_DATE_TO);

      this.truckId = row.getLong(COL_VEHICLE);
      this.truckNumber = row.getValue(ALS_VEHICLE_NUMBER);
      this.trailerId = row.getLong(COL_TRAILER);
      this.trailerNumber = row.getValue(ALS_TRAILER_NUMBER);

      this.notes = row.getValue(COL_TRIP_NOTES);

      JustDate start = BeeUtils.nvl(this.dateFrom, this.date.getDate());
      JustDate end = BeeUtils.nvl(this.dateTo, this.plannedEndDate);

      this.range = Range.closed(start, BeeUtils.max(start, end));
    }

    @Override
    public Long getColorSource() {
      return tripId;
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }
  }

  private static class Vehicle implements HasDateRange {
    private static final int numberIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_NUMBER);
    private static final int startIndex =
        Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_START_DATE);
    private static final int endIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_END_DATE);

    private static final int parentModelNameIndex =
        Data.getColumnIndex(VIEW_VEHICLES, COL_PARENT_MODEL_NAME);
    private static final int modelNameIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_MODEL_NAME);
    private static final int notesIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_NOTES);

    private final BeeRow row;

    private final Long id;
    private final String number;

    private final Range<JustDate> range;

    private Vehicle(BeeRow row) {
      this.row = row;

      this.id = row.getId();
      this.number = row.getString(numberIndex);

      this.range = ChartHelper.getActivity(row.getDate(startIndex), row.getDate(endIndex));
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }

    private String getInactivityTitle(Range<JustDate> inactivity) {
      if (inactivity == null || getRange() == null) {
        return BeeConst.STRING_EMPTY;

      } else if (inactivity.hasUpperBound() && getRange().hasLowerBound()
          && BeeUtils.isLess(inactivity.upperEndpoint(), getRange().lowerEndpoint())) {
        return ChartHelper.buildTitle(Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_START_DATE),
            getRange().lowerEndpoint());

      } else if (inactivity.hasLowerBound() && getRange().hasUpperBound()
          && BeeUtils.isMore(inactivity.lowerEndpoint(), getRange().upperEndpoint())) {
        return ChartHelper.buildTitle(Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_END_DATE),
            getRange().upperEndpoint());

      } else {
        return BeeConst.STRING_EMPTY;
      }
    }

    private String getInfo() {
      return BeeUtils.joinWords(row.getString(parentModelNameIndex), row.getString(modelNameIndex),
          row.getString(notesIndex));
    }

    private String getTitle() {
      return BeeUtils.trim(row.getString(notesIndex));
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(VehicleTimeBoard.class);

  private static final String STYLE_PREFIX = "bee-tr-vtb-";

  private static final String STYLE_VEHICLE_PREFIX = STYLE_PREFIX + "Vehicle-";
  private static final String STYLE_VEHICLE_ROW_SEPARATOR = STYLE_VEHICLE_PREFIX + "row-sep";

  private static final String STYLE_NUMBER_PREFIX = STYLE_PREFIX + "Number-";
  private static final String STYLE_NUMBER_PANEL = STYLE_NUMBER_PREFIX + "panel";
  private static final String STYLE_NUMBER_LABEL = STYLE_NUMBER_PREFIX + "label";
  private static final String STYLE_NUMBER_OVERLAP = STYLE_NUMBER_PREFIX + "overlap";

  private static final String STYLE_INFO_PREFIX = STYLE_PREFIX + "Info-";
  private static final String STYLE_INFO_PANEL = STYLE_INFO_PREFIX + "panel";
  private static final String STYLE_INFO_LABEL = STYLE_INFO_PREFIX + "label";
  private static final String STYLE_INFO_OVERLAP = STYLE_INFO_PREFIX + "overlap";

  private static final String STYLE_SERVICE_PREFIX = STYLE_PREFIX + "Service-";
  private static final String STYLE_SERVICE_PANEL = STYLE_SERVICE_PREFIX + "panel";
  private static final String STYLE_SERVICE_LABEL = STYLE_SERVICE_PREFIX + "label";

  private static final String STYLE_INACTIVE = STYLE_PREFIX + "Inactive";
  private static final String STYLE_OVERLAP = STYLE_PREFIX + "Overlap";

  private final List<Vehicle> vehicles = Lists.newArrayList();

  private final Multimap<Long, Trip> trips = ArrayListMultimap.create();
  private final Multimap<Long, Freight> freights = ArrayListMultimap.create();
  private final Multimap<Long, CargoHandling> handling = ArrayListMultimap.create();

  private final Multimap<Long, VehicleService> services = ArrayListMultimap.create();

  private int numberWidth = BeeConst.UNDEF;
  private int infoWidth = BeeConst.UNDEF;

  private boolean separateCargo = false;

  private final Set<String> numberPanels = Sets.newHashSet();
  private final Set<String> infoPanels = Sets.newHashSet();

  protected VehicleTimeBoard() {
    super();

    addStyleName(STYLE_PREFIX + "View");

    setRelevantDataViews(VIEW_VEHICLES, VIEW_TRIPS, VIEW_ORDER_CARGO, VIEW_CARGO_HANDLING,
        VIEW_CARGO_TRIPS, VIEW_TRIP_CARGO, VIEW_TRIP_DRIVERS, VIEW_VEHICLE_SERVICES,
        CommonsConstants.VIEW_COLORS, CommonsConstants.VIEW_THEME_COLORS);
  }

  @Override
  protected Collection<? extends HasDateRange> getChartItems() {
    return trips.values();
  }

  protected abstract String getDayWidthColumnName();

  @Override
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.CONFIGURE);
  }

  protected abstract String getInfoWidthColumnName();

  protected abstract String getItemOpacityColumnName();

  protected abstract String getNumberWidthColumnName();

  protected abstract String getRelatedTripColumnName();

  protected abstract String getSeparateCargoColumnName();

  @Override
  protected void initData(BeeRowSet rowSet) {
    vehicles.clear();
    trips.clear();
    freights.clear();
    handling.clear();
    services.clear();

    if (rowSet == null) {
      updateMaxRange();
      return;
    }

    String serialized = rowSet.getTableProperty(PROP_VEHICLES);
    if (!BeeUtils.isEmpty(serialized)) {
      BeeRowSet brs = BeeRowSet.restore(serialized);
      for (BeeRow row : brs.getRows()) {
        vehicles.add(new Vehicle(row));
      }
    }

    serialized = rowSet.getTableProperty(PROP_TRIPS);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      int index = srs.getColumnIndex(getRelatedTripColumnName());
      for (SimpleRow row : srs) {
        trips.put(row.getLong(index), new Trip(row));
      }
    }

    serialized = rowSet.getTableProperty(PROP_CARGO);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      for (SimpleRow row : srs) {
        freights.put(row.getLong(COL_TRIP_ID), new Freight(row));
      }
    }

    serialized = rowSet.getTableProperty(PROP_CARGO_HANDLING);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      for (SimpleRow row : srs) {
        handling.put(row.getLong(COL_CARGO), new CargoHandling(row));
      }
    }

    serialized = rowSet.getTableProperty(PROP_VEHICLE_SERVICES);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      for (SimpleRow row : srs) {
        VehicleService service = new VehicleService(row);
        services.put(service.getVehicleId(), service);
      }
    }

    updateMaxRange();
    logger.debug(getCaption(), vehicles.size(), trips.size(), freights.size(), handling.size(),
        services.size());
  }

  @Override
  protected Collection<? extends HasDateRange> initItems(SimpleRowSet data) {
    return null;
  }

  @Override
  protected void prepareChart(Size canvasSize) {
    setNumberWidth(ChartHelper.getPixels(getSettings(), getNumberWidthColumnName(), 80,
        ChartHelper.DEFAULT_MOVER_WIDTH + 1, canvasSize.getWidth() / 3));
    setInfoWidth(ChartHelper.getPixels(getSettings(), getInfoWidthColumnName(), 120,
        ChartHelper.DEFAULT_MOVER_WIDTH + 1, canvasSize.getWidth() / 3));

    setChartLeft(getNumberWidth() + getInfoWidth());
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());

    setDayColumnWidth(ChartHelper.getPixels(getSettings(), getDayWidthColumnName(), 20,
        1, getChartWidth()));

    setSeparateCargo(ChartHelper.getBoolean(getSettings(), getSeparateCargoColumnName()));
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    numberPanels.clear();
    infoPanels.clear();

    List<ChartRowLayout> vehicleLayout = doLayout();

    int rc = 0;
    for (ChartRowLayout layout : vehicleLayout) {
      rc += layout.size();
    }

    initContent(panel, rc);
    if (vehicleLayout.isEmpty()) {
      return;
    }

    int calendarWidth = getCalendarWidth();

    Double opacity = ChartHelper.getOpacity(getSettings(), getItemOpacityColumnName());

    Edges margins = new Edges();
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    Widget offWidget;

    int rowIndex = 0;
    for (ChartRowLayout layout : vehicleLayout) {

      int size = layout.size();
      int lastRow = rowIndex + size - 1;

      int top = rowIndex * getRowHeight();

      if (rowIndex > 0) {
        ChartHelper.addRowSeparator(panel, STYLE_VEHICLE_ROW_SEPARATOR, top, 0,
            getChartLeft() + calendarWidth);
      }

      Vehicle vehicle = vehicles.get(layout.getDataIndex());
      Assert.notNull(vehicle, "vehicle not found");
      boolean hasOverlap = !layout.getOverlap().isEmpty();

      IdentifiableWidget numberWidget = createNumberWidget(vehicle, hasOverlap);
      addNumberWidget(panel, numberWidget.asWidget(), rowIndex, lastRow);
      numberPanels.add(numberWidget.getId());

      IdentifiableWidget infoWidget = createInfoWidget(vehicle, hasOverlap);
      addInfoWidget(panel, infoWidget.asWidget(), rowIndex, lastRow);
      infoPanels.add(infoWidget.getId());

      for (int i = 1; i < size; i++) {
        ChartHelper.addRowSeparator(panel, top + getRowHeight() * i, getChartLeft(), calendarWidth);
      }

      for (HasDateRange item : layout.getInactivity()) {
        if (item instanceof VehicleService) {
          offWidget = ((VehicleService) item).createWidget(this, STYLE_SERVICE_PANEL,
              STYLE_SERVICE_LABEL);
        } else {
          offWidget = new CustomDiv(STYLE_INACTIVE);
          UiHelper.maybeSetTitle(offWidget, vehicle.getInactivityTitle(item.getRange()));
        }

        Rectangle rectangle = getRectangle(item.getRange(), rowIndex, lastRow);
        ChartHelper.apply(offWidget, rectangle, margins);

        panel.add(offWidget);
      }

      rowIndex += size;
    }
  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
    Mover numberMover = ChartHelper.createHorizontalMover();
    StyleUtils.setLeft(numberMover, getNumberWidth() - ChartHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(numberMover, height);

    numberMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onNumberResize(event);
      }
    });

    panel.add(numberMover);

    Mover infoMover = ChartHelper.createHorizontalMover();
    StyleUtils.setLeft(infoMover, getChartLeft() - ChartHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(infoMover, height);

    infoMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onInfoResize(event);
      }
    });

    panel.add(infoMover);
  }

  private void addInfoWidget(HasWidgets panel, Widget widget, int firstRow, int lastRow) {
    Rectangle rectangle = ChartHelper.getRectangle(getNumberWidth(), getInfoWidth(),
        firstRow, lastRow, getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget, rectangle, margins);
    panel.add(widget);
  }

  private void addNumberWidget(HasWidgets panel, Widget widget, int firstRow, int lastRow) {
    Rectangle rectangle = ChartHelper.getRectangle(0, getNumberWidth(), firstRow, lastRow,
        getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget, rectangle, margins);
    panel.add(widget);
  }

  private IdentifiableWidget createInfoWidget(Vehicle vehicle, boolean hasOverlap) {
    Simple panel = new Simple();
    panel.addStyleName(STYLE_INFO_PANEL);
    if (hasOverlap) {
      panel.addStyleName(STYLE_INFO_OVERLAP);
    }

    final Long vehicleId = vehicle.id;

    BeeLabel label = new BeeLabel(vehicle.getInfo());
    label.addStyleName(STYLE_INFO_LABEL);

    UiHelper.maybeSetTitle(label, vehicle.getTitle());

    label.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_VEHICLES, vehicleId);
      }
    });

    panel.add(label);

    return panel;
  }

  private IdentifiableWidget createNumberWidget(Vehicle vehicle, boolean hasOverlap) {
    Simple panel = new Simple();
    panel.addStyleName(STYLE_NUMBER_PANEL);
    if (hasOverlap) {
      panel.addStyleName(STYLE_NUMBER_OVERLAP);
    }

    final Long vehicleId = vehicle.id;

    BeeLabel label = new BeeLabel(vehicle.number);
    label.addStyleName(STYLE_NUMBER_LABEL);

    UiHelper.maybeSetTitle(label, vehicle.getTitle());

    label.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_VEHICLES, vehicleId);
      }
    });

    panel.add(label);

    return panel;
  }

  private List<ChartRowLayout> doLayout() {
    List<ChartRowLayout> result = Lists.newArrayList();
    Range<JustDate> range = getVisibleRange();

    for (int vehicleIndex = 0; vehicleIndex < vehicles.size(); vehicleIndex++) {
      Vehicle vehicle = vehicles.get(vehicleIndex);

      if (ChartHelper.isActive(vehicle, range)) {
        ChartRowLayout layout = new ChartRowLayout(vehicleIndex);

        if (trips.containsKey(vehicle.id)) {
          if (separateCargo()) {
            Collection<Trip> vehicleTrips = trips.get(vehicle.id);

            for (Trip trip : vehicleTrips) {
              if (freights.containsKey(trip.tripId)) {
                layout.addItems(ChartHelper.getActiveItems(freights.get(trip.tripId), range),
                    range, true);
              }
            }

          } else {
            layout.addItems(ChartHelper.getActiveItems(trips.get(vehicle.id), range), range, true);
          }
        }

        layout.addInactivity(ChartHelper.getInactivity(vehicle, range), range);
        if (services.containsKey(vehicle.id)) {
          layout.addInactivity(ChartHelper.getActiveItems(services.get(vehicle.id), range), range);
        }

        result.add(layout);
      }
    }
    return result;
  }

  private int getInfoWidth() {
    return infoWidth;
  }

  private int getNumberWidth() {
    return numberWidth;
  }

  private void onInfoResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft = getNumberWidth() + 300;
    if (getChartWidth() > 0) {
      maxLeft = Math.min(maxLeft, getChartLeft() + getChartWidth() / 2);
    }

    int newLeft = BeeUtils.clamp(oldLeft + delta, getNumberWidth() + 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      int infoPx = newLeft - getNumberWidth() + ChartHelper.DEFAULT_MOVER_WIDTH;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : infoPanels) {
          StyleUtils.setWidth(id, infoPx - ChartHelper.DEFAULT_MOVER_WIDTH);
        }
      }

      if (event.isFinished() && updateSetting(getInfoWidthColumnName(), infoPx)) {
        setInfoWidth(infoPx);
        render(false);
      }
    }
  }

  private void onNumberResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int newLeft = BeeUtils.clamp(oldLeft + delta, 1,
        getChartLeft() - ChartHelper.DEFAULT_MOVER_WIDTH * 2 - 1);

    if (newLeft != oldLeft || event.isFinished()) {
      int numberPx = newLeft + ChartHelper.DEFAULT_MOVER_WIDTH;
      int infoPx = getChartLeft() - numberPx;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : numberPanels) {
          StyleUtils.setWidth(id, numberPx - ChartHelper.DEFAULT_MOVER_WIDTH);
        }

        for (String id : infoPanels) {
          Element element = Document.get().getElementById(id);
          if (element != null) {
            StyleUtils.setLeft(element, numberPx);
            StyleUtils.setWidth(element, infoPx - ChartHelper.DEFAULT_MOVER_WIDTH);
          }
        }
      }

      if (event.isFinished() && updateSettings(getNumberWidthColumnName(), numberPx,
          getInfoWidthColumnName(), infoPx)) {
        setNumberWidth(numberPx);
        setInfoWidth(infoPx);
      }
    }
  }

  private boolean separateCargo() {
    return separateCargo;
  }

  private void setInfoWidth(int infoWidth) {
    this.infoWidth = infoWidth;
  }

  private void setNumberWidth(int numberWidth) {
    this.numberWidth = numberWidth;
  }

  private void setSeparateCargo(boolean separateCargo) {
    this.separateCargo = separateCargo;
  }
}
