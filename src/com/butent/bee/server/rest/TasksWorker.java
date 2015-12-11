package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;

import java.util.EnumSet;

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
}
