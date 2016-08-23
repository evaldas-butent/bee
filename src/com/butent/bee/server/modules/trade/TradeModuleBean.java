package com.butent.bee.server.modules.trade;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.server.concurrency.ConcurrencyBean;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
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
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.websocket.Endpoint;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.BorderStyle;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.css.values.WordWrap;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
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
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.modules.trade.TradeConstants.OperationType;
import com.butent.bee.shared.modules.trade.TradeDocumentData;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
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
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Stateless
@LocalBean
public class TradeModuleBean implements BeeModule, ConcurrencyBean.HasTimerService {

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
  ConcurrencyBean cb;
  @EJB
  TradeActBean act;
  @EJB
  DataEditorBean deb;
  @EJB
  MailModuleBean mail;
  @EJB
  MailStorageBean mailStore;

  @EJB
  ClassifiersModuleBean cls;

  @Resource
  TimerService timerService;

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

    } else if (BeeUtils.same(svc, SVC_GET_DOCUMENT_TYPE_CAPTION_AND_FILTER)) {
      response = getDocumentTypeCaptionAndFilter(reqInfo);

    } else if (BeeUtils.same(svc, SVC_DOCUMENT_PHASE_TRANSITION)) {
      response = tryPhaseTransition(reqInfo);

    } else if (BeeUtils.same(svc, SVC_REMIND_DEBTS_EMAIL)) {
      response = sendDebtsRemindEmail(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_SALE_AMOUNTS)) {
      response = getSaleAmounts(reqInfo.getParameter(VAR_VIEW_NAME),
          reqInfo.getParameter(Service.VAR_COLUMN),
          Filter.restore(reqInfo.getParameter(EcConstants.VAR_FILTER)));

    } else if (BeeUtils.same(svc, SVC_SEND_COMPANY_TO_ERP)) {
      response = sendCompanyToERP(reqInfo.getParameterLong(COL_COMPANY));
    } else {
      String msg = BeeUtils.joinWords("Trade service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    return response;
  }

  @Override
  public void ejbTimeout(Timer timer) {
    if (cb.isParameterTimer(timer, PRM_ERP_REFRESH_INTERVAL)) {
      importERPPayments();
    }
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
          .addFields(TBL_ERP_SALES, COL_TRADE_DATE, COL_TRADE_TERM)
          .addFrom(TBL_ERP_SALES)
          .setWhere(SqlUtils.and(SqlUtils.or(SqlUtils.equals(TBL_ERP_SALES, COL_SALE_PAYER,
              companyId),
              SqlUtils.and(SqlUtils.isNull(TBL_ERP_SALES, COL_SALE_PAYER),
                  SqlUtils.equals(TBL_ERP_SALES, COL_TRADE_CUSTOMER, companyId))),
              SqlUtils.more(SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_DEBT), 0), 0)));

      if (DataUtils.isId(curr)) {
        query.addExpr(ExchangeUtils.exchangeFieldTo(query, TBL_ERP_SALES, COL_TRADE_DEBT,
            COL_TRADE_CURRENCY, COL_TRADE_DATE, curr), COL_TRADE_DEBT);
        // .addExpr(ExchangeUtils.exchangeFieldTo(query, TBL_ERP_SALES, COL_TRADE_PAID,
        // COL_TRADE_CURRENCY, COL_TRADE_PAYMENT_TIME, curr), COL_TRADE_PAID);
      } else {
        query.addExpr(ExchangeUtils.exchangeField(query, TBL_ERP_SALES, COL_TRADE_DEBT,
            COL_TRADE_CURRENCY, COL_TRADE_DATE), COL_TRADE_DEBT);
        // .addExpr(ExchangeUtils.exchangeField(query, TBL_ERP_SALES, COL_TRADE_PAID,
        // COL_TRADE_CURRENCY, COL_TRADE_PAYMENT_TIME), COL_TRADE_PAID);
      }
      double debt = 0.0;
      double overdue = 0.0;

      for (SimpleRow row : qs.getData(query)) {
        double xxx = BeeUtils.unbox(row.getDouble(COL_TRADE_DEBT));
        // - BeeUtils.unbox(row.getDouble(COL_TRADE_PAID));

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
    Collection<BeeParameter> params = new ArrayList<>();
    params.add(BeeParameter.createNumber(module, PRM_ERP_REFRESH_INTERVAL));
    params.addAll(act.getDefaultParameters(module));
    return params;
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
  public TimerService getTimerService() {
    return timerService;
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
    cb.createIntervalTimer(this.getClass(), PRM_ERP_REFRESH_INTERVAL);

    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      @AllowConcurrentEvents
      public void fillInvoiceNumber(ViewModifyEvent event) {
        if (event.isBefore()
            && Objects.equals(sys.getViewSource(event.getTargetName()), TBL_SALES)) {
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

      @Subscribe
      @AllowConcurrentEvents
      public void modifyTradeStock(ViewModifyEvent event) {
        if (event.isTarget(VIEW_TRADE_DOCUMENT_ITEMS)) {
          List<BeeColumn> columns;
          BeeRow row;

          if (event instanceof ViewInsertEvent) {
            columns = ((ViewInsertEvent) event).getColumns();
            row = ((ViewInsertEvent) event).getRow();

            // if (event.isBefore()) {
            // }

          } else if (event.isBefore() && event instanceof ViewUpdateEvent) {
            columns = ((ViewUpdateEvent) event).getColumns();
            row = ((ViewUpdateEvent) event).getRow();

            if (row != null) {
              int index = DataUtils.getColumnIndex(COL_TRADE_ITEM_QUANTITY, columns);
              if (!BeeConst.isUndef(index)) {
                event.addErrors(onTradeItemQuantityUpdate(row.getId(), row.getDouble(index)));
              }

              if (!event.hasErrors()) {
                index = DataUtils.getColumnIndex(COL_TRADE_ITEM_WAREHOUSE, columns);
                if (!BeeConst.isUndef(index)) {
                  event.addErrors(onTradeItemWarehouseUpdate(row.getId(), row.getLong(index)));
                }
              }
            }

            // } else if (event.isBefore() && event instanceof ViewDeleteEvent) {

          }

          // } else if (event.isTarget(VIEW_TRADE_DOCUMENTS)) {

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

    MenuService.TRADE_DOCUMENTS.setTransformer(input -> {
      List<Menu> result = new ArrayList<>();

      if (input instanceof MenuItem) {
        BeeRowSet data = qs.getViewData(VIEW_TRADE_DOCUMENT_TYPES);

        if (!DataUtils.isEmpty(data)) {
          String language = usr.getLanguage();

          for (BeeRow row : data) {
            long id = row.getId();

            MenuItem item = (MenuItem) input.copy();

            item.setName(BeeUtils.join(BeeConst.STRING_UNDER, input.getName(), id));
            item.setLabel(DataUtils.getTranslation(data, row, COL_DOCUMENT_TYPE_NAME, language));

            item.setParameters(BeeUtils.toString(id));

            result.add(item);
          }
        }
      }

      return result;
    });

    act.init();
  }

  private ResponseObject getDocumentTypeCaptionAndFilter(RequestInfo reqInfo) {
    Long typeId = reqInfo.getParameterLong(COL_DOCUMENT_TYPE);
    if (!DataUtils.isId(typeId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_DOCUMENT_TYPE);
    }

    BeeRowSet typeData = qs.getViewData(VIEW_TRADE_DOCUMENT_TYPES, Filter.compareId(typeId));
    if (DataUtils.isEmpty(typeData)) {
      return ResponseObject.error(reqInfo.getService(), typeId, "not found");
    }

    BeeRow typeRow = typeData.getRow(0);

    String caption = DataUtils.getTranslation(typeData, typeRow, COL_DOCUMENT_TYPE_NAME,
        usr.getLanguage());
    CompoundFilter filter = Filter.and();

    CompoundFilter statusFilter = Filter.or();

    for (TradeDocumentPhase phase : TradeDocumentPhase.values()) {
      Boolean v = DataUtils.getBoolean(typeData, typeRow, phase.getDocumentTypeColumnName());
      if (BeeUtils.isTrue(v)) {
        statusFilter.add(Filter.equals(COL_TRADE_DOCUMENT_PHASE, phase));
      }
    }

    if (!statusFilter.isEmpty()) {
      filter.add(statusFilter);
    }

    Set<Long> operations = qs.getLongSet(new SqlSelect()
        .addFields(TBL_TRADE_TYPE_OPERATIONS, COL_TRADE_OPERATION)
        .addFrom(TBL_TRADE_TYPE_OPERATIONS)
        .setWhere(SqlUtils.equals(TBL_TRADE_TYPE_OPERATIONS, COL_DOCUMENT_TYPE, typeId)));

    if (!BeeUtils.isEmpty(operations)) {
      filter.add(Filter.any(COL_TRADE_OPERATION, operations));
    }

    Set<Long> statuses = qs.getLongSet(new SqlSelect()
        .addFields(TBL_TRADE_TYPE_STATUSES, COL_TRADE_DOCUMENT_STATUS)
        .addFrom(TBL_TRADE_TYPE_STATUSES)
        .setWhere(SqlUtils.equals(TBL_TRADE_TYPE_STATUSES, COL_DOCUMENT_TYPE, typeId)));

    if (!BeeUtils.isEmpty(statuses)) {
      filter.add(Filter.any(COL_TRADE_DOCUMENT_STATUS, statuses));
    }

    Set<Long> tags = qs.getLongSet(new SqlSelect()
        .addFields(TBL_TRADE_TYPE_TAGS, COL_TRADE_TAG)
        .addFrom(TBL_TRADE_TYPE_TAGS)
        .setWhere(SqlUtils.equals(TBL_TRADE_TYPE_TAGS, COL_DOCUMENT_TYPE, typeId)));

    if (!BeeUtils.isEmpty(tags)) {
      filter.add(Filter.in(sys.getIdName(VIEW_TRADE_DOCUMENTS), VIEW_TRADE_DOCUMENT_TAGS,
          COL_TRADE_DOCUMENT, Filter.any(COL_TRADE_TAG, tags)));
    }

    if (filter.isEmpty()) {
      filter = null;
    }

    return ResponseObject.response(Pair.of(caption, filter));
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
      query.addField(tradeItems, sys.getIdName(tradeItems), tradeItems)
          .addFields(tradeItems, COL_TRADE_DISCOUNT, COL_ITEM)
          .addFields(TBL_TRADE_ACTS, COL_TRADE_NUMBER, COL_TRADE_CONTACT);

      if (BeeUtils.same(tradeItems, TBL_TRADE_ACT_ITEMS)) {
        query.addFields(TBL_TRADE_ACT_ITEMS, sys.getIdName(tradeItems))
            .addFields(TBL_ITEMS, COL_TRADE_WEIGHT, COL_ITEM_AREA);
      }
      if (BeeUtils.same(tradeItems, TBL_TRADE_ACT_SERVICES)) {
        query.addFields(TBL_TRADE_ACT_SERVICES, COL_TA_SERVICE_FROM, COL_TA_SERVICE_TO,
            COL_TA_SERVICE_TARIFF, COL_TA_SERVICE_MIN, COL_TA_SERVICE_DAYS)
            .addFields(TBL_ITEMS, COL_TRADE_TIME_UNIT);
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
                SqlUtils.inList(TBL_ERP_SALES, COL_TRADE_CUSTOMER, companyIds))),
        SqlUtils.or(SqlUtils.less(TBL_ERP_SALES, COL_TRADE_TERM, (new JustDate()).getTime()),
            SqlUtils
                .isNull(TBL_ERP_SALES, COL_TRADE_TERM)),
        SqlUtils.less(SqlUtils.minus(
            SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_PAID), 0),
            SqlUtils.field(TBL_ERP_SALES, COL_TRADE_AMOUNT)), 0)));

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
                SqlUtils.inList(TBL_ERP_SALES, COL_TRADE_CUSTOMER, companyIds))),
        SqlUtils.equals(TBL_ERP_SALES, COL_TRADE_PAID, SqlUtils.field(TBL_ERP_SALES,
            COL_TRADE_AMOUNT)),
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
                SqlUtils.inList(TBL_ERP_SALES, COL_TRADE_CUSTOMER, companyIds))),
        SqlUtils.less(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_TERM), SqlUtils
            .field(TBL_ERP_SALES, COL_TRADE_PAYMENT_TIME)),
        SqlUtils.equals(TBL_ERP_SALES, COL_TRADE_AMOUNT, SqlUtils.field(TBL_ERP_SALES,
            COL_TRADE_PAID)),
        SqlUtils.notNull(TBL_ERP_SALES, COL_TRADE_PAYMENT_TIME),
        SqlUtils.notNull(TBL_ERP_SALES, COL_TRADE_TERM)));
    select.addGroup(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_CUSTOMER));

    return qs.getData(select);
  }

  private ResponseObject getSaleAmounts(String viewName, String relColumn, Filter filter) {
    Assert.notEmpty(viewName);
    Assert.notEmpty(relColumn);

    BeeView view = sys.getView(viewName);

    SqlSelect select = view.getQuery(usr.getCurrentUserId(), filter)
        .resetFields().resetOrder();

    if (BeeUtils.same(view.getSourceName(), TBL_COMPANIES)) {
      select.addFields(view.getSourceAlias(), sys.getIdName(view.getSourceName()));
    } else {
      select.addFields(view.getSourceAlias(), relColumn);
    }

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_ERP_SALES);

    if (BeeUtils.same(view.getSourceName(), TBL_COMPANIES)) {
      query.setWhere(SqlUtils.in(TBL_ERP_SALES, COL_TRADE_CUSTOMER, select));
    } else {
      query.setWhere(SqlUtils.in(TBL_ERP_SALES, sys.getIdName(TBL_ERP_SALES), select));
    }

    IsExpression amountXpr = ExchangeUtils.exchangeField(query,
        SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_AMOUNT), 0),
        SqlUtils.field(TBL_ERP_SALES, COL_CURRENCY), SqlUtils.field(TBL_ERP_SALES, COL_TRADE_DATE));

    IsExpression paidXpr = ExchangeUtils.exchangeField(query,
        SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_PAID), 0),
        SqlUtils.field(TBL_ERP_SALES, COL_CURRENCY), SqlUtils.field(TBL_ERP_SALES, COL_TRADE_DATE));

    IsExpression debtXpr = ExchangeUtils.exchangeField(query,
        SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_DEBT), 0),
        SqlUtils.field(TBL_ERP_SALES, COL_CURRENCY), SqlUtils.field(TBL_ERP_SALES, COL_TRADE_DATE));

    query.addSum(amountXpr, VAR_AMOUNT);
    query.addSum(paidXpr, VAR_TOTAL);
    query.addSum(debtXpr, VAR_DEBT);
    query.addCount("SalesCount");

    SimpleRowSet rs = qs.getData(query);

    Dictionary loc = usr.getDictionary();
    return ResponseObject.info(BeeUtils.joinWords(loc.trdAmount(),
        BeeUtils.round(rs.getValue(0, VAR_AMOUNT), 2),
        loc.trdPaid(),
        BeeUtils.round(rs.getValue(0, VAR_TOTAL), 2),
        loc.trdDebt(),
        BeeUtils.round(rs.getValue(0, VAR_DEBT), 2),
        loc.trdInvoices(),
        BeeUtils.unbox(rs.getInt(0, "SalesCount"))));

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

  private void importERPPayments() {
    long historyId = sys.eventStart(PRM_ERP_REFRESH_INTERVAL);
    int c = 0;

    SimpleRowSet debts = qs.getData(new SqlSelect()
        .addField(TBL_SALES, sys.getIdName(TBL_SALES), COL_SALE)
        .addFields(TBL_SALES, COL_TRADE_PAID)
        .addFrom(TBL_SALES)
        .setWhere(SqlUtils.and(SqlUtils.isNull(TBL_SALES, COL_SALE_PROFORMA),
            SqlUtils.or(SqlUtils.isNull(TBL_SALES, COL_TRADE_PAID),
                SqlUtils.less(TBL_SALES, COL_TRADE_PAID,
                    SqlUtils.field(TBL_SALES, COL_TRADE_AMOUNT))))));

    if (!debts.isEmpty()) {
      StringBuilder ids = new StringBuilder();

      for (SimpleRow row : debts) {
        if (ids.length() > 0) {
          ids.append(",");
        }
        ids.append("'").append(TradeModuleBean.encodeId(TBL_SALES, row.getLong(COL_SALE)))
            .append("'");
      }
      String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
      String remoteLogin = prm.getText(PRM_ERP_LOGIN);
      String remotePassword = prm.getText(PRM_ERP_PASSWORD);

      try {
        SimpleRowSet payments = ButentWS.connect(remoteAddress, remoteLogin, remotePassword)
            .getSQLData("SELECT extern_id AS id,"
                + " CASE WHEN oper_apm IS NULL THEN data ELSE apm_data END AS data,"
                + " CASE WHEN oper_apm IS NULL THEN viso ELSE apm_suma END AS suma"
                + " FROM apyvarta"
                + " INNER JOIN operac ON apyvarta.operacija = operac.operacija"
                + " WHERE pajamos=0 AND extern_id IN(" + ids.toString() + ")",
                "id", "data", "suma");

        for (SimpleRow payment : payments) {
          String id = TradeModuleBean.decodeId(TBL_SALES, payment.getLong("id"));
          Double paid = payment.getDouble("suma");

          if (!Objects.equals(paid,
              BeeUtils.toDoubleOrNull(debts.getValueByKey(COL_SALE, id, COL_TRADE_PAID)))) {

            c += qs.updateData(new SqlUpdate(TBL_SALES)
                .addConstant(COL_TRADE_PAID, paid)
                .addConstant(COL_TRADE_PAYMENT_TIME,
                    TimeUtils.parseDateTime(payment.getValue("data")))
                .setWhere(sys.idEquals(TBL_SALES, BeeUtils.toLong(id))));
          }
        }
      } catch (BeeException e) {
        logger.error(e);
        sys.eventError(historyId, e);
        return;
      }
    }
    sys.eventEnd(historyId, "OK", BeeUtils.joinWords("Updated", c, "records"));
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
      String label = Localized.maybeTranslate(rs.getColumnLabel(i), usr.getGlossary());

      if (BeeUtils.same(rs.getColumnId(i), COL_TRADE_INVOICE_NO)) {
        label = usr.getDictionary().trdInvoice();
      }

      if (BeeUtils.same(rs.getColumnId(i), COL_TRADE_AMOUNT)) {
        label = usr.getDictionary().trdAmount();
      }
      Th th = th().text(label);
      th.setBorderWidth("1px");
      th.setBorderStyle(BorderStyle.SOLID);
      th.setBorderColor("black");
      trHead.append(th);
    }

    Th th = th().text(usr.getDictionary().trdOverdueInDays());
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

    footer.append(td().append(b().text(usr.getDictionary().total())));
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

  private ResponseObject sendDebtsRemindEmail(RequestInfo req) {
    Long senderMailAccountId = mail.getSenderAccountId(SVC_REMIND_DEBTS_EMAIL);
    ResponseObject resp = ResponseObject.emptyResponse();

    if (!DataUtils.isId(senderMailAccountId)) {
      return ResponseObject.error(usr.getDictionary().mailAccountNotFound());
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
        errorMails.put(companyId, usr.getDictionary().mailRecipientAddressNotFound());
        continue;
      }

      Document mailDocument = renderCompanyDebtMail(subject, p1, p2, companyId);

      if (mailDocument == null) {
        errorMails.put(companyId, usr.getDictionary().noData());
        continue;
      }

      try {
        // logger.info(mailDocument.buildLines());
        MailAccount account = mailStore.getAccount(senderMailAccountId);
        MimeMessage message = mail.sendMail(account,
            ArrayUtils.toArray(
                Lists.newArrayList(emails.get(companyId).values())), null, null, subject,
            mailDocument
                .buildLines(),
            null, null);

        mail.storeMessage(account, message, account.getSentFolder());
        sentEmailCompanyIds.add(companyId);
      } catch (MessagingException | BeeRuntimeException ex) {
        logger.error(ex);
        errorMails.put(companyId, ex.getMessage());
      }
    }

    String message = BeeUtils.joinWords(usr.getDictionary().mailMessageSentCount(),
        sentEmailCompanyIds.size(), br().build());

    if (!BeeUtils.isEmpty(errorMails.keySet())) {
      message = BeeUtils.joinWords(message, usr.getDictionary().errors(), br().build());

      Filter filter = Filter.idIn(errorMails.keySet());
      BeeRowSet rs =
          qs.getViewData(VIEW_COMPANIES, filter, null, Lists.newArrayList(COL_COMPANY_NAME));
      int i = 0;
      for (Long id : errorMails.keySet()) {

        message = BeeUtils.joinWords(message,
            rs.getRowById(id).getString(rs.getColumnIndex(COL_COMPANY_NAME)), errorMails.get(id),
            br().build());

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

    SqlSelect query = new SqlSelect()
        .addFields(trade, COL_TRADE_DATE, COL_TRADE_INVOICE_NO,
            COL_TRADE_NUMBER, COL_TRADE_TERM, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER)
        .addFields(TBL_TRADE_OPERATIONS, COL_OPERATION_NAME)
        .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
        .addField(COL_TRADE_WAREHOUSE_FROM, COL_WAREHOUSE_CODE, COL_TRADE_WAREHOUSE_FROM)
        .addFields(PayrollConstants.TBL_EMPLOYEES, PayrollConstants.COL_TAB_NUMBER)
        .addFrom(trade)
        .addFromLeft(TBL_TRADE_OPERATIONS,
            sys.joinTables(TBL_TRADE_OPERATIONS, trade, COL_TRADE_OPERATION))
        .addFromLeft(TBL_CURRENCIES, sys.joinTables(TBL_CURRENCIES, trade, COL_CURRENCY))
        .addFromLeft(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM,
            sys.joinTables(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM, trade,
                COL_TRADE_WAREHOUSE_FROM))
        .addFromLeft(TBL_USERS, sys.joinTables(TBL_USERS, trade, COL_TRADE_MANAGER))
        .addFromLeft(PayrollConstants.TBL_EMPLOYEES,
            SqlUtils.joinUsing(TBL_USERS, PayrollConstants.TBL_EMPLOYEES, COL_COMPANY_PERSON))
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
            .addField(COL_TRADE_WAREHOUSE_TO, COL_WAREHOUSE_CODE, COL_TRADE_WAREHOUSE_TO)
            .addFromLeft(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_TO,
                sys.joinTables(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_TO, trade,
                    COL_TRADE_WAREHOUSE_TO));
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

      if (invoices.hasColumn(COL_TRADE_WAREHOUSE_TO)) {
        warehouse = invoice.getValue(COL_TRADE_WAREHOUSE_TO);
        client = companies.get(invoice.getLong(COL_TRADE_SUPPLIER));
      } else {
        warehouse = invoice.getValue(COL_TRADE_WAREHOUSE_FROM);
        client = companies.get(invoice.getLong(COL_TRADE_CUSTOMER));
      }
      WSDocument doc = new WSDocument(encodeId(trade, invoice.getLong(itemsRelation)),
          invoice.getDateTime(COL_TRADE_DATE), invoice.getValue(COL_OPERATION_NAME), client,
          warehouse);

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
      doc.setManager(invoice.getValue(PayrollConstants.COL_TAB_NUMBER));

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

  private ResponseObject tryPhaseTransition(RequestInfo reqInfo) {
    BeeRowSet newRowSet = BeeRowSet.restore(reqInfo.getContent());
    if (DataUtils.isEmpty(newRowSet)) {
      return ResponseObject.error(reqInfo.getService(), "content not available");
    }

    BeeRow newRow = newRowSet.getRow(0);
    long docId = newRow.getId();

    BeeRowSet oldRowSet = qs.getViewData(VIEW_TRADE_DOCUMENTS, Filter.compareId(docId));
    if (DataUtils.isEmpty(oldRowSet)) {
      return ResponseObject.error(reqInfo.getService(), "row", docId, "not found");
    }

    BeeRow oldRow = oldRowSet.getRow(0);

    TradeDocumentPhase oldPhase = EnumUtils.getEnumByIndex(TradeDocumentPhase.class,
        oldRow.getInteger(oldRowSet.getColumnIndex(COL_TRADE_DOCUMENT_PHASE)));
    TradeDocumentPhase newPhase = EnumUtils.getEnumByIndex(TradeDocumentPhase.class,
        newRow.getInteger(newRowSet.getColumnIndex(COL_TRADE_DOCUMENT_PHASE)));

    if (newPhase == null) {
      return ResponseObject.error(reqInfo.getService(), docId, "new phase not specified");
    }
    if (newPhase == oldPhase) {
      return ResponseObject.warning(reqInfo.getService(), docId, "phase not changed");
    }

    boolean oldStock = oldPhase != null && oldPhase.modifyStock();
    boolean newStock = newPhase.modifyStock();

    if (oldStock != newStock) {
      OperationType operationType = EnumUtils.getEnumByIndex(OperationType.class,
          newRow.getInteger(newRowSet.getColumnIndex(COL_OPERATION_TYPE)));

      Long warehouseFrom = newRow.getLong(newRowSet.getColumnIndex(COL_TRADE_WAREHOUSE_FROM));
      Long warehouseTo = newRow.getLong(newRowSet.getColumnIndex(COL_TRADE_WAREHOUSE_TO));

      String errorMessage = verifyPhaseTransition(docId, operationType, warehouseFrom, warehouseTo,
          newStock);
      if (!BeeUtils.isEmpty(errorMessage)) {
        return ResponseObject.error(reqInfo.getService(), docId, errorMessage);
      }

      ResponseObject response = doPhaseTransition(docId, operationType, warehouseFrom, warehouseTo,
          newStock);
      if (response != null && response.hasErrors()) {
        return response;
      }
    }

    return commitRow(oldRowSet.getViewName(), oldRowSet.getColumns(), oldRow, newRow);
  }

  private ResponseObject doPhaseTransition(long docId, OperationType operationType,
      Long warehouseFrom, Long warehouseTo, boolean toStock) {

    IsCondition itemCondition = SqlUtils.equals(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT,
        docId);

    ResponseObject response;

    switch (operationType) {
      case PURCHASE:
        if (toStock) {
          response = insertStock(itemCondition, warehouseTo);
        } else {
          response = deleteStock(itemCondition);
        }
        break;

      case SALE:
        if (toStock) {
          response = adoptItems(itemCondition, warehouseFrom);
        } else {
          response = leaveParents(itemCondition);
        }
        break;

      case TRANSFER:
        if (toStock) {
          response = adoptItems(itemCondition, warehouseFrom);
          if (!response.hasErrors()) {
            response = insertStock(itemCondition, warehouseTo);
          }

        } else {
          response = leaveParents(itemCondition);
          if (!response.hasErrors()) {
            response = deleteStock(itemCondition);
          }
        }
        break;
    }

    return ResponseObject.emptyResponse();
  }

  private ResponseObject adoptItems(IsCondition itemCondition, Long warehouseFrom) {
    int count = 0;

    String idName = sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS);

    SqlSelect itemQuery = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENT_ITEMS, idName, COL_ITEM, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .setWhere(itemCondition);

    SimpleRowSet itemData = qs.getData(itemQuery);
    if (!DataUtils.isEmpty(itemData)) {
      ResponseObject response;

      for (SimpleRow itemRow : itemData) {
        Long id = itemRow.getLong(idName);
        Long item = itemRow.getLong(COL_ITEM);

        Double qty = itemRow.getDouble(COL_TRADE_ITEM_QUANTITY);

        Long parent = findParent(item, warehouseFrom, qty);
        if (!DataUtils.isId(parent)) {
          return ResponseObject.error("parent not found for id", id, "item", item, "qty", qty,
              "warehouse", warehouseFrom);
        }

        SqlUpdate stockUpdate = new SqlUpdate(TBL_TRADE_STOCK)
            .addExpression(COL_STOCK_QUANTITY,
                SqlUtils.minus(SqlUtils.field(TBL_TRADE_STOCK, COL_STOCK_QUANTITY), qty))
            .setWhere(SqlUtils.equals(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM, parent));

        response = qs.updateDataWithResponse(stockUpdate);
        if (response.hasErrors()) {
          return response;
        }

        SqlUpdate itemUpdate = new SqlUpdate(TBL_TRADE_DOCUMENT_ITEMS)
            .addConstant(COL_TRADE_ITEM_PARENT, parent)
            .setWhere(SqlUtils.equals(TBL_TRADE_DOCUMENT_ITEMS, idName, id));

        response = qs.updateDataWithResponse(itemUpdate);
        if (response.hasErrors()) {
          return response;
        }

        count++;
      }
    }

    return ResponseObject.response(count);
  }

  private Long findParent(Long item, Long warehouse, Double qty) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM)
        .addFrom(TBL_TRADE_STOCK)
        .addFromInner(TBL_TRADE_DOCUMENT_ITEMS, sys.joinTables(TBL_TRADE_DOCUMENT_ITEMS,
            TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM));

    HasConditions where = SqlUtils.and(
        SqlUtils.equals(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM, item),
        SqlUtils.moreEqual(TBL_TRADE_STOCK, COL_STOCK_QUANTITY, qty));

    if (DataUtils.isId(warehouse)) {
      where.add(SqlUtils.equals(TBL_TRADE_STOCK, COL_STOCK_WAREHOUSE, warehouse));
    }

    query.setWhere(where);

    return BeeUtils.getQuietly(qs.getLongList(query), 0);
  }

  private ResponseObject leaveParents(IsCondition itemCondition) {
    int count = 0;

    String idName = sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS);
    String itemQty = SqlUtils.uniqueName();
    String stockQty = SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENT_ITEMS, idName, COL_TRADE_ITEM_PARENT)
        .addField(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY, itemQty)
        .addField(TBL_TRADE_STOCK, COL_STOCK_QUANTITY, stockQty)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .addFromInner(TBL_TRADE_STOCK,
            SqlUtils.join(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_PARENT,
                TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM))
        .setWhere(itemCondition);

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      ResponseObject response;

      for (SimpleRow row : data) {
        Long id = row.getLong(idName);
        Long parent = row.getLong(COL_TRADE_ITEM_PARENT);

        double qty = BeeUtils.unbox(row.getDouble(stockQty))
            + BeeUtils.unbox(row.getDouble(itemQty));

        SqlUpdate stockUpdate = new SqlUpdate(TBL_TRADE_STOCK)
            .addConstant(COL_STOCK_QUANTITY, qty)
            .setWhere(SqlUtils.equals(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM, parent));

        response = qs.updateDataWithResponse(stockUpdate);
        if (response.hasErrors()) {
          return response;
        }

        SqlUpdate itemUpdate = new SqlUpdate(TBL_TRADE_DOCUMENT_ITEMS)
            .addConstant(COL_TRADE_ITEM_PARENT, null)
            .setWhere(SqlUtils.equals(TBL_TRADE_DOCUMENT_ITEMS, idName, id));

        response = qs.updateDataWithResponse(itemUpdate);
        if (response.hasErrors()) {
          return response;
        }

        count++;
      }
    }

    return ResponseObject.response(count);
  }

  private ResponseObject deleteStock(IsCondition itemCondition) {
    SqlDelete delete = new SqlDelete(TBL_TRADE_STOCK)
        .setWhere(SqlUtils.in(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM,
            TBL_TRADE_DOCUMENT_ITEMS, sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS), itemCondition));

    return qs.updateDataWithResponse(delete);
  }

  private ResponseObject insertStock(IsCondition itemCondition, Long warehouseTo) {
    int count = 0;

    String idName = sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS);

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENT_ITEMS, idName, COL_TRADE_ITEM_PARENT,
            COL_TRADE_ITEM_WAREHOUSE, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .setWhere(itemCondition);

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        Long id = row.getLong(idName);

        Long parent = row.getLong(COL_TRADE_ITEM_PARENT);
        Long primary = DataUtils.isId(parent) ? getPrimary(parent) : id;

        Long warehouse = BeeUtils.nvl(row.getLong(COL_TRADE_ITEM_WAREHOUSE), warehouseTo);
        Double qty = row.getDouble(COL_TRADE_ITEM_QUANTITY);

        SqlInsert insert = new SqlInsert(TBL_TRADE_STOCK)
            .addConstant(COL_PRIMARY_DOCUMENT_ITEM, primary)
            .addConstant(COL_TRADE_DOCUMENT_ITEM, id)
            .addConstant(COL_STOCK_WAREHOUSE, warehouse)
            .addConstant(COL_STOCK_QUANTITY, qty);

        ResponseObject response = qs.insertDataWithResponse(insert);
        if (response.hasErrors()) {
          return response;
        }

        count++;
      }
    }

    return ResponseObject.response(count);
  }

  private Long getPrimary(long parent) {
    return qs.getLong(TBL_TRADE_STOCK, COL_PRIMARY_DOCUMENT_ITEM, COL_TRADE_DOCUMENT_ITEM, parent);
  }

  private String verifyPhaseTransition(long docId, OperationType operationType,
      Long warehouseFrom, Long warehouseTo, boolean toStock) {

    IsCondition itemCondition = SqlUtils.equals(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT,
        docId);
    if (!qs.sqlExists(TBL_TRADE_DOCUMENT_ITEMS, itemCondition)) {
      return null;
    }

    if (operationType == null) {
      return "operation type not specified";
    }
    if (toStock && EnumUtils.in(operationType, OperationType.PURCHASE, OperationType.TRANSFER)
        && !DataUtils.isId(warehouseTo)) {
      return "warehouse-receiver not specified";
    }

    String errorMessage = null;

    switch (operationType) {
      case PURCHASE:
        if (toStock && hasChildren(itemCondition)) {
          errorMessage = "illegal";
        }
        break;

      case SALE:
        if (toStock && !verifyStock(itemCondition, warehouseFrom)) {
          errorMessage = "never enough";
        }
        break;

      case TRANSFER:
        if (toStock && !verifyStock(itemCondition, warehouseFrom)) {
          errorMessage = "it's never enough";
        }
        if (!toStock && hasChildren(itemCondition)) {
          errorMessage = "taboo";
        }
        break;
    }

    return errorMessage;
  }

  private boolean verifyStock(IsCondition itemCondition, Long warehouseFrom) {
    SqlSelect inputQuery = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM)
        .addSum(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .setWhere(itemCondition)
        .addGroup(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM);

    Map<Long, Double> inputQuantities = getQuantities(inputQuery);
    if (BeeUtils.isEmpty(inputQuantities)) {
      return true;
    }

    SqlSelect stockQuery = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM)
        .addSum(TBL_TRADE_STOCK, COL_STOCK_QUANTITY)
        .addFrom(TBL_TRADE_STOCK)
        .addFromInner(TBL_TRADE_DOCUMENT_ITEMS, sys.joinTables(TBL_TRADE_DOCUMENT_ITEMS,
            TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM))
        .addGroup(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM);

    if (DataUtils.isId(warehouseFrom)) {
      stockQuery.setWhere(SqlUtils.equals(TBL_TRADE_STOCK, COL_STOCK_WAREHOUSE, warehouseFrom));
    }

    Map<Long, Double> stockQuantities = getQuantities(stockQuery);
    if (stockQuantities.isEmpty()) {
      return false;
    }

    for (Map.Entry<Long, Double> entry : inputQuantities.entrySet()) {
      Double qty = stockQuantities.get(entry.getKey());
      if (qty == null || BeeUtils.isMore(entry.getValue(), qty)) {
        return false;
      }
    }

    return true;
  }

  private Map<Long, Double> getQuantities(SqlSelect query) {
    Map<Long, Double> quantities = new HashMap<>();

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        quantities.put(row.getLong(0), row.getDouble(1));
      }
    }

    return quantities;
  }

  private boolean hasChildren(IsCondition itemCondition) {
    return qs.sqlExists(TBL_TRADE_DOCUMENT_ITEMS,
        SqlUtils.in(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_PARENT,
            TBL_TRADE_DOCUMENT_ITEMS, sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS), itemCondition));
  }

  private ResponseObject commitRow(String viewName, List<BeeColumn> columns, BeeRow oldRow,
      BeeRow newRow) {

    BeeRowSet updated = DataUtils.getUpdated(viewName, columns, oldRow, newRow, null);

    if (DataUtils.isEmpty(updated)) {
      return ResponseObject.response(newRow);
    } else {
      return deb.commitRow(updated);
    }
  }

  private void fireStockUpdate(IsCondition where, String fieldName) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_STOCK,
            sys.getIdName(TBL_TRADE_STOCK), sys.getVersionName(TBL_TRADE_STOCK), fieldName)
        .addFrom(TBL_TRADE_STOCK)
        .setWhere(where);

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      CellSource source = sys.getCellSource(VIEW_TRADE_STOCK, fieldName);

      for (SimpleRow row : data) {
        CellUpdateEvent.fire(Endpoint.getModificationShooter(), VIEW_TRADE_STOCK,
            row.getLong(0), row.getLong(1), source, row.getValue(fieldName));
      }
    }
  }

  private boolean modifyItemStock(long itemId) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENTS, COL_TRADE_DOCUMENT_PHASE)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .addFromInner(TBL_TRADE_DOCUMENTS, sys.joinTables(TBL_TRADE_DOCUMENTS,
            TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
        .setWhere(sys.idEquals(TBL_TRADE_DOCUMENT_ITEMS, itemId));

    TradeDocumentPhase phase = EnumUtils.getEnumByIndex(TradeDocumentPhase.class, qs.getInt(query));
    return phase != null && phase.modifyStock();
  }

  private ResponseObject onTradeItemQuantityUpdate(long itemId, Double newQty) {
    if (!BeeUtils.isPositive(newQty)) {
      return ResponseObject.error("invalid quantity", newQty);
    }

    if (modifyItemStock(itemId)) {
      String idName = sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS);

      Double oldQty = qs.getDouble(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY,
          idName, itemId);

      if (BeeUtils.isDouble(oldQty) && !newQty.equals(oldQty)) {
        IsCondition itemWhere = SqlUtils.equals(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM, itemId);
        IsCondition parentWhere = null;

        boolean updateItemStock = qs.sqlExists(TBL_TRADE_STOCK, itemWhere);
        boolean updateParentStock = false;

        Double itemOldStock = null;
        Double itemNewStock = null;
        Double parentOldStock = null;
        Double parentNewStock = null;

        if (updateItemStock) {
          itemOldStock = qs.getDouble(TBL_TRADE_STOCK, COL_STOCK_QUANTITY, itemWhere);
          itemNewStock = BeeUtils.unbox(itemOldStock) - oldQty + newQty;

          if (BeeUtils.isNegative(itemNewStock)) {
            return ResponseObject.error("item stock", itemOldStock, "-", oldQty, "+", newQty,
                "=", itemNewStock);
          }
        }

        Long parent = qs.getLong(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_PARENT,
            idName, itemId);

        if (DataUtils.isId(parent)) {
          parentWhere = SqlUtils.equals(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM, parent);
          updateParentStock = qs.sqlExists(TBL_TRADE_STOCK, parentWhere);

          if (updateParentStock) {
            parentOldStock = qs.getDouble(TBL_TRADE_STOCK, COL_STOCK_QUANTITY, parentWhere);
            parentNewStock = BeeUtils.unbox(parentOldStock) + oldQty - newQty;

            if (BeeUtils.isNegative(parentNewStock)) {
              return ResponseObject.error("parent stock", parentOldStock, "+", oldQty, "-", newQty,
                  "=", parentNewStock);
            }
          }
        }

        if (updateItemStock) {
          SqlUpdate update = new SqlUpdate(TBL_TRADE_STOCK)
              .addConstant(COL_STOCK_QUANTITY, itemNewStock)
              .setWhere(itemWhere);

          ResponseObject response = qs.updateDataWithResponse(update);
          if (response.hasErrors()) {
            return response;
          }
        }

        if (updateParentStock) {
          SqlUpdate update = new SqlUpdate(TBL_TRADE_STOCK)
              .addConstant(COL_STOCK_QUANTITY, parentNewStock)
              .setWhere(parentWhere);

          ResponseObject response = qs.updateDataWithResponse(update);
          if (response.hasErrors()) {
            return response;
          }
        }

        if (updateItemStock) {
          if (BeeUtils.isPositive(itemOldStock) && BeeUtils.isPositive(itemNewStock)) {
            fireStockUpdate(itemWhere, COL_STOCK_QUANTITY);
          } else {
            Endpoint.refreshViews(VIEW_TRADE_STOCK);
          }
        }

        if (updateParentStock) {
          if (BeeUtils.isPositive(parentOldStock) && BeeUtils.isPositive(parentNewStock)) {
            fireStockUpdate(parentWhere, COL_STOCK_QUANTITY);
          } else {
            Endpoint.refreshViews(VIEW_TRADE_STOCK);
          }
        }
      }
    }

    return ResponseObject.emptyResponse();
  }

  private OperationType getOperationTypeByTradeItem(long itemId) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_OPERATIONS, COL_OPERATION_TYPE)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .addFromInner(TBL_TRADE_DOCUMENTS, sys.joinTables(TBL_TRADE_DOCUMENTS,
            TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
        .addFromInner(TBL_TRADE_OPERATIONS, sys.joinTables(TBL_TRADE_OPERATIONS,
            TBL_TRADE_DOCUMENTS, COL_TRADE_OPERATION))
        .setWhere(sys.idEquals(TBL_TRADE_DOCUMENT_ITEMS, itemId));

    return EnumUtils.getEnumByIndex(OperationType.class, qs.getInt(query));
  }

  private String getDocumentFieldByTradeItem(long itemId, String fieldName) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENTS, fieldName)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .addFromInner(TBL_TRADE_DOCUMENTS, sys.joinTables(TBL_TRADE_DOCUMENTS,
            TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
        .setWhere(sys.idEquals(TBL_TRADE_DOCUMENT_ITEMS, itemId));

    return qs.getValue(query);
  }

  private ResponseObject onTradeItemWarehouseUpdate(long itemId, Long newValue) {
    OperationType operationType = getOperationTypeByTradeItem(itemId);

    if (operationType != null && operationType.producesStock() && modifyItemStock(itemId)) {
      String idName = sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS);

      Long oldValue = qs.getLong(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_WAREHOUSE,
          idName, itemId);

      Long warehouse;
      if (DataUtils.isId(newValue)) {
        warehouse = newValue;
      } else {
        warehouse = BeeUtils.toLongOrNull(getDocumentFieldByTradeItem(itemId,
            COL_TRADE_WAREHOUSE_TO));
      }

      if (!Objects.equals(oldValue, warehouse)) {
        if (!DataUtils.isId(warehouse)) {
          return ResponseObject.error("warehouse required");
        }

        SqlUpdate update = new SqlUpdate(TBL_TRADE_STOCK)
            .addConstant(COL_STOCK_WAREHOUSE, warehouse)
            .setWhere(SqlUtils.equals(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM, itemId));

        ResponseObject response = qs.updateDataWithResponse(update);
        if (response.hasErrors()) {
          return response;
        }

        if (BeeUtils.isPositive(response.getResponseAsInt())) {
          Endpoint.refreshRows(qs.getViewData(VIEW_TRADE_STOCK,
              Filter.equals(COL_TRADE_DOCUMENT_ITEM, itemId)));
        }
      }
    }

    return ResponseObject.emptyResponse();
  }
}
