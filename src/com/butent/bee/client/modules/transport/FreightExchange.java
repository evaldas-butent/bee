package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

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
  private static final String STYLE_CUSTOMER_COLUMN_SEPARATOR = STYLE_CUSTOMER_PREFIX + "col-sep";
  private static final String STYLE_CUSTOMER_ROW_SEPARATOR = STYLE_CUSTOMER_PREFIX + "row-sep";
  private static final String STYLE_CUSTOMER_PANEL = STYLE_CUSTOMER_PREFIX + "panel";
  private static final String STYLE_CUSTOMER_LABEL = STYLE_CUSTOMER_PREFIX + "label";

  private static final String STYLE_ORDER_PREFIX = STYLE_PREFIX + "Order-";
  private static final String STYLE_ORDER_COLUMN_SEPARATOR = STYLE_ORDER_PREFIX + "col-sep";
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

  private FreightExchange() {
    super();
    addStyleName(STYLE_PREFIX + "View");
    
    setRelevantDataViews(VIEW_ORDERS, VIEW_CARGO, VIEW_CARGO_TRIPS, CommonsConstants.VIEW_COLORS,
        CommonsConstants.VIEW_THEME_COLORS);
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
  protected Collection<? extends ChartItem> getChartItems() {
    return items;
  }

  @Override
  protected String getDataService() {
    return DATA_SERVICE;
  }

  @Override
  protected String getSettingsFormName() {
    return FORM_FX_SETTINGS;
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
  protected void prepareChart(int canvasWidth, int canvasHeight) {
    setCustomerWidth(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_CUSTOMER, 100,
        canvasWidth / 5));
    setOrderWidth(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_ORDER, 60,
        canvasWidth / 5));

    setHeaderHeight(ChartHelper.getPixels(getSettings(), COL_FX_HEADER_HEIGHT, 20,
        canvasHeight / 5));
    setFooterHeight(ChartHelper.getPixels(getSettings(), COL_FX_FOOTER_HEIGHT, 30,
        canvasHeight / 3));

    setChartLeft(getCustomerWidth() + getOrderWidth());
    setChartWidth(canvasWidth - getChartLeft() - getChartRight());

    setDayColumnWidth(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_DAY, 20));

    int scrollAreaHeight = canvasHeight - getHeaderHeight() - getFooterHeight();
    setRowHeight(ChartHelper.getPixels(getSettings(), COL_FX_PIXELS_PER_ROW, 20,
        scrollAreaHeight / 2));

    int slider = ChartHelper.getPixels(getSettings(), COL_FX_SLIDER_WIDTH, 5);
    setSliderWidth(BeeUtils.clamp(slider, 1, getChartRight()));

    setBarHeight(ChartHelper.getPixels(getSettings(), COL_FX_BAR_HEIGHT, BeeConst.UNDEF,
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

    ChartHelper.addColumnSeparator(panel, STYLE_CUSTOMER_COLUMN_SEPARATOR, getCustomerWidth(),
        height);
    ChartHelper.addColumnSeparator(panel, STYLE_ORDER_COLUMN_SEPARATOR, getChartLeft(), height);

    ChartHelper.renderDayColumns(panel, getVisibleRange(), getChartLeft(), getDayColumnWidth(),
        height, false, true);

    JustDate firstDate = getVisibleRange().lowerEndpoint();
    JustDate lastDate = getVisibleRange().upperEndpoint();

    Long lastCustomer = null;
    Long lastOrder = null;

    Widget customerWidget = null;
    Widget orderWidget = null;

    int customerStartRow = 0;
    int orderStartRow = 0;
    
    Double itemOpacity = ChartHelper.getOpacity(getSettings(), COL_FX_ITEM_OPACITY);

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
          ChartHelper.addLegendWidget(panel, customerWidget, 0, getCustomerWidth(),
              customerStartRow, row - 1, getRowHeight(),
              ChartHelper.DEFAULT_SEPARATOR_WIDTH, ChartHelper.DEFAULT_SEPARATOR_HEIGHT);

          customerWidget = createCustomerWidget(rowItem);
          customerStartRow = row;

          lastCustomer = rowItem.customerId;
        }

        if (orderChanged) {
          ChartHelper.addLegendWidget(panel, orderWidget, getCustomerWidth(), getOrderWidth(),
              orderStartRow, row - 1, getRowHeight(),
              ChartHelper.DEFAULT_SEPARATOR_WIDTH, ChartHelper.DEFAULT_SEPARATOR_HEIGHT);

          orderWidget = createOrderWidget(rowItem);
          orderStartRow = row;

          lastOrder = rowItem.orderId;
        }

        if (customerChanged) {
          ChartHelper.addRowSeparator(panel, STYLE_CUSTOMER_ROW_SEPARATOR, top, 0,
              getCustomerWidth() + getOrderWidth() + getChartWidth());
        } else if (orderChanged) {
          ChartHelper.addRowSeparator(panel, STYLE_ORDER_ROW_SEPARATOR, top, getCustomerWidth(),
              getOrderWidth() + getChartWidth());
        } else {
          ChartHelper.addRowSeparator(panel, top, getChartLeft(), getChartWidth());
        }
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
      ChartHelper.addLegendWidget(panel, customerWidget, 0, getCustomerWidth(),
          customerStartRow, lastRow, getRowHeight(),
          ChartHelper.DEFAULT_SEPARATOR_WIDTH, ChartHelper.DEFAULT_SEPARATOR_HEIGHT);
    }
    if (orderWidget != null) {
      ChartHelper.addLegendWidget(panel, orderWidget, getCustomerWidth(), getOrderWidth(),
          orderStartRow, lastRow, getRowHeight(),
          ChartHelper.DEFAULT_SEPARATOR_WIDTH, ChartHelper.DEFAULT_SEPARATOR_HEIGHT);
    }

    ChartHelper.addBottomSeparator(panel, height, 0, getCustomerWidth() + getOrderWidth()
        + getChartWidth() + ChartHelper.DEFAULT_SEPARATOR_WIDTH);
  }

  private Widget createCustomerWidget(Freight item) {
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

    panel.setTitle(BeeUtils.buildLines(item.cargoDescription,
        BeeUtils.joinWords("Pakrovimas:", item.loadingDate, loading),
        BeeUtils.joinWords("Iškrovimas:", item.unloadingDate, unloading)));

    final Long cargoId = item.cargoId;
    ClickHandler opener = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_CARGO, cargoId);
      }
    };
    
    DomUtils.setDraggable(panel);
    
    panel.addDragStartHandler(new DragStartHandler() {
      @Override
      public void onDragStart(DragStartEvent event) {
        panel.addStyleName(STYLE_ITEM_DRAG);
        
        EventUtils.allowMove(event);
        EventUtils.setDndData(event, cargoId);
      }
    });
    
    panel.addDragEndHandler(new DragEndHandler() {
      @Override
      public void onDragEnd(DragEndEvent event) {
        panel.removeStyleName(STYLE_ITEM_DRAG);
      }
    });

//    panel.addClickHandler(opener);

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

  private Widget createOrderWidget(Freight item) {
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

  private int getCustomerWidth() {
    return customerWidth;
  }

  private int getOrderWidth() {
    return orderWidth;
  }

  private void setCustomerWidth(int customerWidth) {
    this.customerWidth = customerWidth;
  }

  private void setOrderWidth(int orderWidth) {
    this.orderWidth = orderWidth;
  }
}
