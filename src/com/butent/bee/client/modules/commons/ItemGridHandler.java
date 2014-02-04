package com.butent.bee.client.modules.commons;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

class ItemGridHandler extends AbstractGridInterceptor implements SelectionHandler<IsRow> {

  private static final String FILTER_KEY = "f1";
  private final boolean services;

  private IsRow selectedCategory;

  ItemGridHandler(boolean showServices) {
    this.services = showServices;
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof TreeView && BeeUtils.same(name, "Categories")) {
      ((TreeView) widget).addSelectionHandler(this);
    }
  }

  @Override
  public String getCaption() {
    if (showServices()) {
      return Localized.getConstants().services();
    } else {
      return Localized.getConstants().goods();
    }
  }

  @Override
  public String getRowCaption(IsRow row, boolean edit) {
    if (edit) {
      return showServices() ? Localized.getConstants().service() : Localized.getConstants().item();
    } else {
      return showServices() ? Localized.getConstants().newService()
          : Localized.getConstants().newItem();
    }
  }

  @Override
  public String getSupplierKey() {
    return BeeUtils.normalize(BeeUtils.join(BeeConst.STRING_UNDER, "grid",
        CommonsConstants.TBL_ITEMS, getCaption()));
  }

  @Override
  public boolean onLoad(GridDescription gridDescription) {
    gridDescription.setCaption(null);

    Filter filter = Filter.isNull(CommonsConstants.COL_ITEM_IS_SERVICE);

    if (showServices()) {
      filter = Filter.isNot(filter);
    }
    gridDescription.setFilter(filter);
    return true;
  }

  @Override
  public void onSelection(SelectionEvent<IsRow> event) {
    if (event != null && getGridPresenter() != null) {
      Long category = null;
      setSelectedCategory(event.getSelectedItem());

      if (getSelectedCategory() != null) {
        category = getSelectedCategory().getId();
      }
      getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(category));
      getGridPresenter().refresh(true);
    }
  }

  @Override
  public void onShow(GridPresenter presenter) {
    setGridPresenter(presenter);
  }

  public boolean showServices() {
    return services;
  }

  IsRow getSelectedCategory() {
    return selectedCategory;
  }

  private static Filter getFilter(Long category) {
    if (category == null) {
      return null;
    } else {
      return Filter.in(Data.getIdColumn(CommonsConstants.VIEW_ITEMS),
          CommonsConstants.VIEW_ITEM_CATEGORIES, CommonsConstants.COL_ITEM,
          ComparisonFilter.isEqual(CommonsConstants.COL_CATEGORY, new LongValue(category)));
    }
  }

  private void setSelectedCategory(IsRow selectedCategory) {
    this.selectedCategory = selectedCategory;
  }
}