package com.butent.bee.client.view;

import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.shared.data.IsRow;

public interface DataView extends View, NotificationListener  {

  void finishNewRow(IsRow row);

  void startNewRow();
}
