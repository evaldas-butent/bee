package com.butent.bee.client.view;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;

import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.HasParent;
import com.butent.bee.shared.data.IsRow;

public interface TreeView extends View, HasParent, HasSelectionHandlers<IsRow> {

  void addItem(Long parentId, String text, IsRow item, boolean focus);

  IsRow getSelectedItem();

  TreePresenter getTreePresenter();

  void removeItem(IsRow item);

  void removeItems();

  void updateItem(String text, IsRow item);
}
