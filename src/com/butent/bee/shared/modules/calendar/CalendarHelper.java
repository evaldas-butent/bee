package com.butent.bee.shared.modules.calendar;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.ALS_COMPANY_NAME;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.ALS_CONTACT_PERSON;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CalendarHelper {

  public static final String KEY_PERIOD = "Period";

  private static final String LABEL_SEPARATOR = BeeConst.STRING_COLON + BeeConst.STRING_SPACE;
  private static final String CHILD_SEPARATOR = ", ";

  private static final String SUBSTITUTE_PREFIX = "{";
  private static final String SUBSTITUTE_SUFFIX = "}";

  public static String build(String label, String value, boolean addLabel) {
    if (addLabel) {
      return join(label, value);
    } else {
      return BeeUtils.trim(value);
    }
  }

  public static Map<String, String> getAppointmentReminderDataLabels(Dictionary dic,
                                                                     String appointmentIdName) {
    Map<String, String> labels = new HashMap<>();

    labels.put(appointmentIdName, dic.captionId());
    labels.put(CalendarConstants.COL_CREATED, dic.registered());
    labels.put(CalendarConstants.COL_SUMMARY, dic.summary());
    labels.put(CalendarConstants.COL_DESCRIPTION, dic.description());
    labels.put(CalendarConstants.COL_STATUS, dic.calAppointmentStatus());
    labels.put(CalendarConstants.COL_START_DATE_TIME, dic.calAppointmentStart());
    labels.put(CalendarConstants.COL_END_DATE_TIME, dic.calAppointmentEnd());
    labels.put(CalendarConstants.ALS_APPOINTMENT_TYPE_NAME, dic.type());
    labels.put(ALS_COMPANY_NAME, dic.client());
    labels.put(ALS_CONTACT_PERSON, dic.companyPerson());

    return labels;
  }

  public static Map<String, ValueType> getAppointmentReminderDataTypes(String appointmentIdName) {
    Map<String, ValueType> dataTypes = new HashMap<>();

    dataTypes.put(appointmentIdName, ValueType.LONG);
    dataTypes.put(CalendarConstants.COL_CREATED, ValueType.DATE_TIME);
    dataTypes.put(CalendarConstants.COL_SUMMARY, ValueType.TEXT);
    dataTypes.put(CalendarConstants.COL_STATUS, ValueType.INTEGER);
    dataTypes.put(CalendarConstants.COL_START_DATE_TIME, ValueType.DATE_TIME);
    dataTypes.put(CalendarConstants.COL_END_DATE_TIME, ValueType.DATE_TIME);
    dataTypes.put(CalendarConstants.ALS_APPOINTMENT_TYPE_NAME, ValueType.TEXT);
    dataTypes.put(ALS_COMPANY_NAME, ValueType.TEXT);
    dataTypes.put(ALS_CONTACT_PERSON, ValueType.TEXT);

    return dataTypes;
  }

  public static Map<String, String> getAppointmentReminderDataEnumKeys() {
    Map<String, String> enumKeys = new HashMap<>();
    enumKeys.put(CalendarConstants.COL_STATUS,
            NameUtils.getClassName(CalendarConstants.AppointmentStatus.class));

    return enumKeys;
  }

  public static boolean hasSubstitutes(String s) {
    return s != null && s.contains(SUBSTITUTE_PREFIX) && s.contains(SUBSTITUTE_SUFFIX);
  }

  public static String join(String label, String value) {
    if (BeeUtils.isEmpty(value)) {
      return BeeConst.STRING_EMPTY;
    } else if (BeeUtils.isEmpty(label)) {
      return BeeUtils.trim(value);
    } else {
      return label + LABEL_SEPARATOR + BeeUtils.trim(value);
    }
  }

  public static String joinChildren(List<String> children) {
    return BeeUtils.join(CHILD_SEPARATOR, children);
  }

  public static String wrap(String s) {
    return SUBSTITUTE_PREFIX + s.trim() + SUBSTITUTE_SUFFIX;
  }

  private CalendarHelper() {
  }
}
