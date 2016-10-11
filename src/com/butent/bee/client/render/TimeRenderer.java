package com.butent.bee.client.render;

import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasValueFormatter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.function.Function;

public class TimeRenderer extends AbstractCellRenderer implements HasValueFormatter {

  private final String dayLabel;

  private final boolean showSeconds;
  private final boolean showMillis;

  public TimeRenderer(CellSource cellSource, String options) {
    super(cellSource);

    if (BeeUtils.isEmpty(options)) {
      this.dayLabel = null;

      this.showSeconds = false;
      this.showMillis = false;

    } else {
      this.dayLabel = BeeUtils.containsSame(options, "d")
          ? Localized.dictionary().dayShort() : null;

      this.showSeconds = BeeUtils.containsSame(options, "s");
      this.showMillis = BeeUtils.containsSame(options, "ms");
    }
  }

  @Override
  public Function<Value, String> getValueFormatter() {
    return value -> (value == null) ? null : format(value.getLong());
  }

  @Override
  public String render(IsRow row) {
    return format(getLong(row));
  }

  private String format(Long time) {
    if (BeeUtils.isPositive(time)) {
      return TimeUtils.renderTime(time, dayLabel, showSeconds, showMillis);
    } else {
      return null;
    }
  }
}
