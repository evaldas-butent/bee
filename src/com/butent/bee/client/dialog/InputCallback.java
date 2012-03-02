package com.butent.bee.client.dialog;

import com.butent.bee.shared.utils.BeeUtils;

public abstract class InputCallback {

  public String getErrorMessage() {
    return null;
  }

  public void onCancel() {
  }

  public abstract void onSuccess();

  public void onTimeout() {
    if (BeeUtils.isEmpty(getErrorMessage())) {
      onSuccess();
    } else {
      onCancel();
    }
  }
}
