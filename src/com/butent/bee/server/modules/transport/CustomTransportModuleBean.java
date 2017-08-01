package com.butent.bee.server.modules.transport;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
@LocalBean
public class CustomTransportModuleBean implements DataEventHandler {

  @EJB SystemBean sys;
  @EJB QueryServiceBean qs;
  @EJB ParamHolderBean prm;
  @EJB UserServiceBean usr;

  @Subscribe
  @AllowConcurrentEvents
  public void calcInvoiceVat(DataEvent.ViewQueryEvent event) {
    if (event.isAfter(VIEW_CARGO_INVOICES, VIEW_CARGO_PURCHASE_INVOICES,
        VIEW_SELF_SERVICE_INVOICES) && event.hasData()) {
      String tbl;
      String fld;

      if (event.isTarget(VIEW_CARGO_PURCHASE_INVOICES)) {
        tbl = TBL_PURCHASE_ITEMS;
        fld = COL_PURCHASE;
      } else {
        tbl = TBL_SALE_ITEMS;
        fld = COL_SALE;
      }
      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(tbl, fld)
          .addSum(TradeModuleBean.getVatExpression(tbl), COL_TRADE_VAT)
          .addFrom(tbl)
          .setWhere(SqlUtils.inList(tbl, fld, event.getRowset().getRowIds()))
          .addGroup(tbl, fld));

      for (BeeRow row : event.getRowset().getRows()) {
        row.setProperty(COL_TRADE_VAT,
            rs.getValueByKey(fld, BeeUtils.toString(row.getId()), COL_TRADE_VAT));
      }
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void convertToMainCurrency(DataEvent.ViewQueryEvent event) {
    if (event.isAfter(VIEW_SELF_SERVICE_INVOICES)) {
      BeeRowSet rowSet = event.getRowset();
      Long mainCurrency = prm.getRelation(PRM_CURRENCY);
      Map<Long, Double> amountMap = new HashMap<>();
      Map<Long, Double> paidMap = new HashMap<>();

      if (rowSet.isEmpty() || !DataUtils.isId(mainCurrency)) {
        return;
      }

      SqlSelect query = new SqlSelect()
          .addField(TBL_SALES, sys.getIdName(TBL_SALES), COL_SALE)
          .addFrom(TBL_SALES)
          .setWhere(sys.idInList(TBL_SALES, rowSet.getRowIds()));

      IsExpression amountExch = ExchangeUtils.exchangeFieldTo(query, SqlUtils.field(TBL_SALES,
          COL_AMOUNT), SqlUtils.field(TBL_SALES, COL_TRADE_CURRENCY), SqlUtils.field(TBL_SALES,
          COL_DATE), SqlUtils.constant(mainCurrency));
      query.addExpr(amountExch, COL_AMOUNT);

      IsExpression paidExch = ExchangeUtils.exchangeFieldTo(query, SqlUtils.field(TBL_SALES,
          COL_TRADE_PAID), SqlUtils.field(TBL_SALES, COL_TRADE_CURRENCY),
          SqlUtils.field(TBL_SALES, COL_DATE), SqlUtils.constant(mainCurrency));
      query.addExpr(paidExch, COL_TRADE_PAID);

      SimpleRowSet set = qs.getData(query);
      for (SimpleRowSet.SimpleRow row : set) {
        amountMap.put(row.getLong(COL_SALE), row.getDouble(COL_AMOUNT));
        if (BeeUtils.isPositive(row.getDouble(COL_TRADE_PAID))) {
          paidMap.put(row.getLong(COL_SALE), row.getDouble(COL_TRADE_PAID));
        }
      }

      for (BeeRow row : rowSet) {
        row.setProperty(PROP_AMOUNT_IN_EUR, amountMap.get(row.getId()));
        if (paidMap.containsKey(row.getId())) {
          row.setProperty(PROP_PAID_IN_EUR, paidMap.get(row.getId()));
        }
      }
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void convertVATToMainCurrency(DataEvent.ViewQueryEvent event) {
    if (event.isAfter(VIEW_SELF_SERVICE_INVOICES)) {
      BeeRowSet rowSet = event.getRowset();
      Long mainCurrency = prm.getRelation(PRM_CURRENCY);

      if (rowSet.isEmpty() || !DataUtils.isId(mainCurrency)) {
        return;
      }

      SqlSelect query = new SqlSelect()
          .addFields(TBL_SALE_ITEMS, COL_SALE)
          .addFrom(TBL_SALE_ITEMS)
          .addFromInner(TBL_SALES,
              sys.joinTables(TBL_SALES, TBL_SALE_ITEMS, COL_SALE))
          .setWhere(SqlUtils.inList(TBL_SALE_ITEMS, COL_SALE, rowSet.getRowIds()))
          .addGroup(TBL_SALE_ITEMS, COL_SALE);

      IsExpression convertedVat = ExchangeUtils.exchangeFieldTo(query,
          TradeModuleBean.getVatExpression(TBL_SALE_ITEMS), SqlUtils.field(TBL_SALES,
              COL_CURRENCY), SqlUtils.field(TBL_SALES, COL_DATE),
          SqlUtils.constant(mainCurrency));

      query.addSum(convertedVat, PROP_VAT_IN_EUR);

      SimpleRowSet data = qs.getData(query);

      for (BeeRow row : rowSet) {
        row.setProperty(PROP_VAT_IN_EUR, data.getValueByKey(COL_SALE,
            BeeUtils.toString(row.getId()), PROP_VAT_IN_EUR));
      }
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void fillNewNumbers(DataEvent.ViewQueryEvent event) {
    if (event.isAfter(VIEW_CARGO_PURCHASE_INVOICES) && event.hasData()) {
      BeeRowSet rowSet = event.getRowset();

      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(TBL_PURCHASE_ITEMS, COL_PURCHASE, COL_NUMBER)
          .addFrom(TBL_PURCHASE_ITEMS)
          .setWhere(SqlUtils.inList(TBL_PURCHASE_ITEMS, COL_PURCHASE, rowSet.getRowIds())));

      for (BeeRow row : rowSet.getRows()) {
        row.setProperty("NewNumber", rs.getValueByKey(COL_PURCHASE, BeeUtils.toString(row.getId()),
            COL_NUMBER));
      }
    }
  }

  public ResponseObject getAssessmentAmounts(String viewName, Filter filter) {
    Map<String, Double> result = new HashMap<>();
    BeeView view = sys.getView(viewName);
    Assert.state(BeeUtils.same(view.getSourceName(), TBL_ASSESSMENTS));

    SqlSelect select = view.getQuery(usr.getCurrentUserId(), filter)
        .resetFields().resetOrder()
        .addFields(view.getSourceAlias(), view.getSourceIdName());

    for (String tbl : new String[] {TBL_CARGO_INCOMES, TBL_CARGO_EXPENSES}) {
      SqlSelect query = new SqlSelect()
          .addFrom(TBL_ASSESSMENTS)
          .addFromInner(TBL_ORDER_CARGO,
              sys.joinTables(TBL_ORDER_CARGO, TBL_ASSESSMENTS, COL_CARGO))
          .addFromInner(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
          .addFromInner(tbl, SqlUtils.joinUsing(TBL_ASSESSMENTS, tbl, COL_CARGO))
          .setWhere(SqlUtils.in(TBL_ASSESSMENTS, sys.getIdName(TBL_ASSESSMENTS), select));

      IsExpression xpr = ExchangeUtils.exchangeField(query,
          TradeModuleBean.getWithoutVatExpression(tbl, SqlUtils.field(tbl, COL_AMOUNT)),
          SqlUtils.field(tbl, COL_CURRENCY), SqlUtils.nvl(SqlUtils.field(tbl, COL_DATE),
              SqlUtils.field(TBL_ORDERS, COL_DATE)));

      result.put(tbl, qs.getDouble(query.addSum(xpr, VAR_TOTAL)));
    }
    Dictionary loc = usr.getDictionary();

    return ResponseObject.info(BeeUtils.joinWords(loc.incomes(),
        BeeUtils.round(BeeUtils.unbox(result.get(TBL_CARGO_INCOMES)), 2)),
        BeeUtils.joinWords(loc.expenses(),
            BeeUtils.round(BeeUtils.unbox(result.get(TBL_CARGO_EXPENSES)), 2)),
        BeeUtils.joinWords(loc.profit(),
            BeeUtils.round(BeeUtils.round(BeeUtils.unbox(result.get(TBL_CARGO_INCOMES)), 2)
                - BeeUtils.round(BeeUtils.unbox(result.get(TBL_CARGO_EXPENSES)), 2), 2)));
  }

  @Subscribe
  @AllowConcurrentEvents
  public void maybeInsertAssessmentObservers(DataEvent.ViewInsertEvent event) {
    String tbl = sys.getViewSource(event.getTargetName());

    if (BeeUtils.same(tbl, TBL_ASSESSMENTS) && event.isAfter()) {
      Long responsibility = prm.getRelation(PRM_SALES_RESPONSIBILITY);
      Long target = event.getRow().getId();
      int customerIdx = DataUtils.getColumnIndex(COL_CUSTOMER, event.getColumns());
      Long customer;

      if (BeeConst.isUndef(customerIdx)) {
        customer = qs.getLong(new SqlSelect()
            .addFields(TBL_ORDERS, COL_CUSTOMER)
            .addFrom(TBL_ASSESSMENTS)
            .addFromLeft(TBL_ORDER_CARGO, sys.joinTables(TBL_ORDER_CARGO, TBL_ASSESSMENTS,
                COL_CARGO))
            .addFromLeft(TBL_ORDERS, sys.joinTables(TBL_ORDERS, TBL_ORDER_CARGO, COL_ORDER))
            .setWhere(sys.idEquals(TBL_ASSESSMENTS, target)));
      } else {
        customer = event.getRow().getLong(customerIdx);
      }

      if (DataUtils.isId(customer) && DataUtils.isId(responsibility)) {
        SqlSelect selectUsers = new SqlSelect()
            .addFields(TBL_COMPANY_USERS, COL_USER)
            .addFrom(TBL_COMPANY_USERS)
            .setWhere(SqlUtils.and(SqlUtils.equals(TBL_COMPANY_USERS,
                COL_COMPANY_USER_RESPONSIBILITY, responsibility, COL_COMPANY, customer)));

        SqlSelect selectObservers = new SqlSelect()
            .addFields(TBL_ASSESSMENT_OBSERVERS, COL_USER)
            .addFrom(TBL_ASSESSMENT_OBSERVERS)
            .setWhere(SqlUtils.equals(TBL_ASSESSMENT_OBSERVERS, COL_ASSESSMENT, target));

        Set<Long> observers = qs.getLongSet(selectObservers);
        Set<Long> users = qs.getLongSet(selectUsers);

        users.removeAll(observers);

        if (!BeeUtils.isEmpty(users)) {
          for (Long user : users) {
            SqlInsert insert = new SqlInsert(TBL_ASSESSMENT_OBSERVERS)
                .addFields(COL_ASSESSMENT, COL_USER);
            insert.addValues(target, user);

            qs.insertData(insert);
          }
        }
      }
    }
  }
}