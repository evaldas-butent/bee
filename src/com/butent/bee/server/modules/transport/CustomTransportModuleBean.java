package com.butent.bee.server.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.DataEvent;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class CustomTransportModuleBean {

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  ParamHolderBean prm;

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