package com.butent.bee.client.view.navigation;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.data.event.ScopeChangeEvent;

public interface PagerView extends View, ScopeChangeEvent.Handler, Printable {
  void start(HasDataTable display);
}
