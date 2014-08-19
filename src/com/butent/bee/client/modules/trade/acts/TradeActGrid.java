package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.ui.GridDescription;

public class TradeActGrid extends AbstractGridInterceptor {

  private final TradeActKind kind;

  TradeActGrid(TradeActKind kind) {
    super();
    this.kind = kind;
  }

  @Override
  public String getCaption() {
    if (kind == null) {
      return Localized.getConstants().tradeActsAll();
    } else {
      return Localized.getConstants().tradeActs() + " - " + kind.getCaption();
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActGrid(kind);
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    Filter filter = (kind == null) ? null : kind.getFilter();
    gridDescription.setFilter(filter);
    return true;
  }
}
