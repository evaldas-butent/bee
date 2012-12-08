package com.butent.bee.server.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.FileNameUtils;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CustomProperties;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  SystemBean sys;
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
    return qs.getSearchResults(VIEW_TASKS,
        Filter.anyContains(Sets.newHashSet(COL_SUMMARY, COL_DESCRIPTION, COL_COMPANY_NAME,
            COL_EXECUTOR_FIRST_NAME, COL_EXECUTOR_LAST_NAME), query));
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
    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void getTaskChildProperties(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), VIEW_TASKS)) {
          BeeRowSet rowSet = event.getRowset();
          
          if (!rowSet.isEmpty()) {
            Set<Long> taskIds = Sets.newHashSet();
            if (rowSet.getNumberOfRows() < 100) {
              for (BeeRow row : rowSet.getRows()) {
                taskIds.add(row.getId());
              }
            }
            
            SqlSelect tuQuery = new SqlSelect().addFrom(TBL_TASK_USERS)
                .addFields(TBL_TASK_USERS, COL_TASK, COL_LAST_ACCESS, COL_STAR);
            
            IsCondition uwh = SqlUtils.equal(TBL_TASK_USERS, COL_USER, usr.getCurrentUserId());
            
            if (taskIds.isEmpty()) {
              tuQuery.setWhere(uwh);
            } else {
              tuQuery.setWhere(SqlUtils.and(uwh, SqlUtils.any(TBL_TASK_USERS, COL_TASK, taskIds))); 
            }
            
            SimpleRowSet tuData = qs.getData(tuQuery);
            int taskIndex = tuData.getColumnIndex(COL_TASK);
            int accessIndex = tuData.getColumnIndex(COL_LAST_ACCESS);
            int starIndex = tuData.getColumnIndex(COL_STAR);
            
            for (String[] tuRow : tuData.getRows()) {
              long taskId = BeeUtils.toLong(tuRow[taskIndex]);
              BeeRow row = rowSet.getRowById(taskId);
              if (row == null) {
                continue;
              }
              
              row.setProperty(PROP_USER, BeeConst.STRING_PLUS);
              
              if (tuRow[accessIndex] != null) {
                row.setProperty(PROP_LAST_ACCESS, tuRow[accessIndex]);
              }
              if (tuRow[starIndex] != null) {
                row.setProperty(PROP_STAR, tuRow[starIndex]);
              }
            }
            
            SqlSelect teQuery = new SqlSelect().addFrom(TBL_TASK_EVENTS)
                .addFields(TBL_TASK_EVENTS, COL_TASK)
                .addMax(TBL_TASK_EVENTS, COL_PUBLISH_TIME)
                .addGroup(TBL_TASK_EVENTS, COL_TASK);
            
            if (!taskIds.isEmpty()) {
              teQuery.setWhere(SqlUtils.any(TBL_TASK_EVENTS, COL_TASK, taskIds));
            }
            
            SimpleRowSet teData = qs.getData(teQuery);
            taskIndex = teData.getColumnIndex(COL_TASK);
            int publishIndex = teData.getColumnIndex(COL_PUBLISH_TIME);
            
            for (String[] teRow : teData.getRows()) {
              long taskId = BeeUtils.toLong(teRow[taskIndex]);
              BeeRow row = rowSet.getRowById(taskId);
              
              if (teRow[publishIndex] != null) {
                row.setProperty(PROP_LAST_PUBLISH, teRow[publishIndex]);
              }
            }
          }
        }
      }
    });
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

  private ResponseObject createTaskUser(long taskId, long userId, Long millis) {
    SqlInsert si = new SqlInsert(TBL_TASK_USERS)
        .addConstant(COL_TASK, taskId)
        .addConstant(COL_USER, userId);

    if (millis != null) {
      si.addConstant(COL_LAST_ACCESS, millis);
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

    BeeRowSet taskData;
    BeeRow taskRow;
    long taskId;

    String dataParam = reqInfo.getParameter(VAR_TASK_DATA);
    if (BeeUtils.isEmpty(dataParam)) {
      taskData = null;
      taskRow = null;
      taskId = getTaskId(reqInfo);
    } else {
      taskData = BeeRowSet.restore(dataParam);
      taskRow = taskData.getRow(0);
      taskId = taskRow.getId();
    }

    long currentUser = usr.getCurrentUserId();
    long now = System.currentTimeMillis();
    
    int exIndex;

    switch (event) {
      case CREATE:
        Map<String, String> properties = taskRow.getProperties();

        List<Long> executors = DataUtils.parseIdList(properties.get(PROP_EXECUTORS));
        List<Long> observers = DataUtils.parseIdList(properties.get(PROP_OBSERVERS));

        List<Long> tasks = Lists.newArrayList();

        exIndex = taskData.getColumnIndex(COL_EXECUTOR);

        for (long executor : executors) {
          taskData.clearRows();
          taskData.addRow(DataUtils.cloneRow(taskRow));
          taskData.getRow(0).setValue(exIndex, executor);

          response = deb.commitRow(taskData, false);
          if (response.hasErrors()) {
            break;
          }

          taskId = ((BeeRow) response.getResponse()).getId();

          response = registerTaskEvent(taskId, currentUser, event, now);
          if (!response.hasErrors()) {
            createTaskUser(taskId, currentUser, now);
          }

          if (!response.hasErrors() && executor != currentUser) {
            response = createTaskUser(taskId, executor, null);
          }

          if (!response.hasErrors()) {
            for (long obsId : observers) {
              if (obsId != currentUser && obsId != executor) {
                response = createTaskUser(taskId, obsId, null);
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

      case VISIT:
        List<Long> taskUsers = getTaskUsers(taskId);
        if (taskUsers.contains(currentUser)) {
          response = registerTaskVisit(taskId, currentUser, now);
        }

        if (response == null || !response.hasErrors()) {
          if (taskData == null) {
            BeeRowSet rowSet = qs.getViewData(VIEW_TASKS, ComparisonFilter.compareId(taskId));
            if (rowSet.getNumberOfRows() == 1) {
              response = ResponseObject.response(rowSet.getRow(0));
            } else {
              response = ResponseObject.error("Task not found: " + taskId);
            }
          } else {
            response = deb.commitRow(taskData, true);
          }
        }
        
        if (!response.hasErrors() && response.hasResponse(BeeRow.class)) {
          CustomProperties taskProperties = new CustomProperties();
          Set<Long> exclude = DataUtils.parseIdSet(reqInfo.getParameter(VAR_TASK_EXCLUDE));
          if (!exclude.isEmpty()) {
            taskUsers.removeAll(exclude);
          }
          if (!taskUsers.isEmpty()) {
            taskProperties.put(PROP_OBSERVERS, DataUtils.buildIdList(taskUsers));
          }

          Map<String, List<Long>> taskRelations = getTaskRelations(taskId);
          for (Map.Entry<String, List<Long>> entry : taskRelations.entrySet()) {
            taskProperties.put(entry.getKey(), DataUtils.buildIdList(entry.getValue()));
          }

          List<StoredFile> files = getTaskFiles(taskId);
          if (!files.isEmpty()) {
            taskProperties.put(PROP_FILES, Codec.beeSerialize(files));
          }

          BeeRowSet events = qs.getViewData(VIEW_TASK_EVENTS,
              ComparisonFilter.isEqual(COL_TASK, new LongValue(taskId)));
          if (!DataUtils.isEmpty(events)) {
            taskProperties.put(PROP_EVENTS, events.serialize());
          }
          
          ((BeeRow) response.getResponse()).setProperties(taskProperties);
        }
        break;

      case COMMENT:
        response = registerTaskVisit(taskId, currentUser, now);
        if (!response.hasErrors()) {
          response = registerTaskEvent(taskId, currentUser, event, reqInfo, null, now);
        }
        break;

      case FORWARD:
        exIndex = taskData.getColumnIndex(COL_EXECUTOR);

        long oldUser = BeeUtils.toLong(taskRow.getShadowString(exIndex));
        long newUser = BeeUtils.toLong(taskRow.getString(exIndex));

        IsCondition wh = SqlUtils.equal(TBL_TASK_USERS, COL_TASK, taskId);

        response = registerTaskVisit(taskId, currentUser, now);

        if (!response.hasErrors() && !qs.sqlExists(TBL_TASK_USERS, SqlUtils.and(wh,
            SqlUtils.equal(TBL_TASK_USERS, COL_USER, newUser)))) {
          response = createTaskUser(taskId, newUser, null);
        }

        if (!response.hasErrors()) {
          response = registerTaskEvent(taskId, currentUser, event, reqInfo,
              BeeUtils.join(" -> ", usr.getUserSign(oldUser), usr.getUserSign(newUser)), now);
        }

        if (!response.hasErrors()) {
          response = deb.commitRow(taskData, true);
        }
        break;

      case EXTEND:
        String oldTerm = taskData.getShadowString(0, COL_FINISH_TIME);
        String newTerm = taskData.getString(0, COL_FINISH_TIME);

        response = registerTaskEvent(taskId, currentUser, event, reqInfo,
            BeeUtils.join(" -> ", formatDateTime(oldTerm), formatDateTime(newTerm)), now);
        if (!response.hasErrors()) {
          response = deb.commitRow(taskData, true);
        }
        break;

      case EDIT:
        response = registerTaskVisit(taskId, currentUser, now);

        for (BeeColumn col : taskData.getColumns()) {
          String colName = col.getId();
          String oldValue = taskData.getShadowString(0, colName);
          String value = taskData.getString(0, colName);

          String note = colName + ": " + BeeUtils.join(" -> ", oldValue, value);

          if (!response.hasErrors()) {
            response = registerTaskEvent(taskId, currentUser, event, reqInfo, note, now);
          }
        }

        if (!response.hasErrors()) {
          response = deb.commitRow(taskData, true);
        }
        break;

      case SUSPEND:
      case CANCEL:
      case COMPLETE:
      case APPROVE:
      case RENEW:
      case ACTIVATE:
        response = registerTaskEvent(taskId, currentUser, event, reqInfo, null, now);
        if (!response.hasErrors()) {
          response = registerTaskVisit(taskId, currentUser, now);
        }

        if (!response.hasErrors()) {
          response = deb.commitRow(taskData, true);
        }
        break;
    }

    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private String formatDateTime(String serialized) {
    DateTime dateTime = TimeUtils.toDateTimeOrNull(serialized);
    return (dateTime == null) ? BeeConst.STRING_EMPTY : dateTime.toCompactString();
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

  private List<Long> getTaskUsers(long taskId) {
    SqlSelect query = new SqlSelect()
        .addFrom(TBL_TASK_USERS)
        .addFields(TBL_TASK_USERS, COL_USER)
        .setWhere(SqlUtils.equal(TBL_TASK_USERS, COL_TASK, taskId))
        .addOrder(TBL_TASK_USERS, sys.getIdName(TBL_TASK_USERS));
    
    return Lists.newArrayList(qs.getLongColumn(query));
  }

  private ResponseObject registerTaskDuration(long durationType, RequestInfo reqInfo) {
    Long date = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_TASK_DURATION_DATE));
    if (date == null) {
      return ResponseObject.error("task duration date not specified");
    }

    String time = reqInfo.getParameter(VAR_TASK_DURATION_TIME);
    if (BeeUtils.isEmpty(time)) {
      return ResponseObject.error("task duration time not specified");
    }

    return qs.insertDataWithResponse(new SqlInsert(TBL_EVENT_DURATIONS)
        .addConstant(COL_DURATION_TYPE, durationType)
        .addConstant(COL_DURATION_DATE, date)
        .addConstant(COL_DURATION, time));
  }

  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event, long millis) {
    return registerTaskEvent(taskId, userId, event, null, null, null, millis);
  }

  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event,
      RequestInfo reqInfo, String note, long millis) {
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

    return registerTaskEvent(taskId, userId, event, comment, note, durationId, millis);
  }

  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event,
      String comment, String note, Long durationId, long millis) {

    SqlInsert si = new SqlInsert(TBL_TASK_EVENTS)
        .addConstant(COL_TASK, taskId)
        .addConstant(COL_PUBLISHER, userId)
        .addConstant(COL_PUBLISH_TIME, millis)
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

  private ResponseObject registerTaskVisit(long taskId, long userId, long millis) {
    HasConditions where = SqlUtils.and(
        SqlUtils.equal(TBL_TASK_USERS, COL_TASK, taskId),
        SqlUtils.equal(TBL_TASK_USERS, COL_USER, userId));

    return qs.updateDataWithResponse(new SqlUpdate(TBL_TASK_USERS)
        .addConstant(COL_LAST_ACCESS, millis)
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
