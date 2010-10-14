package com.butent.bee.egg.client.widget;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasBeeKeyHandler;
import com.butent.bee.egg.client.event.HasBeeValueChangeHandler;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeIntegerBox extends IntegerBox implements HasId,
    HasBeeKeyHandler, HasBeeValueChangeHandler<Integer> {
  private String fieldName = null;

  public BeeIntegerBox() {
    super();
    createId();
    addDefaultHandlers();
  }

  public BeeIntegerBox(String fieldName) {
    this();
    this.fieldName = fieldName;

    setWidth("6em");
    setAlignment(ValueBoxBase.TextAlignment.RIGHT);
    setValue(BeeGlobal.getFieldInt(fieldName));
  }

  public void createId() {
    DomUtils.createId(this, "int");
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

  public boolean onValueChange(Integer value) {
    if (!BeeUtils.isEmpty(getFieldName())) {
      BeeGlobal.setFieldValue(getFieldName(), value);
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
    BeeKeeper.getBus().addIntVch(this);
  }

}
