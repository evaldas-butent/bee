package com.butent.bee.server.modules.tasks;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.websocket.Endpoint;
import com.butent.bee.shared.Locality;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.FiresModificationEvents;
import com.butent.bee.shared.data.event.ModificationEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.websocket.messages.ModificationMessage;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/")
@Stateless

public class RestfulTaskBuilder {

  final BeeLogger logger = LogUtils.getLogger(RestfulTaskBuilder.class);
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  ParamHolderBean prm;
  @EJB
  DataEditorBean deb;

  @POST
  @Path("request")
  public String entry(@FormParam("name") String companyName,
      @FormParam("code") String companyCode, @FormParam("message") String message) {

    Long companyId = null;

    if (BeeUtils.isEmpty(companyName) && BeeUtils.isEmpty(companyCode)) {
      return "Bad request: Company or Code required"; /* CRM MUST RETURN company code or name or
      "NEŽINOMAS"
      value */
    }

    if (!BeeUtils.isEmpty(companyCode)) {
      companyId = qs.getId(ClassifierConstants.TBL_COMPANIES, ClassifierConstants
          .COL_COMPANY_CODE, companyCode);
    }

    if (!DataUtils.isId(companyId) && !BeeUtils.isEmpty(companyName)) {

      companyId = qs.getId(ClassifierConstants.TBL_COMPANIES, ClassifierConstants
          .COL_COMPANY_NAME, companyName);
    }

    if (!DataUtils.isId(companyId)) {
      logger.warning("Rest company ", companyName, companyCode, "not found");
      companyId = prm.getRelation(AdministrationConstants.PRM_COMPANY);

    }

    if (!DataUtils.isId(companyId)) {
      return "Rest Unknown request";
    }

    DataInfo reqDataInfo = sys.getDataInfo(TaskConstants.VIEW_REQUESTS);

    BeeRow row = DataUtils.createEmptyRow(reqDataInfo.getColumns().size());
    row.setValue(reqDataInfo.getColumnIndex(TaskConstants.COL_REQUEST_CUSTOMER), companyId);
    row.setValue(reqDataInfo.getColumnIndex(TaskConstants.COL_REQUEST_CONTENT), message);

    BeeRowSet reqRowSet = DataUtils.createRowSetForInsert(TaskConstants.VIEW_REQUESTS,
        reqDataInfo.getColumns(), row);

    ResponseObject resp = deb.commitRow(reqRowSet, RowInfo.class);

    if (!resp.hasResponse()) {
      return BeeUtils.joinWords("Rest internal error", resp.getErrors());
    }

    fireNotify();

    logger.info("Rest data commited", resp.getResponse());
    RowInfo rowInfo = (RowInfo) resp.getResponse();
    return BeeUtils.toString(rowInfo.getId());
  }

  private void fireNotify() {

    FiresModificationEvents commando = new FiresModificationEvents() {
      @Override
      public void fireModificationEvent(ModificationEvent<?> event, Locality locality) {
        SqlSelect usrSel = new SqlSelect().setDistinctMode(true)
            .addFields(NewsConstants.TBL_USER_FEEDS, NewsConstants.COL_UF_USER)
            .addFrom(NewsConstants.TBL_USER_FEEDS)
            .setWhere(SqlUtils.or(SqlUtils
                .equals(NewsConstants.TBL_USER_FEEDS, NewsConstants.COL_UF_FEED,
                    Feed.REQUESTS_ALL.name()), SqlUtils
                .equals(NewsConstants.TBL_USER_FEEDS, NewsConstants.COL_UF_FEED,
                    Feed.REQUESTS_ASSIGNED.name())));


        for (Long user : qs.getLongColumn(usrSel)) {
          Endpoint.sendToUser(user, new ModificationMessage(event));
        }
      }
    };

    DataChangeEvent.fireRefresh(commando, TaskConstants.VIEW_REQUESTS);
  }
}
