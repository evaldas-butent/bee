package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;

public interface HasIndexedWidgets extends HasWidgets, InsertPanel, IsWidget {
  boolean isEmpty();
}
