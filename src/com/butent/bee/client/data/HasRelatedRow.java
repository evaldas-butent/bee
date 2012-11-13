package com.butent.bee.client.data;

import com.butent.bee.shared.data.BeeRow;

public interface HasRelatedRow {
  
  BeeRow getRelatedRow();
  
  void setRelatedRow(BeeRow row);
}
