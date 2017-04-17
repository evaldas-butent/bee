package com.butent.bee.server.modules.trade;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SqlConstants;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeReportGroup;
import com.butent.bee.shared.report.ReportParameters;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class TradeReportsBean {

  private static BeeLogger logger = LogUtils.getLogger(TradeReportsBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;

  public ResponseObject doService(String service, RequestInfo reqInfo) {
    ResponseObject response;

    String svc = BeeUtils.trim(service);
    switch (svc) {
      case SVC_TRADE_STOCK_REPORT:
        response = doStockReport(reqInfo);
        break;

      case SVC_TRADE_MOVEMENT_OF_GOODS_REPORT:
        response = doMovementOfGoodsReport(reqInfo);
        break;

      default:
        String msg = BeeUtils.joinWords("service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  private ResponseObject doStockReport(RequestInfo reqInfo) {
    if (!reqInfo.hasParameter(Service.VAR_REPORT_PARAMETERS)) {
      return ResponseObject.parameterNotFound(reqInfo.getLabel(), Service.VAR_REPORT_PARAMETERS);
    }

    ReportParameters parameters =
        ReportParameters.restore(reqInfo.getParameter(Service.VAR_REPORT_PARAMETERS));

    DateTime date = parameters.getDateTime(RP_DATE);

    boolean showQuantity = parameters.getBoolean(RP_SHOW_QUANTITY);
    boolean showAmount = parameters.getBoolean(RP_SHOW_AMOUNT);

    if (!showQuantity && !showAmount) {
      showQuantity = true;
      showAmount = true;
    }

    ItemPrice itemPrice = parameters.getEnum(RP_ITEM_PRICE, ItemPrice.class);
    if (itemPrice == ItemPrice.COST) {
      itemPrice = null;
    }

    Long currency = parameters.getLong(RP_CURRENCY);

    Set<Long> warehouses = parameters.getIds(RP_WAREHOUSES);
    Set<Long> suppliers = parameters.getIds(RP_SUPPLIERS);
    Set<Long> manufacturers = parameters.getIds(RP_MANUFACTURERS);
    Set<Long> documents = parameters.getIds(RP_DOCUMENTS);

    Set<Long> itemTypes = parameters.getIds(RP_ITEM_TYPES);
    Set<Long> itemGroups = parameters.getIds(RP_ITEM_GROUPS);
    Set<Long> itemCategories = parameters.getIds(RP_ITEM_CATEGORIES);
    Set<Long> items = parameters.getIds(RP_ITEMS);

    DateTime receivedFrom = parameters.getDateTime(RP_RECEIVED_FROM);
    DateTime receivedTo = parameters.getDateTime(RP_RECEIVED_TO);

    String itemFilter = parameters.getText(RP_ITEM_FILTER);

    if (!items.isEmpty()) {
      manufacturers.clear();

      itemTypes.clear();
      itemGroups.clear();
      itemCategories.clear();

      itemFilter = null;
    }

    List<TradeReportGroup> rowGroups = TradeReportGroup.parseList(parameters, 5);
    boolean summary = parameters.getBoolean(RP_SUMMARY);

    TradeReportGroup columnGroup = TradeReportGroup.parse(parameters.getText(RP_COLUMNS));
    if (columnGroup == null) {
      columnGroup = TradeReportGroup.WAREHOUSE;
    }

    String aliasStock = TBL_TRADE_STOCK;
    IsCondition stockCondition = getStockCondition(aliasStock, warehouses);

    String aliasPrimaryDocumentItems = SqlUtils.uniqueName("pdi");
    IsCondition primaryDocumentItemCondition = getPrimaryDocumentItemCondition(
        aliasPrimaryDocumentItems, items, itemCategories, manufacturers);

    String aliasPrimaryDocuments = SqlUtils.uniqueName("pdo");
    IsCondition primaryDocumentCondition =
        getPrimaryDocumentCondition(aliasPrimaryDocuments, suppliers, receivedFrom, receivedTo);

    String aliasDocumentItems = SqlUtils.uniqueName("dit");
    IsCondition documentItemCondition = getDocumentItemCondition(aliasDocumentItems, documents);

    String aliasItems = TBL_ITEMS;
    IsCondition itemTypeCondition = getItemTypeCondition(aliasItems, itemTypes);
    IsCondition itemGroupCondition = getItemGroupCondition(aliasItems, itemGroups);

    IsCondition itemCondition = parseItemFilter(itemFilter);

    boolean needsItems = itemTypeCondition != null || itemGroupCondition != null
        || itemCondition != null;

    String documentItemId = sys.getIdName(TBL_TRADE_DOCUMENT_ITEMS);
    String documentId = sys.getIdName(TBL_TRADE_DOCUMENTS);

    String aliasQuantity = SqlUtils.uniqueName("qty");

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_STOCK, COL_TRADE_DOCUMENT_ITEM, COL_STOCK_WAREHOUSE);

    if (date == null) {
      query.addField(TBL_TRADE_STOCK, COL_STOCK_QUANTITY, aliasQuantity);
    } else {
      query.addExpr(zero(TBL_TRADE_STOCK, COL_STOCK_QUANTITY), aliasQuantity);
    }

    query.addFrom(TBL_TRADE_STOCK);

    if (primaryDocumentItemCondition != null || primaryDocumentCondition != null || needsItems) {
      query.addFromLeft(TBL_TRADE_DOCUMENT_ITEMS, aliasPrimaryDocumentItems,
          SqlUtils.join(aliasPrimaryDocumentItems, documentItemId,
              aliasStock, COL_PRIMARY_DOCUMENT_ITEM));
    }
    if (primaryDocumentCondition != null) {
      query.addFromLeft(TBL_TRADE_DOCUMENTS, aliasPrimaryDocuments,
          SqlUtils.join(aliasPrimaryDocuments, documentId,
              aliasPrimaryDocumentItems, COL_TRADE_DOCUMENT));
    }

    if (documentItemCondition != null) {
      query.addFromLeft(TBL_TRADE_DOCUMENT_ITEMS, aliasDocumentItems,
          SqlUtils.join(aliasDocumentItems, documentItemId,
              aliasStock, COL_TRADE_DOCUMENT_ITEM));
    }

    if (needsItems) {
      query.addFromLeft(TBL_ITEMS, sys.joinTables(TBL_ITEMS, aliasPrimaryDocumentItems, COL_ITEM));
    }

    HasConditions where = SqlUtils.and(stockCondition, primaryDocumentItemCondition,
        primaryDocumentCondition, documentItemCondition, itemTypeCondition, itemGroupCondition,
        itemCondition);

    if (date == null) {
      where.add(SqlUtils.nonZero(TBL_TRADE_STOCK, COL_STOCK_QUANTITY));
    }

    query.setWhere(normalize(where));

    String tmp = qs.sqlCreateTemp(query);
    if (qs.isEmpty(tmp)) {
      qs.sqlDropTemp(tmp);
      return ResponseObject.emptyResponse();
    }

    qs.sqlIndex(tmp, COL_TRADE_DOCUMENT_ITEM);

    if (date != null) {
      ResponseObject response = calculateStock(tmp, aliasQuantity, date);
      if (response.hasErrors()) {
        qs.sqlDropTemp(tmp);
        return response;
      }

      SqlDelete delete = new SqlDelete(tmp)
          .setWhere(SqlUtils.or(SqlUtils.isNull(tmp, aliasQuantity),
              SqlUtils.equals(tmp, aliasQuantity, 0)));

      response = qs.updateDataWithResponse(delete);
      if (response.hasErrors()) {
        qs.sqlDropTemp(tmp);
        return response;
      }

      if (qs.isEmpty(tmp)) {
        qs.sqlDropTemp(tmp);
        return ResponseObject.emptyResponse();
      }
    }

    SimpleRowSet data = qs.getData(new SqlSelect().addAllFields(tmp).addFrom(tmp));
    qs.sqlDropTemp(tmp);

    if (DataUtils.isEmpty(data)) {
      return ResponseObject.emptyResponse();
    }

    return ResponseObject.response(data);
  }

  private ResponseObject doMovementOfGoodsReport(RequestInfo reqInfo) {
    return ResponseObject.emptyResponse();
  }

  private static IsCondition getItemTypeCondition(String alias, Collection<Long> itemTypes) {
    return BeeUtils.isEmpty(itemTypes) ? null : SqlUtils.inList(alias, COL_ITEM_TYPE, itemTypes);
  }

  private static IsCondition getItemGroupCondition(String alias, Collection<Long> itemGroups) {
    return BeeUtils.isEmpty(itemGroups) ? null : SqlUtils.inList(alias, COL_ITEM_GROUP, itemGroups);
  }

  private static IsCondition getPrimaryDocumentItemCondition(String alias,
      Collection<Long> items, Collection<Long> categories, Collection<Long> manufacturers) {

    HasConditions conditions = SqlUtils.and();

    if (!BeeUtils.isEmpty(items)) {
      conditions.add(SqlUtils.inList(alias, COL_ITEM, items));
    }
    if (!BeeUtils.isEmpty(categories)) {
      conditions.add(SqlUtils.in(alias, COL_ITEM, TBL_ITEM_CATEGORIES, COL_ITEM,
          SqlUtils.inList(TBL_ITEM_CATEGORIES, COL_CATEGORY, categories)));
    }
    if (!BeeUtils.isEmpty(manufacturers)) {
      conditions.add(SqlUtils.in(alias, COL_ITEM, TBL_ITEM_MANUFACTURERS, COL_ITEM,
          SqlUtils.inList(TBL_ITEM_MANUFACTURERS, COL_ITEM_MANUFACTURER, manufacturers)));
    }

    return normalize(conditions);
  }

  private static IsCondition getPrimaryDocumentCondition(String alias, Collection<Long> suppliers,
      DateTime receivedFrom, DateTime receivedTo) {

    HasConditions conditions = SqlUtils.and();

    if (!BeeUtils.isEmpty(suppliers)) {
      conditions.add(SqlUtils.inList(alias, COL_TRADE_SUPPLIER, suppliers));
    }

    if (receivedFrom != null) {
      conditions.add(SqlUtils.moreEqual(alias, COL_TRADE_DATE, receivedFrom));
    }
    if (receivedTo != null) {
      conditions.add(SqlUtils.less(alias, COL_TRADE_DATE, receivedTo));
    }

    return normalize(conditions);
  }

  private static IsCondition getDocumentItemCondition(String alias, Collection<Long> documents) {
    return BeeUtils.isEmpty(documents)
        ? null : SqlUtils.inList(alias, COL_TRADE_DOCUMENT, documents);
  }

  private static IsCondition getStockCondition(String alias, Collection<Long> warehouses) {
    return BeeUtils.isEmpty(warehouses)
        ? null : SqlUtils.inList(alias, COL_STOCK_WAREHOUSE, warehouses);
  }

  private static IsCondition normalize(HasConditions conditions) {
    if (conditions == null || conditions.isEmpty()) {
      return null;
    } else if (conditions.size() == 1) {
      return conditions.peek();
    } else {
      return conditions;
    }
  }

  private IsCondition parseItemFilter(String input) {
    if (BeeUtils.isEmpty(input)) {
      return null;

    } else {
      BeeView view = sys.getView(VIEW_ITEMS);
      Long userId = usr.getCurrentUserId();

      Filter filter = view.parseFilter(input, userId);
      return (filter == null) ? null : view.getCondition(filter);
    }
  }

  private IsExpression zero(String tblName, String fldName) {
    int precision = sys.getFieldPrecision(tblName, fldName);
    int scale = sys.getFieldScale(tblName, fldName);

    return SqlUtils.cast(SqlUtils.constant(0), SqlConstants.SqlDataType.DECIMAL, precision, scale);
  }

  private ResponseObject calculateStock(String tbl, String fld, DateTime date) {
    Set<Integer> producerTypes = Arrays.stream(OperationType.values())
        .filter(OperationType::producesStock)
        .map(OperationType::ordinal)
        .collect(Collectors.toSet());

    Set<Integer> consumerTypes = Arrays.stream(OperationType.values())
        .filter(OperationType::consumesStock)
        .map(OperationType::ordinal)
        .collect(Collectors.toSet());

    Set<Integer> stockPhases = Arrays.stream(TradeDocumentPhase.values())
        .filter(TradeDocumentPhase::modifyStock)
        .map(TradeDocumentPhase::ordinal)
        .collect(Collectors.toSet());

    IsCondition dateCondition = (date == null)
        ? null : SqlUtils.less(TBL_TRADE_DOCUMENTS, COL_TRADE_DATE, date);

    IsCondition producerCondition = SqlUtils.inList(TBL_TRADE_OPERATIONS, COL_OPERATION_TYPE,
        producerTypes);
    IsCondition consumerCondition = SqlUtils.inList(TBL_TRADE_OPERATIONS, COL_OPERATION_TYPE,
        consumerTypes);

    IsCondition phaseCondition = SqlUtils.inList(TBL_TRADE_DOCUMENTS, COL_TRADE_DOCUMENT_PHASE,
        stockPhases);

    SqlSelect producerQuery = new SqlSelect()
        .addFields(tbl, COL_TRADE_DOCUMENT_ITEM)
        .addFields(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(tbl)
        .addFromInner(TBL_TRADE_DOCUMENT_ITEMS, sys.joinTables(TBL_TRADE_DOCUMENT_ITEMS,
            tbl, COL_TRADE_DOCUMENT_ITEM))
        .addFromInner(TBL_TRADE_DOCUMENTS, sys.joinTables(TBL_TRADE_DOCUMENTS,
            TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
        .addFromInner(TBL_TRADE_OPERATIONS, sys.joinTables(TBL_TRADE_OPERATIONS,
            TBL_TRADE_DOCUMENTS, COL_TRADE_OPERATION))
        .setWhere(SqlUtils.and(dateCondition, producerCondition, phaseCondition));

    String producers = qs.sqlCreateTemp(producerQuery);

    if (qs.isEmpty(producers)) {
      qs.sqlDropTemp(producers);

    } else {
      qs.sqlIndex(producers, COL_TRADE_DOCUMENT_ITEM);

      SqlUpdate update = new SqlUpdate(tbl)
          .setFrom(producers, SqlUtils.join(producers, COL_TRADE_DOCUMENT_ITEM,
              tbl, COL_TRADE_DOCUMENT_ITEM))
          .addExpression(fld, SqlUtils.field(producers, COL_TRADE_ITEM_QUANTITY));

      ResponseObject updResponse = qs.updateDataWithResponse(update);
      qs.sqlDropTemp(producers);

      if (updResponse.hasErrors()) {
        return updResponse;
      }
    }

    SqlSelect consumerQuery = new SqlSelect()
        .addFields(tbl, COL_TRADE_DOCUMENT_ITEM)
        .addSum(TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(tbl)
        .addFromInner(TBL_TRADE_DOCUMENT_ITEMS, SqlUtils.join(tbl, COL_TRADE_DOCUMENT_ITEM,
            TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_ITEM_PARENT))
        .addFromInner(TBL_TRADE_DOCUMENTS, sys.joinTables(TBL_TRADE_DOCUMENTS,
            TBL_TRADE_DOCUMENT_ITEMS, COL_TRADE_DOCUMENT))
        .addFromInner(TBL_TRADE_OPERATIONS, sys.joinTables(TBL_TRADE_OPERATIONS,
            TBL_TRADE_DOCUMENTS, COL_TRADE_OPERATION))
        .setWhere(SqlUtils.and(dateCondition, consumerCondition, phaseCondition))
        .addGroup(tbl, COL_TRADE_DOCUMENT_ITEM);

    String consumers = qs.sqlCreateTemp(consumerQuery);

    if (qs.isEmpty(consumers)) {
      qs.sqlDropTemp(consumers);

    } else {
      qs.sqlIndex(consumers, COL_TRADE_DOCUMENT_ITEM);

      SqlUpdate update = new SqlUpdate(tbl)
          .setFrom(consumers, SqlUtils.join(consumers, COL_TRADE_DOCUMENT_ITEM,
              tbl, COL_TRADE_DOCUMENT_ITEM))
          .addExpression(fld,
              SqlUtils.minus(SqlUtils.field(tbl, fld),
                  SqlUtils.field(consumers, COL_TRADE_ITEM_QUANTITY)));

      ResponseObject updResponse = qs.updateDataWithResponse(update);
      qs.sqlDropTemp(consumers);

      if (updResponse.hasErrors()) {
        return updResponse;
      }
    }

    return ResponseObject.emptyResponse();
  }
}
