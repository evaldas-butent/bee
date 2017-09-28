package com.butent.bee.client.data;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.DataInfo;

@FunctionalInterface
public interface FormNameProvider {
  String getFormName(DataInfo dataInfo, IsRow row);
}
