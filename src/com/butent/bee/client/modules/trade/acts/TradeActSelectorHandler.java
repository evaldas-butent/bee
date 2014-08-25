package com.butent.bee.client.modules.trade.acts;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

class TradeActSelectorHandler implements SelectorEvent.Handler {

  TradeActSelectorHandler() {
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    String relatedViewName = event.getRelatedViewName();
    if (BeeUtils.isEmpty(relatedViewName)) {
      return;
    }

    switch (relatedViewName) {
      case TradeConstants.VIEW_TRADE_OPERATIONS:
        if (event.isOpened()) {
          TradeActKind kind = getKind(event);

          if (kind != null) {
            Filter filter;
            if (kind == TradeActKind.SUPPLEMENT) {
              filter = Filter.or(Filter.equals(COL_TA_KIND, TradeActKind.SALE),
                  Filter.equals(COL_TA_KIND, kind));
            } else {
              filter = Filter.equals(COL_TA_KIND, kind);
            }

            event.getSelector().setAdditionalFilter(filter);
          }
        }
        break;
    }
  }

  private static TradeActKind getKind(SelectorEvent event) {
    DataView dataView = UiHelper.getDataView(event.getSelector());

    if (dataView != null && VIEW_TRADE_ACTS.equals(dataView.getViewName())
        && dataView.getActiveRow() != null) {

      return EnumUtils.getEnumByIndex(TradeActKind.class,
          dataView.getActiveRow().getInteger(dataView.getDataIndex(COL_TA_KIND)));
    } else {
      return null;
    }
  }
}
