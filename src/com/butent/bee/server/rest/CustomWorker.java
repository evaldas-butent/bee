package com.butent.bee.server.rest;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.server.Invocation;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.websocket.Endpoint;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.FiresModificationEvents;
import com.butent.bee.shared.data.filter.Operator;
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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("/")
@Stateless
public class CustomWorker {

  final BeeLogger logger = LogUtils.getLogger(CustomWorker.class);
  private static final String REQUEST_TARGET = "Reg. id:";
  private static final String COMPANY_UNKNOWN = "NEŽINOMAS";

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  ParamHolderBean prm;
  @EJB
  DataEditorBean deb;
  @EJB
  FileStorageBean fs;

  @GET
  @Path("{api}.pdf")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Trusted
  public Response getApi(@PathParam("api") String name) {
    String content = qs.getValue(new SqlSelect()
      .addFields(TBL_DOCUMENT_DATA, COL_DOCUMENT_CONTENT)
      .addFrom(TBL_DOCUMENTS)
      .addFromInner(TBL_DOCUMENT_DATA,
        sys.joinTables(TBL_DOCUMENT_DATA, TBL_DOCUMENTS, COL_DOCUMENT_DATA))
      .setWhere(SqlUtils.equals(TBL_DOCUMENTS, COL_DOCUMENT_NAME, name)));

    if (BeeUtils.isEmpty(content)) {
      throw new NotFoundException();
    }
    return Invocation.locateRemoteBean(FileServiceApplication.class)
      .getFile(fs.createPdf(content).getHash(), name + ".pdf");
  }

  @GET
  @Path("endpoint")
  @Produces(MediaType.TEXT_PLAIN)
  @Trusted(secret = "B-NOVO")
  public String getPath(@HeaderParam("licence") String licence) {
    String endpoint = null;

    if (!BeeUtils.isEmpty(licence)) {
      endpoint = qs.getValue(new SqlSelect()
        .addFields(TBL_COMPANY_LICENCES, COL_LICENCE_ENDPOINT)
        .addFrom(TBL_COMPANY_LICENCES)
        .setWhere(SqlUtils.equals(TBL_COMPANY_LICENCES, COL_LICENCE, licence)));
    }
    if (BeeUtils.isEmpty(endpoint)) {
      throw new NotFoundException(licence);
    }
    return endpoint;
  }

  @POST
  @Path("request")
  public String entry(@FormParam("name") String companyName,
                      @FormParam("code") String companyCode, @FormParam("message") String message) {


    Long companyId = null;
    int targetIdx = message.indexOf(REQUEST_TARGET);

    if (BeeConst.isUndef(targetIdx)) {
      if (BeeUtils.isEmpty(companyName) && BeeUtils.isEmpty(companyCode)) {
        return "Bad request: Company or Code required";
      }

      if (!BeeUtils.isEmpty(companyCode)) {
        companyId = qs.getId(ClassifierConstants.TBL_COMPANIES, ClassifierConstants.COL_COMPANY_CODE,
          companyCode);
      }
      if (!DataUtils.isId(companyId) && !BeeUtils.isEmpty(companyName)) {
        companyId = qs.getId(ClassifierConstants.TBL_COMPANIES, ClassifierConstants.COL_COMPANY_NAME,
          companyName);
      }

      if (!DataUtils.isId(companyId)) {
        logger.warning("Rest company ", companyName, companyCode, "not found");
        companyId = prm.getRelation(AdministrationConstants.PRM_COMPANY);
      }

      if (!DataUtils.isId(companyId)) {
        return "Rest Unknown request";
      }

    } else {
      companyId = qs.getId(ClassifierConstants.TBL_COMPANIES, ClassifierConstants.COL_COMPANY_NAME,
        COMPANY_UNKNOWN);

      if (!DataUtils.isId(companyId)) {
        return "Nežinomas klientas nerastas";
      }

      String nanoNumber = message.substring(targetIdx + 8, message.length()).trim();

      SqlSelect selectCompanies = new SqlSelect()
        .addField(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), COL_COMPANY)
        .addFields(TBL_COMPANIES, COL_COMPANY_NANO_NUMBER)
        .addFrom(TBL_COMPANIES)
        .setWhere(SqlUtils.compare(TBL_COMPANIES, COL_COMPANY_NANO_NUMBER, Operator.CONTAINS, nanoNumber));

      SimpleRowSet rowSet = qs.getData(selectCompanies);
      List<Long> nanoNrList = new ArrayList<>();

      for (SimpleRowSet.SimpleRow row : rowSet) {
        String companyNanoNumber = row.getValue(COL_COMPANY_NANO_NUMBER);
        List<String> nanoNumbers = Arrays.stream(companyNanoNumber.split(";"))
          .filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());

        for (String nr : nanoNumbers) {
          if (Objects.equals(nr.trim(), nanoNumber)) {
            nanoNrList.add(row.getLong(COL_COMPANY));
          }
        }
      }

      if (nanoNrList.size() == 1) {
        companyId = nanoNrList.get(0);
      }
    }

    DataInfo reqDataInfo = sys.getDataInfo(TaskConstants.VIEW_REQUESTS);

    BeeRow row = DataUtils.createEmptyRow(reqDataInfo.getColumns().size());
    row.setValue(reqDataInfo.getColumnIndex(TaskConstants.COL_REQUEST_CUSTOMER), companyId);
    row.setValue(reqDataInfo.getColumnIndex(TaskConstants.COL_REQUEST_CONTENT), message);
    row.setValue(reqDataInfo.getColumnIndex(TaskConstants.COL_SUMMARY), "*");

    Long type = prm.getRelation(TaskConstants.PRM_DEFAULT_REST_REQUEST_TYPE);

    if (DataUtils.isId(type)) {
      row.setValue(reqDataInfo.getColumnIndex(TaskConstants.COL_REQUEST_TYPE), type);
    }
    Long form = prm.getRelation(TaskConstants.PRM_DEFAULT_REST_REQUEST_FORM);

    if (DataUtils.isId(form)) {
      row.setValue(reqDataInfo.getColumnIndex("RequestForm"), form);
    }
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
    FiresModificationEvents commando = (event, locality) -> {
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
    };
    DataChangeEvent.fireRefresh(commando, TaskConstants.VIEW_REQUESTS);
  }
}