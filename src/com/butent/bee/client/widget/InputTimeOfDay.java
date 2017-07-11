package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.DateTimeFormatInfo.DateTimeFormatInfo;
import com.butent.bee.shared.time.TimeUtils;

public class InputTimeOfDay extends InputTime {

  public InputTimeOfDay() {
    super();
  }

  @Override
  public String getIdPrefix() {
    return "tod";
  }

  @Override
  protected CharMatcher getDefaultCharMatcher() {
    return CharMatcher.inRange(BeeConst.CHAR_ZERO, BeeConst.CHAR_NINE)
        .or(CharMatcher.is(TimeUtils.TIME_FIELD_SEPARATOR));
  }

  @Override
  protected int getDefaultMaxLength() {
    return 5;
  }

  @Override
  protected long getDefaultMaxMillis() {
    return TimeUtils.MILLIS_PER_DAY;
  }

  @Override
  protected String getDefaultStyleName() {
    return BeeConst.CSS_CLASS_PREFIX + "InputTimeOfDay";
  }

  @Override
  protected String getPlaceholder() {
    DateTimeFormatInfo dtfInfo = BeeKeeper.getUser().getDateTimeFormatInfo();
    return (dtfInfo == null) ? BeeConst.STRING_EMPTY : dtfInfo.timeOfDayPlaceholder();
  }
}
