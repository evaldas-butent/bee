package com.butent.bee.client.view;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;

import com.butent.bee.shared.data.IsRow;

public interface TreeView extends View, HasSelectionHandlers<IsRow> {

  void addItem(Long parentId, String text, IsRow item);

  IsRow getSelectedItem();

  void removeItem(IsRow item);

  void removeItems();

  void updateItem(String text, IsRow item);
}
