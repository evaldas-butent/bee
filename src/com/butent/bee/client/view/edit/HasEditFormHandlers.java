package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasEditFormHandlers {

  HandlerRegistration addEditFormHandler(EditFormEvent.Handler handler);
}
