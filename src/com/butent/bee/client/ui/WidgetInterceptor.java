package com.butent.bee.client.ui;

import com.google.gwt.xml.client.Element;

import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.HasWidgetSupplier;

public interface WidgetInterceptor extends HasCaption, HasWidgetSupplier {

  void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback);

  boolean beforeCreateWidget(String name, Element description);

  IdentifiableWidget createCustomWidget(String name, Element description);
}
