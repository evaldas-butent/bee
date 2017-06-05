package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InsertPanel;

public interface HasIndexedWidgets extends HasWidgets, InsertPanel, IdentifiableWidget {
  boolean isEmpty();
}
