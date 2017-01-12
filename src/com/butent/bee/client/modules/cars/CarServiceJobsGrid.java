package com.butent.bee.client.modules.cars;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_CURRENCY;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.ParentRowRefreshGrid;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CarServiceJobsGrid extends ParentRowRefreshGrid implements SelectorEvent.Handler {
  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (Objects.equals(source, COL_JOB) && editor instanceof DataSelector) {
      ((DataSelector) editor).addSelectorHandler(this);
    }
    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    FormView parentForm = ViewHelper.getForm(getGridView());

    if (Objects.isNull(parentForm)) {
      return;
    }
    if (event.isOpened()) {
      event.getSelector().setAdditionalFilter(Filter.or(Filter.isNull(COL_MODEL),
          Filter.equals(COL_MODEL, parentForm.getLongValue(COL_MODEL))));

    } else if (event.isChanged()) {
      String srcView = event.getRelatedViewName();
      IsRow srcRow = event.getRelatedRow();

      if (srcRow == null) {
        return;
      }
      FormView form = ViewHelper.getForm(event.getSelector());

      if (form == null || !form.isFlushable()) {
        return;
      }
      IsRow target = form.getActiveRow();

      if (target == null) {
        return;
      }
      Map<String, Object> values = new HashMap<>();

      values.put(COL_DURATION, BeeUtils.nvl(Data.getString(srcView, srcRow,
          COL_MODEL + COL_DURATION), Data.getString(srcView, srcRow, COL_DURATION)));

      Double price = BeeUtils.nvl(Data.getDouble(srcView, srcRow, COL_MODEL + COL_PRICE),
          Data.getDouble(srcView, srcRow, COL_PRICE));

      Long currency = BeeUtils.nvl(Data.getLong(srcView, srcRow, COL_MODEL + COL_CURRENCY),
          Data.getLong(srcView, srcRow, COL_CURRENCY));

      if (BeeUtils.allNotNull(price, currency)) {
        price = BeeUtils.round(Money.exchange(currency, parentForm.getLongValue(COL_CURRENCY),
            price, parentForm.getDateTimeValue(COL_DATE)), 2);
      }
      values.put(COL_PRICE, price);

      values.forEach((col, val) -> {
        target.setValue(form.getDataIndex(col), Value.getValue(val));
        form.refreshBySource(col);
      });
    }
  }
}
