package com.butent.bee.server.modules.trade;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewModifyEvent;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEvent.ViewUpdateEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.modules.classifiers.ClassifiersModuleBean;
import com.butent.bee.server.modules.mail.MailAccount;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.server.modules.mail.MailStorageBean;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.BorderStyle;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.css.values.WordWrap;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.elements.Caption;
import com.butent.bee.shared.html.builder.elements.Pre;
import com.butent.bee.shared.html.builder.elements.Table;
import com.butent.bee.shared.html.builder.elements.Td;
import com.butent.bee.shared.html.builder.elements.Th;
import com.butent.bee.shared.html.builder.elements.Tr;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.trade.TradeDocumentData;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.webservice.ButentWS;
import com.butent.webservice.WSDocument;
import com.butent.webservice.WSDocument.WSDocumentItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
  @EJB
  ParamHolderBean prm;
  @EJB
  TradeActBean act;
  @EJB
  MailModuleBean mail;
  @EJB
  MailStorageBean mailStore;

  @EJB
  ClassifiersModuleBean cls;

  public static String decodeId(String trade, Long id) {
    Assert.notEmpty(trade);
    Long normalizedId;

    switch (trade) {
      case TBL_PURCHASES:
        normalizedId = id / 2;
        break;

      case TBL_SALES:
        normalizedId = (id - 1) / 2;
        break;

      default:
        throw new BeeRuntimeException("View source not supported: " + trade);
    }
    return BeeUtils.toString(normalizedId);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result = new ArrayList<>();

    Set<String> columns = Sets.newHashSet(COL_TRADE_NUMBER, COL_TRADE_INVOICE_NO,
        ALS_CUSTOMER_NAME);
    result.addAll(qs.getSearchResults(VIEW_SALES, Filter.anyContains(columns, query)));

    if (usr.isModuleVisible(ModuleAndSub.of(getModule(), SubModule.ACTS))) {
      List<SearchResult> actSr = act.doSearch(query);
      if (!BeeUtils.isEmpty(actSr)) {
        result.addAll(actSr);
      }
    }
    return result;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    SubModule subModule = reqInfo.getSubModule();

    if (subModule == SubModule.ACTS) {
      response = act.doService(svc, reqInfo);

    } else if (BeeUtils.same(svc, SVC_ITEMS_INFO)) {
      response = getItemsInfo(reqInfo.getParameter("view_name"),
          BeeUtils.toLongOrNull(reqInfo.getParameter("id")),
          reqInfo.getParameter(COL_CURRENCY), reqInfo.getParameter("TypeTable"));

    } else if (BeeUtils.same(svc, SVC_CREDIT_INFO)) {
      response = getCreditInfo(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_COMPANY)));

    } else if (BeeUtils.same(svc, SVC_GET_DOCUMENT_DATA)) {
      response = getTradeDocumentData(reqInfo);

    } else if (BeeUtils.same(svc, SVC_SEND_TO_ERP)) {
      response = sendToERP(reqInfo.getParameter(VAR_VIEW_NAME),
          DataUtils.parseIdSet(reqInfo.getParameter(VAR_ID_LIST)));

    } else if (BeeUtils.same(svc, SVC_REMIND_DEBTS_EMAIL)) {
      response = sendDebtsRemindEmail(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("Trade service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    return response;
  }

  public static String encodeId(String trade, Long id) {
    Assert.notEmpty(trade);
    Long normalizedId;

    switch (trade) {
      case TBL_PURCHASES:
        normalizedId = id * 2;
        break;

      case TBL_SALES:
        normalizedId = id * 2 + 1;
        break;

      default:
        throw new BeeRuntimeException("View source not supported: " + trade);
    }
    return BeeUtils.toString(normalizedId);
  }

  public static IsExpression getAmountExpression(String tblName) {
    return SqlUtils.multiply(SqlUtils.field(tblName, COL_TRADE_ITEM_QUANTITY),
        SqlUtils.field(tblName, COL_TRADE_ITEM_PRICE));
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

    Map<String, Object> resp = new HashMap<>();

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
    String module = getModule().getName();
    return act.getDefaultParameters(module);
  }

  @Override
  public Module getModule() {
    return Module.TRADE;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  public static IsExpression getTotalExpression(String tblName) {
    return getTotalExpression(tblName, getAmountExpression(tblName));
  }

  public static IsExpression getTotalExpression(String tblName, IsExpression amount) {
    return SqlUtils.plus(amount,
        SqlUtils.sqlIf(SqlUtils.isNull(tblName, COL_TRADE_VAT_PLUS), 0,
            getVatExpression(tblName, amount)));
  }

  public static IsExpression getVatExpression(String tblName) {
    return getVatExpression(tblName, getAmountExpression(tblName));
  }

  public static IsExpression getVatExpression(String tblName, IsExpression amount) {
    return SqlUtils.sqlCase(null,
        SqlUtils.isNull(tblName, COL_TRADE_VAT), 0,
        SqlUtils.isNull(tblName, COL_TRADE_VAT_PERC), SqlUtils.field(tblName, COL_TRADE_VAT),
        SqlUtils.notNull(tblName, COL_TRADE_VAT_PLUS), SqlUtils.multiply(SqlUtils
            .divide(amount, 100), SqlUtils.field(tblName, COL_TRADE_VAT)),
        SqlUtils.multiply(
            SqlUtils.divide(amount, SqlUtils.plus(100, SqlUtils.field(tblName, COL_TRADE_VAT))),
            SqlUtils.field(tblName, COL_TRADE_VAT)));
  }

  public static IsExpression getWithoutVatExpression(String tblName) {
    return getWithoutVatExpression(tblName, getAmountExpression(tblName));
  }

  public static IsExpression getWithoutVatExpression(String tblName, IsExpression amount) {
    return SqlUtils.minus(amount,
        SqlUtils.sqlIf(SqlUtils.notNull(tblName, COL_TRADE_VAT_PLUS), 0,
            getVatExpression(tblName, amount)));
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
            row.addValue(Value.getValue(qs.getNextNumber(TBL_SALES, COL_TRADE_INVOICE_NO, prefix,
                COL_TRADE_INVOICE_PREFIX)));
          }
        }
      }

      @Subscribe
      public void fillOverdueAverageProp(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (BeeUtils.same(event.getTargetName(), VIEW_DEBTS)) {
          BeeRowSet gridRowset = event.getRowset();

          if (gridRowset.isEmpty()) {
            return;
          }

          List<Long> companiesId = Lists.newArrayList();

          for (BeeRow gridRow : gridRowset) {
            int idxCustomer =
                DataUtils.getColumnIndex(COL_TRADE_CUSTOMER, gridRowset.getColumns(), false);

            if (idxCustomer > -1) {
              Long customerId = gridRow.getLong(idxCustomer);

              if (DataUtils.isId(customerId) && !companiesId.contains(customerId)) {
                companiesId.add(customerId);
              }
            }
          }

          SimpleRowSet overdueResult = getDebtsOverdueCount(companiesId);
          Map<Long, String[]> overdueData = Maps.newHashMap();

          for (String[] row : overdueResult.getRows()) {
            overdueData.put(BeeUtils.toLong(row[overdueResult.getColumnIndex(COL_TRADE_CUSTOMER)]),
                row);
          }

          SimpleRowSet salesCountResult = getERPSalesCount(companiesId);
          Map<Long, String[]> salesCountData = Maps.newHashMap();

          for (String[] row : salesCountResult.getRows()) {
            salesCountData.put(BeeUtils.toLong(row[salesCountResult
                .getColumnIndex(COL_TRADE_CUSTOMER)]),
                row);
          }

          SimpleRowSet salesSumResult = getERPSalesOverdueSum(companiesId);
          Map<Long, String[]> salesSumData = Maps.newHashMap();

          for (String[] row : salesSumResult.getRows()) {
            salesSumData.put(BeeUtils.toLong(row[salesSumResult
                .getColumnIndex(COL_TRADE_CUSTOMER)]),
                row);
          }

          for (BeeRow gridRow : gridRowset) {
            int idxCostumer =
                DataUtils.getColumnIndex(COL_TRADE_CUSTOMER, gridRowset.getColumns(), false);
            int idxOverdueCnt = overdueResult.getColumnIndex(ALS_OVERDUE_COUNT);
            int idxOverdueSum = overdueResult.getColumnIndex(ALS_OVERDUE_SUM);

            if (idxCostumer < 0) {
              continue;
            }
            String[] overdueRow =
                overdueData.get(gridRow.getLong(idxCostumer));
            Long oCount =
                overdueRow != null
                    ? BeeUtils.toLong(overdueRow[idxOverdueCnt])
                    : 0L;
            Long oSumDays =
                overdueRow != null
                    ? BeeUtils.toLong(overdueRow[idxOverdueSum])
                    / 86400000L : 0L;

            String[] salesRow = salesCountData.get(gridRow.getLong(
                gridRowset.getColumnIndex(COL_TRADE_CUSTOMER)));

            Long sCount =
                salesRow != null
                    ? BeeUtils.toLong(salesRow[salesCountResult.getColumnIndex(ALS_SALES_COUNT)])
                    : 0L;

            salesRow = salesSumData.get(gridRow.getLong(
                gridRowset.getColumnIndex(COL_TRADE_CUSTOMER)));

            Long sSumDays =
                salesRow != null
                    ? BeeUtils.toLong(salesRow[salesSumResult.getColumnIndex(ALS_SALES_SUM)])
                    / 86400000L : 0L;

            Long sum = oCount + sCount;

            if (sum.compareTo(Long.valueOf(0)) == 0) {
              continue;
            }

            Long days = oSumDays + sSumDays;

            Long avg = days / sum;

            gridRow.setProperty(PROP_AVERAGE_OVERDUE, BeeUtils.toString(avg));
          }

        }
      }

      @Subscribe
      public void fillDebtReportsProperties(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (BeeUtils.same(event.getTargetName(), VIEW_DEBT_REPORTS)) {
          BeeRowSet gridRowset = event.getRowset();

          if (gridRowset.isEmpty()) {
            return;
          }

          List<Long> companiesId = Lists.newArrayList();

          for (IsRow gridRow : gridRowset) {
            companiesId.add(gridRow.getId());
          }

          Map<Long, Map<Long, String>> reminderEmails =
              cls.getCompaniesRemindEmailAddresses(companiesId);

          for (IsRow gridRow : gridRowset) {
            Map<Long, String> emailsData = reminderEmails.get(gridRow.getId());

            if (emailsData.isEmpty()) {
              continue;
            }

            String emails = BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, emailsData.values());

            if (!BeeUtils.isEmpty(emails)) {
              gridRow.setProperty(PROP_REMIND_EMAIL, emails);
            }
          }

        }
      }
    });

    act.init();
  }

  private ResponseObject getItemsInfo(String viewName, Long id, String currencyTo,
      String typeTable) {
    if (!sys.isView(viewName)) {
      return ResponseObject.error("Wrong view name");
    }
    if (!DataUtils.isId(id)) {
      return ResponseObject.error("Wrong document ID");
    }

    String trade = sys.getView(viewName).getSourceName();
    String tradeItems;
    String itemsRelation;
    String articleSource;

    if (BeeUtils.same(trade, TBL_SALES)) {
      tradeItems = TBL_SALE_ITEMS;
      itemsRelation = COL_SALE;
      articleSource = TBL_SALE_ITEMS;
    } else if (BeeUtils.same(trade, TBL_PURCHASES)) {
      tradeItems = TBL_PURCHASE_ITEMS;
      itemsRelation = COL_PURCHASE;
      articleSource = TBL_PURCHASE_ITEMS;
    } else if (BeeUtils.same(trade, TBL_TRADE_ACTS)) {
      itemsRelation = COL_TRADE_ACT;
      tradeItems = typeTable;
      articleSource = TBL_ITEMS;
    } else {
      return ResponseObject.error("View source not supported:", trade);
    }
    SqlSelect query =
        new SqlSelect()
            .addFields(TBL_ITEMS,
                COL_ITEM_NAME, COL_ITEM_NAME + "2", COL_ITEM_NAME + "3", COL_ITEM_BARCODE)
            .addField(TBL_UNITS, COL_UNIT_NAME, COL_UNIT)
            .addFields(tradeItems, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
                COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC, COL_TRADE_ITEM_NOTE)
            .addFields(articleSource, COL_TRADE_ITEM_ARTICLE)
            .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
            .addFrom(tradeItems)
            .addFromInner(trade, sys.joinTables(trade, tradeItems, itemsRelation))
            .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, tradeItems, COL_ITEM))
            .addFromInner(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
            .addFromInner(TBL_CURRENCIES, sys.joinTables(TBL_CURRENCIES, trade, COL_CURRENCY))
            .setWhere(SqlUtils.equals(tradeItems, itemsRelation, id));

    if (BeeUtils.same(trade, TBL_TRADE_ACTS)) {
      query.addFields(tradeItems, COL_TRADE_DISCOUNT);
      query.addFields(TBL_ITEMS, COL_TRADE_WEIGHT);
      query.addFields(TBL_ITEMS, COL_ITEM_AREA);
      query.addFields(TBL_TRADE_ACTS, COL_TRADE_NUMBER);
      query.addFields(TBL_TRADE_ACTS, COL_TRADE_CONTACT);
      query.addFields(TBL_ITEMS, COL_TRADE_TIME_UNIT);

      if (BeeUtils.same(tradeItems, TBL_TRADE_ACT_ITEMS)) {
        query.addFields(TBL_TRADE_ACT_ITEMS, sys.getIdName(tradeItems));
      }

      if (BeeUtils.same(tradeItems, TBL_TRADE_ACT_SERVICES)) {
        query.addFields(TBL_TRADE_ACT_SERVICES, COL_TA_SERVICE_FROM);
        query.addFields(TBL_TRADE_ACT_SERVICES, COL_TA_SERVICE_TO);
        query.addFields(TBL_TRADE_ACT_SERVICES, COL_TA_SERVICE_TARIFF);
        query.addFields(TBL_TRADE_ACT_SERVICES, COL_TA_SERVICE_MIN, COL_TA_SERVICE_DAYS);
      }
    } else {
      query.addOrder(tradeItems, COL_TRADE_ITEM_ORDINAL, sys.getIdName(tradeItems));
    }

    if (!BeeUtils.isEmpty(currencyTo)) {
      String currAlias = SqlUtils.uniqueName();

      IsExpression xpr = ExchangeUtils.exchangeFieldTo(query.addFromLeft(TBL_CURRENCIES, currAlias,
          SqlUtils.equals(currAlias, COL_CURRENCY_NAME, currencyTo)),
          SqlUtils.constant(1),
          SqlUtils.field(trade, COL_CURRENCY),
          SqlUtils.field(trade, COL_TRADE_DATE),
          SqlUtils.field(currAlias, sys.getIdName(TBL_CURRENCIES)));

      query.addExpr(xpr, COL_CURRENCY_RATE)
          .addField(currAlias, COL_CURRENCY_NAME, COL_CURRENCY_RATE + COL_CURRENCY);
    }

    SimpleRowSet simpleRowSet = qs.getData(query);

    if (simpleRowSet.hasColumn("TradeActItemID")) {
      BeeRowSet beeRowSet =
          qs.getViewData(VIEW_TRADE_ACT_ITEMS, Filter.equals(COL_TRADE_ACT, id));

      Map<Long, String> itemsRetQty = new LinkedHashMap<>();
      int colListLength = simpleRowSet.getColumnNames().length;
      String[] colList = new String[colListLength + 1];

      for (int i = 0; i < colListLength; i++) {
        colList[i] = simpleRowSet.getColumnName(i);
      }
      colList[colListLength] = COL_TA_RETURNED_QTY;

      SimpleRowSet resultRowSet = new SimpleRowSet(colList);

      for (int i = 0; i < simpleRowSet.getNumberOfRows(); i++) {
        resultRowSet.addEmptyRow();
        for (int j = 0; j < colListLength; j++) {
          resultRowSet.getRow(i).setValue(j, simpleRowSet.getRow(i).getValue(j));
        }
        BeeRow row = beeRowSet.getRow(i);
        itemsRetQty.put(row.getId(), row.getProperty(PRP_RETURNED_QTY));
      }

      for (SimpleRow sr : resultRowSet) {
        sr.setValue(COL_TA_RETURNED_QTY,
            itemsRetQty.get(sr.getLong("TradeActItemID")));
      }
      return ResponseObject.response(resultRowSet);
    } else {
      return ResponseObject.response(simpleRowSet);
    }
  }

  private SimpleRowSet getDebtsOverdueCount(List<Long> companyIds) {
    SqlSelect select = new SqlSelect().addFields(TBL_ERP_SALES, COL_TRADE_CUSTOMER);
    select.addCount(ALS_OVERDUE_COUNT);
    select.addSum(SqlUtils.minus(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_TERM),
        new JustDate().getTime()), ALS_OVERDUE_SUM);
    select.setDistinctMode(true);
    select.addFrom(TBL_ERP_SALES);
    select.setWhere(SqlUtils.and(
        SqlUtils.or(SqlUtils.inList(TBL_ERP_SALES, COL_SALE_PAYER, companyIds),
            SqlUtils.and(SqlUtils.isNull(TBL_ERP_SALES, COL_SALE_PAYER),
                SqlUtils.inList(TBL_ERP_SALES, COL_TRADE_CUSTOMER, companyIds)
                )),
        SqlUtils.or(SqlUtils.less(TBL_ERP_SALES, COL_TRADE_TERM, (new JustDate()).getTime()), SqlUtils
            .isNull(TBL_ERP_SALES, COL_TRADE_TERM)),
        SqlUtils.less(SqlUtils.minus(SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_PAID), 0),
            SqlUtils.field(TBL_ERP_SALES, COL_TRADE_AMOUNT)), 0)
        ));

    select.addGroup(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_CUSTOMER));

    return qs.getData(select);
  }

  private static Integer getOverdueInDays(BeeRowSet rs, IsRow row) {
    Integer overdue = null;

    if (rs == null || row == null) {
      return overdue;
    }

    int idxDate = rs.getColumnIndex(COL_TRADE_DATE);
    int idxTerm = rs.getColumnIndex(COL_TRADE_TERM);

    if (idxDate < 0) {
      return overdue;
    }

    if (idxTerm < 0) {
      idxTerm = idxDate;
    }

    int start = 0;
    int end = (new JustDate()).getDays();

    if (row.getDate(idxTerm) != null) {
      start = row.getDate(idxTerm).getDays();
    } else if (row.getDateTime(idxDate) != null) {
      start = row.getDateTime(idxDate).getDate().getDays();
    } else {
      return overdue;
    }

    overdue = Integer.valueOf(end - start);

    return overdue;
  }

  private SimpleRowSet getERPSalesCount(List<Long> companyIds) {
    SqlSelect select = new SqlSelect().addFields(TBL_ERP_SALES, COL_TRADE_CUSTOMER);
    select.addCount(ALS_SALES_COUNT);
    select.setDistinctMode(true);
    select.addFrom(TBL_ERP_SALES);
    select.setWhere(SqlUtils.and(
        SqlUtils.or(SqlUtils.inList(TBL_ERP_SALES, COL_SALE_PAYER, companyIds),
            SqlUtils.and(SqlUtils.isNull(TBL_ERP_SALES, COL_SALE_PAYER),
                SqlUtils.inList(TBL_ERP_SALES, COL_TRADE_CUSTOMER, companyIds)
                )),
        SqlUtils.equals(TBL_ERP_SALES, COL_TRADE_PAID, SqlUtils.field(TBL_ERP_SALES, COL_TRADE_AMOUNT)),
        SqlUtils.notNull(TBL_ERP_SALES, COL_TRADE_TERM),
        SqlUtils.notNull(TBL_ERP_SALES, COL_TRADE_PAYMENT_TIME)));
    select.addGroup(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_CUSTOMER));

    return qs.getData(select);
  }

  private SimpleRowSet getERPSalesOverdueSum(List<Long> companyIds) {

    SqlSelect select = new SqlSelect().addFields(TBL_ERP_SALES, COL_TRADE_CUSTOMER);
    select.addSum(SqlUtils.minus(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_TERM),
        SqlUtils.field(TBL_ERP_SALES, COL_TRADE_PAYMENT_TIME)), ALS_SALES_SUM);
    select.setDistinctMode(true);
    select.addFrom(TBL_ERP_SALES);
    select.setWhere(SqlUtils.and(
        SqlUtils.or(SqlUtils.inList(TBL_ERP_SALES, COL_SALE_PAYER, companyIds),
            SqlUtils.and(SqlUtils.isNull(TBL_ERP_SALES, COL_SALE_PAYER),
                SqlUtils.inList(TBL_ERP_SALES, COL_TRADE_CUSTOMER, companyIds)
                )),
        SqlUtils.less(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_TERM), SqlUtils
            .field(TBL_ERP_SALES, COL_TRADE_PAYMENT_TIME)),
        SqlUtils.equals(TBL_ERP_SALES, COL_TRADE_AMOUNT, SqlUtils.field(TBL_ERP_SALES, COL_TRADE_PAID)),
        SqlUtils.notNull(TBL_ERP_SALES, COL_TRADE_PAYMENT_TIME),
        SqlUtils.notNull(TBL_ERP_SALES, COL_TRADE_TERM)));
    select.addGroup(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_CUSTOMER));

    return qs.getData(select);
  }

  private ResponseObject getTradeDocumentData(RequestInfo reqInfo) {
    Long docId = reqInfo.getParameterLong(Service.VAR_ID);
    if (!DataUtils.isId(docId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), Service.VAR_ID);
    }

    String itemViewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    if (BeeUtils.isEmpty(itemViewName)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), Service.VAR_VIEW_NAME);
    }

    String itemRelation = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(itemRelation)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), Service.VAR_COLUMN);
    }

    Set<Long> companyIds = DataUtils.parseIdSet(reqInfo.getParameter(VIEW_COMPANIES));

    Set<String> currencyNames = new HashSet<>();

    String[] arr = Codec.beeDeserializeCollection(reqInfo.getParameter(VIEW_CURRENCIES));
    if (arr != null) {
      for (String s : arr) {
        currencyNames.add(s);
      }
    }

    BeeRowSet companies;
    BeeRowSet bankAccounts;

    if (companyIds.isEmpty()) {
      companies = null;
      bankAccounts = null;

    } else {
      companies = qs.getViewData(VIEW_COMPANIES, Filter.idIn(companyIds));
      bankAccounts = qs.getViewData(VIEW_COMPANY_BANK_ACCOUNTS,
          Filter.any(COL_COMPANY, companyIds));
    }

    BeeRowSet items = qs.getViewData(itemViewName, Filter.equals(itemRelation, docId));

    TradeDocumentData tdd = new TradeDocumentData(companies, bankAccounts, items, null, null);
    return ResponseObject.response(tdd);
  }

  private Document renderCompanyDebtMail(String subject, String p1,
      String p2, Long companyId) {
    Document doc = new Document();
    doc.getHead().append(meta().encodingDeclarationUtf8(), title().text(subject));
    doc.getBody().setColor("black");

    Pre pre = pre().text(p1);
    pre.setWordWrap(WordWrap.BREAK_WORD);
    pre.setFontFamily("sans-serif");
    pre.setFontSize(11, CssUnit.PT);
    String first = pre.build();

    pre = pre().text(p2);
    pre.setWordWrap(WordWrap.BREAK_WORD);
    pre.setFontFamily("sans-serif");
    pre.setFontSize(11, CssUnit.PT);
    String last = pre.build();

    Filter filter = Filter.and(
        Filter.isEqual(COL_TRADE_CUSTOMER, Value.getValue(companyId)),
        Filter.compareWithValue(COL_TRADE_DEBT, Operator.GT, Value.getValue(0)));

    BeeRowSet rs = qs.getViewData(VIEW_DEBTS, filter, null,
        Lists.newArrayList(COL_TRADE_INVOICE_NO, COL_TRADE_DATE,
            COL_TRADE_TERM, COL_TRADE_AMOUNT, COL_TRADE_DEBT, ALS_CURRENCY_NAME,
            ALS_CUSTOMER_NAME, COL_SERIES_NAME));

    if (rs.isEmpty()) {
      return null;
    }

    Map<String, Object> creditInfo = Maps.newHashMap();
    ResponseObject resp = getCreditInfo(companyId);
    Double debt = null;

    if (resp.getResponse() instanceof Map) {
      creditInfo = resp.getResponse(creditInfo, logger);
      if (creditInfo.get(VAR_DEBT) instanceof Double) {
        debt = (Double) creditInfo.get(VAR_DEBT);
      }
    }

    int ignoreLast = 2;

    Table table = table();
    Caption caption = caption()
        .text(rs.getRows().get(0).getString(rs.getColumnIndex(ALS_CUSTOMER_NAME)));

    caption.setTextAlign(TextAlign.LEFT);
    table.append(caption);
    Tr trHead = tr();

    for (int i = 0; i < rs.getNumberOfColumns() - ignoreLast; i++) {
      String label = Localized.maybeTranslate(rs.getColumnLabel(i), usr.getLocalizableDictionary());

      if (BeeUtils.same(rs.getColumnId(i), COL_TRADE_INVOICE_NO)) {
        label = usr.getLocalizableConstants().trdInvoice();
      }

      if (BeeUtils.same(rs.getColumnId(i), COL_TRADE_AMOUNT)) {
        label = usr.getLocalizableConstants().trdAmount();
      }
      Th th = th().text(label);
      th.setBorderWidth("1px");
      th.setBorderStyle(BorderStyle.SOLID);
      th.setBorderColor("black");
      trHead.append(th);
    }

    Th th = th().text(usr.getLocalizableConstants().trdOverdueInDays());
    th.setBorderWidth("1px");
    th.setBorderStyle(BorderStyle.SOLID);
    th.setBorderColor("black");
    trHead.insert(rs.getColumnIndex(COL_TRADE_TERM) + 1, th);

    table.append(trHead);

    Range<Long> maybeTime = Range.closed(
        TimeUtils.startOfYear(TimeUtils.today(), -10).getTime(),
        TimeUtils.startOfYear(TimeUtils.today(), 100).getTime());

    for (IsRow row : rs) {
      Tr tr = tr();

      for (int i = 0; i < rs.getNumberOfColumns() - ignoreLast; i++) {
        if (row.isNull(i)) {
          tr.append(td());
          continue;
        }

        ValueType type = rs.getColumnType(i);
        String value = DataUtils.render(rs.getColumn(i), row, i);

        if (type == ValueType.LONG) {
          Long x = row.getLong(i);
          if (x != null && maybeTime.contains(x)) {
            type = ValueType.DATE_TIME;
            value = new JustDate(x).toString();
          }
        }

        if (type == ValueType.DATE_TIME) {
          value = new JustDate(row.getLong(i)).toString();
        }

        if (BeeUtils.same(rs.getColumnId(i), COL_TRADE_INVOICE_NO)) {
          type = ValueType.TEXT;

          int idxInvoicePref = rs.getColumnIndex(COL_SERIES_NAME);

          if (idxInvoicePref > -1
              && !BeeUtils.isEmpty(row.getString(idxInvoicePref))) {
            value = BeeUtils.joinWords(row.getString(idxInvoicePref),
                row.getString(i));
          }
        }

        Td td = td();
        tr.append(td);
        td.text(value);
        td.setPadding("0 5px 0 5px");

        if (ValueType.isNumeric(type) || ValueType.TEXT == type
            && CharMatcher.DIGIT.matchesAnyOf(value) && BeeUtils.isDouble(value)) {
          if (!BeeUtils.same(rs.getColumnId(i), COL_TRADE_INVOICE_NO)) {
            td.setTextAlign(TextAlign.RIGHT);
          }
        }

      }

      Integer overdue = getOverdueInDays(rs, row);
      Td td = td();
      tr.insert(rs.getColumnIndex(COL_TRADE_TERM) + 1, td);
      td.text(overdue == null ? BeeConst.STRING_EMPTY : BeeUtils.toString(overdue));
      td.setPadding("0 5px 0 5px");
      td.setTextAlign(TextAlign.RIGHT);

      table.append(tr);
    }

    Tr footer = tr();
    for (int i = 0; i < rs.getNumberOfColumns() - ignoreLast - 3; i++) {
      footer.append(td());
    }
    footer.append(td());

    footer.append(td().append(b().text(usr.getLocalizableConstants().total())));
    footer.append(td().text(BeeUtils.notEmpty(BeeUtils.toString(debt), BeeConst.STRING_EMPTY)));
    table.append(footer);
    footer.append(td());

    table.setBorderWidth("1px;");
    table.setBorderStyle(BorderStyle.NONE);
    table.setBorderSpacing("0px;");
    table.setFontFamily("sans-serif");
    table.setFontSize(10, CssUnit.PT);

    doc.getBody().append(p().text(first));
    doc.getBody().append(table);
    doc.getBody().append(p().text(last));
    return doc;
  }

  private ResponseObject sendDebtsRemindEmail(RequestInfo req) {
    Long senderMailAccountId = mail.getSenderAccountId(SVC_REMIND_DEBTS_EMAIL);
    ResponseObject resp = ResponseObject.emptyResponse();

    if (!DataUtils.isId(senderMailAccountId)) {
      return ResponseObject.error(usr.getLocalizableConstants().mailAccountNotFound());
    }

    String subject = req.getParameter(VAR_SUBJECT);
    String p1 = req.getParameter(VAR_HEADER);
    String p2 = req.getParameter(VAR_FOOTER);
    List<Long> ids = DataUtils.parseIdList(req.getParameter(VAR_ID_LIST));

    Map<Long, Map<Long, String>> emails = cls.getCompaniesRemindEmailAddresses(ids);
    Map<Long, String> errorMails = Maps.newHashMap();
    Set<Long> sentEmailCompanyIds = Sets.newHashSet();

    for (Long companyId : emails.keySet()) {
      if (BeeUtils.isEmpty(emails.get(companyId).values())) {
        errorMails.put(companyId, usr.getLocalizableConstants().mailRecipientAddressNotFound());
        continue;
      }

      Document mailDocument = renderCompanyDebtMail(subject, p1, p2, companyId);

      if (mailDocument == null) {
        errorMails.put(companyId, usr.getLocalizableConstants().noData());
        continue;
      }

      try {
//        logger.info(mailDocument.buildLines());
        MailAccount account = mailStore.getAccount(senderMailAccountId);
        MimeMessage message = mail.sendMail(account,
            ArrayUtils.toArray(
                Lists.newArrayList(emails.get(companyId).values()
                    )), null, null, subject, mailDocument
                .buildLines(),
            null);

        mail.storeMessage(account, message, account.getSentFolder(), null);
        sentEmailCompanyIds.add(companyId);
      } catch (MessagingException | BeeRuntimeException ex) {
        logger.error(ex);
        errorMails.put(companyId, ex.getMessage());
      }
    }

    String message = BeeUtils.joinWords(usr.getLocalizableConstants().mailMessageSentCount(),
        sentEmailCompanyIds.size(), br().build());

    if (!BeeUtils.isEmpty(errorMails.keySet())) {
      message = BeeUtils.joinWords(message, usr.getLocalizableConstants().errors(), br().build());

      Filter filter = Filter.idIn(errorMails.keySet());
      BeeRowSet rs =
          qs.getViewData(VIEW_COMPANIES, filter, null, Lists.newArrayList(COL_COMPANY_NAME));
      int i = 0;
      for (Long id : errorMails.keySet()) {

        message = BeeUtils.joinWords(message,
            rs.getRowById(id).getString(rs.getColumnIndex(COL_COMPANY_NAME)), errorMails.get(id)
            , br().build());

        if (i > 5) {
          message = BeeUtils.joinWords(message, br().build(),
              BeeUtils.bracket(BeeConst.ELLIPSIS), errorMails.keySet().size() - i);
          break;
        }
        i++;
      }
    }

    resp.setResponse(message);

    return resp;
  }

  private ResponseObject sendToERP(String viewName, Set<Long> ids) {
    if (!sys.isView(viewName)) {
      return ResponseObject.error("Wrong view name");
    }
    String trade = sys.getView(viewName).getSourceName();
    String tradeItems;
    String itemsRelation;

    SqlSelect query =
        new SqlSelect()
            .addFields(trade, COL_TRADE_DATE, COL_TRADE_INVOICE_NO,
                COL_TRADE_NUMBER, COL_TRADE_TERM, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER)
            .addField(TBL_SALE_SERIES, COL_SERIES_NAME, COL_TRADE_INVOICE_PREFIX)
            .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
            .addField(COL_TRADE_WAREHOUSE_FROM, COL_WAREHOUSE_CODE, COL_TRADE_WAREHOUSE_FROM)
            .addFrom(trade)
            .addFromLeft(TBL_SALE_SERIES,
                sys.joinTables(TBL_SALE_SERIES, trade, COL_TRADE_SALE_SERIES))
            .addFromLeft(TBL_CURRENCIES, sys.joinTables(TBL_CURRENCIES, trade, COL_CURRENCY))
            .addFromLeft(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM,
                sys.joinTables(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM, trade,
                    COL_TRADE_WAREHOUSE_FROM))
            .setWhere(sys.idInList(trade, ids));

    if (BeeUtils.same(trade, TBL_SALES)) {
      tradeItems = TBL_SALE_ITEMS;
      itemsRelation = COL_SALE;
      query.addFields(trade, COL_SALE_PAYER);

    } else if (BeeUtils.same(trade, TBL_PURCHASES)) {
      tradeItems = TBL_PURCHASE_ITEMS;
      itemsRelation = COL_PURCHASE;
      query.addField(COL_PURCHASE_WAREHOUSE_TO, COL_WAREHOUSE_CODE, COL_PURCHASE_WAREHOUSE_TO)
          .addFromLeft(TBL_WAREHOUSES, COL_PURCHASE_WAREHOUSE_TO,
              sys.joinTables(TBL_WAREHOUSES, COL_PURCHASE_WAREHOUSE_TO, trade,
                  COL_PURCHASE_WAREHOUSE_TO));
    } else {
      return ResponseObject.error("View source not supported:", trade);
    }
    String remoteNamespace = prm.getText(PRM_ERP_NAMESPACE);
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);

    SimpleRowSet invoices = qs.getData(query.addField(trade, sys.getIdName(trade), itemsRelation));
    Map<Long, String> companies = new HashMap<>();
    ResponseObject response = ResponseObject.emptyResponse();

    for (SimpleRow invoice : invoices) {
      for (String col : new String[] {COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER}) {
        Long id = invoices.hasColumn(col) ? invoice.getLong(col) : null;

        if (DataUtils.isId(id) && !companies.containsKey(id)) {
          SimpleRow data = qs.getRow(new SqlSelect()
              .addFields(TBL_COMPANIES, COL_COMPANY_NAME, COL_COMPANY_CODE, COL_COMPANY_VAT_CODE)
              .addField(TBL_COMPANY_TYPES, COL_COMPANY_TYPE_NAME, COL_COMPANY_TYPE)
              .addFields(TBL_CONTACTS, COL_ADDRESS, COL_POST_INDEX)
              .addField(TBL_CITIES, COL_CITY_NAME, COL_CITY)
              .addField(TBL_COUNTRIES, COL_COUNTRY_NAME, COL_COUNTRY)
              .addFrom(TBL_COMPANIES)
              .addFromLeft(TBL_COMPANY_TYPES,
                  sys.joinTables(TBL_COMPANY_TYPES, TBL_COMPANIES, COL_COMPANY_TYPE))
              .addFromLeft(TBL_CONTACTS, sys.joinTables(TBL_CONTACTS, TBL_COMPANIES, COL_CONTACT))
              .addFromLeft(TBL_CITIES, sys.joinTables(TBL_CITIES, TBL_CONTACTS, COL_CITY))
              .addFromLeft(TBL_COUNTRIES, sys.joinTables(TBL_COUNTRIES, TBL_CONTACTS, COL_COUNTRY))
              .setWhere(sys.idEquals(TBL_COMPANIES, id)));

          try {
            String company = BeeUtils.joinItems(data.getValue(COL_COMPANY_NAME),
                data.getValue(COL_COMPANY_TYPE));

            company = ButentWS.connect(remoteNamespace, remoteAddress, remoteLogin,
                remotePassword)
                .importClient(company, data.getValue(COL_COMPANY_CODE),
                    data.getValue(COL_COMPANY_VAT_CODE), data.getValue(COL_ADDRESS),
                    data.getValue(COL_POST_INDEX), data.getValue(COL_CITY),
                    data.getValue(COL_COUNTRY));

            companies.put(id, company);

          } catch (BeeException e) {
            response.addError(e);
          }
        }
      }
      if (response.hasErrors()) {
        break;
      }
      String operation;
      String warehouse;
      String client;

      if (invoices.hasColumn(COL_PURCHASE_WAREHOUSE_TO)) {
        operation = prm.getText(PRM_ERP_PURCHASE_OPERATION);
        warehouse = invoice.getValue(COL_PURCHASE_WAREHOUSE_TO);
        client = companies.get(invoice.getLong(COL_TRADE_SUPPLIER));
      } else {
        operation = prm.getText(PRM_ERP_OPERATION);
        warehouse = invoice.getValue(COL_TRADE_WAREHOUSE_FROM);
        client = companies.get(invoice.getLong(COL_TRADE_CUSTOMER));
      }
      if (BeeUtils.isEmpty(warehouse)) {
        warehouse = prm.getRelationInfo(PRM_ERP_WAREHOUSE).getB();
      }
      WSDocument doc = new WSDocument(encodeId(trade, invoice.getLong(itemsRelation)),
          invoice.getDateTime(COL_TRADE_DATE), operation, client, warehouse);

      if (invoices.hasColumn(COL_SALE_PAYER)) {
        doc.setPayer(companies.get(invoice.getLong(COL_SALE_PAYER)));
      }
      doc.setNumber(invoice.getValue(COL_TRADE_NUMBER));
      doc.setInvoice(invoice.getValue(COL_TRADE_INVOICE_PREFIX),
          invoice.getValue(COL_TRADE_INVOICE_NO));
      doc.setSupplier(companies.get(invoice.getLong(COL_TRADE_SUPPLIER)));
      doc.setCustomer(companies.get(invoice.getLong(COL_TRADE_CUSTOMER)));
      doc.setTerm(invoice.getDate(COL_TRADE_TERM));
      doc.setCurrency(invoice.getValue(COL_CURRENCY));

      SimpleRowSet items = qs.getData(new SqlSelect()
          .addFields(TBL_ITEMS, COL_ITEM_NAME, COL_ITEM_EXTERNAL_CODE)
          .addFields(tradeItems, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_TRADE_VAT_PLUS,
              COL_TRADE_VAT, COL_TRADE_VAT_PERC, COL_TRADE_ITEM_ARTICLE, COL_TRADE_ITEM_NOTE)
          .addFrom(tradeItems)
          .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, tradeItems, COL_ITEM))
          .setWhere(SqlUtils.equals(tradeItems, itemsRelation, invoice.getLong(itemsRelation))));

      for (SimpleRow item : items) {
        if (BeeUtils.isEmpty(item.getValue(COL_ITEM_EXTERNAL_CODE))) {
          response.addError("Item", BeeUtils.bracket(item.getValue(COL_ITEM_NAME)),
              "does not have ERP code");
          break;
        }
        WSDocumentItem wsItem = doc.addItem(item.getValue(COL_ITEM_EXTERNAL_CODE),
            item.getValue(COL_TRADE_ITEM_QUANTITY));

        wsItem.setPrice(item.getValue(COL_TRADE_ITEM_PRICE));
        wsItem.setVat(item.getValue(COL_TRADE_VAT), item.getBoolean(COL_TRADE_VAT_PERC),
            item.getBoolean(COL_TRADE_VAT_PLUS));
        wsItem.setArticle(item.getValue(COL_TRADE_ITEM_ARTICLE));
        wsItem.setNote(item.getValue(COL_TRADE_ITEM_NOTE));
      }
      if (response.hasErrors()) {
        break;
      }
      try {
        ButentWS.connect(remoteNamespace, remoteAddress, remoteLogin, remotePassword)
            .importDoc(doc);
      } catch (BeeException e) {
        response.addError(e);
        break;
      }
      qs.updateData(new SqlUpdate(trade)
          .addConstant(COL_TRADE_EXPORTED, System.currentTimeMillis())
          .setWhere(sys.idEquals(trade, invoice.getLong(itemsRelation))));
    }
    if (response.hasErrors()) {
      response.log(logger);
    }
    return response;
  }
}
