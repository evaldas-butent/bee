package com.butent.bee.client.view;

@FunctionalInterface
public interface ViewSupplier {
  void create(ViewCallback callback);
}
