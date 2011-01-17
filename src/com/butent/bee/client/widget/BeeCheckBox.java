package com.butent.bee.client.widget;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasBeeValueChangeHandler;
import com.butent.bee.shared.HasBooleanValue;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

public class BeeCheckBox extends CheckBox implements HasId, HasBeeValueChangeHandler<Boolean> {
  private HasBooleanValue source = null;

  private String checkedCaption = null;
  private String uncheckedCaption = null;

  public BeeCheckBox() {
    super();
    init();
  }

  public BeeCheckBox(Element elem) {
    super(elem);
    init();
  }

  public BeeCheckBox(HasBooleanValue source, String label) {
    this();
    initSource(source);
    addDefaultHandler();

    if (!BeeUtils.isEmpty(label)) {
      setText(label);
    }
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

  public BeeCheckBox(Variable source) {
    this();
    initSource(source);
    addDefaultHandler();

    setText(source.getCaption());
  }

  public void createId() {
    DomUtils.createId(this, "c");
  }

  public String getCheckedCaption() {
    return checkedCaption;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public HasBooleanValue getSource() {
    return source;
  }

  public String getUncheckedCaption() {
    return uncheckedCaption;
  }

  public boolean onValueChange(Boolean v) {
    setCaption(v);
    updateSource(v);

    return true;
  }

  public void setCheckedCaption(String checkedCaption) {
    this.checkedCaption = checkedCaption;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSource(HasBooleanValue source) {
    this.source = source;
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

  private void initSource(HasBooleanValue src) {
    if (src != null) {
      setSource(src);
      setValue(src.getBoolean());
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

  private void updateSource(boolean v) {
    if (source != null) {
      source.setValue(v);
    }
  }
}
