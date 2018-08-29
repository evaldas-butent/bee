package com.butent.bee.server.modules.trade;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.Dimensions.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDiscountMode;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeVatMode;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.webservice.ButentWS;
import com.butent.webservice.WSDocument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
        .addField(COL_TRADE_WAREHOUSE_FROM, COL_WAREHOUSE_CODE, COL_TRADE_WAREHOUSE_FROM)
        .addField(COL_TRADE_WAREHOUSE_TO, COL_WAREHOUSE_CODE, COL_TRADE_WAREHOUSE_TO)
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
}
