package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.modules.transport.charts.ChartData.Type;
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

class FreightExchange extends ChartBase {

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
    BeeKeeper.getRpc().makePostRequest(TransportHandler.createArgs(DATA_SERVICE),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            FreightExchange fx = new FreightExchange();
            fx.onCreate(response, callback);
          }
        });
  }

  private final List<OrderCargo> items = Lists.newArrayList();

  private int customerWidth = BeeConst.UNDEF;
  private int orderWidth = BeeConst.UNDEF;

  private final Set<String> customerPanels = Sets.newHashSet();
  private final Set<String> orderPanels = Sets.newHashSet();

  private final Map<Integer, Long> customersByRow = Maps.newHashMap();
  private final Map<Integer, Long> ordersByRow = Maps.newHashMap();
  
  private FreightExchange() {
    super();
    addStyleName(STYLE_PREFIX + "View");

    setRelevantDataViews(VIEW_ORDERS, VIEW_ORDER_CARGO, VIEW_CARGO_TRIPS, VIEW_TRIP_CARGO,
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
  protected boolean filter(DialogBox dialog) {
    Predicate<OrderCargo> predicate = getPredicate();
    
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
  protected Collection<? extends HasDateRange> getChartItems() {
    if (isFiltered()) {
      List<OrderCargo> result = Lists.newArrayList();
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
    return COL_FX_FOOTER_HEIGHT;
  }

  @Override
  protected String getHeaderHeightColumnName() {
    return COL_FX_HEADER_HEIGHT;
  }

  @Override
  protected String getRowHeightColumnName() {
    return COL_FX_PIXELS_PER_ROW;
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
  protected List<ChartData> initFilter() {
    if (items.isEmpty()) {
      return super.initFilter();
    }
    
    ChartData customerData = new ChartData(Type.CUSTOMER);
    ChartData loadData = new ChartData(Type.LOADING);
    ChartData unloadData = new ChartData(Type.UNLOADING);
    ChartData cargoData = new ChartData(Type.CARGO);
    
    for (OrderCargo item : items) {
      if (!BeeUtils.isEmpty(item.getCustomerName())) {
        customerData.add(item.getCustomerName(), item.getCustomerId());
      }

      String loading = getLoadingPlaceInfo(item);
      if (!BeeUtils.isEmpty(loading)) {
        loadData.add(loading);
      }

      String unloading = getUnloadingPlaceInfo(item);
      if (!BeeUtils.isEmpty(unloading)) {
        unloadData.add(unloading);
      }
      
      if (!BeeUtils.isEmpty(item.getCargoDescription())) {
        cargoData.add(item.getCargoDescription(), item.getCargoId());
      }
    }
    
    List<ChartData> result = Lists.newArrayList();

    if (!customerData.isEmpty()) {
      result.add(customerData);
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
      items.add(new OrderCargo(row));
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
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    customerPanels.clear();
    orderPanels.clear();
    
    customersByRow.clear();
    ordersByRow.clear();

    List<List<OrderCargo>> layoutRows = doLayout();
    
    initContent(panel, layoutRows.size());
    if (layoutRows.isEmpty()) {
      return;
    }

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

    for (int row = 0; row < layoutRows.size(); row++) {
      List<OrderCargo> rowItems = layoutRows.get(row);
      int top = row * getRowHeight();

      OrderCargo rowItem = rowItems.get(0);

      if (row == 0) {
        customerWidget = createCustomerWidget(rowItem);
        customerStartRow = row;

        orderWidget = createOrderWidget(rowItem);
        orderStartRow = row;

        lastCustomer = rowItem.getCustomerId();
        lastOrder = rowItem.getOrderId();

      } else {
        boolean customerChanged = !Objects.equal(lastCustomer, rowItem.getCustomerId());
        boolean orderChanged = customerChanged || !Objects.equal(lastOrder, rowItem.getOrderId());

        if (customerChanged) {
          addCustomerWidget(panel, customerWidget, lastCustomer, customerStartRow, row - 1);

          customerWidget = createCustomerWidget(rowItem);
          customerStartRow = row;

          lastCustomer = rowItem.getCustomerId();
        }

        if (orderChanged) {
          addOrderWidget(panel, orderWidget, lastOrder, orderStartRow, row - 1);

          orderWidget = createOrderWidget(rowItem);
          orderStartRow = row;

          lastOrder = rowItem.getOrderId();
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

      for (OrderCargo item : rowItems) {
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
  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
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

  private void addCustomerWidget(HasWidgets panel, IdentifiableWidget widget, Long customerId,
      int firstRow, int lastRow) {

    Rectangle rectangle = ChartHelper.getRectangle(0, getCustomerWidth(), firstRow, lastRow,
        getRowHeight());

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

    Rectangle rectangle = ChartHelper.getRectangle(getCustomerWidth(), getOrderWidth(),
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

  private IdentifiableWidget createCustomerWidget(OrderCargo item) {
    BeeLabel widget = new BeeLabel(item.getCustomerName());
    widget.addStyleName(STYLE_CUSTOMER_LABEL);

    final Long customerId = item.getCustomerId();

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

  private Widget createItemWidget(OrderCargo item) {
    final Flow panel = new Flow();
    panel.addStyleName(STYLE_ITEM_PANEL);
    setItemWidgetColor(item, panel);

    String loading = getLoadingPlaceInfo(item);
    String unloading = getUnloadingPlaceInfo(item);

    String title = item.getTitle(BeeUtils.joinWords(item.getLoadingDate(), loading),
        BeeUtils.joinWords(item.getUnloadingDate(), unloading));
    panel.setTitle(title);

    final Long cargoId = item.getCargoId();

    DndHelper.makeSource(panel, DATA_TYPE_ORDER_CARGO, cargoId, null, item, STYLE_ITEM_DRAG, true);

    ClickHandler opener = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openDataRow(event, VIEW_ORDER_CARGO, cargoId);
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

  private IdentifiableWidget createOrderWidget(OrderCargo item) {
    BeeLabel widget = new BeeLabel(item.getOrderNo());
    widget.addStyleName(STYLE_ORDER_LABEL);

    widget.setTitle(BeeUtils.buildLines(
        BeeUtils.joinWords("Data:", TimeUtils.renderCompact(item.getOrderDate())),
        BeeUtils.joinWords("Būsena:", item.getOrderStatus().getCaption()),
        BeeUtils.joinWords("ID:", item.getOrderId())));

    final Long orderId = item.getOrderId();

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

  private List<List<OrderCargo>> doLayout() {
    List<List<OrderCargo>> rows = Lists.newArrayList();

    Long orderId = null;
    List<OrderCargo> rowItems = Lists.newArrayList();
    
    for (int i = 0; i < items.size(); i++) {
      if (isFiltered() && !getFilteredIndexes().contains(i)) {
        continue;
      }
      OrderCargo item = items.get(i);

      if (BeeUtils.intersects(getVisibleRange(), item.getRange())) {

        if (!Objects.equal(item.getOrderId(), orderId) 
            || BeeUtils.intersects(rowItems, item.getRange())) {

          if (!rowItems.isEmpty()) {
            rows.add(Lists.newArrayList(rowItems));
            rowItems.clear();
          }

          orderId = item.getOrderId();
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
    for (OrderCargo item : items) {
      if (Objects.equal(item.getCustomerId(), customerId)) {
        return item.getCustomerName();
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

  private Predicate<OrderCargo> getPredicate() {
    List<Predicate<OrderCargo>> predicates = Lists.newArrayList();

    for (ChartData data : getFilterData()) {
      if (data.size() <= 1) {
        continue;
      }
      
      final Collection<String> selectedNames = data.getSelectedNames();
      if (selectedNames.isEmpty() || selectedNames.size() >= data.size()) {
        continue;
      }
      
      Predicate<OrderCargo> predicate;
      switch (data.getType()) {
        case CUSTOMER:
          predicate = new Predicate<OrderCargo>() {
            @Override
            public boolean apply(OrderCargo input) {
              return selectedNames.contains(input.getCustomerName());
            }
          };
          break;

        case LOADING:
          predicate = new Predicate<OrderCargo>() {
            @Override
            public boolean apply(OrderCargo input) {
              return selectedNames.contains(getLoadingPlaceInfo(input));
            }
          };
          break;

        case UNLOADING:
          predicate = new Predicate<OrderCargo>() {
            @Override
            public boolean apply(OrderCargo input) {
              return selectedNames.contains(getUnloadingPlaceInfo(input));
            }
          };
          break;
          
        case CARGO:
          predicate = new Predicate<OrderCargo>() {
            @Override
            public boolean apply(OrderCargo input) {
              return selectedNames.contains(input.getCargoDescription());
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

  private void onCustomerResize(MoveEvent event) {
    int delta = event.getDeltaX();

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
    int delta = event.getDeltaX();

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

  private void setCustomerWidth(int customerWidth) {
    this.customerWidth = customerWidth;
  }

  private void setOrderWidth(int orderWidth) {
    this.orderWidth = orderWidth;
  }
}
