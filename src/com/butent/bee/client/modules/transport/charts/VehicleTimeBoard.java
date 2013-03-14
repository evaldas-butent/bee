package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
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
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.transport.charts.CargoEvent.Type;
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
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class VehicleTimeBoard extends ChartBase {

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

  private static final String STYLE_FREIGHT_PREFIX = STYLE_PREFIX + "Freight-";
  private static final String STYLE_FREIGHT_PANEL = STYLE_FREIGHT_PREFIX + "panel";

  private static final String STYLE_DAY_PREFIX = STYLE_PREFIX + "Day-";
  private static final String STYLE_DAY_PANEL = STYLE_DAY_PREFIX + "panel";
  private static final String STYLE_DAY_WIDGET = STYLE_DAY_PREFIX + "widget";
  private static final String STYLE_DAY_COUNTRY = STYLE_DAY_PREFIX + "country";
  private static final String STYLE_DAY_NO_COUNTRY = STYLE_DAY_PREFIX + "no-country";
  private static final String STYLE_DAY_LABEL = STYLE_DAY_PREFIX + "label";

  private static final String STYLE_SERVICE_PREFIX = STYLE_PREFIX + "Service-";
  private static final String STYLE_SERVICE_PANEL = STYLE_SERVICE_PREFIX + "panel";
  private static final String STYLE_SERVICE_LABEL = STYLE_SERVICE_PREFIX + "label";

  private static final String STYLE_INACTIVE = STYLE_PREFIX + "Inactive";
  private static final String STYLE_OVERLAP = STYLE_PREFIX + "Overlap";

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
            minLoad = BeeUtils.min(minLoad, ch.getLoadingDate());
            maxUnload = BeeUtils.max(maxUnload, ch.getUnloadingDate());
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

  private Widget createFreightWidget(Freight freight) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_FREIGHT_PANEL);
    setItemWidgetColor(freight, panel);

    panel.setTitle(freight.getTitle(getLoadingInfo(freight), getUnloadingInfo(freight)));

    final Long cargoId = freight.getCargoId();

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

    final Long vehicleId = vehicle.getId();

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

    final Long vehicleId = vehicle.getId();

    BeeLabel label = new BeeLabel(vehicle.getNumber());
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

  private Widget createTripDayPanel(Trip trip, Multimap<Long, CargoEvent> dayEvents) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_DAY_PANEL);

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
      Widget widget = createTripDayWidget(trip, countryId, dayEvents.get(countryId));
      StyleUtils.setSize(widget, width, height);

      panel.add(widget);
    }

    return panel;
  }

  private Widget createTripDayWidget(Trip trip, Long countryId, Collection<CargoEvent> events) {
    final Flow widget = new Flow();
    widget.addStyleName(STYLE_DAY_WIDGET);

    if (showCountryFlags() && DataUtils.isId(countryId)) {
      getCountryFlag(countryId, new Callback<String>() {
        @Override
        public void onFailure(String... reason) {
          widget.addStyleName(STYLE_DAY_NO_COUNTRY);
        }

        @Override
        public void onSuccess(String result) {
          if (BeeUtils.isEmpty(result)) {
            widget.addStyleName(STYLE_DAY_NO_COUNTRY);
          } else {
            widget.addStyleName(STYLE_DAY_COUNTRY);
            StyleUtils.setBackgroundImage(widget, result);
          }
        }
      });

    } else {
      widget.addStyleName(STYLE_DAY_NO_COUNTRY);
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
          CustomDiv label = new CustomDiv(STYLE_DAY_LABEL);
          label.setText(BeeUtils.join(BeeConst.STRING_SPACE, info));

          widget.add(label);
        }
      }

      List<String> title = Lists.newArrayList();

      Multimap<Freight, CargoEvent> eventsByFreight = LinkedListMultimap.create();
      for (CargoEvent event : events) {
        eventsByFreight.put(event.getFreight(), event);
      }

      for (Freight freight : eventsByFreight.keySet()) {
        EnumSet<CargoEvent.Type> freightEventTypes = EnumSet.noneOf(CargoEvent.Type.class);
        Map<CargoHandling, EnumSet<CargoEvent.Type>> handlingEvents = Maps.newHashMap();

        for (CargoEvent event : eventsByFreight.get(freight)) {
          CargoEvent.Type eventType = event.isLoading()
              ? CargoEvent.Type.LOADING : CargoEvent.Type.UNLOADING;

          if (event.isFreightEvent()) {
            freightEventTypes.add(eventType);
          } else if (handlingEvents.containsKey(event.getCargoHandling())) {
            handlingEvents.get(event.getCargoHandling()).add(eventType);
          } else {
            handlingEvents.put(event.getCargoHandling(), EnumSet.of(eventType));
          }
        }

        String freightLoading = freightEventTypes.contains(CargoEvent.Type.LOADING)
            ? getLoadingInfo(freight) : null;
        String freightUnloading = freightEventTypes.contains(CargoEvent.Type.UNLOADING)
            ? getUnloadingInfo(freight) : null;

        if (!title.isEmpty()) {
          title.add(BeeConst.STRING_NBSP);
        }
        title.add(freight.getTitle(freightLoading, freightUnloading));

        if (!handlingEvents.isEmpty()) {
          title.add(BeeConst.STRING_NBSP);

          for (Map.Entry<CargoHandling, EnumSet<Type>> entry : handlingEvents.entrySet()) {
            String chLoading = entry.getValue().contains(CargoEvent.Type.LOADING)
                ? getLoadingInfo(entry.getKey()) : null;
            String chUnloading = entry.getValue().contains(CargoEvent.Type.UNLOADING)
                ? getUnloadingInfo(entry.getKey()) : null;

            title.add(entry.getKey().getTitle(chLoading, chUnloading));
          }
        }
      }

      title.add(BeeConst.STRING_NBSP);
      title.add(trip.getTitle());

      widget.setTitle(BeeUtils.join(BeeConst.STRING_EOL, title));
    }

    return widget;
  }

  private Widget createTripWidget(Trip trip) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_TRIP_PANEL);
    setItemWidgetColor(trip, panel);

    panel.setTitle(trip.getTitle());

    final Long tripId = trip.getTripId();

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
      Multimap<JustDate, CargoEvent> tripLayout = splitTripByDate(tripId, tripRange);
      Set<JustDate> eventDates = tripLayout.keySet();

      for (JustDate date : eventDates) {
        Multimap<Long, CargoEvent> dayLayout = splitByCountry(tripLayout.get(date));
        if (!dayLayout.isEmpty()) {
          Widget dayWidget = createTripDayPanel(trip, dayLayout);

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

        if (trips.containsKey(vehicle.getId())) {
          if (separateCargo()) {
            Collection<Trip> vehicleTrips = trips.get(vehicle.getId());

            for (Trip trip : vehicleTrips) {
              if (freights.containsKey(trip.getTripId())) {
                layout.addItems(ChartHelper.getActiveItems(freights.get(trip.getTripId()), range),
                    range, ChartRowLayout.FREIGHT_BLENDER);
              }
            }

          } else {
            layout.addItems(ChartHelper.getActiveItems(trips.get(vehicle.getId()), range), range);
          }
        }

        layout.addInactivity(ChartHelper.getInactivity(vehicle, range), range);
        if (services.containsKey(vehicle.getId())) {
          layout.addInactivity(ChartHelper.getActiveItems(services.get(vehicle.getId()), range),
              range);
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

  private Multimap<Long, CargoEvent> splitByCountry(Collection<CargoEvent> events) {
    Multimap<Long, CargoEvent> result = LinkedListMultimap.create();
    if (BeeUtils.isEmpty(events)) {
      return result;
    }

    for (CargoEvent event : events) {
      if (event.isLoading() && event.isFreightEvent()) {
        result.put(event.getCountryId(), event);
      }
    }

    for (CargoEvent event : events) {
      if (event.isLoading() && event.isHandlingEvent()) {
        result.put(event.getCountryId(), event);
      }
    }
    for (CargoEvent event : events) {
      if (event.isUnloading() && event.isHandlingEvent()) {
        result.put(event.getCountryId(), event);
      }
    }

    for (CargoEvent event : events) {
      if (event.isUnloading() && event.isFreightEvent()) {
        result.put(event.getCountryId(), event);
      }
    }

    return result;
  }

  private Multimap<JustDate, CargoEvent> splitFreightByDate(Freight freight, Range<JustDate> range) {
    Multimap<JustDate, CargoEvent> result = ArrayListMultimap.create();
    if (freight == null || range == null || range.isEmpty()) {
      return result;
    }

    if (freight.getLoadingDate() != null && range.contains(freight.getLoadingDate())) {
      result.put(freight.getLoadingDate(), new CargoEvent(freight, null, true));
    }

    if (freight.getUnloadingDate() != null && range.contains(freight.getUnloadingDate())) {
      result.put(freight.getUnloadingDate(), new CargoEvent(freight, null, false));
    }

    if (handling.containsKey(freight.getCargoId())) {
      for (CargoHandling ch : handling.get(freight.getCargoId())) {
        if (ch.getLoadingDate() != null && range.contains(ch.getLoadingDate())) {
          result.put(ch.getLoadingDate(), new CargoEvent(freight, ch, true));
        }

        if (ch.getUnloadingDate() != null && range.contains(ch.getUnloadingDate())) {
          result.put(ch.getUnloadingDate(), new CargoEvent(freight, ch, false));
        }
      }
    }

    return result;
  }

  private Multimap<JustDate, CargoEvent> splitTripByDate(Long tripId, Range<JustDate> range) {
    Multimap<JustDate, CargoEvent> result = ArrayListMultimap.create();
    if (tripId == null || range == null || range.isEmpty() || !freights.containsKey(tripId)) {
      return result;
    }

    Collection<Freight> tripCargos = freights.get(tripId);
    for (Freight freight : tripCargos) {
      result.putAll(splitFreightByDate(freight, range));
    }

    return result;
  }
}
