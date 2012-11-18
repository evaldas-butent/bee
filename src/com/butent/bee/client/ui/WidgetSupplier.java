package com.butent.bee.client.ui;

import com.butent.bee.client.Callback;

public interface WidgetSupplier {
  void create(Callback<IdentifiableWidget> callback);
}
