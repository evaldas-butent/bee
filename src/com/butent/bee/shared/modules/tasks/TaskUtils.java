package com.butent.bee.shared.modules.tasks;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.CellKind;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.ScheduleDateMode;
import com.butent.bee.shared.time.ScheduleDateRange;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class TaskUtils {

  private static final String NOTE_LABEL_SEPARATOR = ": ";

  public static boolean canConfirmTasks(final DataInfo info, final List<BeeRow> rows,
      long userId, ResponseObject resp) {
    Assert.notNull(info);
    Assert.notNull(rows);

    for (IsRow row : rows) {
      if (BeeUtils.unbox(row.getLong(info.getColumnIndex(COL_OWNER))) != userId) {
        if (resp != null) {
          resp.addWarning(
              BeeUtils.joinWords(Localized.dictionary().crmTask(), row.getId()),
              Localized.dictionary().crmTaskConfirmCanManager());
        }
        return false;
      }

      if (BeeUtils.unbox(row.getInteger(info.getColumnIndex(COL_STATUS)))
          != TaskStatus.COMPLETED.ordinal()) {
        if (resp != null) {
          resp.addWarning(
              BeeUtils.joinWords(Localized.dictionary().crmTask(), row.getId()),
              Localized.dictionary().crmTaskMustBePerformed());
        }
        return false;
      }
    }

    if (resp != null) {
      resp.clearMessages();
      resp.setResponse(null);
    }
    return true;
  }

  public static String getDeleteNote(String label, String value) {
    return BeeUtils.join(NOTE_LABEL_SEPARATOR, label,
        BeeUtils.joinWords(Localized.dictionary().crmDeleted().toLowerCase(), value));
  }

  public static List<FileInfo> getFiles(IsRow row) {
    if (BeeUtils.isEmpty(row.getProperty(PROP_FILES))) {
      return Lists.newArrayList();
    }

    return FileInfo.restoreCollection(row.getProperty(PROP_FILES));
  }

  public static String getInsertNote(String label, String value) {
    return BeeUtils.join(NOTE_LABEL_SEPARATOR, label, BeeUtils
        .joinWords(Localized.dictionary().crmAdded().toLowerCase(), value));
  }

  public static List<ScheduleDateRange> getScheduleDateRanges(BeeRowSet rowSet) {
    List<ScheduleDateRange> result = new ArrayList<>();
    if (DataUtils.isEmpty(rowSet)) {
      return result;
    }

    int fromIndex = rowSet.getColumnIndex(COL_RTD_FROM);
    int untilIndex = rowSet.getColumnIndex(COL_RTD_UNTIL);
    int modeIndex = rowSet.getColumnIndex(COL_RTD_MODE);

    for (BeeRow row : rowSet.getRows()) {
      JustDate from = row.getDate(fromIndex);
      JustDate until = row.getDate(untilIndex);

      ScheduleDateMode mode =
          EnumUtils.getEnumByIndex(ScheduleDateMode.class, row.getInteger(modeIndex));

      ScheduleDateRange sdr = ScheduleDateRange.maybeCreate(from, until, mode);
      if (sdr != null) {
        result.add(sdr);
      }
    }

    return result;
  }

  public static Multimap<Long, ScheduleDateRange> getScheduleDateRangesByTask(BeeRowSet rowSet) {
    Multimap<Long, ScheduleDateRange> result = ArrayListMultimap.create();
    if (DataUtils.isEmpty(rowSet)) {
      return result;
    }

    int rtIndex = rowSet.getColumnIndex(COL_RECURRING_TASK);

    int fromIndex = rowSet.getColumnIndex(COL_RTD_FROM);
    int untilIndex = rowSet.getColumnIndex(COL_RTD_UNTIL);
    int modeIndex = rowSet.getColumnIndex(COL_RTD_MODE);

    for (BeeRow row : rowSet.getRows()) {
      Long rt = row.getLong(rtIndex);

      JustDate from = row.getDate(fromIndex);
      JustDate until = row.getDate(untilIndex);

      ScheduleDateMode mode =
          EnumUtils.getEnumByIndex(ScheduleDateMode.class, row.getInteger(modeIndex));

      ScheduleDateRange sdr = ScheduleDateRange.maybeCreate(from, until, mode);
      if (DataUtils.isId(rt) && sdr != null) {
        result.put(rt, sdr);
      }
    }

    return result;
  }

  public static Filter getTaskPrivacyFilter(long userId) {
    return Filter.or(Filter.isNull(COL_PRIVATE_TASK),
        Filter.equals(COL_OWNER, userId), Filter.equals(COL_EXECUTOR, userId),
        Filter.in(COL_TASK_ID, VIEW_TASK_USERS, COL_TASK,
            Filter.equals(AdministrationConstants.COL_USER, userId)));
  }

  public static List<Long> getTaskUsers(IsRow row, List<BeeColumn> columns) {
    List<Long> users = new ArrayList<>();

    Long owner = row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns));
    if (owner != null) {
      users.add(owner);
    }

    Long executor = row.getLong(DataUtils.getColumnIndex(COL_EXECUTOR, columns));
    if (executor != null && !users.contains(executor)) {
      users.add(executor);
    }

    List<Long> observers = DataUtils.parseIdList(row.getProperty(PROP_OBSERVERS));
    for (Long observer : observers) {
      if (!users.contains(observer)) {
        users.add(observer);
      }
    }
    return users;
  }

  public static String getUpdateNote(String label, String oldValue, String newValue) {
    return BeeUtils.join(NOTE_LABEL_SEPARATOR, label, BeeUtils.join(" -> ", oldValue, newValue));
  }

  public static List<String> getUpdateNotes(DataInfo dataInfo, IsRow oldRow, IsRow newRow,
      Function<HasDateValue, String> dateRenderer,
      Function<HasDateValue, String> dateTimeRenderer) {

    List<String> notes = new ArrayList<>();

    if (dataInfo == null || oldRow == null || newRow == null) {
      return notes;
    }
    List<BeeColumn> columns = dataInfo.getColumns();
    for (int i = 0; i < columns.size(); i++) {
      BeeColumn column = columns.get(i);

      String oldValue = oldRow.getString(i);
      String newValue = newRow.getString(i);

      if (!BeeUtils.equalsTrimRight(oldValue, newValue) && column.isEditable()) {
        String label = Localized.getLabel(column);
        String note;

        if (BeeUtils.isEmpty(oldValue)) {
          if (Objects.equals(oldRow.getBoolean(i), Boolean.TRUE)) {
            note = getInsertNote(label, "");
          } else {
            note = getInsertNote(label,
                renderColumn(dataInfo, oldRow, column, i, dateRenderer, dateTimeRenderer));
          }

        } else if (BeeUtils.isEmpty(newValue)) {
          if (Objects.equals(oldRow.getBoolean(i), Boolean.TRUE)) {
            note = getDeleteNote(label, "");
          } else {
            note = getDeleteNote(label,
                renderColumn(dataInfo, oldRow, column, i, dateRenderer, dateTimeRenderer));
          }

        } else {
          note = getUpdateNote(label,
              renderColumn(dataInfo, oldRow, column, i, dateRenderer, dateTimeRenderer),
              renderColumn(dataInfo, newRow, column, i, dateRenderer, dateTimeRenderer));
        }
        notes.add(note);
      }
    }
    return notes;
  }

  @Deprecated
  public static boolean isScheduled(DateTime start) {
    return start != null && TimeUtils.dayDiff(TimeUtils.today(), start) > 0;
  }

  public static String renderColumn(DataInfo dataInfo, IsRow row, BeeColumn column, int index,
      Function<HasDateValue, String> dateRenderer,
      Function<HasDateValue, String> dateTimeRenderer) {

    if (COL_TASK_TYPE.equals(column.getId())) {
      int nameIndex = dataInfo.getColumnIndex(ALS_TASK_TYPE_NAME);

      if (!BeeConst.isUndef(nameIndex)) {
        return row.getString(nameIndex);
      }
    }

    return DataUtils.render(dataInfo, row, column, index, dateRenderer, dateTimeRenderer);
  }

  public static void renderEndResult(Map<String, MultiSelector> relations, FormView formView,
      boolean isOwner, Runnable runnable) {

    HtmlTable table = new HtmlTable();
    table.setColumnCellKind(0, CellKind.LABEL);
    table.setWidth("100%");

    String view = formView.getViewName();
    IsRow row = formView.getActiveRow();

    Map<String, CheckBox> cbMap = new HashMap<>();
    List<String> savedEndResult = Codec.deserializeList(Data.getString(view, row, COL_END_RESULT));

    relations.put(VIEW_TASK_FILES, null);

    int i = 0;
    for (String relation : relations.keySet()) {
      String caption = Data.getViewCaption(relation);

      if (!BeeUtils.isEmpty(caption)) {
        CheckBox cb = new CheckBox();
        cb.setEnabled(isOwner);

        if (savedEndResult.contains(relation)) {
          cb.setChecked(true);
        }

        table.setWidget(i, 0, cb);
        cbMap.put(relation, cb);

        table.setText(i, 1, caption);
        i++;
      }
    }

    Global.inputWidget(Localized.dictionary().crmTaskEndResult(), table, () -> {
      List<String> result = new ArrayList<>();
      List<String> translations = new ArrayList<>();

      for (String viewName : cbMap.keySet()) {
        if (cbMap.get(viewName).isChecked()) {
          result.add(viewName);
          translations.add(Data.getViewCaption(viewName));
        }
      }

      if (savedEndResult.equals(result)) {
        return;
      }

      if (DataUtils.isId(row.getId()) && BeeUtils.same(view, VIEW_TASKS)) {
        int idx = Data.getColumnIndex(view, COL_END_RESULT);

        if (!BeeConst.isUndef(idx)) {
          Queries.update(view, row.getId(), COL_END_RESULT, new TextValue(result.isEmpty()
                  ? BeeConst.STRING_EMPTY : Codec.beeSerialize(result)), (Integer count) -> {
                if (BeeUtils.isPositive(count)) {
                  insertEndResultNote(Collections.singletonList(row.getId()), savedEndResult,
                      translations, runnable);
                }
              });
        }
      } else {
        Data.setValue(view, row, COL_END_RESULT, Codec.beeSerialize(result));
      }
    });
  }

  public static void insertEndResultNote(List<Long> ids, List<String> savedEndResult,
      List<String> endResult, Runnable runnable) {

    String message;

    List<String> savedTranslations = new ArrayList<>();

    if (savedEndResult != null) {
      savedEndResult.forEach(viewName -> savedTranslations.add(Data.getViewCaption(viewName)));
    }

    if (!savedTranslations.isEmpty() && !endResult.isEmpty()) {
      message = TaskUtils.getUpdateNote(Localized.dictionary().crmTaskEndResult(),
          BeeUtils.join(", ", savedTranslations), BeeUtils.join(", ", endResult));
    } else if (savedTranslations.isEmpty() && !endResult.isEmpty()) {
      message = TaskUtils.getInsertNote(Localized.dictionary().crmTaskEndResult(),
          BeeUtils.join(", ", endResult));
    } else {
      message = TaskUtils.getDeleteNote(Localized.dictionary().crmTaskEndResult(), null);
    }

    BeeRowSet rowSet = new BeeRowSet(TBL_TASK_EVENTS, Data.getColumns(TBL_TASK_EVENTS,
        Arrays.asList(COL_TASK, COL_PUBLISHER, COL_PUBLISH_TIME, COL_EVENT_NOTE,
            COL_EVENT)));

    for (Long id : ids) {
      BeeRow r = rowSet.addEmptyRow();
      r.setValue(rowSet.getColumnIndex(COL_TASK), id);
      r.setValue(rowSet.getColumnIndex(COL_PUBLISHER),
          BeeKeeper.getUser().getUserId());
      r.setValue(rowSet.getColumnIndex(COL_PUBLISH_TIME), TimeUtils.nowMillis());
      r.setValue(rowSet.getColumnIndex(COL_EVENT_NOTE), message);
      r.setValue(rowSet.getColumnIndex(COL_EVENT), TaskEvent.EDIT);
    }

    Queries.insertRows(rowSet, result -> {
      if (runnable != null) {
        runnable.run();
      }
    });
  }

  public static boolean sameObservers(IsRow oldRow, IsRow newRow) {
    if (oldRow == null || newRow == null) {
      return false;
    } else {
      return DataUtils.sameIdSet(oldRow.getProperty(PROP_OBSERVERS),
          newRow.getProperty(PROP_OBSERVERS));
    }
  }

  // Verslo Aljansas TID 25505
  public static void vaSummarizeMileage(Table<String, String, Double> mileage, IsRow row,
                                        List<BeeColumn> columns) {
    Assert.notNull(mileage);

    String publisher = BeeUtils.joinWords(
      DataUtils.getString(columns, row, ALS_PUBLISHER_FIRST_NAME),
      DataUtils.getString(columns, row, ALS_PUBLISHER_LAST_NAME)
    );
    String durType = DataUtils.getString(columns, row, COL_DURATION_TYPE);

    if (BeeUtils.isEmpty(publisher) || BeeUtils.isEmpty(durType)) {
      return;
    }

    Double m = BeeUtils.nvl(DataUtils.getDouble(columns, row, COL_VA_MILEAGE), 0D);
    mileage.put(publisher, durType, m + BeeUtils.nvl(mileage.get(publisher, durType), 0D));
  }

  private TaskUtils() {
  }
}
