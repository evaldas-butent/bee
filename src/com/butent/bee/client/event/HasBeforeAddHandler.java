package com.butent.bee.client.event;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public interface HasBeforeAddHandler {
  Widget onBeforeAdd(HasWidgets parent);
}
