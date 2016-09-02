package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

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
import com.butent.bee.shared.modules.projects.ProjectStatus;
import com.butent.bee.shared.modules.tasks.TaskUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

public class ProjectSlackRenderer extends AbstractSlackRenderer {

  private final List<? extends IsColumn> isColumns;

  protected ProjectSlackRenderer(List<? extends IsColumn> columns) {
    super();
    this.isColumns = columns;
  }

  @Override
  public DateTime getStartDateTime(List<? extends IsColumn> columns, IsRow row) {
    return TimeUtils.toDateTimeOrNull(row.getDate(DataUtils.getColumnIndex(COL_PROJECT_START_DATE,
        columns)));
  }

  @Override
  public DateTime getFinishDateTime(List<? extends IsColumn> columns, IsRow row) {
    return TimeUtils.toDateTimeOrNull(row.getDate(DataUtils.getColumnIndex(COL_PROJECT_END_DATE,
        columns)));
  }

  @Override
  public XCell export(IsRow row, int cellIndex, Integer styleRef, XSheet sheet) {
    ProjectStatus status =
        EnumUtils.getEnumByIndex(ProjectStatus.class, Data.getInteger(VIEW_PROJECTS, row,
            COL_PROJECT_STATUS));
    if (status == null || status == ProjectStatus.SCHEDULED) {
      return null;
    } else {
      if (row == null || sheet == null) {
        return null;
      }
      DateTime now = TimeUtils.nowMinutes();
      if (row.getInteger(Data.getColumnIndex(VIEW_PROJECTS, COL_PROJECT_STATUS)) != null) {
        int projectStatus =
            BeeUtils.unbox(row.getInteger(Data.getColumnIndex(VIEW_PROJECTS, COL_PROJECT_STATUS)));
        if (projectStatus == ProjectStatus.APPROVED.ordinal()
            || projectStatus == ProjectStatus.SUSPENDED.ordinal()) {
          if (row.getDateTime(DataUtils.getColumnIndex(COL_PROJECT_APPROVED_DATE, isColumns))
          != null) {
            now =
                row.getDateTime(DataUtils.getColumnIndex(COL_PROJECT_APPROVED_DATE,
                    isColumns));
          }
        }
      }

      DateTime start = getStartDateTime(isColumns, row);
      DateTime finish = getFinishDateTime(isColumns, row);

      TaskUtils.SlackKind kind = TaskUtils.getKind(start, finish, now);
      if (kind == null) {
        return null;
      }

      long minutes = TaskUtils.getMinutes(kind, start, finish, now);
      String text = (minutes == 0L) ? BeeConst.STRING_EMPTY : getFormatedTimeLabel(minutes);

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
    ProjectStatus status =
        EnumUtils.getEnumByIndex(ProjectStatus.class, Data.getInteger(VIEW_PROJECTS, row,
            COL_PROJECT_STATUS));
    if (status == null || status == ProjectStatus.SCHEDULED) {
      return null;
    } else {
      if (row == null) {
        return null;
      }
      DateTime now = TimeUtils.nowMinutes();
      if (row.getInteger(Data.getColumnIndex(VIEW_PROJECTS, COL_PROJECT_STATUS)) != null) {
        int projectStatus =
            BeeUtils.unbox(row.getInteger(Data.getColumnIndex(VIEW_PROJECTS, COL_PROJECT_STATUS)));
        if (projectStatus == ProjectStatus.APPROVED.ordinal()
            || projectStatus == ProjectStatus.SUSPENDED.ordinal()) {
          if (row.getDateTime(DataUtils.getColumnIndex(COL_PROJECT_APPROVED_DATE, isColumns))
          != null) {
            now =
                row.getDateTime(DataUtils.getColumnIndex(COL_PROJECT_APPROVED_DATE,
                    isColumns));
          }
        }
      }

      DateTime start = getStartDateTime(isColumns, row);
      DateTime finish = getFinishDateTime(isColumns, row);

      TaskUtils.SlackKind kind = TaskUtils.getKind(start, finish, now);
      if (kind == null) {
        return BeeConst.STRING_EMPTY;
      }

      long minutes = TaskUtils.getMinutes(kind, start, finish, now);
      if (minutes == 0L) {
        return BeeConst.STRING_EMPTY;
      }

      String label = getFormatedTimeLabel(minutes);
      return createSlackBar(kind.getStyleName(), label);
    }
  }
}
