package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

public class BeeLabel extends Label implements HasId {

  public BeeLabel() {
    super();
    createId();
  }

  public BeeLabel(Element element) {
    super(element);
    createId();
  }

  public BeeLabel(String text, boolean wordWrap) {
    super(text, wordWrap);
    createId();
  }

  public BeeLabel(String text) {
    super(text);
    createId();
  }

  public BeeLabel(Object obj) {
    this(BeeUtils.transform(obj));
  }

  public void createId() {
    BeeDom.createId(this, "l");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
