package com.butent.bee.client.view;

public interface DataFooterView extends View {
  void create(int rowCount, int pageSize, boolean addPaging, boolean addSearch);
}
