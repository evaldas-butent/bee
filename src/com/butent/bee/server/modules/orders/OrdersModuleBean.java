package com.butent.bee.server.modules.orders;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.projects.ProjectConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class OrdersModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(OrdersModuleBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  ParamHolderBean prm;

  @Override
  public List<SearchResult> doSearch(String query) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResponseObject doService(String service, RequestInfo reqInfo) {
    ResponseObject response;

    String svc = BeeUtils.trim(service);

    switch (svc) {
      case SVC_GET_ITEMS_FOR_SELECTION:
        response = getItemsForSelection(reqInfo);
        break;

      case SVC_GET_TEMPLATE_ITEMS:
        response = getTemplateItems(reqInfo);
        break;

      case OrdersConstants.SVC_CREATE_INVOICE_ITEMS:
        response = createInvoiceItems(reqInfo);
        break;

      default:
        String msg = BeeUtils.joinWords("service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Module getModule() {
    return Module.ORDERS;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    // TODO Auto-generated method stub
  }

  private ResponseObject getItemsForSelection(RequestInfo reqInfo) {

    Long orderId = reqInfo.getParameterLong(COL_ORDER);
    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    Long warehouse = reqInfo.getParameterLong(ClassifierConstants.COL_WAREHOUSE);

    CompoundFilter filter = Filter.and();
    filter.add(Filter.isNull(COL_ITEM_IS_SERVICE));

    Set<Long> orderItems = getOrderItems(orderId);
    if (!orderItems.isEmpty()) {
      filter.add(Filter.idNotIn(orderItems));
    }

    if (!BeeUtils.isEmpty(where)) {
      filter.add(Filter.restore(where));
    }

    filter.add(Filter.in(sys.getIdName(TBL_ITEMS), VIEW_ITEM_REMAINDERS, COL_ITEM, Filter.equals(
        ClassifierConstants.COL_WAREHOUSE, warehouse)));

    BeeRowSet items = qs.getViewData(VIEW_ITEMS, filter);

    if (DataUtils.isEmpty(items)) {
      logger.debug(reqInfo.getService(), "no items found", filter);
      return ResponseObject.emptyResponse();
    }

    SqlSelect query =
        new SqlSelect()
            .addAllFields(VIEW_ITEM_REMAINDERS)
            .addFields(TBL_WAREHOUSES, COL_WAREHOUSE_CODE)
            .addFrom(VIEW_ITEM_REMAINDERS)
            .addFromInner(TBL_WAREHOUSES,
                SqlUtils.join(TBL_WAREHOUSES, sys.getIdName(TBL_WAREHOUSES), VIEW_ITEM_REMAINDERS,
                    ClassifierConstants.COL_WAREHOUSE))
            .setWhere(
                SqlUtils.equals(VIEW_ITEM_REMAINDERS, ClassifierConstants.COL_WAREHOUSE, warehouse));

    SimpleRowSet srs = qs.getData(query);

    if (!DataUtils.isEmpty(srs)) {
      BeeView remView = sys.getView(VIEW_ITEM_REMAINDERS);
      items.addColumn(remView.getBeeColumn(ALS_WAREHOUSE_CODE));
      items.addColumn(remView.getBeeColumn(COL_WAREHOUSE_REMAINDER));

      for (BeeRow row : items) {
        Long itemId = row.getId();

        for (SimpleRow sr : srs) {
          if (itemId == sr.getLong(COL_ITEM)) {
            String name = sr.getValue(COL_WAREHOUSE_CODE);
            Double rem = sr.getDouble(COL_WAREHOUSE_REMAINDER);
            row.setValue(row.getNumberOfCells() - 2, name);
            row.setValue(row.getNumberOfCells() - 1, rem);
          }
        }
      }
    }
    return ResponseObject.response(items);
  }

  private ResponseObject createInvoiceItems(RequestInfo reqInfo) {
    Long saleId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SALE));
    Long currency = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_CURRENCY));
    Set<Long> ids = DataUtils.parseIdSet(reqInfo.getParameter(Service.VAR_DATA));

    if (!DataUtils.isId(saleId)) {
      return ResponseObject.error("Wrong account ID");
    }
    if (!DataUtils.isId(currency)) {
      return ResponseObject.error("Wrong currency ID");
    }
    if (BeeUtils.isEmpty(ids)) {
      return ResponseObject.error("Empty ID list");
    }

    IsCondition where = sys.idInList(TBL_ORDER_ITEMS, ids);

    SqlSelect query = new SqlSelect();
    query.addFields(TBL_ORDER_ITEMS, COL_TRADE_VAT_PLUS,
        TradeConstants.COL_TRADE_VAT, COL_TRADE_VAT_PERC, COL_INCOME_ITEM,
        TradeConstants.COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_ORDER_ITEMS)
        .setWhere(where);

    IsExpression vatExch =
        ExchangeUtils.exchangeFieldTo(query, TBL_ORDER_ITEMS, COL_TRADE_VAT,
            COL_TRADE_CURRENCY, COL_INCOME_DATE, currency);

    String vatAlias = "Vat_" + SqlUtils.uniqueName();

    String priceAlias = "Price_" + SqlUtils.uniqueName();
    IsExpression priceExch =
        ExchangeUtils.exchangeFieldTo(query, TBL_ORDER_ITEMS,
            COL_TRADE_ITEM_PRICE, COL_TRADE_CURRENCY,
            COL_INCOME_DATE, currency);

    query.addExpr(priceExch, priceAlias)
        .addExpr(vatExch, vatAlias)
        .addOrder(TBL_ORDER_ITEMS, sys.getIdName(TBL_ORDER_ITEMS));

    LogUtils.getRootLogger().info(query.getQuery());

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.error(TBL_ORDER_ITEMS, ids, "not found");
    }

    ResponseObject response = new ResponseObject();

    for (SimpleRow row : data) {
      Long item = row.getLong(COL_INCOME_ITEM);

      SqlInsert insert = new SqlInsert(TBL_SALE_ITEMS)
          .addConstant(COL_SALE, saleId)
          .addConstant(COL_ITEM, item);

      Boolean vatPerc = row.getBoolean(COL_TRADE_VAT_PERC);
      Double vat;
      if (BeeUtils.isTrue(vatPerc)) {
        insert.addConstant(COL_TRADE_VAT_PERC, vatPerc);
        vat = row.getDouble(COL_TRADE_VAT);
      } else {
        vat = row.getDouble(vatAlias);
      }

      if (BeeUtils.nonZero(vat)) {
        insert.addConstant(COL_TRADE_VAT, vat);
      }

      Boolean vatPlus = row.getBoolean(COL_TRADE_VAT_PLUS);

      if (BeeUtils.isTrue(vatPlus)) {
        insert.addConstant(COL_TRADE_VAT_PLUS, vatPlus);
      }

      Double quantity = row.getDouble(TradeConstants.COL_TRADE_ITEM_QUANTITY);
      Double price = row.getDouble(priceAlias);

      insert.addConstant(COL_TRADE_ITEM_QUANTITY, BeeUtils.unbox(quantity));

      if (price != null) {
        insert.addConstant(COL_TRADE_ITEM_PRICE, price);
      }

      ResponseObject insResponse = qs.insertDataWithResponse(insert);
      if (insResponse.hasErrors()) {
        response.addMessagesFrom(insResponse);
        break;
      }
    }

    if (!response.hasErrors()) {
      SqlUpdate update = new SqlUpdate(TBL_ORDER_ITEMS)
          .addConstant(COL_INCOME_SALE, saleId)
          .setWhere(where);

      ResponseObject updResponse = qs.updateDataWithResponse(update);
      if (updResponse.hasErrors()) {
        response.addMessagesFrom(updResponse);
      }
    }

    return response;
  }

  private Set<Long> getOrderItems(Long orderId) {
    if (DataUtils.isId(orderId)) {
      return qs.getLongSet(new SqlSelect()
          .addFields(TBL_ORDER_ITEMS, COL_ITEM)
          .addFrom(TBL_ORDER_ITEMS)
          .setWhere(SqlUtils.equals(TBL_ORDER_ITEMS, COL_ORDER, orderId)));
    } else {
      return BeeConst.EMPTY_IMMUTABLE_LONG_SET;
    }
  }

  private ResponseObject getTemplateItems(RequestInfo reqInfo) {
    Long templateId = reqInfo.getParameterLong(COL_TEMPLATE);
    if (!DataUtils.isId(templateId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TEMPLATE);
    }

    Long orderId = reqInfo.getParameterLong(COL_ORDER);

    List<BeeRowSet> result = new ArrayList<>();

    Set<Long> itemIds = new HashSet<>();

    Set<Long> ordItems = getOrderItems(orderId);
    Filter filter = getTemplateChildrenFilter(templateId, ordItems);

    BeeRowSet templateItems = qs.getViewData(VIEW_ORDER_TMPL_ITEMS, filter);
    if (!DataUtils.isEmpty(templateItems)) {
      result.add(templateItems);

      int index = templateItems.getColumnIndex(COL_ITEM);
      itemIds.addAll(templateItems.getDistinctLongs(index));
    }

    if (!itemIds.isEmpty()) {
      BeeRowSet items = qs.getViewData(VIEW_ITEMS, Filter.idIn(itemIds));
      if (!DataUtils.isEmpty(items)) {
        result.add(items);
      }
    }

    if (result.isEmpty()) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.response(result).setSize(result.size());
    }
  }

  private static Filter getTemplateChildrenFilter(Long templateId, Collection<Long> excludeItems) {
    if (BeeUtils.isEmpty(excludeItems)) {
      return Filter.equals(COL_TEMPLATE, templateId);
    } else {
      return Filter.and(Filter.equals(COL_TEMPLATE, templateId),
          Filter.exclude(COL_ITEM, excludeItems));
    }
  }
}