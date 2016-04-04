package com.butent.bee.client.modules.classifiers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.grid.interceptor.TreeGridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class ItemsGrid extends TreeGridInterceptor {

  static String getSupplierKey(boolean services) {
    return BeeUtils.join(BeeConst.STRING_UNDER, GRID_ITEMS, services ? "services" : "goods");
  }

  private final boolean services;

  ItemsGrid(boolean services) {
    this.services = services;
  }

  @Override
  public ColumnDescription beforeCreateColumn(GridView gridView,
      ColumnDescription columnDescription) {

    String id = columnDescription.getId();

    if (showServices()) {
      switch (id) {
        case COL_ITEM_WEIGHT:
        case COL_ITEM_AREA:
          return null;
      }

    } else {
      switch (id) {
        case COL_TIME_UNIT:
        case COL_ITEM_DPW:
        case COL_ITEM_MIN_TERM:
          return null;
      }
    }

    return super.beforeCreateColumn(gridView, columnDescription);
  }

  @Override
  public String getCaption() {
    if (showServices()) {
      return Localized.dictionary().services();
    } else {
      return Localized.dictionary().goods();
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ItemsGrid(services);
  }

  @Override
  public List<String> getParentLabels() {
    if (getSelectedTreeItem() == null || getTreeView() == null) {
      return super.getParentLabels();
    } else {
      return getTreeView().getPathLabels(getSelectedTreeItem().getId(), COL_CATEGORY_NAME);
    }
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    gridDescription.setCaption(null);

    Filter filter;
    if (showServices()) {
      filter = Filter.notNull(COL_ITEM_IS_SERVICE);
    } else {
      filter = Filter.isNull(COL_ITEM_IS_SERVICE);
    }

    gridDescription.setFilter(filter);
    return true;
  }

  public boolean showServices() {
    return services;
  }

  @Override
  protected Filter getFilter(Long category) {
    if (category == null) {
      return null;
    } else {
      return Filter.or(Filter.equals(COL_ITEM_TYPE, category),
          Filter.equals(COL_ITEM_GROUP, category),
          Filter.in(Data.getIdColumn(VIEW_ITEMS),
              VIEW_ITEM_CATEGORIES, COL_ITEM, Filter.equals(COL_CATEGORY, category)));
    }
  }

  IsRow getSelectedCategory() {
    return getSelectedTreeItem();
  }
}