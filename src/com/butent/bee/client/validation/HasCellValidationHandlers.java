package com.butent.bee.client.validation;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasCellValidationHandlers {
  HandlerRegistration addCellValidationHandler(CellValidateEvent.Handler handler);

  Boolean fireCellValidation(CellValidateEvent event);
}
