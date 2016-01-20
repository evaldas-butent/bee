package com.butent.bee.server.rest;

import com.google.common.collect.HashMultimap;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.JsonArray;
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

    HashMultimap<String, String> children = HashMultimap.create();
    children.putAll(TBL_FILES, Arrays.asList(COL_FILE, ALS_FILE_NAME));

    return RestResponse.ok(getData(rowSet, children)).setLastSync(time);
  }

  @Override
  protected String getViewName() {
    return "RestTaskEvents";
  }

  @Override
  public RestResponse insert(JsonObject data) {
    TaskEvent event = EnumUtils.getEnumByIndex(TaskEvent.class,
        getValue(data, TaskConstants.COL_EVENT));

    if (event == TaskEvent.COMMENT) {
      RestResponse response = super.insert(data);

      if (!response.hasError()) {
        @SuppressWarnings("unchecked")
        Long eventId = BeeUtils.toLong(BeeUtils.peek((Collection<Map<?, ?>>) response.getResult())
            .get(ID).toString());
        RestResponse resp = storeFiles(BeeUtils.toLong(getValue(data, COL_TASK_ID)), eventId,
            data.getJsonArray(TBL_FILES));

        if (Objects.nonNull(resp)) {
          if (resp.hasError()) {
            response = resp;
          } else {
            response = get(eventId);
          }
        }
      }
      return response;
    }
    return RestResponse.forbidden();
  }

  public RestResponse storeFiles(Long taskId, Long eventId, JsonArray files) {
    if (files != null) {
      try {
        commit(new Runnable() {
          @Override
          public void run() {
            for (JsonObject file : files.getValuesAs(JsonObject.class)) {
              qs.insertData(new SqlInsert(TBL_TASK_FILES)
                  .addConstant(COL_TASK, taskId)
                  .addConstant(COL_TASK_EVENT, eventId)
                  .addConstant(COL_FILE, getValue(file, COL_FILE))
                  .addConstant(COL_FILE_CAPTION, getValue(file, ALS_FILE_NAME)));
            }
          }
        });
      } catch (BeeException e) {
        return RestResponse.error(e);
      }
      return RestResponse.empty();
    }
    return null;
  }

  @Override
  public RestResponse update(Long id, Long version, JsonObject data) {
    return RestResponse.forbidden();
  }
}
