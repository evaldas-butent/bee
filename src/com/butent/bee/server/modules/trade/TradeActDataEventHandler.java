package com.butent.bee.server.modules.trade;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

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
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TradeActDataEventHandler implements DataEventHandler {

  private static final String LAST_INVOICE_DATE = "LastInvoiceDate";

  @EJB SystemBean sys;
  @EJB QueryServiceBean qs;

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
}
