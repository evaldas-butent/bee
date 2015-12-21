package com.butent.bee.client.modules.service;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.modules.service.ServiceConstants.SvcObjectStatus;
import com.butent.bee.shared.ui.GridDescription;

public class ServiceObjectsGrid extends AbstractGridInterceptor implements
    SelectionHandler<IsRow> {

  private static final String FILTER_KEY = "f1";
  private static final String STYLE_TREE_PREFIX = "bee-svc-tree-";

  private TreeView categoryTree;
  private SvcObjectStatus status;

  ServiceObjectsGrid(SvcObjectStatus status) {
    this.status = status;
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof TreeView) {
      categoryTree = (TreeView) widget;
      categoryTree.addSelectionHandler(this);

      if (status == null) {
        categoryTree.addStyleName(STYLE_TREE_PREFIX + "all");
        return;
      }

      switch (status) {
        case POTENTIAL_OBJECT:
          categoryTree.addStyleName(STYLE_TREE_PREFIX + "potential");
          break;
        case PROJECT_OBJECT:
          categoryTree.addStyleName(STYLE_TREE_PREFIX + "project");
          break;
        case SERVICE_OBJECT:
          categoryTree.addStyleName(STYLE_TREE_PREFIX + "service");
          break;
        case TEMPLATE_OBJECT:
          categoryTree.addStyleName(STYLE_TREE_PREFIX + "template");
          break;
        default:
          categoryTree.addStyleName(STYLE_TREE_PREFIX + "service");
          break;
      }
    }
  }

  @Override
  public String getCaption() {
    if (status == null) {
      return Localized.getConstants().svcAllObjects();
    }
    return status.getListCaption();
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public GridInterceptor getInstance() {
    return new ServiceObjectsGrid(status);
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    if (status == null) {
      gridDescription.setFilter(null);
      return true;
    }
    gridDescription.setFilter(Filter.isEqual(COL_OBJECT_STATUS, Value.getValue(status.ordinal())));
    return true;
  }

  @Override
  public void onSelection(SelectionEvent<IsRow> event) {
    if (event != null) {
      Long category = (event.getSelectedItem() == null) ? null : event.getSelectedItem().getId();
      Filter flt;

      if (category != null) {
        flt = Filter.equals(ServiceConstants.COL_SERVICE_CATEGORY, category);
      } else {
        flt = Filter.isFalse();
      }

      getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, flt);
      getGridPresenter().refresh(true, true);
    }
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    int categoryIdx = gridView.getDataIndex(ServiceConstants.COL_SERVICE_CATEGORY);
    int nameIdx = gridView.getDataIndex(ServiceConstants.ALS_SERVICE_CATEGORY_NAME);
    int objectStatusIdx = gridView.getDataIndex(COL_OBJECT_STATUS);

    if (oldRow != null) {
      newRow.setValue(categoryIdx, oldRow.getString(categoryIdx));
      newRow.setValue(nameIdx, oldRow.getString(nameIdx));
    } else if (categoryTree != null) {
      IsRow category = categoryTree.getSelectedItem();

      if (category != null) {
        newRow.setValue(categoryIdx, category.getId());
        newRow.setValue(nameIdx,
            category.getString(DataUtils.getColumnIndex(ServiceConstants.COL_SERVICE_CATEGORY_NAME,
                categoryTree.getTreePresenter().getDataColumns())));
      }
    }

    newRow.setValue(objectStatusIdx, status.ordinal());
    return true;
  }
}
