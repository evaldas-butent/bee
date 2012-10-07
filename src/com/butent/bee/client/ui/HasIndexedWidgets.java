package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.IsWidget;

public interface HasIndexedWidgets extends HasWidgets, IndexedPanel, IsWidget {
  boolean isEmpty();
}
