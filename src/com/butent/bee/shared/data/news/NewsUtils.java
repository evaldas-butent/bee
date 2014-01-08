package com.butent.bee.shared.data.news;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class NewsUtils {

  private static final Map<String, String> usageTables = Maps.newHashMap();

  private static final String FEED_SEPARATOR = BeeConst.STRING_COMMA;
  private static final Splitter feedSplitter =
      Splitter.on(FEED_SEPARATOR).omitEmptyStrings().trimResults();

  static {
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

  public static String getUsageTable(String table) {
    return (table == null) ? null : usageTables.get(table);
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
  
  public static Set<Feed> splitFeeds(String input) {
    Set<Feed> feeds = Sets.newHashSet();
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

  private NewsUtils() {
  }
}
