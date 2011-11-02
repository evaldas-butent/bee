package com.butent.bee.client.view.navigation;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.data.event.ScopeChangeEvent;

/**
 * Extends {@code bee.client.view.view} interface, requires to have a {@code start} method.
 */

public interface PagerView extends View, ScopeChangeEvent.Handler {

  void start(HasDataTable display);
}
