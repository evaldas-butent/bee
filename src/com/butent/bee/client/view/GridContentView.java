package com.butent.bee.client.view;

import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.view.client.HasData;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public interface GridContentView extends View, HasData<IsRow>, HasKeyboardPagingPolicy {
  void create(List<BeeColumn> dataColumns, int rowCount);

  int estimatePageSize(int containerWidth, int containerHeight); 
  
  void updatePageSize(int pageSize, boolean init); 
}
