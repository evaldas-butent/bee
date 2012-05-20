package com.butent.bee.client.view;

import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.data.HasActiveRow;
import com.butent.bee.client.data.HasDataRows;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsRow;

public interface DataView extends View, NotificationListener, HasViewName, HasActiveRow,
    HasDataRows {

  HandlerRegistration addCellValidationHandler(String columnId, CellValidateEvent.Handler handler);

  void finishNewRow(IsRow row);

  void refresh(boolean refreshChildren);
  
  void startNewRow();
}
