package com.butent.bee.server.modules.crm;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CrmModuleBean implements BeeModule {

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
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CrmConstants.CRM_METHOD);

    TaskEvent ev;
    try {
      ev = TaskEvent.valueOf(svc);
    } catch (Exception e) {
      ev = null;
    }
    if (ev != null) {
      response = doTaskEvent(ev, reqInfo);

    } else {
      String msg = BeeUtils.concat(1, "CRM service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public String getName() {
    return CrmConstants.CRM_MODULE;
  }

  private ResponseObject doTaskEvent(TaskEvent event, RequestInfo reqInfo) {
    ResponseObject response = null;
    long time = System.currentTimeMillis();

    switch (event) {
      case ACTIVATED:
        BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
        rs.preliminaryUpdate(0, "EventTime", BeeUtils.toString(time));
        response = deb.commitRow(rs, true);

        if (!response.hasErrors()) {
          ResponseObject resp = registerTaskEvent(((BeeRow) response.getResponse()).getId(), time,
              reqInfo, event, null);

          if (resp.hasErrors()) {
            response = resp;
          }
        }
        break;

      case VISITED:
        response = registerTaskVisit(
            BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_ID)), time, false);
        break;

      case COMMENTED:
        response =
            registerTaskEvent(BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_ID)),
                time, reqInfo, event, null);
        break;

      case FORWARDED:
        rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
        response = deb.commitRow(rs, true);

        if (!response.hasErrors()) {
          long taskId = rs.getRow(0).getId();
          long oldUser = BeeUtils.toLong(rs.getShadowString(0, "Executor"));
          long newUser = BeeUtils.toLong(rs.getString(0, "Executor"));

          ResponseObject resp = registerTaskEvent(taskId, time, reqInfo, event,
              BeeUtils.concat(" -> ", usr.getUserSign(oldUser), usr.getUserSign(newUser)));

          if (resp.hasErrors()) {
            response = resp;
          } else if (BeeUtils.isEmpty(reqInfo.getParameter(CrmConstants.VAR_TASK_OBSERVE))) {
            String tbl = "TaskUsers";
            qs.updateData(new SqlDelete(tbl)
                .setWhere(SqlUtils.and(SqlUtils.equal(tbl, "Task", taskId),
                    SqlUtils.equal(tbl, "User", oldUser))));
          }
        }
        break;

      case EXTENDED:
        rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
        response = deb.commitRow(rs, true);

        if (!response.hasErrors()) {
          String oldTerm = rs.getShadowString(0, "FinishTime");
          String newTerm = rs.getString(0, "FinishTime");

          ResponseObject resp = registerTaskEvent(rs.getRow(0).getId(), time, reqInfo, event,
              BeeUtils.concat(" -> ",
                  TimeUtils.toDateTimeOrNull(oldTerm), TimeUtils.toDateTimeOrNull(newTerm)));

          if (resp.hasErrors()) {
            response = resp;
          }
        }
        break;

      case SUSPENDED:
      case CANCELED:
      case COMPLETED:
      case APPROVED:
      case RENEWED:
        rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
        rs.preliminaryUpdate(0, "EventTime", BeeUtils.toString(time));
        response = deb.commitRow(rs, true);

        if (!response.hasErrors()) {
          ResponseObject resp = registerTaskEvent(rs.getRow(0).getId(), time, reqInfo, event, null);

          if (resp.hasErrors()) {
            response = resp;
          }
        }
        break;
    }
    if (response.hasErrors()) {
      ctx.setRollbackOnly();
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
          .addConstant("DurationInMinutes", minutes));
    }
    if (response == null || !response.hasErrors()) {
      response = qs.insertDataWithResponse(new SqlInsert("TaskEvents")
          .addConstant("Task", taskId)
          .addConstant("Publisher", usr.getCurrentUserId())
          .addConstant("PublishTime", time)
          .addConstant("Comment", reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT))
          .addConstant("Event", event.ordinal())
          .addConstant("EventNote", eventNote)
          .addConstant("EventDuration", response == null ? null : (Long) response.getResponse()));
    }
    if (!response.hasErrors()) {
      response = registerTaskVisit(taskId, time, BeeUtils.equals(event, TaskEvent.ACTIVATED));
    }
    return response;
  }

  private ResponseObject registerTaskVisit(long taskId, long time, boolean newVisit) {
    ResponseObject response = null;
    String tbl = "TaskUsers";
    long userId = usr.getCurrentUserId();

    if (!newVisit) {
      response = qs.updateDataWithResponse(new SqlUpdate(tbl)
          .addConstant("LastAccess", time)
          .setWhere(SqlUtils.and(SqlUtils.equal(tbl, "Task", taskId),
              SqlUtils.equal(tbl, "User", userId))));
    }
    if (newVisit || BeeUtils.isEmpty(response.getResponse(-1, logger))) {
      response = qs.insertDataWithResponse(new SqlInsert(tbl)
          .addConstant("Task", taskId)
          .addConstant("User", userId)
          .addConstant("LastAccess", time));
    }
    return response;
  }
}
