package com.butent.bee.server.modules.commons;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEvent.ViewDeleteEvent;
import com.butent.bee.server.data.ViewEvent.ViewInsertEvent;
import com.butent.bee.server.data.ViewEvent.ViewModifyEvent;
import com.butent.bee.server.data.ViewEvent.ViewUpdateEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeConst.SqlEngine;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Stateless
@LocalBean
public class CommonsModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(CommonsModuleBean.class);

  private static final Splitter ID_SPLITTER =
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  DataEditorBean deb;
  @EJB
  QueryServiceBean qs;
  @EJB
  ParamHolderBean prm;
  @Resource
  EJBContext ctx;

  @Override
  public Collection<String> dependsOn() {
    return null;
  }

  @Override
  public List<SearchResult> doSearch(String query) {

    List<SearchResult> companiesSr = qs.getSearchResults(VIEW_COMPANIES,
        Filter.anyContains(Sets.newHashSet(COL_NAME, COL_CODE, COL_PHONE, COL_EMAIL_ADDRESS,
            COL_ADDRESS, COL_CITY_NAME, COL_COUNTRY_NAME), query));
    
    List<SearchResult> personsSr =
        qs.getSearchResults(VIEW_PERSONS,
            Filter.anyContains(Sets.newHashSet(COL_FIRST_NAME, COL_LAST_NAME, COL_PHONE,
                COL_EMAIL_ADDRESS, COL_ADDRESS, COL_CITY_NAME, COL_COUNTRY_NAME), query));

    List<SearchResult> usersSr = qs.getSearchResults(VIEW_USERS,
        Filter.anyContains(Sets.newHashSet(COL_LOGIN, COL_FIRST_NAME, COL_LAST_NAME), query));

    List<SearchResult> itemsSr = qs.getSearchResults(VIEW_ITEMS,
        Filter.anyContains(Sets.newHashSet(COL_NAME, COL_ARTICLE, COL_BARCODE), query));

    List<SearchResult> commonsSr = Lists.newArrayList();
    commonsSr.addAll(companiesSr);
    commonsSr.addAll(personsSr);
    commonsSr.addAll(usersSr);
    commonsSr.addAll(itemsSr);

    return commonsSr;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(COMMONS_METHOD);

    if (BeeUtils.isPrefix(svc, COMMONS_ITEM_PREFIX)) {
      response = doItemEvent(svc, reqInfo);

    } else if (BeeUtils.isPrefix(svc, COMMONS_PARAMETERS_PREFIX)) {
      response = doParameterEvent(svc, reqInfo);

    } else {
      String msg = BeeUtils.joinWords("Commons service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    List<BeeParameter> params = Lists.newArrayList(
        new BeeParameter(COMMONS_MODULE,
            "ProgramTitle", ParameterType.TEXT, null, false, "BEE"),
        new BeeParameter(COMMONS_MODULE,
            "Precission", ParameterType.NUMBER, "Precission of calculations", true, 5),
        new BeeParameter(COMMONS_MODULE,
            PRM_AUDIT_OFF, ParameterType.BOOLEAN, "Disable database level auditing", false, false));

    params.addAll(getSqlEngineParameters());
    return params;
  }

  @Override
  public String getName() {
    return COMMONS_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void refreshRightsCache(ViewModifyEvent event) {
        if (usr.isRightsTable(event.getViewName()) && event.isAfter()) {
          usr.initRights();
        }
      }

      @Subscribe
      public void refreshUserCache(ViewModifyEvent event) {
        if (usr.isUserTable(event.getViewName()) && event.isAfter()) {
          usr.initUsers();
        }
      }

      @Subscribe
      public void storeEmail(ViewModifyEvent event) {
        if (BeeUtils.same(event.getViewName(), TBL_EMAILS) && event.isBefore()
            && !(event instanceof ViewDeleteEvent)) {

          List<BeeColumn> cols;
          BeeRow row;

          if (event instanceof ViewInsertEvent) {
            cols = ((ViewInsertEvent) event).getColumns();
            row = ((ViewInsertEvent) event).getRow();
          } else {
            cols = ((ViewUpdateEvent) event).getColumns();
            row = ((ViewUpdateEvent) event).getRow();
          }
          int idx = DataUtils.getColumnIndex(COL_EMAIL_ADDRESS, cols);

          if (idx != BeeConst.UNDEF) {
            String email = BeeUtils.normalize(row.getString(idx));

            try {
              new InternetAddress(email, true).validate();
              row.setValue(idx, email);
            } catch (AddressException ex) {
              event.addErrorMessage(BeeUtils.joinWords("Wrong address:", ex.getMessage()));
            }
          }
        }
      }
    });
  }

  private ResponseObject doItemEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_ADD_CATEGORIES)) {
      int cnt = 0;
      long itemId = BeeUtils.toLong(reqInfo.getParameter(VAR_ITEM_ID));
      String categories = reqInfo.getParameter(VAR_ITEM_CATEGORIES);

      for (String catId : ID_SPLITTER.split(categories)) {
        response = qs.insertDataWithResponse(new SqlInsert(TBL_ITEM_CATEGORIES)
            .addConstant(COL_ITEM, itemId)
            .addConstant(COL_CATEGORY, BeeUtils.toLong(catId)));

        if (response.hasErrors()) {
          break;
        }
        cnt++;
      }
      if (!response.hasErrors()) {
        response = ResponseObject.response(cnt);
      }
    } else if (BeeUtils.same(svc, SVC_REMOVE_CATEGORIES)) {
      long itemId = BeeUtils.toLong(reqInfo.getParameter(VAR_ITEM_ID));
      String categories = reqInfo.getParameter(VAR_ITEM_CATEGORIES);

      String tbl = TBL_ITEM_CATEGORIES;
      HasConditions catClause = SqlUtils.or();
      IsCondition cond = SqlUtils.and(SqlUtils.equals(tbl, COL_ITEM, itemId),
          catClause);

      for (String catId : ID_SPLITTER.split(categories)) {
        catClause.add(SqlUtils.equals(tbl, COL_CATEGORY, BeeUtils.toLong(catId)));
      }
      response = qs.updateDataWithResponse(new SqlDelete(tbl).setWhere(cond));

    } else if (BeeUtils.same(svc, SVC_ITEM_CREATE)) {
      BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(VAR_ITEM_DATA));
      String categories = reqInfo.getParameter(VAR_ITEM_CATEGORIES);
      response = deb.commitRow(rs, false);

      if (!response.hasErrors()) {
        long itemId = ((BeeRow) response.getResponse()).getId();

        if (!BeeUtils.isEmpty(categories)) {
          for (String catId : ID_SPLITTER.split(categories)) {
            response =
                qs.insertDataWithResponse(new SqlInsert(TBL_ITEM_CATEGORIES)
                    .addConstant(COL_ITEM, itemId)
                    .addConstant(COL_CATEGORY, BeeUtils.toLong(catId)));

            if (response.hasErrors()) {
              break;
            }
          }
        }
        if (!response.hasErrors()) {
          BeeView view = sys.getView(rs.getViewName());
          rs = qs.getViewData(view.getName(), ComparisonFilter.compareId(itemId));

          if (rs.isEmpty()) {
            String msg = "Optimistic lock exception";
            logger.warning(msg);
            response = ResponseObject.error(msg);
          } else {
            response.setResponse(rs.getRow(0));
          }
        }
      }
    }
    if (response == null) {
      String msg = BeeUtils.joinWords("Items service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);

    } else if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private ResponseObject doParameterEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_GET_PARAMETERS)) {
      List<BeeParameter> params = Lists.newArrayList();

      for (BeeParameter p : prm
          .getParameters(reqInfo.getParameter(VAR_PARAMETERS_MODULE)).values()) {
        BeeParameter param = new BeeParameter(p.getModule(), p.getName(), p.getType(),
            p.getDescription(), p.supportsUsers(), p.getValue());

        if (param.supportsUsers()) {
          param.setUserValue(usr.getCurrentUserId(), p.getUserValue(usr.getCurrentUserId()));
        }
        params.add(param);
      }
      response = ResponseObject.response(params);

    } else if (BeeUtils.same(svc, SVC_CREATE_PARAMETER)) {
      prm.createParameter(BeeParameter.restore(
          reqInfo.getParameter(VAR_PARAMETERS)));
      response = ResponseObject.response(true);

    } else if (BeeUtils.same(svc, SVC_SET_PARAMETER)) {
      prm.setParameter(reqInfo.getParameter(VAR_PARAMETERS_MODULE),
          reqInfo.getParameter(VAR_PARAMETERS),
          reqInfo.getParameter(VAR_PARAMETER_VALUE));
      response = ResponseObject.response(true);

    } else if (BeeUtils.same(svc, SVC_REMOVE_PARAMETERS)) {
      prm.removeParameters(reqInfo.getParameter(VAR_PARAMETERS_MODULE),
          Codec.beeDeserializeCollection(reqInfo.getParameter(VAR_PARAMETERS)));

      response = ResponseObject.response(true);
    }
    if (response == null) {
      String msg = BeeUtils.joinWords("Parameters service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);

    } else if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private Collection<? extends BeeParameter> getSqlEngineParameters() {
    List<BeeParameter> params = Lists.newArrayList();

    for (SqlEngine engine : SqlEngine.values()) {
      BeeParameter param = new BeeParameter(COMMONS_MODULE,
          BeeUtils.join(BeeConst.STRING_EMPTY, PRM_SQL_MESSAGES, engine), ParameterType.MAP,
          BeeUtils.joinWords("Duomenų bazės", engine, "klaidų pranešimai"), false, null);

      switch (engine) {
        case GENERIC:
        case MSSQL:
        case ORACLE:
          break;
        case POSTGRESQL:
          param.setValue(ImmutableMap
              .of(".+duplicate key value violates unique constraint.+(\\(.+=.+\\)).+",
                  "Tokia reikšmė jau egzistuoja: $1",
                  ".+violates foreign key constraint.+from table \"(.+)\"\\.",
                  "Įrašas naudojamas lentelėje \"$1\""));
          break;
      }
      params.add(param);
    }
    return params;
  }
}
