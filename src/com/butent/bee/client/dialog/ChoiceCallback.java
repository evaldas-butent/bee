package com.butent.bee.client.dialog;

@FunctionalInterface
public interface ChoiceCallback {

  default void onCancel() {
  }

  void onSuccess(int value);

  default void onTimeout() {
  }
}
