package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
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

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
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
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FreightExchange extends ChartBase {

  private static class Freight implements ChartItem {
    private final Long orderId;

    private final OrderStatus orderStatus;
    private final DateTime orderDate;
    private final String orderNo;

    private final Long customerId;
    private final String customerName;

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

    private final Range<JustDate> range;

    private Freight(SimpleRow row) {
      super();

      this.orderId = row.getLong(COL_ORDER);

      this.orderStatus = NameUtils.getEnumByIndex(OrderStatus.class, row.getInt(COL_STATUS));
      this.orderDate = row.getDateTime(COL_ORDER_DATE);
      this.orderNo = row.getValue(COL_ORDER_NO);

      this.customerId = row.getLong(COL_CUSTOMER);
      this.customerName = row.getValue(COL_CUSTOMER_NAME);

      this.cargoId = row.getLong(COL_CARGO_ID);
      this.cargoDescription = row.getValue(COL_DESCRIPTION);

      this.loadingDate = row.getDate(loadingColumnAlias(COL_PLACE_DATE));
      this.loadingCountry = row.getLong(loadingColumnAlias(COL_COUNTRY));
      this.loadingPlace = row.getValue(loadingColumnAlias(COL_PLACE_NAME));
      this.loadingTerminal = row.getValue(loadingColumnAlias(COL_TERMINAL));

      this.unloadingDate = row.getDate(unloadingColumnAlias(COL_PLACE_DATE));
      this.unloadingCountry = row.getLong(unloadingColumnAlias(COL_COUNTRY));
      this.unloadingPlace = row.getValue(unloadingColumnAlias(COL_PLACE_NAME));
      this.unloadingTerminal = row.getValue(unloadingColumnAlias(COL_TERMINAL));

      JustDate start = BeeUtils.nvl(loadingDate, unloadingDate, orderDate.getDate());
      JustDate end = BeeUtils.nvl(unloadingDate, start);

      this.range = Range.closed(start, TimeUtils.max(start, end));
    }

    @Override
    public Long getColorSource() {
      return cargoId;
    }

    @Override
    public Range<JustDate> getRange() {
      return range;
    }
  }

  static final String SUPPLIER_KEY = "freight_exchange";
  private static final String DATA_SERVICE = SVC_GET_FX_DATA;

  private static final String STYLE_PREFIX = "bee-tr-fx-";

  private static final String STYLE_CUSTOMER_PREFIX = STYLE_PREFIX + "Customer-";
  private static final String STYLE_CUSTOMER_ROW_SEPARATOR = STYLE_CUSTOMER_PREFIX + "row-sep";
  private static final String STYLE_CUSTOMER_PANEL = STYLE_CUSTOMER_PREFIX + "panel";
  private static final String STYLE_CUSTOMER_LABEL = STYLE_CUSTOMER_PREFIX + "label";

  private static final String STYLE_ORDER_PREFIX = STYLE_PREFIX + "Order-";
  private static final String STYLE_ORDER_ROW_SEPARATOR = STYLE_ORDER_PREFIX + "row-sep";
  private static final String STYLE_ORDER_PANEL = STYLE_ORDER_PREFIX + "panel";
  private static final String STYLE_ORDER_LABEL = STYLE_ORDER_PREFIX + "label";

  private static final String STYLE_ITEM_PREFIX = STYLE_PREFIX + "Item-";
  private static final String STYLE_ITEM_PANEL = STYLE_ITEM_PREFIX + "panel";
  private static final String STYLE_ITEM_LOAD = STYLE_ITEM_PREFIX + "load";
  private static final String STYLE_ITEM_UNLOAD = STYLE_ITEM_PREFIX + "unload";

  private static final String STYLE_ITEM_DRAG = STYLE_ITEM_PREFIX + "drag";

  static void open(final Callback<IdentifiableWidget> callback) {
    Assert.notNull(callback);

    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(DATA_SERVICE),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            FreightExchange fx = new FreightExchange();
            if (fx.setData(response)) {
              callback.onSuccess(fx);
            } else {
              callback.onFailure(fx.getCaption(), "negavo duomenų iš serverio",
                  Global.CONSTANTS.sorry());
            }
          }
        });
  }

  private final List<Freight> items = Lists.newArrayList();

  private int customerWidth = BeeConst.UNDEF;
  private int orderWidth = BeeConst.UNDEF;

  private final Set<String> customerPanels = Sets.newHashSet();
  private final Set<String> orderPanels = Sets.newHashSet();

  private final Map<Integer, Long> customersByRow = Maps.newHashMap();
  private final Map<Integer, Long> ordersByRow = Maps.newHashMap();
  
  private FreightExchange() {
    super();
    addStyleName(STYLE_PREFIX + "View");

    setRelevantDataViews(VIEW_ORDERS, VIEW_CARGO, VIEW_CARGO_TRIPS, VIEW_TRIP_CARGO,
        CommonsConstants.VIEW_COLORS, CommonsConstants.VIEW_THEME_COLORS);
  }

  @Override
  public String getCaption() {
    return "Užsakymų birža";
  }

  @Override
  public String getIdPrefix() {
    return "tr-fx";
  }

  @Override
  public String getSupplierKey() {
    return SUPPLIER_KEY;
  }

  @Override
  public void handleAction(Action action) {
    if (Action.ADD.equals(action)) {
      RowFactory.createRow(VIEW_ORDERS);
    } else {
      super.handleAction(action);
    }
  }

  @Override
  protected String getBarHeightColumnName() {
    return COL_FX_BAR_HEIGHT;
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
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.ADD, Action.CONFIGURE);
  }

  @Override
  protected String getFooterHeightColumnName() {
    return COL_FX_FOOTER_HEIGHT;
  }

  @Override
  protected String getHeaderHeightColumnName() {
    return COL_FX_HEADER_HEIGHT;
  }

  @Override
  protected String getSettingsFormName() {
    return FORM_FX_SETTINGS;
  }

  @Override
  protected String getSliderWidthColumnName() {
    return COL_FX_SLIDER_WIDTH;
  }

  @Override
  protected String getStripOpacityColumnName() {
    return COL_FX_STRIP_OPACITY;
  }
  
  @Override
  protected String getThemeColumnName() {
    return COL_FX_THEME;
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
  protected void onDoubleClickChart(int row, JustDate date) {
    Long customerId = customersByRow.get(row);

    if (customerId != null && TimeUtils.isMeq(date, TimeUtils.today())) {
      DataInfo dataInfo = Data.getDataInfo(VIEW_ORDERS);
      BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);
      
      if (TimeUtils.isMore(date, TimeUtils.today())) {
        newRow.setValue(dataInfo.getColumnIndex(COL_ORDER_DATE), date.getDateTime());
      }
      
      newRow.setValue(dataInfo.getColumnIndex(COL_CUSTOMER), customerId);
      newRow.setValue(dataInfo.getColumnIndex(COL_CUSTOMER_NAME), findCustomerName(customerId));
      
      RowFactory.createRow(dataInfo, newRow);
    }
  }

  @Override
  protected void prepareChart(Size canvasSize) {
    setCustomerWidth(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_CUSTOMER, 100,
        ChartHelper.DEFAULT_MOVER_WIDTH + 1, canvasSize.getWidth() / 3));
    setOrderWidth(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_ORDER, 60,
        ChartHelper.DEFAULT_MOVER_WIDTH + 1, canvasSize.getWidth() / 3));

    setChartLeft(getCustomerWidth() + getOrderWidth());
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());

    setDayColumnWidth(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_DAY, 20,
        1, getChartWidth()));

    setRowHeight(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_ROW, 20,
        1, getScrollAreaHeight(canvasSize.getHeight()) / 2));
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

    Long lastCustomer = null;
    Long lastOrder = null;

    IdentifiableWidget customerWidget = null;
    IdentifiableWidget orderWidget = null;

    int customerStartRow = 0;
    int orderStartRow = 0;

    Double itemOpacity = ChartHelper.getOpacity(getSettings(), COL_FX_ITEM_OPACITY);

    customerPanels.clear();
    orderPanels.clear();
    
    customersByRow.clear();
    ordersByRow.clear();

    for (int row = 0; row < layoutRows.size(); row++) {
      List<Freight> rowItems = layoutRows.get(row);
      int top = row * getRowHeight();

      Freight rowItem = rowItems.get(0);

      if (row == 0) {
        customerWidget = createCustomerWidget(rowItem);
        customerStartRow = row;

        orderWidget = createOrderWidget(rowItem);
        orderStartRow = row;

        lastCustomer = rowItem.customerId;
        lastOrder = rowItem.orderId;

      } else {
        boolean customerChanged = !Objects.equal(lastCustomer, rowItem.customerId);
        boolean orderChanged = customerChanged || !Objects.equal(lastOrder, rowItem.orderId);

        if (customerChanged) {
          addCustomerWidget(panel, customerWidget, lastCustomer, customerStartRow, row - 1);

          customerWidget = createCustomerWidget(rowItem);
          customerStartRow = row;

          lastCustomer = rowItem.customerId;
        }

        if (orderChanged) {
          addOrderWidget(panel, orderWidget, lastOrder, orderStartRow, row - 1);

          orderWidget = createOrderWidget(rowItem);
          orderStartRow = row;

          lastOrder = rowItem.orderId;
        }

        if (customerChanged) {
          ChartHelper.addRowSeparator(panel, STYLE_CUSTOMER_ROW_SEPARATOR, top, 0,
              getCustomerWidth() + getOrderWidth() + calendarWidth);
        } else if (orderChanged) {
          ChartHelper.addRowSeparator(panel, STYLE_ORDER_ROW_SEPARATOR, top, getCustomerWidth(),
              getOrderWidth() + calendarWidth);
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

    if (customerWidget != null) {
      addCustomerWidget(panel, customerWidget, lastCustomer, customerStartRow, lastRow);
    }
    if (orderWidget != null) {
      addOrderWidget(panel, orderWidget, lastOrder, orderStartRow, lastRow);
    }

    ChartHelper.addBottomSeparator(panel, height, 0, getChartLeft() + calendarWidth);
    
    renderMovers(panel, height);
  }

  private void addCustomerWidget(HasWidgets panel, IdentifiableWidget widget, Long customerId,
      int firstRow, int lastRow) {

    Rectangle rectangle = ChartHelper.getLegendRectangle(0, getCustomerWidth(),
        firstRow, lastRow, getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget.asWidget(), rectangle, margins);
    panel.add(widget.asWidget());

    customerPanels.add(widget.getId());
    for (int row = firstRow; row <= lastRow; row++) {
      customersByRow.put(row, customerId);
    }
  }

  private void addOrderWidget(HasWidgets panel, IdentifiableWidget widget, Long orderId,
      int firstRow, int lastRow) {

    Rectangle rectangle = ChartHelper.getLegendRectangle(getCustomerWidth(), getOrderWidth(),
        firstRow, lastRow, getRowHeight());

    Edges margins = new Edges();
    margins.setRight(ChartHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(ChartHelper.ROW_SEPARATOR_HEIGHT);

    ChartHelper.apply(widget.asWidget(), rectangle, margins);
    panel.add(widget.asWidget());

    orderPanels.add(widget.getId());
    for (int row = firstRow; row <= lastRow; row++) {
      ordersByRow.put(row, orderId);
    }
  }

  private IdentifiableWidget createCustomerWidget(Freight item) {
    BeeLabel widget = new BeeLabel(item.customerName);
    widget.addStyleName(STYLE_CUSTOMER_LABEL);

    final Long customerId = item.customerId;

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, CommonsConstants.VIEW_COMPANIES, customerId);
      }
    });

    Simple panel = new Simple(widget);
    panel.addStyleName(STYLE_CUSTOMER_PANEL);

    return panel;
  }

  private Widget createItemWidget(Freight item) {
    final Flow panel = new Flow();
    panel.addStyleName(STYLE_ITEM_PANEL);
    setItemWidgetColor(item, panel);

    String loading = getPlaceLabel(item.loadingCountry, item.loadingPlace, item.loadingTerminal);
    String unloading = getPlaceLabel(item.unloadingCountry, item.unloadingPlace,
        item.unloadingTerminal);

    String title = BeeUtils.buildLines(item.cargoDescription,
        BeeUtils.joinWords("Pakrovimas:", item.loadingDate, loading),
        BeeUtils.joinWords("Iškrovimas:", item.unloadingDate, unloading));

    panel.setTitle(title);

    final Long cargoId = item.cargoId;

    DndHelper.makeSource(panel, DndHelper.ContentType.CARGO, cargoId, null, title, STYLE_ITEM_DRAG);

    ClickHandler opener = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_CARGO, cargoId);
      }
    };

    panel.addClickHandler(opener);

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

  private IdentifiableWidget createOrderWidget(Freight item) {
    BeeLabel widget = new BeeLabel(item.orderNo);
    widget.addStyleName(STYLE_ORDER_LABEL);

    widget.setTitle(BeeUtils.buildLines(
        BeeUtils.joinWords("Data:", TimeUtils.renderCompact(item.orderDate)),
        BeeUtils.joinWords("Būsena:", item.orderStatus.getCaption()),
        BeeUtils.joinWords("ID:", item.orderId)));

    final Long orderId = item.orderId;

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_ORDERS, orderId);
      }
    });

    Simple panel = new Simple(widget);
    panel.addStyleName(STYLE_ORDER_PANEL);

    return panel;
  }

  private List<List<Freight>> doLayout() {
    List<List<Freight>> rows = Lists.newArrayList();

    Long orderId = null;
    List<Freight> rowItems = Lists.newArrayList();

    for (Freight item : items) {
      if (BeeUtils.intersects(getVisibleRange(), item.getRange())) {

        if (!Objects.equal(item.orderId, orderId) ||
            ChartHelper.intersects(rowItems, item.getRange())) {

          if (!rowItems.isEmpty()) {
            rows.add(Lists.newArrayList(rowItems));
            rowItems.clear();
          }

          orderId = item.orderId;
        }

        rowItems.add(item);
      }
    }

    if (!rowItems.isEmpty()) {
      rows.add(Lists.newArrayList(rowItems));
    }
    return rows;
  }

  private String findCustomerName(Long customerId) {
    for (Freight item : items) {
      if (Objects.equal(item.customerId, customerId)) {
        return item.customerName;
      }
    }
    
    return null;
  }

  private int getCustomerWidth() {
    return customerWidth;
  }

  private int getOrderWidth() {
    return orderWidth;
  }

  private void onCustomerResize(MoveEvent event) {
    int delta = event.getDelta();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int newLeft = BeeUtils.clamp(oldLeft + delta, 1,
        getChartLeft() - ChartHelper.DEFAULT_MOVER_WIDTH * 2 - 1);

    if (newLeft != oldLeft || event.isFinished()) {
      int customerPx = newLeft + ChartHelper.DEFAULT_MOVER_WIDTH;
      int orderPx = getChartLeft() - customerPx;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : customerPanels) {
          StyleUtils.setWidth(id, customerPx - ChartHelper.DEFAULT_MOVER_WIDTH);
        }

        for (String id : orderPanels) {
          Element element = Document.get().getElementById(id);
          if (element != null) {
            StyleUtils.setLeft(element, customerPx);
            StyleUtils.setWidth(element, orderPx - ChartHelper.DEFAULT_MOVER_WIDTH);
          }
        }
      }

      if (event.isFinished()
          && updateSettings(COL_FX_PIXELS_PER_CUSTOMER, customerPx, COL_FX_PIXELS_PER_ORDER,
              orderPx)) {
        setCustomerWidth(customerPx);
        setOrderWidth(orderPx);
      }
    }
  }

  private void onOrderResize(MoveEvent event) {
    int delta = event.getDelta();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft = getCustomerWidth() + 300;
    if (getChartWidth() > 0) {
      maxLeft = Math.min(maxLeft, getChartLeft() + getChartWidth() / 2);
    }

    int newLeft = BeeUtils.clamp(oldLeft + delta, getCustomerWidth() + 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      int orderPx = newLeft - getCustomerWidth() + ChartHelper.DEFAULT_MOVER_WIDTH;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : orderPanels) {
          StyleUtils.setWidth(id, orderPx - ChartHelper.DEFAULT_MOVER_WIDTH);
        }
      }

      if (event.isFinished() && updateSetting(COL_FX_PIXELS_PER_ORDER, orderPx)) {
        setOrderWidth(orderPx);
        render(false);
      }
    }
  }

  private void renderMovers(HasWidgets panel, int height) {
    Mover customerMover = ChartHelper.createHorizontalMover();
    StyleUtils.setLeft(customerMover, getCustomerWidth() - ChartHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(customerMover, height);

    customerMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onCustomerResize(event);
      }
    });

    panel.add(customerMover);

    Mover orderMover = ChartHelper.createHorizontalMover();
    StyleUtils.setLeft(orderMover, getChartLeft() - ChartHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(orderMover, height);

    orderMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onOrderResize(event);
      }
    });

    panel.add(orderMover);
  }

  private void setCustomerWidth(int customerWidth) {
    this.customerWidth = customerWidth;
  }

  private void setOrderWidth(int orderWidth) {
    this.orderWidth = orderWidth;
  }
}
