package com.butent.bee.client.render;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public abstract class AbstractSlackRenderer extends AbstractCellRenderer {

  private static final String STYLE_BAR = TaskConstants.STYLE_SLACK_PREFIX + "bar";
  private static final String STYLE_LABEl = TaskConstants.STYLE_SLACK_PREFIX + "label";

  protected static String createSlackBar(String styleName, String label) {
    Div bar = new Div().addClass(STYLE_BAR).addClass(styleName);
    if (!BeeUtils.isEmpty(label)) {
      bar.appendChild(new Div().addClass(STYLE_LABEl).text(label));
    }

    return bar.toString();
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
