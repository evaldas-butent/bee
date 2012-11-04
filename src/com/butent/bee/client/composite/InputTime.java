package com.butent.bee.client.composite;

import com.butent.bee.client.datepicker.DatePicker;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class InputTime extends InputDate {

  public InputTime(ValueType type) {
    super(type, Format.getDefaultTimeFormat());
  }

  public InputTime(ValueType type, DateTimeFormat format) {
    super(type, format);
  }

  @Override
  public HasDateValue getDate() {
    return getDateTime();
  }

  public DateTime getDateTime() {
    return new DateTime(TimeUtils.today().getDateTime().getTime()
        + TimeUtils.parseTime(getBox().getValue()));
  }

  @Override
  public String getIdPrefix() {
    return "time-box";
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_TIME;
  }

  public void setDateTime(DateTime dateTime) {
    setValue(dateTime);
  }

  @Override
  protected DatePicker createDatePicker() {
    return null;
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-TimeBox";
  }

  @Override
  protected boolean handleChar(int charCode) {
    if (charCode == '*' && !getPopup().isShowing()) {
      showPicker();
      return true;
    }

    if (Character.isDigit(BeeUtils.toChar(charCode)) || charCode == DateTime.TIME_FIELD_SEPARATOR) {
      return false;
    }

    DateTime oldDate = getDate().getDateTime();
    DateTime newDate = null;

    long millis = oldDate.getDateTime().getTime();
    int incr;

    switch (charCode) {
      case 'h':
      case 'H':
        incr = (charCode == 'h') ? -1 : 1;
        newDate = new DateTime(millis + incr * TimeUtils.MILLIS_PER_HOUR);
        break;

      case 't':
      case 'T':
      case 'l':
      case 'L':
        incr = TimeUtils.minutesSinceDayStarted(new DateTime());
        if (getStepValue() > 1) {
          incr = BeeUtils.snap(incr, getStepValue());
        } else {
          incr = BeeUtils.snap(incr, TimeUtils.MINUTES_PER_HOUR);
        }
        if (incr > 0) {
          newDate = new DateTime(millis + TimeUtils.MILLIS_PER_MINUTE * incr);
        }
        break;

      case '+':
      case '-':
        incr = (charCode == '+') ? 1 : -1;
        if (getStepValue() > 1) {
          incr *= getStepValue();
        }
        newDate = new DateTime(millis + TimeUtils.MILLIS_PER_MINUTE * incr);
        break;
    }

    if (newDate != null && TimeUtils.sameDate(oldDate, newDate)) {
      setValue(newDate);
    }
    return true;
  }

  @Override
  protected void showPicker() {
    pickTime();
  }
}
