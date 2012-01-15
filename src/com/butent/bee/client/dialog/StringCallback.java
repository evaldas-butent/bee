package com.butent.bee.client.dialog;

public abstract class StringCallback {

  public void onCancel() {
  }
  
  public abstract void onSuccess(String value);
  
  @SuppressWarnings("unused")
  public void onTimeout(String value) {
  }
}
