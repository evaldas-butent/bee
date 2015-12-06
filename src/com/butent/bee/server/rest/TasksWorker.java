package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;
import java.util.Objects;

import javax.json.JsonObject;
import javax.ws.rs.Path;

@Path("tasks")
public class TasksWorker extends CrudWorker {

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

  @Override
  protected String getViewName() {
    return "RestTasks";
  }

  @Override
  public RestResponse update(Long id, Long version, JsonObject data) {
    if (!usr.canEditData(getViewName())) {
      return RestResponse.forbidden();
    }
    RestResponse error = null;

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          BeeRowSet updated = update(getViewName(), id, version, data);

          if (!DataUtils.isEmpty(updated)) {
            DataInfo info = sys.getDataInfo(getViewName());
            BeeRow row = DataUtils.createEmptyRow(info.getColumnCount());

            for (String col : data.keySet()) {
              BeeColumn column = info.getColumn(col);

              if (Objects.nonNull(column)) {
                row.setValue(info.getColumnIndex(col), getValue(data, col));
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
}
