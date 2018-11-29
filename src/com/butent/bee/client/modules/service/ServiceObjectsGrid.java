package com.butent.bee.client.modules.service;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.grid.interceptor.TreeGridInterceptor;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceObjectsGrid extends TreeGridInterceptor {

  ServiceObjectsGrid() {
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action == Action.COPY) {
      if (presenter.getMainView().isEnabled() && DataUtils.hasId(presenter.getActiveRow())) {

        copyServiceObjectData(presenter.getActiveRow().getId(), new IdCallback() {
          @Override
          public void onSuccess(Long dataId) {
            RowEditor.open(ServiceConstants.VIEW_SERVICE_OBJECTS, dataId,
                Opener.MODAL);
          }
        });
        return false;
      }
      return true;
    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public AbstractFilterSupplier getFilterSupplier(String columnName,
      ColumnDescription columnDescription) {

    if (Objects.equals(columnName, ServiceConstants.COL_SERVICE_MAIN_CRITERIA)) {
      return new ServiceObjectCriteriaFilter(null, null, null, null);
    }
    return super.getFilterSupplier(columnName, columnDescription);
  }

  @Override
  public GridInterceptor getInstance() {
    return new ServiceObjectsGrid();
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow, boolean copy) {
    int categoryIdx = gridView.getDataIndex(ServiceConstants.COL_SERVICE_CATEGORY);
    int nameIdx = gridView.getDataIndex(ServiceConstants.ALS_SERVICE_CATEGORY_NAME);

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
    return true;
  }

  @Override
  protected Filter getFilter(Long treeItemId) {
    if (treeItemId != null) {
      TreeView tree = getTreeView();

      Set<Long> categories = tree.getChildItems(BeeUtils.getLast(tree.getPath(treeItemId)), true)
          .stream().map(IsRow::getId).collect(Collectors.toSet());

      categories.add(treeItemId);

      return Filter.any(ServiceConstants.COL_SERVICE_CATEGORY, categories);
    } else {
      return Filter.isTrue();
    }
  }

  private static void copyServiceObjectData(Long dataId, final IdCallback callback) {

    ParameterList args = ServiceKeeper.createArgs(ServiceConstants.SVC_GET_SERVICE_OBJECT_DATA);
    args.addQueryItem(ServiceConstants.COL_SERVICE_OBJECT, dataId);

    BeeKeeper.getRpc().makeRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (!response.hasErrors()) {
          callback.onSuccess(response.getResponseAsLong());
        }
      }
    });
  }
}
