package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
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

import com.butent.bee.client.Callback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Flow;
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
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
abstract class VehicleTimeBoard extends ChartBase {

  private static class CargoEvent {
    private final Freight freight;
    private final CargoHandling cargoHandling;

    private final boolean loading;

    private CargoEvent(Freight freight, CargoHandling cargoHandling, boolean loading) {
      this.freight = freight;
      this.cargoHandling = cargoHandling;

      this.loading = loading;
    }

    private Long getCountryId() {
      if (cargoHandling == null) {
        return loading ? freight.loadingCountry : freight.unloadingCountry;
      } else if (loading) {
        return BeeUtils.nvl(cargoHandling.loadingCountry, freight.loadingCountry);
      } else {
        return BeeUtils.nvl(cargoHandling.unloadingCountry, freight.unloadingCountry);
      }
    }

    private String getPlace() {
      if (cargoHandling == null) {
        return loading ? freight.loadingPlace : freight.unloadingPlace;
      } else {
        return loading ? cargoHandling.loadingPlace : cargoHandling.unloadingPlace;
      }
    }

    private String getTerminal() {
      if (cargoHandling == null) {
        return loading ? freight.loadingTerminal : freight.unloadingTerminal;
      } else {
        return loading ? cargoHandling.loadingTerminal : cargoHandling.unloadingTerminal;
      }
    }
  }

  private static class CargoHandling implements HasDateRange {
    private static final String notesLabel =
        Data.getColumnLabel(VIEW_CARGO_HANDLING, COL_CARGO_HANDLING_NOTES);

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

    private String getTitle(String loadMessage, String unloadMessage) {
      return BeeUtils.buildLines(loadMessage, unloadMessage,
          ChartHelper.buildTitle(notesLabel, notes));
    }
  }

  private static class Freight implements HasDateRange, HasColorSource {
    private static final String customerLabel = Data.getColumnLabel(VIEW_ORDERS, COL_CUSTOMER);
    private static final String notesLabel = Data.getColumnLabel(VIEW_ORDER_CARGO, COL_CARGO_NOTES);

    private final Long tripId;

    private final Long cargoTripId;
    private final Long cargoTripVersion;

    private final Long cargoId;
    private final String cargoDescription;

    private final String notes;

    private final JustDate loadingDate;
    private final Long loadingCountry;
    private final String loadingPlace;
    private final String loadingTerminal;

    private final JustDate unloadingDate;
    private final Long unloadingCountry;
    private final String unloadingPlace;
    private final String unloadingTerminal;

    private final String customerName;

    private Range<JustDate> range;

    private Freight(SimpleRow row, JustDate minLoad, JustDate maxUnload) {
      this.tripId = row.getLong(COL_TRIP_ID);

      this.cargoTripId = row.getLong(COL_CARGO_TRIP_ID);
      this.cargoTripVersion = row.getLong(ALS_CARGO_TRIP_VERSION);

      this.cargoId = row.getLong(COL_CARGO);
      this.cargoDescription = row.getValue(COL_DESCRIPTION);

      this.notes = row.getValue(COL_CARGO_NOTES);
      
      this.loadingDate = BeeUtils.nvl(row.getDate(loadingColumnAlias(COL_PLACE_DATE)),
          row.getDate(defaultLoadingColumnAlias(COL_PLACE_DATE)), minLoad);
      this.loadingCountry = BeeUtils.nvl(row.getLong(loadingColumnAlias(COL_COUNTRY)),
          row.getLong(defaultLoadingColumnAlias(COL_COUNTRY)));
      this.loadingPlace = BeeUtils.nvl(row.getValue(loadingColumnAlias(COL_PLACE_NAME)),
          row.getValue(defaultLoadingColumnAlias(COL_PLACE_NAME)));
      this.loadingTerminal = BeeUtils.nvl(row.getValue(loadingColumnAlias(COL_TERMINAL)),
          row.getValue(defaultLoadingColumnAlias(COL_TERMINAL)));

      this.unloadingDate = BeeUtils.nvl(row.getDate(unloadingColumnAlias(COL_PLACE_DATE)),
          row.getDate(defaultUnloadingColumnAlias(COL_PLACE_DATE)), maxUnload);
      this.unloadingCountry = BeeUtils.nvl(row.getLong(unloadingColumnAlias(COL_COUNTRY)),
          row.getLong(defaultUnloadingColumnAlias(COL_COUNTRY)));
      this.unloadingPlace = BeeUtils.nvl(row.getValue(unloadingColumnAlias(COL_PLACE_NAME)),
          row.getValue(defaultUnloadingColumnAlias(COL_PLACE_NAME)));
      this.unloadingTerminal = BeeUtils.nvl(row.getValue(unloadingColumnAlias(COL_TERMINAL)),
          row.getValue(defaultUnloadingColumnAlias(COL_TERMINAL)));

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

    private void adjustRange(Range<JustDate> tripRange) {
      if (tripRange == null) {
        return;
      }
      if (loadingDate != null && unloadingDate != null) {
        return;
      }

      JustDate lower = BeeUtils.nvl(loadingDate, BeeUtils.getLowerEndpoint(tripRange));
      JustDate upper = BeeUtils.nvl(unloadingDate, BeeUtils.getUpperEndpoint(tripRange));

      setRange(ChartHelper.getActivity(lower, upper));
    }

    private JustDate getMaxDate() {
      return BeeUtils.max(loadingDate, unloadingDate);
    }
    
    private String getTitle(String loadMessage, String unloadMessage) {
      return BeeUtils.buildLines(cargoDescription, loadMessage, unloadMessage,
          ChartHelper.buildTitle(customerLabel, customerName, notesLabel, notes));
    }

    private void setRange(Range<JustDate> range) {
      this.range = range;
    }
  }

  private static class FreightBlender implements ChartRowLayout.Blender {
    private FreightBlender() {
    }

    @Override
    public boolean willItBlend(HasDateRange x, HasDateRange y) {
      if (x instanceof Freight && y instanceof Freight) {
        return Objects.equal(((Freight) x).tripId, ((Freight) y).tripId);
      } else {
        return false;
      }
    }
  }

  private static class Trip implements HasDateRange, HasColorSource {
    private static final String tripNoLabel = Data.getColumnLabel(VIEW_TRIPS, COL_TRIP_NO);
    private static final String truckLabel = Data.getColumnLabel(VIEW_TRIPS, COL_VEHICLE);
    private static final String trailerLabel = Data.getColumnLabel(VIEW_TRIPS, COL_TRAILER);
    private static final String notesLabel = Data.getColumnLabel(VIEW_TRIPS, COL_TRIP_NOTES);

    private static final String driversLabel = Data.getLocalizedCaption(VIEW_DRIVERS);
    private static final String cargosLabel = Data.getLocalizedCaption(VIEW_CARGO_TRIPS);

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

    private final String title;

    private Trip(SimpleRow row, JustDate maxDate, String drv, int cargoCount) {
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
      JustDate end = BeeUtils.nvl(this.dateTo, this.plannedEndDate, maxDate);

      this.range = Range.closed(start, BeeUtils.max(start, end));

      this.title = BeeUtils.buildLines(ChartHelper.getRangeLabel(this.range),
          ChartHelper.buildTitle(tripNoLabel, this.tripNo, truckLabel, this.truckNumber,
              trailerLabel, this.trailerNumber, driversLabel, drv, cargosLabel, cargoCount,
              notesLabel, this.notes));
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

    private static final String startLabel =
        Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_START_DATE);
    private static final String endLabel = Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_END_DATE);

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
        return ChartHelper.buildTitle(startLabel, getRange().lowerEndpoint());

      } else if (inactivity.hasLowerBound() && getRange().hasUpperBound()
          && BeeUtils.isMore(inactivity.lowerEndpoint(), getRange().upperEndpoint())) {
        return ChartHelper.buildTitle(endLabel, getRange().upperEndpoint());

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

  private static final String STYLE_TRIP_PREFIX = STYLE_PREFIX + "Trip-";
  private static final String STYLE_TRIP_PANEL = STYLE_TRIP_PREFIX + "panel";
  private static final String STYLE_TRIP_VOID = STYLE_TRIP_PREFIX + "void";

  private static final String STYLE_TRIP_DAY_PREFIX = STYLE_TRIP_PREFIX + "day-";
  private static final String STYLE_TRIP_DAY_PANEL = STYLE_TRIP_DAY_PREFIX + "panel";
  private static final String STYLE_TRIP_DAY_WIDGET = STYLE_TRIP_DAY_PREFIX + "widget";
  private static final String STYLE_TRIP_DAY_COUNTRY = STYLE_TRIP_DAY_PREFIX + "country";
  private static final String STYLE_TRIP_DAY_NO_COUNTRY = STYLE_TRIP_DAY_PREFIX + "no-country";
  private static final String STYLE_TRIP_DAY_LABEL = STYLE_TRIP_DAY_PREFIX + "label";

  private static final String STYLE_FREIGHT_PREFIX = STYLE_PREFIX + "Freight-";
  private static final String STYLE_FREIGHT_PANEL = STYLE_FREIGHT_PREFIX + "panel";

  private static final String STYLE_SERVICE_PREFIX = STYLE_PREFIX + "Service-";
  private static final String STYLE_SERVICE_PANEL = STYLE_SERVICE_PREFIX + "panel";
  private static final String STYLE_SERVICE_LABEL = STYLE_SERVICE_PREFIX + "label";

  private static final String STYLE_INACTIVE = STYLE_PREFIX + "Inactive";
  private static final String STYLE_OVERLAP = STYLE_PREFIX + "Overlap";

  private static final FreightBlender FREIGHT_BLENDER = new FreightBlender();

  private final List<Vehicle> vehicles = Lists.newArrayList();

  private final Multimap<Long, Trip> trips = ArrayListMultimap.create();
  private final Multimap<Long, String> drivers = HashMultimap.create();

  private final Multimap<Long, Freight> freights = ArrayListMultimap.create();
  private final Multimap<Long, CargoHandling> handling = ArrayListMultimap.create();

  private final Multimap<Long, VehicleService> services = ArrayListMultimap.create();

  private int numberWidth = BeeConst.UNDEF;
  private int infoWidth = BeeConst.UNDEF;

  private boolean separateCargo = false;
  private boolean showCountryFlags = false;
  private boolean showPlaceInfo = false;

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
    return separateCargo() ? freights.values() : trips.values();
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

  protected abstract String getShowCountryFlagsColumnName();
  
  protected abstract String getShowPlaceInfoColumnName();

  @Override
  protected void initData(BeeRowSet rowSet) {
    vehicles.clear();
    trips.clear();
    drivers.clear();
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

    serialized = rowSet.getTableProperty(PROP_DRIVERS);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      for (SimpleRow row : srs) {
        drivers.put(row.getLong(COL_TRIP),
            BeeUtils.joinWords(row.getValue(CommonsConstants.COL_FIRST_NAME),
                row.getValue(CommonsConstants.COL_LAST_NAME)));
      }
    }

    serialized = rowSet.getTableProperty(PROP_CARGO_HANDLING);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      for (SimpleRow row : srs) {
        handling.put(row.getLong(COL_CARGO), new CargoHandling(row));
      }
    }

    serialized = rowSet.getTableProperty(PROP_CARGO);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);

      for (SimpleRow row : srs) {
        JustDate minLoad = null;
        JustDate maxUnload = null;
        Long cargoId = row.getLong(COL_CARGO);

        if (handling.containsKey(cargoId)) {
          for (CargoHandling ch : handling.get(cargoId)) {
            minLoad = BeeUtils.min(minLoad, ch.loadingDate);
            maxUnload = BeeUtils.max(maxUnload, ch.unloadingDate);
          }
        }

        freights.put(row.getLong(COL_TRIP_ID), new Freight(row, minLoad, maxUnload));
      }
    }

    serialized = rowSet.getTableProperty(PROP_TRIPS);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet srs = SimpleRowSet.restore(serialized);
      int index = srs.getColumnIndex(getRelatedTripColumnName());

      for (SimpleRow row : srs) {
        Long tripId = row.getLong(COL_TRIP_ID);

        String drv = drivers.containsKey(tripId)
            ? BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, drivers.get(tripId)) : null;
        int cargoCount = 0;

        if (freights.containsKey(tripId)) {
          JustDate maxDate = null;
          for (Freight freight : freights.get(tripId)) {
            maxDate = BeeUtils.max(maxDate, freight.getMaxDate());
            cargoCount++;
          }

          Trip trip = new Trip(row, maxDate, drv, cargoCount);
          trips.put(row.getLong(index), trip);

          for (Freight freight : freights.get(tripId)) {
            freight.adjustRange(trip.getRange());
          }

        } else {
          trips.put(row.getLong(index), new Trip(row, null, drv, cargoCount));
        }
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

    setSeparateCargo(ChartHelper.getBoolean(getSettings(), getSeparateCargoColumnName()));
    updateMaxRange();

    logger.debug(getCaption(), vehicles.size(), trips.size(), drivers.size(), freights.size(),
        handling.size(), services.size());
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

    boolean sc = ChartHelper.getBoolean(getSettings(), getSeparateCargoColumnName());
    if (separateCargo() != sc) {
      setSeparateCargo(sc);
      updateMaxRange();
    }
    
    setShowCountryFlags(ChartHelper.getBoolean(getSettings(), getShowCountryFlagsColumnName()));
    setShowPlaceInfo(ChartHelper.getBoolean(getSettings(), getShowPlaceInfoColumnName()));
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
    Widget itemWidget;
    Widget overlapWidget;

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

      boolean hasOverlap = layout.hasOverlap();

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

      for (int i = 0; i < layout.getRows().size(); i++) {
        for (HasDateRange item : layout.getRows().get(i)) {

          if (item instanceof Trip) {
            itemWidget = createTripWidget((Trip) item);
          } else if (item instanceof Freight) {
            itemWidget = createFreightWidget((Freight) item);
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

          if (hasOverlap) {
            Set<Range<JustDate>> overlap = layout.getOverlap(item.getRange());

            for (Range<JustDate> over : overlap) {
              overlapWidget = new CustomDiv(STYLE_OVERLAP);

              Rectangle rectangle = getRectangle(over, rowIndex + i);
              ChartHelper.apply(overlapWidget, rectangle, margins);

              panel.add(overlapWidget);
            }
          }
        }
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

  private String buildFreightTitle(Freight freight) {
    String loading = getPlaceLabel(freight.loadingCountry, freight.loadingPlace,
        freight.loadingTerminal);
    String unloading = getPlaceLabel(freight.unloadingCountry, freight.unloadingPlace,
        freight.unloadingTerminal);

    return freight.getTitle(loading, unloading);
  }

  private Widget createFreightWidget(Freight freight) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_FREIGHT_PANEL);
    setItemWidgetColor(freight, panel);

    panel.setTitle(buildFreightTitle(freight));

    final Long cargoId = freight.cargoId;

    panel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_ORDER_CARGO, cargoId);
      }
    });

    return panel;
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

  private Widget createTripDayPanel(Multimap<Long, CargoEvent> dayEvents) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_TRIP_DAY_PANEL);

    int dayWidth = getDayColumnWidth();
    int dayHeight = getRowHeight();

    Set<Long> countryIds = dayEvents.keySet();
    int widgetCount = countryIds.size();

    int width;
    int height;

    if (widgetCount == 1) {
      width = dayWidth;
      height = dayHeight;

    } else if (dayWidth > dayHeight) {
      width = dayWidth / widgetCount;
      height = dayHeight;

    } else {
      width = dayWidth;
      height = dayHeight / widgetCount;
    }

    for (Long countryId : countryIds) {
      Widget widget = createTripDayWidget(countryId, dayEvents.get(countryId));
      StyleUtils.setSize(widget, width, height);

      panel.add(widget);
    }

    return panel;
  }

  private Widget createTripDayWidget(Long countryId, Collection<CargoEvent> events) {
    final Flow widget = new Flow();
    widget.addStyleName(STYLE_TRIP_DAY_WIDGET);

    if (showCountryFlags() && DataUtils.isId(countryId)) {
      getCountryFlag(countryId, new Callback<String>() {
        @Override
        public void onFailure(String... reason) {
          widget.addStyleName(STYLE_TRIP_DAY_NO_COUNTRY);
        }

        @Override
        public void onSuccess(String result) {
          if (BeeUtils.isEmpty(result)) {
            widget.addStyleName(STYLE_TRIP_DAY_NO_COUNTRY);
          } else {
            widget.addStyleName(STYLE_TRIP_DAY_COUNTRY);
            StyleUtils.setBackgroundImage(widget, result);
          }
        }
      });

    } else {
      widget.addStyleName(STYLE_TRIP_DAY_NO_COUNTRY);
    }
    
    if (!BeeUtils.isEmpty(events)) {
      if (showPlaceInfo()) {
        List<String> info = Lists.newArrayList();
        
        if (!showCountryFlags() && DataUtils.isId(countryId)) {
          String countryLabel = getCountryLabel(countryId);
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
          CustomDiv label = new CustomDiv(STYLE_TRIP_DAY_LABEL);
          label.setText(BeeUtils.join(BeeConst.STRING_SPACE, info));
          
          widget.add(label);
        }
      }
    }

    return widget;
  }

  private Widget createTripWidget(Trip trip) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_TRIP_PANEL);
    setItemWidgetColor(trip, panel);

    panel.setTitle(trip.title);

    final Long tripId = trip.tripId;

    panel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_TRIPS, tripId);
      }
    });

    Range<JustDate> tripRange =
        ChartHelper.normalizedCopyOf(trip.getRange().intersection(getVisibleRange()));
    if (tripRange == null || tripRange.isEmpty()) {
      return panel;
    }

    List<Range<JustDate>> voidRanges;

    if (freights.containsKey(tripId)) {
      Multimap<JustDate, CargoEvent> tripLayout = doTripLayout(tripId, tripRange);
      Set<JustDate> eventDates = tripLayout.keySet();

      for (JustDate date : eventDates) {
        Multimap<Long, CargoEvent> dayLayout = doTripDayLayout(tripLayout.get(date));
        if (!dayLayout.isEmpty()) {
          Widget dayWidget = createTripDayPanel(dayLayout);

          StyleUtils.setLeft(dayWidget, getRelativeLeft(tripRange, date));
          StyleUtils.setWidth(dayWidget, getDayColumnWidth());

          panel.add(dayWidget);
        }
      }

      voidRanges = getVoidRanges(tripRange, eventDates, freights.get(tripId));

    } else {
      voidRanges = Lists.newArrayList();
      voidRanges.add(tripRange);
    }

    for (Range<JustDate> voidRange : voidRanges) {
      Widget voidWidget = new CustomDiv(STYLE_TRIP_VOID);

      StyleUtils.setLeft(voidWidget, getRelativeLeft(tripRange, voidRange.lowerEndpoint()));
      StyleUtils.setWidth(voidWidget, ChartHelper.getSize(voidRange) * getDayColumnWidth());

      panel.add(voidWidget);
    }

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
                    range, FREIGHT_BLENDER);
              }
            }

          } else {
            layout.addItems(ChartHelper.getActiveItems(trips.get(vehicle.id), range), range);
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

  private Multimap<Long, CargoEvent> doTripDayLayout(Collection<CargoEvent> events) {
    Multimap<Long, CargoEvent> result = LinkedListMultimap.create();
    for (CargoEvent event : events) {
      result.put(event.getCountryId(), event);
    }
    return result;
  }

  private Multimap<JustDate, CargoEvent> doTripLayout(Long tripId, Range<JustDate> range) {
    Multimap<JustDate, CargoEvent> result = ArrayListMultimap.create();
    if (tripId == null || range == null || range.isEmpty()) {
      return result;
    }

    Collection<Freight> tripCargos = freights.get(tripId);
    if (BeeUtils.isEmpty(tripCargos)) {
      return result;
    }

    for (Freight freight : tripCargos) {
      if (freight.loadingDate != null && range.contains(freight.loadingDate)) {
        result.put(freight.loadingDate, new CargoEvent(freight, null, true));
      }

      if (handling.containsKey(freight.cargoId)) {
        for (CargoHandling ch : handling.get(freight.cargoId)) {
          if (ch.loadingDate != null && range.contains(ch.loadingDate)) {
            result.put(ch.loadingDate, new CargoEvent(freight, ch, true));
          }
        }
      }
    }

    for (Freight freight : tripCargos) {
      if (handling.containsKey(freight.cargoId)) {
        for (CargoHandling ch : handling.get(freight.cargoId)) {
          if (ch.unloadingDate != null && range.contains(ch.unloadingDate)) {
            result.put(ch.unloadingDate, new CargoEvent(freight, ch, false));
          }
        }
      }

      if (freight.unloadingDate != null && range.contains(freight.unloadingDate)) {
        result.put(freight.unloadingDate, new CargoEvent(freight, null, false));
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

  private int getRelativeLeft(Range<JustDate> parent, JustDate date) {
    return TimeUtils.dayDiff(parent.lowerEndpoint(), date) * getDayColumnWidth();
  }

  private List<Range<JustDate>> getVoidRanges(Range<JustDate> tripRange,
      Set<JustDate> eventDates, Collection<Freight> tripFreights) {

    List<Range<JustDate>> result = Lists.newArrayList();
    int tripDays = ChartHelper.getSize(tripRange);

    Set<JustDate> usedDates = Sets.newHashSet();

    if (!BeeUtils.isEmpty(eventDates)) {
      if (eventDates.size() >= tripDays) {
        return result;
      }
      usedDates.addAll(eventDates);
    }

    if (!BeeUtils.isEmpty(tripFreights)) {
      for (Freight freight : tripFreights) {
        if (ChartHelper.isActive(freight, tripRange)) {
          Range<JustDate> freightRange = 
              ChartHelper.normalizedCopyOf(freight.getRange().intersection(tripRange));
          if (freightRange == null) {
            continue;
          }
          
          int freightDays = ChartHelper.getSize(freightRange);
          if (freightDays >= tripDays) {
            return result;
          }
          
          for (int i = 0; i < freightDays; i++) {
            usedDates.add(TimeUtils.nextDay(freightRange.lowerEndpoint(), i));
          }
          
          if (usedDates.size() >= tripDays) {
            return result;
          }
        }
      }
    }
    
    if (BeeUtils.isEmpty(usedDates)) {
      result.add(tripRange);
      return result;
    }

    List<JustDate> dates = Lists.newArrayList(usedDates);
    Collections.sort(dates);

    for (int i = 0; i < dates.size(); i++) {
      JustDate date = dates.get(i);

      if (i == 0 && TimeUtils.isMore(date, tripRange.lowerEndpoint())) {
        result.add(Range.closed(tripRange.lowerEndpoint(), TimeUtils.previousDay(date)));
      }

      if (i > 0 && TimeUtils.dayDiff(dates.get(i - 1), date) > 1) {
        result.add(Range.closed(TimeUtils.nextDay(dates.get(i - 1)), TimeUtils.previousDay(date)));
      }

      if (i == dates.size() - 1 && TimeUtils.isLess(date, tripRange.upperEndpoint())) {
        result.add(Range.closed(TimeUtils.nextDay(date), tripRange.upperEndpoint()));
      }
    }

    return result;
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
}
