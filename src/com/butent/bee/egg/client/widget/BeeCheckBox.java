package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;

import com.butent.bee.egg.client.Global;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasBeeValueChangeHandler;
import com.butent.bee.egg.shared.BeeName;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.Pair;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeCheckBox extends CheckBox implements HasId,
    HasBeeValueChangeHandler<Boolean> {
  private String propKey = null;
  private String fieldName = null;

  private String checkedCaption = null;
  private String uncheckedCaption = null;

  public BeeCheckBox() {
    super();
    init();
  }

  public BeeCheckBox(BeeName nm) {
    this();

    String fld = nm.getName();
    if (!BeeUtils.isEmpty(fld)) {
      setText(Global.getFieldCaption(fld));
      initField(fld);
      addDefaultHandler();
    }
  }

  public BeeCheckBox(Element elem) {
    super(elem);
    init();
  }

  public BeeCheckBox(Pair<String, String> caption) {
    this();

    setUncheckedCaption(caption.getA());
    setCheckedCaption(caption.getB());

    setCaption();
    addDefaultHandler();
  }

  public BeeCheckBox(String label) {
    super(label);
    init();
  }

  public BeeCheckBox(String label, boolean asHTML) {
    super(label, asHTML);
    init();
  }

  public void createId() {
    DomUtils.createId(this, "c");
  }

  public String getCheckedCaption() {
    return checkedCaption;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getPropKey() {
    return propKey;
  }

  public String getUncheckedCaption() {
    return uncheckedCaption;
  }

  public boolean onValueChange(Boolean v) {
    setCaption(v);
    updateField(v);

    return true;
  }

  public void setCheckedCaption(String checkedCaption) {
    this.checkedCaption = checkedCaption;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setPropKey(String propKey) {
    this.propKey = propKey;
  }

  public void setUncheckedCaption(String uncheckedCaption) {
    this.uncheckedCaption = uncheckedCaption;
  }

  private void addDefaultHandler() {
    BeeKeeper.getBus().addBoolVch(this);
  }
  
  private void init() {
    createId();
    setStyleName("bee-CheckBox");
  }

  private void initField(String fld) {
    if (!BeeUtils.isEmpty(fld)) {
      setFieldName(fld);
      setValue(BeeUtils.toBoolean(Global.getFieldValue(fld)));
    }
  }

  private void setCaption() {
    setCaption(getValue());
  }

  private void setCaption(boolean v) {
    if (v) {
      if (!BeeUtils.isEmpty(checkedCaption)) {
        setText(checkedCaption);
      }
    } else {
      if (!BeeUtils.isEmpty(uncheckedCaption)) {
        setText(uncheckedCaption);
      }
    }
  }

  private void updateField(boolean v) {
    if (!BeeUtils.isEmpty(getFieldName())) {
      Global.setFieldValue(getFieldName(), BeeUtils.toString(v));
    }
  }

}
