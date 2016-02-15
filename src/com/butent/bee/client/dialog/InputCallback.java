package com.butent.bee.client.dialog;

import com.butent.bee.client.view.form.CloseCallback;

@FunctionalInterface
public interface InputCallback {

  default String getErrorMessage() {
    return null;
  }

  default void onAdd() {
  }

  default void onCancel() {
  }

  default void onClose(CloseCallback closeCallback) {
    closeCallback.onClose();
  }

  default void onDelete(DialogBox dialog) {
    dialog.close();
  }

  void onSuccess();
}
