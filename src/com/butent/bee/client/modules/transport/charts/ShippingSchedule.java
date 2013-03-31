package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.logical.MotionEvent;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.modules.transport.charts.ChartRowLayout.GroupLayout;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

class ShippingSchedule extends VehicleTimeBoard implements MotionEvent.Handler {

  static final String SUPPLIER_KEY = "shipping_schedule";
  private static final String DATA_SERVICE = SVC_GET_SS_DATA;

  private static final String STYLE_PREFIX = "bee-tr-ss-";

  private static final String STYLE_TRIP_GROUP_PREFIX = STYLE_PREFIX + "trip-Group-";
  private static final String STYLE_TRIP_GROUP_ROW_SEPARATOR = STYLE_TRIP_GROUP_PREFIX + "row-sep";
  private static final String STYLE_TRIP_GROUP_PANEL = STYLE_TRIP_GROUP_PREFIX + "panel";
  private static final String STYLE_TRIP_GROUP_LABEL = STYLE_TRIP_GROUP_PREFIX + "label";
  private static final String STYLE_TRIP_GROUP_TRAILER = STYLE_TRIP_GROUP_PREFIX + "trailer";
  private static final String STYLE_TRIP_GROUP_OVERLAP = STYLE_TRIP_GROUP_PREFIX + "overlap";
  private static final String STYLE_TRIP_GROUP_DRAG_OVER = STYLE_TRIP_GROUP_PREFIX + "dragOver";

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

  private boolean separateTrips = false;

  private final Map<Integer, Long> tripsByRow = Maps.newHashMap();

  private ShippingSchedule() {
    super();
    addStyleName(STYLE_PREFIX + "View");
  }

  @Override
  public String getCaption() {
    return Global.CONSTANTS.shippingSchedule();
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
    if (!DATA_TYPE_ORDER_CARGO.equals(event.getDataType())) {
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
  protected BeeRow createNewTripRow(DataInfo dataInfo, int rowIndex, JustDate date) {
    BeeRow newRow = super.createNewTripRow(dataInfo, rowIndex, date);

    if (tripsByRow.containsKey(rowIndex)) {
      Trip trip = findTripById(tripsByRow.get(rowIndex));

      if (trip != null && trip.getTrailerId() != null) {
        newRow.setValue(dataInfo.getColumnIndex(COL_TRAILER), trip.getTrailerId());
        newRow.setValue(dataInfo.getColumnIndex(COL_TRAILER_NUMBER), trip.getTrailerNumber());
      }
    }
    return newRow;
  }

  @Override
  protected String getDataService() {
    return DATA_SERVICE;
  }

  @Override
  protected String getDataType() {
    return DATA_TYPE_TRUCK;
  }

  @Override
  protected String getDayWidthColumnName() {
    return COL_SS_PIXELS_PER_DAY;
  }

  @Override
  protected String getFooterHeightColumnName() {
    return COL_SS_FOOTER_HEIGHT;
  }

  @Override
  protected Long getGroupIdForFreightLayout(Trip trip) {
    return separateTrips() ? trip.getTripId() : trip.getTruckId();
  }

  @Override
  protected Long getGroupIdForTripLayout(Trip trip) {
    return separateTrips() ? trip.getTripId() : trip.getTruckId();
  }

  @Override
  protected String getHeaderHeightColumnName() {
    return COL_SS_HEADER_HEIGHT;
  }

  @Override
  protected String getInfoWidthColumnName() {
    return COL_SS_PIXELS_PER_TRIP;
  }

  @Override
  protected String getItemOpacityColumnName() {
    return COL_SS_ITEM_OPACITY;
  }

  @Override
  protected String getNumberWidthColumnName() {
    return COL_SS_PIXELS_PER_TRUCK;
  }

  @Override
  protected String getRowHeightColumnName() {
    return COL_SS_PIXELS_PER_ROW;
  }

  @Override
  protected String getSeparateCargoColumnName() {
    return COL_SS_SEPARATE_CARGO;
  }

  @Override
  protected String getSettingsFormName() {
    return FORM_SS_SETTINGS;
  }

  @Override
  protected String getShowCountryFlagsColumnName() {
    return COL_SS_COUNTRY_FLAGS;
  }

  @Override
  protected String getShowPlaceInfoColumnName() {
    return COL_SS_PLACE_INFO;
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
  protected boolean isInfoColumnVisible() {
    return separateTrips();
  }

  @Override
  protected boolean layoutIdleVehicles() {
    return false;
  }
  
  @Override
  protected void prepareChart(Size canvasSize) {
    setSeparateTrips(ChartHelper.getBoolean(getSettings(), COL_SS_SEPARATE_TRIPS));
    super.prepareChart(canvasSize);
  }
  
  @Override
  protected List<HandlerRegistration> register() {
    List<HandlerRegistration> list = super.register();
    list.add(MotionEvent.register(this));

    return list;
  }

  @Override
  protected void renderContentInit() {
    super.renderContentInit();
    tripsByRow.clear();
  }

  @Override
  protected void renderInfoCell(ChartRowLayout layout, Vehicle vehicle, ComplexPanel panel,
      int firstRow, int lastRow) {
    
    for (GroupLayout group : layout.getGroups()) {
      Trip trip = findTripById(group.getGroupId());
      
      if (trip != null) {
        IdentifiableWidget tripGroupWidget = createTripGroupWidget(trip, group.hasOverlap());
        addTripGroupWidget(panel, tripGroupWidget, trip.getTripId(), 
            firstRow + group.getFirstRow(), firstRow + group.getLastRow());
      }
    }
  }

  @Override
  protected void renderRowSeparators(ComplexPanel panel, int firstRow, int lastRow) {
    Long lastTrip = tripsByRow.get(firstRow);

    for (int rowIndex = firstRow + 1; rowIndex <= lastRow; rowIndex++) {
      int top = rowIndex * getRowHeight(); 
      Long currentTrip = tripsByRow.get(rowIndex);

      if (Objects.equal(lastTrip, currentTrip)) {
        ChartHelper.addRowSeparator(panel, top, getChartLeft(), getCalendarWidth());
      } else {
        ChartHelper.addRowSeparator(panel, STYLE_TRIP_GROUP_ROW_SEPARATOR, top,
            getNumberWidth(), getInfoWidth() + getCalendarWidth());
      }
    }
  }

  private void addTripGroupWidget(HasWidgets panel, IdentifiableWidget widget, Long tripId,
      int firstRow, int lastRow) {
    addInfoWidget(panel, widget, firstRow, lastRow);

    for (int row = firstRow; row <= lastRow; row++) {
      tripsByRow.put(row, tripId);
    }
  }
  
  private IdentifiableWidget createTripGroupWidget(Trip trip, boolean hasOverlap) {
    final Flow panel = new Flow();
    panel.addStyleName(STYLE_TRIP_GROUP_PANEL);
    if (hasOverlap) {
      panel.addStyleName(STYLE_TRIP_GROUP_OVERLAP);
    }

    final Long tripId = trip.getTripId();
    final String tripTitle = trip.getTitle();
    
    trip.makeTarget(panel, STYLE_TRIP_GROUP_DRAG_OVER);

    BeeLabel label = new BeeLabel(trip.getTripNo());
    label.addStyleName(STYLE_TRIP_GROUP_LABEL);

    label.setTitle(tripTitle);

    label.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_TRIPS, tripId);
      }
    });

    panel.add(label);

    if (trip.getTrailerId() != null) {
      BeeLabel trailer = new BeeLabel(trip.getTrailerNumber());
      trailer.addStyleName(STYLE_TRIP_GROUP_TRAILER);

      final Long trailerId = trip.getTrailerId();

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

  private boolean separateTrips() {
    return separateTrips;
  }

  private void setSeparateTrips(boolean separateTrips) {
    this.separateTrips = separateTrips;
  }
}
