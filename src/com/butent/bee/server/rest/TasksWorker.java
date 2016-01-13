package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.COL_EVENT;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.modules.tasks.TasksModuleBean;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.tasks.TaskUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("tasks")
public class TasksWorker extends CrudWorker {

  @EJB
  TasksModuleBean task;
  @EJB
  TaskEventsWorker events;

  @GET
  @Path("{" + ID + ":\\d+}/access")
  public RestResponse access(@PathParam(ID) Long id) {
    ResponseObject response = task.accessTask(id);

    if (response.hasErrors()) {
      return RestResponse.error(ArrayUtils.joinWords(response.getErrors()));
    }
    SimpleRowSet.SimpleRow row = qs.getRow(new SqlSelect()
        .addField(TBL_TASKS, sys.getVersionName(TBL_TASKS), VERSION)
        .addFields(TBL_TASKS, COL_STATUS, COL_START_TIME, COL_EXECUTOR)
        .addFrom(TBL_TASKS)
        .setWhere(sys.idEquals(TBL_TASKS, id)));

    TaskStatus oldStatus = EnumUtils.getEnumByIndex(TaskStatus.class, row.getInt(COL_STATUS));

    if (Objects.equals(row.getLong(COL_EXECUTOR), usr.getCurrentUserId())
        && (TaskStatus.NOT_VISITED == oldStatus || TaskStatus.SCHEDULED == oldStatus
        && !TaskUtils.isScheduled(row.getDateTime(COL_START_TIME)))) {

      return update(id, row.getLong(VERSION), Json.createObjectBuilder()
          .add(COL_STATUS, TaskStatus.ACTIVE.ordinal())
          .add(OLD_VALUES, Json.createObjectBuilder().add(COL_STATUS, oldStatus.ordinal()))
          .add(COL_TASK_EVENT,
              Json.createObjectBuilder().add(COL_EVENT, TaskEvent.VISIT.ordinal())).build());
    }
    return get(id);
  }

  @Override
  public RestResponse delete(Long id, Long version) {
    return RestResponse.forbidden();
  }

  @Override
  public RestResponse get(Filter filter) {
    long time = System.currentTimeMillis();
    BeeRowSet rowSet = qs.getViewData(getViewName(), filter);
    return RestResponse.ok(getData(rowSet, TBL_FILES, COL_FILE, ALS_FILE_NAME)).setLastSync(time);
  }

  @Override
  public RestResponse getAll(Long lastSynced) {
    CompoundFilter filter = Filter.or();
    Long userId = usr.getCurrentUserId();

    filter.add(Filter.and(Filter.equals(COL_EXECUTOR, userId),
        Filter.any(COL_STATUS, EnumSet.of(TaskStatus.NOT_VISITED, TaskStatus.ACTIVE))));

    filter.add(Filter.and(Filter.equals(COL_OWNER, userId), Filter.isNot(Filter.any(COL_STATUS,
        EnumSet.of(TaskStatus.APPROVED, TaskStatus.CANCELED)))));

    filter.add(Filter.isNull(COL_LAST_ACCESS))
        .add(Filter.compareWithColumn(COL_PUBLISH_TIME, Operator.GT, COL_LAST_ACCESS));

    return get(filter);
  }

  @GET
  @Path("{" + ID + ":\\d+}/events")
  public RestResponse getEvents(@PathParam(ID) Long id,
      @HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {

    return events.get(Filter.and(Filter.equals(COL_TASK + ID, id),
        Filter.compareVersion(Operator.GT, BeeUtils.unbox(lastSynced))));
  }

  @Override
  public RestResponse insert(JsonObject data) {
    Holder<Long> idHolder = Holder.of(BeeUtils.toLongOrNull(getValue(data, ID)));

    if (DataUtils.isId(idHolder.get()) || !usr.canCreateData(getViewName())) {
      return RestResponse.forbidden();
    }
    RestResponse response = null;

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          BeeView view = sys.getView(getViewName());
          List<BeeColumn> cols = new ArrayList<>();
          List<String> vals = new ArrayList<>();

          for (String col : data.keySet()) {
            if (view.hasColumn(col)) {
              BeeColumn column = view.getBeeColumn(col);

              if (!column.isForeign()) {
                String value = getValue(data, col);

                if (!BeeUtils.isEmpty(value)) {
                  cols.add(column);
                  vals.add(value);
                }
              }
            }
          }
          BeeRowSet rs = DataUtils.createRowSetForInsert(view.getName(), cols, vals);
          rs.setRowProperty(0, PROP_EXECUTORS, getValue(data, COL_EXECUTOR));

          Map<String, String> params = new HashMap<>();
          params.put(VAR_TASK_DATA, Codec.beeSerialize(rs));

          ResponseObject resp = task.doTaskEvent(TaskEvent.CREATE.name(), params);

          if (resp.hasErrors()) {
            throw new BeeRuntimeException(ArrayUtils.joinWords(resp.getErrors()));
          }
          idHolder.set(BeeUtils.peek(((BeeRowSet) resp.getResponse()).getRowIds()));
        }
      });
    } catch (BeeException e) {
      response = RestResponse.error(e);
      e.printStackTrace();
    }

    if (Objects.isNull(response)) {
      response = get(idHolder.get());
    }
    return response;
  }

  @Override
  public RestResponse update(Long id, Long version, JsonObject taskData) {
    LogUtils.getRootLogger().info(taskData);

    if (!usr.canEditData(getViewName())) {
      return RestResponse.forbidden();
    }
    Holder<RestResponse> error = Holder.absent();

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          DataInfo info = sys.getDataInfo(getViewName());
          List<BeeColumn> columns = info.getColumns();
          BeeRow row = DataUtils.createEmptyRow(columns.size());
          row.setId(id);
          row.setVersion(version);
          BeeRow oldRow = DataUtils.cloneRow(row);
          BeeRowSet updated = null;

          JsonObject oldTask = taskData.getJsonObject(OLD_VALUES);
          JsonObject taskEvent = taskData.getJsonObject(COL_TASK_EVENT);

          if (Objects.nonNull(oldTask) && Objects.nonNull(taskEvent)) {
            for (String col : oldTask.keySet()) {
              int idx = DataUtils.getColumnIndex(col, columns);

              if (!BeeConst.isUndef(idx)) {
                row.setValue(idx, getValue(taskData, col));
                oldRow.setValue(idx, getValue(oldTask, col));
              }
            }
            updated = DataUtils.getUpdated(info.getViewName(), columns, oldRow, row, null);
          }
          if (!DataUtils.isEmpty(updated)) {
            Map<String, String> params = new HashMap<>();
            params.put(VAR_TASK_DATA, Codec.beeSerialize(updated));

            List<String> notes = TaskUtils.getUpdateNotes(info, oldRow, row);

            if (!notes.isEmpty()) {
              params.put(VAR_TASK_NOTES, Codec.beeSerialize(notes));
            }
            String comment = getValue(taskEvent, COL_COMMENT);

            if (!BeeUtils.isEmpty(comment)) {
              params.put(VAR_TASK_COMMENT, comment);
            }
            String time = getValue(taskEvent, COL_DURATION);

            if (!BeeUtils.isEmpty(time)) {
              params.put(VAR_TASK_DURATION_DATE, getValue(taskEvent, COL_DURATION_DATE));
              params.put(VAR_TASK_DURATION_TYPE, getValue(taskEvent, COL_DURATION_TYPE + ID));
              params.put(VAR_TASK_DURATION_TIME, time);
            }
            TaskEvent event = EnumUtils.getEnumByIndex(TaskEvent.class,
                getValue(taskEvent, COL_EVENT));

            if (event == TaskEvent.VISIT) {
              params.put(VAR_TASK_VISITED, "1");
            }
            ResponseObject resp = task.doTaskEvent(event.name(), params);

            if (resp.hasErrors()) {
              throw new BeeRuntimeException(ArrayUtils.joinWords(resp.getErrors()));
            }
            if (updated.containsColumn(COL_EXECUTOR)) {
              Long executor = updated.getLong(0, COL_EXECUTOR);

              SimpleRowSet.SimpleRow taskRow = qs.getRow(new SqlSelect()
                  .addFields(TBL_TASKS, COL_OWNER)
                  .addField(TBL_TASK_USERS, sys.getIdName(TBL_TASK_USERS), ID)
                  .addFrom(TBL_TASKS)
                  .addFromLeft(TBL_TASK_USERS,
                      SqlUtils.and(sys.joinTables(TBL_TASKS, TBL_TASK_USERS, COL_TASK),
                          SqlUtils.equals(TBL_TASK_USERS, AdministrationConstants.COL_USER,
                              executor)))
                  .setWhere(sys.idEquals(TBL_TASKS, id)));

              Long userId = taskRow.getLong(ID);

              if (Objects.isNull(userId)) {
                qs.insertData(new SqlInsert(TBL_TASK_USERS)
                    .addConstant(COL_TASK, id)
                    .addConstant(AdministrationConstants.COL_USER, executor));

              } else if (!Objects.equals(taskRow.getLong(COL_OWNER), executor)) {
                qs.updateData(new SqlUpdate(TBL_TASK_USERS)
                    .addConstant(COL_LAST_ACCESS, null)
                    .setWhere(sys.idEquals(TBL_TASK_USERS, userId)));
              }
            }
          } else {
            error.set(RestResponse.error(Localized.getConstants().noData()));
          }
        }
      });
    } catch (BeeException e) {
      error.set(RestResponse.error(e));
    }
    RestResponse response = get(id);

    if (error.isNotNull()) {
      response = error.get().setResult(response.getResult());
    }
    return response;
  }

  @Override
  protected String getViewName() {
    return "RestTasks";
  }
}
