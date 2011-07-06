package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;

import com.butent.bee.shared.HasStringValue;

/**
 * Implements a user interface component for inserting passwords.
 */

public class InputPassword extends InputText {

  public InputPassword() {
    super(Document.get().createPasswordInputElement());
  }

  public InputPassword(HasStringValue source) {
    this();
    initSource(source);
  }

  @Override
  protected String getDefaultIdPrefix() {
    return "pswd";
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InputPassword";
  }
}
