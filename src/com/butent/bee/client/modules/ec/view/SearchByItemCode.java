package com.butent.bee.client.modules.ec.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.modules.ec.EcItem;
import com.butent.bee.client.modules.ec.widget.ItemList;
import com.butent.bee.client.modules.ec.widget.ItemSelector;

import java.util.List;

class SearchByItemCode extends EcView implements SelectionHandler<List<EcItem>> {
  
  private final ItemList itemList = new ItemList();

  SearchByItemCode() {
    super();
  }

  @Override
  public void onSelection(SelectionEvent<List<EcItem>> event) {
    itemList.render(event.getSelectedItem());
  }

  @Override
  protected void createUi() {
    ItemSelector selector = new ItemSelector();
    add(selector);
    
    add(itemList);
    
    selector.addSelectionHandler(this);
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByItemCode";
  }
}
