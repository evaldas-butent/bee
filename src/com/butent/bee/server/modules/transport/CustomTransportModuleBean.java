package com.butent.bee.server.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.DataEvent;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.modules.trade.TradeModuleBean;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

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
}