package com.butent.bee.client.ui;

import com.google.gwt.xml.client.Element;

import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.HasWidgetSupplier;
import com.butent.bee.shared.ui.Relation;

import java.util.Set;

public interface WidgetInterceptor extends HasCaption, HasWidgetSupplier {

  void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback);

  boolean beforeCreateWidget(String name, Element description);

  void configureRelation(String name, Relation relation);

  IdentifiableWidget createCustomWidget(String name, Element description);

  Boolean getBooleanValue(String source);

  int getDataIndex(String source);

  DateTime getDateTimeValue(String source);

  JustDate getDateValue(String source);

  Set<Action> getDisabledActions(Set<Action> defaultActions);

  Set<Action> getEnabledActions(Set<Action> defaultActions);

  Integer getIntegerValue(String source);

  Long getLongValue(String source);

  String getStringValue(String source);
}
