package com.butent.bee.server.modules.crm;

import com.google.common.base.Objects;
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
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
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

    if (BeeUtils.isPrefix(svc, CRM_PROJECT_PREFIX)) {
      response = doProjectEvent(BeeUtils.removePrefix(svc, CRM_PROJECT_PREFIX), reqInfo);

    } else if (BeeUtils.isPrefix(svc, CRM_TASK_PREFIX)) {
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

  private ResponseObject doProjectEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_ADD_OBSERVERS)) {
      int cnt = 0;
      long projectId = BeeUtils.toLong(reqInfo.getParameter(VAR_PROJECT_ID));
      String observers = reqInfo.getParameter(VAR_PROJECT_OBSERVERS);

      for (long obsId : DataUtils.parseIdList(observers)) {
        response = registerProjectVisit(projectId, obsId, null, true);
        if (response.hasErrors()) {
          break;
        }
        cnt++;
      }
      if (!response.hasErrors()) {
        response = ResponseObject.response(cnt);
      }
    } else if (BeeUtils.same(svc, SVC_REMOVE_OBSERVERS)) {
      long projectId = BeeUtils.toLong(reqInfo.getParameter(VAR_PROJECT_ID));
      String observers = reqInfo.getParameter(VAR_PROJECT_OBSERVERS);

      HasConditions usrClause = SqlUtils.or();
      IsCondition cond = SqlUtils.and(SqlUtils.equal(TBL_PROJECT_USERS, COL_PROJECT, projectId),
          usrClause);

      for (long obsId : DataUtils.parseIdList(observers)) {
        usrClause.add(SqlUtils.equal(TBL_PROJECT_USERS, COL_USER, obsId));
      }
      response = qs.updateDataWithResponse(new SqlDelete(TBL_PROJECT_USERS)
          .setWhere(cond));

    } else {
      long time = System.currentTimeMillis();
      long currentUser = usr.getCurrentUserId();
      ProjectEvent event;

      try {
        event = ProjectEvent.valueOf(svc);
      } catch (Exception e) {
        event = null;
      }
      if (event != null) {
        switch (event) {
          case CREATED:
            BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(VAR_PROJECT_DATA));
            String observers = reqInfo.getParameter(VAR_PROJECT_OBSERVERS);
            response = deb.commitRow(rs, false);

            if (!response.hasErrors()) {
              long projectId = ((BeeRow) response.getResponse()).getId();
              response = registerProjectEvent(projectId, time, reqInfo, event, null);

              if (!response.hasErrors() && !BeeUtils.isEmpty(observers)) {
                for (long obsId : DataUtils.parseIdList(observers)) {
                  if (obsId != currentUser) {
                    response = registerProjectVisit(projectId, obsId, null, true);
                    if (response.hasErrors()) {
                      break;
                    }
                  }
                }
              }
              if (!response.hasErrors()) {
                rs = qs.getViewData(rs.getViewName(), ComparisonFilter.compareId(projectId));

                if (rs.isEmpty()) {
                  String msg = "Optimistic lock exception";
                  logger.warning(msg);
                  response = ResponseObject.error(msg);
                } else {
                  response.setResponse(rs.getRow(0));
                }
              }
            }
            break;

          case VISITED:
            response = registerProjectVisit(BeeUtils.toLong(reqInfo.getParameter(VAR_PROJECT_ID)),
                currentUser, time, false);
            break;

          case COMMENTED:
            response = registerProjectEvent(BeeUtils.toLong(reqInfo.getParameter(VAR_PROJECT_ID)),
                time, reqInfo, event, null);
            break;

          case ACTIVATED:
          case SUSPENDED:
          case CANCELED:
          case COMPLETED:
          case RENEWED:
            rs = BeeRowSet.restore(reqInfo.getParameter(VAR_PROJECT_DATA));
            long projectId = rs.getRow(0).getId();

            response = deb.commitRow(rs, false);

            if (!response.hasErrors()) {
              response = registerProjectEvent(projectId, time, reqInfo, event, null);
            }
            break;

          case DELETED:
            Assert.untouchable();
            break;

          case EXTENDED:
          case UPDATED:
            break;
        }
      }
    }
    if (response == null) {
      String msg = BeeUtils.joinWords("Project service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);

    } else if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
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
        String executors = reqInfo.getParameter(VAR_TASK_EXECUTORS);
        String observers = reqInfo.getParameter(VAR_TASK_OBSERVERS);

        int exIndex = DataUtils.getColumnIndex(COL_EXECUTOR, rs.getColumns());
        int cnt = 0;
        
        BeeRow row = rs.getRow(0);

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

              if (response.hasErrors()) {
                break;
              }
            }
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
          response = ResponseObject.response(getTaskUsers(taskId, null));
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

      case UPDATED:
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

  private String getTaskUsers(long taskId, List<Long> exclude) {
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

  private ResponseObject registerProjectEvent(long projectId, long time, RequestInfo reqInfo,
      ProjectEvent event, String eventNote) {
    ResponseObject response = null;
    long currentUser = usr.getCurrentUserId();

    response = qs.insertDataWithResponse(new SqlInsert("ProjectEvents")
        .addConstant(COL_PROJECT, projectId)
        .addConstant("Publisher", currentUser)
        .addConstant("PublishTime", time)
        .addConstant("Comment", reqInfo.getParameter(VAR_PROJECT_COMMENT))
        .addConstant(COL_EVENT, event.ordinal())
        .addConstant("EventNote", eventNote));

    if (!response.hasErrors()) {
      response = registerProjectVisit(projectId, currentUser, time,
          Objects.equal(event, ProjectEvent.CREATED));
    }
    return response;
  }

  private ResponseObject registerProjectVisit(long projectId, long userId, Long time,
      boolean newVisit) {
    ResponseObject response = null;

    if (!newVisit) {
      response = qs.updateDataWithResponse(new SqlUpdate(TBL_PROJECT_USERS)
          .addConstant(COL_LAST_ACCESS, time)
          .setWhere(SqlUtils.and(
              SqlUtils.equal(TBL_PROJECT_USERS, COL_PROJECT, projectId),
              SqlUtils.equal(TBL_PROJECT_USERS, COL_USER, userId))));
    }
    if (newVisit || response.getResponse(-1, logger) == 0) {
      response = qs.insertDataWithResponse(new SqlInsert(TBL_PROJECT_USERS)
          .addConstant(COL_PROJECT, projectId)
          .addConstant(COL_USER, userId)
          .addConstant(COL_LAST_ACCESS, time));
    }
    if (!response.hasErrors()) {
      response = ResponseObject.response(time);
    }
    return response;
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
}
