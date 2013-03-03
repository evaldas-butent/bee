package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.event.logical.MotionEvent;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.transport.ChartData.Type;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ShippingSchedule extends ChartBase implements MotionEvent.Handler {

  private static class Freight implements HasDateRange, HasColorSource {

    private final Long tripId;
    private final DateTime tripDate;
    private final String tripNo;

    private final Long vehicleId;
    private final String vehicleNumber;

    private final Long trailerId;
    private final String trailerNumber;

    private final JustDate tripDateFrom;
    private final JustDate tripDateTo;

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
      super();

      this.tripId = row.getLong(COL_TRIP_ID);
      this.tripDate = row.getDateTime(ALS_TRIP_DATE);
      this.tripNo = row.getValue(COL_TRIP_NO);

      this.tripDateFrom = row.getDate(COL_TRIP_DATE_FROM);
      this.tripDateTo = row.getDate(COL_TRIP_DATE_TO);

      this.vehicleId = row.getLong(COL_VEHICLE);
      this.vehicleNumber = row.getValue(ALS_VEHICLE_NUMBER);

      this.trailerId = row.getLong(COL_TRAILER);
      this.trailerNumber = row.getValue(ALS_TRAILER_NUMBER);

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

      JustDate start = BeeUtils.nvl(loadingDate, unloadingDate,
          BeeUtils.nvl(tripDateFrom, tripDateTo, tripDate.getDate()));
      JustDate end = BeeUtils.nvl(unloadingDate, loadingDate,
          BeeUtils.nvl(tripDateTo, tripDateFrom, tripDate.getDate()));

      this.range = Range.closed(start, TimeUtils.max(start, end));
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

  static final String SUPPLIER_KEY = "shipping_schedule";
  private static final String DATA_SERVICE = SVC_GET_SS_DATA;

  private static final String STYLE_PREFIX = "bee-tr-ss-";

  private static final String STYLE_VEHICLE_PREFIX = STYLE_PREFIX + "Vehicle-";
  private static final String STYLE_VEHICLE_ROW_SEPARATOR = STYLE_VEHICLE_PREFIX + "row-sep";
  private static final String STYLE_VEHICLE_PANEL = STYLE_VEHICLE_PREFIX + "panel";
  private static final String STYLE_VEHICLE_LABEL = STYLE_VEHICLE_PREFIX + "label";
  private static final String STYLE_VEHICLE_OVER = STYLE_VEHICLE_PREFIX + "over";

  private static final String STYLE_TRIP_PREFIX = STYLE_PREFIX + "Trip-";
  private static final String STYLE_TRIP_ROW_SEPARATOR = STYLE_TRIP_PREFIX + "row-sep";
  private static final String STYLE_TRIP_PANEL = STYLE_TRIP_PREFIX + "panel";
  private static final String STYLE_TRIP_LABEL = STYLE_TRIP_PREFIX + "label";
  private static final String STYLE_TRIP_TRAILER = STYLE_TRIP_PREFIX + "trailer";
  private static final String STYLE_TRIP_OVER = STYLE_TRIP_PREFIX + "over";

  private static final String STYLE_ITEM_PREFIX = STYLE_PREFIX + "Item-";
  private static final String STYLE_ITEM_PANEL = STYLE_ITEM_PREFIX + "panel";
  private static final String STYLE_ITEM_TRIP = STYLE_ITEM_PREFIX + "trip";
  private static final String STYLE_ITEM_CARGO = STYLE_ITEM_PREFIX + "cargo";
  private static final String STYLE_ITEM_LOAD = STYLE_ITEM_PREFIX + "load";
  private static final String STYLE_ITEM_UNLOAD = STYLE_ITEM_PREFIX + "unload";

  private static final String STYLE_ITEM_DRAG = STYLE_ITEM_PREFIX + "drag";
  private static final String STYLE_ITEM_OVER = STYLE_ITEM_PREFIX + "over";

  private static final String STYLE_SERVICE_PREFIX = STYLE_PREFIX + "Service-";
  private static final String STYLE_SERVICE_PANEL = STYLE_SERVICE_PREFIX + "panel";
  private static final String STYLE_SERVICE_LABEL = STYLE_SERVICE_PREFIX + "label";

  private static final int SCROLL_STEP = 2;

  static void open(final Callback<IdentifiableWidget> callback) {
    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(DATA_SERVICE),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            ShippingSchedule ss = new ShippingSchedule();
            ss.onCreate(response, callback);
          }
        });
  }

  private final List<Freight> items = Lists.newArrayList();

  private final Map<Long, String> drivers = Maps.newHashMap();
  private final Map<Long, List<VehicleService>> services = Maps.newHashMap();

  private int vehicleWidth = BeeConst.UNDEF;
  private int tripWidth = BeeConst.UNDEF;

  private boolean separateTrips = false;

  private final Set<String> vehiclePanels = Sets.newHashSet();
  private final Set<String> tripPanels = Sets.newHashSet();

  private final Map<Integer, Long> vehiclesByRow = Maps.newHashMap();
  private final Map<Integer, Long> tripsByRow = Maps.newHashMap();

  private ShippingSchedule() {
    super();
    addStyleName(STYLE_PREFIX + "View");

    setRelevantDataViews(VIEW_TRIPS, VIEW_VEHICLES, VIEW_ORDERS, VIEW_ORDER_CARGO, VIEW_CARGO_TRIPS,
        VIEW_TRIP_CARGO, VIEW_TRIP_DRIVERS, VIEW_VEHICLE_SERVICES, CommonsConstants.VIEW_COLORS,
        CommonsConstants.VIEW_THEME_COLORS);
  }

  @Override
  public String getCaption() {
    return "Reisų kalendorius";
  }

  @Override
  public String getIdPrefix() {
    return "tr-ss";
  }

  @Override
  public String getSupplierKey() {
    return SUPPLIER_KEY;
  }

  @Override
  public void handleAction(Action action) {
    if (Action.ADD.equals(action)) {
      RowFactory.createRow(VIEW_TRIPS);
    } else {
      super.handleAction(action);
    }
  }

  @Override
  public void onMotion(MotionEvent event) {
    if (!DATA_TYPE_CARGO.equals(event.getDataType())) {
      return;
    }

    Element panel = getScrollArea();

    if (event.getDirectionY() != null && panel != null
        && BeeUtils.betweenInclusive(event.getCurrentX(), panel.getAbsoluteLeft(),
            panel.getAbsoluteRight())) {

      int panelTop = panel.getAbsoluteTop();
      int panelheight = panel.getClientHeight();

      int oldPos = panel.getScrollTop();
      int scrollHeight = panel.getScrollHeight();

      int y = event.getCurrentY();
      int rh = getRowHeight();

      int newPos = BeeConst.UNDEF;

      if (oldPos > 0 && BeeUtils.betweenExclusive(y, panelTop - rh, panelTop)
          && event.getDirectionY() == Direction.NORTH) {
        newPos = Math.max(oldPos - SCROLL_STEP, 0);

      } else if (panelheight < scrollHeight
          && BeeUtils.betweenInclusive(y, panelTop + panelheight + 1, panelTop + panelheight + rh)
          && event.getDirectionY() == Direction.SOUTH) {
        newPos = Math.min(oldPos + SCROLL_STEP, scrollHeight - panelheight);
      }

      if (newPos >= 0 && newPos != oldPos) {
        panel.setScrollTop(newPos);
      }
    }
  }

  @Override
  protected boolean filter(DialogBox dialog) {
    Predicate<Freight> predicate = getPredicate();
    
    List<Integer> match = Lists.newArrayList();
    if (predicate != null) {
      for (int i = 0; i < items.size(); i++) {
        if (predicate.apply(items.get(i))) {
          match.add(i);
        }
      }
      
      if (match.isEmpty()) {
        return false;
      }
      if (match.size() >= items.size()) {
        match.clear();
      }
    }
    
    dialog.close();
    
    updateFilteredIndexes(match);
    return true;
  }

  @Override
  protected String getBarHeightColumnName() {
    return COL_SS_BAR_HEIGHT;
  }

  @Override
  protected Collection<? extends HasDateRange> getChartItems() {
    if (isFiltered()) {
      List<Freight> result = Lists.newArrayList();
      for (int index : getFilteredIndexes()) {
        result.add(items.get(index));
      }
      return result;
      
    } else {
      return items;
    }
  }

  @Override
  protected String getDataService() {
    return DATA_SERVICE;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.ADD, Action.CONFIGURE, Action.FILTER);
  }

  @Override
  protected String getFooterHeightColumnName() {
    return COL_SS_FOOTER_HEIGHT;
  }

  @Override
  protected String getHeaderHeightColumnName() {
    return COL_SS_HEADER_HEIGHT;
  }

  @Override
  protected String getSettingsFormName() {
    return FORM_SS_SETTINGS;
  }

  @Override
  protected String getSliderWidthColumnName() {
    return COL_SS_SLIDER_WIDTH;
  }

  @Override
  protected String getStripOpacityColumnName() {
    return COL_SS_STRIP_OPACITY;
  }

  @Override
  protected String getThemeColumnName() {
    return COL_SS_THEME;
  }

  @Override
  protected void initData(BeeRowSet rowSet) {
    drivers.clear();
    services.clear();

    if (rowSet == null) {
      return;
    }

    String serialized = rowSet.getTableProperty(PROP_DRIVERS);
    if (!BeeUtils.isEmpty(serialized)) {
      String[] arr = Codec.beeDeserializeCollection(serialized);
      if (arr != null) {
        for (int i = 0; i < arr.length - 1; i += 2) {
          if (BeeUtils.isLong(arr[i]) && !BeeUtils.isEmpty(arr[i + 1])) {
            drivers.put(BeeUtils.toLong(arr[i]), arr[i + 1]);
          }
        }
      }
    }

    serialized = rowSet.getTableProperty(PROP_VEHICLE_SERVICES);
    if (!BeeUtils.isEmpty(serialized)) {
      SimpleRowSet vsData = SimpleRowSet.restore(serialized);

      List<VehicleService> vs = Lists.newArrayList();
      Long lastVehicle = null;

      for (SimpleRow row : vsData) {
        VehicleService service = new VehicleService(row);

        if (!Objects.equal(service.getVehicleId(), lastVehicle)) {
          if (!vs.isEmpty()) {
            services.put(lastVehicle, Lists.newArrayList(vs));
            vs.clear();
          }
          lastVehicle = service.getVehicleId();
        }

        vs.add(service);
      }

      if (!vs.isEmpty()) {
        services.put(lastVehicle, Lists.newArrayList(vs));
      }
    }
  }
  
  @Override
  protected List<ChartData> initFilter() {
    if (items.isEmpty()) {
      return super.initFilter();
    }
    
    ChartData vehicleData = new ChartData(Type.VEHICLE);
    ChartData trailerData = new ChartData(Type.TRAILER);
    ChartData driverData = new ChartData(Type.DRIVER);

    ChartData customerData = new ChartData(Type.CUSTOMER);
    ChartData orderData = new ChartData(Type.ORDER);

    ChartData loadData = new ChartData(Type.LOADING);
    ChartData unloadData = new ChartData(Type.UNLOADING);
    ChartData cargoData = new ChartData(Type.CARGO);
    
    for (Freight item : items) {
      if (!BeeUtils.isEmpty(item.vehicleNumber)) {
        vehicleData.add(item.vehicleNumber, item.vehicleId);
      }

      if (!BeeUtils.isEmpty(item.trailerNumber)) {
        trailerData.add(item.trailerNumber, item.trailerId);
      }
      
      if (item.tripId != null) {
        String drv = drivers.get(item.tripId);
        if (!BeeUtils.isEmpty(drv)) {
          driverData.add(drv);
        }
      }
      
      if (!BeeUtils.isEmpty(item.customerName)) {
        customerData.add(item.customerName);
      }
      if (!BeeUtils.isEmpty(item.orderNo)) {
        orderData.add(item.orderNo);
      }

      String loading = getPlaceLabel(item.loadingCountry, item.loadingPlace, item.loadingTerminal);
      if (!BeeUtils.isEmpty(loading)) {
        loadData.add(loading);
      }

      String unloading = getPlaceLabel(item.unloadingCountry, item.unloadingPlace,
          item.unloadingTerminal);
      if (!BeeUtils.isEmpty(unloading)) {
        unloadData.add(unloading);
      }
      
      if (!BeeUtils.isEmpty(item.cargoDescription)) {
        cargoData.add(item.cargoDescription);
      }
    }
    
    List<ChartData> result = Lists.newArrayList();

    if (!vehicleData.isEmpty()) {
      result.add(vehicleData);
    }
    if (!trailerData.isEmpty()) {
      result.add(trailerData);
    }
    if (!driverData.isEmpty()) {
      result.add(driverData);
    }
    
    if (!customerData.isEmpty()) {
      result.add(customerData);
    }
    if (!orderData.isEmpty()) {
      result.add(orderData);
    }

    if (!loadData.isEmpty()) {
      result.add(loadData);
    }
    if (!unloadData.isEmpty()) {
      result.add(unloadData);
    }

    if (!cargoData.isEmpty()) {
      result.add(cargoData);
    }
    
    return result;
  }

  @Override
  protected Collection<? extends HasDateRange> initItems(SimpleRowSet data) {
    items.clear();
    for (SimpleRow row : data) {
      items.add(new Freight(row));
    }

    return items;
  }

  @Override
  protected void onDoubleClickChart(int row, JustDate date) {
    Long vehicleId = vehiclesByRow.get(row);

    if (vehicleId != null && TimeUtils.isMeq(date, TimeUtils.today())) {
      DataInfo dataInfo = Data.getDataInfo(VIEW_TRIPS);
      BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);

      if (TimeUtils.isMore(date, TimeUtils.today())) {
        newRow.setValue(dataInfo.getColumnIndex(COL_TRIP_DATE), date.getDateTime());
      }

      newRow.setValue(dataInfo.getColumnIndex(COL_VEHICLE), vehicleId);
      newRow.setValue(dataInfo.getColumnIndex(COL_VEHICLE_NUMBER), findVehicleNumber(vehicleId));

      if (tripsByRow.containsKey(row)) {
        List<Freight> tripItems = filterByTrip(tripsByRow.get(row));
        if (!tripItems.isEmpty()) {
          Freight item = tripItems.get(0);

          if (item.trailerId != null) {
            newRow.setValue(dataInfo.getColumnIndex(COL_TRAILER), item.trailerId);
            newRow.setValue(dataInfo.getColumnIndex(COL_TRAILER_NUMBER), item.trailerNumber);
          }
        }
      }

      RowFactory.createRow(dataInfo, newRow);
    }
  }

  @Override
  protected void prepareChart(Size canvasSize) {
    setVehicleWidth(ChartHelper.getPixels(getSettings(), COL_SS_PIXELS_PER_TRUCK, 80,
        ChartHelper.DEFAULT_MOVER_WIDTH + 1, canvasSize.getWidth() / 3));

    setSeparateTrips(ChartHelper.getBoolean(getSettings(), COL_SS_SEPARATE_TRIPS));
    if (separateTrips()) {
      setTripWidth(ChartHelper.getPixels(getSettings(), COL_SS_PIXELS_PER_TRIP, 80,
          ChartHelper.DEFAULT_MOVER_WIDTH + 1, canvasSize.getWidth() / 3));
    } else {
      setTripWidth(0);
    }

    setChartLeft(getVehicleWidth() + getTripWidth());
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());

    setDayColumnWidth(ChartHelper.getPixels(getSettings(), COL_SS_PIXELS_PER_DAY, 20,
        1, getChartWidth()));

    setRowHeight(ChartHelper.getPixels(getSettings(), COL_SS_PIXELS_PER_ROW, 20,
        1, getScrollAreaHeight(canvasSize.getHeight()) / 2));
  }

  @Override
  protected List<HandlerRegistration> register() {
    List<HandlerRegistration> list = super.register();
    list.add(MotionEvent.register(this));

    return list;
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    List<List<Freight>> layoutRows = doLayout();
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

    Long lastVehicle = null;
    Long lastTrip = null;

    IdentifiableWidget vehicleWidget = null;
    IdentifiableWidget tripWidget = null;

    int vehicleStartRow = 0;
    int tripStartRow = 0;

    Double itemOpacity = ChartHelper.getOpacity(getSettings(), COL_SS_ITEM_OPACITY);

    vehiclePanels.clear();
    tripPanels.clear();

    vehiclesByRow.clear();
    tripsByRow.clear();

    for (int row = 0; row < layoutRows.size(); row++) {
      List<Freight> rowItems = layoutRows.get(row);
      int top = row * getRowHeight();

      Freight rowItem = rowItems.get(0);

      if (row == 0) {
        vehicleWidget = createVehicleWidget(rowItem);
        vehicleStartRow = row;
        lastVehicle = rowItem.vehicleId;

        if (separateTrips()) {
          tripWidget = createTripWidget(rowItem);
        }
        tripStartRow = row;
        lastTrip = rowItem.tripId;

      } else {
        boolean vehicleChanged = !Objects.equal(lastVehicle, rowItem.vehicleId);
        boolean tripChanged = vehicleChanged || !Objects.equal(lastTrip, rowItem.tripId);

        if (vehicleChanged) {
          addVehicleWidget(panel, vehicleWidget, lastVehicle, vehicleStartRow, row - 1);
          addVehicleServices(panel, lastVehicle, vehicleStartRow, row - 1);

          vehicleWidget = createVehicleWidget(rowItem);

          vehicleStartRow = row;
          lastVehicle = rowItem.vehicleId;
        }

        if (tripChanged) {
          if (separateTrips()) {
            addTripWidget(panel, tripWidget, lastTrip, tripStartRow, row - 1);
            tripWidget = createTripWidget(rowItem);
          }

          tripStartRow = row;
          lastTrip = rowItem.tripId;
        }

        if (vehicleChanged) {
          ChartHelper.addRowSeparator(panel, STYLE_VEHICLE_ROW_SEPARATOR, top, 0,
              getVehicleWidth() + getTripWidth() + calendarWidth);
        } else if (tripChanged && separateTrips()) {
          ChartHelper.addRowSeparator(panel, STYLE_TRIP_ROW_SEPARATOR, top, getVehicleWidth(),
              getTripWidth() + calendarWidth);
        } else {
          ChartHelper.addRowSeparator(panel, top, getChartLeft(), calendarWidth);
        }
      }

      for (Freight item : rowItems) {
        JustDate start = TimeUtils.clamp(item.getRange().lowerEndpoint(), firstDate, lastDate);
        JustDate end = TimeUtils.clamp(item.getRange().upperEndpoint(), firstDate, lastDate);

        int left = getChartLeft() + TimeUtils.dayDiff(firstDate, start) * getDayColumnWidth();
        int width = (TimeUtils.dayDiff(start, end) + 1) * getDayColumnWidth();

        Rectangle rectangle = new Rectangle(left, top, width,
            getRowHeight() - ChartHelper.ROW_SEPARATOR_HEIGHT);

        Widget itemWidget = createItemWidget(item);
        rectangle.applyTo(itemWidget);
        if (itemOpacity != null) {
          StyleUtils.setOpacity(itemWidget, itemOpacity);
        }

        panel.add(itemWidget);
      }
    }

    int lastRow = layoutRows.size() - 1;

    if (vehicleWidget != null) {
      addVehicleWidget(panel, vehicleWidget, lastVehicle, vehicleStartRow, lastRow);
      addVehicleServices(panel, lastVehicle, vehicleStartRow, lastRow);
    }
    if (tripWidget != null) {
      addTripWidget(panel, tripWidget, lastTrip, tripStartRow, lastRow);
    }

    ChartHelper.addBottomSeparator(panel, height, 0, getChartLeft() + calendarWidth);

    renderMovers(panel, height);
  }

  private void addTripWidget(HasWidgets panel, IdentifiableWidget widget, Long tripId,
      int firstRow, int lastRow) {

    Rectangle rectangle = ChartHelper.getRectangle(getVehicleWidth(), getTripWidth(),
        firstRow, lastRow, getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget.asWidget(), rectangle, margins);
    panel.add(widget.asWidget());

    tripPanels.add(widget.getId());
    for (int row = firstRow; row <= lastRow; row++) {
      tripsByRow.put(row, tripId);
    }
  }

  private void addVehicleServices(HasWidgets panel, Long vehicleId, int firstRow, int lastRow) {
    if (services.containsKey(vehicleId)) {
      JustDate firstDate = getVisibleRange().lowerEndpoint();
      JustDate lastDate = getVisibleRange().upperEndpoint();

      List<VehicleService> serviceItems = services.get(vehicleId);

      for (VehicleService item : serviceItems) {
        if (BeeUtils.intersects(getVisibleRange(), item.getRange())) {
          JustDate start = TimeUtils.clamp(item.getRange().lowerEndpoint(), firstDate, lastDate);
          JustDate end = TimeUtils.clamp(item.getRange().upperEndpoint(), firstDate, lastDate);

          int left = getChartLeft() + TimeUtils.dayDiff(firstDate, start) * getDayColumnWidth();
          int width = (TimeUtils.dayDiff(start, end) + 1) * getDayColumnWidth();

          int top = firstRow * getRowHeight();
          int height = (lastRow - firstRow + 1) * getRowHeight();

          Rectangle rectangle = new Rectangle(left, top, width,
              height - ChartHelper.ROW_SEPARATOR_HEIGHT);

          Widget serviceWidget = createServiceWidget(item);
          rectangle.applyTo(serviceWidget);

          panel.add(serviceWidget);
        }
      }
    }
  }

  private void addVehicleWidget(HasWidgets panel, IdentifiableWidget widget, Long vehicleId,
      int firstRow, int lastRow) {

    Rectangle rectangle = ChartHelper.getRectangle(0, getVehicleWidth(), firstRow, lastRow,
        getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget.asWidget(), rectangle, margins);
    panel.add(widget.asWidget());

    vehiclePanels.add(widget.getId());
    for (int row = firstRow; row <= lastRow; row++) {
      vehiclesByRow.put(row, vehicleId);
    }
  }

  private void assignCargoToTrip(long cargoId, Long sourceTrip, long targetTrip) {
    final String viewName = VIEW_CARGO_TRIPS;

    if (sourceTrip == null) {
      List<BeeColumn> columns = Data.getColumns(viewName, Lists.newArrayList(COL_CARGO, COL_TRIP));
      List<String> values = Lists.newArrayList(BeeUtils.toString(cargoId),
          BeeUtils.toString(targetTrip));

      Queries.insert(viewName, columns, values, new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          BeeKeeper.getBus().fireEvent(new RowInsertEvent(viewName, result));
        }
      });

    } else {
      Freight sourceItem = null;
      for (Freight item : items) {
        if (Objects.equal(cargoId, item.cargoId)) {
          sourceItem = item;
          break;
        }
      }

      if (sourceItem == null) {
        LogUtils.getRootLogger().warning("cargo source not found:", cargoId, sourceTrip);
        return;
      }

      List<BeeColumn> columns = Data.getColumns(viewName, Lists.newArrayList(COL_TRIP));
      List<String> oldValues = Lists.newArrayList(BeeUtils.toString(sourceTrip));
      List<String> newValues = Lists.newArrayList(BeeUtils.toString(targetTrip));

      Queries.update(viewName, sourceItem.cargoTripId, sourceItem.cargoTripVersion,
          columns, oldValues, newValues, new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(viewName, result));
            }
          });
    }
  }

  private String buildTripTitle(Freight item) {
    return ChartHelper.buildTitle("Reiso Nr.", item.tripNo,
        "Vilkikas", item.vehicleNumber, "Puspriekabė", item.trailerNumber,
        "Vairuotojai", drivers.get(item.tripId));
  }

  private Widget createItemWidget(final Freight item) {
    final Flow panel = new Flow();
    panel.addStyleName(STYLE_ITEM_PANEL);
    setItemWidgetColor(item, panel);

    final Long tripId = item.tripId;
    Long cargoId = item.cargoId;

    panel.addStyleName((cargoId == null) ? STYLE_ITEM_TRIP : STYLE_ITEM_CARGO);

    String loading = getPlaceLabel(item.loadingCountry, item.loadingPlace, item.loadingTerminal);
    String unloading = getPlaceLabel(item.unloadingCountry, item.unloadingPlace,
        item.unloadingTerminal);

    String loadTitle = BeeUtils.emptyToNull(BeeUtils.joinWords(item.loadingDate, loading));
    String unloadTitle = BeeUtils.emptyToNull(BeeUtils.joinWords(item.unloadingDate, unloading));

    final String tripTitle = buildTripTitle(item);

    String cargoTitle = (cargoId == null) ? null : ChartHelper.buildTitle(
        "Užsakymo Nr.", item.orderNo, "Užsakovas", item.customerName,
        "Krovinys", item.cargoDescription,
        "Pakrovimas", loadTitle, "Iškrovimas", unloadTitle);

    String itemTitle = BeeUtils.buildLines(tripTitle, cargoTitle);
    panel.setTitle(itemTitle);

    panel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_TRIPS, tripId);
      }
    });

    if (cargoId != null) {
      DndHelper.makeSource(panel, DATA_TYPE_CARGO, cargoId, tripId, itemTitle, STYLE_ITEM_DRAG,
          true);
    }

    DndHelper.makeTarget(panel, DATA_TYPE_CARGO, STYLE_ITEM_OVER,
        new Predicate<Long>() {
          @Override
          public boolean apply(Long input) {
            return !Objects.equal(tripId, DndHelper.getRelatedId());
          }
        }, new Procedure<Long>() {
          @Override
          public void call(Long parameter) {
            dropCargoOnTrip(parameter, tripId, tripTitle, panel, STYLE_ITEM_OVER);
          }
        });

    if (!BeeUtils.isEmpty(loading)) {
      BeeLabel loadingLabel = new BeeLabel(loading);
      loadingLabel.addStyleName(STYLE_ITEM_LOAD);

      panel.add(loadingLabel);
    }

    if (!BeeUtils.isEmpty(unloading)) {
      BeeLabel unloadingLabel = new BeeLabel(unloading);
      unloadingLabel.addStyleName(STYLE_ITEM_UNLOAD);

      panel.add(unloadingLabel);
    }

    return panel;
  }

  private Widget createServiceWidget(VehicleService service) {
    Flow panel = new Flow();
    panel.addStyleName(STYLE_SERVICE_PANEL);

    panel.setTitle(BeeUtils.buildLines(service.getVehicleNumber(),
        service.getName(), service.getNotes()));

    final Long vehicleId = service.getVehicleId();
    panel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_VEHICLES, vehicleId);
      }
    });

    BeeLabel label = new BeeLabel(service.getName());
    label.addStyleName(STYLE_SERVICE_LABEL);

    panel.add(label);

    return panel;
  }

  private IdentifiableWidget createTripWidget(Freight item) {
    final Flow panel = new Flow();
    panel.addStyleName(STYLE_TRIP_PANEL);

    final Long tripId = item.tripId;
    final String tripTitle = buildTripTitle(item);

    DndHelper.makeTarget(panel, DATA_TYPE_CARGO, STYLE_TRIP_OVER,
        new Predicate<Long>() {
          @Override
          public boolean apply(Long input) {
            return !Objects.equal(tripId, DndHelper.getRelatedId());
          }
        }, new Procedure<Long>() {
          @Override
          public void call(Long parameter) {
            dropCargoOnTrip(parameter, tripId, tripTitle, panel, STYLE_TRIP_OVER);
          }
        });

    BeeLabel label = new BeeLabel(item.tripNo);
    label.addStyleName(STYLE_TRIP_LABEL);

    label.setTitle(tripTitle);

    label.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_TRIPS, tripId);
      }
    });

    panel.add(label);

    if (item.trailerId != null) {
      BeeLabel trailer = new BeeLabel(item.trailerNumber);
      trailer.addStyleName(STYLE_TRIP_TRAILER);

      final Long trailerId = item.trailerId;

      trailer.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          openDataRow(event, VIEW_VEHICLES, trailerId);
        }
      });

      panel.add(trailer);
    }

    return panel;
  }

  private IdentifiableWidget createVehicleWidget(Freight item) {
    final Simple panel = new Simple();
    panel.addStyleName(STYLE_VEHICLE_PANEL);

    final Long vehicleId = item.vehicleId;
    final String vehicleNumber = item.vehicleNumber;

    DndHelper.makeTarget(panel, DATA_TYPE_CARGO, STYLE_VEHICLE_OVER,
        DndHelper.alwaysTarget, new Procedure<Long>() {
          @Override
          public void call(Long parameter) {
            dropCargoOnVehicle(parameter, vehicleId, vehicleNumber, panel, STYLE_VEHICLE_OVER);
          }
        });

    BeeLabel widget = new BeeLabel(vehicleNumber);
    widget.addStyleName(STYLE_VEHICLE_LABEL);

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_VEHICLES, vehicleId);
      }
    });

    panel.add(widget);

    return panel;
  }

  private List<List<Freight>> doLayout() {
    List<List<Freight>> rows = Lists.newArrayList();

    Long lastId = null;
    List<Freight> rowItems = Lists.newArrayList();

    for (int i = 0; i < items.size(); i++) {
      if (isFiltered() && !getFilteredIndexes().contains(i)) {
        continue;
      }
      Freight item = items.get(i);

      if (BeeUtils.intersects(getVisibleRange(), item.getRange())) {

        Long id = separateTrips() ? item.tripId : item.vehicleId;

        if (!Objects.equal(id, lastId) || BeeUtils.intersects(rowItems, item.getRange())) {
          if (!rowItems.isEmpty()) {
            rows.add(Lists.newArrayList(rowItems));
            rowItems.clear();
          }

          lastId = id;
        }

        rowItems.add(item);
      }
    }

    if (!rowItems.isEmpty()) {
      rows.add(Lists.newArrayList(rowItems));
    }
    return rows;
  }

  private void dropCargoOnTrip(final Long cargoId, final Long targetTrip, String targetDescription,
      final Widget targetWidget, final String targetStyle) {

    final Long sourceTrip = DndHelper.getRelatedId();
    String sourceDescription = DndHelper.getDataDescription();

    if (!DataUtils.isId(cargoId) || !DataUtils.isId(targetTrip)) {
      return;
    }
    if (Objects.equal(sourceTrip, targetTrip)) {
      return;
    }

    List<String> messages = Lists.newArrayList("KROVINYS:", sourceDescription, "REISAS:",
        targetDescription);

    List<Freight> targetItems = filterByTrip(targetTrip);
    List<String> targetCargo = Lists.newArrayList();

    for (Freight item : targetItems) {
      if (item.cargoId != null) {
        String loading = BeeUtils.joinWords(item.loadingDate,
            getPlaceLabel(item.loadingCountry, item.loadingPlace, item.loadingTerminal));
        String unloading = BeeUtils.joinWords(item.unloadingDate,
            getPlaceLabel(item.unloadingCountry, item.unloadingPlace, item.unloadingTerminal));

        String message = ChartHelper.buildMessage(BeeConst.DEFAULT_LIST_SEPARATOR,
            "Krovinys", item.cargoDescription, "Pakrovimas", loading, "Iškrovimas", unloading);
        if (!BeeUtils.isEmpty(message)) {
          targetCargo.add(message);
        }
      }
    }

    if (!targetCargo.isEmpty()) {
      messages.add(BeeUtils.joinWords("REISO KROVINIAI:", BeeUtils.bracket(targetCargo.size())));
      messages.add(BeeUtils.join(BeeConst.STRING_EOL, targetCargo));
    }

    messages.add("Priskirti krovinį reisui ?");

    Global.confirm("Krovinio priskyrimas reisui", Icon.QUESTION, messages,
        new ConfirmationCallback() {
          @Override
          public void onCancel() {
            reset();
          }

          @Override
          public void onConfirm() {
            reset();
            assignCargoToTrip(cargoId, sourceTrip, targetTrip);
          }

          private void reset() {
            if (targetWidget != null && !BeeUtils.isEmpty(targetStyle)) {
              targetWidget.removeStyleName(targetStyle);
            }
          }
        });
  }

  private void dropCargoOnVehicle(final Long cargoId, final Long vehicleId,
      String vehicleNumber, final Widget targetWidget, final String targetStyle) {

    final Long sourceTrip = DndHelper.getRelatedId();
    String sourceDescription = DndHelper.getDataDescription();

    if (!DataUtils.isId(cargoId) || !DataUtils.isId(vehicleId)) {
      return;
    }

    List<String> messages = Lists.newArrayList("KROVINYS:", sourceDescription, "VILKIKAS:",
        vehicleNumber, "Sukurti kroviniui naują reisą ?");

    Global.confirm("Naujo reiso sukūrimas", Icon.QUESTION, messages, new ConfirmationCallback() {
      @Override
      public void onCancel() {
        reset();
      }

      @Override
      public void onConfirm() {
        reset();

        final String viewName = VIEW_TRIPS;
        final DataInfo dataInfo = Data.getDataInfo(viewName);

        BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);
        newRow.setValue(dataInfo.getColumnIndex(COL_VEHICLE), vehicleId);

        Queries.insert(viewName, dataInfo.getColumns(), newRow, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            BeeKeeper.getBus().fireEvent(new RowInsertEvent(viewName, result));
            assignCargoToTrip(cargoId, sourceTrip, result.getId());

            BeeKeeper.getScreen().notifyInfo("Sukurtas naujas reisas",
                "Nr. " + result.getString(dataInfo.getColumnIndex(COL_TRIP_NO)));
          }
        });
      }

      private void reset() {
        if (targetWidget != null && !BeeUtils.isEmpty(targetStyle)) {
          targetWidget.removeStyleName(targetStyle);
        }
      }
    });
  }

  private List<Freight> filterByTrip(Long tripId) {
    List<Freight> result = Lists.newArrayList();

    for (Freight item : items) {
      if (Objects.equal(item.tripId, tripId)) {
        result.add(item);
      }
    }

    return result;
  }

  private String findVehicleNumber(Long vehicleId) {
    if (services.containsKey(vehicleId)) {
      List<VehicleService> vsList = services.get(vehicleId);
      if (!vsList.isEmpty()) {
        return vsList.get(0).getVehicleNumber();
      }
    }

    for (Freight item : items) {
      if (Objects.equal(item.vehicleId, vehicleId)) {
        return item.vehicleNumber;
      }
    }

    return null;
  }

  private Predicate<Freight> getPredicate() {
    List<Predicate<Freight>> predicates = Lists.newArrayList();

    for (ChartData data : getFilterData()) {
      if (data.size() <= 1) {
        continue;
      }
      
      final Collection<String> selectedNames = data.getSelectedNames();
      if (selectedNames.isEmpty() || selectedNames.size() >= data.size()) {
        continue;
      }
      
      Predicate<Freight> predicate;
      switch (data.getType()) {
        case VEHICLE:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              return selectedNames.contains(input.vehicleNumber);
            }
          };
          break;

        case TRAILER:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              return selectedNames.contains(input.trailerNumber);
            }
          };
          break;

        case DRIVER:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              if (input.tripId == null) {
                return false;
              } else {
                return selectedNames.contains(drivers.get(input.tripId));
              }
            }
          };
          break;
          
        case CUSTOMER:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              return selectedNames.contains(input.customerName);
            }
          };
          break;

        case ORDER:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              return selectedNames.contains(input.orderNo);
            }
          };
          break;
          
        case LOADING:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              return selectedNames.contains(getPlaceLabel(input.loadingCountry, input.loadingPlace,
                  input.loadingTerminal));
            }
          };
          break;

        case UNLOADING:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              return selectedNames.contains(getPlaceLabel(input.unloadingCountry,
                  input.unloadingPlace, input.unloadingTerminal));
            }
          };
          break;
          
        case CARGO:
          predicate = new Predicate<Freight>() {
            @Override
            public boolean apply(Freight input) {
              return selectedNames.contains(input.cargoDescription);
            }
          };
          break;
          
        default:
          Assert.untouchable();
          predicate = null;
      }
      
      if (predicate != null) {
        predicates.add(predicate);
      }
    }

    if (predicates.isEmpty()) {
      return null;
    } else if (predicates.size() == 1) {
      return predicates.get(0);
    } else {
      return Predicates.and(predicates);
    }
  }

  private int getTripWidth() {
    return tripWidth;
  }

  private int getVehicleWidth() {
    return vehicleWidth;
  }

  private void onTripResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft = getVehicleWidth() + 300;
    if (getChartWidth() > 0) {
      maxLeft = Math.min(maxLeft, getChartLeft() + getChartWidth() / 2);
    }

    int newLeft = BeeUtils.clamp(oldLeft + delta, getVehicleWidth() + 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      int tripPx = newLeft - getVehicleWidth() + ChartHelper.DEFAULT_MOVER_WIDTH;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : tripPanels) {
          StyleUtils.setWidth(id, tripPx - ChartHelper.DEFAULT_MOVER_WIDTH);
        }
      }

      if (event.isFinished() && updateSetting(COL_SS_PIXELS_PER_TRIP, tripPx)) {
        setTripWidth(tripPx);
        render(false);
      }
    }
  }

  private void onVehicleResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft;
    if (separateTrips()) {
      maxLeft = getChartLeft() - ChartHelper.DEFAULT_MOVER_WIDTH * 2 - 1;
    } else {
      maxLeft = 300;
      if (getChartWidth() > 0) {
        maxLeft = Math.min(maxLeft, getChartLeft() + getChartWidth() / 2);
      }
    }

    int newLeft = BeeUtils.clamp(oldLeft + delta, 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      int vehiclePx = newLeft + ChartHelper.DEFAULT_MOVER_WIDTH;
      int tripPx = separateTrips() ? getChartLeft() - vehiclePx : BeeConst.UNDEF;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : vehiclePanels) {
          StyleUtils.setWidth(id, vehiclePx - ChartHelper.DEFAULT_MOVER_WIDTH);
        }

        if (separateTrips()) {
          for (String id : tripPanels) {
            Element element = Document.get().getElementById(id);
            if (element != null) {
              StyleUtils.setLeft(element, vehiclePx);
              StyleUtils.setWidth(element, tripPx - ChartHelper.DEFAULT_MOVER_WIDTH);
            }
          }
        }
      }

      if (event.isFinished()) {
        if (separateTrips()) {
          if (updateSettings(COL_SS_PIXELS_PER_TRUCK, vehiclePx, COL_SS_PIXELS_PER_TRIP, tripPx)) {
            setVehicleWidth(vehiclePx);
            setTripWidth(tripPx);
          }

        } else if (updateSetting(COL_SS_PIXELS_PER_TRUCK, vehiclePx)) {
          setVehicleWidth(vehiclePx);
          render(false);
        }
      }
    }
  }

  private void renderMovers(HasWidgets panel, int height) {
    Mover vehicleMover = ChartHelper.createHorizontalMover();
    StyleUtils.setLeft(vehicleMover, getVehicleWidth() - ChartHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(vehicleMover, height);

    vehicleMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onVehicleResize(event);
      }
    });

    panel.add(vehicleMover);

    if (separateTrips()) {
      Mover tripMover = ChartHelper.createHorizontalMover();
      StyleUtils.setLeft(tripMover, getChartLeft() - ChartHelper.DEFAULT_MOVER_WIDTH);
      StyleUtils.setHeight(tripMover, height);

      tripMover.addMoveHandler(new MoveEvent.Handler() {
        @Override
        public void onMove(MoveEvent event) {
          onTripResize(event);
        }
      });

      panel.add(tripMover);
    }
  }

  private boolean separateTrips() {
    return separateTrips;
  }

  private void setSeparateTrips(boolean separateTrips) {
    this.separateTrips = separateTrips;
  }
  
  private void setTripWidth(int tripWidth) {
    this.tripWidth = tripWidth;
  }
  
  private void setVehicleWidth(int vehicleWidth) {
    this.vehicleWidth = vehicleWidth;
  }
}
