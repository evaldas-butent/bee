package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.modules.trade.InvoiceBuilder;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CargoPurchasesGrid extends InvoiceBuilder {

  @Override
  protected void createInvoice(final BeeRowSet data, final BiConsumer<BeeRowSet, BeeRow> consumer) {
    final DataInfo targetInfo = Data.getDataInfo(getTargetView());
    final BeeRow newRow = RowFactory.createEmptyRow(targetInfo, true);

    Set<String> orders = new TreeSet<>();
    Map<Long, String> suppliers = new HashMap<>();
    Map<Long, String> currencies = new HashMap<>();
    String number = null;

    DataInfo info = Data.getDataInfo(getViewName());
    int order = info.getColumnIndex(COL_ASSESSMENT);
    int suplId = info.getColumnIndex(COL_TRADE_SUPPLIER);
    int suplName = info.getColumnIndex(COL_TRADE_SUPPLIER + "Name");
    int currId = info.getColumnIndex(COL_CURRENCY);
    int currName = info.getColumnIndex(ALS_CURRENCY_NAME);
    int numberIdx = info.getColumnIndex(COL_NUMBER);

    for (BeeRow row : data.getRows()) {
      orders.add(row.getString(order));

      Long id = row.getLong(suplId);
      if (DataUtils.isId(id)) {
        suppliers.put(id, row.getString(suplName));
      }
      id = row.getLong(currId);
      if (DataUtils.isId(id)) {
        currencies.put(id, row.getString(currName));
      }
      if (number != null && !BeeUtils.same(row.getString(numberIdx), number)) {
        number = "";
      } else {
        number = BeeUtils.nvl(row.getString(numberIdx), "");
      }
    }
    newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_NUMBER), BeeUtils.joinItems(orders));
    newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_MANAGER), BeeKeeper.getUser().getUserId());
    newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_MANAGER + COL_PERSON),
        BeeKeeper.getUser().getUserData().getCompanyPerson());
    newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_MANAGER + COL_FIRST_NAME),
        BeeKeeper.getUser().getFirstName());
    newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_MANAGER + COL_LAST_NAME),
        BeeKeeper.getUser().getLastName());

    if (suppliers.size() == 1) {
      Map.Entry<Long, String> entry = BeeUtils.peek(suppliers.entrySet());
      newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_SUPPLIER), entry.getKey());
      newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_SUPPLIER + "Name"), entry.getValue());
    }
    if (currencies.size() == 1) {
      Map.Entry<Long, String> entry = BeeUtils.peek(currencies.entrySet());
      newRow.setValue(targetInfo.getColumnIndex(COL_CURRENCY), entry.getKey());
      newRow.setValue(targetInfo.getColumnIndex(ALS_CURRENCY_NAME), entry.getValue());
    }
    if (!BeeUtils.isEmpty(number)) {
      number = BeeUtils.trim(number);
      String prefix = null;
      int sep = number.indexOf(BeeConst.CHAR_SPACE);

      if (BeeUtils.isNonNegative(sep)) {
        prefix = number.substring(0, sep);
        number = number.substring(sep + 1);
      }
      newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_INVOICE_PREFIX), prefix);
      newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_INVOICE_NO), number);
    }
    int sale = info.getColumnIndex(COL_SALE);
    String operation = null;

    for (BeeRow row : data.getRows()) {
      String op = DataUtils.isId(row.getLong(sale)) ? PRM_ACCUMULATION_OPERATION
          : PRM_PURCHASE_OPERATION;

      if (BeeUtils.isEmpty(operation)) {
        operation = op;
      } else if (!BeeUtils.same(operation, op)) {
        operation = null;
        break;
      }
    }
    if (BeeUtils.isEmpty(operation)) {
      consumer.accept(data, newRow);
      return;
    }
    Global.getRelationParameter(operation, new BiConsumer<Long, String>() {
      @Override
      public void accept(Long opId, String op) {
        if (DataUtils.isId(opId)) {
          newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_OPERATION), opId);
          newRow.setValue(targetInfo.getColumnIndex(COL_OPERATION_NAME), op);

          Queries.getRow(TBL_TRADE_OPERATIONS, opId,
              Arrays.asList(COL_OPERATION_WAREHOUSE_TO, COL_OPERATION_WAREHOUSE_TO + "Code"),
              new RowCallback() {
                @Override
                public void onSuccess(BeeRow result) {
                  if (result != null) {
                    newRow.setValue(targetInfo.getColumnIndex(COL_PURCHASE_WAREHOUSE_TO),
                        result.getLong(0));
                    newRow.setValue(targetInfo.getColumnIndex(COL_PURCHASE_WAREHOUSE_TO + "Code"),
                        result.getString(1));
                  }
                  consumer.accept(data, newRow);
                }
              });
        } else {
          consumer.accept(data, newRow);
        }
      }
    });
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoPurchasesGrid();
  }

  @Override
  protected String getRelationColumn() {
    return COL_PURCHASE;
  }

  @Override
  protected ParameterList getRequestArgs() {
    return TransportHandler.createArgs(SVC_CREATE_INVOICE_ITEMS);
  }

  @Override
  protected String getTargetView() {
    return VIEW_CARGO_PURCHASE_INVOICES;
  }
}
