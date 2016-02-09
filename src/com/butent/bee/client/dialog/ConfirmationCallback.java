package com.butent.bee.client.dialog;

public interface ConfirmationCallback {

  default void onCancel() {
  }

  void onConfirm();
}
