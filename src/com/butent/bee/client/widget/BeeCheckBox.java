package com.butent.bee.client.widget;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a standard check box user interface component.
 */

public class BeeCheckBox extends CheckBox implements BooleanWidget {

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

  public String getUncheckedCaption() {
    return uncheckedCaption;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isChange(event.getType())) {
      Boolean v = BeeUtils.unbox(getValue());
      setCaption(v);
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

  public void setUncheckedCaption(String uncheckedCaption) {
    this.uncheckedCaption = uncheckedCaption;
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-CheckBox");
    sinkEvents(Event.ONCHANGE);
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
}
