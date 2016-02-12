package com.butent.bee.client.dialog;

@FunctionalInterface
public interface ConfirmationCallback {

  default void onCancel() {
  }

  void onConfirm();
}
