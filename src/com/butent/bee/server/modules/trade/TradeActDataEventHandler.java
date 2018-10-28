package com.butent.bee.server.modules.trade;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.function.Function;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TradeActDataEventHandler implements DataEventHandler {

  private static final String LAST_INVOICE_DATE = "LastInvoiceDate";

  @EJB SystemBean sys;
  @EJB QueryServiceBean qs;
  @EJB CustomTradeModuleBean custom;

  public Table<Long, Long, Double> getStock(Collection<Long> items, Long... warehouses) {
    IsCondition itemsClause = BeeUtils.isEmpty(items) ? null
        : SqlUtils.inList(TBL_TRADE_ACT_ITEMS, COL_ITEM, items);

    Function<String, IsCondition> whClause = field ->
        SqlUtils.and(SqlUtils.notNull(TBL_TRADE_OPERATIONS, field), ArrayUtils.isEmpty(warehouses)
            ? null : SqlUtils.inList(TBL_TRADE_OPERATIONS, field, (Object[]) warehouses));

    Table<Long, Long, Double> result = HashBasedTable.create();

    SqlSelect union = new SqlSelect()
        .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addField(TBL_TRADE_OPERATIONS, COL_OPERATION_WAREHOUSE_TO, COL_STOCK_WAREHOUSE)
        .addSum(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_ACT_ITEMS)
        .addFromInner(TBL_TRADE_ACTS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT))
        .addFromInner(TBL_TRADE_OPERATIONS,
            sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_ACTS, COL_TA_OPERATION))
        .setWhere(SqlUtils.and(itemsClause, whClause.apply(COL_OPERATION_WAREHOUSE_TO)))
        .addGroup(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addGroup(TBL_TRADE_OPERATIONS, COL_OPERATION_WAREHOUSE_TO)
        .addUnion(new SqlSelect()
            .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
            .addField(TBL_TRADE_OPERATIONS, COL_OPERATION_WAREHOUSE_FROM, COL_STOCK_WAREHOUSE)
            .addSum(SqlUtils.multiply(SqlUtils.field(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY),
                SqlUtils.constant(-1)), COL_TRADE_ITEM_QUANTITY)
            .addFrom(TBL_TRADE_ACT_ITEMS)
            .addFromInner(TBL_TRADE_ACTS,
                sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT))
            .addFromInner(TBL_TRADE_OPERATIONS,
                sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_ACTS, COL_TA_OPERATION))
            .setWhere(SqlUtils.and(itemsClause, whClause.apply(COL_OPERATION_WAREHOUSE_FROM)))
            .addGroup(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
            .addGroup(TBL_TRADE_OPERATIONS, COL_OPERATION_WAREHOUSE_FROM));

    String subq = SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect()
        .addFields(subq, COL_TA_ITEM, COL_STOCK_WAREHOUSE)
        .addSum(subq, COL_TRADE_ITEM_QUANTITY, COL_STOCK_QUANTITY)
        .addFrom(union, subq)
        .addGroup(subq, COL_TA_ITEM, COL_STOCK_WAREHOUSE);

    qs.getData(query).forEach(row -> result.put(row.getLong(COL_TA_ITEM),
        row.getLong(COL_STOCK_WAREHOUSE), row.getDouble(COL_STOCK_QUANTITY)));

    custom.getTradeActStock(items).rowMap().forEach((item, map) -> map.forEach((wrh, qty) ->
        result.put(item, wrh, BeeUtils.unbox(result.get(item, wrh)) + qty)));

    custom.getErpStock(items).forEach((item, triplet) -> result.put(item, triplet.getA(),
        BeeUtils.unbox(result.get(item, triplet.getA())) + triplet.getC()));

    return result;
  }

  public void initConditions() {
    BeeView.registerConditionProvider(LAST_INVOICE_DATE, (view, args) -> {
      String col = BeeUtils.getQuietly(args, 0);
      String val = BeeUtils.getQuietly(args, 1);

      if (BeeUtils.anyEmpty(col, val)) {
        return null;
      }
      String[] split = val.split(BeeConst.STRING_COMMA, 2);
      String start = ArrayUtils.getQuietly(split, 0);
      String end = ArrayUtils.getQuietly(split, 1);

      IsCondition dateClause = null;

      if (!BeeUtils.isEmpty(start)) {
        dateClause = SqlUtils.and(dateClause,
            SqlUtils.moreEqual(TBL_SALES, COL_TRADE_DATE, start));
      }
      if (!BeeUtils.isEmpty(end)) {
        dateClause = SqlUtils.and(dateClause,
            SqlUtils.lessEqual(TBL_SALES, COL_TRADE_DATE, end));
      }
      return SqlUtils.not(SqlUtils.in(TBL_TRADE_ACTS, sys.getIdName(TBL_TRADE_ACTS),
          new SqlSelect().setDistinctMode(true)
              .addFields(TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT)
              .addFrom(TBL_TRADE_ACT_INVOICES)
              .addFromInner(TBL_TRADE_ACT_SERVICES, sys.joinTables(TBL_TRADE_ACT_SERVICES,
                  TBL_TRADE_ACT_INVOICES, COL_TA_INVOICE_SERVICE))
              .addFromInner(TBL_SALE_ITEMS, sys.joinTables(TBL_SALE_ITEMS, TBL_TRADE_ACT_INVOICES,
                  COL_TA_INVOICE_ITEM))
              .addFromInner(TBL_SALES, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
              .setWhere(dateClause)));
    });
  }

  @Subscribe
  @AllowConcurrentEvents
  public void setItemStock(DataEvent.ViewQueryEvent event) {
    if (event.isAfter(TBL_TRADE_ACT_ITEMS) && event.hasData()) {
      BeeRowSet rowSet = event.getRowset();

      Long warehouse = qs.getLong(new SqlSelect()
          .addFields(TBL_TRADE_OPERATIONS, COL_OPERATION_WAREHOUSE_FROM)
          .addFrom(TBL_TRADE_ACTS)
          .addFromInner(TBL_TRADE_OPERATIONS,
              sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_ACTS, COL_TA_OPERATION))
          .setWhere(sys.idEquals(TBL_TRADE_ACTS,
              rowSet.getLong(0, rowSet.getColumnIndex(COL_TRADE_ACT)))));

      if (DataUtils.isId(warehouse)) {
        int itemIdx = rowSet.getColumnIndex(COL_TA_ITEM);
        Table<Long, Long, Double> stock = getStock(rowSet.getDistinctLongs(itemIdx), warehouse);

        rowSet.forEach(beeRow -> beeRow.setProperty(COL_WAREHOUSE_REMAINDER,
            stock.get(beeRow.getLong(itemIdx), warehouse)));
      }
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void setLastInvoiceDateAndReturnedCount(DataEvent.ViewQueryEvent event) {
    if (event.isAfter(TBL_TRADE_ACTS) && event.hasData()) {
      BeeRowSet rowSet = event.getRowset();

      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT)
          .addMax(TBL_SALES, COL_TRADE_DATE)
          .addFrom(TBL_TRADE_ACT_INVOICES)
          .addFromInner(TBL_TRADE_ACT_SERVICES, sys.joinTables(TBL_TRADE_ACT_SERVICES,
              TBL_TRADE_ACT_INVOICES, COL_TA_INVOICE_SERVICE))
          .addFromInner(TBL_SALE_ITEMS, sys.joinTables(TBL_SALE_ITEMS, TBL_TRADE_ACT_INVOICES,
              COL_TA_INVOICE_ITEM))
          .addFromInner(TBL_SALES, sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
          .setWhere(SqlUtils.inList(TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT, rowSet.getRowIds()))
          .addGroup(TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT));

      rowSet.forEach(beeRow -> beeRow.setProperty(LAST_INVOICE_DATE,
          rs.getValueByKey(COL_TRADE_ACT, BeeUtils.toString(beeRow.getId()), COL_TRADE_DATE)));

      SimpleRowSet ret = qs.getData(new SqlSelect()
          .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_PARENT)
          .addCountDistinct(TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT, COL_TRADE_ACT)
          .addFrom(TBL_TRADE_ACT_ITEMS)
          .setWhere(SqlUtils.inList(TBL_TRADE_ACT_ITEMS, COL_TA_PARENT, rowSet.getRowIds()))
          .addGroup(TBL_TRADE_ACT_ITEMS, COL_TA_PARENT));

      rowSet.forEach(beeRow -> beeRow.setProperty(ALS_RETURNED_COUNT,
          ret.getValueByKey(COL_TA_PARENT, BeeUtils.toString(beeRow.getId()), COL_TRADE_ACT)));
    }
  }
}
