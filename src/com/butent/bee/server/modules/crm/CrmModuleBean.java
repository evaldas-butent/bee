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

    if (BeeUtils.same(svc, TaskEvent.COMMENTED.name())) {
      long id = BeeUtils.toLong(reqInfo.getParameter(CrmConstants.VAR_TASK_ID));
      String comment = reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT);

      response = qs.insertDataWithResponse(new SqlInsert("TaskEvents")
          .addConstant("Task", id)
          .addConstant("Publisher", usr.getCurrentUserId())
          .addConstant("PublishTime", System.currentTimeMillis())
          .addConstant("Comment", comment)
          .addConstant("Event", TaskEvent.COMMENTED.ordinal()));

    } else if (BeeUtils.same(svc, TaskEvent.EXTENDED.name())) {
      BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(CrmConstants.VAR_TASK_DATA));

      long oldTerm = rs.getLong(0, rs.getColumnIndex("FinishTime"));
      String term = reqInfo.getParameter(CrmConstants.VAR_TASK_TERM);
      String comment = reqInfo.getParameter(CrmConstants.VAR_TASK_COMMENT);

      rs.getRow(0).preliminaryUpdate(rs.getColumnIndex("FinishTime"), term);

      response = deb.commitRow(rs, true);

      if (!response.hasErrors()) {
        qs.insertData(new SqlInsert("TaskEvents")
            .addConstant("Task", rs.getRow(0).getId())
            .addConstant("Publisher", usr.getCurrentUserId())
            .addConstant("PublishTime", System.currentTimeMillis())
            .addConstant("Comment", comment)
            .addConstant("Event", TaskEvent.EXTENDED.ordinal())
            .addConstant("EventNote", BeeUtils.concat(" -> ",
                TimeUtils.toDateTimeOrNull(oldTerm), TimeUtils.toDateTimeOrNull(term))));
      }

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
}
