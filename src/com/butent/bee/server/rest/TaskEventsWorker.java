package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskEvent;
import com.butent.bee.shared.utils.EnumUtils;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.JsonObject;
import javax.ws.rs.Path;

@Path("taskevents")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class TaskEventsWorker extends CrudWorker {

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
  protected String getViewName() {
    return "RestTaskEvents";
  }

  @Override
  public RestResponse insert(JsonObject data) {
    TaskEvent event = EnumUtils.getEnumByIndex(TaskEvent.class, getValue(data, COL_EVENT));

    if (event == TaskEvent.COMMENT) {
      return super.insert(data);
    }
    return RestResponse.forbidden();
  }

  @Override
  public RestResponse update(Long id, Long version, JsonObject data) {
    return RestResponse.forbidden();
  }
}
