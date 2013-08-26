package com.butent.bee.client.modules.transport;

import com.google.common.collect.ImmutableMap;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.TBL_SERVICES;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class TransportSelectorHandler implements Handler {

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), TBL_SERVICES)) {
      handleServices(event);
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
    final DataView dataView = UiHelper.getDataView(event.getSelector());

    if (dataView == null || BeeUtils.isEmpty(dataView.getViewName())) {
      return;
    }
    final DataInfo targetInfo = Data.getDataInfo(dataView.getViewName());
    final IsRow target = dataView.getActiveRow();

    if (target == null) {
      return;
    }
    final Value hasVat = source.getValue(sourceInfo.getColumnIndex(COL_VAT));

    if (BeeUtils.unbox(hasVat.getBoolean())) {
      Consumer<String> consumer = new Consumer<String>() {
        @Override
        public void accept(String prm) {
          Map<String, Value> updatedColumns = ImmutableMap
              .of(COL_VAT, Value.getValue(BeeUtils.toIntOrNull(prm)), COL_VAT_PERC, hasVat);

          for (String targetColumn : updatedColumns.keySet()) {
            int targetIndex = targetInfo.getColumnIndex(targetColumn);

            if (BeeConst.isUndef(targetIndex)) {
              continue;
            }
            Value value = updatedColumns.get(targetColumn);

            if (dataView.isFlushable()) {
              target.setValue(targetIndex, value);
            } else {
              target.preliminaryUpdate(targetIndex, value.getString());
            }
            dataView.refreshBySource(targetColumn);
          }
        }
      };
      String vat = source.getString(sourceInfo.getColumnIndex(COL_VAT_PERC));

      if (BeeUtils.isPositiveInt(vat)) {
        consumer.accept(vat);
      } else {
        Global.getParameter(COMMONS_MODULE, PRM_VAT_PERCENT, consumer);
      }
    }
  }
}
