package com.butent.bee.client.view;

import com.butent.bee.shared.data.BeeColumn;

import java.util.List;

public interface GridContainerView extends View {
  void create(String caption, List<BeeColumn> dataColumns, int rowCount);
  
  GridContentView getContent();
}
