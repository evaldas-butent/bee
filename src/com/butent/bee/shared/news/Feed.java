package com.butent.bee.shared.news;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.ui.HasCaption;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum Feed implements HasCaption {
  TASKS_ASSIGNED(Localized.getConstants().feedTasksAssigned(), CrmConstants.TBL_TASKS,
      Sets.newHashSet(CrmConstants.COL_FINISH_TIME, CrmConstants.COL_STATUS),
      CrmConstants.VIEW_TASKS, Lists.newArrayList(CrmConstants.COL_START_TIME,
          CrmConstants.COL_STATUS)),
  TASKS_DELEGATED(Localized.getConstants().feedTasksDelegated(), CrmConstants.TBL_TASKS,
      Sets.newHashSet(CrmConstants.COL_FINISH_TIME, CrmConstants.COL_STATUS),
      CrmConstants.VIEW_TASKS, Lists.newArrayList(CrmConstants.COL_START_TIME,
          CrmConstants.COL_STATUS)),
  TASKS_OBSERVED(Localized.getConstants().feedTasksObserved(), CrmConstants.TBL_TASKS,
      Sets.newHashSet(CrmConstants.COL_FINISH_TIME, CrmConstants.COL_STATUS),
      CrmConstants.VIEW_TASKS, Lists.newArrayList(CrmConstants.COL_START_TIME,
          CrmConstants.COL_STATUS)),
  TASKS_ALL(Localized.getConstants().feedTasksAll(), CrmConstants.TBL_TASKS,
      Sets.newHashSet(CrmConstants.COL_FINISH_TIME, CrmConstants.COL_STATUS),
      CrmConstants.VIEW_TASKS, Lists.newArrayList(CrmConstants.COL_START_TIME,
          CrmConstants.COL_STATUS)),

  COMPANIES_MY(Localized.getConstants().feedCompaniesMy(), CommonsConstants.TBL_COMPANY_USERS,
      Sets.newHashSet(CommonsConstants.COL_COMPANY_USER_RESPONSIBILITY),
      CommonsConstants.VIEW_COMPANIES, Lists.newArrayList(CommonsConstants.COL_COMPANY_NAME)),
  COMPANIES_ALL(Localized.getConstants().feedCompaniesAll(), CommonsConstants.TBL_COMPANIES,
      Sets.newHashSet(CommonsConstants.COL_COMPANY_NAME), CommonsConstants.VIEW_COMPANIES,
      Lists.newArrayList(CommonsConstants.COL_COMPANY_NAME)),
  PERSONS(Localized.getConstants().feedPersons(), CommonsConstants.TBL_PERSONS,
      Sets.newHashSet(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME),
      CommonsConstants.VIEW_PERSONS, Lists.newArrayList(CommonsConstants.COL_FIRST_NAME,
          CommonsConstants.COL_LAST_NAME)),
  GOODS(Localized.getConstants().feedGoods(), CommonsConstants.TBL_ITEMS,
      Sets.newHashSet(CommonsConstants.COL_ITEM_NAME, CommonsConstants.COL_ITEM_ARTICLE,
          CommonsConstants.COL_ITEM_PRICE, CommonsConstants.COL_ITEM_CURRENCY),
      CommonsConstants.VIEW_ITEMS, Lists.newArrayList(CommonsConstants.COL_ITEM_NAME,
          CommonsConstants.COL_ITEM_ARTICLE)),

  DOCUMENTS(Localized.getConstants().feedDocuments(), CrmConstants.TBL_DOCUMENTS,
      Collections.<String> emptySet(), CrmConstants.VIEW_DOCUMENTS,
      Lists.newArrayList(CrmConstants.COL_DOCUMENT_NAME)),

  APPOINTMENTS_MY(Localized.getConstants().feedAppointmentsMy(),
      CalendarConstants.TBL_APPOINTMENT_ATTENDEES,
      Sets.newHashSet(CalendarConstants.COL_START_DATE_TIME, CalendarConstants.COL_END_DATE_TIME),
      CalendarConstants.VIEW_APPOINTMENTS,
      Lists.newArrayList(CalendarConstants.COL_START_DATE_TIME, CalendarConstants.COL_SUMMARY)),
  APPOINTMENTS_ALL(Localized.getConstants().feedAppointmentsAll(),
      CalendarConstants.TBL_APPOINTMENTS,
      Sets.newHashSet(CalendarConstants.COL_START_DATE_TIME, CalendarConstants.COL_END_DATE_TIME),
      CalendarConstants.VIEW_APPOINTMENTS,
      Lists.newArrayList(CalendarConstants.COL_START_DATE_TIME, CalendarConstants.COL_SUMMARY)),

  EC_CLIENTS_MY(Localized.getConstants().feedEcClientsMy(), EcConstants.TBL_CLIENTS,
      Sets.newHashSet(EcConstants.COL_CLIENT_MANAGER), EcConstants.VIEW_CLIENTS,
      Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME)),
  EC_CLIENTS_ALL(Localized.getConstants().feedEcClientsAll(), EcConstants.TBL_CLIENTS,
      Sets.newHashSet(EcConstants.COL_CLIENT_MANAGER), EcConstants.VIEW_CLIENTS,
      Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME)),
  EC_ORDERS_MY(Localized.getConstants().feedEcOrdersMy(), EcConstants.TBL_ORDERS,
      Sets.newHashSet(EcConstants.COL_ORDER_STATUS), EcConstants.VIEW_ORDERS,
      Lists.newArrayList(EcConstants.COL_ORDER_NUMBER, EcConstants.ALS_ORDER_CLIENT_COMPANY_NAME)),
  EC_ORDERS_ALL(Localized.getConstants().feedEcOrdersAll(), EcConstants.TBL_ORDERS,
      Sets.newHashSet(EcConstants.COL_ORDER_STATUS), EcConstants.VIEW_ORDERS,
      Lists.newArrayList(EcConstants.COL_ORDER_NUMBER, EcConstants.ALS_ORDER_CLIENT_COMPANY_NAME)),
  EC_REGISTRATIONS(Localized.getConstants().feedEcRegistrations(), EcConstants.TBL_REGISTRATIONS,
      Collections.<String> emptySet(), EcConstants.VIEW_REGISTRATIONS,
      Lists.newArrayList(EcConstants.COL_REGISTRATION_FIRST_NAME,
          EcConstants.COL_REGISTRATION_LAST_NAME)),

  ORDER_CARGO(Localized.getConstants().feedTrCargo(), TransportConstants.TBL_ORDER_CARGO,
      Sets.newHashSet(TransportConstants.COL_ORDER,
          TransportConstants.loadingColumnAlias(TransportConstants.COL_PLACE_DATE)),
      TransportConstants.VIEW_ORDER_CARGO,
      Lists.newArrayList(TransportConstants.loadingColumnAlias(TransportConstants.COL_PLACE_DATE),
          TransportConstants.COL_CARGO_DESCRIPTION)),
  TRANSPORTATION_ORDERS_MY(Localized.getConstants().feedTrOrdersMy(),
      TransportConstants.TBL_ORDERS, Sets.newHashSet(TransportConstants.COL_STATUS),
      TransportConstants.VIEW_ORDERS, Lists.newArrayList(TransportConstants.COL_ORDER_DATE,
          TransportConstants.COL_ORDER_NO)),
  TRANSPORTATION_ORDERS_ALL(Localized.getConstants().feedTrOrdersAll(),
      TransportConstants.TBL_ORDERS, Sets.newHashSet(TransportConstants.COL_STATUS),
      TransportConstants.VIEW_ORDERS, Lists.newArrayList(TransportConstants.COL_ORDER_DATE,
          TransportConstants.COL_ORDER_NO)),
  TRIPS(Localized.getConstants().feedTrTrips(), TransportConstants.TBL_TRIPS,
      Sets.newHashSet(TransportConstants.COL_TRIP_STATUS), TransportConstants.VIEW_TRIPS,
      Lists.newArrayList(TransportConstants.COL_TRIP_DATE, TransportConstants.COL_TRIP_NO)),

  CARGO_REQUESTS_MY(Localized.getConstants().feedTrRequestsMy(),
      TransportConstants.TBL_CARGO_REQUESTS,
      Sets.newHashSet(TransportConstants.COL_CARGO_REQUEST_STATUS),
      TransportConstants.VIEW_CARGO_REQUESTS,
      Lists.newArrayList(TransportConstants.ALS_REQUEST_CUSTOMER_FIRST_NAME,
          TransportConstants.ALS_REQUEST_CUSTOMER_LAST_NAME)),
  CARGO_REQUESTS_ALL(Localized.getConstants().feedTrRequestsAll(),
      TransportConstants.TBL_CARGO_REQUESTS,
      Sets.newHashSet(TransportConstants.COL_CARGO_REQUEST_STATUS),
      TransportConstants.VIEW_CARGO_REQUESTS,
      Lists.newArrayList(TransportConstants.ALS_REQUEST_CUSTOMER_FIRST_NAME,
          TransportConstants.ALS_REQUEST_CUSTOMER_LAST_NAME)),
  SHIPMENT_REQUESTS_MY(Localized.getConstants().feedTrRequestsUnregisteredMy(),
      TransportConstants.TBL_SHIPMENT_REQUESTS,
      Sets.newHashSet(TransportConstants.COL_QUERY_STATUS),
      TransportConstants.VIEW_SHIPMENT_REQUESTS,
      Lists.newArrayList(TransportConstants.COL_QUERY_CUSTOMER_NAME)),
  SHIPMENT_REQUESTS_ALL(Localized.getConstants().feedTrRequestsUnregisteredAll(),
      TransportConstants.TBL_SHIPMENT_REQUESTS,
      Sets.newHashSet(TransportConstants.COL_QUERY_STATUS),
      TransportConstants.VIEW_SHIPMENT_REQUESTS,
      Lists.newArrayList(TransportConstants.COL_QUERY_CUSTOMER_NAME)),
  TRANSPORT_REGISTRATIONS(Localized.getConstants().feedTrRegistrations(),
      TransportConstants.TBL_REGISTRATIONS,
      Sets.newHashSet(TransportConstants.COL_REGISTRATION_STATUS),
      TransportConstants.VIEW_REGISTRATIONS,
      Lists.newArrayList(TransportConstants.COL_REGISTRATION_COMPANY_NAME)),

  VEHICLES(Localized.getConstants().feedTrVehicles(), TransportConstants.TBL_VEHICLES,
      Sets.newHashSet(TransportConstants.COL_VEHICLE_NUMBER), TransportConstants.VIEW_VEHICLES,
      Lists.newArrayList(TransportConstants.COL_VEHICLE_NUMBER)),
  DRIVERS(Localized.getConstants().feedTrDrivers(), TransportConstants.TBL_DRIVERS,
      Sets.newHashSet(TransportConstants.COL_DRIVER_START_DATE,
          TransportConstants.COL_DRIVER_END_DATE), TransportConstants.VIEW_DRIVERS,
      Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME));

  private final String caption;
  private final String table;

  private final Set<String> observerColumns;

  private final String headlineView;
  private final List<String> headlineColumns;

  private Feed(String caption, String table, Set<String> observerColumns,
      String headlineView, List<String> headlineColumns) {
    this.caption = caption;
    this.table = table;
    this.observerColumns = observerColumns;
    this.headlineView = headlineView;
    this.headlineColumns = headlineColumns;
  }

  @Override
  public String getCaption() {
    return caption;
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
    return (table == null) ? null : NewsUtils.getUsageTable(table);
  }
}
