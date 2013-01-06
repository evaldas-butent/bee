package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
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
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class ShippingSchedule extends ChartBase {

  private static class Freight implements ChartItem, TruckItem {

    private final Long tripId;
    private final DateTime tripDate;
    private final String tripNo;

    private final Long vehicleId;
    private final String vehicleNumber;
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

    @Override
    public Long getVehicleId() {
      return vehicleId;
    }

    @Override
    public String getVehicleNumber() {
      return vehicleNumber;
    }
  }

  private interface TruckItem extends HasDateRange {

    Long getVehicleId();

    String getVehicleNumber();
  }

  private static class VehicleService implements TruckItem {
    private final Long vehicleId;
    private final String vehicleNumber;

    private final JustDate date;
    private final String name;
    private final String notes;

    private final Range<JustDate> range;

    private VehicleService(SimpleRow row) {
      super();

      this.vehicleId = row.getLong(COL_VEHICLE);
      this.vehicleNumber = row.getValue(COL_NUMBER);

      this.date = row.getDate(COL_SERVICE_DATE);
      this.name = row.getValue(COL_SERVICE_NAME);
      this.notes = row.getValue(COL_SERVICE_NOTES);

      this.range = Range.closed(date, date);
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }

    @Override
    public Long getVehicleId() {
      return vehicleId;
    }

    @Override
    public String getVehicleNumber() {
      return vehicleNumber;
    }
  }

  static final String SUPPLIER_KEY = "shipping_schedule";
  private static final String DATA_SERVICE = SVC_GET_SS_DATA;

  private static final String STYLE_PREFIX = "bee-tr-ss-";

  private static final String STYLE_VEHICLE_PREFIX = STYLE_PREFIX + "Vehicle-";
  private static final String STYLE_VEHICLE_COLUMN_SEPARATOR = STYLE_VEHICLE_PREFIX + "col-sep";
  private static final String STYLE_VEHICLE_ROW_SEPARATOR = STYLE_VEHICLE_PREFIX + "row-sep";
  private static final String STYLE_VEHICLE_PANEL = STYLE_VEHICLE_PREFIX + "panel";
  private static final String STYLE_VEHICLE_LABEL = STYLE_VEHICLE_PREFIX + "label";

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

  static void open(final Callback<IdentifiableWidget> callback) {
    Assert.notNull(callback);

    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(DATA_SERVICE),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            ShippingSchedule ss = new ShippingSchedule();
            if (ss.setData(response)) {
              callback.onSuccess(ss);
            } else {
              callback.onFailure(ss.getCaption(), "negavo duomenų iš serverio",
                  Global.CONSTANTS.sorry());
            }
          }
        });
  }

  private final List<Freight> items = Lists.newArrayList();

  private final Map<Long, String> drivers = Maps.newHashMap();
  private final Map<Long, List<VehicleService>> services = Maps.newHashMap();

  private int vehicleWidth = BeeConst.UNDEF;

  private ShippingSchedule() {
    super();
    addStyleName(STYLE_PREFIX + "View");

    setRelevantDataViews(VIEW_TRIPS, VIEW_VEHICLES, VIEW_ORDERS, VIEW_CARGO, VIEW_CARGO_TRIPS,
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
  protected Collection<? extends ChartItem> getChartItems() {
    return items;
  }

  @Override
  protected String getDataService() {
    return DATA_SERVICE;
  }

  @Override
  protected String getSettingsFormName() {
    return FORM_SS_SETTINGS;
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

        if (!Objects.equal(service.vehicleId, lastVehicle)) {
          if (!vs.isEmpty()) {
            services.put(lastVehicle, Lists.newArrayList(vs));
            vs.clear();
          }
          lastVehicle = service.vehicleId;
        }

        vs.add(service);
      }

      if (!vs.isEmpty()) {
        services.put(lastVehicle, Lists.newArrayList(vs));
      }
    }
  }

  @Override
  protected Collection<? extends ChartItem> initItems(SimpleRowSet data) {
    items.clear();
    for (SimpleRow row : data) {
      items.add(new Freight(row));
    }

    return items;
  }

  @Override
  protected void prepareChart(int canvasWidth, int canvasHeight) {
    setVehicleWidth(ChartHelper.getPixels(getSettings(), COL_SS_PIXELS_PER_TRUCK, 80,
        canvasWidth / 5));

    setHeaderHeight(ChartHelper.getPixels(getSettings(), COL_SS_HEADER_HEIGHT, 20,
        canvasHeight / 5));
    setFooterHeight(ChartHelper.getPixels(getSettings(), COL_SS_FOOTER_HEIGHT, 30,
        canvasHeight / 3));

    setChartLeft(getVehicleWidth());
    setChartWidth(canvasWidth - getChartLeft() - getChartRight());

    setDayColumnWidth(ChartHelper.getPixels(getSettings(), COL_SS_PIXELS_PER_DAY, 20));

    int scrollAreaHeight = canvasHeight - getHeaderHeight() - getFooterHeight();
    setRowHeight(ChartHelper.getPixels(getSettings(), COL_SS_PIXELS_PER_ROW, 20,
        scrollAreaHeight / 2));

    int slider = ChartHelper.getPixels(getSettings(), COL_SS_SLIDER_WIDTH, 5);
    setSliderWidth(BeeUtils.clamp(slider, 1, getChartRight()));

    setBarHeight(ChartHelper.getPixels(getSettings(), COL_SS_BAR_HEIGHT, BeeConst.UNDEF,
        getFooterHeight() / 2));
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    List<List<TruckItem>> layoutRows = doLayout();
    if (layoutRows.isEmpty()) {
      return;
    }

    int height = layoutRows.size() * getRowHeight();
    StyleUtils.setHeight(panel, height);

    ChartHelper.addColumnSeparator(panel, STYLE_VEHICLE_COLUMN_SEPARATOR, getVehicleWidth(),
        height);
    ChartHelper.renderDayColumns(panel, getVisibleRange(), getChartLeft(), getDayColumnWidth(),
        height, false, true);

    JustDate firstDate = getVisibleRange().lowerEndpoint();
    JustDate lastDate = getVisibleRange().upperEndpoint();

    Widget vehicleWidget = null;

    Long lastVehicle = null;
    int vehicleStartRow = 0;

    Double itemOpacity = ChartHelper.getOpacity(getSettings(), COL_SS_ITEM_OPACITY);

    for (int row = 0; row < layoutRows.size(); row++) {
      List<TruckItem> rowItems = layoutRows.get(row);
      int top = row * getRowHeight();

      TruckItem firstItem = rowItems.get(0);

      if (row == 0) {
        vehicleWidget = createVehicleWidget(firstItem);

        vehicleStartRow = row;
        lastVehicle = firstItem.getVehicleId();

      } else if (Objects.equal(lastVehicle, firstItem.getVehicleId())) {
        ChartHelper.addRowSeparator(panel, top, getChartLeft(), getChartWidth());

      } else {
        ChartHelper.addLegendWidget(panel, vehicleWidget, 0, getVehicleWidth(),
            vehicleStartRow, row - 1, getRowHeight(),
            ChartHelper.DEFAULT_SEPARATOR_WIDTH, ChartHelper.DEFAULT_SEPARATOR_HEIGHT);

        vehicleWidget = createVehicleWidget(firstItem);

        vehicleStartRow = row;
        lastVehicle = firstItem.getVehicleId();

        ChartHelper.addRowSeparator(panel, STYLE_VEHICLE_ROW_SEPARATOR, top, 0,
            getVehicleWidth() + getChartWidth());
      }

      for (TruckItem item : rowItems) {
        JustDate start = TimeUtils.clamp(item.getRange().lowerEndpoint(), firstDate, lastDate);
        JustDate end = TimeUtils.clamp(item.getRange().upperEndpoint(), firstDate, lastDate);

        int left = getChartLeft() + TimeUtils.dayDiff(firstDate, start) * getDayColumnWidth();
        int width = (TimeUtils.dayDiff(start, end) + 1) * getDayColumnWidth();

        Rectangle rectangle = new Rectangle(left + ChartHelper.DEFAULT_SEPARATOR_WIDTH,
            top + ChartHelper.DEFAULT_SEPARATOR_HEIGHT,
            width - ChartHelper.DEFAULT_SEPARATOR_WIDTH,
            getRowHeight() - ChartHelper.DEFAULT_SEPARATOR_HEIGHT);

        if (item instanceof Freight) {
          Widget itemWidget = createItemWidget((Freight) item);
          rectangle.applyTo(itemWidget);
          if (itemOpacity != null) {
            StyleUtils.setOpacity(itemWidget, itemOpacity);
          }

          panel.add(itemWidget);

        } else if (item instanceof VehicleService) {
          Widget serviceWidget = createServiceWidget((VehicleService) item);
          rectangle.applyTo(serviceWidget);

          panel.add(serviceWidget);
        }
      }
    }

    if (vehicleWidget != null) {
      ChartHelper.addLegendWidget(panel, vehicleWidget, 0, getVehicleWidth(),
          vehicleStartRow, layoutRows.size() - 1, getRowHeight(),
          ChartHelper.DEFAULT_SEPARATOR_WIDTH, ChartHelper.DEFAULT_SEPARATOR_HEIGHT);
    }

    ChartHelper.addBottomSeparator(panel, height, 0, getVehicleWidth() + getChartWidth()
        + ChartHelper.DEFAULT_SEPARATOR_WIDTH);
  }

  @Override
  protected void renderMaxRange(HasWidgets panel) {
  }

  @Override
  protected void renderVisibleRange(HasWidgets panel) {
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

    final String tripTitle = ChartHelper.buildTitle("Reiso Nr.", item.tripNo,
        "Vilkikas", item.vehicleNumber, "Puspriekabė", item.trailerNumber,
        "Vairuotojai", drivers.get(tripId));

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
      DndHelper.makeSource(panel, DndHelper.ContentType.CARGO, cargoId, tripId, itemTitle,
          STYLE_ITEM_DRAG);
    }

    DndHelper.makeTarget(panel, DndHelper.ContentType.CARGO, STYLE_ITEM_OVER,
        new Predicate<Long>() {
          @Override
          public boolean apply(Long input) {
            return !Objects.equal(tripId, DndHelper.getRelatedId());
          }
        }, new Procedure<Long>() {
          @Override
          public void call(Long parameter) {
            onDropCargo(parameter, tripId, tripTitle, panel);
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

    panel.setTitle(BeeUtils.buildLines(service.name, service.notes));

    final Long vehicleId = service.getVehicleId();
    panel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_VEHICLES, vehicleId);
      }
    });

    BeeLabel label = new BeeLabel(service.name);
    label.addStyleName(STYLE_SERVICE_LABEL);

    panel.add(label);

    return panel;
  }

  private Widget createVehicleWidget(TruckItem item) {
    BeeLabel widget = new BeeLabel(item.getVehicleNumber());
    widget.addStyleName(STYLE_VEHICLE_LABEL);

    final Long vehicleId = item.getVehicleId();

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_VEHICLES, vehicleId);
      }
    });

    Simple panel = new Simple(widget);
    panel.addStyleName(STYLE_VEHICLE_PANEL);

    return panel;
  }

  private List<List<TruckItem>> doLayout() {
    List<List<TruckItem>> rows = Lists.newArrayList();

    Long lastVehicle = null;
    List<TruckItem> rowItems = Lists.newArrayList();

    for (Freight item : items) {

      if (!Objects.equal(item.vehicleId, lastVehicle)) {
        lastVehicle = item.vehicleId;

        if (!rowItems.isEmpty()) {
          rows.add(Lists.newArrayList(rowItems));
          rowItems.clear();
        }

        if (services.containsKey(lastVehicle)) {
          List<VehicleService> vs = services.get(lastVehicle);

          for (VehicleService service : vs) {
            if (BeeUtils.intersects(getVisibleRange(), service.getRange())) {
              if (ChartHelper.intersects(rowItems, service.getRange())) {
                rows.add(Lists.newArrayList(rowItems));
                rowItems.clear();
              }
              rowItems.add(service);
            }
          }
        }
      }

      if (BeeUtils.intersects(getVisibleRange(), item.getRange())) {
        if (ChartHelper.intersects(rowItems, item.getRange())) {
          rows.add(Lists.newArrayList(rowItems));
          rowItems.clear();
        }
        rowItems.add(item);
      }
    }

    if (!rowItems.isEmpty()) {
      rows.add(Lists.newArrayList(rowItems));
    }
    return rows;
  }

  private int getVehicleWidth() {
    return vehicleWidth;
  }

  private void onDropCargo(final Long cargoId, final Long targetTrip, String targetDescription,
      final Widget targetWidget) {

    final Long sourceTrip = DndHelper.getRelatedId();
    String sourceDescription = DndHelper.getDataDescription();

    if (!DataUtils.isId(cargoId) || !DataUtils.isId(targetTrip)) {
      return;
    }
    if (Objects.equal(sourceTrip, targetTrip)) {
      return;
    }
    
    List<String> messages = Lists.newArrayList("KROVINYS:", sourceDescription, "REISAS:",
        targetDescription, "Priskirti krovinį reisui ?");
    
    Global.getMsgBoxen().decide("Krovinio priskyrimas reisui", messages, new DecisionCallback() {
      @Override
      public void onCancel() {
        reset();
      }
      
      @Override
      public void onConfirm() {
        reset();
        
        final String viewName = VIEW_CARGO_TRIPS;
        
        if (sourceTrip == null) {
          List<BeeColumn> columns = Data.getColumns(viewName,
              Lists.newArrayList(COL_CARGO, COL_TRIP));
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

      @Override
      public void onDeny() {
        reset();
      }

      private void reset() {
        if (targetWidget != null) {
          targetWidget.removeStyleName(STYLE_ITEM_OVER);
        }
      }
    });
  }

  private void setVehicleWidth(int vehicleWidth) {
    this.vehicleWidth = vehicleWidth;
  }
}
