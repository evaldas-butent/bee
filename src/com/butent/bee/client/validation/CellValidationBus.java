package com.butent.bee.client.validation;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

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

  public Boolean fireCellValidation(CellValidateEvent event) {
    Assert.notNull(event);
    Boolean ok = true;

    if (!event.isCanceled() && handlers != null) {
      for (CellValidateEvent.Handler handler : handlers) {
        ok = handler.validateCell(event);

        if (!BeeUtils.isEmpty(ok)) {
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
