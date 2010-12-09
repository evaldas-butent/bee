package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.egg.client.Global;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasBeeKeyHandler;
import com.butent.bee.egg.client.event.HasBeeValueChangeHandler;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeTextBox extends TextBox implements HasId, HasBeeKeyHandler,
    HasBeeValueChangeHandler<String> {
  private String fieldName = null;

  public BeeTextBox() {
    super();
    init();
  }

  public BeeTextBox(Element element) {
    super(element);
    init();
  }

  public BeeTextBox(String fieldName) {
    this();
    
    if (!BeeUtils.isEmpty(fieldName)) {
      this.fieldName = fieldName;

      String v = Global.getFieldValue(fieldName);
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

  public String getFieldName() {
    return fieldName;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public boolean onBeeKey(KeyPressEvent event) {
    return true;
  }

  public boolean onValueChange(String value) {
    if (!BeeUtils.isEmpty(getFieldName())) {
      Global.setFieldValue(getFieldName(), value);
    }

    return true;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
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
