package com.butent.bee.client.modules.service;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.grid.interceptor.TreeGridInterceptor;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.modules.service.ServiceConstants.SvcObjectStatus;
import com.butent.bee.shared.ui.GridDescription;

public class ServiceObjectsGrid extends TreeGridInterceptor {

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
        case LOST_OBJECT:
          categoryTree.addStyleName(STYLE_TREE_PREFIX + "lost");
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
      return Localized.dictionary().svcAllObjects();
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
      gridDescription.setFilter(Filter.isNotEqual(COL_OBJECT_STATUS, Value.getValue(
          SvcObjectStatus.TEMPLATE_OBJECT.ordinal())));
      return true;
    }
    gridDescription.setFilter(Filter.isEqual(COL_OBJECT_STATUS, Value.getValue(status.ordinal())));
    return true;
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    int categoryIdx = gridView.getDataIndex(ServiceConstants.COL_SERVICE_CATEGORY);
    int nameIdx = gridView.getDataIndex(ServiceConstants.ALS_SERVICE_CATEGORY_NAME);
    int objectStatusIdx = gridView.getDataIndex(COL_OBJECT_STATUS);

    if (oldRow != null) {
      newRow.setValue(categoryIdx, oldRow.getString(categoryIdx));
      newRow.setValue(nameIdx, oldRow.getString(nameIdx));
    } else if (getTreeView() != null && getSelectedTreeItem() != null) {
      IsRow category = getSelectedTreeItem();

      if (category != null) {
        newRow.setValue(categoryIdx, category.getId());
        newRow.setValue(nameIdx,
            category.getString(getTreeColumnIndex(ServiceConstants.COL_SERVICE_CATEGORY_NAME)));
      }
    }

    newRow.setValue(objectStatusIdx, status.ordinal());
    return true;
  }

  @Override
  protected Filter getFilter(Long treeItemId) {
    if (treeItemId != null) {
      return Filter.equals(ServiceConstants.COL_SERVICE_CATEGORY, treeItemId);
    } else {
      return Filter.isFalse();
    }
  }
}
