package com.butent.bee.server.modules.trade;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.AdministrationModuleBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
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
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.Totalizer;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;

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
  @EJB
  ParamHolderBean prm;
  @EJB
  AdministrationModuleBean adm;

  public List<SearchResult> doSearch(String query) {
    Set<String> columns = Sets.newHashSet(COL_TA_NAME, COL_TA_NUMBER, COL_OPERATION_NAME,
        COL_STATUS_NAME, ALS_COMPANY_NAME, COL_COMPANY_OBJECT_NAME);
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

    } else if (BeeUtils.same(svc, SVC_CREATE_ACT_INVOICE)) {
      response = createInvoice(reqInfo);

    } else if (BeeUtils.same(svc, SVC_CONVERT_ACT_TO_SALE)) {
      response = convertToSale(reqInfo);

    } else if (BeeUtils.same(svc, SVC_ITEMS_BY_COMPANY_REPORT)) {
      response = getItemsByCompanyReport(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    return response;
  }

  public Collection<BeeParameter> getDefaultParameters(String module) {
    return Lists.newArrayList(
        BeeParameter.createText(module, PRM_IMPORT_TA_ITEM_RX, false, RX_IMPORT_ACT_ITEM),
        BeeParameter.createNumber(module, PRM_TA_NUMBER_LENGTH, false, 6));
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
            BeeColumn column = sys.getView(VIEW_TRADE_ACTS).getBeeColumn(COL_TA_NUMBER);
            String number = getNextActNumber(series, column.getPrecision());

            if (!BeeUtils.isEmpty(number)) {
              event.addValue(column, new TextValue(number));
            }
          }
        }
      }

      @Subscribe
      public void maybeSetReturnedQty(ViewQueryEvent event) {
        if (event.isAfter() && event.isTarget(VIEW_TRADE_ACT_ITEMS) && event.hasData()
            && event.getColumnCount() >= sys.getView(event.getTargetName()).getColumnCount()) {

          BeeRowSet rowSet = event.getRowset();
          List<Long> actIds = DataUtils.getDistinct(rowSet, COL_TRADE_ACT);

          int actIndex = rowSet.getColumnIndex(COL_TRADE_ACT);
          int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);

          int qtyScale = rowSet.getColumn(COL_TRADE_ITEM_QUANTITY).getScale();

          for (Long actId : actIds) {
            TradeActKind kind = getActKind(actId);

            if (kind != null && kind.enableReturn()) {
              Map<Long, Double> returnedItems = getReturnedItems(actId);

              if (!returnedItems.isEmpty()) {
                for (BeeRow row : rowSet) {
                  if (actId.equals(row.getLong(actIndex))) {
                    Double qty = returnedItems.get(row.getLong(itemIndex));

                    if (BeeUtils.isPositive(qty)) {
                      row.setProperty(PRP_RETURNED_QTY, BeeUtils.toString(qty, qtyScale));
                    }
                  }
                }
              }
            }
          }
        }
      }
    });
  }

  private ResponseObject convertToSale(RequestInfo reqInfo) {
    Long actId = reqInfo.getParameterLong(COL_TRADE_ACT);
    if (!DataUtils.isId(actId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_TRADE_ACT);
    }

    BeeRowSet rowSet = qs.getViewData(VIEW_TRADE_ACTS, Filter.compareId(actId));
    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject.error(reqInfo.getService(), VIEW_TRADE_ACTS, actId, "not available");
    }

    BeeRow oldRow = rowSet.getRow(0);
    BeeRow newRow = DataUtils.cloneRow(oldRow);

    newRow.setValue(rowSet.getColumnIndex(COL_TA_KIND), TradeActKind.SALE.ordinal());

    Long series = oldRow.getLong(rowSet.getColumnIndex(COL_TA_SERIES));
    int numberIndex = rowSet.getColumnIndex(COL_TA_NUMBER);

    if (DataUtils.isId(series) && oldRow.isNull(numberIndex)) {
      String number = getNextActNumber(series, rowSet.getColumn(numberIndex).getPrecision());

      if (!BeeUtils.isEmpty(number)) {
        newRow.setValue(numberIndex, number);
      }
    }

    Long operation = getDefaultOperation(TradeActKind.SALE);
    newRow.setValue(rowSet.getColumnIndex(COL_TA_OPERATION), operation);

    BeeRowSet updated = DataUtils.getUpdated(rowSet.getViewName(), rowSet.getColumns(),
        oldRow, newRow, null);

    return deb.commitRow(updated);
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

  private ResponseObject createInvoice(RequestInfo reqInfo) {
    String serialized = reqInfo.getParameter(VIEW_SALES);
    if (BeeUtils.isEmpty(serialized)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), VIEW_SALES);
    }

    BeeRowSet sales = BeeRowSet.restore(serialized);
    if (DataUtils.isEmpty(sales)) {
      return ResponseObject.error(reqInfo.getService(), sales.getViewName(), "is empty");
    }

    serialized = reqInfo.getParameter(VIEW_SALE_ITEMS);
    if (BeeUtils.isEmpty(serialized)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), VIEW_SALE_ITEMS);
    }

    BeeRowSet saleItems = BeeRowSet.restore(serialized);
    if (DataUtils.isEmpty(saleItems)) {
      return ResponseObject.error(reqInfo.getService(), saleItems.getViewName(), "is empty");
    }

    serialized = reqInfo.getParameter(VIEW_TRADE_ACT_INVOICES);
    if (BeeUtils.isEmpty(serialized)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), VIEW_TRADE_ACT_INVOICES);
    }

    BeeRowSet relations = BeeRowSet.restore(serialized);
    if (DataUtils.isEmpty(relations)) {
      return ResponseObject.error(reqInfo.getService(), relations.getViewName(), "is empty");
    }

    ResponseObject response = deb.commitRow(sales);
    if (response.hasErrors() || !response.hasResponse(BeeRow.class)) {
      return response;
    }

    long invoiceId = ((BeeRow) response.getResponse()).getId();

    int colIndex = saleItems.getColumnIndex(COL_SALE);

    for (int i = 0; i < saleItems.getNumberOfRows(); i++) {
      saleItems.setValue(i, colIndex, invoiceId);

      ResponseObject insResponse = deb.commitRow(saleItems, i, RowInfo.class);
      if (insResponse.hasErrors()) {
        response.addMessagesFrom(insResponse);
        break;
      }
    }

    colIndex = relations.getColumnIndex(COL_SALE);

    for (int i = 0; i < relations.getNumberOfRows(); i++) {
      relations.setValue(i, colIndex, invoiceId);

      ResponseObject insResponse = deb.commitRow(relations, i, RowInfo.class);
      if (insResponse.hasErrors()) {
        response.addMessagesFrom(insResponse);
        break;
      }
    }

    return response;
  }

  private long exchange(String target, String fromCol, long to, long time, String... columns) {
    long result = 0;

    SqlSelect fromQuery = new SqlSelect()
        .setDistinctMode(true)
        .addFields(target, fromCol)
        .addFrom(target)
        .setWhere(SqlUtils.and(SqlUtils.notNull(target, fromCol),
            SqlUtils.notEqual(target, fromCol, to)));

    List<Long> currencies = qs.getLongList(fromQuery);
    if (currencies.isEmpty()) {
      return result;
    }

    double toRate = adm.getRate(to, time);

    for (long from : currencies) {
      double fromRate = adm.getRate(from, time);
      if (Double.compare(toRate, fromRate) == BeeConst.COMPARE_EQUAL) {
        continue;
      }

      double rate = fromRate / toRate;

      for (String column : columns) {
        SqlUpdate update = new SqlUpdate(target)
            .addExpression(column, SqlUtils.multiply(SqlUtils.field(target, column), rate))
            .setWhere(SqlUtils.notNull(target, column));

        int count = qs.updateData(update);
        if (count > 0) {
          result += count;
        }

      }
    }

    return result;
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

  private TradeActKind getActKind(Long actId) {
    if (DataUtils.isId(actId)) {
      Integer value = qs.getInt(new SqlSelect()
          .addFields(TBL_TRADE_ACTS, COL_TA_KIND)
          .addFrom(TBL_TRADE_ACTS)
          .setWhere(sys.idEquals(TBL_TRADE_ACTS, actId)));

      return EnumUtils.getEnumByIndex(TradeActKind.class, value);

    } else {
      return null;
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

  private Long getDefaultOperation(TradeActKind kind) {
    IsCondition where = SqlUtils.equals(TBL_TRADE_OPERATIONS, COL_OPERATION_KIND, kind.ordinal());

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TRADE_OPERATIONS, sys.getIdName(TBL_TRADE_OPERATIONS))
        .addFrom(TBL_TRADE_OPERATIONS)
        .setWhere(SqlUtils.and(where,
            SqlUtils.notNull(TBL_TRADE_OPERATIONS, COL_OPERATION_DEFAULT)));

    List<Long> operations = qs.getLongList(query);
    if (operations.size() == 1) {
      return operations.get(0);
    }

    query.setWhere(where);
    operations = qs.getLongList(query);

    return (operations.size() == 1) ? operations.get(0) : null;
  }

  private String getNextActNumber(long series, int maxLength) {
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

    String number = big.add(BigInteger.ONE).toString();

    Integer length = prm.getInteger(PRM_TA_NUMBER_LENGTH);
    if (BeeUtils.isPositive(length) && length > number.length()) {
      number = BeeUtils.padLeft(number, length, BeeConst.CHAR_ZERO);
    }

    if (maxLength > 0 && number.length() > maxLength) {
      number = number.substring(number.length() - maxLength);
    }

    return number;
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

    SqlSelect rangeQuery = new SqlSelect()
        .addFields(TBL_TRADE_ACT_INVOICES, COL_TA_INVOICE_FROM, COL_TA_INVOICE_TO)
        .addFrom(TBL_TRADE_ACT_INVOICES);

    IsCondition rangeCondition = SqlUtils.and(
        SqlUtils.notNull(TBL_TRADE_ACT_INVOICES, COL_TA_INVOICE_FROM),
        SqlUtils.notNull(TBL_TRADE_ACT_INVOICES, COL_TA_INVOICE_TO));

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

      rangeQuery.setWhere(SqlUtils.and(rangeCondition,
          SqlUtils.equals(TBL_TRADE_ACT_INVOICES, COL_TRADE_ACT, actId)));

      SimpleRowSet rangeData = qs.getData(rangeQuery);
      if (!DataUtils.isEmpty(rangeData)) {
        act.setProperty(TBL_TRADE_ACT_INVOICES, rangeData.serialize());
      }
    }

    Double vatPercent = prm.getDouble(AdministrationConstants.PRM_VAT_PERCENT);
    if (BeeUtils.isPositive(vatPercent)) {
      acts.setTableProperty(AdministrationConstants.PRM_VAT_PERCENT, vatPercent.toString());
    }

    return ResponseObject.response(acts);
  }

  private ResponseObject getItemsByCompanyReport(RequestInfo reqInfo) {
    Long startDate = reqInfo.getParameterLong(Service.VAR_FROM);
    Long endDate = reqInfo.getParameterLong(Service.VAR_TO);

    Long currency = reqInfo.getParameterLong(COL_TA_CURRENCY);

    Set<Long> companies = DataUtils.parseIdSet(reqInfo.getParameter(COL_TA_COMPANY));
    Set<Long> objects = DataUtils.parseIdSet(reqInfo.getParameter(COL_TA_OBJECT));

    Set<Long> operations = DataUtils.parseIdSet(reqInfo.getParameter(COL_TA_OPERATION));
    Set<Long> statuses = DataUtils.parseIdSet(reqInfo.getParameter(COL_TA_STATUS));

    Set<Long> series = DataUtils.parseIdSet(reqInfo.getParameter(COL_TA_SERIES));
    Set<Long> managers = DataUtils.parseIdSet(reqInfo.getParameter(COL_TA_MANAGER));

    Set<Long> warehouses = DataUtils.parseIdSet(reqInfo.getParameter(COL_WAREHOUSE));

    Set<Long> categories = DataUtils.parseIdSet(reqInfo.getParameter(COL_CATEGORY));
    Set<Long> items = DataUtils.parseIdSet(reqInfo.getParameter(COL_TA_ITEM));

    List<String> groupBy = NameUtils.toList(reqInfo.getParameter(Service.VAR_GROUP_BY));

    HasConditions where = SqlUtils.and();
    where.add(SqlUtils.or(
        SqlUtils.equals(TBL_TRADE_ACTS, COL_TA_KIND, TradeActKind.SALE.ordinal()),
        SqlUtils.equals(TBL_TRADE_ACTS, COL_TA_KIND, TradeActKind.SUPPLEMENT.ordinal())));

    if (startDate != null) {
      where.add(SqlUtils.moreEqual(TBL_TRADE_ACTS, COL_TA_DATE, startDate));
    }
    if (endDate != null) {
      where.add(SqlUtils.less(TBL_TRADE_ACTS, COL_TA_DATE, endDate));
    }

    if (!companies.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_ACTS, COL_TA_COMPANY, companies));
    }
    if (!objects.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_ACTS, COL_TA_OBJECT, objects));
    }

    if (!operations.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_ACTS, COL_TA_OPERATION, operations));
    }
    if (!statuses.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_ACTS, COL_TA_STATUS, statuses));
    }

    if (!series.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_ACTS, COL_TA_SERIES, series));
    }
    if (!managers.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_ACTS, COL_TA_MANAGER, managers));
    }

    if (!warehouses.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_OPERATIONS, COL_OPERATION_WAREHOUSE_TO, warehouses));
    }

    if (!categories.isEmpty()) {
      where.add(SqlUtils.in(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM, TBL_ITEM_CATEGORIES, COL_ITEM,
          SqlUtils.inList(TBL_ITEM_CATEGORIES, COL_CATEGORY, categories)));
    }
    if (!items.isEmpty()) {
      where.add(SqlUtils.inList(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM, items));
    }

    SqlSelect returnQuery = new SqlSelect()
        .addFields(TBL_TRADE_ACTS, COL_TA_PARENT)
        .addFields(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM)
        .addSum(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY, ALS_RETURNED_QTY)
        .addFrom(TBL_TRADE_ACTS)
        .addFromInner(TBL_TRADE_ACT_ITEMS,
            sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT))
        .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_TRADE_ACTS, COL_TA_PARENT),
            SqlUtils.equals(TBL_TRADE_ACTS, COL_TA_KIND, TradeActKind.RETURN.ordinal())))
        .addGroup(TBL_TRADE_ACTS, COL_TA_PARENT)
        .addGroup(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM);

    String returnAlias = "Ret_" + SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect();

    query.addFrom(TBL_TRADE_ACTS);
    query.addFromLeft(TBL_TRADE_OPERATIONS,
        sys.joinTables(TBL_TRADE_OPERATIONS, TBL_TRADE_ACTS, COL_TA_OPERATION));
    query.addFromLeft(TBL_TRADE_ACT_ITEMS,
        sys.joinTables(TBL_TRADE_ACTS, TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT));

    query.addFromLeft(returnQuery, returnAlias, SqlUtils.and(
        SqlUtils.join(TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT, returnAlias, COL_TA_PARENT),
        SqlUtils.join(TBL_TRADE_ACT_ITEMS, COL_TA_ITEM, returnAlias, COL_TA_ITEM)));

    if (groupBy.isEmpty()) {
      query.addFromLeft(TBL_TRADE_SERIES,
          sys.joinTables(TBL_TRADE_SERIES, TBL_TRADE_ACTS, COL_TA_SERIES));
      query.addFromLeft(TBL_TRADE_STATUSES,
          sys.joinTables(TBL_TRADE_STATUSES, TBL_TRADE_ACTS, COL_TA_STATUS));

      query.addFields(TBL_TRADE_ACT_ITEMS, COL_TRADE_ACT);
      query.addFields(TBL_TRADE_ACTS, COL_TA_NAME, COL_TA_DATE);
      query.addFields(TBL_TRADE_SERIES, COL_SERIES_NAME);
      query.addFields(TBL_TRADE_ACTS, COL_TA_NUMBER);
      query.addFields(TBL_TRADE_OPERATIONS, COL_OPERATION_NAME);
      query.addFields(TBL_TRADE_STATUSES, COL_STATUS_NAME);
    }

    if (groupBy.isEmpty() || groupBy.contains(COL_TA_COMPANY)) {
      query.addFromLeft(TBL_COMPANIES,
          sys.joinTables(TBL_COMPANIES, TBL_TRADE_ACTS, COL_TA_COMPANY));

      query.addField(TBL_COMPANIES, COL_COMPANY_NAME, ALS_COMPANY_NAME);
    }

    if (groupBy.isEmpty() || groupBy.contains(COL_TA_OBJECT)) {
      query.addFromLeft(TBL_COMPANY_OBJECTS,
          sys.joinTables(TBL_COMPANY_OBJECTS, TBL_TRADE_ACTS, COL_TA_OBJECT));

      query.addFields(TBL_COMPANY_OBJECTS, COL_COMPANY_OBJECT_NAME);
    }

    if (groupBy.isEmpty() || groupBy.contains(COL_TA_MANAGER)) {
      query.addFromLeft(TBL_USERS,
          sys.joinTables(TBL_USERS, TBL_TRADE_ACTS, COL_TA_MANAGER));
      query.addFromLeft(TBL_COMPANY_PERSONS,
          sys.joinTables(TBL_COMPANY_PERSONS, TBL_USERS, COL_COMPANY_PERSON));
      query.addFromLeft(TBL_PERSONS,
          sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON));

      query.addFields(TBL_PERSONS, COL_FIRST_NAME, COL_LAST_NAME);
    }

    if (groupBy.isEmpty() || groupBy.contains(COL_WAREHOUSE)) {
      query.addFromLeft(TBL_WAREHOUSES,
          sys.joinTables(TBL_WAREHOUSES, TBL_TRADE_OPERATIONS, COL_OPERATION_WAREHOUSE_TO));

      query.addField(TBL_WAREHOUSES, COL_WAREHOUSE_CODE, ALS_WAREHOUSE_CODE);
    }

    if (groupBy.isEmpty() || groupBy.contains(COL_TA_ITEM)
        || groupBy.contains(COL_ITEM_TYPE) || groupBy.contains(COL_ITEM_GROUP)) {

      query.addFromLeft(TBL_ITEMS,
          sys.joinTables(TBL_ITEMS, TBL_TRADE_ACT_ITEMS, COL_TA_ITEM));
    }

    if (groupBy.contains(COL_ITEM_TYPE)) {
      query.addFromLeft(TBL_ITEM_CATEGORY_TREE, ALS_ITEM_TYPES,
          sys.joinTables(TBL_ITEM_CATEGORY_TREE, ALS_ITEM_TYPES, TBL_ITEMS, COL_ITEM_TYPE));

      query.addField(ALS_ITEM_TYPES, COL_CATEGORY_NAME, ALS_ITEM_TYPE_NAME);
    }

    if (groupBy.contains(COL_ITEM_GROUP)) {
      query.addFromLeft(TBL_ITEM_CATEGORY_TREE, ALS_ITEM_GROUPS,
          sys.joinTables(TBL_ITEM_CATEGORY_TREE, ALS_ITEM_GROUPS, TBL_ITEMS, COL_ITEM_GROUP));

      query.addField(ALS_ITEM_GROUPS, COL_CATEGORY_NAME, ALS_ITEM_GROUP_NAME);
    }

    if (groupBy.isEmpty() || groupBy.contains(COL_TA_ITEM)) {
      query.addFromLeft(TBL_UNITS,
          sys.joinTables(TBL_UNITS, TBL_ITEMS, COL_UNIT));

      query.addField(TBL_ITEMS, COL_ITEM_NAME, ALS_ITEM_NAME);
      query.addFields(TBL_ITEMS, COL_ITEM_ARTICLE);
      query.addField(TBL_UNITS, COL_UNIT_NAME, ALS_UNIT_NAME);
    }

    query.addFields(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY);
    query.addFields(returnAlias, ALS_RETURNED_QTY);
    query.addField(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY, ALS_REMAINING_QTY);

    query.addFields(TBL_TRADE_ACT_ITEMS, COL_TRADE_ITEM_PRICE);
    query.addFields(TBL_TRADE_ACTS, COL_TA_CURRENCY);

    query.addFields(TBL_TRADE_ACT_ITEMS, COL_TRADE_DISCOUNT);

    int amountPrecision = 15;
    int amountScale = 2;

    query.addEmptyNumeric(ALS_BASE_AMOUNT, amountPrecision, amountScale);
    query.addEmptyNumeric(ALS_DISCOUNT_AMOUNT, amountPrecision, amountScale);
    query.addEmptyNumeric(ALS_TOTAL_AMOUNT, amountPrecision, amountScale);

    String itemIdName = sys.getIdName(TBL_TRADE_ACT_ITEMS);
    if (groupBy.isEmpty()) {
      query.addFields(TBL_TRADE_ACT_ITEMS, itemIdName);
    }

    query.setWhere(where);

    String tmp = qs.sqlCreateTemp(query);

    SqlUpdate update = new SqlUpdate(tmp)
        .addExpression(ALS_REMAINING_QTY,
            SqlUtils.minus(SqlUtils.field(tmp, COL_TRADE_ITEM_QUANTITY),
                SqlUtils.field(tmp, ALS_RETURNED_QTY)))
        .setWhere(SqlUtils.positive(tmp, ALS_RETURNED_QTY));

    qs.updateData(update);

    IsCondition condition = SqlUtils.positive(tmp, ALS_REMAINING_QTY);
    if (!qs.sqlExists(tmp, condition)) {
      return ResponseObject.emptyResponse();
    }

    if (!DataUtils.isId(currency)) {
      currency = prm.getRelation(PRM_CURRENCY);
    }
    if (DataUtils.isId(currency)) {
      exchange(tmp, COL_TA_CURRENCY, currency, System.currentTimeMillis(), COL_TRADE_ITEM_PRICE);
    }

    update = new SqlUpdate(tmp)
        .addExpression(ALS_BASE_AMOUNT,
            SqlUtils.multiply(SqlUtils.field(tmp, ALS_REMAINING_QTY),
                SqlUtils.field(tmp, COL_TRADE_ITEM_PRICE)))
        .setWhere(SqlUtils.and(SqlUtils.positive(tmp, ALS_REMAINING_QTY),
            SqlUtils.positive(tmp, COL_TRADE_ITEM_PRICE)));

    qs.updateData(update);

    update = new SqlUpdate(tmp)
        .addExpression(ALS_DISCOUNT_AMOUNT,
            SqlUtils.divide(SqlUtils.multiply(SqlUtils.field(tmp, ALS_BASE_AMOUNT),
                SqlUtils.field(tmp, COL_TRADE_DISCOUNT)), 100))
        .setWhere(SqlUtils.and(SqlUtils.positive(tmp, ALS_BASE_AMOUNT),
            SqlUtils.notNull(tmp, COL_TRADE_DISCOUNT)));

    qs.updateData(update);

    update = new SqlUpdate(tmp)
        .addExpression(ALS_TOTAL_AMOUNT,
            SqlUtils.minus(SqlUtils.field(tmp, ALS_BASE_AMOUNT),
                SqlUtils.field(tmp, ALS_DISCOUNT_AMOUNT)))
        .setWhere(SqlUtils.notNull(tmp, ALS_DISCOUNT_AMOUNT));

    qs.updateData(update);

    update = new SqlUpdate(tmp)
        .addExpression(ALS_TOTAL_AMOUNT, SqlUtils.field(tmp, ALS_BASE_AMOUNT))
        .setWhere(SqlUtils.isNull(tmp, ALS_DISCOUNT_AMOUNT));

    qs.updateData(update);

    query = new SqlSelect();

    if (groupBy.isEmpty()) {
      query.addFields(tmp, COL_TRADE_ACT, COL_TA_NAME, COL_TA_DATE,
          COL_SERIES_NAME, COL_TA_NUMBER, COL_OPERATION_NAME,
          ALS_COMPANY_NAME, COL_COMPANY_OBJECT_NAME,
          itemIdName, ALS_ITEM_NAME, COL_ITEM_ARTICLE,
          COL_TRADE_ITEM_QUANTITY, ALS_UNIT_NAME, ALS_RETURNED_QTY, ALS_REMAINING_QTY,
          COL_TRADE_ITEM_PRICE, ALS_BASE_AMOUNT, COL_TRADE_DISCOUNT, ALS_DISCOUNT_AMOUNT,
          ALS_TOTAL_AMOUNT);

      query.addOrder(tmp, COL_TA_DATE, COL_TRADE_ACT, itemIdName);

    } else {
      for (String group : groupBy) {
        List<String> fields = new ArrayList<>();
        List<String> order = new ArrayList<>();

        switch (group) {
          case COL_ITEM_TYPE:
            fields.add(ALS_ITEM_TYPE_NAME);
            break;

          case COL_ITEM_GROUP:
            fields.add(ALS_ITEM_GROUP_NAME);
            break;

          case COL_TA_ITEM:
            fields.add(ALS_ITEM_NAME);
            fields.add(COL_ITEM_ARTICLE);
            fields.add(ALS_UNIT_NAME);
            break;

          case COL_TA_COMPANY:
            fields.add(ALS_COMPANY_NAME);
            break;

          case COL_TA_OBJECT:
            fields.add(COL_COMPANY_OBJECT_NAME);
            break;

          case COL_TA_MANAGER:
            fields.add(COL_FIRST_NAME);
            fields.add(COL_LAST_NAME);

            order.add(COL_LAST_NAME);
            order.add(COL_FIRST_NAME);
            break;

          case COL_WAREHOUSE:
            fields.add(ALS_WAREHOUSE_CODE);
            break;
        }

        for (String field : fields) {
          query.addFields(tmp, field);
        }

        if (order.isEmpty()) {
          order.addAll(fields);
        }
        for (String field : order) {
          query.addGroup(tmp, field);
          query.addOrder(tmp, field);
        }
      }

      query.addSum(tmp, COL_TRADE_ITEM_QUANTITY);
      query.addSum(tmp, ALS_RETURNED_QTY);
      query.addSum(tmp, ALS_REMAINING_QTY);
      query.addSum(tmp, ALS_BASE_AMOUNT);
      query.addSum(tmp, ALS_DISCOUNT_AMOUNT);
      query.addSum(tmp, ALS_TOTAL_AMOUNT);
    }

    query.addFrom(tmp);
    query.setWhere(condition);

    SimpleRowSet data = qs.getData(query);

    if (DataUtils.isEmpty(data)) {
      return ResponseObject.emptyResponse();

    } else {
      if (groupBy.isEmpty()) {
        data.removeColumn(itemIdName);
      }

      return ResponseObject.response(data);
    }
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
