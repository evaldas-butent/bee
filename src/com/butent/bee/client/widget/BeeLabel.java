package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements standard label user interface component.
 */

public class BeeLabel extends Label implements HasId {

  public BeeLabel() {
    super();
    init();
  }

  public BeeLabel(Element element) {
    super(element);
    init();
  }

  public BeeLabel(Object obj) {
    this(BeeUtils.transform(obj));
  }

  public BeeLabel(String text) {
    this();
    setText(text);
    init();
  }

  public BeeLabel(HorizontalAlignmentConstant align) {
    this();
    setHorizontalAlignment(align);
  }

  public BeeLabel(String text, boolean wordWrap) {
    this(text);
    setWordWrap(wordWrap);
    init();
  }

  public BeeLabel(String text, HorizontalAlignmentConstant align) {
    this(text);
    setHorizontalAlignment(align);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return DomUtils.LABEL_ID_PREFIX;
  }
  
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Label");
  }
}
