package com.butent.bee.client.view.navigation;

import com.google.gwt.view.client.HasRows;

import com.butent.bee.client.view.View;

/**
 * Extends {@code bee.client.view.view} interface, requires to have a {@code start} method.
 */

public interface PagerView extends View {
  void start(HasRows display);

}
