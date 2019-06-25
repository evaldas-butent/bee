package com.butent.bee.client.modules.calendar;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.ui.Action;

import java.util.Collection;
import java.util.Map;
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
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
    if (!BeeKeeper.getUser().isAdministrator()) {
      return Provider.createDefaultParentFilters(Filter.and(Filter.isNull(COL_CALENDAR_IS_SERVICE),
          Filter.or(Filter.isNull(COL_CALENDAR_OWNER),
              Filter.equals(COL_CALENDAR_OWNER, BeeKeeper.getUser().getUserId()),
              Filter.isNull(COL_VISIBILITY),
              Filter.notEquals(COL_VISIBILITY, CalendarVisibility.PRIVATE))));
    }
    return super.getInitialParentFilters(uiOptions);
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
