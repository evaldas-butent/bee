package com.butent.bee.server.modules.service;

import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.administration.ExtensionIcons;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class ServiceModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(ServiceModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response;

    if (BeeUtils.same(svc, SVC_CREATE_INVOICE_ITEMS)) {
      response = createInvoiceItems(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("service not recognized:", svc);
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
  public Module getModule() {
    return Module.SERVICE;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void setRowProperties(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (BeeUtils.same(event.getTargetName(), VIEW_OBJECT_FILES)) {
          ExtensionIcons.setIcons(event.getRowset(), AdministrationConstants.ALS_FILE_NAME,
              AdministrationConstants.PROP_ICON);

        } else if (BeeUtils.same(event.getTargetName(), VIEW_OBJECTS)
            && !DataUtils.isEmpty(event.getRowset())) {

          BeeRowSet rowSet = event.getRowset();
          List<Long> rowIds = rowSet.getRowIds();

          BeeView view = sys.getView(VIEW_OBJECT_CRITERIA);
          SqlSelect query = view.getQuery();

          query.setWhere(SqlUtils.and(query.getWhere(),
              SqlUtils.isNull(view.getSourceAlias(), COL_CRITERIA_GROUP_NAME),
              SqlUtils.inList(view.getSourceAlias(), COL_SERVICE_OBJECT, rowIds)));

          SimpleRowSet criteria = qs.getData(query);

          if (!DataUtils.isEmpty(criteria)) {
            for (SimpleRow row : criteria) {
              BeeRow r = rowSet.getRowById(row.getLong(COL_SERVICE_OBJECT));

              if (r != null) {
                r.setProperty(COL_CRITERION_NAME + row.getValue(COL_CRITERION_NAME),
                    row.getValue(COL_CRITERION_VALUE));
              }
            }
          }
        }
      }
    });
  }

  private ResponseObject createInvoiceItems(RequestInfo reqInfo) {
    Long invId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_MAINTENANCE_INVOICE));
    if (!DataUtils.isId(invId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_MAINTENANCE_INVOICE);
    }

    Long currency =
        BeeUtils.toLongOrNull(reqInfo.getParameter(AdministrationConstants.COL_CURRENCY));
    if (!DataUtils.isId(currency)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(),
          AdministrationConstants.COL_CURRENCY);
    }

    Set<Long> ids = DataUtils.parseIdSet(reqInfo.getParameter(VIEW_MAINTENANCE));
    if (ids.isEmpty()) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), VIEW_MAINTENANCE);
    }

    Long mainItem = BeeUtils.toLongOrNull(reqInfo.getParameter(PROP_MAIN_ITEM));

    IsCondition where = sys.idInList(TBL_MAINTENANCE, ids);

    SqlSelect query = new SqlSelect()
        .addFields(TBL_MAINTENANCE, COL_MAINTENANCE_ITEM, COL_MAINTENANCE_QUANTITY,
            COL_MAINTENANCE_PRICE)
        .addFrom(TBL_MAINTENANCE)
        .setWhere(where);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.error(TBL_MAINTENANCE, ids, "not found");
    }

    ResponseObject response = new ResponseObject();

    if (DataUtils.isId(mainItem)) {
      double quantity = BeeConst.DOUBLE_ZERO;
      double total = BeeConst.DOUBLE_ZERO;

      for (SimpleRow row : data) {
        Double q = row.getDouble(COL_MAINTENANCE_QUANTITY);
        Double p = row.getDouble(COL_MAINTENANCE_PRICE);

        if (BeeUtils.isPositive(q) && BeeUtils.isPositive(p)) {
          quantity += q;
          total += q * p;
        } else {
          quantity += BeeUtils.unbox(q);
        }
      }

      double price;
      if (BeeUtils.isPositive(quantity) && BeeUtils.isPositive(total)) {
        price = total;
        quantity = 1;
      } else {
        price = BeeConst.DOUBLE_ZERO;
      }

      SqlInsert insert = new SqlInsert(TradeConstants.TBL_SALE_ITEMS)
          .addConstant(TradeConstants.COL_SALE, invId)
          .addConstant(ClassifierConstants.COL_ITEM, mainItem)
          .addConstant(TradeConstants.COL_TRADE_ITEM_QUANTITY, quantity)
          .addConstant(TradeConstants.COL_TRADE_ITEM_PRICE, price);

      ResponseObject insResponse = qs.insertDataWithResponse(insert);
      if (insResponse.hasErrors()) {
        response.addMessagesFrom(insResponse);
      }

    } else {
      for (SimpleRow row : data) {
        Double q = row.getDouble(COL_MAINTENANCE_QUANTITY);
        Double p = row.getDouble(COL_MAINTENANCE_PRICE);

        SqlInsert insert = new SqlInsert(TradeConstants.TBL_SALE_ITEMS)
            .addConstant(TradeConstants.COL_SALE, invId)
            .addConstant(ClassifierConstants.COL_ITEM, row.getLong(COL_MAINTENANCE_ITEM))
            .addConstant(TradeConstants.COL_TRADE_ITEM_QUANTITY, BeeUtils.unbox(q))
            .addConstant(TradeConstants.COL_TRADE_ITEM_PRICE, BeeUtils.unbox(p));

        ResponseObject insResponse = qs.insertDataWithResponse(insert);
        if (insResponse.hasErrors()) {
          response.addMessagesFrom(insResponse);
          break;
        }
      }
    }

    if (!response.hasErrors()) {
      SqlUpdate update = new SqlUpdate(TBL_MAINTENANCE)
          .addConstant(COL_MAINTENANCE_INVOICE, invId)
          .setWhere(where);
      
      ResponseObject updResponse = qs.updateDataWithResponse(update);
      if (updResponse.hasErrors()) {
        response.addMessagesFrom(updResponse);
      }
    }

    return response;
  }
}
