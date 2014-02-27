package com.butent.bee.server.modules.trade;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewModifyEvent;
import com.butent.bee.server.data.DataEvent.ViewUpdateEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TradeModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(TradeModuleBean.class);

  public static IsExpression getTotalExpression(String tblName) {
    return getTotalExpression(tblName,
        SqlUtils.multiply(SqlUtils.field(tblName, COL_TRADE_ITEM_QUANTITY),
            SqlUtils.field(tblName, COL_TRADE_ITEM_PRICE)));
  }

  public static IsExpression getTotalExpression(String tblName, IsExpression amount) {
    return SqlUtils.plus(amount,
        SqlUtils.sqlCase(null,
            SqlUtils.or(SqlUtils.isNull(tblName, COL_TRADE_VAT_PLUS),
                SqlUtils.isNull(tblName, COL_TRADE_VAT)), 0,
            SqlUtils.notNull(tblName, COL_TRADE_VAT_PERC),
            SqlUtils.multiply(SqlUtils.divide(amount, 100),
                SqlUtils.field(tblName, COL_TRADE_VAT)),
            SqlUtils.field(tblName, COL_TRADE_VAT)));
  }

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_ITEMS_INFO)) {
      response = getItemsInfo(reqInfo.getParameter("view_name"),
          BeeUtils.toLongOrNull(reqInfo.getParameter("id")),
          reqInfo.getParameter(ExchangeUtils.COL_CURRENCY));

    } else if (BeeUtils.same(svc, SVC_CREDIT_INFO)) {
      response = getCreditInfo(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_COMPANY)));

    } else {
      String msg = BeeUtils.joinWords("Trade service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  public ResponseObject getCreditInfo(Long companyId) {
    if (!DataUtils.isId(companyId)) {
      return ResponseObject.emptyResponse();
    }
    SimpleRow company = qs.getRow(new SqlSelect()
        .addFields(TBL_COMPANIES, COL_COMPANY_NAME, COL_COMPANY_CREDIT_LIMIT,
            COL_COMPANY_LIMIT_CURRENCY, COL_COMPANY_CREDIT_DAYS)
        .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
        .addFrom(TBL_COMPANIES)
        .addFromLeft(TBL_CURRENCIES,
            sys.joinTables(TBL_CURRENCIES, TBL_COMPANIES, COL_COMPANY_LIMIT_CURRENCY))
        .setWhere(sys.idEquals(TBL_COMPANIES, companyId)));

    Map<String, Object> resp = Maps.newHashMap();

    if (company != null) {
      double limit = BeeUtils.unbox(company.getDouble(COL_COMPANY_CREDIT_LIMIT));
      Long curr = company.getLong(COL_COMPANY_LIMIT_CURRENCY);
      int days = BeeUtils.unbox(company.getInt(COL_COMPANY_CREDIT_DAYS));

      SqlSelect query = new SqlSelect()
          .addFields(TBL_SALES, COL_TRADE_DATE, COL_TRADE_TERM)
          .addFrom(TBL_SALES)
          .setWhere(SqlUtils.and(SqlUtils.or(SqlUtils.equals(TBL_SALES, COL_SALE_PAYER, companyId),
              SqlUtils.and(SqlUtils.isNull(TBL_SALES, COL_SALE_PAYER),
                  SqlUtils.equals(TBL_SALES, COL_TRADE_CUSTOMER, companyId))),
              SqlUtils.less(SqlUtils.nvl(SqlUtils.field(TBL_SALES, COL_TRADE_PAID), 0),
                  SqlUtils.nvl(SqlUtils.field(TBL_SALES, COL_TRADE_AMOUNT), 0))));

      if (DataUtils.isId(curr)) {
        query.addExpr(ExchangeUtils.exchangeFieldTo(query, TBL_SALES, COL_TRADE_AMOUNT,
            COL_TRADE_CURRENCY, COL_TRADE_DATE, curr), COL_TRADE_AMOUNT)
            .addExpr(ExchangeUtils.exchangeFieldTo(query, TBL_SALES, COL_TRADE_PAID,
                COL_TRADE_CURRENCY, COL_TRADE_PAYMENT_TIME, curr), COL_TRADE_PAID);
      } else {
        query.addExpr(ExchangeUtils.exchangeField(query, TBL_SALES, COL_TRADE_AMOUNT,
            COL_TRADE_CURRENCY, COL_TRADE_DATE), COL_TRADE_AMOUNT)
            .addExpr(ExchangeUtils.exchangeField(query, TBL_SALES, COL_TRADE_PAID,
                COL_TRADE_CURRENCY, COL_TRADE_PAYMENT_TIME), COL_TRADE_PAID);
      }
      double debt = 0.0;
      double overdue = 0.0;

      for (SimpleRow row : qs.getData(query)) {
        double xxx = BeeUtils.unbox(row.getDouble(COL_TRADE_AMOUNT))
            - BeeUtils.unbox(row.getDouble(COL_TRADE_PAID));

        int dayDiff = TimeUtils.dayDiff(BeeUtils.nvl(row.getDateTime(COL_TRADE_TERM),
            TimeUtils.nextDay(row.getDateTime(COL_TRADE_DATE), days)), TimeUtils.nowMinutes());

        if (dayDiff > 0) {
          overdue += xxx;
        }
        debt += xxx;
      }
      resp.put(COL_COMPANY_NAME, company.getValue(COL_COMPANY_NAME));
      resp.put(COL_COMPANY_CREDIT_LIMIT, limit);
      resp.put(COL_COMPANY_LIMIT_CURRENCY, curr);
      resp.put(COL_CURRENCY, company.getValue(COL_CURRENCY));
      resp.put(COL_COMPANY_CREDIT_DAYS, days);
      resp.put(VAR_DEBT, BeeUtils.round(debt, 2));
      resp.put(VAR_OVERDUE, BeeUtils.round(overdue, 2));
    }
    return ResponseObject.response(resp);
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public Module getModule() {
    return Module.TRADE;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void fillInvoiceNumber(ViewModifyEvent event) {
        if (BeeUtils.inListSame(event.getTargetName(), TBL_SALES,
            TransportConstants.VIEW_CARGO_INVOICES) && event.isBefore()) {
          List<BeeColumn> cols = null;
          IsRow row = null;
          String prefix = null;

          if (event instanceof ViewInsertEvent) {
            cols = ((ViewInsertEvent) event).getColumns();
            row = ((ViewInsertEvent) event).getRow();
          } else if (event instanceof ViewUpdateEvent) {
            cols = ((ViewUpdateEvent) event).getColumns();
            row = ((ViewUpdateEvent) event).getRow();
          } else {
            return;
          }
          int idx = DataUtils.getColumnIndex(COL_TRADE_INVOICE_PREFIX, cols);

          if (idx != BeeConst.UNDEF) {
            prefix = row.getString(idx);
          }
          if (!BeeUtils.isEmpty(prefix)
              && DataUtils.getColumnIndex(COL_TRADE_INVOICE_NO, cols) == BeeConst.UNDEF) {
            cols.add(new BeeColumn(COL_TRADE_INVOICE_NO));
            row.addCell(Value.getValue(qs.getNextNumber(TBL_SALES, COL_TRADE_INVOICE_NO, prefix,
                COL_TRADE_INVOICE_PREFIX)));
          }
        }
      }
    });
  }

  private ResponseObject getItemsInfo(String viewName, Long id, String currencyTo) {
    if (!sys.isView(viewName)) {
      return ResponseObject.error("Wrong view name");
    }
    if (!DataUtils.isId(id)) {
      return ResponseObject.error("Wrong document ID");
    }
    String trade = sys.getView(viewName).getSourceName();
    String tradeItems;
    String itemsRelation;

    if (BeeUtils.same(trade, TBL_SALES)) {
      tradeItems = TBL_SALE_ITEMS;
      itemsRelation = COL_SALE;
    } else if (BeeUtils.same(trade, TBL_PURCHASES)) {
      tradeItems = TBL_PURCHASE_ITEMS;
      itemsRelation = COL_PURCHASE;
    } else {
      return ResponseObject.error("View source not supported:", trade);
    }
    SqlSelect query = new SqlSelect()
        .addFields(TBL_ITEMS, COL_ITEM_NAME)
        .addField(TBL_UNITS, COL_UNIT_NAME, COL_UNIT)
        .addFields(tradeItems, COL_ITEM_ARTICLE, COL_TRADE_ITEM_QUANTITY,
            COL_TRADE_ITEM_PRICE, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC,
            COL_TRADE_ITEM_NOTE)
        .addField(ExchangeUtils.TBL_CURRENCIES, ExchangeUtils.COL_CURRENCY_NAME,
            ExchangeUtils.COL_CURRENCY)
        .addFrom(tradeItems)
        .addFromInner(trade, sys.joinTables(trade, tradeItems, itemsRelation))
        .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, tradeItems, COL_ITEM))
        .addFromInner(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
        .addFromInner(ExchangeUtils.TBL_CURRENCIES,
            sys.joinTables(ExchangeUtils.TBL_CURRENCIES, trade, ExchangeUtils.COL_CURRENCY))
        .setWhere(SqlUtils.equals(tradeItems, itemsRelation, id))
        .addOrder(tradeItems, sys.getIdName(tradeItems));

    if (!BeeUtils.isEmpty(currencyTo)) {
      String currAlias = SqlUtils.uniqueName();

      IsExpression xpr = ExchangeUtils.exchangeFieldTo(query
          .addFromLeft(ExchangeUtils.TBL_CURRENCIES, currAlias,
              SqlUtils.equals(currAlias, ExchangeUtils.COL_CURRENCY_NAME, currencyTo)),
          SqlUtils.constant(1),
          SqlUtils.field(trade, ExchangeUtils.COL_CURRENCY),
          SqlUtils.field(trade, COL_TRADE_DATE),
          SqlUtils.field(currAlias, sys.getIdName(ExchangeUtils.TBL_CURRENCIES)));

      query.addExpr(xpr, ExchangeUtils.COL_RATES_RATE)
          .addField(currAlias, ExchangeUtils.COL_CURRENCY_NAME,
              ExchangeUtils.COL_RATES_RATE + ExchangeUtils.COL_CURRENCY);
    }
    return ResponseObject.response(qs.getData(query));
  }
}
