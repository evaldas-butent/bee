package com.butent.bee.server.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CrmModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(CrmModuleBean.class);

  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @Resource
  EJBContext ctx;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CRM_METHOD);

    if (BeeUtils.isPrefix(svc, CRM_TASK_PREFIX)) {
      response = doTaskEvent(BeeUtils.removePrefix(svc, CRM_TASK_PREFIX), reqInfo);

    } else {
      String msg = BeeUtils.joinWords("CRM service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public String getName() {
    return CRM_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
  }

  private ResponseObject createTaskRelations(long taskId, Map<String, String> properties) {
    int count = 0;
    if (BeeUtils.isEmpty(properties)) {
      return ResponseObject.response(count);
    }

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String tableName = translateTaskPropertyNameToTableName(entry.getKey());
      if (BeeUtils.isEmpty(tableName)) {
        continue;
      }

      List<Long> idList = DataUtils.parseIdList(entry.getValue());
      if (idList.isEmpty()) {
        continue;
      }

      for (long relId : idList) {
        ResponseObject ro = qs.insertDataWithResponse(new SqlInsert(CommonsConstants.TBL_RELATIONS)
            .addConstant(CommonsConstants.COL_TABLE_1, TBL_TASKS)
            .addConstant(CommonsConstants.COL_ROW_1, taskId)
            .addConstant(CommonsConstants.COL_TABLE_2, tableName)
            .addConstant(CommonsConstants.COL_ROW_2, relId));
        if (ro.hasErrors()) {
          return ro;
        }

        count++;
      }
    }
    return ResponseObject.response(count);
  }

  private ResponseObject doTaskEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    TaskEvent event = NameUtils.getEnumByName(TaskEvent.class, svc);
    if (event == null) {
      String msg = BeeUtils.joinWords("Task service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
      return response;
    }

    long time = System.currentTimeMillis();
    long currentUser = usr.getCurrentUserId();

    long taskId;

    switch (event) {
      case ACTIVATED:
        BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(VAR_TASK_DATA));
        BeeRow row = rs.getRow(0);

        Map<String, String> properties = row.getProperties();

        String executors = properties.get(PROP_EXECUTORS);
        String observers = properties.get(PROP_OBSERVERS);

        int exIndex = DataUtils.getColumnIndex(COL_EXECUTOR, rs.getColumns());
        int cnt = 0;

        for (long executor : DataUtils.parseIdList(executors)) {
          rs.clearRows();
          rs.addRow(DataUtils.cloneRow(row));
          rs.getRow(0).setValue(exIndex, executor);

          response = deb.commitRow(rs, false);

          if (!response.hasErrors()) {
            cnt++;
            taskId = ((BeeRow) response.getResponse()).getId();
            response = registerTaskEvent(taskId, time, reqInfo, event, null);

            if (!response.hasErrors() && executor != currentUser) {
              response = registerTaskVisit(taskId, executor, null);
            }

            if (!response.hasErrors() && !BeeUtils.isEmpty(observers)) {
              for (long obsId : DataUtils.parseIdList(observers)) {
                if (obsId != currentUser && obsId != executor) {
                  response = registerTaskVisit(taskId, obsId, null);
                  if (response.hasErrors()) {
                    break;
                  }
                }
              }
            }

            if (!response.hasErrors()) {
              response = createTaskRelations(taskId, properties);
            }
          }

          if (response.hasErrors()) {
            break;
          }
        }

        if (!response.hasErrors()) {
          response = ResponseObject.response(cnt);
        }
        break;

      case VISITED:
        taskId = getTaskId(reqInfo);
        response = registerTaskVisit(taskId, currentUser, time);

        if (!response.hasErrors()) {
          Map<String, String> taskProperties = Maps.newHashMap();
          
          String taskUsers = getTaskUsers(taskId, 
              DataUtils.parseIdSet(reqInfo.getParameter(VAR_TASK_DATA)));
          if (!BeeUtils.isEmpty(taskUsers)) {
            taskProperties.put(PROP_OBSERVERS, taskUsers);
          }
          
          Map<String, List<Long>> taskRelations = getTaskRelations(taskId);
          for (Map.Entry<String, List<Long>> entry : taskRelations.entrySet()) {
            taskProperties.put(entry.getKey(), DataUtils.buildIdList(entry.getValue()));
          }
          
          response = ResponseObject.response(taskProperties);
        }
        break;

      case COMMENTED:
        response = registerTaskEvent(getTaskId(reqInfo), time, reqInfo, event, null);
        break;

      case FORWARDED:
        rs = BeeRowSet.restore(reqInfo.getParameter(VAR_TASK_DATA));
        taskId = rs.getRow(0).getId();
        long oldUser = BeeUtils.toLong(rs.getShadowString(0, COL_EXECUTOR));
        long newUser = BeeUtils.toLong(rs.getString(0, COL_EXECUTOR));
        IsCondition wh = SqlUtils.equal(TBL_TASK_USERS, COL_TASK, taskId);

        response = deb.commitRow(rs, false);

        if (!response.hasErrors()
            && !qs.sqlExists(TBL_TASK_USERS, SqlUtils.and(wh,
                SqlUtils.equal(TBL_TASK_USERS, COL_USER, newUser)))) {
          response = registerTaskVisit(taskId, newUser, null);
        }
        if (!response.hasErrors()) {
          response = registerTaskEvent(taskId, time, reqInfo, event,
              BeeUtils.join(" -> ", usr.getUserSign(oldUser), usr.getUserSign(newUser)));

          if (!response.hasErrors()
              && !BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_OBSERVE))) {
            qs.updateData(new SqlDelete(TBL_TASK_USERS)
                .setWhere(SqlUtils.and(wh, SqlUtils.equal(TBL_TASK_USERS, COL_USER, oldUser))));
          }
        }
        break;

      case EXTENDED:
        rs = BeeRowSet.restore(reqInfo.getParameter(VAR_TASK_DATA));
        taskId = rs.getRow(0).getId();
        String oldTerm = rs.getShadowString(0, "FinishTime");
        String newTerm = rs.getString(0, "FinishTime");

        response = deb.commitRow(rs, false);

        if (!response.hasErrors()) {
          response = registerTaskEvent(taskId, time, reqInfo, event,
              BeeUtils.join(" -> ",
                  TimeUtils.toDateTimeOrNull(oldTerm), TimeUtils.toDateTimeOrNull(newTerm)));
        }
        break;

      case EDITED:
        rs = BeeRowSet.restore(reqInfo.getParameter(VAR_TASK_DATA));
        taskId = rs.getRow(0).getId();
        Map<String, String> prm = Maps.newHashMap();
        reqInfo.setParams(prm);

        for (BeeColumn col : rs.getColumns()) {
          String colName = col.getId();
          String oldValue = rs.getShadowString(0, colName);
          String value = rs.getString(0, colName);
          prm.put(VAR_TASK_COMMENT, BeeUtils.join(" -> ", oldValue, value));

          if (response == null || !response.hasErrors()) {
            response = registerTaskEvent(taskId, time, reqInfo, event, colName);
          }
        }
        if (!response.hasErrors()) {
          response = deb.commitRow(rs, true);
        }
        break;

      case SUSPENDED:
      case CANCELED:
      case COMPLETED:
      case APPROVED:
      case RENEWED:
        rs = BeeRowSet.restore(reqInfo.getParameter(VAR_TASK_DATA));
        taskId = rs.getRow(0).getId();

        response = deb.commitRow(rs, false);

        if (!response.hasErrors()) {
          response = registerTaskEvent(taskId, time, reqInfo, event, null);
        }
        break;

      case DELETED:
        Assert.untouchable();
    }

    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private long getTaskId(RequestInfo reqInfo) {
    return BeeUtils.toLong(reqInfo.getParameter(VAR_TASK_ID));
  }

  private Map<String, List<Long>> getTaskRelations(long taskId) {
    Map<String, List<Long>> result = Maps.newHashMap();

    SqlSelect query = new SqlSelect().addFrom(CommonsConstants.TBL_RELATIONS)
        .addFields(CommonsConstants.TBL_RELATIONS,
            CommonsConstants.COL_TABLE_1, CommonsConstants.COL_ROW_1,
            CommonsConstants.COL_TABLE_2, CommonsConstants.COL_ROW_2)
        .setWhere(SqlUtils.or(
            SqlUtils.and(
                SqlUtils.equal(CommonsConstants.TBL_RELATIONS,
                    CommonsConstants.COL_TABLE_1, TBL_TASKS),
                SqlUtils.equal(CommonsConstants.TBL_RELATIONS,
                    CommonsConstants.COL_ROW_1, taskId)),
            SqlUtils.and(
                SqlUtils.equal(CommonsConstants.TBL_RELATIONS,
                    CommonsConstants.COL_TABLE_2, TBL_TASKS),
                SqlUtils.equal(CommonsConstants.TBL_RELATIONS,
                    CommonsConstants.COL_ROW_2, taskId)
                )));

    for (Map<String, String> row : qs.getData(query)) {
      String t1 = row.get(CommonsConstants.COL_TABLE_1);
      long r1 = BeeUtils.toLong(row.get(CommonsConstants.COL_ROW_1));
      String t2 = row.get(CommonsConstants.COL_TABLE_2);
      long r2 = BeeUtils.toLong(row.get(CommonsConstants.COL_ROW_2));
      
      String key;
      long id;
      
      if (BeeUtils.same(t1, TBL_TASKS) && r1 == taskId) {
        key = t2;
        id = r2;
      } else {
        key = t1;
        id = r1;
      }
      
      if (result.containsKey(key)) {
        result.get(key).add(id);
      } else {
        result.put(key, Lists.newArrayList(id));
      }
    }

    return result;
  }

  private String getTaskUsers(long taskId, Collection<Long> exclude) {
    SqlSelect query = new SqlSelect().addFrom(TBL_TASK_USERS).addFields(TBL_TASK_USERS, COL_USER);

    IsCondition condition = SqlUtils.equal(TBL_TASK_USERS, COL_TASK, taskId);
    if (BeeUtils.isEmpty(exclude)) {
      query.setWhere(condition);
    } else {
      HasConditions where = SqlUtils.and(condition);
      for (Long user : exclude) {
        where.add(SqlUtils.notEqual(TBL_TASK_USERS, COL_USER, user));
      }
      query.setWhere(where);
    }

    return DataUtils.buildIdList(qs.getLongColumn(query));
  }

  private ResponseObject registerTaskEvent(long taskId, long time, RequestInfo reqInfo,
      TaskEvent event, String eventNote) {
    ResponseObject response = null;

    int minutes = BeeUtils.toInt(reqInfo.getParameter(VAR_TASK_DURATION_TIME));

    if (BeeUtils.isPositive(minutes)) {
      response = qs.insertDataWithResponse(new SqlInsert("EventDurations")
          .addConstant("DurationDate",
              BeeUtils.toLong(reqInfo.getParameter(VAR_TASK_DURATION_DATE)))
          .addConstant("DurationType",
              BeeUtils.toLong(reqInfo.getParameter(VAR_TASK_DURATION_TYPE)))
          .addConstant("Duration", minutes));
    }
    long currentUser = usr.getCurrentUserId();

    if (response == null || !response.hasErrors()) {
      response = qs.insertDataWithResponse(new SqlInsert("TaskEvents")
          .addConstant(COL_TASK, taskId)
          .addConstant("Publisher", currentUser)
          .addConstant("PublishTime", time)
          .addConstant("Comment", reqInfo.getParameter(VAR_TASK_COMMENT))
          .addConstant(COL_EVENT, event.ordinal())
          .addConstant("EventNote", eventNote)
          .addConstant("EventDuration", response == null ? null : (Long) response.getResponse()));
    }
    if (!response.hasErrors()) {
      response = registerTaskVisit(taskId, currentUser, time);
    }
    return response;
  }

  private ResponseObject registerTaskVisit(long taskId, long userId, Long time) {
    ResponseObject response;

    HasConditions where = SqlUtils.and(
        SqlUtils.equal(TBL_TASK_USERS, COL_TASK, taskId),
        SqlUtils.equal(TBL_TASK_USERS, COL_USER, userId));

    if (qs.sqlExists(TBL_TASK_USERS, where)) {
      response = qs.updateDataWithResponse(new SqlUpdate(TBL_TASK_USERS)
          .addConstant(COL_LAST_ACCESS, time)
          .setWhere(where));
    } else {
      response = qs.insertDataWithResponse(new SqlInsert(TBL_TASK_USERS)
          .addConstant(COL_TASK, taskId)
          .addConstant(COL_USER, userId)
          .addConstant(COL_LAST_ACCESS, time));
    }

    if (!response.hasErrors()) {
      response = ResponseObject.response(time);
    }
    return response;
  }

  private String translateTaskPropertyNameToTableName(String propertyName) {
    if (BeeUtils.same(propertyName, PROP_COMPANIES)) {
      return CommonsConstants.TBL_COMPANIES;
    } else if (BeeUtils.same(propertyName, PROP_PERSONS)) {
      return CommonsConstants.TBL_PERSONS;
    } else if (BeeUtils.same(propertyName, PROP_APPOINTMENTS)) {
      return CalendarConstants.TBL_APPOINTMENTS;
    } else if (BeeUtils.same(propertyName, PROP_TASKS)) {
      return TBL_TASKS;
    } else {
      return null;
    }
  }
}
