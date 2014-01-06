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
  COMPANIES(Localized.getConstants().companies(), TBL_COMPANIES, null,
      Sets.newHashSet(COL_COMPANY_NAME), "CompanyUsage",
      Lists.newArrayList(COL_COMPANY_NAME)),
  PERSONS(Localized.getConstants().persons(), TBL_PERSONS, null,
      Sets.newHashSet(COL_FIRST_NAME, COL_LAST_NAME), "PersonUsage",
      Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME)),
  ITEMS(Localized.getConstants().goods(), TBL_ITEMS, null,
      Sets.newHashSet(COL_ITEM_NAME, COL_ITEM_ARTICLE, COL_ITEM_PRICE, COL_ITEM_CURRENCY),
      "ItemUsage", Lists.newArrayList(COL_ITEM_NAME, COL_ITEM_ARTICLE)),

  APPOINTMENTS(Localized.getConstants().appointments(), CalendarConstants.TBL_APPOINTMENTS, null,
      Sets.newHashSet(CalendarConstants.COL_START_DATE_TIME, CalendarConstants.COL_END_DATE_TIME),
      "AppointmentUsage", Lists.newArrayList(CalendarConstants.COL_SUMMARY,
          CalendarConstants.COL_START_DATE_TIME, CalendarConstants.COL_END_DATE_TIME)),

  DOCUMENTS(Localized.getConstants().documents(), CrmConstants.TBL_DOCUMENTS, null,
      Collections.<String> emptySet(), "DocumentUsage",
      Lists.newArrayList(CrmConstants.COL_DOCUMENT_NAME)),

  EC_CLIENTS(Localized.getConstants().ecClients(), EcConstants.TBL_CLIENTS, null,
      Sets.newHashSet(EcConstants.COL_CLIENT_MANAGER), "EcClientUsage",
      Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME)),
  EC_ORDERS(Localized.getConstants().ecOrders(), EcConstants.TBL_ORDERS, null,
      Sets.newHashSet(EcConstants.COL_ORDER_STATUS), "EcOrderUsage",
      Lists.newArrayList(EcConstants.COL_ORDER_NUMBER, EcConstants.ALS_ORDER_CLIENT_COMPANY_NAME)),
  EC_REGISTRATIONS(Localized.getConstants().ecRegistrations(), EcConstants.TBL_REGISTRATIONS, null,
      Collections.<String> emptySet(), "EcRegUsage",
      Lists.newArrayList(EcConstants.COL_REGISTRATION_FIRST_NAME,
          EcConstants.COL_REGISTRATION_LAST_NAME)),

//  PURCHASES("PurchaseUsage"),
//  SALES("SaleUsage"),

  CARGO_REQUESTS(Localized.getConstants().trRequests(), TBL_CARGO_REQUESTS, null,
      Sets.newHashSet(COL_CARGO_REQUEST_STATUS), "CargoRequestUsage",
      Lists.newArrayList(ALS_REQUEST_CUSTOMER_FIRST_NAME, ALS_REQUEST_CUSTOMER_LAST_NAME)),
  DRIVERS(Localized.getConstants().drivers(), TBL_DRIVERS, null,
      Sets.newHashSet(COL_DRIVER_START_DATE, COL_DRIVER_END_DATE), "DriverUsage",
      Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME)),
  ORDER_CARGO(Localized.getConstants().cargos(), TBL_ORDER_CARGO, null,
      Sets.newHashSet(COL_ORDER, loadingColumnAlias(COL_PLACE_DATE)), "OrderCargoUsage",
      Lists.newArrayList(loadingColumnAlias(COL_PLACE_DATE), COL_CARGO_DESCRIPTION)),
  SHIPMENT_REQUESTS(Localized.getConstants().trRequestsUnregistered(), TBL_SHIPMENT_REQUESTS, null,
      Sets.newHashSet(COL_QUERY_STATUS), "ShipmentReqUsage",
      Lists.newArrayList(COL_QUERY_CUSTOMER_NAME)),
  TRANSPORTATION_ORDERS(Localized.getConstants().trTransportationOrders(), TBL_ORDERS, null,
      Sets.newHashSet(COL_STATUS), "TranspOrderUsage",
      Lists.newArrayList(COL_ORDER_DATE, COL_ORDER_NO)),
  TRANSPORT_REGISTRATIONS(Localized.getConstants().trRegistrations(), TBL_REGISTRATIONS, null,
      Sets.newHashSet(COL_REGISTRATION_STATUS), "TranspRegUsage",
      Lists.newArrayList(COL_REGISTRATION_COMPANY_NAME)),
  TRIPS(Localized.getConstants().trips(), TBL_TRIPS, null,
      Sets.newHashSet(COL_TRIP_STATUS), "TripUsage",
      Lists.newArrayList(COL_TRIP_DATE, COL_TRIP_NO)),
  VEHICLES(Localized.getConstants().vehicles(), TBL_VEHICLES, null,
      Sets.newHashSet(COL_VEHICLE_NUMBER), "VehicleUsage",
      Lists.newArrayList(COL_VEHICLE_NUMBER));

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

  private final String caption;
  private final String table;

  private final String userColumn;

  private final Set<String> observerColumns;

  private final String usageTable;

  private final List<String> headlineColumns;

  private Feed(String caption, String table, String userColumn, Set<String> observerColumns,
      String usageTable, List<String> headlineColumns) {
    this.caption = caption;
    this.table = table;
    this.userColumn = userColumn;
    this.observerColumns = observerColumns;
    this.usageTable = usageTable;
    this.headlineColumns = headlineColumns;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public List<String> getHeadlineColumns() {
    return headlineColumns;
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
