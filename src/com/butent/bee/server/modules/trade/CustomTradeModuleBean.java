package com.butent.bee.server.modules.trade;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.Dimensions.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Triplet;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SqlConstants;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDiscountMode;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeVatMode;
import com.butent.bee.shared.report.ReportInfo;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.webservice.ButentWS;
import com.butent.webservice.WSDocument;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CustomTradeModuleBean {

  @EJB SystemBean sys;
  @EJB QueryServiceBean qs;
  @EJB ParamHolderBean prm;
  @EJB TradeModuleBean trade;

  public Multimap<Long, Triplet<Long, String, Double>> getErpStock(Collection<Long> items) {
    Multimap<Long, Triplet<Long, String, Double>> result = ArrayListMultimap.create();

    Pair<Long, String> erp = prm.getRelationInfo(PRM_ERP_WAREHOUSE);

    if (erp.noNulls()) {
      SimpleRowSet data = qs.getData(new SqlSelect()
          .addField(TBL_ITEMS, sys.getIdName(TBL_ITEMS), COL_ITEM)
          .addFields(TBL_ITEMS, COL_EXTERNAL_STOCK)
          .addFrom(TBL_ITEMS)
          .setWhere(SqlUtils.and(sys.idInList(TBL_ITEMS, items),
              SqlUtils.notNull(TBL_ITEMS, COL_EXTERNAL_STOCK),
              SqlUtils.notEqual(TBL_ITEMS, COL_EXTERNAL_STOCK, 0))));

      if (!DataUtils.isEmpty(data)) {
        data.forEach(row -> result.put(row.getLong(COL_ITEM),
            Triplet.of(erp.getA(), erp.getB(), row.getDouble(COL_EXTERNAL_STOCK))));
      }
    }
    return result;
  }

  public Table<Long, Long, Double> getTradeActStock(Collection<Long> items, Long... warehouses) {
    String wrh = "TradeActWarehouse";

    IsCondition phaseCondition = SqlUtils.inList(TBL_TRADE_DOCUMENTS, COL_TRADE_DOCUMENT_PHASE,
        Arrays.stream(TradeDocumentPhase.values()).filter(TradeDocumentPhase::modifyStock)
            .collect(Collectors.toSet()));

    IsCondition producerCondition = SqlUtils.inList(TBL_TRADE_OPERATIONS, COL_OPERATION_TYPE,
        Arrays.stream(OperationType.values()).filter(OperationType::producesStock)
            .collect(Collectors.toSet()));

    IsCondition consumerCondition = SqlUtils.inList(TBL_TRADE_OPERATIONS, COL_OPERATION_TYPE,
        Arrays.stream(OperationType.values()).filter(OperationType::consumesStock)
            .collect(Collectors.toSet()));

    IsCondition itemsClause = SqlUtils.and(SqlUtils.isNull(TBL_ITEMS, COL_ITEM_IS_SERVICE),
        BeeUtils.isEmpty(items) ? null
            : SqlUtils.inList(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM, items));

    IsCondition wrhClause = SqlUtils.and(SqlUtils.notNull(TBL_TRADE_DOCUMENTS, wrh),
        ArrayUtils.isEmpty(warehouses) ? null
            : SqlUtils.inList(TBL_TRADE_DOCUMENTS, wrh, (Object[]) warehouses));

    Table<Long, Long, Double> result = HashBasedTable.create();

    SqlSelect union = new SqlSelect()
        .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM)
        .addFields(TBL_TRADE_DOCUMENTS, wrh)
        .addSum(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
        .addFromInner(TBL_TRADE_DOCUMENTS,
            sys.joinTables(TBL_TRADE_DOCUMENTS, TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
        .addFromInner(TBL_TRADE_OPERATIONS,
            sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_DOCUMENTS, COL_TRADE_OPERATION))
        .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM))
        .setWhere(SqlUtils.and(phaseCondition, producerCondition, wrhClause, itemsClause))
        .addGroup(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM)
        .addGroup(TBL_TRADE_DOCUMENTS, wrh)
        .addUnion(new SqlSelect()
            .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM)
            .addFields(TBL_TRADE_DOCUMENTS, wrh)
            .addSum(SqlUtils.multiply(SqlUtils.field(TBL_TRADE_DOCUMENT_ITEMS,
                COL_TRADE_ITEM_QUANTITY), SqlUtils.constant(-1)), COL_TRADE_ITEM_QUANTITY)
            .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
            .addFromInner(TBL_TRADE_DOCUMENTS,
                sys.joinTables(TBL_TRADE_DOCUMENTS, TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
            .addFromInner(TBL_TRADE_OPERATIONS,
                sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_DOCUMENTS, COL_TRADE_OPERATION))
            .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM))
            .setWhere(SqlUtils.and(phaseCondition, consumerCondition, wrhClause, itemsClause))
            .addGroup(TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM)
            .addGroup(TBL_TRADE_DOCUMENTS, wrh));

    String subq = SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect()
        .addFields(subq, COL_ITEM, wrh)
        .addSum(subq, COL_TRADE_ITEM_QUANTITY)
        .addFrom(union, subq)
        .addGroup(subq, COL_ITEM, wrh);

    qs.getData(query).forEach(row -> result.put(row.getLong(COL_ITEM), row.getLong(wrh),
        row.getDouble(COL_STOCK_QUANTITY)));

    return result;
  }

  public ResponseObject sendDocumentToErp(Set<Long> ids) {
    ResponseObject response = ResponseObject.emptyResponse();
    int cnt = 0;
    String remoteAddress = prm.getText(PRM_ERP_ADDRESS);
    String remoteLogin = prm.getText(PRM_ERP_LOGIN);
    String remotePassword = prm.getText(PRM_ERP_PASSWORD);

    String ACTION = "Veikla";
    String CENTER = "Centras";

    SqlSelect query = new SqlSelect()
        .addField(TBL_TRADE_DOCUMENTS, sys.getIdName(TBL_TRADE_DOCUMENTS), COL_TRADE_DOCUMENT)
        .addFields(TBL_TRADE_DOCUMENTS, COL_TRADE_DATE, COL_TRADE_SERIES, COL_TRADE_NUMBER,
            COL_TRADE_TERM, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_TRADE_PAYER,
            COL_TRADE_NOTES, COL_TRADE_DOCUMENT_VAT_MODE, COL_TRADE_DOCUMENT_DISCOUNT_MODE)
        .addFields(TBL_TRADE_OPERATIONS, COL_OPERATION_NAME, COL_OPERATION_TYPE)
        .addField(TBL_CURRENCIES, COL_CURRENCY_NAME, COL_CURRENCY)
        .addExpr(SqlUtils.nvl(SqlUtils.field(COL_TRADE_WAREHOUSE_FROM, "ERPCode"),
            SqlUtils.field(COL_TRADE_WAREHOUSE_FROM, COL_WAREHOUSE_CODE)), COL_TRADE_WAREHOUSE_FROM)
        .addExpr(SqlUtils.nvl(SqlUtils.field(COL_TRADE_WAREHOUSE_TO, "ERPCode"),
            SqlUtils.field(COL_TRADE_WAREHOUSE_TO, COL_WAREHOUSE_CODE)), COL_TRADE_WAREHOUSE_TO)
        .addFields(PayrollConstants.TBL_EMPLOYEES, PayrollConstants.COL_TAB_NUMBER)
        .addField(TBL_COMPANY_DEPARTMENTS, COL_OBJECT_NAME, COL_DEPARTMENT_NAME)
        .addField(getTableName(1), getNameColumn(1), ACTION)
        .addField(getTableName(2), getNameColumn(2), CENTER)
        .addFrom(TBL_TRADE_DOCUMENTS)
        .addFromInner(TBL_TRADE_OPERATIONS,
            sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_DOCUMENTS, COL_TRADE_OPERATION))
        .addFromInner(TBL_CURRENCIES,
            sys.joinTables(TBL_CURRENCIES, TBL_TRADE_DOCUMENTS, COL_CURRENCY))
        .addFromLeft(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM,
            sys.joinTables(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_FROM, TBL_TRADE_DOCUMENTS,
                COL_TRADE_WAREHOUSE_FROM))
        .addFromLeft(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_TO,
            sys.joinTables(TBL_WAREHOUSES, COL_TRADE_WAREHOUSE_TO, TBL_TRADE_DOCUMENTS,
                COL_TRADE_WAREHOUSE_TO))
        .addFromLeft(PayrollConstants.TBL_EMPLOYEES,
            sys.joinTables(PayrollConstants.TBL_EMPLOYEES, TBL_TRADE_DOCUMENTS, COL_TRADE_MANAGER))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, PayrollConstants.TBL_EMPLOYEES, COL_COMPANY_PERSON))
        .addFromLeft(TBL_COMPANY_DEPARTMENTS,
            sys.joinTables(TBL_COMPANY_DEPARTMENTS, TBL_COMPANY_PERSONS, COL_DEPARTMENT))
        .addFromLeft(TBL_EXTRA_DIMENSIONS,
            sys.joinTables(TBL_EXTRA_DIMENSIONS, TBL_TRADE_DOCUMENTS, COL_EXTRA_DIMENSIONS))
        .addFromLeft(getTableName(1),
            sys.joinTables(getTableName(1), TBL_EXTRA_DIMENSIONS, getRelationColumn(1)))
        .addFromLeft(getTableName(2),
            sys.joinTables(getTableName(2), TBL_EXTRA_DIMENSIONS, getRelationColumn(2)))
        .setWhere(SqlUtils.and(sys.idInList(TBL_TRADE_DOCUMENTS, ids),
            SqlUtils.inList(TBL_TRADE_DOCUMENTS, COL_TRADE_DOCUMENT_PHASE,
                TradeDocumentPhase.getStockPhases())));

    SimpleRowSet invoices = qs.getData(query);

    Map<Long, String> companies = new HashMap<>();

    for (SimpleRowSet.SimpleRow invoice : invoices) {
      for (String col : Arrays.asList(COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_TRADE_PAYER)) {
        Long companyId = invoice.getLong(col);

        if (DataUtils.isId(companyId) && !companies.containsKey(companyId)) {
          ResponseObject resp = trade.sendCompanyToERP(companyId);

          if (resp.hasErrors()) {
            response.addErrorsFrom(resp);
            break;
          }
          companies.put(companyId, resp.getResponseAsString());
        }
      }
      if (response.hasErrors()) {
        break;
      }
      String warehouse;
      String client;

      switch (invoice.getEnum(COL_OPERATION_TYPE, OperationType.class)) {
        case PURCHASE:
          warehouse = invoice.getValue(COL_TRADE_WAREHOUSE_TO);
          client = companies.get(invoice.getLong(COL_TRADE_SUPPLIER));
          break;

        case SALE:
          warehouse = invoice.getValue(COL_TRADE_WAREHOUSE_FROM);
          client = companies.get(invoice.getLong(COL_TRADE_CUSTOMER));
          break;

        default:
          continue;
      }
      Long documentId = invoice.getLong(COL_TRADE_DOCUMENT);

      WSDocument wsDoc = new WSDocument(encodeId(documentId), invoice.getDateTime(COL_TRADE_DATE),
          invoice.getValue(COL_OPERATION_NAME), client, warehouse);

      wsDoc.setInvoice(invoice.getValue(COL_TRADE_SERIES), invoice.getValue(COL_TRADE_NUMBER));
      wsDoc.setSupplier(companies.get(invoice.getLong(COL_TRADE_SUPPLIER)));
      wsDoc.setCustomer(companies.get(invoice.getLong(COL_TRADE_CUSTOMER)));
      wsDoc.setPayer(companies.get(invoice.getLong(COL_TRADE_PAYER)));
      wsDoc.setTerm(invoice.getDate(COL_TRADE_TERM));
      wsDoc.setCurrency(invoice.getValue(COL_CURRENCY));
      wsDoc.setManager(invoice.getValue(PayrollConstants.COL_TAB_NUMBER));
      wsDoc.setNotes(invoice.getValue(COL_TRADE_NOTES));
      wsDoc.setDepartment(invoice.getValue(COL_DEPARTMENT_NAME));
      wsDoc.setAction(invoice.getValue(ACTION));
      wsDoc.setCenter(invoice.getValue(CENTER));

      SqlSelect itemsQuery = new SqlSelect()
          .addFields(TBL_ITEMS, COL_ITEM_NAME, COL_ITEM_EXTERNAL_CODE)
          .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
              COL_TRADE_DOCUMENT_ITEM_VAT, COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT,
              COL_TRADE_DOCUMENT_ITEM_DISCOUNT, COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT,
              COL_TRADE_ITEM_ARTICLE, COL_TRADE_ITEM_NOTE)
          .addFrom(TBL_TRADE_DOCUMENT_ITEMS)
          .addFromInner(TBL_ITEMS, sys.joinTables(TBL_ITEMS, TBL_TRADE_DOCUMENT_ITEMS, COL_ITEM))
          .setWhere(SqlUtils.equals(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT, documentId));

      TradeVatMode vatMode = invoice.getEnum(COL_TRADE_DOCUMENT_VAT_MODE, TradeVatMode.class);
      TradeDiscountMode discountMode = invoice.getEnum(COL_TRADE_DOCUMENT_DISCOUNT_MODE,
          TradeDiscountMode.class);

      SimpleRowSet items = qs.getData(itemsQuery);

      for (SimpleRowSet.SimpleRow item : items) {
        if (BeeUtils.isEmpty(item.getValue(COL_ITEM_EXTERNAL_CODE))) {
          response.addError("Item", BeeUtils.bracket(item.getValue(COL_ITEM_NAME)),
              "does not have ERP code");
          break;
        }
        WSDocument.WSDocumentItem wsItem = wsDoc.addItem(item.getValue(COL_ITEM_EXTERNAL_CODE),
            item.getValue(COL_TRADE_ITEM_QUANTITY));

        wsItem.setPrice(item.getValue(COL_TRADE_ITEM_PRICE));

        if (Objects.nonNull(vatMode)) {
          wsItem.setVat(item.getValue(COL_TRADE_DOCUMENT_ITEM_VAT),
              item.getBoolean(COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT),
              Objects.equals(vatMode, TradeVatMode.PLUS));
        }
        if (Objects.nonNull(discountMode)) {
          wsItem.setDiscount(item.getValue(COL_TRADE_DOCUMENT_ITEM_DISCOUNT),
              item.getBoolean(COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT));
        }
        wsItem.setArticle(item.getValue(COL_TRADE_ITEM_ARTICLE));
        wsItem.setNote(item.getValue(COL_TRADE_ITEM_NOTE));
      }
      if (response.hasErrors()) {
        break;
      }
      try {
        ButentWS.connect(remoteAddress, remoteLogin, remotePassword).importDoc(wsDoc);
      } catch (BeeException e) {
        response.addError(e);
        break;
      }
      qs.updateData(new SqlUpdate(TBL_TRADE_DOCUMENTS)
          .addConstant(COL_TRADE_EXPORTED, System.currentTimeMillis())
          .addConstant(COL_TRADE_DOCUMENT_PHASE, TradeDocumentPhase.APPROVED)
          .setWhere(sys.idEquals(TBL_TRADE_DOCUMENTS, documentId)));
      cnt++;
    }
    if (response.hasErrors()) {
      response.log(TradeModuleBean.logger);
    }
    response.setResponse(cnt);
    return response;
  }

  private static String encodeId(Long documentId) {
    return BeeUtils.toString(documentId + 1e9);
  }

  public ResponseObject getDebtReport(RequestInfo reqInfo) {
    ReportInfo report = ReportInfo.restore(reqInfo.getParameter(Service.VAR_DATA));

    HasConditions clause = SqlUtils.and(SqlUtils.more(SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES,
        COL_TRADE_DEBT), 0), 0));

    clause.add(report.getCondition(TBL_ERP_SALES, COL_TRADE_DATE));
    clause.add(report.getCondition(TBL_ERP_SALES, COL_TRADE_TERM));
    clause.add(report.getCondition(SqlUtils.field(TBL_COMPANIES, COL_COMPANY_NAME),
        COL_TRADE_CUSTOMER));
    clause.add(report.getCondition(TBL_ERP_SALES, COL_TRADE_ERP_INVOICE));
    clause.add(report.getCondition(SqlUtils.field("CompanyBankruptRisks", "Name"),
        "BankruptcyRisk"));
    clause.add(report.getCondition(SqlUtils.field("DelayedPaymentRisks", "Name"),
        "DelayedPaymentRisk"));
    clause.add(report.getCondition(SqlUtils.field(VIEW_FINANCIAL_STATES, "Name"),
        COL_COMPANY_FINANCIAL_STATE));

    String tmp = qs.sqlCreateTemp(new SqlSelect()
        .addField(TBL_ERP_SALES, COL_TRADE_CUSTOMER, COL_COMPANY)
        .addFields(TBL_ERP_SALES, COL_TRADE_DATE, COL_TRADE_TERM, COL_TRADE_ERP_INVOICE,
            COL_TRADE_DEBT)
        .addEmptyDouble(VAR_OVERDUE)
        .addEmptyDouble(VAR_UNTOLERATED)
        .addEmptyInt(VAR_OVERDUE_DAYS)
        .addEmptyString(COL_COMPANY_USER_RESPONSIBILITY, 61)
        .addEmptyString(COL_TRADE_DEBT + COL_TRADE_MANAGER, 61)
        .addExpr(SqlUtils.concat(SqlUtils.nvl(SqlUtils.field(TBL_SALES_SERIES, COL_SERIES_NAME),
            "''"), SqlUtils.nvl(SqlUtils.field(TBL_ERP_SALES, COL_TRADE_INVOICE_NO), "''")),
            COL_TRADE_INVOICE_NO)
        .addField(TBL_COMPANIES, COL_COMPANY_NAME, COL_TRADE_CUSTOMER)
        .addFields(TBL_COMPANIES, COL_COMPANY_CREDIT_DAYS, COL_COMPANY_TOLERATED_DAYS,
            "ExternalAdvance")
        .addExpr(SqlUtils.concat(SqlUtils.field(TBL_PERSONS, COL_FIRST_NAME), "' '",
            SqlUtils.nvl(SqlUtils.field(TBL_PERSONS, COL_LAST_NAME), "''")), COL_TRADE_MANAGER)
        .addField("CompanyBankruptRisks", "Name", "BankruptcyRisk")
        .addField("DelayedPaymentRisks", "Name", "DelayedPaymentRisk")
        .addField(VIEW_FINANCIAL_STATES, "Name", COL_COMPANY_FINANCIAL_STATE)
        .addFrom(TBL_ERP_SALES)
        .addFromLeft(TBL_COMPANIES,
            sys.joinTables(TBL_COMPANIES, TBL_ERP_SALES, COL_TRADE_CUSTOMER))
        .addFromLeft("CompanyBankruptRisks",
            sys.joinTables("CompanyBankruptRisks", TBL_COMPANIES, "BankruptcyRisk"))
        .addFromLeft("DelayedPaymentRisks",
            sys.joinTables("DelayedPaymentRisks", TBL_COMPANIES, "DelayedPaymentRisk"))
        .addFromLeft(VIEW_FINANCIAL_STATES,
            sys.joinTables(VIEW_FINANCIAL_STATES, TBL_COMPANIES, COL_COMPANY_FINANCIAL_STATE))
        .addFromLeft(TBL_SALES_SERIES,
            sys.joinTables(TBL_SALES_SERIES, TBL_ERP_SALES, COL_TRADE_SALE_SERIES))
        .addFromLeft(TBL_USERS,
            sys.joinTables(TBL_USERS, TBL_ERP_SALES, COL_TRADE_MANAGER))
        .addFromLeft(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_USERS, COL_COMPANY_PERSON))
        .addFromLeft(TBL_PERSONS,
            sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
        .setWhere(clause));

    qs.updateData(new SqlUpdate(tmp)
        .addExpression(COL_TRADE_TERM, SqlUtils.plus(SqlUtils.name(COL_TRADE_DATE),
            SqlUtils.multiply(SqlUtils.nvl(SqlUtils.name(COL_COMPANY_CREDIT_DAYS), 0),
                TimeUtils.MILLIS_PER_DAY)))
        .setWhere(SqlUtils.isNull(tmp, COL_TRADE_TERM)));

    long now = System.currentTimeMillis();

    if (report.requiresField(VAR_OVERDUE)) {
      qs.updateData(new SqlUpdate(tmp)
          .addExpression(VAR_OVERDUE, SqlUtils.name(COL_TRADE_DEBT))
          .setWhere(SqlUtils.less(tmp, COL_TRADE_TERM, now)));
    }
    if (report.requiresField(VAR_UNTOLERATED)) {
      qs.updateData(new SqlUpdate(tmp)
          .addExpression(VAR_UNTOLERATED, SqlUtils.name(COL_TRADE_DEBT))
          .setWhere(SqlUtils.more(SqlUtils.minus(now, SqlUtils.name(COL_TRADE_TERM)),
              SqlUtils.multiply(SqlUtils.nvl(SqlUtils.name(COL_COMPANY_TOLERATED_DAYS), 0),
                  SqlUtils.cast(SqlUtils.constant(TimeUtils.MILLIS_PER_DAY),
                      SqlConstants.SqlDataType.LONG, 0, 0)))));
    }
    if (report.requiresField(VAR_OVERDUE_DAYS)) {
      qs.updateData(new SqlUpdate(tmp)
          .addExpression(VAR_OVERDUE_DAYS, SqlUtils.divide(SqlUtils.minus(now,
              SqlUtils.name(COL_TRADE_TERM)), TimeUtils.MILLIS_PER_DAY))
          .setWhere(SqlUtils.less(tmp, COL_TRADE_TERM, now)));
    }
    String subq = "subq";

    if (report.requiresField(COL_COMPANY_USER_RESPONSIBILITY)) {
      qs.updateData(new SqlUpdate(tmp)
          .addExpression(COL_COMPANY_USER_RESPONSIBILITY, SqlUtils.concat(SqlUtils.field(subq,
              COL_FIRST_NAME), "' '", SqlUtils.nvl(SqlUtils.field(subq, COL_LAST_NAME), "''")))
          .setFrom(new SqlSelect()
                  .addFields(TBL_COMPANY_USERS, COL_COMPANY)
                  .addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME)
                  .addFrom(TBL_COMPANY_USERS)
                  .addFromInner(TBL_USERS,
                      sys.joinTables(TBL_USERS, TBL_COMPANY_USERS, COL_USER))
                  .addFromInner(TBL_COMPANY_PERSONS,
                      sys.joinTables(TBL_COMPANY_PERSONS, TBL_USERS, COL_COMPANY_PERSON))
                  .addFromInner(TBL_PERSONS,
                      sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
                  .setWhere(SqlUtils.notNull(TBL_COMPANY_USERS, COL_COMPANY_USER_RESPONSIBILITY)),
              subq, SqlUtils.joinUsing(tmp, subq, COL_COMPANY)));
    }
    if (report.requiresField(COL_TRADE_DEBT + COL_TRADE_MANAGER)) {
      String debts = qs.sqlCreateTemp(new SqlSelect()
          .addFields(tmp, COL_COMPANY, COL_TRADE_MANAGER)
          .addSum(tmp, COL_TRADE_DEBT)
          .addFrom(tmp)
          .addGroup(tmp, COL_COMPANY, COL_TRADE_MANAGER));

      qs.updateData(new SqlUpdate(tmp)
          .addExpression(COL_TRADE_DEBT + COL_TRADE_MANAGER,
              SqlUtils.field(subq, COL_TRADE_MANAGER))
          .setFrom(new SqlSelect()
                  .addFields(debts, COL_COMPANY, COL_TRADE_MANAGER)
                  .addFrom(debts)
                  .addFromInner(new SqlSelect()
                          .addFields(debts, COL_COMPANY)
                          .addMax(debts, COL_TRADE_DEBT)
                          .addFrom(debts)
                          .addGroup(debts, COL_COMPANY), "xxx",
                      SqlUtils.joinUsing(debts, "xxx", COL_COMPANY, COL_TRADE_DEBT)),
              subq, SqlUtils.joinUsing(tmp, subq, COL_COMPANY)));

      qs.sqlDropTemp(debts);
    }
    return report.getResultResponse(qs, tmp,
        Localizations.getDictionary(reqInfo.getParameter(VAR_LOCALE)),
        report.getCondition(tmp, COL_TRADE_MANAGER),
        report.getCondition(tmp, COL_COMPANY_USER_RESPONSIBILITY),
        report.getCondition(tmp, COL_TRADE_DEBT + COL_TRADE_MANAGER),
        report.getCondition(tmp, COL_TRADE_INVOICE_NO));
  }

  public Multimap<Long, String> getTradeActEmails(Set<Long> invoices) {
    HashMultimap<Long, String> emails = HashMultimap.create();

    SimpleRowSet rs = qs.getData(new SqlSelect().setDistinctMode(true)
        .addFields(TBL_SALE_ITEMS, COL_SALE)
        .addFields(TBL_EMAILS, COL_EMAIL_ADDRESS)
        .addFrom(TBL_TRADE_ACT_INVOICES)
        .addFromInner(TBL_SALE_ITEMS,
            sys.joinTables(TBL_SALE_ITEMS, TBL_TRADE_ACT_INVOICES, "SaleItem"))
        .addFromInner(TBL_TRADE_ACT_SERVICES,
            sys.joinTables(TBL_TRADE_ACT_SERVICES, TBL_TRADE_ACT_INVOICES, COL_TA_INVOICE_SERVICE))
        .addFromInner(TBL_TRADE_ACTS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT))
        .addFromInner(TBL_COMPANY_PERSONS,
            sys.joinTables(TBL_COMPANY_PERSONS, TBL_TRADE_ACTS, COL_CONTACT))
        .addFromInner(TBL_CONTACTS, sys.joinTables(TBL_CONTACTS, TBL_COMPANY_PERSONS, COL_CONTACT))
        .addFromInner(TBL_EMAILS, sys.joinTables(TBL_EMAILS, TBL_CONTACTS, COL_EMAIL))
        .setWhere(SqlUtils.and(SqlUtils.inList(TBL_SALE_ITEMS, COL_SALE, invoices),
            SqlUtils.notNull(TBL_EMAILS, COL_EMAIL_ADDRESS))));

    rs.forEach(row -> emails.put(row.getLong(COL_SALE), row.getValue(COL_EMAIL_ADDRESS)));

    return emails;
  }
}
