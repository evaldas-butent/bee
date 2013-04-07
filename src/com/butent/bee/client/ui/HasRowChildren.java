package com.butent.bee.client.ui;

import com.butent.bee.shared.data.RowChildren;

public interface HasRowChildren {
  
  RowChildren getChildrenForInsert();

  RowChildren getChildrenForUpdate();
}
