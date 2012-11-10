package com.butent.bee.server.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.FileNameUtils;
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
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
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

  private ResponseObject createTaskUser(long taskId, long userId, boolean registerTime) {
    SqlInsert si = new SqlInsert(TBL_TASK_USERS)
        .addConstant(COL_TASK, taskId)
        .addConstant(COL_USER, userId);
    
    if (registerTime) {
      si.addConstant(COL_LAST_ACCESS, System.currentTimeMillis());
    }

    return qs.insertDataWithResponse(si);
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

    long currentUser = usr.getCurrentUserId();
    long taskId;

    switch (event) {
      case ACTIVATED:
        BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(VAR_TASK_DATA));
        BeeRow row = rs.getRow(0);

        Map<String, String> properties = row.getProperties();

        List<Long> executors = DataUtils.parseIdList(properties.get(PROP_EXECUTORS));
        List<Long> observers = DataUtils.parseIdList(properties.get(PROP_OBSERVERS));

        List<Long> tasks = Lists.newArrayList();

        int exIndex = DataUtils.getColumnIndex(COL_EXECUTOR, rs.getColumns());

        for (long executor : executors) {
          rs.clearRows();
          rs.addRow(DataUtils.cloneRow(row));
          rs.getRow(0).setValue(exIndex, executor);

          response = deb.commitRow(rs, false);
          if (response.hasErrors()) {
            break;
          }

          taskId = ((BeeRow) response.getResponse()).getId();

          response = registerTaskEvent(taskId, currentUser, event);
          if (!response.hasErrors()) {
            createTaskUser(taskId, currentUser, true);
          }

          if (!response.hasErrors() && executor != currentUser) {
            response = createTaskUser(taskId, executor, false);
          }

          if (!response.hasErrors()) {
            for (long obsId : observers) {
              if (obsId != currentUser && obsId != executor) {
                response = createTaskUser(taskId, obsId, false);
                if (response.hasErrors()) {
                  break;
                }
              }
            }
          }

          if (!response.hasErrors()) {
            response = createTaskRelations(taskId, properties);
          }
          if (!response.hasErrors()) {
            tasks.add(taskId);
          }

          if (response.hasErrors()) {
            break;
          }
        }

        if (!response.hasErrors()) {
          if (tasks.isEmpty()) {
            response = ResponseObject.error("No tasks created");
          } else {
            response = ResponseObject.response(DataUtils.buildIdList(tasks));
          }
        }
        break;

      case VISITED:
        taskId = getTaskId(reqInfo);
        response = registerTaskVisit(taskId, currentUser);

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
          
          List<StoredFile> files = getTaskFiles(taskId);
          if (!files.isEmpty()) {
            taskProperties.put(PROP_FILES, Codec.beeSerialize(files));
          }

          response = ResponseObject.response(taskProperties);
        }
        break;

      case COMMENTED:
        taskId = getTaskId(reqInfo);
        response = registerTaskVisit(taskId, currentUser);

        if (!response.hasErrors()) {
          response = registerTaskEvent(getTaskId(reqInfo), currentUser, event, reqInfo, null);
        }
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
          response = createTaskUser(taskId, newUser, false);
        }
        if (!response.hasErrors()) {
          response = registerTaskEvent(taskId, currentUser, event, reqInfo, 
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
        String oldTerm = rs.getShadowString(0, COL_FINISH_TIME);
        String newTerm = rs.getString(0, COL_FINISH_TIME);

        response = deb.commitRow(rs, false);

        if (!response.hasErrors()) {
          response = registerTaskEvent(taskId, currentUser, event, reqInfo,
              BeeUtils.join(" -> ",
                  TimeUtils.toDateTimeOrNull(oldTerm), TimeUtils.toDateTimeOrNull(newTerm)));
        }
        break;

      case EDITED:
        rs = BeeRowSet.restore(reqInfo.getParameter(VAR_TASK_DATA));
        taskId = rs.getRow(0).getId();
        Map<String, String> prm = Maps.newHashMap();

        for (BeeColumn col : rs.getColumns()) {
          String colName = col.getId();
          String oldValue = rs.getShadowString(0, colName);
          String value = rs.getString(0, colName);
          prm.put(VAR_TASK_COMMENT, BeeUtils.join(" -> ", oldValue, value));

          if (response == null || !response.hasErrors()) {
            response = registerTaskEvent(taskId, currentUser, event, reqInfo, colName);
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
          response = registerTaskEvent(taskId, currentUser, event, reqInfo, null);
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

  private List<StoredFile> getTaskFiles(long taskId) {
    List<StoredFile> result = Lists.newArrayList();

    BeeRowSet rowSet = qs.getViewData(VIEW_TASK_FILES,
        ComparisonFilter.isEqual(COL_TASK, new LongValue(taskId)));
    if (rowSet == null || rowSet.isEmpty()) {
      return result;
    }
    
    for (BeeRow row : rowSet.getRows()) {
      StoredFile sf = new StoredFile(DataUtils.getLong(rowSet, row, COL_FILE),
          DataUtils.getString(rowSet, row, COL_FILE_NAME),
          DataUtils.getLong(rowSet, row, COL_FILE_SIZE),
          DataUtils.getString(rowSet, row, COL_FILE_TYPE));

      Long teId = DataUtils.getLong(rowSet, row, COL_TASK_EVENT);
      if (teId != null) {
        sf.setRelatedId(teId);
      }
      
      String caption = DataUtils.getString(rowSet, row, COL_CAPTION);
      if (!BeeUtils.isEmpty(caption)) {
        sf.setCaption(caption);
      }
      
      sf.setIcon(FileNameUtils.getExtensionIcon(sf.getName()));
      result.add(sf);
    }

    return result;
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

  private ResponseObject registerTaskDuration(long durationType, RequestInfo reqInfo) {
    Long date = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_TASK_DURATION_DATE));
    if (date == null) {
      return ResponseObject.error("task duration date not specified");
    }

    Integer minutes = BeeUtils.toIntOrNull(reqInfo.getParameter(VAR_TASK_DURATION_TIME));
    if (!BeeUtils.isPositive(minutes)) {
      return ResponseObject.error("task duration date not specified");
    }

    return qs.insertDataWithResponse(new SqlInsert(TBL_EVENT_DURATIONS)
        .addConstant(COL_DURATION_TYPE, durationType)
        .addConstant(COL_DURATION_DATE, date)
        .addConstant(COL_DURATION, minutes));
  }

  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event) {
    return registerTaskEvent(taskId, userId, event, null, null, null);
  }
  
  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event,
      RequestInfo reqInfo, String note) {
    String comment = reqInfo.getParameter(VAR_TASK_COMMENT);

    Long durationId = null;
    Long durationType = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_TASK_DURATION_TYPE));

    if (DataUtils.isId(durationType)) {
      ResponseObject response = registerTaskDuration(durationType, reqInfo);
      if (response.hasErrors()) {
        return response;
      } else if (response.hasResponse(Long.class)) {
        durationId = (Long) response.getResponse();
      }
    }
    
    return registerTaskEvent(taskId, userId, event, comment, note, durationId);
  }

  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event,
      String comment, String note, Long durationId) {

    SqlInsert si = new SqlInsert(TBL_TASK_EVENTS)
        .addConstant(COL_TASK, taskId)
        .addConstant(COL_PUBLISHER, userId)
        .addConstant(COL_PUBLISH_TIME, System.currentTimeMillis())
        .addConstant(COL_EVENT, event.ordinal());

    if (!BeeUtils.isEmpty(comment)) {
      si.addConstant(COL_COMMENT, comment);
    }
    if (!BeeUtils.isEmpty(note)) {
      si.addConstant(COL_EVENT_NOTE, note);
    }

    if (DataUtils.isId(durationId)) {
      si.addConstant(COL_EVENT_DURATION, durationId);
    }

    return qs.insertDataWithResponse(si);
  }

  private ResponseObject registerTaskVisit(long taskId, long userId) {
    HasConditions where = SqlUtils.and(
        SqlUtils.equal(TBL_TASK_USERS, COL_TASK, taskId),
        SqlUtils.equal(TBL_TASK_USERS, COL_USER, userId));

    return qs.updateDataWithResponse(new SqlUpdate(TBL_TASK_USERS)
        .addConstant(COL_LAST_ACCESS, System.currentTimeMillis())
        .setWhere(where));
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
