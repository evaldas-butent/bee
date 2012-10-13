package com.butent.bee.client.modules.commons;

import com.butent.bee.client.data.HasDataProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CommonsSelectorHandler implements SelectorEvent.Handler {

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), UserServiceBean.TBL_ROLES)) {
      handleRoles(event);
    }
  }

  private void handleRoles(SelectorEvent event) {
    if (!event.isOpened()) {
      return;
    }
    GridView gridView = UiHelper.getGrid(event.getSelector());
    if (gridView == null) {
      return;
    }
    IsRow row = gridView.getActiveRow();
    if (row == null) {
      return;
    }
    long id = row.getId();

    if (BeeUtils.same(gridView.getViewName(), UserServiceBean.TBL_USER_ROLES)) {
      Provider provider = ((HasDataProvider) gridView.getViewPresenter()).getDataProvider();

      if (provider != null) {
        int index = provider.getColumnIndex(UserServiceBean.FLD_ROLE);
        Long exclude = DataUtils.isId(id) ? row.getLong(index) : null;
        List<Long> used = DataUtils.getDistinct(gridView.getRowData(), index, exclude);

        if (!BeeUtils.isEmpty(used)) {
          CompoundFilter filter = Filter.and();

          for (Long value : used) {
            filter.add(ComparisonFilter.compareId(Operator.NE, value));
          }
          event.getSelector().setAdditionalFilter(filter);
        }
      }
    }
  }
}
