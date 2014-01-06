package com.butent.bee.shared.data.news;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum Feed implements HasCaption {
  COMPANIES(null, Localized.getConstants().companies(), TBL_COMPANIES, null,
      Sets.newHashSet(COL_COMPANY_NAME), "CompanyUsage",
      VIEW_COMPANIES, Lists.newArrayList(COL_COMPANY_NAME)),
  PERSONS(null, Localized.getConstants().persons(), TBL_PERSONS, null,
      Sets.newHashSet(COL_FIRST_NAME, COL_LAST_NAME), "PersonUsage",
      VIEW_PERSONS, Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME)),
  ITEMS(null, Localized.getConstants().goods(), TBL_ITEMS, null,
      Sets.newHashSet(COL_ITEM_NAME, COL_ITEM_ARTICLE, COL_ITEM_PRICE, COL_ITEM_CURRENCY),
      "ItemUsage", VIEW_ITEMS, Lists.newArrayList(COL_ITEM_NAME, COL_ITEM_ARTICLE)),

  DOCUMENTS(null, Localized.getConstants().documents(), CrmConstants.TBL_DOCUMENTS, null,
      Collections.<String> emptySet(), "DocumentUsage",
      CrmConstants.VIEW_DOCUMENTS, Lists.newArrayList(CrmConstants.COL_DOCUMENT_NAME)),

  APPOINTMENTS(Localized.getConstants().calendar(),
      Localized.getConstants().appointments(), CalendarConstants.TBL_APPOINTMENTS, null,
      Sets.newHashSet(CalendarConstants.COL_START_DATE_TIME, CalendarConstants.COL_END_DATE_TIME),
      "AppointmentUsage", CalendarConstants.VIEW_APPOINTMENTS,
      Lists.newArrayList(CalendarConstants.COL_SUMMARY, CalendarConstants.COL_START_DATE_TIME,
          CalendarConstants.COL_END_DATE_TIME)),

  EC_CLIENTS(Localized.getConstants().ecMenu(), Localized.getConstants().ecClients(),
      EcConstants.TBL_CLIENTS, null, Sets.newHashSet(EcConstants.COL_CLIENT_MANAGER),
      "EcClientUsage", EcConstants.VIEW_CLIENTS, Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME)),
  EC_ORDERS(Localized.getConstants().ecMenu(), Localized.getConstants().ecOrders(),
      EcConstants.TBL_ORDERS, null, Sets.newHashSet(EcConstants.COL_ORDER_STATUS), "EcOrderUsage",
      EcConstants.VIEW_ORDERS, Lists.newArrayList(EcConstants.COL_ORDER_NUMBER,
          EcConstants.ALS_ORDER_CLIENT_COMPANY_NAME)),
  EC_REGISTRATIONS(Localized.getConstants().ecMenu(), Localized.getConstants().ecRegistrations(),
      EcConstants.TBL_REGISTRATIONS, null, Collections.<String> emptySet(), "EcRegUsage",
      EcConstants.VIEW_REGISTRATIONS, Lists.newArrayList(EcConstants.COL_REGISTRATION_FIRST_NAME,
          EcConstants.COL_REGISTRATION_LAST_NAME)),

  // PURCHASES("PurchaseUsage"),
  // SALES("SaleUsage"),

  CARGO_REQUESTS(Localized.getConstants().transport(), Localized.getConstants().trRequests(),
      TBL_CARGO_REQUESTS, null, Sets.newHashSet(COL_CARGO_REQUEST_STATUS), "CargoRequestUsage",
      VIEW_CARGO_REQUESTS, Lists.newArrayList(ALS_REQUEST_CUSTOMER_FIRST_NAME,
          ALS_REQUEST_CUSTOMER_LAST_NAME)),
  DRIVERS(Localized.getConstants().transport(), Localized.getConstants().drivers(), TBL_DRIVERS,
      null, Sets.newHashSet(COL_DRIVER_START_DATE, COL_DRIVER_END_DATE), "DriverUsage",
      VIEW_DRIVERS, Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME)),
  ORDER_CARGO(Localized.getConstants().transport(), Localized.getConstants().cargos(),
      TBL_ORDER_CARGO, null, Sets.newHashSet(COL_ORDER, loadingColumnAlias(COL_PLACE_DATE)),
      "OrderCargoUsage", VIEW_ORDER_CARGO,
      Lists.newArrayList(loadingColumnAlias(COL_PLACE_DATE), COL_CARGO_DESCRIPTION)),
  SHIPMENT_REQUESTS(Localized.getConstants().transport(),
      Localized.getConstants().trRequestsUnregistered(), TBL_SHIPMENT_REQUESTS, null,
      Sets.newHashSet(COL_QUERY_STATUS), "ShipmentReqUsage", VIEW_SHIPMENT_REQUESTS,
      Lists.newArrayList(COL_QUERY_CUSTOMER_NAME)),
  TRANSPORTATION_ORDERS(Localized.getConstants().transport(),
      Localized.getConstants().trTransportationOrders(), TBL_ORDERS, null,
      Sets.newHashSet(COL_STATUS), "TranspOrderUsage", VIEW_ORDERS,
      Lists.newArrayList(COL_ORDER_DATE, COL_ORDER_NO)),
  TRANSPORT_REGISTRATIONS(Localized.getConstants().transport(),
      Localized.getConstants().trRegistrations(), TBL_REGISTRATIONS, null,
      Sets.newHashSet(COL_REGISTRATION_STATUS), "TranspRegUsage", VIEW_REGISTRATIONS,
      Lists.newArrayList(COL_REGISTRATION_COMPANY_NAME)),
  TRIPS(Localized.getConstants().transport(), Localized.getConstants().trips(), TBL_TRIPS, null,
      Sets.newHashSet(COL_TRIP_STATUS), "TripUsage", VIEW_TRIPS,
      Lists.newArrayList(COL_TRIP_DATE, COL_TRIP_NO)),
  VEHICLES(Localized.getConstants().transport(), Localized.getConstants().vehicles(),
      TBL_VEHICLES, null, Sets.newHashSet(COL_VEHICLE_NUMBER), "VehicleUsage", VIEW_VEHICLES,
      Lists.newArrayList(COL_VEHICLE_NUMBER));

  private static final String CAPTION_SEPARATOR = " - ";

  public static Feed findFeedWithUsageTable(String table) {
    if (BeeUtils.isEmpty(table)) {
      return null;
    }

    for (Feed feed : values()) {
      if (table.equals(feed.table) && feed.usageTable != null) {
        return feed;
      }
    }
    return null;
  }

  private static String joinCaption(String first, String second) {
    return BeeUtils.join(CAPTION_SEPARATOR, first, second);
  }

  private final String caption1;
  private final String caption2;
  private final String table;

  private final String userColumn;

  private final Set<String> observerColumns;

  private final String usageTable;

  private final String headlineView;
  private final List<String> headlineColumns;

  private Feed(String caption1, String caption2, String table, String userColumn,
      Set<String> observerColumns, String usageTable, String headlineView,
      List<String> headlineColumns) {
    this.caption1 = caption1;
    this.caption2 = caption2;
    this.table = table;
    this.userColumn = userColumn;
    this.observerColumns = observerColumns;
    this.usageTable = usageTable;
    this.headlineView = headlineView;
    this.headlineColumns = headlineColumns;
  }

  @Override
  public String getCaption() {
    if (caption1 == null) {
      return caption2;
    } else if (caption2 == null) {
      return caption1;
    } else {
      return joinCaption(caption1, caption2);
    }
  }

  public List<String> getHeadlineColumns() {
    return headlineColumns;
  }

  public String getHeadlineView() {
    return headlineView;
  }

  public Set<String> getObserverColumns() {
    return observerColumns;
  }

  public String getTable() {
    return table;
  }

  public String getUsageTable() {
    return usageTable;
  }

  public String getUserColumn() {
    return userColumn;
  }
}
