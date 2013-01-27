package com.butent.bee.server.modules.crm;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.FileNameUtils;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmUtils;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.time.DateTime;
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

    } else if (BeeUtils.same(svc, SVC_GET_TASK_DATA)) {
      response = getTaskData(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CHANGED_TASKS)) {
      response = getChangedTasks();
      
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
      public void setRowProperties(ViewQueryEvent event) {
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

            IsCondition uwh = SqlUtils.equals(TBL_TASK_USERS, COL_USER, usr.getCurrentUserId());

            if (taskIds.isEmpty()) {
              tuQuery.setWhere(uwh);
            } else {
              tuQuery.setWhere(SqlUtils.and(uwh,
                  SqlUtils.inList(TBL_TASK_USERS, COL_TASK, taskIds)));
            }

            SimpleRowSet tuData = qs.getData(tuQuery);
            int taskIndex = tuData.getColumnIndex(COL_TASK);
            int accessIndex = tuData.getColumnIndex(COL_LAST_ACCESS);
            int starIndex = tuData.getColumnIndex(COL_STAR);

            for (SimpleRow tuRow : tuData) {
              long taskId = tuRow.getLong(taskIndex);
              BeeRow row = rowSet.getRowById(taskId);
              if (row == null) {
                continue;
              }

              row.setProperty(PROP_USER, BeeConst.STRING_PLUS);

              if (tuRow.getValue(accessIndex) != null) {
                row.setProperty(PROP_LAST_ACCESS, tuRow.getValue(accessIndex));
              }
              if (tuRow.getValue(starIndex) != null) {
                row.setProperty(PROP_STAR, tuRow.getValue(starIndex));
              }
            }

            SqlSelect teQuery = new SqlSelect().addFrom(TBL_TASK_EVENTS)
                .addFields(TBL_TASK_EVENTS, COL_TASK)
                .addMax(TBL_TASK_EVENTS, COL_PUBLISH_TIME)
                .addGroup(TBL_TASK_EVENTS, COL_TASK);

            if (!taskIds.isEmpty()) {
              teQuery.setWhere(SqlUtils.inList(TBL_TASK_EVENTS, COL_TASK, taskIds));
            }

            SimpleRowSet teData = qs.getData(teQuery);
            taskIndex = teData.getColumnIndex(COL_TASK);
            int publishIndex = teData.getColumnIndex(COL_PUBLISH_TIME);

            for (SimpleRow teRow : teData) {
              long taskId = teRow.getLong(taskIndex);
              BeeRow row = rowSet.getRowById(taskId);

              if (teRow.getValue(publishIndex) != null) {
                row.setProperty(PROP_LAST_PUBLISH, teRow.getValue(publishIndex));
              }
            }
          }

        } else if (BeeUtils.same(event.getViewName(), VIEW_DOCUMENT_FILES)) {
          BeeRowSet rowSet = event.getRowset();

          if (!rowSet.isEmpty()) {
            int fnIndex = rowSet.getColumnIndex(COL_FILE_NAME);

            for (BeeRow row : rowSet.getRows()) {
              String icon = FileNameUtils.getExtensionIcon(row.getString(fnIndex));
              if (!BeeUtils.isEmpty(icon)) {
                row.setProperty(PROP_ICON, icon);
              }
            }
          }
        }
      }
    });
  }

  private void addTaskProperties(BeeRow row, List<BeeColumn> columns, Collection<Long> taskUsers,
      Long eventId) {
    long taskId = row.getId();

    if (!BeeUtils.isEmpty(taskUsers)) {
      taskUsers.remove(row.getLong(DataUtils.getColumnIndex(COL_EXECUTOR, columns)));
      taskUsers.remove(row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns)));

      if (!taskUsers.isEmpty()) {
        row.setProperty(PROP_OBSERVERS, DataUtils.buildIdList(taskUsers));
      }
    }

    Map<String, List<Long>> taskRelations = getTaskRelations(taskId);
    for (Map.Entry<String, List<Long>> entry : taskRelations.entrySet()) {
      row.setProperty(entry.getKey(), DataUtils.buildIdList(entry.getValue()));
    }

    List<StoredFile> files = getTaskFiles(taskId);
    if (!files.isEmpty()) {
      row.setProperty(PROP_FILES, Codec.beeSerialize(files));
    }

    BeeRowSet events = qs.getViewData(VIEW_TASK_EVENTS,
        ComparisonFilter.isEqual(COL_TASK, new LongValue(taskId)));
    if (!DataUtils.isEmpty(events)) {
      row.setProperty(PROP_EVENTS, events.serialize());
    }

    if (eventId != null) {
      row.setProperty(PROP_LAST_EVENT_ID, BeeUtils.toString(eventId));
    }
  }

  private ResponseObject commitTaskData(BeeRowSet data, Collection<Long> oldUsers,
      boolean checkUsers, Set<String> updatedRelations, Long eventId) {

    ResponseObject response;
    BeeRow row = data.getRow(0);
    
    List<Long> newUsers;
    if (checkUsers) {
      newUsers = CrmUtils.getTaskUsers(row, data.getColumns());
      if (!BeeUtils.sameElements(oldUsers, newUsers)) {
        updateTaskUsers(row.getId(), oldUsers, newUsers);
      }
    } else {
      newUsers = Lists.newArrayList(oldUsers);
    }

    if (!BeeUtils.isEmpty(updatedRelations)) {
      updateTaskRelations(row.getId(), updatedRelations, row);
    }

    Map<Integer, String> shadow = row.getShadow();
    if (shadow != null && !shadow.isEmpty()) {
      List<BeeColumn> columns = Lists.newArrayList();
      List<String> oldValues = Lists.newArrayList();
      List<String> newValues = Lists.newArrayList();

      for (Map.Entry<Integer, String> entry : shadow.entrySet()) {
        columns.add(data.getColumn(entry.getKey()));

        oldValues.add(entry.getValue());
        newValues.add(row.getString(entry.getKey()));
      }
      
      BeeRow updRow = new BeeRow(row.getId(), row.getVersion(), oldValues);
      for (int i = 0; i < columns.size(); i++) {
        updRow.preliminaryUpdate(i, newValues.get(i));
      }

      BeeRowSet updated = new BeeRowSet(data.getViewName(), columns);
      updated.addRow(updRow);

      response = deb.commitRow(updated, true);
      if (!response.hasErrors() && response.hasResponse(BeeRow.class)) {
        addTaskProperties((BeeRow) response.getResponse(), data.getColumns(), newUsers, eventId);
      }

    } else {
      response = getTaskData(row.getId(), eventId);
    }

    return response;
  }

  private ResponseObject createTaskRelations(long taskId, Map<String, String> properties) {
    int count = 0;
    if (BeeUtils.isEmpty(properties)) {
      return ResponseObject.response(count);
    }

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String relation = CrmUtils.translateTaskPropertyToRelation(entry.getKey());
      if (BeeUtils.isEmpty(relation)) {
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
            .addConstant(CommonsConstants.COL_TABLE_2, relation)
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

    String dataParam = reqInfo.getParameter(VAR_TASK_DATA);
    if (BeeUtils.isEmpty(dataParam)) {
      String msg = BeeUtils.joinWords("Task data not received:", svc, event);
      logger.warning(msg);
      response = ResponseObject.error(msg);
      return response;
    }

    BeeRowSet taskData = BeeRowSet.restore(dataParam);
    BeeRow taskRow = taskData.getRow(0);

    long taskId = taskRow.getId();

    long currentUser = usr.getCurrentUserId();
    long now = System.currentTimeMillis();

    Long eventId = null;
    String eventNote;
    
    String notes = reqInfo.getParameter(VAR_TASK_NOTES);
    if (BeeUtils.isEmpty(notes)) {
      eventNote = null;
    } else {
      eventNote = BeeUtils.buildLines(Codec.beeDeserializeCollection(notes));
    }

    Set<Long> oldUsers = DataUtils.parseIdSet(reqInfo.getParameter(VAR_TASK_USERS));
    Set<String> updatedRelations = NameUtils.toSet(reqInfo.getParameter(VAR_TASK_RELATIONS));

    switch (event) {
      case CREATE:
        
        Map<String, String> properties = taskRow.getProperties();

        List<Long> executors = DataUtils.parseIdList(properties.get(PROP_EXECUTORS));
        List<Long> observers = DataUtils.parseIdList(properties.get(PROP_OBSERVERS));

        List<Long> tasks = Lists.newArrayList();

        for (long executor : executors) {
          DateTime start = taskRow.getDateTime(taskData.getColumnIndex(COL_START_TIME));
          
          BeeRow newRow = DataUtils.cloneRow(taskRow);
          newRow.setValue(taskData.getColumnIndex(COL_EXECUTOR), executor);

          TaskStatus status;
          if (CrmUtils.isScheduled(start)) {
            status = TaskStatus.SCHEDULED;
          } else {
            status = (executor == currentUser) ? TaskStatus.ACTIVE : TaskStatus.NOT_VISITED;
          }
          newRow.setValue(taskData.getColumnIndex(COL_STATUS), status.ordinal());

          taskData.clearRows();
          taskData.addRow(newRow);

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
        
        if (oldUsers.contains(currentUser)) {
          response = registerTaskVisit(taskId, currentUser, now);
        }

        if (response == null || !response.hasErrors()) {
          response = commitTaskData(taskData, oldUsers, false, updatedRelations, eventId);
        }
        break;

      case FORWARD:
      case EXTEND:
      case EDIT:
      case COMMENT:
      case SUSPEND:
      case CANCEL:
      case COMPLETE:
      case APPROVE:
      case RENEW:
      case ACTIVATE:

        response = registerTaskEvent(taskId, currentUser, event, reqInfo, eventNote, now);
        if (response.hasResponse(Long.class)) {
          eventId = (Long) response.getResponse();
        }
        if (!response.hasErrors()) {
          response = registerTaskVisit(taskId, currentUser, now);
        }

        if (!response.hasErrors()) {
          response = commitTaskData(taskData, oldUsers, true, updatedRelations, eventId);
        }
        break;
    }

    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private ResponseObject getChangedTasks() {
    IsCondition uwh = SqlUtils.equals(TBL_TASK_USERS, COL_USER, usr.getCurrentUserId());

    SqlSelect tNewQuery = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TASK_USERS)
        .addFields(TBL_TASK_USERS, COL_TASK)
        .setWhere(SqlUtils.and(uwh, SqlUtils.isNull(TBL_TASK_USERS, COL_LAST_ACCESS)));
    
    Long[] newTasks = qs.getLongColumn(tNewQuery);
    logger.debug("new tasks", newTasks);
    
    String idName = sys.getIdName(TBL_TASKS); 
    
    SqlSelect inner = new SqlSelect()
        .addFrom(TBL_TASKS)
        .addFromInner(TBL_TASK_USERS, sys.joinTables(TBL_TASKS, TBL_TASK_USERS, COL_TASK))
        .addFromInner(TBL_TASK_EVENTS, sys.joinTables(TBL_TASKS, TBL_TASK_EVENTS, COL_TASK))
        .addFields(TBL_TASKS, idName)
        .addMax(TBL_TASK_USERS, COL_LAST_ACCESS)
        .addMax(TBL_TASK_EVENTS, COL_PUBLISH_TIME)
        .addGroup(TBL_TASKS, idName)
        .setWhere(SqlUtils.and(uwh, SqlUtils.notNull(TBL_TASK_USERS, COL_LAST_ACCESS)));

    String alias = "tupd_" + SqlUtils.uniqueName();
    
    SqlSelect tUpdQuery = new SqlSelect().setDistinctMode(true)
        .addFrom(inner, alias)
        .addFields(alias, idName)
        .setWhere(SqlUtils.more(alias, COL_PUBLISH_TIME, SqlUtils.field(alias, COL_LAST_ACCESS)));
    
    Long[] updTasks = qs.getLongColumn(tUpdQuery);
    logger.debug("upd tasks", updTasks);
    
    List<Long> result = Lists.newArrayList();
    
    long cntNew = (newTasks == null) ? 0 : newTasks.length;
    result.add(cntNew);
    if (cntNew > 0) {
      result.addAll(Lists.newArrayList((newTasks)));
    }

    if (updTasks != null && updTasks.length > 0) {
      result.addAll(Lists.newArrayList((updTasks)));
    }
    
    return ResponseObject.response(Joiner.on(BeeConst.CHAR_COMMA).join(result));
  }

  private ResponseObject getTaskData(long taskId, Long eventId) {
    BeeRowSet rowSet = qs.getViewData(VIEW_TASKS, ComparisonFilter.compareId(taskId));
    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject.error(SVC_GET_TASK_DATA, "task id: " + taskId + " not found");
    }

    BeeRow data = rowSet.getRow(0);
    addTaskProperties(data, rowSet.getColumns(), getTaskUsers(taskId), eventId);

    return ResponseObject.response(data);
  }

  private ResponseObject getTaskData(RequestInfo reqInfo) {
    long taskId = BeeUtils.toLong(reqInfo.getParameter(VAR_TASK_ID));
    if (!DataUtils.isId(taskId)) {
      String msg = BeeUtils.joinWords(SVC_GET_TASK_DATA, "task id not received");
      logger.warning(msg);
      return ResponseObject.error(msg);
    }

    return getTaskData(taskId, null);
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

  private Map<String, List<Long>> getTaskRelations(long taskId) {
    Map<String, List<Long>> result = Maps.newHashMap();

    SqlSelect query =
        new SqlSelect().addFrom(CommonsConstants.TBL_RELATIONS)
            .addFields(CommonsConstants.TBL_RELATIONS,
                CommonsConstants.COL_TABLE_1, CommonsConstants.COL_ROW_1,
                CommonsConstants.COL_TABLE_2, CommonsConstants.COL_ROW_2)
            .setWhere(SqlUtils.or(
                SqlUtils.equals(CommonsConstants.TBL_RELATIONS, CommonsConstants.COL_TABLE_1,
                    TBL_TASKS, CommonsConstants.COL_ROW_1, taskId),
                SqlUtils.equals(CommonsConstants.TBL_RELATIONS, CommonsConstants.COL_TABLE_2,
                    TBL_TASKS, CommonsConstants.COL_ROW_2, taskId)));

    for (SimpleRow row : qs.getData(query)) {
      String t1 = row.getValue(CommonsConstants.COL_TABLE_1);
      long r1 = row.getLong(CommonsConstants.COL_ROW_1);
      String t2 = row.getValue(CommonsConstants.COL_TABLE_2);
      long r2 = row.getLong(CommonsConstants.COL_ROW_2);

      String key;
      long id;

      if (BeeUtils.same(t1, TBL_TASKS) && r1 == taskId) {
        key = CrmUtils.translateRelationToTaskProperty(t2);
        id = r2;
      } else {
        key = CrmUtils.translateRelationToTaskProperty(t1);
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
    if (!DataUtils.isId(taskId)) {
      return Lists.newArrayList();
    }

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_TASK_USERS)
        .addFields(TBL_TASK_USERS, COL_USER)
        .setWhere(SqlUtils.equals(TBL_TASK_USERS, COL_TASK, taskId))
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
    IsCondition where = SqlUtils.equals(TBL_TASK_USERS, COL_TASK, taskId, COL_USER, userId);

    return qs.updateDataWithResponse(new SqlUpdate(TBL_TASK_USERS)
        .addConstant(COL_LAST_ACCESS, millis)
        .setWhere(where));
  }

  private void updateTaskRelation(long taskId, String property, Collection<Long> oldValues,
      Collection<Long> newValues) {
    if (BeeUtils.sameElements(oldValues, newValues)) {
      return;
    }

    List<Long> insert = Lists.newArrayList(newValues);
    insert.removeAll(oldValues);

    List<Long> delete = Lists.newArrayList(oldValues);
    delete.removeAll(newValues);

    String relation = CrmUtils.translateTaskPropertyToRelation(property);
    
    for (Long value : insert) {
      logger.debug("add task relation", taskId, relation, value);

      qs.insertData(new SqlInsert(CommonsConstants.TBL_RELATIONS)
          .addConstant(CommonsConstants.COL_TABLE_1, TBL_TASKS)
          .addConstant(CommonsConstants.COL_ROW_1, taskId)
          .addConstant(CommonsConstants.COL_TABLE_2, relation)
          .addConstant(CommonsConstants.COL_ROW_2, value));
    }

    for (Long value : delete) {
      logger.debug("delete task relation", taskId, relation, value);

      IsCondition condition = SqlUtils.or(
          SqlUtils.equals(CommonsConstants.TBL_RELATIONS, CommonsConstants.COL_TABLE_1, TBL_TASKS,
              CommonsConstants.COL_ROW_1, taskId, CommonsConstants.COL_TABLE_2, relation,
              CommonsConstants.COL_ROW_2, value),
          SqlUtils.equals(CommonsConstants.TBL_RELATIONS, CommonsConstants.COL_TABLE_1, relation,
              CommonsConstants.COL_ROW_1, value, CommonsConstants.COL_TABLE_2, TBL_TASKS,
              CommonsConstants.COL_ROW_2, taskId));

      qs.updateData(new SqlDelete(CommonsConstants.TBL_RELATIONS).setWhere(condition));
    }
  }

  private void updateTaskRelations(long taskId, Set<String> updatedRelations, BeeRow row) {
    Map<String, List<Long>> oldRelations = getTaskRelations(taskId);

    for (String relation : updatedRelations) {
      List<Long> oldValues = Lists.newArrayList();
      if (oldRelations.containsKey(relation)) {
        oldValues.addAll(oldRelations.get(relation));
      }

      List<Long> newValues = DataUtils.parseIdList(row.getProperty(relation));

      updateTaskRelation(taskId, relation, oldValues, newValues);
    }
  }
  
  private void updateTaskUsers(long taskId, Collection<Long> oldUsers, Collection<Long> newUsers) {
    List<Long> insert = Lists.newArrayList(newUsers);
    insert.removeAll(oldUsers);

    List<Long> delete = Lists.newArrayList(oldUsers);
    delete.removeAll(newUsers);

    for (Long user : insert) {
      createTaskUser(taskId, user, null);
    }

    String tblName = TBL_TASK_USERS;
    for (Long user : delete) {
      IsCondition condition = SqlUtils.equals(tblName, COL_TASK, taskId, COL_USER, user);
      qs.updateData(new SqlDelete(tblName).setWhere(condition));
    }
  }
}
