package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.BeeProperties;
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
    createId();
  }

  public BeeCheckBox(BeeName nm) {
    this();

    String fld = nm.getName();
    if (!BeeUtils.isEmpty(fld)) {
      setText(BeeGlobal.getFieldCaption(fld));
      initField(fld);
      addDefaultHandler();
    }
  }

  public BeeCheckBox(Element elem) {
    super(elem);
    createId();
  }

  public BeeCheckBox(Pair<String, String> caption) {
    this();

    setUncheckedCaption(caption.getA());
    setCheckedCaption(caption.getB());

    setCaption();
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

  public BeeCheckBox(String label) {
    super(label);
    createId();
  }

  public BeeCheckBox(String label, boolean asHTML) {
    super(label, asHTML);
    createId();
  }

  public BeeCheckBox(String label, String property) {
    this(label);

    initProperty(property);
    addDefaultHandler();
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
    updateProperty(v);
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

  private void initField(String fld) {
    if (!BeeUtils.isEmpty(fld)) {
      setFieldName(fld);
      setValue(BeeUtils.toBoolean(BeeGlobal.getFieldValue(fld)));
    }
  }

  private void initProperty(String p) {
    if (!BeeUtils.isEmpty(p)) {
      setPropKey(p);
      setValue(BeeProperties.getBooleanProperty(p));
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
      BeeGlobal.setFieldValue(getFieldName(), BeeUtils.toString(v));
    }
  }

  private void updateProperty(boolean v) {
    String p = getPropKey();
    if (!BeeUtils.isEmpty(p)) {
      BeeProperties.setProperty(p, BeeUtils.toString(v));
    }
  }

}
