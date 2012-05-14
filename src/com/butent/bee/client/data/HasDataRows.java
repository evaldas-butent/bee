package com.butent.bee.client.data;

import com.butent.bee.shared.data.IsRow;

import java.util.List;

public interface HasDataRows {
  List<? extends IsRow> getRowData();
}
