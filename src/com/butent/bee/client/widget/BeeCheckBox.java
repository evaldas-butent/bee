package com.butent.bee.client.widget;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.HasBooleanValue;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a standard check box user interface component.
 */

public class BeeCheckBox extends CheckBox implements BooleanWidget {

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

    if (!BeeUtils.isEmpty(label)) {
      setText(label);
    }
  }

  public BeeCheckBox(Pair<String, String> caption) {
    this();

    setUncheckedCaption(caption.getA());
    setCheckedCaption(caption.getB());

    setCaption();
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

    setText(source.getCaption());
  }

  public String getCheckedCaption() {
    return checkedCaption;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "cb";
  }

  public HasBooleanValue getSource() {
    return source;
  }

  public String getUncheckedCaption() {
    return uncheckedCaption;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isChange(event.getType())) {
      Boolean v = BeeUtils.unbox(getValue());
      setCaption(v);
      updateSource(v);
    }

    super.onBrowserEvent(event);
  }

  public void setCheckedCaption(String checkedCaption) {
    this.checkedCaption = checkedCaption;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSource(HasBooleanValue source) {
    this.source = source;
  }

  public void setUncheckedCaption(String uncheckedCaption) {
    this.uncheckedCaption = uncheckedCaption;
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-CheckBox");
    sinkEvents(Event.ONCHANGE);
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
