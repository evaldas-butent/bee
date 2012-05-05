package com.butent.bee.client.dialog;

import com.butent.bee.shared.Validator;
import com.butent.bee.shared.utils.BeeUtils;

public abstract class DialogCallback<T> implements Validator<T> {
  
  private boolean required;

  public DialogCallback() {
    this(true);
  }
  
  public DialogCallback(boolean required) {
    this.required = required;
  }

  public String getMessage(T value) {
    if (validate(value)) {
      return null;
    } else if (isRequired() && BeeUtils.isEmpty(value)) {
      return "Value required";
    } else {
      return "No way!";
    }
  }

  public boolean isRequired() {
    return required;
  }
  
  public void onCancel() {
  }

  public abstract void onSuccess(T value);

  public void onTimeout(T value) {
    if (validate(value)) {
      onSuccess(value);
    } else {
      onCancel();
    }
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean validate(T value) {
    if (isRequired()) {
      return !BeeUtils.isEmpty(value);
    } else {
      return true;
    }
  }
}
