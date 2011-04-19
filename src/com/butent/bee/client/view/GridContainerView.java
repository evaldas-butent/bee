package com.butent.bee.client.view;

import com.butent.bee.shared.data.BeeColumn;

import java.util.List;

public interface GridContainerView extends View, HasSearchView {
  void create(String caption, List<BeeColumn> dataColumns, int rowCount);
  
  int estimatePageSize(int containerWidth, int containerHeight); 

  GridContentView getContent();
  
  void updatePageSize(int pageSize); 
}
