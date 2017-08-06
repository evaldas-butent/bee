package com.butent.bee.client.rights;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.i18n.Collator;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.cars.CarsConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.RightsObjectType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

final class ListRightsHandler extends MultiStateForm {

  private static final Comparator<RightsObject> comparator = (o1, o2)
      -> ComparisonChain.start()
      .compare(o1.getModuleAndSub(), o2.getModuleAndSub(), Ordering.natural().nullsLast())
      .compare(o1.getParent(), o2.getParent(), Ordering.natural().nullsFirst())
      .compare(o1.getCaption(), o2.getCaption(), Collator.CASE_INSENSITIVE_NULLS_FIRST)
      .compare(o1.getName(), o2.getName())
      .result();

  private static final Multimap<String, Pair<String, String>> lists = ArrayListMultimap.create();

  ListRightsHandler() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new ListRightsHandler();
  }

  @Override
  protected RightsObjectType getObjectType() {
    return RightsObjectType.LIST;
  }

  @Override
  protected int getValueStartCol() {
    return 4;
  }

  @Override
  protected boolean hasValue(RightsObject object) {
    return object.hasParent();
  }

  @Override
  protected void initObjects(Consumer<List<RightsObject>> consumer) {
    if (lists.isEmpty()) {
      initLists(Localized.dictionary());
    }

    List<RightsObject> result = new ArrayList<>();

    lists.keySet().forEach(viewName -> {
      DataInfo dataInfo = Data.getDataInfo(viewName);

      if (dataInfo != null) {
        String module = dataInfo.getModule();
        ModuleAndSub ms = RightsHelper.getFirstVisibleModule(module);

        if (ms != null || Module.NEVER_MIND.equals(module)) {
          String viewCaption = Localized.maybeTranslate(dataInfo.getCaption());

          RightsObject viewObject = new RightsObject(viewName,
              BeeUtils.notEmpty(viewCaption, viewName), ms);
          result.add(viewObject);

          lists.get(viewName).forEach(pair -> {
            String name = pair.getA();
            String caption = pair.getB();

            result.add(new RightsObject(name, BeeUtils.notEmpty(caption, name), viewName));
          });
        }
      }
    });

    result.sort(comparator);
    consumer.accept(result);
  }

  private static void initLists(Dictionary dictionary) {
    add(AdministrationConstants.VIEW_USERS, AdministrationConstants.TBL_USER_ROLES,
        dictionary.roles());

    add(CalendarConstants.VIEW_CALENDARS, CalendarConstants.VIEW_CAL_ATTENDEE_TYPES,
        dictionary.calAttendeeTypes());
    add(CalendarConstants.VIEW_CALENDARS, CalendarConstants.VIEW_CALENDAR_ATTENDEES,
        dictionary.calAttendees());
    add(CalendarConstants.VIEW_CALENDARS, CalendarConstants.VIEW_CAL_APPOINTMENT_TYPES,
        dictionary.calAppointmentTypes());

    add(CarsConstants.TBL_SERVICE_EVENTS, CarsConstants.TBL_SERVICE_SYMPTOMS,
        dictionary.symptoms());
    add(CarsConstants.TBL_SERVICE_ORDERS, CarsConstants.TBL_SERVICE_SYMPTOMS,
        dictionary.symptoms());
    add(CarsConstants.TBL_CONF_OPTIONS, CarsConstants.TBL_CONF_PACKET_OPTIONS,
        dictionary.packet());

    add(ClassifierConstants.VIEW_COMPANIES, ClassifierConstants.TBL_COMPANY_ACTIVITY_STORE,
        dictionary.companyActivities());
    add(ClassifierConstants.VIEW_COMPANIES, ClassifierConstants.TBL_COMPANY_RELATION_TYPE_STORE,
        dictionary.companyRelation());

    add(ClassifierConstants.VIEW_COMPANIES, MailConstants.VIEW_RCPS_GROUPS_CONTACTS,
        dictionary.mailRecipientsGroups());
    add(ClassifierConstants.VIEW_COMPANY_PERSONS, MailConstants.VIEW_RCPS_GROUPS_CONTACTS,
        dictionary.mailRecipientsGroups());
    add(ClassifierConstants.VIEW_COMPANY_CONTACTS, MailConstants.VIEW_RCPS_GROUPS_CONTACTS,
        dictionary.mailRecipientsGroups());
    add(ClassifierConstants.VIEW_PERSONS, MailConstants.VIEW_RCPS_GROUPS_CONTACTS,
        dictionary.mailRecipientsGroups());

    add(ClassifierConstants.VIEW_ITEMS, ClassifierConstants.TBL_ITEM_CATEGORIES,
        dictionary.itemCategories());

    add(EcConstants.VIEW_CLIENTS, EcConstants.TBL_PRIMARY_WAREHOUSES,
        dictionary.ecWarehousesPrimary());
    add(EcConstants.VIEW_CLIENTS, EcConstants.TBL_SECONDARY_WAREHOUSES,
        dictionary.ecWarehousesSecondary());

    add(MailConstants.TBL_ACCOUNTS, MailConstants.TBL_ACCOUNT_USERS, dictionary.users());

    add(TaskConstants.VIEW_RECURRING_TASKS, TaskConstants.TBL_RT_EXECUTORS,
        dictionary.crmTaskExecutors());
    add(TaskConstants.VIEW_RECURRING_TASKS, TaskConstants.TBL_RT_EXECUTOR_GROUPS,
        dictionary.crmTaskExecutorGroups());
    add(TaskConstants.VIEW_RECURRING_TASKS, TaskConstants.TBL_RT_OBSERVERS,
        dictionary.crmTaskObservers());
    add(TaskConstants.VIEW_RECURRING_TASKS, TaskConstants.TBL_RT_OBSERVER_GROUPS,
        dictionary.crmTaskObserverGroups());

    add(TradeConstants.VIEW_TRADE_DOCUMENTS, TradeConstants.VIEW_TRADE_DOCUMENT_TAGS,
        dictionary.tags());

    add(TradeConstants.VIEW_TRADE_DOCUMENT_TYPES, TradeConstants.TBL_TRADE_TYPE_OPERATIONS,
        dictionary.trdOperations());
    add(TradeConstants.VIEW_TRADE_DOCUMENT_TYPES, TradeConstants.TBL_TRADE_TYPE_STATUSES,
        dictionary.statuses());
    add(TradeConstants.VIEW_TRADE_DOCUMENT_TYPES, TradeConstants.TBL_TRADE_TYPE_TAGS,
        dictionary.tags());

    add(TransportConstants.VIEW_DRIVERS, TransportConstants.TBL_DRIVER_GROUPS,
        dictionary.driverGroups());
    add(TransportConstants.VIEW_VEHICLES, TransportConstants.TBL_VEHICLE_GROUPS,
        dictionary.vehicleGroups());
  }

  private static void add(String viewName, String name, String caption) {
    lists.put(viewName, Pair.of(name, caption));
  }
}
