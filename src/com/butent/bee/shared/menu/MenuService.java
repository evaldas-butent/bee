package com.butent.bee.shared.menu;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.shared.modules.administration.AdministrationConstants.RightsState;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;

import java.util.EnumSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlEnumValue;

public enum MenuService {

  @XmlEnumValue("form")
  FORM(RightsState.VIEW),
  @XmlEnumValue("grid")
  GRID(RightsState.VIEW),
  @XmlEnumValue("new")
  NEW(EnumSet.of(RightsState.VIEW, RightsState.EDIT, RightsState.CREATE), true),
  @XmlEnumValue("parameters")
  PARAMETERS(RightsState.VIEW,
      Sets.newHashSet(VIEW_PARAMETERS, VIEW_USER_PARAMETERS)),

  @XmlEnumValue("items")
  ITEMS(RightsState.VIEW, ClassifierConstants.VIEW_ITEMS),
  @XmlEnumValue("update_exchange_rates")
  UPDATE_EXCHANGE_RATES(EnumSet.of(RightsState.VIEW, RightsState.CREATE),
      VIEW_CURRENCY_RATES),

  @XmlEnumValue("calendar_reports")
  CALENDAR_REPORTS(RightsState.VIEW, CalendarConstants.VIEW_APPOINTMENTS),

  @XmlEnumValue("task_list")
  TASK_LIST(RightsState.VIEW, TaskConstants.VIEW_TASKS),
  @XmlEnumValue("task_reports")
  TASK_REPORTS(RightsState.VIEW, TaskConstants.VIEW_TASKS),

  @XmlEnumValue("discuss_list")
  DISCUSS_LIST(RightsState.VIEW),

  @XmlEnumValue("open_mail")
  OPEN_MAIL(RightsState.VIEW),
  @XmlEnumValue("restart_proxy")
  RESTART_PROXY(RightsState.VIEW),

  @XmlEnumValue("ensure_categories_and_open_grid")
  ENSURE_CATEGORIES_AND_OPEN_GRID(RightsState.VIEW, true),
  @XmlEnumValue("edit_terms_of_delivery")
  EDIT_TERMS_OF_DELIVERY(EnumSet.of(RightsState.VIEW, RightsState.EDIT)),
  @XmlEnumValue("edit_ec_contacts")
  EDIT_EC_CONTACTS(EnumSet.of(RightsState.VIEW, RightsState.EDIT)),

  @XmlEnumValue("freight_exchange")
  FREIGHT_EXCHANGE(RightsState.VIEW, TransportConstants.VIEW_ORDER_CARGO),
  @XmlEnumValue("shipping_schedule")
  SHIPPING_SCHEDULE(RightsState.VIEW, TransportConstants.VIEW_TRIPS),
  @XmlEnumValue("driver_time_board")
  DRIVER_TIME_BOARD(RightsState.VIEW, TransportConstants.VIEW_DRIVERS),
  @XmlEnumValue("truck_time_board")
  TRUCK_TIME_BOARD(RightsState.VIEW, TransportConstants.VIEW_TRIPS),
  @XmlEnumValue("trailer_time_board")
  TRAILER_TIME_BOARD(RightsState.VIEW, TransportConstants.VIEW_TRIPS);

  public interface DataNameProvider extends Function<String, Set<String>> {
  }

  private final Set<RightsState> dataRightsStates;
  private DataNameProvider dataNameProvider;

  private MenuHandler handler;

  private MenuService(RightsState dataRightsState) {
    this.dataRightsStates = EnumSet.of(dataRightsState);
  }

  private MenuService(RightsState dataRightsState, String dataName) {
    this(dataRightsState);
    setDataName(dataName);
  }

  private MenuService(RightsState dataRightsState, Set<String> dataNames) {
    this(dataRightsState);
    setDataNames(dataNames);
  }

  private MenuService(RightsState dataRightsState, boolean dataIsParameter) {
    this(dataRightsState);
    if (dataIsParameter) {
      setDataIsParameter();
    }
  }

  private MenuService(EnumSet<RightsState> dataRightsStates) {
    this.dataRightsStates = dataRightsStates;
  }

  private MenuService(EnumSet<RightsState> dataRightsStates, String dataName) {
    this(dataRightsStates);
    setDataName(dataName);
  }

  private MenuService(EnumSet<RightsState> dataRightsStates, boolean dataIsParameter) {
    this(dataRightsStates);
    if (dataIsParameter) {
      setDataIsParameter();
    }
  }

  public Set<RightsState> getDataRightsStates() {
    return dataRightsStates;
  }

  public MenuHandler getHandler() {
    return handler;
  }

  public Set<String> getDataNames(String parameter) {
    return dataNameProvider == null ? null : dataNameProvider.apply(parameter);
  }

  public void setDataName(final String dataName) {
    setDataNames(Sets.newHashSet(dataName));
  }

  public void setDataNames(final Set<String> dataNames) {
    this.dataNameProvider = new DataNameProvider() {
      @Override
      public Set<String> apply(String input) {
        return dataNames;
      }
    };
  }

  public void setDataIsParameter() {
    this.dataNameProvider = new DataNameProvider() {
      @Override
      public Set<String> apply(String input) {
        return Sets.newHashSet(input);
      }
    };
  }

  public void setDataNameProvider(DataNameProvider dataNameProvider) {
    this.dataNameProvider = dataNameProvider;
  }

  public void setHandler(MenuHandler handler) {
    this.handler = handler;
  }
}
