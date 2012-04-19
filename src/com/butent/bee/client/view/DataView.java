package com.butent.bee.client.view;

import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.shared.data.IsRow;

public interface DataView extends View, NotificationListener  {

  HandlerRegistration addCellValidationHandler(String columnId, CellValidateEvent.Handler handler);
  
  void finishNewRow(IsRow row);

  void startNewRow();
}
