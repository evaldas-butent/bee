package com.butent.bee.client.validation;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;

import java.util.List;

public class CellValidationBus implements HasCellValidationHandlers {
  
  private List<CellValidateEvent.Handler> handlers = null;

  public CellValidationBus() {
    super();
  }

  public HandlerRegistration addCellValidationHandler(final CellValidateEvent.Handler handler) {
    Assert.notNull(handler);
    if (handlers == null) {
      handlers = Lists.newArrayList();
    }
    
    if (!handlers.contains(handler)) {
      handlers.add(handler);
    }
    
    return new HandlerRegistration() {
      public void removeHandler() {
        handlers.remove(handler);
      }
    };
  }
  
  public boolean fireCellValidation(CellValidateEvent event) {
    Assert.notNull(event);
    boolean ok = true;
    
    if (!event.isCanceled() && !handlers.isEmpty()) {
      for (CellValidateEvent.Handler handler : handlers) {
        ok = handler.validateCell(event);

        if (ok) {
          if (event.isCanceled()) {
            break;
          }
        } else {
          event.cancel();
          break;
        }
      }
    }
    return ok;
  }
}
