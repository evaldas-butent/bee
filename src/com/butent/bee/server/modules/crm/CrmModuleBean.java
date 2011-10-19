package com.butent.bee.server.modules.crm;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.logging.Logger;

import javax.ejb.EJB;
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

    switch (event) {
      case COMMENTED:
        response = registerTaskEvent(
            BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_ID)),
            reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT), event, null);
        break;

      case FORWARDED:
        BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
        response = deb.commitRow(rs, true);

        if (!response.hasErrors()) {
          long oldUser = BeeUtils.toLong(rs.getShadowString(0, "Executor"));
          long newUser = BeeUtils.toLong(rs.getString(0, "Executor"));

          registerTaskEvent(rs.getRow(0).getId(),
              reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT), event,
              BeeUtils.concat(" -> ", usr.getUserSign(oldUser), usr.getUserSign(newUser)));
        }
        break;

      case EXTENDED:
        rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
        response = deb.commitRow(rs, true);

        if (!response.hasErrors()) {
          String oldTerm = rs.getShadowString(0, "FinishTime");
          String newTerm = rs.getString(0, "FinishTime");

          registerTaskEvent(rs.getRow(0).getId(),
              reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT), event,
              BeeUtils.concat(" -> ",
                  TimeUtils.toDateTimeOrNull(oldTerm), TimeUtils.toDateTimeOrNull(newTerm)));
        }
        break;

      case SUSPENDED:
      case CANCELED:
      case COMPLETED:
      case APPROVED:
      case RENEWED:
        rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));
        rs.preliminaryUpdate(0, "EventTime", BeeUtils.toString(System.currentTimeMillis()));
        response = deb.commitRow(rs, true);

        if (!response.hasErrors()) {
          registerTaskEvent(rs.getRow(0).getId(),
              reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT), event, null);
        }
        break;

      case ACTIVATED:
        String msg = BeeUtils.concat(1, "Task service not allowed:", event.name());
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }
    return response;
  }

  private ResponseObject registerTaskEvent(long taskId, String comment, TaskEvent event,
      String eventNote) {
    return qs.insertDataWithResponse(new SqlInsert("TaskEvents")
        .addConstant("Task", taskId)
        .addConstant("Publisher", usr.getCurrentUserId())
        .addConstant("PublishTime", System.currentTimeMillis())
        .addConstant("Comment", comment)
        .addConstant("Event", event.ordinal())
        .addConstant("EventNote", eventNote));
  }
}
