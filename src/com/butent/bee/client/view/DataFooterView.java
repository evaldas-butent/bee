package com.butent.bee.client.view;

import com.google.gwt.view.client.HasRows;

public interface DataFooterView extends View, HasSearchView {
  void create(HasRows display, int rowCount, int pageSize, boolean addPaging, boolean addSearch);
}
