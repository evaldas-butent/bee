package com.butent.bee.client.view.grid.interceptor;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.ui.GridDescription;

import java.util.List;

public abstract class TreeGridInterceptor extends AbstractGridInterceptor implements
    SelectionHandler<IsRow> {

  private TreeView treeView;
  private IsRow selectedTreeItem;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof TreeView) {
      treeView = (TreeView) widget;
      treeView.addSelectionHandler(this);
    }
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public void onSelection(SelectionEvent<IsRow> event) {
    if (event != null && getGridPresenter() != null) {
      setSelectedTreeItem(event.getSelectedItem());

      Long id = (event.getSelectedItem() == null) ? null : event.getSelectedItem().getId();
      Filter filter = getFilter(id);

      getGridPresenter().getDataProvider().setDefaultParentFilter(filter);
      getGridPresenter().refresh(true, true);
    }
  }

  protected abstract Filter getFilter(Long treeItemId);

  protected IsRow getSelectedTreeItem() {
    return selectedTreeItem;
  }

  protected int getTreeColumnIndex(String colName) {
    return DataUtils.getColumnIndex(colName, getTreeDataColumns(), true);
  }

  protected List<BeeColumn> getTreeDataColumns() {
    if (getTreeView() == null || getTreeView().getTreePresenter() == null) {
      return null;
    } else {
      return getTreeView().getTreePresenter().getDataColumns();
    }
  }

  protected TreeView getTreeView() {
    return treeView;
  }

  private void setSelectedTreeItem(IsRow selectedTreeItem) {
    this.selectedTreeItem = selectedTreeItem;
  }
}
