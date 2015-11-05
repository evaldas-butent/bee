package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;

class TradeActServicePicker extends TradeActItemPicker {

  @Override
  protected Filter getDefaultItemFilter() {
    return Filter.notNull(ClassifierConstants.COL_ITEM_IS_SERVICE);
  }

  @Override
  public Long getWarehouseFrom(IsRow row) {
    return null;
  }

  @Override
  public boolean setIsOrder() {
    return false;
  }
}