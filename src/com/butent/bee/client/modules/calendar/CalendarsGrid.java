package com.butent.bee.client.modules.calendar;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;

import java.util.Set;

public class CalendarsGrid extends AbstractGridInterceptor {

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    return BeeKeeper.getUser().isAdministrator() && super.beforeAddRow(presenter, copy);
  }

  @Override
  public Set<Action> getDisabledActions(Set<Action> defaultActions) {
    Set<Action> disabledActions = super.getDisabledActions(defaultActions);

    if (!BeeKeeper.getUser().isAdministrator()) {
      disabledActions.add(Action.ADD);
      disabledActions.add(Action.DELETE);
    }
    return disabledActions;
  }

  @Override
  public GridInterceptor getInstance() {
    return new CalendarsGrid();
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    return BeeKeeper.getUser().isAdministrator() && super.isRowEditable(row);
  }
}
