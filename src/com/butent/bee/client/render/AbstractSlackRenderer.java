package com.butent.bee.client.render;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public abstract class AbstractSlackRenderer extends AbstractCellRenderer {

  public enum SlackKind {
    LATE, OPENING, ENDGAME, SCHEDULED;

    public String getStyleName() {
      return STYLE_PREFIX + name().toLowerCase();
    }
  }

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "misc-Slack-";

  private static final String STYLE_BAR = STYLE_PREFIX + "bar";
  private static final String STYLE_LABEl = STYLE_PREFIX + "label";

  protected static String createSlackBar(SlackKind kind, String label) {
    Div bar = new Div().addClass(STYLE_BAR).addClass(kind.getStyleName());
    if (!BeeUtils.isEmpty(label)) {
      bar.appendChild(new Div().addClass(STYLE_LABEl).text(label));
    }

    return bar.toString();
  }

  public static SlackKind getKind(DateTime start, DateTime finish, DateTime now) {

    if (finish != null && TimeUtils.isLess(finish, now)) {
      return SlackKind.LATE;
    } else if (start != null && TimeUtils.isMore(finish, start) && TimeUtils.isMeq(now, start)
        && TimeUtils.isLess(now, finish)) {
      if (now.getTime() - start.getTime() < (finish.getTime() - start.getTime()) / 2) {
        return SlackKind.OPENING;
      } else {
        return SlackKind.ENDGAME;
      }

    } else {
      return null;
    }
  }

  public static String getFormatedTimeLabel(long minutes) {
    if (minutes <= 0L) {
      return BeeConst.STRING_ZERO;

    } else if (minutes < TimeUtils.MINUTES_PER_DAY) {
      return TimeUtils.renderMinutes(BeeUtils.toInt(minutes), false);

    } else {
      return BeeUtils.toString(minutes / TimeUtils.MINUTES_PER_DAY);
    }
  }

  public static long getMinutes(SlackKind kind, DateTime start, DateTime finish, DateTime now) {

    switch (kind) {
      case LATE:
        return (now.getTime() - finish.getTime()) / TimeUtils.MILLIS_PER_MINUTE;

      case OPENING:
      case ENDGAME:
        return (finish.getTime() - now.getTime()) / TimeUtils.MILLIS_PER_MINUTE;

      case SCHEDULED:
        return TimeUtils.dayDiff(now, start) * TimeUtils.MINUTES_PER_DAY;

      default:
        Assert.untouchable();
        return 0L;
    }
  }

  protected AbstractSlackRenderer() {
    super(null);
  }

  /**
   * This method must return start DateTime which will be used in
   * {@code export(IsRow, int, Integer, XSheet}, {@code render(IsRow)} and
   * {@code getKind(DateTime, DateTime)} methods.
   * 
   * If null is returned then the calculations won't work.
   * 
   * @param columns are IsColumn columns.
   * @param row is provided current IsRow.
   * @return start DateTime which will be used for calculations.
   */
  public abstract DateTime getStartDateTime(List<? extends IsColumn> columns, IsRow row);

  /**
   * This method must return end DateTime which will be used in
   * {@code export(IsRow, int, Integer, XSheet}, {@code render(IsRow)} and
   * {@code getKind(DateTime, DateTime)} methods.
   * 
   * If null is returned then the calculations won't work.
   * 
   * @param columns are IsColumn columns.
   * @param row is provided current IsRow.
   * @return end DateTime which will be used for calculations.
   */
  public abstract DateTime getFinishDateTime(List<? extends IsColumn> columns, IsRow row);

}
