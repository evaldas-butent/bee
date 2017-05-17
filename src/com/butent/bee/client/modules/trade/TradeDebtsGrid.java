package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.trade.DebtKind;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Map;

class TradeDebtsGrid extends AbstractGridInterceptor {

  private final DebtKind debtKind;

  TradeDebtsGrid(DebtKind debtKind) {
    this.debtKind = debtKind;
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeDebtsGrid(debtKind);
  }

  @Override
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
    return Provider.createDefaultParentFilters(buildFilter(null));
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();

    IsRow row = event.getRowValue();
    if (row != null) {
      RowEditor.open(getViewName(), row, Opener.NEW_TAB);
    }
  }

  void onCompanyChange(Long company) {
    GridPresenter presenter = getGridPresenter();

    if (presenter != null
        && presenter.getDataProvider().setDefaultParentFilter(buildFilter(company))) {

      presenter.handleAction(Action.REFRESH);
    }
  }

  private Filter buildFilter(Long company) {
    String arg1 = Codec.pack(debtKind);
    String arg2 = BeeUtils.toStringOrNull(company);

    return Filter.custom(FILTER_HAS_TRADE_DEBT, arg1, arg2);
  }
}
