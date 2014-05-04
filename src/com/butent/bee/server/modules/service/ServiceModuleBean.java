package com.butent.bee.server.modules.service;

import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.modules.administration.ExtensionIcons;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class ServiceModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(ServiceModuleBean.class);

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
    ResponseObject response;

    if (BeeUtils.same(svc, SVC_CREATE_INVOICE_ITEMS)) {
      response = createInvoiceItems(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CALENDAR_DATA)) {
      response = getCalendarData(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("service not recognized:", svc);
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
  public Module getModule() {
    return Module.SERVICE;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void setRowProperties(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (BeeUtils.same(event.getTargetName(), VIEW_SERVICE_FILES)) {
          ExtensionIcons.setIcons(event.getRowset(), AdministrationConstants.ALS_FILE_NAME,
              AdministrationConstants.PROP_ICON);

        } else if (BeeUtils.same(event.getTargetName(), VIEW_SERVICE_OBJECTS)
            && !DataUtils.isEmpty(event.getRowset())) {

          BeeRowSet rowSet = event.getRowset();
          List<Long> rowIds = rowSet.getRowIds();

          BeeView view = sys.getView(VIEW_SERVICE_OBJECT_CRITERIA);
          SqlSelect query = view.getQuery();

          query.setWhere(SqlUtils.and(query.getWhere(),
              SqlUtils.isNull(view.getSourceAlias(), COL_SERVICE_CRITERIA_GROUP_NAME),
              SqlUtils.inList(view.getSourceAlias(), COL_SERVICE_OBJECT, rowIds)));

          SimpleRowSet criteria = qs.getData(query);

          if (!DataUtils.isEmpty(criteria)) {
            for (SimpleRow row : criteria) {
              BeeRow r = rowSet.getRowById(row.getLong(COL_SERVICE_OBJECT));

              if (r != null) {
                r.setProperty(COL_SERVICE_CRITERION_NAME
                    + row.getValue(COL_SERVICE_CRITERION_NAME),
                    row.getValue(COL_SERVICE_CRITERION_VALUE));
              }
            }
          }
        }
      }
    });
  }

  private ResponseObject createInvoiceItems(RequestInfo reqInfo) {
    Long invId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_MAINTENANCE_INVOICE));
    if (!DataUtils.isId(invId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_MAINTENANCE_INVOICE);
    }

    Long currency =
        BeeUtils.toLongOrNull(reqInfo.getParameter(AdministrationConstants.COL_CURRENCY));
    if (!DataUtils.isId(currency)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(),
          AdministrationConstants.COL_CURRENCY);
    }

    Set<Long> ids = DataUtils.parseIdSet(reqInfo.getParameter(VIEW_MAINTENANCE));
    if (ids.isEmpty()) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), VIEW_MAINTENANCE);
    }

    Long mainItem = BeeUtils.toLongOrNull(reqInfo.getParameter(PROP_MAIN_ITEM));

    IsCondition where = sys.idInList(TBL_MAINTENANCE, ids);

    SqlSelect query = new SqlSelect()
        .addFields(TBL_MAINTENANCE, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC)
        .addFrom(TBL_MAINTENANCE)
        .setWhere(where);

    IsExpression vatExch = ExchangeUtils.exchangeFieldTo(query,
        TBL_MAINTENANCE, COL_TRADE_VAT, AdministrationConstants.COL_CURRENCY,
        COL_MAINTENANCE_DATE, currency);

    String vatAlias = "Vat_" + SqlUtils.uniqueName();

    String priceAlias;
    String amountAlias;

    if (DataUtils.isId(mainItem) && ids.size() > 1) {
      IsExpression amountExch = ExchangeUtils.exchangeFieldTo(query,
          TradeModuleBean.getTotalExpression(TBL_MAINTENANCE),
          SqlUtils.field(TBL_MAINTENANCE, AdministrationConstants.COL_CURRENCY),
          SqlUtils.field(TBL_MAINTENANCE, COL_MAINTENANCE_DATE),
          SqlUtils.constant(currency));

      priceAlias = null;
      amountAlias = "Amount_" + SqlUtils.uniqueName();

      query.addSum(TBL_MAINTENANCE, COL_TRADE_ITEM_QUANTITY)
          .addSum(amountExch, amountAlias)
          .addSum(vatExch, vatAlias)
          .addGroup(TBL_MAINTENANCE, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC);

    } else {
      IsExpression priceExch = ExchangeUtils.exchangeFieldTo(query,
          TBL_MAINTENANCE, COL_TRADE_ITEM_PRICE, AdministrationConstants.COL_CURRENCY,
          COL_MAINTENANCE_DATE, currency);

      priceAlias = "Price_" + SqlUtils.uniqueName();
      amountAlias = null;

      query.addFields(TBL_MAINTENANCE, COL_MAINTENANCE_ITEM, COL_TRADE_ITEM_QUANTITY,
          COL_MAINTENANCE_NOTES)
          .addExpr(priceExch, priceAlias)
          .addExpr(vatExch, vatAlias)
          .addOrder(TBL_MAINTENANCE, sys.getIdName(TBL_MAINTENANCE));
    }

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return ResponseObject.error(TBL_MAINTENANCE, ids, "not found");
    }

    ResponseObject response = new ResponseObject();

    for (SimpleRow row : data) {
      Long item = DataUtils.isId(mainItem) ? mainItem : row.getLong(COL_MAINTENANCE_ITEM);

      SqlInsert insert = new SqlInsert(TBL_SALE_ITEMS)
          .addConstant(COL_SALE, invId)
          .addConstant(ClassifierConstants.COL_ITEM, item);

      Boolean vatPerc = row.getBoolean(COL_TRADE_VAT_PERC);
      Double vat;

      if (BeeUtils.isTrue(vatPerc)) {
        insert.addConstant(COL_TRADE_VAT_PERC, vatPerc);
        vat = row.getDouble(COL_TRADE_VAT);
      } else {
        vat = row.getDouble(vatAlias);
      }

      if (BeeUtils.nonZero(vat)) {
        insert.addConstant(COL_TRADE_VAT, vat);
      }

      Boolean vatPlus = row.getBoolean(COL_TRADE_VAT_PLUS);
      if (BeeUtils.isTrue(vatPlus)) {
        insert.addConstant(COL_TRADE_VAT_PLUS, vatPlus);
      }

      Double quantity = row.getDouble(COL_TRADE_ITEM_QUANTITY);
      Double price;

      if (BeeUtils.isEmpty(amountAlias)) {
        price = row.getDouble(priceAlias);

      } else {
        Double amount = row.getDouble(amountAlias);
        if (BeeUtils.isTrue(vatPlus) && BeeUtils.nonZero(vat)) {
          if (BeeUtils.isTrue(vatPerc)) {
            if (BeeUtils.nonZero(amount)) {
              amount = amount * 100d / (100d + vat);
            }
          } else {
            amount = BeeUtils.unbox(amount) - vat;
          }
        }

        if (BeeUtils.isPositive(amount) && BeeUtils.isPositive(quantity)) {
          price = amount / quantity;
        } else {
          quantity = BeeConst.DOUBLE_ONE;
          price = amount;
        }
      }

      insert.addConstant(COL_TRADE_ITEM_QUANTITY, BeeUtils.unbox(quantity));
      if (price != null) {
        insert.addConstant(COL_TRADE_ITEM_PRICE, price);
      }

      if (data.hasColumn(COL_MAINTENANCE_NOTES)) {
        String notes = row.getValue(COL_MAINTENANCE_NOTES);
        if (!BeeUtils.isEmpty(notes)) {
          insert.addConstant(COL_TRADE_ITEM_NOTE, notes);
        }
      }

      ResponseObject insResponse = qs.insertDataWithResponse(insert);
      if (insResponse.hasErrors()) {
        response.addMessagesFrom(insResponse);
        break;
      }
    }

    if (!response.hasErrors()) {
      SqlUpdate update = new SqlUpdate(TBL_MAINTENANCE)
          .addConstant(COL_MAINTENANCE_INVOICE, invId)
          .setWhere(where);

      ResponseObject updResponse = qs.updateDataWithResponse(update);
      if (updResponse.hasErrors()) {
        response.addMessagesFrom(updResponse);
      }
    }

    return response;
  }

  private ResponseObject getCalendarData(RequestInfo reqInfo) {
    BeeRowSet settings = getSettings();
    if (DataUtils.isEmpty(settings)) {
      return ResponseObject.error(reqInfo.getService(), "user settings not available");
    }

    String idName = sys.getIdName(TBL_SERVICE_OBJECTS);

    SqlSelect objectQuery = new SqlSelect()
        .addFields(TBL_SERVICE_OBJECTS, idName, COL_SERVICE_OBJECT_CATEGORY,
            COL_SERVICE_OBJECT_CUSTOMER, COL_SERVICE_OBJECT_ADDRESS)
        .addField(TBL_SERVICE_TREE, COL_SERVICE_CATEGORY_NAME, ALS_SERVICE_CATEGORY_NAME)
        .addField(ClassifierConstants.TBL_COMPANIES,
            ClassifierConstants.COL_COMPANY_NAME, ALS_SERVICE_CUSTOMER_NAME)
        .addFrom(TBL_SERVICE_OBJECTS)
        .addFromLeft(TBL_SERVICE_TREE, sys.joinTables(TBL_SERVICE_TREE,
            TBL_SERVICE_OBJECTS, COL_SERVICE_OBJECT_CATEGORY))
        .addFromLeft(ClassifierConstants.TBL_COMPANIES,
            sys.joinTables(ClassifierConstants.TBL_COMPANIES,
                TBL_SERVICE_OBJECTS, COL_SERVICE_OBJECT_CUSTOMER))
        .setWhere(SqlUtils.in(TBL_SERVICE_OBJECTS, idName,
            TBL_RELATIONS, COL_SERVICE_OBJECT,
            SqlUtils.or(SqlUtils.notNull(TBL_RELATIONS, TaskConstants.COL_TASK),
                SqlUtils.notNull(TBL_RELATIONS, TaskConstants.COL_RECURRING_TASK))))
        .addOrder(ClassifierConstants.TBL_COMPANIES, ClassifierConstants.COL_COMPANY_NAME)
        .addOrder(TBL_SERVICE_OBJECTS, COL_SERVICE_OBJECT_ADDRESS, idName);

    SimpleRowSet objectData = qs.getData(objectQuery);
    if (DataUtils.isEmpty(objectData)) {
      return ResponseObject.response(settings);
    }

    settings.setTableProperty(TBL_SERVICE_OBJECTS, objectData.serialize());

    idName = sys.getIdName(TaskConstants.TBL_TASKS);

    SqlSelect taskQuery = new SqlSelect()
        .addAllFields(TaskConstants.TBL_TASKS)
        .addFrom(TaskConstants.TBL_TASKS)
        .setWhere(SqlUtils.in(TaskConstants.TBL_TASKS, idName,
            TBL_RELATIONS, TaskConstants.COL_TASK,
            SqlUtils.notNull(TBL_RELATIONS, COL_SERVICE_OBJECT)))
        .addOrder(TaskConstants.TBL_TASKS, TaskConstants.COL_FINISH_TIME, idName);

    SimpleRowSet taskData = qs.getData(taskQuery);
    if (!DataUtils.isEmpty(taskData)) {
      settings.setTableProperty(TaskConstants.TBL_TASKS, taskData.serialize());
    }

    idName = sys.getIdName(TaskConstants.TBL_RECURRING_TASKS);

    SqlSelect rtQuery = new SqlSelect()
        .addAllFields(TaskConstants.TBL_RECURRING_TASKS)
        .addFrom(TaskConstants.TBL_RECURRING_TASKS)
        .setWhere(SqlUtils.in(TaskConstants.TBL_RECURRING_TASKS, idName,
            TBL_RELATIONS, TaskConstants.COL_RECURRING_TASK,
            SqlUtils.notNull(TBL_RELATIONS, COL_SERVICE_OBJECT)))
        .addOrder(TaskConstants.TBL_RECURRING_TASKS, TaskConstants.COL_RT_SCHEDULE_FROM, idName);

    SimpleRowSet rtData = qs.getData(rtQuery);
    if (!DataUtils.isEmpty(rtData)) {
      settings.setTableProperty(TaskConstants.TBL_RECURRING_TASKS, rtData.serialize());
    }

    SqlSelect relationQuery = new SqlSelect()
        .addFields(TBL_RELATIONS, COL_SERVICE_OBJECT, TaskConstants.COL_TASK,
            TaskConstants.COL_RECURRING_TASK)
        .addFrom(TBL_RELATIONS)
        .setWhere(SqlUtils.and(SqlUtils.notNull(TBL_RELATIONS, COL_SERVICE_OBJECT),
            SqlUtils.or(SqlUtils.notNull(TBL_RELATIONS, TaskConstants.COL_TASK),
                SqlUtils.notNull(TBL_RELATIONS, TaskConstants.COL_RECURRING_TASK))));

    SimpleRowSet relationData = qs.getData(relationQuery);
    if (!DataUtils.isEmpty(relationData)) {
      settings.setTableProperty(TBL_RELATIONS, relationData.serialize());
    }
    
    return ResponseObject.response(settings);
  }

  private BeeRowSet getSettings() {
    long userId = usr.getCurrentUserId();
    Filter filter = Filter.equals(COL_USER, userId);

    BeeRowSet rowSet = qs.getViewData(VIEW_SERVICE_SETTINGS, filter);
    if (!DataUtils.isEmpty(rowSet)) {
      return rowSet;
    }

    SqlInsert sqlInsert = new SqlInsert(TBL_SERVICE_SETTINGS).addConstant(COL_USER, userId);

    ResponseObject response = qs.insertDataWithResponse(sqlInsert);
    if (response.hasErrors()) {
      return null;
    } else {
      return qs.getViewData(VIEW_SERVICE_SETTINGS, filter);
    }
  }
}
