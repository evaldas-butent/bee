package com.butent.bee.client.modules.cars;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_CURRENCY;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.modules.classifiers.ClassifierKeeper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.ParentRowRefreshGrid;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CarServiceJobsGrid extends ParentRowRefreshGrid implements SelectorEvent.Handler {

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (editor instanceof DataSelector && ((DataSelector) editor).hasRelatedView(TBL_CAR_JOBS)) {
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
      Filter filter = Filter.isNull(COL_MODEL);

      if (DataUtils.isId(parentForm.getLongValue(COL_MODEL))) {
        filter = Filter.or(filter, Filter.equals(COL_MODEL, parentForm.getLongValue(COL_MODEL)));
      }
      event.getSelector().setAdditionalFilter(filter);

    } else if (event.isChangePending()) {
      String srcView = event.getRelatedViewName();
      IsRow srcRow = event.getRelatedRow();
      if (srcRow == null) {
        return;
      }
      DataView dataView = ViewHelper.getDataView(event.getSelector());
      if (dataView == null || BeeUtils.isEmpty(dataView.getViewName()) || !dataView.isFlushable()) {
        return;
      }
      IsRow target = dataView.getActiveRow();
      if (target == null) {
        return;
      }
      Map<String, String> values = new HashMap<>();

      values.put(COL_DURATION, BeeUtils.nvl(Data.getString(srcView, srcRow,
          COL_MODEL + COL_DURATION), Data.getString(srcView, srcRow, COL_DURATION)));

      Double price = BeeUtils.nvl(Data.getDouble(srcView, srcRow, COL_MODEL + COL_PRICE),
          Data.getDouble(srcView, srcRow, COL_PRICE));

      Long currency = BeeUtils.nvl(Data.getLong(srcView, srcRow, COL_MODEL + COL_CURRENCY),
          Data.getLong(srcView, srcRow, COL_CURRENCY));

      DateTime mainDate = parentForm.getDateTimeValue(COL_DATE);
      Long mainCurrency = parentForm.getLongValue(COL_CURRENCY);

      if (BeeUtils.allNotNull(price, currency)) {
        price = BeeUtils.round(Money.exchange(currency, mainCurrency, price, mainDate), 2);
      }
      values.put(COL_PRICE, BeeUtils.toStringOrNull(price));
      values.put(COL_TRADE_DOCUMENT_ITEM_DISCOUNT, null);
      values.put(COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT, null);

      Map<String, String> options = new HashMap<>();
      options.put(COL_DISCOUNT_COMPANY, parentForm.getStringValue(COL_CUSTOMER));
      options.put(Service.VAR_TIME, BeeUtils.toString(mainDate.getTime()));
      options.put(COL_DISCOUNT_CURRENCY, BeeUtils.toStringOrNull(mainCurrency));
      options.put(COL_DISCOUNT_WAREHOUSE, parentForm.getStringValue(COL_WAREHOUSE));
      options.put(COL_MODEL, parentForm.getStringValue(COL_MODEL));
      options.put(COL_PRODUCTION_DATE, parentForm.getStringValue(COL_PRODUCTION_DATE));

      ClassifierKeeper.getPriceAndDiscount(Data.getLong(srcView, srcRow, COL_ITEM), options,
          (prc, percent) -> {
            if (BeeUtils.isPositive(prc)
                && BeeUtils.isLess(prc, BeeUtils.toDoubleOrNull(values.get(COL_PRICE)))) {
              values.put(COL_PRICE, BeeUtils.toString(prc, 2));
            }
            values.put(COL_TRADE_DOCUMENT_ITEM_DISCOUNT, BeeUtils.toStringOrNull(percent));
            values.put(COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT,
                BeeUtils.toString(BeeUtils.isPositive(percent)));

            values.forEach((col, val) -> {
              target.setValue(dataView.getDataIndex(col), val);
              dataView.refreshBySource(col);
            });
          });
    }
  }
}
