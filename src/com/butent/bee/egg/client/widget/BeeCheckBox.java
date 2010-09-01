package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.BeeBus;
import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeProperties;
import com.butent.bee.egg.client.event.HasBeeValueChangeHandler;
import com.butent.bee.egg.client.utils.BeeDom;

import com.butent.bee.egg.shared.BeeName;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.Pair;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;

public class BeeCheckBox extends CheckBox implements HasId,
    HasBeeValueChangeHandler<Boolean> {
  private String propKey = null;
  private String fieldName = null;

  private String checkedCaption = null;
  private String uncheckedCaption = null;

  public BeeCheckBox() {
    super();
    createId();
  }

  public BeeCheckBox(Element elem) {
    super(elem);
    createId();
  }

  public BeeCheckBox(String label, boolean asHTML) {
    super(label, asHTML);
    createId();
  }

  public BeeCheckBox(String label) {
    super(label);
    createId();
  }

  public BeeCheckBox(String label, String property) {
    this(label);

    initProperty(property);
    addDefaultHandler();
  }

  public BeeCheckBox(Pair<String, String> caption, String property) {
    this();

    setUncheckedCaption(caption.getA());
    setCheckedCaption(caption.getB());

    initProperty(property);
    setCaption();

    addDefaultHandler();
  }

  public BeeCheckBox(Pair<String, String> caption) {
    this();

    setUncheckedCaption(caption.getA());
    setCheckedCaption(caption.getB());

    setCaption();
    addDefaultHandler();
  }

  public BeeCheckBox(BeeName nm) {
    this();
    createId();

    String fld = nm.getName();
    if (!BeeUtils.isEmpty(fld)) {
      setText(BeeGlobal.getFieldCaption(fld));
      initField(fld);
      addDefaultHandler();
    }
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public String getPropKey() {
    return propKey;
  }

  public void setPropKey(String propKey) {
    this.propKey = propKey;
  }

  public String getCheckedCaption() {
    return checkedCaption;
  }

  public void setCheckedCaption(String checkedCaption) {
    this.checkedCaption = checkedCaption;
  }

  public String getUncheckedCaption() {
    return uncheckedCaption;
  }

  public void setUncheckedCaption(String uncheckedCaption) {
    this.uncheckedCaption = uncheckedCaption;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public boolean onValueChange(Boolean v) {
    setCaption(v);
    updateProperty(v);
    updateField(v);

    return true;
  }

  private void createId() {
    BeeDom.setId(this);
  }

  private void addDefaultHandler() {
    BeeBus.addBoolVch(this);
  }

  private void initProperty(String p) {
    if (!BeeUtils.isEmpty(p)) {
      setPropKey(p);
      setValue(BeeProperties.getBooleanProperty(p));
    }
  }

  private void initField(String fld) {
    if (!BeeUtils.isEmpty(fld)) {
      setFieldName(fld);
      setValue(BeeUtils.toBoolean(BeeGlobal.getFieldValue(fld)));
    }
  }

  private void updateProperty(boolean v) {
    String p = getPropKey();
    if (!BeeUtils.isEmpty(p))
      BeeProperties.setProperty(p, BeeUtils.toString(v));
  }

  private void updateField(boolean v) {
    if (!BeeUtils.isEmpty(getFieldName()))
      BeeGlobal.setFieldValue(getFieldName(), BeeUtils.toString(v));
  }

  private void setCaption(boolean v) {
    if (v) {
      if (!BeeUtils.isEmpty(checkedCaption))
        setText(checkedCaption);
    } else {
      if (!BeeUtils.isEmpty(uncheckedCaption))
        setText(uncheckedCaption);
    }
  }

  private void setCaption() {
    setCaption(getValue());
  }

}
