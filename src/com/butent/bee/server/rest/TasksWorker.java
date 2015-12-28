package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.modules.tasks.TasksModuleBean;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("tasks")
public class TasksWorker extends CrudWorker {

  @EJB
  TasksModuleBean task;

  @Inject
  TaskEventsWorker events;

  @GET
  @Path("{" + ID + ":\\d+}/access")
  public RestResponse access(@PathParam(ID) Long id) {
    ResponseObject response = task.accessTask(id);

    if (response.hasErrors()) {
      return RestResponse.error(ArrayUtils.joinWords(response.getErrors()));
    }
    return get(id);
  }

  @Override
  public RestResponse delete(Long id, Long version) {
    return RestResponse.forbidden();
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

    return events.get(Filter.and(Filter.equals(TaskConstants.COL_TASK + ID, id),
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
          BeeView view = sys.getView(TBL_TASKS);
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
          BeeRowSet rs = DataUtils.createRowSetForInsert(VIEW_TASKS, cols, vals);
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
  public RestResponse update(Long id, Long version, JsonObject data) {
    if (!usr.canEditData(getViewName())) {
      return RestResponse.forbidden();
    }
    JsonObject task = data.getJsonObject(COL_TASK);
    JsonObject event = data.getJsonObject(COL_EVENT);

    RestResponse error = null;

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          BeeRowSet updated = null;//update(getViewName(), id, version, data);

          if (!DataUtils.isEmpty(updated)) {
            DataInfo info = sys.getDataInfo(getViewName());
            BeeRow row = DataUtils.createEmptyRow(info.getColumnCount());

            for (String col : task.keySet()) {
              BeeColumn column = info.getColumn(col);

              if (Objects.nonNull(column)) {
                row.setValue(info.getColumnIndex(col), getValue(task, col));
              }
            }
            for (BeeColumn column : updated.getColumns()) {
              String note = BeeUtils.join(": ", Localized.getLabel(column),
                  DataUtils.render(info, row, column, info.getColumnIndex(column.getId())));
              LogUtils.getRootLogger().warning(note);
            }
          }
        }
      });
    } catch (BeeException e) {
      error = RestResponse.error(e);
    }
    RestResponse response = get(id);

    if (Objects.nonNull(error)) {
      response = error.setResult(response.getResult());
    }
    return response;
  }

  @Override
  protected String getViewName() {
    return "RestTasks";
  }
}
