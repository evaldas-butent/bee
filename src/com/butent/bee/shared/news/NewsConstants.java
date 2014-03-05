package com.butent.bee.shared.news;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.classifiers.ClassifiersConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsConstants;
import com.butent.bee.shared.modules.documents.DocumentsConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public final class NewsConstants {

  public static final String TBL_USER_FEEDS = "UserFeeds";
  public static final String VIEW_USER_FEEDS = "UserFeeds";

  public static final String COL_UF_USER = "User";
  public static final String COL_UF_FEED = "Feed";

  public static final String COL_UF_CAPTION = "Caption";
  public static final String COL_UF_SUBSCRIPTION_DATE = "SubscriptionDate";
  public static final String COL_UF_ORDINAL = "Ordinal";

  public static final String COL_USAGE_USER = "User";
  public static final String COL_USAGE_ACCESS = "Access";
  public static final String COL_USAGE_UPDATE = "Update";

  public static final String GRID_USER_FEEDS = "UserFeeds";

  private static final Map<String, String> usageTables = Maps.newHashMap();
  private static final Multimap<String, String> observedColumns = HashMultimap.create();

  static {
    initUsageTables();
    initObservedColumns();
  }

  public static boolean anyObserved(String table, List<? extends IsColumn> columns) {
    if (hasObservedColumns(table) && !BeeUtils.isEmpty(columns)) {
      for (IsColumn column : columns) {
        if (observedColumns.containsEntry(table, column.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  public static String getUsageTable(String table) {
    return (table == null) ? null : usageTables.get(table);
  }

  public static boolean hasObservedColumns(String table) {
    return (table == null) ? false : observedColumns.containsKey(table);
  }

  public static boolean hasUsageTable(String table) {
    return (table == null) ? false : usageTables.containsKey(table);
  }

  private static void initObservedColumns() {
    observedColumns.put(ClassifiersConstants.TBL_COMPANY_USERS,
        ClassifiersConstants.COL_COMPANY_USER_RESPONSIBILITY);
    observedColumns.put(ClassifiersConstants.TBL_COMPANIES, ClassifiersConstants.COL_COMPANY_NAME);

    observedColumns.put(ClassifiersConstants.TBL_PERSONS, ClassifiersConstants.COL_FIRST_NAME);
    observedColumns.put(ClassifiersConstants.TBL_PERSONS, ClassifiersConstants.COL_LAST_NAME);

    observedColumns.put(ClassifiersConstants.TBL_ITEMS, ClassifiersConstants.COL_ITEM_NAME);
    observedColumns.put(ClassifiersConstants.TBL_ITEMS, ClassifiersConstants.COL_ITEM_ARTICLE);
    observedColumns.put(ClassifiersConstants.TBL_ITEMS, ClassifiersConstants.COL_ITEM_PRICE);
    observedColumns.put(ClassifiersConstants.TBL_ITEMS, ClassifiersConstants.COL_ITEM_CURRENCY);

    observedColumns.put(CalendarConstants.TBL_APPOINTMENTS, CalendarConstants.COL_START_DATE_TIME);
    observedColumns.put(CalendarConstants.TBL_APPOINTMENTS, CalendarConstants.COL_END_DATE_TIME);
    observedColumns.put(CalendarConstants.TBL_APPOINTMENTS, CalendarConstants.COL_STATUS);

    observedColumns.put(EcConstants.TBL_CLIENTS, EcConstants.COL_CLIENT_MANAGER);
    observedColumns.put(EcConstants.TBL_ORDERS, EcConstants.COL_ORDER_STATUS);

    observedColumns.put(TransportConstants.TBL_ORDER_CARGO, TransportConstants.COL_ORDER);
    observedColumns.put(TransportConstants.TBL_ORDER_CARGO,
        TransportConstants.loadingColumnAlias(TransportConstants.COL_PLACE_DATE));

    observedColumns.put(TransportConstants.TBL_ORDERS, TransportConstants.COL_STATUS);
    observedColumns.put(TransportConstants.TBL_TRIPS, TransportConstants.COL_TRIP_STATUS);

    observedColumns.put(TransportConstants.TBL_CARGO_REQUESTS,
        TransportConstants.COL_CARGO_REQUEST_STATUS);
    observedColumns.put(TransportConstants.TBL_SHIPMENT_REQUESTS,
        TransportConstants.COL_QUERY_STATUS);
    observedColumns.put(TransportConstants.TBL_REGISTRATIONS,
        TransportConstants.COL_REGISTRATION_STATUS);

    observedColumns.put(TransportConstants.TBL_VEHICLES, TransportConstants.COL_VEHICLE_NUMBER);

    observedColumns.put(TransportConstants.TBL_DRIVERS, TransportConstants.COL_DRIVER_START_DATE);
    observedColumns.put(TransportConstants.TBL_DRIVERS, TransportConstants.COL_DRIVER_END_DATE);

  }

  private static void initUsageTables() {
    usageTables.put(ClassifiersConstants.TBL_COMPANY_USERS, "CompanyUserUsage");
    usageTables.put(ClassifiersConstants.TBL_COMPANIES, "CompanyUsage");
    usageTables.put(ClassifiersConstants.TBL_PERSONS, "PersonUsage");
    usageTables.put(ClassifiersConstants.TBL_ITEMS, "ItemUsage");

    usageTables.put(DocumentsConstants.TBL_DOCUMENTS, "DocumentUsage");

    usageTables.put(CalendarConstants.TBL_APPOINTMENT_ATTENDEES, "AppAttUsage");
    usageTables.put(CalendarConstants.TBL_APPOINTMENTS, "AppointmentUsage");

    usageTables.put(EcConstants.TBL_CLIENTS, "EcClientUsage");
    usageTables.put(EcConstants.TBL_ORDERS, "EcOrderUsage");
    usageTables.put(EcConstants.TBL_REGISTRATIONS, "EcRegUsage");

    usageTables.put(TransportConstants.TBL_ORDER_CARGO, "OrderCargoUsage");
    usageTables.put(TransportConstants.TBL_ORDERS, "TranspOrderUsage");
    usageTables.put(TransportConstants.TBL_TRIPS, "TripUsage");

    usageTables.put(TransportConstants.TBL_CARGO_REQUESTS, "CargoRequestUsage");
    usageTables.put(TransportConstants.TBL_SHIPMENT_REQUESTS, "ShipmentReqUsage");
    usageTables.put(TransportConstants.TBL_REGISTRATIONS, "TranspRegUsage");

    usageTables.put(TransportConstants.TBL_VEHICLES, "VehicleUsage");
    usageTables.put(TransportConstants.TBL_DRIVERS, "DriverUsage");
    usageTables.put(DiscussionsConstants.TBL_DISCUSSIONS,
        DiscussionsConstants.TBL_DISCUSSIONS_USAGE);
  }

  private NewsConstants() {
  }
}
