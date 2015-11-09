package com.butent.bee.client.data;

import com.butent.bee.shared.data.IsRow;

public interface HasActiveRow {

  IsRow getActiveRow();

  long getActiveRowId();
}
