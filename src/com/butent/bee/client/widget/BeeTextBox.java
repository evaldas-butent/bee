package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasBeeKeyHandler;
import com.butent.bee.client.event.HasBeeValueChangeHandler;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a text box that allows a single line of text to be entered.
 */

public class BeeTextBox extends TextBox implements HasId, HasBeeKeyHandler,
    HasBeeValueChangeHandler<String> {
  private HasStringValue source = null;

  public BeeTextBox() {
    super();
    init();
  }

  public BeeTextBox(Element element) {
    super(element);
    init();
  }

  public BeeTextBox(HasStringValue source) {
    this();

    if (source != null) {
      setSource(source);
      String v = source.getString();
      if (!BeeUtils.isEmpty(v)) {
        setValue(v);
      }
    }
  }

  public void createId() {
    DomUtils.createId(this, "txt");
  }

  public String getDefaultStyleName() {
    return "bee-TextBox";
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public HasStringValue getSource() {
    return source;
  }

  public boolean onBeeKey(KeyPressEvent event) {
    return true;
  }

  public boolean onValueChange(String value) {
    if (source != null) {
      source.setValue(value);
    }
    return true;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addKeyHandler(this);
    BeeKeeper.getBus().addStringVch(this);
  }

  private void init() {
    setStyleName(getDefaultStyleName());
    createId();
    addDefaultHandlers();
  }
}
