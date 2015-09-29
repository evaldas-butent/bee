package com.butent.bee.client.modules.service;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;

public class ServiceObjectsGrid extends AbstractGridInterceptor implements
    SelectionHandler<IsRow> {

  private static final String FILTER_KEY = "f1";

  private TreeView categoryTree;

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
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof TreeView) {
      categoryTree = (TreeView) widget;
      categoryTree.addSelectionHandler(this);
    }
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public GridInterceptor getInstance() {
    return new ServiceObjectsGrid();
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
    return true;
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
