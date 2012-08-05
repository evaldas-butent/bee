package com.butent.bee.server.modules.crm;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.ProjectEvent;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CrmModuleBean implements BeeModule {

  private static final Splitter USER_ID_SPLITTER =
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();

  private static Logger logger = Logger.getLogger(CrmModuleBean.class.getName());

  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @Resource
  EJBContext ctx;

  @Override
  public String dependsOn() {
    return CommonsConstants.COMMONS_MODULE;
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CrmConstants.CRM_METHOD);

    if (BeeUtils.isPrefix(svc, CrmConstants.CRM_PROJECT_PREFIX)) {
      response =
          doProjectEvent(BeeUtils.removePrefix(svc, CrmConstants.CRM_PROJECT_PREFIX), reqInfo);

    } else if (BeeUtils.isPrefix(svc, CrmConstants.CRM_TASK_PREFIX)) {
      response = doTaskEvent(BeeUtils.removePrefix(svc, CrmConstants.CRM_TASK_PREFIX), reqInfo);

    } else {
      String msg = BeeUtils.concat(1, "CRM service not recognized:", svc);
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
    return CrmConstants.CRM_MODULE;
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

    if (BeeUtils.same(svc, CrmConstants.SVC_ADD_OBSERVERS)) {
      int cnt = 0;
      long projectId = BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_PROJECT_ID));
      String observers = reqInfo.getParameter(CrmConstants.VAR_PROJECT_OBSERVERS);

      for (String obsId : USER_ID_SPLITTER.split(observers)) {
        response = registerProjectVisit(projectId, BeeUtils.toLong(obsId), null, true);
        if (response.hasErrors()) {
          break;
        }
        cnt++;
      }
      if (!response.hasErrors()) {
        response = ResponseObject.response(cnt);
      }
    } else if (BeeUtils.same(svc, CrmConstants.SVC_REMOVE_OBSERVERS)) {
      long projectId = BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_PROJECT_ID));
      String observers = reqInfo.getParameter(CrmConstants.VAR_PROJECT_OBSERVERS);

      HasConditions usrClause = SqlUtils.or();
      IsCondition cond = SqlUtils.and(SqlUtils.equal(CrmConstants.TBL_PROJECT_USERS,
          CrmConstants.COL_PROJECT, projectId), usrClause);

      for (String obsId : USER_ID_SPLITTER.split(observers)) {
        usrClause.add(SqlUtils.equal(CrmConstants.TBL_PROJECT_USERS, CrmConstants.COL_USER,
            BeeUtils.toLong(obsId)));
      }
      response = qs.updateDataWithResponse(new SqlDelete(CrmConstants.TBL_PROJECT_USERS)
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
            BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_PROJECT_DATA));
            String observers = reqInfo.getParameter(CrmConstants.VAR_PROJECT_OBSERVERS);
            response = deb.commitRow(rs, false);

            if (!response.hasErrors()) {
              long projectId = ((BeeRow) response.getResponse()).getId();
              response = registerProjectEvent(projectId, time, reqInfo, event, null);

              if (!response.hasErrors() && !BeeUtils.isEmpty(observers)) {
                for (String obsId : USER_ID_SPLITTER.split(observers)) {
                  long observer = BeeUtils.toLong(obsId);

                  if (observer != currentUser) {
                    response = registerProjectVisit(projectId, observer, null, true);

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
            response = registerProjectVisit(
                BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_PROJECT_ID)),
                currentUser, time, false);
            break;

          case COMMENTED:
            response = registerProjectEvent(
                BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_PROJECT_ID)),
                time, reqInfo, event, null);
            break;

          case ACTIVATED:
          case SUSPENDED:
          case CANCELED:
          case COMPLETED:
          case RENEWED:
            rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_PROJECT_DATA));
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
            // TODO
            break;
        }
      }
    }
    if (response == null) {
      String msg = BeeUtils.concat(1, "Project service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);

    } else if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private ResponseObject doTaskEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;
    TaskEvent event;

    try {
      event = TaskEvent.valueOf(svc);
    } catch (Exception e) {
      event = null;
    }
    if (event != null) {
      long time = System.currentTimeMillis();
      long currentUser = usr.getCurrentUserId();

      switch (event) {
        case ACTIVATED:
          BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
          String executors = reqInfo.getParameter(CrmConstants.VAR_TASK_EXECUTORS);
          String observers = reqInfo.getParameter(CrmConstants.VAR_TASK_OBSERVERS);

          int exIndex = DataUtils.getColumnIndex(CrmConstants.COL_EXECUTOR, rs.getColumns());
          int cnt = 0;

          for (String exId : USER_ID_SPLITTER.split(executors)) {
            rs.getRow(0).setValue(exIndex, exId);

            response = deb.commitRow(rs, false);

            if (!response.hasErrors()) {
              cnt++;
              long taskId = ((BeeRow) response.getResponse()).getId();
              long executor = BeeUtils.toLong(exId);
              response = registerTaskEvent(taskId, time, reqInfo, event, null);

              if (!response.hasErrors() && executor != currentUser) {
                response = registerTaskVisit(taskId, executor, null, true);
              }
              if (!response.hasErrors() && !BeeUtils.isEmpty(observers)) {
                for (String obsId : USER_ID_SPLITTER.split(observers)) {
                  long observer = BeeUtils.toLong(obsId);

                  if (observer != currentUser && observer != executor) {
                    response = registerTaskVisit(taskId, observer, null, true);

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
          response =
              registerTaskVisit(BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_ID)),
                  currentUser, time, false);
          break;

        case COMMENTED:
          response =
              registerTaskEvent(BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_ID)),
                  time, reqInfo, event, null);
          break;

        case FORWARDED:
          rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
          long taskId = rs.getRow(0).getId();
          long oldUser = BeeUtils.toLong(rs.getShadowString(0, CrmConstants.COL_EXECUTOR));
          long newUser = BeeUtils.toLong(rs.getString(0, CrmConstants.COL_EXECUTOR));
          IsCondition wh =
              SqlUtils.equal(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_TASK, taskId);

          response = deb.commitRow(rs, false);

          if (!response.hasErrors()
              && !qs.sqlExists(CrmConstants.TBL_TASK_USERS, SqlUtils.and(wh,
                  SqlUtils.equal(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_USER, newUser)))) {
            response = registerTaskVisit(taskId, newUser, null, true);
          }
          if (!response.hasErrors()) {
            response = registerTaskEvent(taskId, time, reqInfo, event,
                BeeUtils.concat(" -> ", usr.getUserSign(oldUser), usr.getUserSign(newUser)));

            if (!response.hasErrors()
                && !BeeUtils.isEmpty(reqInfo.getParameter(CrmConstants.VAR_TASK_OBSERVE))) {
              qs.updateData(new SqlDelete(CrmConstants.TBL_TASK_USERS)
                  .setWhere(SqlUtils.and(wh,
                      SqlUtils.equal(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_USER, oldUser))));
            }
          }
          break;

        case EXTENDED:
          rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
          taskId = rs.getRow(0).getId();
          String oldTerm = rs.getShadowString(0, "FinishTime");
          String newTerm = rs.getString(0, "FinishTime");

          response = deb.commitRow(rs, false);

          if (!response.hasErrors()) {
            response = registerTaskEvent(taskId, time, reqInfo, event,
                BeeUtils.concat(" -> ",
                    TimeUtils.toDateTimeOrNull(oldTerm), TimeUtils.toDateTimeOrNull(newTerm)));
          }
          break;

        case UPDATED:
          rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
          taskId = rs.getRow(0).getId();
          Map<String, String> prm = Maps.newHashMap();
          reqInfo.setParams(prm);

          for (BeeColumn col : rs.getColumns()) {
            String colName = col.getId();
            String oldValue = rs.getShadowString(0, colName);
            String value = rs.getString(0, colName);
            prm.put(CrmConstants.VAR_TASK_COMMENT, BeeUtils.concat(" -> ", oldValue, value));

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
          rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
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
    } else {
      String msg = BeeUtils.concat(1, "Task service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  private ResponseObject registerProjectEvent(long projectId, long time, RequestInfo reqInfo,
      ProjectEvent event, String eventNote) {
    ResponseObject response = null;
    long currentUser = usr.getCurrentUserId();

    response = qs.insertDataWithResponse(new SqlInsert("ProjectEvents")
        .addConstant(CrmConstants.COL_PROJECT, projectId)
        .addConstant("Publisher", currentUser)
        .addConstant("PublishTime", time)
        .addConstant("Comment", reqInfo.getParameter(CrmConstants.VAR_PROJECT_COMMENT))
        .addConstant(CrmConstants.COL_EVENT, event.ordinal())
        .addConstant("EventNote", eventNote));

    if (!response.hasErrors()) {
      response = registerProjectVisit(projectId, currentUser, time,
          BeeUtils.equals(event, ProjectEvent.CREATED));
    }
    return response;
  }

  private ResponseObject registerProjectVisit(long projectId, long userId, Long time,
      boolean newVisit) {
    ResponseObject response = null;

    if (!newVisit) {
      response = qs.updateDataWithResponse(new SqlUpdate(CrmConstants.TBL_PROJECT_USERS)
          .addConstant(CrmConstants.COL_LAST_ACCESS, time)
          .setWhere(SqlUtils.and(
              SqlUtils.equal(CrmConstants.TBL_PROJECT_USERS, CrmConstants.COL_PROJECT, projectId),
              SqlUtils.equal(CrmConstants.TBL_PROJECT_USERS, CrmConstants.COL_USER, userId))));
    }
    if (newVisit || BeeUtils.isEmpty(response.getResponse(-1, logger))) {
      response = qs.insertDataWithResponse(new SqlInsert(CrmConstants.TBL_PROJECT_USERS)
          .addConstant(CrmConstants.COL_PROJECT, projectId)
          .addConstant(CrmConstants.COL_USER, userId)
          .addConstant(CrmConstants.COL_LAST_ACCESS, time));
    }
    if (!response.hasErrors()) {
      response = ResponseObject.response(time);
    }
    return response;
  }

  private ResponseObject registerTaskEvent(long taskId, long time, RequestInfo reqInfo,
      TaskEvent event, String eventNote) {
    ResponseObject response = null;

    int minutes = BeeUtils.toInt(reqInfo.getParameter(CrmConstants.VAR_TASK_DURATION_TIME));

    if (BeeUtils.isPositive(minutes)) {
      response = qs.insertDataWithResponse(new SqlInsert("EventDurations")
          .addConstant("DurationDate",
              BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_DURATION_DATE)))
          .addConstant("DurationType",
              BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_DURATION_TYPE)))
          .addConstant("Duration", minutes));
    }
    long currentUser = usr.getCurrentUserId();

    if (response == null || !response.hasErrors()) {
      response = qs.insertDataWithResponse(new SqlInsert("TaskEvents")
          .addConstant(CrmConstants.COL_TASK, taskId)
          .addConstant("Publisher", currentUser)
          .addConstant("PublishTime", time)
          .addConstant("Comment", reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT))
          .addConstant(CrmConstants.COL_EVENT, event.ordinal())
          .addConstant("EventNote", eventNote)
          .addConstant("EventDuration", response == null ? null : (Long) response.getResponse()));
    }
    if (!response.hasErrors()) {
      response =
          registerTaskVisit(taskId, currentUser, time, BeeUtils.equals(event, TaskEvent.ACTIVATED));
    }
    return response;
  }

  private ResponseObject registerTaskVisit(long taskId, long userId, Long time, boolean newVisit) {
    ResponseObject response = null;

    if (!newVisit) {
      response =
          qs.updateDataWithResponse(new SqlUpdate(CrmConstants.TBL_TASK_USERS)
              .addConstant(CrmConstants.COL_LAST_ACCESS, time)
              .setWhere(SqlUtils.and(
                  SqlUtils.equal(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_TASK, taskId),
                  SqlUtils.equal(CrmConstants.TBL_TASK_USERS, CrmConstants.COL_USER, userId))));
    }
    if (newVisit || BeeUtils.isEmpty(response.getResponse(-1, logger))) {
      response = qs.insertDataWithResponse(new SqlInsert(CrmConstants.TBL_TASK_USERS)
          .addConstant(CrmConstants.COL_TASK, taskId)
          .addConstant(CrmConstants.COL_USER, userId)
          .addConstant(CrmConstants.COL_LAST_ACCESS, time));
    }
    if (!response.hasErrors()) {
      response = ResponseObject.response(time);
    }
    return response;
  }
}
