package com.butent.bee.server.modules.trade;

import com.google.common.collect.Lists;
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
import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.ibm.icu.text.RuleBasedNumberFormat;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TradeModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(TradeModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(TRADE_METHOD);

    if (BeeUtils.same(svc, SVC_ITEMS_INFO)) {
      response = getItemsInfo(reqInfo.getParameter("view_name"),
          BeeUtils.toLongOrNull(reqInfo.getParameter("id")),
          reqInfo.getParameter(ExchangeUtils.COL_CURRENCY));

    } else if (BeeUtils.same(svc, SVC_NUMBER_TO_WORDS)) {
      response = getNumberInWords(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TRADE_AMOUNT)),
          reqInfo.getParameter("Locale"));

    } else {
      String msg = BeeUtils.joinWords("Trade service not recognized:", svc);
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
  public String getName() {
    return TRADE_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
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
        .addFields(TBL_ITEMS, COL_NAME)
        .addField(TBL_UNITS, COL_NAME, COL_UNIT)
        .addFields(trade, COL_TRADE_VAT_INCL)
        .addFields(tradeItems, COL_ITEM_ARTICLE, COL_TRADE_ITEM_QUANTITY,
            COL_TRADE_ITEM_PRICE, COL_TRADE_ITEM_VAT, COL_TRADE_ITEM_VAT_PERC, COL_TRADE_ITEM_NOTE)
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

      query.addExpr(xpr, ExchangeUtils.COL_RATES_RATE);
    }
    return ResponseObject.response(qs.getData(query));
  }

  private ResponseObject getNumberInWords(Long number, String locale) {
    Assert.notNull(number);

    Locale loc = I18nUtils.toLocale(locale);

    if (loc == null) {
      loc = usr.getLocale();
    }
    return ResponseObject.response(new RuleBasedNumberFormat(loc, RuleBasedNumberFormat.SPELLOUT)
        .format(number));
  }
}
