package com.butent.bee.client.modules.commons;

import com.google.common.collect.Sets;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

class ItemGridHandler extends AbstractGridInterceptor implements SelectionHandler<IsRow> {

  private static final BeeLogger logger = LogUtils.getLogger(ItemGridHandler.class);
  
  private static final String FILTER_KEY = "f1";
  private final boolean services;
  private IsRow selectedCategory = null;

  ItemGridHandler(boolean showServices) {
    this.services = showServices;
  }

  @Override
  public String getCaption() {
    if (showServices()) {
      return "Paslaugos";
    } else {
      return "Prekės";
    }
  }

  @Override
  public String getRowCaption(IsRow row, boolean edit) {
    return (edit ? "" : "Nauja ") + (showServices() ? "Paslauga" : "Prekė");
  }

  @Override
  public String getSupplierKey() {
    return BeeUtils.normalize(BeeUtils.join(BeeConst.STRING_UNDER, "grid",
        CommonsConstants.TBL_ITEMS, getCaption()));
  }

  @Override
  public boolean onLoad(GridDescription gridDescription) {
    gridDescription.setCaption(null);

    Filter filter = Filter.isEmpty(CommonsConstants.COL_SERVICE);

    if (showServices()) {
      filter = Filter.isNot(filter);
    }
    gridDescription.setFilter(filter);
    return true;
  }
  
  @Override
  public boolean onSaveChanges(GridView gridView, SaveChangesEvent event) {
    String oldCateg = event.getOldRow().getProperty(CommonsConstants.PROP_CATEGORIES);
    String newCateg = event.getNewRow().getProperty(CommonsConstants.PROP_CATEGORIES);
    
    if (!BeeUtils.same(oldCateg, newCateg)) {
      Set<Long> oldValues = DataUtils.parseIdSet(oldCateg);
      Set<Long> newValues = DataUtils.parseIdSet(newCateg);
      
      Set<Long> insert = Sets.newHashSet(newValues);
      insert.removeAll(oldValues);

      Set<Long> delete = Sets.newHashSet(oldValues);
      delete.removeAll(newValues);
      
      long itemId = event.getRowId();
      
      if (!delete.isEmpty()) {
        ParameterList args = CommonsKeeper.createArgs(CommonsConstants.SVC_REMOVE_CATEGORIES);
        args.addDataItem(CommonsConstants.VAR_ITEM_ID, itemId);
        args.addDataItem(CommonsConstants.VAR_ITEM_CATEGORIES, DataUtils.buildIdList(delete));

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            logger.info(CommonsConstants.SVC_REMOVE_CATEGORIES, response.getResponse());
          }
        });
      }

      if (!insert.isEmpty()) {
        ParameterList args = CommonsKeeper.createArgs(CommonsConstants.SVC_ADD_CATEGORIES);
        args.addDataItem(CommonsConstants.VAR_ITEM_ID, itemId);
        args.addDataItem(CommonsConstants.VAR_ITEM_CATEGORIES, DataUtils.buildIdList(insert));

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            logger.info(CommonsConstants.SVC_ADD_CATEGORIES, response.getResponse());
          }
        });
      }
    }
    
    return super.onSaveChanges(gridView, event);
  }

  @Override
  public void onSelection(SelectionEvent<IsRow> event) {
    if (event == null) {
      return;
    }
    if (getGridPresenter() != null) {
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

  private Filter getFilter(Long category) {
    if (category == null) {
      return null;
    } else {
      return ComparisonFilter.isEqual(CommonsConstants.COL_CATEGORY, new LongValue(category));
    }
  }

  private void setSelectedCategory(IsRow selectedCategory) {
    this.selectedCategory = selectedCategory;
  }
}