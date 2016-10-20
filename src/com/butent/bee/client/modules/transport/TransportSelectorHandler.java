package com.butent.bee.client.modules.transport;

import com.google.common.collect.ImmutableMap;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.DecimalValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class TransportSelectorHandler implements Handler {

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.inList(event.getRelatedViewName(), TBL_SERVICES, ClassifierConstants.TBL_ITEMS)) {
      handleServices(event);

    } else if (BeeUtils.same(event.getRelatedViewName(), ClassifierConstants.TBL_COMPANIES)) {
      handleCompanies(event);
    }
  }

  private static void handleCompanies(SelectorEvent event) {
    if (!event.isChanged()) {
      return;
    }
    final DataView dataView = ViewHelper.getDataView(event.getSelector());

    if (dataView == null || !BeeUtils.same(dataView.getViewName(), "CargoInvoices")
        || !dataView.isFlushable()) {
      return;
    }
    final DataInfo targetInfo = Data.getDataInfo(dataView.getViewName());
    final IsRow target = dataView.getActiveRow();

    if (target == null) {
      return;
    }

    RpcCallback<String> consumer = new RpcCallback<String>() {
      @Override
      public void onSuccess(String result) {
        int days = BeeUtils.toInt(result);

        if (BeeUtils.isPositive(days)) {
          target.setValue(targetInfo.getColumnIndex(COL_TRADE_TERM),
              new DateValue(TimeUtils
                  .nextDay(target.getDateTime(targetInfo.getColumnIndex(COL_DATE)), days)));
        } else {
          target.clearCell(targetInfo.getColumnIndex(COL_TRADE_TERM));
        }
        dataView.refreshBySource(COL_TRADE_TERM);
      }
    };

    Long id = target.getLong(targetInfo.getColumnIndex(COL_PAYER));

    if (!DataUtils.isId(id)) {
      id = target.getLong(targetInfo.getColumnIndex(COL_CUSTOMER));
    }
    if (DataUtils.isId(id)) {
      Queries.getValue(ClassifierConstants.TBL_COMPANIES, id, "CreditDays", consumer);
    } else {
      consumer.onSuccess(null);
    }
  }

  private static void handleServices(SelectorEvent event) {
    if (!event.isChanged()) {
      return;
    }
    final DataInfo sourceInfo = Data.getDataInfo(event.getRelatedViewName());
    final IsRow source = event.getRelatedRow();

    if (source == null) {
      return;
    }
    final DataView dataView = ViewHelper.getDataView(event.getSelector());

    if (dataView == null || BeeUtils.isEmpty(dataView.getViewName()) || !dataView.isFlushable()) {
      return;
    }
    final DataInfo targetInfo = Data.getDataInfo(dataView.getViewName());
    final IsRow target = dataView.getActiveRow();

    if (target == null) {
      return;
    }
    Double vat = source.getDouble(sourceInfo.getColumnIndex(COL_TRADE_VAT_PERC));
    boolean vatPerc = vat != null;

    Map<String, Value> updatedColumns = ImmutableMap
        .of(COL_TRADE_VAT, vatPerc ? Value.getValue(vat) : DecimalValue.getNullValue(),
            COL_TRADE_VAT_PERC, vatPerc ? Value.getValue(vatPerc) : BooleanValue.getNullValue());

    for (String targetColumn : updatedColumns.keySet()) {
      int targetIndex = targetInfo.getColumnIndex(targetColumn);

      if (BeeConst.isUndef(targetIndex)) {
        continue;
      }
      target.setValue(targetIndex, updatedColumns.get(targetColumn));
      dataView.refreshBySource(targetColumn);
    }
  }
}
