package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.PRM_VAT_PERCENT;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_VAT_PLUS;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_COSTS_VAT;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.ParentRowRefreshGrid;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public abstract class TransportVatGridInterceptor extends ParentRowRefreshGrid {

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (Objects.equals(source, COL_TRADE_VAT_PLUS) && editor instanceof InputBoolean) {
      ((InputBoolean) editor).addValueChangeHandler(valueChangeEvent -> {
        FormView form = ViewHelper.getForm(editor);

        int vatColumnIndex = Data.getColumnIndex(getViewName(), COL_COSTS_VAT);
        IsRow row = form.getActiveRow();
        String vat = row.getString(vatColumnIndex);

        if (BeeUtils.toBoolean(valueChangeEvent.getValue()) && BeeUtils.isEmpty(vat)) {
          row.setValue(vatColumnIndex, BeeUtils.unbox(Global.getParameterNumber(PRM_VAT_PERCENT)));
          row.setValue(Data.getColumnIndex(getViewName(),
              ClassifierConstants.COL_ITEM_VAT_PERCENT), true);

          form.refreshBySource(COL_COSTS_VAT);
          form.refreshBySource(ClassifierConstants.COL_ITEM_VAT_PERCENT);
        }
      });
    }
    super.afterCreateEditor(source, editor, embedded);
  }
}
