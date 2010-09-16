package com.butent.bee.egg.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.SimpleCheckBox;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasBeeClickHandler;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeSimpleCheckBox extends SimpleCheckBox implements HasId,
    HasBeeClickHandler {
  private String fieldName = null;

  public BeeSimpleCheckBox() {
    super();
    createId();
  }

  public BeeSimpleCheckBox(String fld) {
    this();

    if (!BeeUtils.isEmpty(fld)) {
      initField(fld);
      addDefaultHandler();
    }
  }

  public void createId() {
    DomUtils.createId(this, "sc");
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public boolean onBeeClick(ClickEvent event) {
    updateField(isChecked());
    return true;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void addDefaultHandler() {
    BeeKeeper.getBus().addClickHandler(this);
  }

  private void initField(String fld) {
    if (!BeeUtils.isEmpty(fld)) {
      setFieldName(fld);
      setChecked(BeeUtils.toBoolean(BeeGlobal.getFieldValue(fld)));
    }
  }

  private void updateField(boolean v) {
    if (!BeeUtils.isEmpty(getFieldName())) {
      BeeGlobal.setFieldValue(getFieldName(), BeeUtils.toString(v));
    }
  }

}
