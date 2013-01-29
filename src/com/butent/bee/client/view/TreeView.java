package com.butent.bee.client.view;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;

import com.butent.bee.client.event.logical.CatchEvent;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.HasCaption;

import java.util.Collection;

public interface TreeView extends View, HasSelectionHandlers<IsRow>,
    CatchEvent.HasCatchHandlers<IsRow>, HasCaption {

  void addItem(Long parentId, String text, IsRow item, boolean focus);

  Collection<IsRow> getChildItems(IsRow item, boolean recurse);

  IsRow getParentItem(IsRow item);

  IsRow getSelectedItem();

  TreePresenter getTreePresenter();

  void removeItem(IsRow item);

  void removeItems();

  void updateItem(String text, IsRow item);
}
