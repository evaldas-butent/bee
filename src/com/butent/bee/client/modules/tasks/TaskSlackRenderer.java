package com.butent.bee.client.modules.tasks;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.render.AbstractSlackRenderer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

public class TaskSlackRenderer extends AbstractSlackRenderer {

  private final List<? extends IsColumn> isColumns;

  TaskSlackRenderer(List<? extends IsColumn> columns) {
    super();
    this.isColumns = columns;
  }

  @Override
  public DateTime getStartDateTime(List<? extends IsColumn> columns, IsRow row) {
    return row.getDateTime(DataUtils.getColumnIndex(TaskConstants.COL_START_TIME, columns));
  }

  @Override
  public DateTime getFinishDateTime(List<? extends IsColumn> columns, IsRow row) {
    return row.getDateTime(DataUtils.getColumnIndex(TaskConstants.COL_FINISH_TIME, columns));
  }

  @Override
  public XCell export(IsRow row, int cellIndex, Integer styleRef, XSheet sheet) {
    TaskStatus status =
        EnumUtils.getEnumByIndex(TaskStatus.class, Data.getInteger(VIEW_TASKS, row, COL_STATUS));
    if (status == null || status == TaskStatus.COMPLETED || status == TaskStatus.CANCELED
        || status == TaskStatus.APPROVED) {
      return null;
    } else {
      if (row == null || sheet == null) {
        return null;
      }
      DateTime now = TimeUtils.nowMinutes();

      DateTime start = getStartDateTime(isColumns, row);
      DateTime finish = getFinishDateTime(isColumns, row);

      SlackKind kind = getKind(start, finish, now);
      if (kind == null) {
        return null;
      }

      long minutes = getMinutes(kind, start, finish, now);
      String text = (minutes == 0L) ? BeeConst.STRING_EMPTY : getLabel(minutes);

      XStyle style = new XStyle();
      XFont font;

      switch (kind) {
        case LATE:
          style.setColor(Colors.RED);

          font = XFont.bold();
          font.setColor(Colors.WHITE);
          style.setFontRef(sheet.registerFont(font));
          break;

        case OPENING:
          style.setColor(Colors.GREEN);
          style.setTextAlign(TextAlign.CENTER);

          font = XFont.bold();
          font.setColor(Colors.WHITE);
          style.setFontRef(sheet.registerFont(font));
          break;

        case ENDGAME:
          style.setColor(Colors.ORANGE);
          style.setTextAlign(TextAlign.CENTER);

          font = XFont.bold();
          font.setColor(Colors.WHITE);
          style.setFontRef(sheet.registerFont(font));
          break;

        case SCHEDULED:
          style.setColor(Colors.YELLOW);
          style.setTextAlign(TextAlign.RIGHT);
          break;
      }

      return new XCell(cellIndex, text, sheet.registerStyle(style));
    }
  }

  @Override
  public String render(IsRow row) {
    TaskStatus status =
        EnumUtils.getEnumByIndex(TaskStatus.class, Data.getInteger(VIEW_TASKS, row, COL_STATUS));
    if (status == null || status == TaskStatus.COMPLETED || status == TaskStatus.CANCELED
        || status == TaskStatus.APPROVED) {
      return null;
    } else {
      if (row == null) {
        return null;
      }
      DateTime now = TimeUtils.nowMinutes();

      DateTime start = getStartDateTime(isColumns, row);
      DateTime finish = getFinishDateTime(isColumns, row);

      SlackKind kind = getKind(start, finish, now);
      if (kind == null) {
        return BeeConst.STRING_EMPTY;
      }

      long minutes = getMinutes(kind, start, finish, now);
      if (minutes == 0L) {
        return BeeConst.STRING_EMPTY;
      }

      String label = getLabel(minutes);
      return format(kind, label);
    }
  }

}
