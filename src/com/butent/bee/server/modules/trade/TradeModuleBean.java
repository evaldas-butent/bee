package com.butent.bee.server.modules.trade;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
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
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.trade.TradeDocumentData;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  @EJB
  ParamHolderBean prm;
  @EJB
  TradeActBean act;

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
          reqInfo.getParameter(COL_CURRENCY));

    } else if (BeeUtils.same(svc, SVC_CREDIT_INFO)) {
      response = getCreditInfo(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_COMPANY)));

    } else if (BeeUtils.same(svc, SVC_GET_DOCUMENT_DATA)) {
      response = getTradeDocumentData(reqInfo);

    } else if (BeeUtils.same(svc, SVC_SEND_TO_ERP)) {
      response = sendToERP(reqInfo.getParameter(VAR_VIEW_NAME),
          DataUtils.parseIdSet(reqInfo.getParameter(VAR_ID_LIST)));

    } else if (BeeUtils.same(svc, SVC_SEND_COMPANY_TO_ERP)) {
      response = sendCompanyToERP(reqInfo.getParameterLong(COL_COMPANY));

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
        if (BeeUtils.same(sys.getViewSource(event.getTargetName()), TBL_SALES)
            && event.isBefore()) {
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
          int prefixIdx = DataUtils.getColumnIndex(COL_TRADE_SALE_SERIES, cols);

          if (!BeeConst.isUndef(prefixIdx)) {
            prefix = row.getString(prefixIdx);
          }
          if (!BeeUtils.isEmpty(prefix)) {
            int numberIdx = DataUtils.getColumnIndex(COL_TRADE_INVOICE_NO, cols);

            if (BeeConst.isUndef(numberIdx)) {
              cols.add(new BeeColumn(COL_TRADE_INVOICE_NO));
              row.addValue(null);
              numberIdx = row.getNumberOfCells() - 1;

            } else if (!BeeUtils.isEmpty(row.getString(numberIdx))) {
              return;
            }
            row.setValue(numberIdx, qs.getNextNumber(TBL_SALES, COL_TRADE_INVOICE_NO, prefix,
                COL_TRADE_SALE_SERIES));
          }
        }
      }
    });

    act.init();
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
        .addFields(TBL_ITEMS,
            COL_ITEM_NAME, COL_ITEM_NAME + "2", COL_ITEM_NAME + "3", COL_ITEM_BARCODE)
        .addField(TBL_UNITS, COL_UNIT_NAME, COL_UNIT)
        .addFields(tradeItems, COL_ITEM_ARTICLE, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
            COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC, COL_TRADE_ITEM_NOTE)
        .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
        .addFrom(tradeItems)
        .addFromInner(trade, sys.joinTables(trade, tradeItems, itemsRelation))
        .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, tradeItems, COL_ITEM))
        .addFromInner(TBL_UNITS, sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT))
        .addFromInner(TBL_CURRENCIES, sys.joinTables(TBL_CURRENCIES, trade, COL_CURRENCY))
        .setWhere(SqlUtils.equals(tradeItems, itemsRelation, id))
        .addOrder(tradeItems, COL_TRADE_ITEM_ORDINAL, sys.getIdName(tradeItems));

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
    return ResponseObject.response(qs.getData(query));
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

  private ResponseObject sendCompanyToERP(Long companyId) {
    ResponseObject response = ResponseObject.emptyResponse();
    SimpleRow data = null;

    if (DataUtils.isId(companyId)) {
      data = qs.getRow(new SqlSelect()
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
          .setWhere(sys.idEquals(TBL_COMPANIES, companyId)));
    }
    if (data != null) {
      try {
        String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
        String remoteLogin = prm.getText(PRM_ERP_LOGIN);
        String remotePassword = prm.getText(PRM_ERP_PASSWORD);

        String company = BeeUtils.joinItems(data.getValue(COL_COMPANY_NAME),
            data.getValue(COL_COMPANY_TYPE));

        company = ButentWS.connect(remoteAddress, remoteLogin, remotePassword)
            .importClient(company, data.getValue(COL_COMPANY_CODE),
                data.getValue(COL_COMPANY_VAT_CODE), data.getValue(COL_ADDRESS),
                data.getValue(COL_POST_INDEX), data.getValue(COL_CITY),
                data.getValue(COL_COUNTRY));

        response.setResponse(company);

      } catch (BeeException e) {
        response.addError(e);
      }
    } else {
      response.addError("Wrong company id", companyId);
    }
    return response;
  }

  private ResponseObject sendToERP(String viewName, Set<Long> ids) {
    if (!sys.isView(viewName)) {
      return ResponseObject.error("Wrong view name");
    }
    String trade = sys.getView(viewName).getSourceName();
    String tradeItems;
    String itemsRelation;

    SqlSelect query = new SqlSelect()
        .addFields(trade, COL_TRADE_DATE, COL_TRADE_INVOICE_NO,
            COL_TRADE_NUMBER, COL_TRADE_TERM, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER)
        .addFields(TBL_TRADE_OPERATIONS, COL_OPERATION_NAME)
        .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
        .addField(COL_TRADE_WAREHOUSE_FROM, COL_WAREHOUSE_CODE, COL_TRADE_WAREHOUSE_FROM)
        .addField(TBL_USERS, "EmployerId", COL_TRADE_MANAGER)
        .addFrom(trade)
        .addFromLeft(TBL_TRADE_OPERATIONS,
            sys.joinTables(TBL_TRADE_OPERATIONS, trade, COL_TRADE_OPERATION))
        .addFromLeft(TBL_CURRENCIES, sys.joinTables(TBL_CURRENCIES, trade, COL_CURRENCY))
        .addFromLeft(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM,
            sys.joinTables(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM, trade,
                COL_TRADE_WAREHOUSE_FROM))
        .addFromLeft(TBL_USERS, sys.joinTables(TBL_USERS, trade, COL_TRADE_MANAGER))
        .setWhere(sys.idInList(trade, ids));

    switch (trade) {
      case TBL_SALES:
        tradeItems = TBL_SALE_ITEMS;
        itemsRelation = COL_SALE;

        query.addField(TBL_SALES_SERIES, COL_SERIES_NAME, COL_TRADE_INVOICE_PREFIX)
            .addFields(trade, COL_SALE_PAYER)
            .addFromLeft(TBL_SALES_SERIES,
                sys.joinTables(TBL_SALES_SERIES, trade, COL_TRADE_SALE_SERIES));
        break;

      case TBL_PURCHASES:
        tradeItems = TBL_PURCHASE_ITEMS;
        itemsRelation = COL_PURCHASE;

        query.addFields(trade, COL_TRADE_INVOICE_PREFIX)
            .addField(COL_PURCHASE_WAREHOUSE_TO, COL_WAREHOUSE_CODE, COL_PURCHASE_WAREHOUSE_TO)
            .addFromLeft(TBL_WAREHOUSES, COL_PURCHASE_WAREHOUSE_TO,
                sys.joinTables(TBL_WAREHOUSES, COL_PURCHASE_WAREHOUSE_TO, trade,
                    COL_PURCHASE_WAREHOUSE_TO));
        break;

      default:
        return ResponseObject.error("View source not supported:", trade);
    }
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

            company = ButentWS.connect(remoteAddress, remoteLogin, remotePassword)
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
      String warehouse;
      String client;

      if (invoices.hasColumn(COL_PURCHASE_WAREHOUSE_TO)) {
        warehouse = invoice.getValue(COL_PURCHASE_WAREHOUSE_TO);
        client = companies.get(invoice.getLong(COL_TRADE_SUPPLIER));
      } else {
        warehouse = invoice.getValue(COL_TRADE_WAREHOUSE_FROM);
        client = companies.get(invoice.getLong(COL_TRADE_CUSTOMER));
      }
      WSDocument doc = new WSDocument(encodeId(trade, invoice.getLong(itemsRelation)),
          TimeUtils.startOfDay(invoice.getDateTime(COL_TRADE_DATE)),
          invoice.getValue(COL_OPERATION_NAME), client, warehouse);

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
      doc.setManager(invoice.getValue(COL_TRADE_MANAGER));

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

        String article = item.getValue(COL_TRADE_ITEM_ARTICLE);
        Long incomeId = BeeUtils.toLongOrNull(ArrayUtils.getQuietly(BeeUtils.split(article, '_'),
            1));

        if (DataUtils.isId(incomeId)) {
          if (BeeUtils.same(tradeItems, TBL_SALE_ITEMS)) {
            article = BeeUtils.join("_",
                BeeUtils.joinWords(invoice.getValue(COL_TRADE_INVOICE_PREFIX),
                    invoice.getValue(COL_TRADE_INVOICE_NO)), incomeId);
          } else {
            SimpleRow row = qs.getRow(new SqlSelect()
                .addField(TBL_SALES_SERIES, COL_SERIES_NAME, COL_TRADE_INVOICE_PREFIX)
                .addFields(TBL_SALES, COL_TRADE_INVOICE_NO)
                .addFrom(TransportConstants.TBL_CARGO_INCOMES)
                .addFromInner(TBL_SALES,
                    sys.joinTables(TBL_SALES, TransportConstants.TBL_CARGO_INCOMES, COL_SALE))
                .addFromLeft(TBL_SALES_SERIES,
                    sys.joinTables(TBL_SALES_SERIES, TBL_SALES, COL_TRADE_SALE_SERIES))
                .setWhere(sys.idEquals(TransportConstants.TBL_CARGO_INCOMES, incomeId)));

            if (row != null) {
              article = BeeUtils.join("_",
                  BeeUtils.joinWords(row.getValue(COL_TRADE_INVOICE_PREFIX),
                      row.getValue(COL_TRADE_INVOICE_NO)), incomeId);
            }
          }
        }
        wsItem.setArticle(article);
        wsItem.setNote(item.getValue(COL_TRADE_ITEM_NOTE));
      }
      if (response.hasErrors()) {
        break;
      }
      try {
        ButentWS.connect(remoteAddress, remoteLogin, remotePassword)
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
