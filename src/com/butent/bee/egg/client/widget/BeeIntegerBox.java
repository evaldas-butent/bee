package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.BeeBus;
import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.event.HasBeeKeyHandler;
import com.butent.bee.egg.client.event.HasBeeValueChangeHandler;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.app.client.IntegerBox;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.TextBoxBase;

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

    setTextAlignment(TextBoxBase.ALIGN_RIGHT);
    setValue(BeeGlobal.getFieldInt(fieldName));
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public boolean onBeeKey(KeyPressEvent event) {
    return true;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public boolean onValueChange(Integer value) {
    if (!BeeUtils.isEmpty(getFieldName()))
      BeeGlobal.setFieldValue(getFieldName(), value);

    return true;
  }

  private void createId() {
    BeeDom.setId(this);
  }

  private void addDefaultHandlers() {
    BeeBus.addKeyHandler(this);
    BeeBus.addIntVch(this);
  }

}
