package com.butent.bee.shared.news;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NewsUtils {

  public static final String COL_USAGE_USER = "User";
  public static final String COL_USAGE_ACCESS = "Access";
  public static final String COL_USAGE_UPDATE = "Update";
  
  private static final Map<String, String> usageTables = Maps.newHashMap();
  private static final Multimap<String, String> observedColumns = HashMultimap.create();

  private static final String FEED_SEPARATOR = BeeConst.STRING_COMMA;
  private static final Splitter feedSplitter =
      Splitter.on(FEED_SEPARATOR).omitEmptyStrings().trimResults();

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

  public static long getStartTime(DateTime startDate) {
    return (startDate == null) ? 0L : startDate.getTime();
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

  public static String joinFeeds(Collection<Feed> feeds) {
    if (BeeUtils.isEmpty(feeds)) {
      return BeeConst.STRING_EMPTY;
    }

    Set<Integer> ordinals = Sets.newHashSet();
    for (Feed feed : feeds) {
      if (feed != null) {
        ordinals.add(feed.ordinal());
      }
    }

    return BeeUtils.join(FEED_SEPARATOR, ordinals);
  }

  public static List<Feed> splitFeeds(String input) {
    List<Feed> feeds = Lists.newArrayList();
    if (BeeUtils.isEmpty(input)) {
      return feeds;
    }

    for (String s : feedSplitter.split(input)) {
      Feed feed = EnumUtils.getEnumByIndex(Feed.class, s);
      if (feed != null) {
        feeds.add(feed);
      }
    }
    return feeds;
  }

  private static void initObservedColumns() {
    observedColumns.put(CommonsConstants.TBL_COMPANY_USERS,
        CommonsConstants.COL_COMPANY_USER_RESPONSIBILITY);
    observedColumns.put(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_COMPANY_NAME);

    observedColumns.put(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_FIRST_NAME);
    observedColumns.put(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_LAST_NAME);

    observedColumns.put(CommonsConstants.TBL_ITEMS, CommonsConstants.COL_ITEM_NAME);
    observedColumns.put(CommonsConstants.TBL_ITEMS, CommonsConstants.COL_ITEM_ARTICLE);
    observedColumns.put(CommonsConstants.TBL_ITEMS, CommonsConstants.COL_ITEM_PRICE);
    observedColumns.put(CommonsConstants.TBL_ITEMS, CommonsConstants.COL_ITEM_CURRENCY);

    observedColumns.put(CalendarConstants.TBL_APPOINTMENTS, CalendarConstants.COL_START_DATE_TIME);
    observedColumns.put(CalendarConstants.TBL_APPOINTMENTS, CalendarConstants.COL_END_DATE_TIME);

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
    usageTables.put(CommonsConstants.TBL_COMPANY_USERS, "CompanyUserUsage");
    usageTables.put(CommonsConstants.TBL_COMPANIES, "CompanyUsage");
    usageTables.put(CommonsConstants.TBL_PERSONS, "PersonUsage");
    usageTables.put(CommonsConstants.TBL_ITEMS, "ItemUsage");

    usageTables.put(CrmConstants.TBL_DOCUMENTS, "DocumentUsage");

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
  }

  private NewsUtils() {
  }
}
