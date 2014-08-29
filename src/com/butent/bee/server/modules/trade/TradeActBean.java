package com.butent.bee.server.modules.trade;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class TradeActBean {

  private static BeeLogger logger = LogUtils.getLogger(TradeActBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;

  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response;

    if (BeeUtils.same(svc, SVC_GET_ITEMS_FOR_SELECTION)) {
      response = getItemsForSelection(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    return response;
  }

  private ResponseObject getItemsForSelection(RequestInfo reqInfo) {
    TradeActKind kind = EnumUtils.getEnumByIndex(TradeActKind.class,
        reqInfo.getParameter(COL_TA_KIND));
    if (kind == null) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TA_KIND);
    }

    Long actId = reqInfo.getParameterLong(COL_TRADE_ACT);
    Long warehouse = reqInfo.getParameterLong(COL_WAREHOUSE);

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);

    Set<Long> actItems;
    if (DataUtils.isId(actId)) {
      actItems = qs.getLongSet(new SqlSelect()
          .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
          .addFrom(TBL_TRADE_ACT_ITEMS)
          .setWhere(SqlUtils.equals(TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT, actId)));

    } else {
      actItems = new HashSet<>();
    }

    CompoundFilter filter = Filter.and();
    filter.add(Filter.isNull(COL_ITEM_IS_SERVICE));

    if (!actItems.isEmpty()) {
      filter.add(Filter.idNotIn(actItems));
    }
    if (!BeeUtils.isEmpty(where)) {
      filter.add(Filter.restore(where));
    }

    BeeRowSet items = qs.getViewData(VIEW_ITEMS, filter);
    if (DataUtils.isEmpty(items)) {
      logger.debug(reqInfo.getService(), "no items found", filter);
      return ResponseObject.emptyResponse();
    }

    if (kind.showStock()) {
      List<Long> itemIds = (items.getNumberOfRows() < 200)
          ? items.getRowIds() : Collections.emptyList();
      Table<Long, Long, Double> stock = getStock(itemIds);

      if (stock != null) {
        int scale = sys.getFieldScale(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY);

        List<BeeRow> hasStock = new ArrayList<>();
        List<BeeRow> noStock = new ArrayList<>();

        for (BeeRow row : items) {
          boolean has = false;

          if (stock.containsRow(row.getId())) {
            for (Map.Entry<Long, Double> entry : stock.row(row.getId()).entrySet()) {
              row.setProperty(PRP_WAREHOUSE_PREFIX + BeeUtils.toString(entry.getKey()),
                  BeeUtils.toString(entry.getValue(), scale));

              if (warehouse != null && warehouse.equals(entry.getKey())) {
                has = BeeUtils.isPositive(entry.getValue());
              }
            }
          }

          if (warehouse != null) {
            if (has) {
              hasStock.add(row);
            } else {
              noStock.add(row);
            }
          }
        }

        if (!hasStock.isEmpty() && !noStock.isEmpty()) {
          items.clearRows();
          items.addRows(hasStock);
          items.addRows(noStock);
        }

        items.setTableProperty(TBL_WAREHOUSES, DataUtils.buildIdList(stock.columnKeySet()));
      }
    }

    return ResponseObject.response(items);
  }

  private Table<Long, Long, Double> getStock(Collection<Long> items) {
    Table<Long, Long, Double> result = HashBasedTable.create();

    IsCondition condition = BeeUtils.isEmpty(items)
        ? null : SqlUtils.inList(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM, items);

    SqlSelect plusQuery = getStockQuery(condition, true);
    SimpleRowSet plusData = qs.getData(plusQuery);

    if (!DataUtils.isEmpty(plusData)) {
      for (SimpleRow row : plusData) {
        Double qty = row.getDouble(COL_TRADE_ITEM_QUANTITY);
        if (BeeUtils.nonZero(qty)) {
          result.put(row.getLong(COL_TA_ITEM), row.getLong(COL_OPERATION_WAREHOUSE_TO), qty);
        }
      }
    }

    SqlSelect minusQuery = getStockQuery(condition, false);
    SimpleRowSet minusData = qs.getData(minusQuery);

    if (!DataUtils.isEmpty(minusData)) {
      for (SimpleRow row : minusData) {
        Double qty = row.getDouble(COL_TRADE_ITEM_QUANTITY);

        if (BeeUtils.nonZero(qty)) {
          Long item = row.getLong(COL_TA_ITEM);
          Long warehouse = row.getLong(COL_OPERATION_WAREHOUSE_FROM);

          Double stock = result.get(item, warehouse);

          if (stock == null) {
            result.put(item, warehouse, -qty);
          } else if (stock.equals(qty)) {
            result.remove(item, warehouse);
          } else {
            result.put(item, warehouse, stock - qty);
          }
        }
      }
    }

    return result;
  }

  private SqlSelect getStockQuery(IsCondition condition, boolean plus) {
    String colWarehouse = plus ? COL_OPERATION_WAREHOUSE_TO : COL_OPERATION_WAREHOUSE_FROM;

    return new SqlSelect()
        .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addFields(TBL_TRADE_OPERATIONS, colWarehouse)
        .addSum(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_ACT_ITEMS)
        .addFromInner(TBL_TRADE_ACTS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT))
        .addFromInner(TBL_TRADE_OPERATIONS,
            sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_ACTS, COL_TA_OPERATION))
        .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_TRADE_OPERATIONS, colWarehouse), condition))
        .addGroup(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addGroup(TBL_TRADE_OPERATIONS, colWarehouse);
  }
}
