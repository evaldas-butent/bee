package com.butent.bee.server.modules.service;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.TBL_ORDER_ITEMS;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.Invocation;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;

class MaintenanceItemsDataProvider implements QueryServiceBean.ViewDataProvider {

  @Override
  public BeeRowSet getViewData(BeeView view, SqlSelect query, Filter filter) {
    return Invocation.locateRemoteBean(QueryServiceBean.class).getViewData(getQuery(filter)
        .setLimit(query.getLimit())
        .setOffset(query.getOffset()), view, false, null);
  }

  @Override
  public int getViewSize(BeeView view, SqlSelect query, Filter filter) {
    return Invocation.locateRemoteBean(QueryServiceBean.class).sqlCount(getQuery(filter));
  }

  private static SqlSelect getQuery(Filter filter) {
    Map<String, String> values = getFilterValues(filter);
    HasConditions wh = SqlUtils.and(SqlUtils.equals(TBL_SERVICE_MAINTENANCE, COL_SERVICE_OBJECT,
        values.get(COL_SERVICE_OBJECT)));
    SystemBean sys = Invocation.locateRemoteBean(SystemBean.class);

    return new SqlSelect()
        .addFields(TBL_SERVICE_ITEMS, COL_SERVICE_MAINTENANCE)
        .addFields(TBL_SERVICE_MAINTENANCE, COL_SERVICE_OBJECT, COL_MAINTENANCE_DATE)
        .addEmptyLong(COL_TRADE_DOCUMENT)
        .addEmptyString(COL_SERIES, 10)
        .addEmptyString(COL_TRADE_NUMBER, 50)
        .addFields(TBL_ORDER_ITEMS, COL_ITEM, COL_TRADE_ITEM_QUANTITY, COL_ITEM_PRICE,
            COL_TRADE_DISCOUNT, COL_TRADE_DOCUMENT_ITEM_VAT)
        .addExpr(SqlUtils.sqlIf(SqlUtils.isNull(TBL_ORDER_ITEMS, COL_TRADE_DISCOUNT), null, 1),
            COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT)
        .addField(TBL_ORDER_ITEMS, COL_ITEM_VAT_PERCENT, COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT)
        .addFields(TBL_SERVICE_ITEMS, COL_ITEM_ARTICLE)
        .addField(TBL_ITEMS, COL_ITEM_NAME, ALS_ITEM_NAME)
        .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE, COL_ITEM_IS_SERVICE)
        .addField(TBL_UNITS, COL_UNIT_NAME, ALS_UNIT_NAME)
        .addFields(TBL_ORDER_ITEMS, COL_TRADE_ITEM_NOTE)
        .addFrom(TBL_SERVICE_MAINTENANCE)
        .addFromInner(TBL_SERVICE_ITEMS,
            sys.joinTables(TBL_SERVICE_MAINTENANCE, TBL_SERVICE_ITEMS, COL_SERVICE_MAINTENANCE))
        .addFromInner(TBL_ORDER_ITEMS,
            sys.joinTables(TBL_SERVICE_ITEMS, TBL_ORDER_ITEMS, COL_SERVICE_ITEM))
        .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_ORDER_ITEMS, COL_ITEM))
        .addFromInner(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
        .setWhere(wh)

        .addUnion(new SqlSelect()
            .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_SERVICE_MAINTENANCE)
            .addFields(TBL_SERVICE_MAINTENANCE, COL_SERVICE_OBJECT, COL_MAINTENANCE_DATE)
            .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT)
            .addFields(TBL_TRADE_DOCUMENTS, COL_SERIES, COL_TRADE_NUMBER)
            .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM, COL_TRADE_ITEM_QUANTITY,
                COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT, COL_TRADE_DOCUMENT_ITEM_VAT,
                COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT, COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT)
            .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM_ARTICLE)
            .addField(TBL_ITEMS, COL_ITEM_NAME, ALS_ITEM_NAME)
            .addFields(TBL_ITEMS, COL_ITEM_EXTERNAL_CODE, COL_ITEM_IS_SERVICE)
            .addField(TBL_UNITS, COL_UNIT_NAME, ALS_UNIT_NAME)
            .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_NOTE)
            .addFrom(TBL_SERVICE_MAINTENANCE)
            .addFromInner(TBL_TRADE_DOCUMENT_ITEMS, sys.joinTables(TBL_SERVICE_MAINTENANCE,
                TBL_TRADE_DOCUMENT_ITEMS, COL_SERVICE_MAINTENANCE))
            .addFromInner(TBL_TRADE_DOCUMENTS,
                sys.joinTables(TBL_TRADE_DOCUMENTS, TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
            .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM))
            .addFromInner(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
            .setWhere(SqlUtils.and(wh, SqlUtils.not(SqlUtils.in(TBL_TRADE_DOCUMENT_ITEMS,
                sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS), TBL_MAINTENANCE_INVOICES,
                COL_TRADE_DOCUMENT_ITEM, SqlUtils.notNull(TBL_MAINTENANCE_INVOICES,
                    COL_TRADE_DOCUMENT_ITEM)))))
            .addOrderDesc(null, COL_MAINTENANCE_DATE));
  }

  private static Map<String, String> getFilterValues(Filter filter) {
    Map<String, String> values = new HashMap<>();

    if (filter instanceof CompoundFilter) {
      for (Filter subFilter : ((CompoundFilter) filter).getSubFilters()) {
        values.putAll(getFilterValues(subFilter));
      }
    } else if (filter instanceof ColumnValueFilter) {
      ColumnValueFilter flt = (ColumnValueFilter) filter;
      values.put(flt.getColumn(), BeeUtils.peek(flt.getValue()).getString());
    }
    return values;
  }
}
