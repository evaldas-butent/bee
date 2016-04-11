package com.butent.bee.client.modules.tasks;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.ProvidesGridColumnRenderer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasRowValue;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RowToLong;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeRenderer extends AbstractCellRenderer implements HasRowValue {

  public static class Provider implements ProvidesGridColumnRenderer {
    @Override
    public AbstractCellRenderer getRenderer(String columnName,
        List<? extends IsColumn> dataColumns, ColumnDescription columnDescription,
        CellSource cellSource) {

      return new TimeRenderer(dataColumns);
    }
  }

  private static final String[] LONG_COLUMNS = new String[] {COL_ACTUAL_DURATION};

  private final Map<String, RowToLong> longFunctions = new HashMap<>();

  public TimeRenderer(List<? extends IsColumn> columns) {
    super(null);
    for (String colName : LONG_COLUMNS) {
      int idx = DataUtils.getColumnIndex(colName, columns);

      if (!BeeConst.isUndef(idx)) {
        longFunctions.put(colName, RowToLong.at(idx));
      }
    }
  }

  @Override
  public boolean dependsOnSource(String source) {
    return longFunctions.containsKey(source);
  }

  public Long getActualDuration(IsRow row) {
    if (row == null) {
      return null;
    } else {
      Long milisecs = getLongNumber(COL_ACTUAL_DURATION, row);
      return milisecs;
    }
  }

  private Long getLongNumber(String key, IsRow row) {
    RowToLong function = longFunctions.get(key);
    return (function == null) ? null : function.apply(row);
  }

  @Override
  public ValueType getValueType() {
    return ValueType.LONG;
  }

  @Override
  public Integer initExport(XSheet sheet) {
    if (sheet == null) {
      return null;

    } else {
      XStyle style = XStyle.right();
      style.setFormat(Format.getDecimalPattern(2));
      style.setFontRef(sheet.registerFont(XFont.bold()));

      return sheet.registerStyle(style);
    }
  }

  @Override
  public String render(IsRow row) {
    Long v = evaluate(row);
    return (v == null) ? null : BeeUtils.toString(v, 2);
  }

  protected Long evaluate(IsRow row) {
    return getActualDuration(row);
  }

  @Override
  public Value getRowValue(IsRow row) {
    Long v = evaluate(row);
    return (v == null) ? new LongValue(0L) : new LongValue(v);
  }

}
