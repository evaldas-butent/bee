package com.butent.bee.server.modules.crm;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.ExtensionIcons;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskStatus;
import com.butent.bee.shared.modules.crm.CrmUtils;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.Headline;
import com.butent.bee.shared.news.HeadlineProducer;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
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
  @EJB
  NewsBean news;

  @Resource
  EJBContext ctx;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {

    List<SearchResult> result = Lists.newArrayList();

    List<SearchResult> docsSr = qs.getSearchResults(VIEW_DOCUMENTS,
        Filter.anyContains(Sets.newHashSet(COL_NUMBER, COL_REGISTRATION_NUMBER, COL_NAME,
            COL_DOCUMENT_CATEGORY_NAME, COL_DOCUMENT_TYPE_NAME, COL_DOCUMENT_PLACE_NAME,
            COL_DOCUMENT_STATUS_NAME), query));

    List<SearchResult> tasksSr = qs.getSearchResults(VIEW_TASKS,
        Filter.anyContains(Sets.newHashSet(COL_SUMMARY, COL_DESCRIPTION, COL_COMPANY_NAME,
            COL_EXECUTOR_FIRST_NAME, COL_EXECUTOR_LAST_NAME), query));

    List<SearchResult> taskDurationsSr = qs.getSearchResults(VIEW_TASK_DURATIONS,
        Filter.anyContains(Sets.newHashSet(COL_DURATION_TYPE, COL_COMMENT,
            COL_COMPANY_NAME, COL_SUMMARY, COL_PUBLISHER_FIRST_NAME, COL_PUBLISHER_LAST_NAME),
            query));

    List<SearchResult> taskTemplatesSr = qs.getSearchResults(VIEW_TASK_TEMPLATES,
        Filter.anyContains(Sets.newHashSet(COL_NAME, COL_SUMMARY, COL_DESCRIPTION,
            COL_COMPANY_NAME, COL_CONTACT_FIRST_NAME, COL_CONTACT_LAST_NAME), query));

    result.addAll(docsSr);
    result.addAll(tasksSr);
    result.addAll(taskDurationsSr);
    result.addAll(taskTemplatesSr);

    return result;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CRM_METHOD);

    if (BeeUtils.isPrefix(svc, CRM_TASK_PREFIX)) {
      response = doTaskEvent(BeeUtils.removePrefix(svc, CRM_TASK_PREFIX), reqInfo);

    } else if (BeeUtils.same(svc, SVC_ACCESS_TASK)) {
      response = accessTask(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_TASK_DATA)) {
      response = getTaskData(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CHANGED_TASKS)) {
      response = getChangedTasks();

    } else if (BeeUtils.same(svc, SVC_TASKS_REPORTS_COMPANY_TIMES)) {
      response = getCompanyTimesReport(reqInfo);

    } else if (BeeUtils.same(svc, SVC_TASKS_REPORTS_TYPE_HOURS)) {
      response = getTypeHoursReport(reqInfo);

    } else if (BeeUtils.same(svc, SVC_TASKS_REPORTS_USERS_HOURS)) {
      response = getUsersHoursReport(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_REQUEST_FILES)) {
      response = getRequestFiles(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_REQUEST)));

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
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void setRowProperties(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }
        if (BeeUtils.same(event.getTargetName(), VIEW_TASKS)) {
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

        } else if (BeeUtils.same(event.getTargetName(), VIEW_DOCUMENT_FILES)) {
          BeeRowSet rowSet = event.getRowset();

          if (!rowSet.isEmpty()) {
            int fnIndex = rowSet.getColumnIndex(COL_FILE_NAME);

            if (!BeeConst.isUndef(fnIndex)) {
              for (BeeRow row : rowSet.getRows()) {
                String icon = ExtensionIcons.getIcon(row.getString(fnIndex));
                if (!BeeUtils.isEmpty(icon)) {
                  row.setProperty(CommonsConstants.PROP_ICON, icon);
                }
              }
            }
          }
        } else if (BeeUtils.same(event.getTargetName(), TBL_DOCUMENT_TEMPLATES)) {
          Map<Long, IsRow> indexedRows = Maps.newHashMap();
          BeeRowSet rowSet = event.getRowset();
          int idx = rowSet.getColumnIndex(COL_DOCUMENT_DATA);

          for (BeeRow row : rowSet.getRows()) {
            Long id = row.getLong(idx);

            if (DataUtils.isId(id)) {
              indexedRows.put(id, row);
            }
          }
          if (!indexedRows.isEmpty()) {
            BeeView view = sys.getView(VIEW_MAIN_CRITERIA);
            SqlSelect query = view.getQuery();

            query.setWhere(SqlUtils.and(query.getWhere(),
                SqlUtils.inList(view.getSourceAlias(), COL_DOCUMENT_DATA, indexedRows.keySet())));

            for (SimpleRow row : qs.getData(query)) {
              IsRow r = indexedRows.get(row.getLong(COL_DOCUMENT_DATA));

              if (r != null) {
                r.setProperty(COL_CRITERION_NAME + row.getValue(COL_CRITERION_NAME),
                    row.getValue(COL_CRITERION_VALUE));
              }
            }
          }
        }
      }
    });

    TaskUsageQueryProvider usageQueryProvider = new TaskUsageQueryProvider();

    news.registerUsageQueryProvider(Feed.TASKS_ALL, usageQueryProvider);
    news.registerUsageQueryProvider(Feed.TASKS_ASSIGNED, usageQueryProvider);
    news.registerUsageQueryProvider(Feed.TASKS_DELEGATED, usageQueryProvider);
    news.registerUsageQueryProvider(Feed.TASKS_OBSERVED, usageQueryProvider);

    HeadlineProducer headlineProducer = new HeadlineProducer() {
      @Override
      public Headline produce(Feed feed, long userId, BeeRowSet rowSet, IsRow row, boolean isNew) {
        String caption = DataUtils.getString(rowSet, row, COL_SUMMARY);
        if (BeeUtils.isEmpty(caption)) {
          caption = BeeUtils.bracket(row.getId());
        }

        List<String> subtitles = Lists.newArrayList();

        DateTime finish = DataUtils.getDateTime(rowSet, row, COL_FINISH_TIME);
        if (finish != null) {
          subtitles.add(finish.toCompactString());
        }

        TaskStatus status = EnumUtils.getEnumByIndex(TaskStatus.class,
            DataUtils.getInteger(rowSet, row, COL_STATUS));
        if (status != null) {
          subtitles.add(status.getCaption(usr.getLocalizableConstants(userId)));
        }

        if (feed != Feed.TASKS_ASSIGNED) {
          subtitles.add(BeeUtils.joinWords(
              DataUtils.getString(rowSet, row, COL_EXECUTOR_FIRST_NAME),
              DataUtils.getString(rowSet, row, COL_EXECUTOR_LAST_NAME)));
        }

        return Headline.create(row.getId(), caption, subtitles, isNew);
      }
    };

    news.registerHeadlineProducer(Feed.TASKS_ALL, headlineProducer);
    news.registerHeadlineProducer(Feed.TASKS_ASSIGNED, headlineProducer);
    news.registerHeadlineProducer(Feed.TASKS_DELEGATED, headlineProducer);
    news.registerHeadlineProducer(Feed.TASKS_OBSERVED, headlineProducer);
  }

  private ResponseObject accessTask(RequestInfo reqInfo) {
    Long taskId = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_TASK_ID));
    if (!DataUtils.isId(taskId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), VAR_TASK_ID);
    }

    Long userId = usr.getCurrentUserId();
    if (!DataUtils.isId(userId)) {
      return ResponseObject.error(reqInfo.getService(), "user id not available");
    }

    IsCondition where = SqlUtils.equals(TBL_TASK_USERS, COL_TASK, taskId, COL_USER, userId);
    if (qs.sqlExists(TBL_TASK_USERS, where)) {
      return registerTaskVisit(taskId, userId, System.currentTimeMillis());
    } else {
      logger.warning("task", taskId, "access by unauthorized user", userId);
      return ResponseObject.emptyResponse();
    }
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

    Multimap<String, Long> taskRelations = getTaskRelations(taskId);
    for (String property : taskRelations.keySet()) {
      row.setProperty(property, DataUtils.buildIdList(taskRelations.get(property)));
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
    ResponseObject response = new ResponseObject();
    List<RowChildren> children = Lists.newArrayList();

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String relation = CrmUtils.translateTaskPropertyToRelation(entry.getKey());

      if (BeeUtils.allNotEmpty(relation, entry.getValue())) {
        children.add(RowChildren.create(CommonsConstants.TBL_RELATIONS, COL_TASK, null,
            relation, entry.getValue()));
      }
    }
    if (!BeeUtils.isEmpty(children)) {
      count = deb.commitChildren(taskId, children, response);
    }
    return response.setResponse(count);
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

    TaskEvent event = EnumUtils.getEnumByName(TaskEvent.class, svc);
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

        if (reqInfo.hasParameter(VAR_TASK_VISITED)) {
          response = registerTaskEvent(taskId, currentUser, event, now);
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

        Long finishTime = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_TASK_FINISH_TIME));

        response = registerTaskEvent(taskId, currentUser, event, reqInfo, eventNote, finishTime,
            now);
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
      result.addAll(Lists.newArrayList(newTasks));
    }

    if (updTasks != null && updTasks.length > 0) {
      result.addAll(Lists.newArrayList(updTasks));
    }

    return ResponseObject.response(Joiner.on(BeeConst.CHAR_COMMA).join(result));
  }

  private ResponseObject getCompanyTimesReport(RequestInfo reqInfo) {
    LocalizableConstants constants = usr.getLocalizableConstants();
    boolean hideZeroTimes = false;

    SqlSelect companiesListQuery =
        new SqlSelect()
            .addFields(CommonsConstants.TBL_COMPANIES,
                sys.getIdName(CommonsConstants.TBL_COMPANIES))
            .addFields(CommonsConstants.TBL_COMPANIES, COL_NAME)
            .addField(CommonsConstants.TBL_COMPANY_TYPES, COL_NAME,
                CommonsConstants.ALS_COMPANY_TYPE)
            .addFrom(CommonsConstants.TBL_COMPANIES)
            .addFromLeft(
                CommonsConstants.TBL_COMPANY_TYPES,
                sys.joinTables(CommonsConstants.TBL_COMPANY_TYPES,
                    CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_COMPANY_TYPE))
            .setWhere(SqlUtils.sqlTrue())
            .addOrder(CommonsConstants.TBL_COMPANIES, COL_NAME);

    if (reqInfo.hasParameter(VAR_TASK_COMPANY)) {
      companiesListQuery.setWhere(SqlUtils.and(companiesListQuery.getWhere(), SqlUtils.inList(
          CommonsConstants.TBL_COMPANIES, sys.getIdName(CommonsConstants.TBL_COMPANIES), DataUtils
              .parseIdList(reqInfo.getParameter(VAR_TASK_COMPANY)))));
    }

    if (reqInfo.hasParameter(VAR_TASK_DURATION_HIDE_ZEROS)) {
      hideZeroTimes = true;
    }

    SimpleRowSet companiesListSet = qs.getData(companiesListQuery);
    SimpleRowSet result = new SimpleRowSet(new String[] {COL_NAME, COL_DURATION});
    long totalTimeMls = 0;

    result.addRow(new String[] {constants.client(), constants.crmSpentTime()});

    /* Register times in tasks without company */
    companiesListSet.addRow(new String[] {null, "—", null});

    for (int i = 0; i < companiesListSet.getNumberOfRows(); i++) {
      String compFullName =
          companiesListSet.getValue(i, COL_NAME)
              + (!BeeUtils.isEmpty(companiesListSet.getValue(i, CommonsConstants.ALS_COMPANY_TYPE))
                  ? ", " + companiesListSet.getValue(i, CommonsConstants.ALS_COMPANY_TYPE) : "");
      String dTime = "0:00";

      SqlSelect companyTimesQuery = new SqlSelect()
          .addFields(TBL_EVENT_DURATIONS, COL_DURATION)
          .addFrom(TBL_TASK_EVENTS)
          .addFromRight(TBL_EVENT_DURATIONS,
              sys.joinTables(TBL_EVENT_DURATIONS, TBL_TASK_EVENTS, COL_EVENT_DURATION))
          .addFromLeft(TBL_DURATION_TYPES,
              sys.joinTables(TBL_DURATION_TYPES, TBL_EVENT_DURATIONS, COL_DURATION_TYPE))
          .addFromLeft(TBL_TASKS,
              sys.joinTables(TBL_TASKS, TBL_TASK_EVENTS, COL_TASK))
          .addFromLeft(CommonsConstants.TBL_COMPANIES,
              sys.joinTables(CommonsConstants.TBL_COMPANIES, TBL_TASKS, COL_COMPANY))
          .addFromLeft(CommonsConstants.TBL_USERS,
              sys.joinTables(CommonsConstants.TBL_USERS, TBL_TASK_EVENTS, COL_PUBLISHER))
          .setWhere(
              SqlUtils.equals(CommonsConstants.TBL_COMPANIES, sys
                  .getIdName(CommonsConstants.TBL_COMPANIES), companiesListSet.getValue(i, sys
                  .getIdName(CommonsConstants.TBL_COMPANIES))));

      if (reqInfo.hasParameter(VAR_TASK_DURATION_DATE_FROM)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_DATE_FROM))) {
          companyTimesQuery.setWhere(SqlUtils.and(companyTimesQuery.getWhere(), SqlUtils
              .moreEqual(TBL_EVENT_DURATIONS, COL_DURATION_DATE, reqInfo
                  .getParameter(VAR_TASK_DURATION_DATE_FROM))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_DURATION_DATE_TO)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_DATE_TO))) {
          companyTimesQuery.setWhere(SqlUtils.and(companyTimesQuery.getWhere(), SqlUtils
              .lessEqual(TBL_EVENT_DURATIONS, COL_DURATION_DATE, reqInfo
                  .getParameter(VAR_TASK_DURATION_DATE_TO))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_PUBLISHER)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_PUBLISHER))) {
          companyTimesQuery.setWhere(SqlUtils.and(companyTimesQuery.getWhere(), SqlUtils.inList(
              CommonsConstants.TBL_USERS, sys.getIdName(CommonsConstants.TBL_USERS), DataUtils
                  .parseIdList(reqInfo.getParameter(VAR_TASK_PUBLISHER)))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_DURATION_TYPE)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_TYPE))) {
          companyTimesQuery.setWhere(SqlUtils.and(companyTimesQuery.getWhere(), SqlUtils.inList(
              TBL_DURATION_TYPES, sys.getIdName(TBL_DURATION_TYPES), DataUtils
                  .parseIdList(reqInfo.getParameter(VAR_TASK_DURATION_TYPE)))));
        }
      }

      SimpleRowSet companyTimes = qs.getData(companyTimesQuery);
      long dTimeMls = TimeUtils.parseTime(dTime);

      for (int j = 0; j < companyTimes.getNumberOfRows(); j++) {
        Long timeMls = TimeUtils.parseTime(companyTimes.getValue(j, companyTimes
            .getColumnIndex(COL_DURATION)));
        dTimeMls += timeMls;
      }

      totalTimeMls += dTimeMls;

      if (!(hideZeroTimes && dTimeMls <= 0)) {
        dTime = new DateTime(dTimeMls).toUtcTimeString();
        result.addRow(new String[] {compFullName, dTime});
      }
    }

    result.addRow(new String[] {constants.totalOf() + ":",
        new DateTime(totalTimeMls).toUtcTimeString()});

    ResponseObject resp = ResponseObject.response(result);
    return resp;
  }

  private ResponseObject getRequestFiles(Long requestId) {
    Assert.state(DataUtils.isId(requestId));

    SimpleRowSet data =
        qs.getData(new SqlSelect()
            .addFields(TBL_REQUEST_FILES, COL_FILE, COL_CAPTION)
            .addFields(CommonsConstants.TBL_FILES, CommonsConstants.COL_FILE_NAME,
                CommonsConstants.COL_FILE_SIZE, CommonsConstants.COL_FILE_TYPE)
            .addFrom(TBL_REQUEST_FILES)
            .addFromInner(CommonsConstants.TBL_FILES,
                sys.joinTables(CommonsConstants.TBL_FILES, TBL_REQUEST_FILES, COL_FILE))
            .setWhere(SqlUtils.equals(TBL_REQUEST_FILES, COL_REQUEST, requestId)));

    List<StoredFile> files = Lists.newArrayList();

    for (SimpleRow file : data) {
      StoredFile sf = new StoredFile(file.getLong(COL_FILE),
          BeeUtils.notEmpty(file.getValue(COL_CAPTION),
              file.getValue(CommonsConstants.COL_FILE_NAME)),
          file.getLong(CommonsConstants.COL_FILE_SIZE),
          file.getValue(CommonsConstants.COL_FILE_TYPE));

      sf.setIcon(ExtensionIcons.getIcon(sf.getName()));
      files.add(sf);
    }
    return ResponseObject.response(files);
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

      sf.setIcon(ExtensionIcons.getIcon(sf.getName()));
      result.add(sf);
    }

    return result;
  }

  private Multimap<String, Long> getTaskRelations(long taskId) {
    Multimap<String, Long> res = HashMultimap.create();

    for (String relation : CrmUtils.getRelations()) {
      Long[] ids =
          qs.getRelatedValues(CommonsConstants.TBL_RELATIONS, COL_TASK, taskId, relation);

      if (ids != null) {
        String property = CrmUtils.translateRelationToTaskProperty(relation);

        for (Long id : ids) {
          res.put(property, id);
        }
      }
    }
    return res;
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

  private ResponseObject getTypeHoursReport(RequestInfo reqInfo) {
    LocalizableConstants constants = usr.getLocalizableConstants();
    SqlSelect durationTypes = new SqlSelect()
        .addFrom(TBL_DURATION_TYPES)
        .addFields(TBL_DURATION_TYPES, COL_NAME)
        .setWhere(SqlUtils.sqlTrue())
        .addOrder(TBL_DURATION_TYPES, COL_NAME);

    boolean hideTimeZeros = false;

    if (reqInfo.hasParameter(VAR_TASK_DURATION_TYPE)) {
      durationTypes.setWhere(SqlUtils.and(durationTypes.getWhere(), SqlUtils.inList(
          TBL_DURATION_TYPES, sys.getIdName(TBL_DURATION_TYPES), DataUtils.parseIdList(reqInfo
              .getParameter(VAR_TASK_DURATION_TYPE)))));
    }

    if (reqInfo.hasParameter(VAR_TASK_DURATION_HIDE_ZEROS)) {
      hideTimeZeros = true;
    }

    SimpleRowSet dTypesList = qs.getData(durationTypes);
    SimpleRowSet result = new SimpleRowSet(new String[] {COL_NAME, COL_DURATION});

    result.addRow(new String[] {constants.crmDurationType(), constants.crmSpentTime()});
    Assert.notNull(dTypesList);

    long totalTimeMls = 0;

    for (int i = 0; i < dTypesList.getNumberOfRows(); i++) {
      String dType = dTypesList.getValue(i, dTypesList.getColumnIndex(COL_NAME));
      String dTime = "0:00";

      SqlSelect dTypeTime = new SqlSelect()
          .addFrom(TBL_TASK_EVENTS)
          .addFromRight(TBL_EVENT_DURATIONS,
              sys.joinTables(TBL_EVENT_DURATIONS, TBL_TASK_EVENTS, COL_EVENT_DURATION))
          .addFromLeft(TBL_DURATION_TYPES,
              sys.joinTables(TBL_DURATION_TYPES, TBL_EVENT_DURATIONS, COL_DURATION_TYPE))
          .addFromLeft(TBL_TASKS,
              sys.joinTables(TBL_TASKS, TBL_TASK_EVENTS, COL_TASK))
          .addFromLeft(CommonsConstants.TBL_COMPANIES,
              sys.joinTables(CommonsConstants.TBL_COMPANIES, TBL_TASKS, COL_COMPANY))
          .addFromLeft(CommonsConstants.TBL_USERS,
              sys.joinTables(CommonsConstants.TBL_USERS, TBL_TASK_EVENTS, COL_PUBLISHER))
          .addFields(TBL_DURATION_TYPES, COL_NAME)
          .addFields(TBL_EVENT_DURATIONS, COL_DURATION)
          .setWhere(SqlUtils.equals(TBL_DURATION_TYPES, COL_NAME, dType));

      if (reqInfo.hasParameter(VAR_TASK_DURATION_DATE_FROM)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_DATE_FROM))) {
          dTypeTime.setWhere(SqlUtils.and(dTypeTime.getWhere(), SqlUtils
              .moreEqual(TBL_EVENT_DURATIONS, COL_DURATION_DATE, reqInfo
                  .getParameter(VAR_TASK_DURATION_DATE_FROM))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_DURATION_DATE_TO)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_DATE_TO))) {
          dTypeTime.setWhere(SqlUtils.and(dTypeTime.getWhere(), SqlUtils
              .lessEqual(TBL_EVENT_DURATIONS, COL_DURATION_DATE, reqInfo
                  .getParameter(VAR_TASK_DURATION_DATE_TO))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_COMPANY)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_COMPANY))) {
          dTypeTime.setWhere(SqlUtils.and(dTypeTime.getWhere(), SqlUtils.inList(
              CommonsConstants.TBL_COMPANIES, sys.getIdName(CommonsConstants.TBL_COMPANIES),
              DataUtils.parseIdList(reqInfo.getParameter(VAR_TASK_COMPANY)))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_PUBLISHER)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_PUBLISHER))) {
          dTypeTime.setWhere(SqlUtils.and(dTypeTime.getWhere(), SqlUtils.inList(
              CommonsConstants.TBL_USERS, sys.getIdName(CommonsConstants.TBL_USERS), DataUtils
                  .parseIdList(reqInfo.getParameter(VAR_TASK_PUBLISHER)))));
        }
      }

      SimpleRowSet dTypeTimes = qs.getData(dTypeTime);
      Assert.notNull(dTypeTimes);

      long dTimeMls = TimeUtils.parseTime(dTime);

      for (int j = 0; j < dTypeTimes.getNumberOfRows(); j++) {
        Long timeMls = TimeUtils.parseTime(dTypeTimes.getValue(j, dTypeTimes
            .getColumnIndex(COL_DURATION)));
        dTimeMls += timeMls;
      }

      totalTimeMls += dTimeMls;

      if (!(hideTimeZeros && dTimeMls <= 0)) {
        dTime = new DateTime(dTimeMls).toUtcTimeString();
        result.addRow(new String[] {dType, dTime});
      }
    }

    result.addRow(new String[] {
        constants.totalOf() + ":", new DateTime(totalTimeMls).toUtcTimeString()});

    ResponseObject resp = ResponseObject.response(result);
    return resp;
  }

  private ResponseObject getUsersHoursReport(RequestInfo reqInfo) {
    LocalizableConstants constants = usr.getLocalizableConstants();
    SqlSelect userListQuery =
        new SqlSelect()
            .addFields(CommonsConstants.TBL_USERS, sys.getIdName(CommonsConstants.TBL_USERS))
            .addFields(CommonsConstants.TBL_PERSONS,
                CommonsConstants.COL_FIRST_NAME,
                CommonsConstants.COL_LAST_NAME)
            .addFrom(CommonsConstants.TBL_USERS)
            .addFromLeft(
                CommonsConstants.TBL_COMPANY_PERSONS,
                sys.joinTables(CommonsConstants.TBL_COMPANY_PERSONS, CommonsConstants.TBL_USERS,
                    CommonsConstants.COL_COMPANY_PERSON))
            .addFromLeft(
                CommonsConstants.TBL_PERSONS,
                sys.joinTables(CommonsConstants.TBL_PERSONS, CommonsConstants.TBL_COMPANY_PERSONS,
                    CommonsConstants.COL_PERSON)).setWhere(SqlUtils.sqlTrue())
            .setWhere(SqlUtils.sqlTrue())
            .addOrder(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_FIRST_NAME,
                CommonsConstants.COL_LAST_NAME);

    boolean hideTimeZeros = false;

    if (reqInfo.hasParameter(VAR_TASK_PUBLISHER)) {
      userListQuery.setWhere(SqlUtils.and(userListQuery.getWhere(), SqlUtils.inList(
          CommonsConstants.TBL_USERS, sys.getIdName(CommonsConstants.TBL_USERS), DataUtils
              .parseIdList(reqInfo.getParameter(VAR_TASK_PUBLISHER)))));
    }

    if (reqInfo.hasParameter(VAR_TASK_DURATION_HIDE_ZEROS)) {
      hideTimeZeros = true;
    }

    SimpleRowSet usersListSet = qs.getData(userListQuery);
    SimpleRowSet result = new SimpleRowSet(new String[] {COL_NAME, COL_DURATION});
    long totalTimeMls = 0;

    result.addRow(new String[] {
        constants.userFullName(), constants.crmSpentTime()});

    for (int i = 0; i < usersListSet.getNumberOfRows(); i++) {
      String userFullName =
          (!BeeUtils.isEmpty(usersListSet.getValue(i, CommonsConstants.COL_FIRST_NAME))
              ? usersListSet.getValue(i, CommonsConstants.COL_FIRST_NAME) : "") + " "
              + (!BeeUtils.isEmpty(usersListSet.getValue(i, CommonsConstants.COL_LAST_NAME))
                  ? usersListSet.getValue(i, CommonsConstants.COL_LAST_NAME) : "");

      userFullName = BeeUtils.isEmpty(userFullName) ? "—" : userFullName;
      String dTime = "0:00";

      SqlSelect userTimesQuery = new SqlSelect()
          .addFields(TBL_EVENT_DURATIONS, COL_DURATION)
          .addFrom(TBL_TASK_EVENTS)
          .addFromRight(TBL_EVENT_DURATIONS,
              sys.joinTables(TBL_EVENT_DURATIONS, TBL_TASK_EVENTS, COL_EVENT_DURATION))
          .addFromLeft(TBL_DURATION_TYPES,
              sys.joinTables(TBL_DURATION_TYPES, TBL_EVENT_DURATIONS, COL_DURATION_TYPE))
          .addFromLeft(TBL_TASKS,
              sys.joinTables(TBL_TASKS, TBL_TASK_EVENTS, COL_TASK))
          .addFromLeft(CommonsConstants.TBL_COMPANIES,
              sys.joinTables(CommonsConstants.TBL_COMPANIES, TBL_TASKS, COL_COMPANY))
          .addFromLeft(CommonsConstants.TBL_USERS,
              sys.joinTables(CommonsConstants.TBL_USERS, TBL_TASK_EVENTS, COL_PUBLISHER))
          .setWhere(
              SqlUtils.equals(CommonsConstants.TBL_USERS, sys
                  .getIdName(CommonsConstants.TBL_USERS), usersListSet.getValue(i, sys
                  .getIdName(CommonsConstants.TBL_USERS))));

      if (reqInfo.hasParameter(VAR_TASK_DURATION_DATE_FROM)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_DATE_FROM))) {
          userTimesQuery.setWhere(SqlUtils.and(userTimesQuery.getWhere(), SqlUtils
              .moreEqual(TBL_EVENT_DURATIONS, COL_DURATION_DATE, reqInfo
                  .getParameter(VAR_TASK_DURATION_DATE_FROM))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_DURATION_DATE_TO)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_DATE_TO))) {
          userTimesQuery.setWhere(SqlUtils.and(userTimesQuery.getWhere(), SqlUtils
              .lessEqual(TBL_EVENT_DURATIONS, COL_DURATION_DATE, reqInfo
                  .getParameter(VAR_TASK_DURATION_DATE_TO))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_COMPANY)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_COMPANY))) {
          userTimesQuery.setWhere(SqlUtils.and(userTimesQuery.getWhere(), SqlUtils.inList(
              CommonsConstants.TBL_COMPANIES, sys.getIdName(CommonsConstants.TBL_COMPANIES),
              DataUtils.parseIdList(reqInfo.getParameter(VAR_TASK_COMPANY)))));
        }
      }

      if (reqInfo.hasParameter(VAR_TASK_DURATION_TYPE)) {
        if (!BeeUtils.isEmpty(reqInfo.getParameter(VAR_TASK_DURATION_TYPE))) {
          userTimesQuery.setWhere(SqlUtils.and(userTimesQuery.getWhere(), SqlUtils.inList(
              TBL_DURATION_TYPES, sys.getIdName(TBL_DURATION_TYPES), DataUtils
                  .parseIdList(reqInfo.getParameter(VAR_TASK_DURATION_TYPE)))));
        }
      }

      SimpleRowSet companyTimes = qs.getData(userTimesQuery);
      long dTimeMls = TimeUtils.parseTime(dTime);

      for (int j = 0; j < companyTimes.getNumberOfRows(); j++) {
        Long timeMls = TimeUtils.parseTime(companyTimes.getValue(j, companyTimes
            .getColumnIndex(COL_DURATION)));
        dTimeMls += timeMls;
      }

      totalTimeMls += dTimeMls;

      if (!(hideTimeZeros && dTimeMls <= 0)) {
        dTime = new DateTime(dTimeMls).toUtcTimeString();
        result.addRow(new String[] {userFullName, dTime});
      }
    }

    result.addRow(new String[] {
        constants.totalOf() + ":", new DateTime(totalTimeMls).toUtcTimeString()});

    ResponseObject resp = ResponseObject.response(result);
    return resp;
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
    return registerTaskEvent(taskId, userId, event, null, null, null, null, millis);
  }

  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event,
      RequestInfo reqInfo, String note, Long finishTime, long millis) {
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

    return registerTaskEvent(taskId, userId, event, comment, note, finishTime, durationId, millis);
  }

  private ResponseObject registerTaskEvent(long taskId, long userId, TaskEvent event,
      String comment, String note, Long finishTime, Long durationId, long millis) {

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

    if (BeeUtils.isPositive(finishTime)) {
      si.addConstant(COL_FINISH_TIME, finishTime);
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

  private ResponseObject updateTaskRelations(long taskId, Set<String> updatedRelations,
      BeeRow row) {
    ResponseObject response = new ResponseObject();
    List<RowChildren> children = Lists.newArrayList();

    for (String property : updatedRelations) {
      String relation = CrmUtils.translateTaskPropertyToRelation(property);

      if (!BeeUtils.isEmpty(relation)) {
        children.add(RowChildren.create(CommonsConstants.TBL_RELATIONS, COL_TASK, taskId,
            relation, row.getProperty(property)));
      }
    }
    int count = 0;

    if (!BeeUtils.isEmpty(children)) {
      count = deb.commitChildren(taskId, children, response);
    }
    return response.setResponse(count);
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
