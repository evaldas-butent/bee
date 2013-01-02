package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
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
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class ShippingSchedule extends ChartBase {

  private static class Freight implements ChartItem {

    private final Long tripId;
    private final DateTime tripDate;
    private final String tripNo;

    private final Long vehicleId;
    private final String vehicleNumber;
    private final String trailerNumber;

    private final JustDate tripDateFrom;
    private final JustDate tripDateTo;

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

      this.cargoDescription = row.getValue(COL_DESCRIPTION);

      this.loadingDate = row.getDate(loadingColumnAlias(COL_PLACE_DATE));
      this.loadingCountry = row.getLong(loadingColumnAlias(COL_COUNTRY));
      this.loadingPlace = row.getValue(loadingColumnAlias(COL_PLACE_NAME));
      this.loadingTerminal = row.getValue(loadingColumnAlias(COL_TERMINAL));

      this.unloadingDate = row.getDate(unloadingColumnAlias(COL_PLACE_DATE));
      this.unloadingCountry = row.getLong(unloadingColumnAlias(COL_COUNTRY));
      this.unloadingPlace = row.getValue(unloadingColumnAlias(COL_PLACE_NAME));
      this.unloadingTerminal = row.getValue(unloadingColumnAlias(COL_TERMINAL));

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
  private static final String STYLE_VEHICLE_COLUMN_SEPARATOR = STYLE_VEHICLE_PREFIX + "col-sep";
  private static final String STYLE_VEHICLE_PANEL = STYLE_VEHICLE_PREFIX + "panel";
  private static final String STYLE_VEHICLE_LABEL = STYLE_VEHICLE_PREFIX + "label";

  private static final String STYLE_ITEM_PREFIX = STYLE_PREFIX + "Item-";
  private static final String STYLE_ITEM_PANEL = STYLE_ITEM_PREFIX + "panel";
  private static final String STYLE_ITEM_LOAD = STYLE_ITEM_PREFIX + "load";
  private static final String STYLE_ITEM_UNLOAD = STYLE_ITEM_PREFIX + "unload";

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

  private int vehicleWidth = BeeConst.UNDEF;

  private ShippingSchedule() {
    super();
    addStyleName(STYLE_PREFIX + "View");
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
  protected String getThemeColumnName() {
    return COL_SS_THEME;
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
    List<List<Freight>> layoutRows = doLayout();
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

    for (int row = 0; row < layoutRows.size(); row++) {
      List<Freight> rowItems = layoutRows.get(row);
      int top = row * getRowHeight();

      Widget vehicleWidget = createvehicleWidget(rowItems.get(0));
      StyleUtils.setLeft(vehicleWidget, 0);
      StyleUtils.setWidth(vehicleWidget, getVehicleWidth() - ChartHelper.DEFAULT_SEPARATOR_WIDTH);

      StyleUtils.setTop(vehicleWidget, top + ChartHelper.DEFAULT_SEPARATOR_HEIGHT);
      StyleUtils.setHeight(vehicleWidget, getRowHeight() - ChartHelper.DEFAULT_SEPARATOR_HEIGHT);

      panel.add(vehicleWidget);

      if (row < layoutRows.size() - 1) {
        ChartHelper.addRowSeparator(panel, top + getRowHeight(), 0,
            getVehicleWidth() + getChartWidth());
      }

      for (Freight item : rowItems) {
        JustDate start = TimeUtils.clamp(item.getRange().lowerEndpoint(), firstDate, lastDate);
        JustDate end = TimeUtils.clamp(item.getRange().upperEndpoint(), firstDate, lastDate);

        int left = getChartLeft() + TimeUtils.dayDiff(firstDate, start) * getDayColumnWidth();
        int width = (TimeUtils.dayDiff(start, end) + 1) * getDayColumnWidth();

        Rectangle rectangle = new Rectangle(left + ChartHelper.DEFAULT_SEPARATOR_WIDTH,
            top + ChartHelper.DEFAULT_SEPARATOR_HEIGHT,
            width - ChartHelper.DEFAULT_SEPARATOR_WIDTH,
            getRowHeight() - ChartHelper.DEFAULT_SEPARATOR_HEIGHT);
        addItemWidgets(item, panel, rectangle);
      }
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

  private void addItemWidgets(Freight item, HasWidgets panel, Rectangle rectangle) {
    CustomDiv itemWidget = new CustomDiv(STYLE_ITEM_PANEL);
    setItemWidgetColor(item, itemWidget);

    rectangle.applyTo(itemWidget);

    String loading = getPlaceLabel(item.loadingCountry, item.loadingPlace, item.loadingTerminal);
    String unloading = getPlaceLabel(item.unloadingCountry, item.unloadingPlace,
        item.unloadingTerminal);

    String loadTitle = BeeUtils.emptyToNull(BeeUtils.joinWords(item.loadingDate, loading));
    String unloadTitle = BeeUtils.emptyToNull(BeeUtils.joinWords(item.unloadingDate, unloading));
    
    String title = ChartHelper.buildTitle("Reiso Nr.", item.tripNo,
        "Vilkikas", item.vehicleNumber, "Puspriekabė", item.trailerNumber,
        "Užsakymo Nr.", item.orderNo, "Užsakovas", item.customerName,
        "Krovinys", item.cargoDescription,
        "Pakrovimas", loadTitle, "Iškrovimas", unloadTitle);

    final Long tripId = item.tripId;
    ClickHandler opener = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_TRIPS, tripId);
      }
    };

    panel.add(itemWidget);

    if (BeeUtils.allEmpty(loading, unloading)) {
      itemWidget.setTitle(title);
      itemWidget.addClickHandler(opener);
    }

    if (!BeeUtils.isEmpty(loading)) {
      BeeLabel loadingLabel = new BeeLabel(loading);
      loadingLabel.addStyleName(STYLE_ITEM_LOAD);

      if (BeeUtils.isEmpty(unloading)) {
        rectangle.applyTo(loadingLabel);

        loadingLabel.setTitle(title);
        loadingLabel.addClickHandler(opener);

      } else {
        rectangle.applyLeft(loadingLabel.getElement().getStyle());
        rectangle.applyTop(loadingLabel.getElement().getStyle());
      }

      panel.add(loadingLabel);
    }

    if (!BeeUtils.isEmpty(unloading)) {
      BeeLabel unloadingLabel = new BeeLabel(unloading);
      unloadingLabel.addStyleName(STYLE_ITEM_UNLOAD);

      rectangle.applyTo(unloadingLabel);

      unloadingLabel.setTitle(title);
      unloadingLabel.addClickHandler(opener);

      panel.add(unloadingLabel);
    }
  }

  private Widget createvehicleWidget(Freight item) {
    BeeLabel widget = new BeeLabel(item.vehicleNumber);
    widget.addStyleName(STYLE_VEHICLE_LABEL);

    final Long vehicleId = item.vehicleId;

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

  private List<List<Freight>> doLayout() {
    List<List<Freight>> rows = Lists.newArrayList();

    Long vehicleId = null;
    List<Freight> rowItems = Lists.newArrayList();

    for (Freight item : items) {
      if (BeeUtils.intersects(getVisibleRange(), item.getRange())) {

        if (!Objects.equal(item.vehicleId, vehicleId)) {
          if (!rowItems.isEmpty()) {
            rows.add(Lists.newArrayList(rowItems));
            rowItems.clear();
          }

          vehicleId = item.vehicleId;
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

  private void setVehicleWidth(int vehicleWidth) {
    this.vehicleWidth = vehicleWidth;
  }
}
