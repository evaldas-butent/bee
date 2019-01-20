package com.butent.bee.client.modules.trade;

import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_STOCK_QUANTITY;

import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.grid.interceptor.TreeGridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;

import java.util.Set;

public class TradeStockGrid extends TreeGridInterceptor {

  private static final Set<String> showUpdatedColumns =
      Sets.newHashSet(ALS_ITEM_NAME, ALS_WAREHOUSE_CODE, COL_STOCK_QUANTITY);

  private long updatedOn;

  TradeStockGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeStockGrid();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    super.afterCreateWidget(name, widget, callback);

    if (widget instanceof TreeView) {
      TreePresenter treePresenter = ((TreeView) widget).getTreePresenter();

      if (treePresenter != null) {
        treePresenter.setFilter(Filter.or(Filter.notNull(COL_CATEGORY_GOODS),
            Filter.isNull(COL_CATEGORY_SERVICES)));
      }
    }
  }

  @Override
  public void afterRender(GridView gridView, RenderingEvent event) {
    if (updatedOn > 0 && gridView != null && !gridView.isEmpty()) {
      for (IsRow row : gridView.getRowData()) {
        if (row.getVersion() > updatedOn && !gridView.getGrid().isRowUpdated(row.getId())) {
          gridView.getGrid().addUpdatedSources(row.getId(), showUpdatedColumns);
        }
      }
    }

    this.updatedOn = System.currentTimeMillis();

    super.afterRender(gridView, event);
  }

  @Override
  protected Filter getFilter(Long category) {
    if (category == null) {
      return null;
    } else {
      return Filter.or(Filter.equals(COL_ITEM + COL_ITEM_TYPE, category),
          Filter.equals(COL_ITEM + COL_ITEM_GROUP, category),
          Filter.in(COL_ITEM, VIEW_ITEM_CATEGORIES, COL_ITEM,
              Filter.equals(COL_CATEGORY, category)));
    }
  }
}
