package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.modules.trade.InvoiceBuilder;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public class CargoPurchasesGrid extends InvoiceBuilder {

  @Override
  protected void createInvoice(BeeRowSet data, BiConsumer<BeeRowSet, BeeRow> consumer) {
    DataInfo targetInfo = Data.getDataInfo(getTargetView());
    BeeRow newRow = RowFactory.createEmptyRow(targetInfo, true);

    Set<String> orders = new TreeSet<>();
    Map<Long, String> suppliers = new HashMap<>();
    Map<Long, String> currencies = new HashMap<>();
    String number = null;

    DataInfo info = Data.getDataInfo(getViewName());
    int order = info.getColumnIndex(COL_ORDER_NO);
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
    newRow.setValue(targetInfo.getColumnIndex(COL_TRADE_NOTES), BeeUtils.joinItems(orders));
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
    consumer.accept(data, newRow);
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoPurchasesGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (Objects.equals(event.getColumnId(), COL_ORDER_NO)) {
      Long assessment = Data.getLong(getViewName(), event.getRowValue(), COL_ASSESSMENT);

      if (DataUtils.isId(assessment)) {
        RowEditor.open(VIEW_ASSESSMENTS, assessment);
        event.consume();
        return;
      }
    }
    super.onEditStart(event);
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
