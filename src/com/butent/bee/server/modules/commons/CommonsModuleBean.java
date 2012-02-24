package com.butent.bee.server.modules.commons;

import com.google.common.base.Splitter;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CommonsModuleBean implements BeeModule {

  private static Logger logger = Logger.getLogger(CommonsModuleBean.class.getName());

  private static final Splitter ID_SPLITTER =
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();

  @EJB
  SystemBean sys;
  @EJB
  DataEditorBean deb;
  @EJB
  QueryServiceBean qs;
  @Resource
  EJBContext ctx;

  @Override
  public String dependsOn() {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CommonsConstants.COMMONS_METHOD);

    if (BeeUtils.isPrefix(svc, CommonsConstants.COMMONS_ITEM_PREFIX)) {
      response = doItemEvent(svc, reqInfo);

    } else {
      String msg = BeeUtils.concat(1, "Commons service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public String getName() {
    return CommonsConstants.COMMONS_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  private ResponseObject doItemEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, CommonsConstants.SVC_ADD_CATEGORIES)) {
      int cnt = 0;
      long itemId = BeeUtils.toLong(reqInfo.getParameter(CommonsConstants.VAR_ITEM_ID));
      String categories = reqInfo.getParameter(CommonsConstants.VAR_ITEM_CATEGORIES);

      for (String catId : ID_SPLITTER.split(categories)) {
        response = qs.insertDataWithResponse(new SqlInsert(CommonsConstants.TBL_ITEM_CATEGORIES)
            .addConstant(CommonsConstants.COL_ITEM, itemId)
            .addConstant(CommonsConstants.COL_CATEGORY, BeeUtils.toLong(catId)));

        if (response.hasErrors()) {
          break;
        }
        cnt++;
      }
      if (!response.hasErrors()) {
        response = ResponseObject.response(cnt);
      }
    } else if (BeeUtils.same(svc, CommonsConstants.SVC_REMOVE_CATEGORIES)) {
      long itemId = BeeUtils.toLong(reqInfo.getParameter(CommonsConstants.VAR_ITEM_ID));
      String categories = reqInfo.getParameter(CommonsConstants.VAR_ITEM_CATEGORIES);

      String tbl = CommonsConstants.TBL_ITEM_CATEGORIES;
      HasConditions catClause = SqlUtils.or();
      IsCondition cond = SqlUtils.and(SqlUtils.equal(tbl, CommonsConstants.COL_ITEM, itemId),
          catClause);

      for (String catId : ID_SPLITTER.split(categories)) {
        catClause.add(SqlUtils.equal(tbl, CommonsConstants.COL_CATEGORY, BeeUtils.toLong(catId)));
      }
      response = qs.updateDataWithResponse(new SqlDelete(tbl).setWhere(cond));

    } else if (BeeUtils.same(svc, CommonsConstants.SVC_ITEM_CREATE)) {
      BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(CommonsConstants.VAR_ITEM_DATA));
      String categories = reqInfo.getParameter(CommonsConstants.VAR_ITEM_CATEGORIES);
      response = deb.commitRow(rs, false);

      if (!response.hasErrors()) {
        long itemId = ((BeeRow) response.getResponse()).getId();

        if (!BeeUtils.isEmpty(categories)) {
          for (String catId : ID_SPLITTER.split(categories)) {
            response =
                qs.insertDataWithResponse(new SqlInsert(CommonsConstants.TBL_ITEM_CATEGORIES)
                    .addConstant(CommonsConstants.COL_ITEM, itemId)
                    .addConstant(CommonsConstants.COL_CATEGORY, BeeUtils.toLong(catId)));

            if (response.hasErrors()) {
              break;
            }
          }
        }
        if (!response.hasErrors()) {
          BeeView view = sys.getView(rs.getViewName());
          rs = sys.getViewData(view.getName(), ComparisonFilter.compareId(itemId), null, 0, 0);

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
      String msg = BeeUtils.concat(1, "Items service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);

    } else if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }
}
