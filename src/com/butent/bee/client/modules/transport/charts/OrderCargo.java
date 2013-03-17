package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

class OrderCargo implements HasDateRange, HasColorSource, HasShipmentInfo {
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

  OrderCargo(SimpleRow row) {
    this.orderId = row.getLong(COL_ORDER);

    this.orderStatus = NameUtils.getEnumByIndex(OrderStatus.class, row.getInt(COL_STATUS));
    this.orderDate = row.getDateTime(COL_ORDER_DATE);
    this.orderNo = row.getValue(COL_ORDER_NO);

    this.customerId = row.getLong(COL_CUSTOMER);
    this.customerName = row.getValue(COL_CUSTOMER_NAME);

    this.cargoId = row.getLong(COL_CARGO_ID);
    this.cargoDescription = row.getValue(COL_CARGO_DESCRIPTION);

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
  public Long getLoadingCountry() {
    return loadingCountry;
  }

  @Override
  public JustDate getLoadingDate() {
    return loadingDate;
  }

  @Override
  public String getLoadingPlace() {
    return loadingPlace;
  }

  @Override
  public String getLoadingTerminal() {
    return loadingTerminal;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  @Override
  public Long getUnloadingCountry() {
    return unloadingCountry;
  }

  @Override
  public JustDate getUnloadingDate() {
    return unloadingDate;
  }

  @Override
  public String getUnloadingPlace() {
    return unloadingPlace;
  }

  @Override
  public String getUnloadingTerminal() {
    return unloadingTerminal;
  }

  void assignToTrip(Long tripId, boolean fire) {
    if (!DataUtils.isId(tripId)) {
      return;
    }

    final String viewName = VIEW_CARGO_TRIPS;

    List<BeeColumn> columns = Data.getColumns(viewName, Lists.newArrayList(COL_CARGO, COL_TRIP));
    List<String> values = Queries.asList(getCargoId(), tripId);

    RowCallback callback;
    if (fire) {
      callback = new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          BeeKeeper.getBus().fireEvent(new RowInsertEvent(viewName, result));
        }
      };
    } else {
      callback = null;
    }

    Queries.insert(viewName, columns, values, callback);
  }

  String getCargoDescription() {
    return cargoDescription;
  }

  Long getCargoId() {
    return cargoId;
  }

  Long getCustomerId() {
    return customerId;
  }

  String getCustomerName() {
    return customerName;
  }

  DateTime getOrderDate() {
    return orderDate;
  }

  Long getOrderId() {
    return orderId;
  }

  String getOrderNo() {
    return orderNo;
  }
  
  OrderStatus getOrderStatus() {
    return orderStatus;
  }
}
