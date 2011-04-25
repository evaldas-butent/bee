package com.butent.bee.client.view.grid;

import com.google.gwt.view.client.HasData;

import com.butent.bee.client.view.HasKeyboardPaging;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public interface GridView extends View, HasData<IsRow>, HasKeyboardPaging {
  void create(List<BeeColumn> dataColumns, int rowCount, BeeRowSet rowSet);

  int estimatePageSize(int containerWidth, int containerHeight); 
  
  boolean isCellEditing();
  
  void updatePageSize(int pageSize, boolean init); 
}
