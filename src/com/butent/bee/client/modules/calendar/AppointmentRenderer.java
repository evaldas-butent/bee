package com.butent.bee.client.modules.calendar;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

class AppointmentRenderer {

  private static final String DEFAULT_SIMPLE_HEADER_TEMPLATE;
  private static final String DEFAULT_SIMPLE_BODY_TEMPLATE;

  private static final String DEFAULT_MULTI_HEADER_TEMPLATE;
  private static final String DEFAULT_MULTI_BODY_TEMPLATE;

  private static final String DEFAULT_COMPACT_TEMPLATE;
  private static final String DEFAULT_TITLE_TEMPLATE;

  private static final Splitter TEMPLATE_SPLITTER =
      Splitter.on(CharMatcher.inRange('\u0000', '\u001f')).omitEmptyStrings().trimResults();

  private static final String HTML_LINE_SEPARATOR = "<br/>";
  private static final String TEXT_LINE_SEPARATOR = String.valueOf('\n');

  private static final String SIMPLE_HTML_SEPARATOR = HTML_LINE_SEPARATOR;
  private static final String MULTI_HTML_SEPARATOR = " ";

  private static final String CHILD_SEPARATOR = ", ";
  private static final String PERIOD_SEPARATOR = " - ";

  private static final String SUBSTITUTE_PREFIX = "{";
  private static final String SUBSTITUTE_SUFFIX = "}";

  private static final String KEY_RESOURCES = "Resources";
  private static final String KEY_PROPERTIES = "Properties";
  private static final String KEY_REMINDERS = "Reminders";

  private static final String KEY_PERIOD = "Period";

  private static final DateTimeFormat DATE_TIME_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
  private static final DateTimeFormat DATE_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
  private static final DateTimeFormat TIME_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_SHORT);

  private static final DateTimeFormat MONTH_DAY = DateTimeFormat.getFormat("MM-dd");
  private static final DateTimeFormat MONTH_DAY_TIME = DateTimeFormat.getFormat("MM-dd HH:mm");

  static {
    DEFAULT_SIMPLE_HEADER_TEMPLATE = wrap(COL_SUMMARY);
    DEFAULT_SIMPLE_BODY_TEMPLATE = BeeUtils.buildLines(wrap(COL_COMPANY_NAME),
        BeeUtils.concat(1, wrap(COL_VEHICLE_PARENT_MODEL), wrap(COL_VEHICLE_MODEL)),
        wrap(COL_VEHICLE_NUMBER), wrap(KEY_PROPERTIES), wrap(KEY_RESOURCES), wrap(COL_DESCRIPTION),
        wrap(KEY_REMINDERS));

    DEFAULT_MULTI_HEADER_TEMPLATE = BeeUtils.concat(1, wrap(KEY_PERIOD), wrap(COL_SUMMARY));
    DEFAULT_MULTI_BODY_TEMPLATE = BeeUtils.concat(1, wrap(COL_COMPANY_NAME),
        wrap(COL_VEHICLE_PARENT_MODEL), wrap(COL_VEHICLE_MODEL), wrap(COL_VEHICLE_NUMBER),
        wrap(KEY_PROPERTIES), wrap(KEY_RESOURCES));

    DEFAULT_COMPACT_TEMPLATE = wrap(COL_SUMMARY);
    DEFAULT_TITLE_TEMPLATE = BeeUtils.buildLines(wrap(KEY_PERIOD), wrap(COL_STATUS),
        BeeConst.STRING_EMPTY, wrap(COL_COMPANY_NAME),
        BeeUtils.concat(1, wrap(COL_VEHICLE_PARENT_MODEL), wrap(COL_VEHICLE_MODEL),
            wrap(COL_VEHICLE_NUMBER)), BeeConst.STRING_EMPTY,
        wrap(KEY_PROPERTIES), wrap(KEY_RESOURCES), BeeConst.STRING_EMPTY, wrap(COL_DESCRIPTION),
        BeeConst.STRING_EMPTY, BeeUtils.concat(1, wrap(COL_ORGANIZER_FIRST_NAME),
            wrap(COL_ORGANIZER_LAST_NAME)), wrap(KEY_REMINDERS));
  }

  private static String wrap(String s) {
    return SUBSTITUTE_PREFIX + s.trim() + SUBSTITUTE_SUFFIX;
  }

  AppointmentRenderer() {
    super();
  }

  void render(AppointmentWidget appointmentWidget, String headerTemplate, String bodyTemplate,
      String titleTemplate, boolean multi) {
    Map<String, String> substitutes = getSubstitutes(appointmentWidget.getAppointment());
    String separator = multi ? MULTI_HTML_SEPARATOR : SIMPLE_HTML_SEPARATOR;

    String template = BeeUtils.ifString(headerTemplate,
        multi ? DEFAULT_MULTI_HEADER_TEMPLATE : DEFAULT_SIMPLE_HEADER_TEMPLATE);
    String header = parseTemplate(template, substitutes, separator);
    appointmentWidget.setHeaderHtml(header);

    template = BeeUtils.ifString(bodyTemplate,
        multi ? DEFAULT_MULTI_BODY_TEMPLATE : DEFAULT_SIMPLE_BODY_TEMPLATE);
    String body = parseTemplate(template, substitutes, separator);
    appointmentWidget.setBodyHtml(body);

    template = BeeUtils.ifString(titleTemplate, DEFAULT_TITLE_TEMPLATE);
    String title = parseTemplate(template, substitutes, TEXT_LINE_SEPARATOR);
    appointmentWidget.setTitleText(title);
  }

  void renderMulti(AppointmentWidget appointmentWidget) {
    render(appointmentWidget, DEFAULT_MULTI_HEADER_TEMPLATE, DEFAULT_MULTI_BODY_TEMPLATE,
        DEFAULT_TITLE_TEMPLATE, true);
  }

  String renderPeriod(DateTime start, DateTime end) {
    if (start == null) {
      if (end == null) {
        return BeeConst.STRING_EMPTY;
      } else {
        return PERIOD_SEPARATOR + renderDateTime(end);
      }

    } else if (end == null) {
      return renderDateTime(start) + PERIOD_SEPARATOR;

    } else if (TimeUtils.sameDate(start, end)) {
      return renderDateTime(start) + PERIOD_SEPARATOR + TIME_FORMAT.format(end);

    } else {
      return renderDateTime(start) + PERIOD_SEPARATOR + renderDateTime(end);
    }
  }

  void renderSimple(AppointmentWidget appointmentWidget) {
    render(appointmentWidget, DEFAULT_SIMPLE_HEADER_TEMPLATE, DEFAULT_SIMPLE_BODY_TEMPLATE,
        DEFAULT_TITLE_TEMPLATE, false);
  }

  private Map<String, String> getSubstitutes(Appointment appointment) {
    Map<String, String> result = Maps.newHashMap();

    IsRow row = appointment.getRow();
    List<BeeColumn> columns = CalendarKeeper.getAppointmentViewColumns();

    for (int i = 0; i < columns.size(); i++) {
      String key = columns.get(i).getId();
      String value = row.getString(i);

      if (key.equals(COL_STATUS) && BeeUtils.isInt(value)) {
        value = UiHelper.getCaption(AppointmentStatus.class, BeeUtils.toInt(value));
      }

      result.put(wrap(key), BeeUtils.trim(value));
    }

    String attNames = BeeConst.STRING_EMPTY;
    String propNames = BeeConst.STRING_EMPTY;
    String remindNames = BeeConst.STRING_EMPTY;

    if (!appointment.getAttendees().isEmpty()) {
      for (Long id : appointment.getAttendees()) {
        attNames = BeeUtils.concat(CHILD_SEPARATOR, attNames, CalendarKeeper.getAttendeeName(id));
      }
    }
    if (!appointment.getProperties().isEmpty()) {
      for (Long id : appointment.getProperties()) {
        propNames = BeeUtils.concat(CHILD_SEPARATOR, propNames, CalendarKeeper.getPropertyName(id));
      }
    }
    if (!appointment.getReminders().isEmpty()) {
      for (Long id : appointment.getReminders()) {
        remindNames = BeeUtils.concat(CHILD_SEPARATOR, remindNames,
            CalendarKeeper.getReminderTypeName(id));
      }
    }

    result.put(wrap(KEY_RESOURCES), attNames);
    result.put(wrap(KEY_PROPERTIES), propNames);
    result.put(wrap(KEY_REMINDERS), remindNames);

    result.put(wrap(KEY_PERIOD), renderPeriod(appointment.getStart(), appointment.getEnd()));

    return result;
  }

  private String parseLine(String line, Map<String, String> substitutes) {
    if (BeeUtils.isEmpty(line) || substitutes.isEmpty() || !line.contains(SUBSTITUTE_PREFIX)) {
      return line;
    }

    String result = line.trim();
    for (Map.Entry<String, String> entry : substitutes.entrySet()) {
      if (entry.getValue() != null) {
        result = result.replace(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  private String parseTemplate(String template, Map<String, String> substitutes, String separator) {
    if (BeeUtils.isEmpty(template)) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    for (String line : TEMPLATE_SPLITTER.split(template)) {
      String s = parseLine(line, substitutes);
      if (!BeeUtils.isEmpty(s)) {
        if (sb.length() > 0) {
          sb.append(separator);
        }
        sb.append(s);
      }
    }
    return sb.toString();
  }

  private String renderDateTime(DateTime dateTime) {
    if (dateTime.getYear() == TimeUtils.today().getYear()) {
      if (dateTime.getHour() > 0 || dateTime.getMinute() > 0) {
        return MONTH_DAY_TIME.format(dateTime);
      } else {
        return MONTH_DAY.format(dateTime);
      }

    } else {
      if (dateTime.getHour() > 0 || dateTime.getMinute() > 0) {
        return DATE_TIME_FORMAT.format(dateTime);
      } else {
        return DATE_FORMAT.format(dateTime);
      }
    }
  }
}
