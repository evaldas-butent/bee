package com.butent.bee.server.modules.trade;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class TradeActBean {

  private static BeeLogger logger = LogUtils.getLogger(TradeActBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @EJB
  DataEditorBean deb;

  public List<SearchResult> doSearch(String query) {
    Set<String> columns = Sets.newHashSet(COL_TA_NUMBER, COL_OPERATION_NAME, COL_STATUS_NAME,
        ALS_COMPANY_NAME);
    return qs.getSearchResults(VIEW_TRADE_ACTS, Filter.anyContains(columns, query));
  }

  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response;

    if (BeeUtils.same(svc, SVC_GET_ITEMS_FOR_SELECTION)) {
      response = getItemsForSelection(reqInfo);

    } else if (BeeUtils.same(svc, SVC_COPY_ACT)) {
      response = copyAct(reqInfo);

    } else if (BeeUtils.same(svc, SVC_SAVE_ACT_AS_TEMPLATE)) {
      response = saveActAsTemplate(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_TEMPLATE_ITEMS_AND_SERVICES)) {
      response = getTemplateItemsAndServices(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_ITEMS_FOR_RETURN)) {
      response = getItemsForReturn(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_ACTS_FOR_INVOICE)) {
      response = getActsForInvoice(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    return response;
  }

  public Collection<BeeParameter> getDefaultParameters(String module) {
    return Lists.newArrayList(
        BeeParameter.createText(module, PRM_IMPORT_TA_ITEM_RX, false, RX_IMPORT_ACT_ITEM));
  }

  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void fillActNumber(ViewInsertEvent event) {
        if (event.isBefore() && event.isTarget(VIEW_TRADE_ACTS)
            && !DataUtils.contains(event.getColumns(), COL_TA_NUMBER)) {

          TradeActKind kind = null;
          Long series = null;

          for (int i = 0; i < event.getColumns().size(); i++) {
            switch (event.getColumns().get(i).getId()) {
              case COL_TA_KIND:
                kind = EnumUtils.getEnumByIndex(TradeActKind.class, event.getRow().getInteger(i));
                break;
              case COL_TA_SERIES:
                series = event.getRow().getLong(i);
                break;
            }
          }

          if (kind != null && kind.autoNumber() && DataUtils.isId(series)) {
            String number = getNextActNumber(series);
            BeeColumn column = sys.getView(VIEW_TRADE_ACTS).getBeeColumn(COL_TA_NUMBER);

            if (!BeeUtils.isEmpty(number) && number.length() <= column.getPrecision()) {
              event.addValue(column, new TextValue(number));
            }
          }
        }
      }
    });
  }

  private ResponseObject copyAct(RequestInfo reqInfo) {
    Long actId = reqInfo.getParameterLong(COL_TRADE_ACT);
    if (!DataUtils.isId(actId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TRADE_ACT);
    }

    BeeRowSet rowSet = qs.getViewData(VIEW_TRADE_ACTS, Filter.compareId(actId));
    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject.error(reqInfo.getService(), VIEW_TRADE_ACTS, actId, "not available");
    }

    BeeRow row = rowSet.getRow(0);

    row.setValue(rowSet.getColumnIndex(COL_TA_DATE), TimeUtils.nowMinutes());

    int index = rowSet.getColumnIndex(COL_TA_UNTIL);
    if (!row.isNull(index) && BeeUtils.isLess(row.getDateTime(index),
        TimeUtils.startOfNextMonth(TimeUtils.today()).getDateTime())) {
      row.clearCell(index);
    }

    row.clearCell(rowSet.getColumnIndex(COL_TA_NUMBER));

    BeeRowSet insert = DataUtils.createRowSetForInsert(rowSet.getViewName(), rowSet.getColumns(),
        row);
    ResponseObject response = deb.commitRow(insert);

    if (!response.hasErrors() && response.hasResponse(BeeRow.class)) {
      long newId = ((BeeRow) response.getResponse()).getId();

      qs.copyData(TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT, actId, newId);
      qs.copyData(TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT, actId, newId);
    }

    return response;
  }

  private Set<Long> getActItems(Long actId) {
    if (DataUtils.isId(actId)) {
      return qs.getLongSet(new SqlSelect()
          .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
          .addFrom(TBL_TRADE_ACT_ITEMS)
          .setWhere(SqlUtils.equals(TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT, actId)));

    } else {
      return BeeConst.EMPTY_IMMUTABLE_LONG_SET;
    }
  }

  private Set<Long> getActServices(Long actId) {
    if (DataUtils.isId(actId)) {
      return qs.getLongSet(new SqlSelect()
          .addFields(TBL_TRADE_ACT_SERVICES, COL_TA_ITEM)
          .addFrom(TBL_TRADE_ACT_SERVICES)
          .setWhere(SqlUtils.equals(TBL_TRADE_ACT_SERVICES, COL_TRADE_ACT, actId)));

    } else {
      return BeeConst.EMPTY_IMMUTABLE_LONG_SET;
    }
  }

  private String getNextActNumber(long series) {
    IsCondition where = SqlUtils.and(SqlUtils.equals(TBL_TRADE_ACTS, COL_TA_SERIES, series),
        SqlUtils.notNull(TBL_TRADE_ACTS, COL_TA_NUMBER));

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_ACTS, COL_TA_NUMBER)
        .addFrom(TBL_TRADE_ACTS)
        .setWhere(where);

    String[] values = qs.getColumn(query);

    long max = 0;
    BigInteger bigMax = null;

    if (!ArrayUtils.isEmpty(values)) {
      for (String value : values) {
        if (BeeUtils.isDigit(value)) {
          if (BeeUtils.isLong(value)) {
            max = Math.max(max, BeeUtils.toLong(value));

          } else {
            BigInteger big = new BigInteger(value);

            if (bigMax == null || BeeUtils.isLess(bigMax, big)) {
              bigMax = big;
            }
          }
        }
      }
    }

    BigInteger big = new BigInteger(BeeUtils.toString(max));
    if (bigMax != null) {
      big = big.max(bigMax);
    }

    return big.add(BigInteger.ONE).toString();
  }

  private ResponseObject getActsForInvoice(RequestInfo reqInfo) {
    String dFrom = reqInfo.getParameter(COL_TA_SERVICE_FROM);
    String dTo = reqInfo.getParameter(COL_TA_SERVICE_TO);

    Long company = reqInfo.getParameterLong(COL_TA_COMPANY);

    LongValue timeFrom =
        BeeUtils.isDigit(dFrom) ? new LongValue(TimeUtils.daysToTime(BeeUtils.toInt(dFrom))) : null;
    LongValue timeTo =
        BeeUtils.isDigit(dTo) ? new LongValue(TimeUtils.daysToTime(BeeUtils.toInt(dTo))) : null;

    CompoundFilter filter = Filter.and();
    filter.add(TradeActKind.getFilterForInvoiceBuilder());

    filter.add(Filter.or(Filter.isNull(COL_TA_STATUS), Filter.notNull(COL_STATUS_ACTIVE)));

    if (timeFrom != null) {
      filter.add(Filter.or(Filter.isNull(COL_TA_UNTIL), Filter.isMore(COL_TA_UNTIL, timeFrom)));
    }
    if (timeTo != null) {
      filter.add(Filter.isLess(COL_TA_DATE, timeTo));
    }

    if (DataUtils.isId(company)) {
      filter.add(Filter.equals(COL_TA_COMPANY, company));
    }

    filter.add(Filter.in(sys.getIdName(VIEW_TRADE_ACTS), VIEW_TRADE_ACT_SERVICES, COL_TRADE_ACT));

    BeeRowSet acts = qs.getViewData(VIEW_TRADE_ACTS, filter);
    if (DataUtils.isEmpty(acts)) {
      return ResponseObject.emptyResponse();
    }

    Filter itemFilter = Filter.and(Filter.isPositive(COL_TRADE_ITEM_QUANTITY),
        Filter.isPositive(COL_TRADE_ITEM_PRICE));

    Totalizer itemTotalizer = null;

    SqlSelect lastInvoiceQuery = new SqlSelect()
        .addMax(TBL_TRADE_ACT_INVOICES, COL_TA_INVOICE_TO)
        .addFrom(TBL_TRADE_ACT_INVOICES);

    for (BeeRow act : acts) {
      long actId = act.getId();

      BeeRowSet items = qs.getViewData(VIEW_TRADE_ACT_ITEMS,
          Filter.and(Filter.equals(COL_TRADE_ACT, actId), itemFilter));

      if (!DataUtils.isEmpty(items)) {
        Map<Long, Double> returnedItems = getReturnedItems(actId);

        if (itemTotalizer == null) {
          itemTotalizer = new Totalizer(items.getColumns());
        }

        double itemTotal = BeeConst.DOUBLE_ZERO;
        double returnedTotal = BeeConst.DOUBLE_ZERO;

        for (BeeRow item : items) {
          Double total = itemTotalizer.getTotal(item);
          if (BeeUtils.isPositive(total)) {
            itemTotal += total;
          }

          if (!returnedItems.isEmpty()) {
            Double qty = returnedItems.get(DataUtils.getLong(items, item, COL_TA_ITEM));

            if (BeeUtils.isPositive(qty)) {
              item.setValue(items.getColumnIndex(COL_TRADE_ITEM_QUANTITY), qty);
              Double returned = itemTotalizer.getTotal(item);

              if (BeeUtils.isPositive(returned)) {
                returnedTotal += returned;
              }
            }
          }
        }

        if (BeeUtils.isPositive(itemTotal)) {
          act.setProperty(PRP_ITEM_TOTAL, BeeUtils.toString(itemTotal, 2));
        }
        if (BeeUtils.isPositive(returnedTotal)) {
          act.setProperty(PRP_RETURNED_TOTAL, BeeUtils.toString(returnedTotal, 2));
        }
      }

      lastInvoiceQuery.setWhere(SqlUtils.equals(TBL_TRADE_ACT_INVOICES, COL_TRADE_ACT, actId));
      JustDate latest = qs.getDate(lastInvoiceQuery);
      if (latest != null) {
        act.setProperty(PRP_LATEST_INVOICE, latest.toString());
      }
    }

    return ResponseObject.response(acts);
  }

  private ResponseObject getItemsForReturn(RequestInfo reqInfo) {
    Long actId = reqInfo.getParameterLong(COL_TRADE_ACT);
    if (!DataUtils.isId(actId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TRADE_ACT);
    }

    Filter filter = Filter.and(Filter.equals(COL_TRADE_ACT, actId),
        Filter.isPositive(COL_TRADE_ITEM_QUANTITY));

    BeeRowSet parentItems = qs.getViewData(VIEW_TRADE_ACT_ITEMS, filter);
    if (DataUtils.isEmpty(parentItems)) {
      return ResponseObject.emptyResponse();
    }

    BeeRowSet parentAct = qs.getViewData(VIEW_TRADE_ACTS, Filter.compareId(actId));
    String serializedParent = DataUtils.isEmpty(parentAct) ? null : parentAct.getRow(0).serialize();

    Map<Long, Double> returnedItems = getReturnedItems(actId);

    if (BeeUtils.isEmpty(returnedItems)) {
      parentItems.setTableProperty(PRP_PARENT_ACT, serializedParent);
      return ResponseObject.response(parentItems);
    }

    BeeRowSet result = new BeeRowSet(parentItems.getViewName(), parentItems.getColumns());

    int itemIndex = parentItems.getColumnIndex(COL_TA_ITEM);
    int qtyIndex = parentItems.getColumnIndex(COL_TRADE_ITEM_QUANTITY);

    for (BeeRow parentRow : parentItems) {
      double qty = parentRow.getDouble(qtyIndex);
      Double returnedQty = returnedItems.get(parentRow.getLong(itemIndex));

      boolean found = BeeUtils.isPositive(returnedQty);
      if (found) {
        qty -= returnedQty;
      }

      if (BeeUtils.isPositive(qty)) {
        BeeRow row = DataUtils.cloneRow(parentRow);
        if (found) {
          row.setValue(qtyIndex, qty);
        }

        result.addRow(row);
      }
    }

    if (DataUtils.isEmpty(result)) {
      return ResponseObject.emptyResponse();
    } else {
      result.setTableProperty(PRP_PARENT_ACT, serializedParent);
      return ResponseObject.response(result);
    }
  }

  private ResponseObject getItemsForSelection(RequestInfo reqInfo) {
    TradeActKind kind = EnumUtils.getEnumByIndex(TradeActKind.class,
        reqInfo.getParameter(COL_TA_KIND));

    if (kind == null) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TA_KIND);
    }

    Long actId = reqInfo.getParameterLong(COL_TRADE_ACT);
    Long warehouse = reqInfo.getParameterLong(COL_WAREHOUSE);

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);

    CompoundFilter filter = Filter.and();
    filter.add(Filter.isNull(COL_ITEM_IS_SERVICE));

    Set<Long> actItems = getActItems(actId);
    if (!actItems.isEmpty()) {
      filter.add(Filter.idNotIn(actItems));
    }

    if (!BeeUtils.isEmpty(where)) {
      filter.add(Filter.restore(where));
    }

    BeeRowSet items = qs.getViewData(VIEW_ITEMS, filter);
    if (DataUtils.isEmpty(items)) {
      logger.debug(reqInfo.getService(), "no items found", filter);
      return ResponseObject.emptyResponse();
    }

    if (kind.showStock()) {
      List<Long> itemIds = (items.getNumberOfRows() < 200)
          ? items.getRowIds() : BeeConst.EMPTY_IMMUTABLE_LONG_LIST;
      Table<Long, Long, Double> stock = getStock(itemIds);

      if (stock != null) {
        int scale = sys.getFieldScale(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY);

        List<BeeRow> hasStock = new ArrayList<>();
        List<BeeRow> noStock = new ArrayList<>();

        for (BeeRow row : items) {
          boolean has = false;

          if (stock.containsRow(row.getId())) {
            for (Map.Entry<Long, Double> entry : stock.row(row.getId()).entrySet()) {
              row.setProperty(PRP_WAREHOUSE_PREFIX + BeeUtils.toString(entry.getKey()),
                  BeeUtils.toString(entry.getValue(), scale));

              if (warehouse != null && warehouse.equals(entry.getKey())) {
                has = BeeUtils.isPositive(entry.getValue());
              }
            }
          }

          if (warehouse != null) {
            if (has) {
              hasStock.add(row);
            } else {
              noStock.add(row);
            }
          }
        }

        if (!hasStock.isEmpty() && !noStock.isEmpty()) {
          items.clearRows();
          items.addRows(hasStock);
          items.addRows(noStock);
        }

        items.setTableProperty(TBL_WAREHOUSES, DataUtils.buildIdList(stock.columnKeySet()));
      }
    }

    return ResponseObject.response(items);
  }

  private Map<Long, Double> getReturnedItems(long actId) {
    Map<Long, Double> result = new HashMap<>();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addSum(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_ACTS)
        .addFromInner(TBL_TRADE_ACT_ITEMS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TRADE_ACTS, COL_TA_PARENT, actId),
            SqlUtils.equals(TBL_TRADE_ACTS, COL_TA_KIND, TradeActKind.RETURN.ordinal())))
        .addGroup(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM);

    SimpleRowSet data = qs.getData(query);
    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        Long item = row.getLong(0);
        Double qty = row.getDouble(1);

        if (DataUtils.isId(item) && BeeUtils.isPositive(qty)) {
          result.put(item, qty);
        }
      }
    }

    return result;
  }

  private Table<Long, Long, Double> getStock(Collection<Long> items) {
    Table<Long, Long, Double> result = HashBasedTable.create();

    IsCondition condition = BeeUtils.isEmpty(items)
        ? null : SqlUtils.inList(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM, items);

    SqlSelect plusQuery = getStockQuery(condition, true);
    SimpleRowSet plusData = qs.getData(plusQuery);

    if (!DataUtils.isEmpty(plusData)) {
      for (SimpleRow row : plusData) {
        Double qty = row.getDouble(COL_TRADE_ITEM_QUANTITY);
        if (BeeUtils.nonZero(qty)) {
          result.put(row.getLong(COL_TA_ITEM), row.getLong(COL_OPERATION_WAREHOUSE_TO), qty);
        }
      }
    }

    SqlSelect minusQuery = getStockQuery(condition, false);
    SimpleRowSet minusData = qs.getData(minusQuery);

    if (!DataUtils.isEmpty(minusData)) {
      for (SimpleRow row : minusData) {
        Double qty = row.getDouble(COL_TRADE_ITEM_QUANTITY);

        if (BeeUtils.nonZero(qty)) {
          Long item = row.getLong(COL_TA_ITEM);
          Long warehouse = row.getLong(COL_OPERATION_WAREHOUSE_FROM);

          Double stock = result.get(item, warehouse);

          if (stock == null) {
            result.put(item, warehouse, -qty);
          } else if (stock.equals(qty)) {
            result.remove(item, warehouse);
          } else {
            result.put(item, warehouse, stock - qty);
          }
        }
      }
    }

    return result;
  }

  private SqlSelect getStockQuery(IsCondition condition, boolean plus) {
    String colWarehouse = plus ? COL_OPERATION_WAREHOUSE_TO : COL_OPERATION_WAREHOUSE_FROM;

    return new SqlSelect()
        .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addFields(TBL_TRADE_OPERATIONS, colWarehouse)
        .addSum(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY)
        .addFrom(TBL_TRADE_ACT_ITEMS)
        .addFromInner(TBL_TRADE_ACTS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT))
        .addFromInner(TBL_TRADE_OPERATIONS,
            sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_ACTS, COL_TA_OPERATION))
        .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_TRADE_OPERATIONS, colWarehouse), condition))
        .addGroup(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addGroup(TBL_TRADE_OPERATIONS, colWarehouse);
  }

  private static Filter getTemplateChildrenFilter(Long templateId, Collection<Long> excludeItems) {
    if (BeeUtils.isEmpty(excludeItems)) {
      return Filter.equals(COL_TRADE_ACT_TEMPLATE, templateId);
    } else {
      return Filter.and(Filter.equals(COL_TRADE_ACT_TEMPLATE, templateId),
          Filter.exclude(COL_TA_ITEM, excludeItems));
    }
  }

  private ResponseObject getTemplateItemsAndServices(RequestInfo reqInfo) {
    Long templateId = reqInfo.getParameterLong(COL_TRADE_ACT_TEMPLATE);
    if (!DataUtils.isId(templateId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TRADE_ACT_TEMPLATE);
    }

    Long actId = reqInfo.getParameterLong(COL_TRADE_ACT);
    TradeActKind kind = EnumUtils.getEnumByIndex(TradeActKind.class,
        reqInfo.getParameter(COL_TA_KIND));

    List<BeeRowSet> result = new ArrayList<>();

    Set<Long> itemIds = new HashSet<>();

    Set<Long> actItems = getActItems(actId);
    Filter filter = getTemplateChildrenFilter(templateId, actItems);

    BeeRowSet templateItems = qs.getViewData(VIEW_TRADE_ACT_TMPL_ITEMS, filter);
    if (!DataUtils.isEmpty(templateItems)) {
      result.add(templateItems);

      int index = templateItems.getColumnIndex(COL_TA_ITEM);
      itemIds.addAll(templateItems.getDistinctLongs(index));
    }

    if (kind != null && kind.enableServices()) {
      Set<Long> actServices = getActServices(actId);
      filter = getTemplateChildrenFilter(templateId, actServices);

      BeeRowSet templateServices = qs.getViewData(VIEW_TRADE_ACT_TMPL_SERVICES, filter);
      if (!DataUtils.isEmpty(templateServices)) {
        result.add(templateServices);

        int index = templateServices.getColumnIndex(COL_TA_ITEM);
        itemIds.addAll(templateServices.getDistinctLongs(index));
      }
    }

    if (!itemIds.isEmpty()) {
      BeeRowSet items = qs.getViewData(VIEW_ITEMS, Filter.idIn(itemIds));
      if (!DataUtils.isEmpty(items)) {
        result.add(items);
      }
    }

    if (result.isEmpty()) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.response(result).setSize(result.size());
    }
  }

  private ResponseObject saveActAsTemplate(RequestInfo reqInfo) {
    Long actId = reqInfo.getParameterLong(COL_TRADE_ACT);
    if (!DataUtils.isId(actId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TRADE_ACT);
    }

    String name = reqInfo.getParameter(COL_TA_TEMPLATE_NAME);
    if (BeeUtils.isEmpty(name)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TA_TEMPLATE_NAME);
    }

    if (qs.sqlExists(TBL_TRADE_ACT_TEMPLATES, COL_TA_TEMPLATE_NAME, name)) {
      return ResponseObject.error(usr.getLocalizableMesssages().valueExists(name));
    }

    SimpleRow actRow = qs.getRow(TBL_TRADE_ACTS, actId);
    if (actRow == null) {
      String msg = BeeUtils.joinWords(reqInfo.getService(), COL_TRADE_ACT, actId, "not found");
      logger.severe(msg);
      return ResponseObject.error(msg);
    }

    SqlInsert insert = new SqlInsert(TBL_TRADE_ACT_TEMPLATES)
        .addConstant(COL_TA_TEMPLATE_NAME, name);

    for (String colName : actRow.getColumnNames()) {
      String value = actRow.getValue(colName);
      if (!BeeUtils.isEmpty(value) && sys.hasField(TBL_TRADE_ACT_TEMPLATES, colName)) {
        insert.addConstant(colName, value);
      }
    }

    long templateId = qs.insertData(insert);
    if (!DataUtils.isId(templateId)) {
      return ResponseObject.error(reqInfo.getService(), "cannot create template");
    }

    saveActChildrenToTemplate(actId, templateId, TBL_TRADE_ACT_ITEMS, TBL_TRADE_ACT_TMPL_ITEMS);
    saveActChildrenToTemplate(actId, templateId, TBL_TRADE_ACT_SERVICES,
        TBL_TRADE_ACT_TMPL_SERVICES);

    BeeRowSet data = qs.getViewData(VIEW_TRADE_ACT_TEMPLATES, Filter.compareId(templateId));
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.error(reqInfo.getService(), "template is dead");
    } else {
      return ResponseObject.response(data.getRow(0));
    }
  }

  private void saveActChildrenToTemplate(long actId, long templateId,
      String sourceTable, String targetTable) {

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addAllFields(sourceTable)
        .addFrom(sourceTable)
        .setWhere(SqlUtils.equals(sourceTable, COL_TRADE_ACT, actId)));

    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        SqlInsert insert = new SqlInsert(targetTable)
            .addConstant(COL_TRADE_ACT_TEMPLATE, templateId);

        for (String colName : row.getColumnNames()) {
          String value = row.getValue(colName);
          if (!BeeUtils.isEmpty(value) && sys.hasField(targetTable, colName)) {
            insert.addConstant(colName, value);
          }
        }

        long id = qs.insertData(insert);
        if (!DataUtils.isId(id)) {
          break;
        }
      }
    }
  }
}
