package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

public class CustomWidget extends Widget implements IdentifiableWidget {

  public CustomWidget(Element element) {
    super();
    setElement(element);
    init();
  }

  public CustomWidget(Element element, String style) {
    this(element);
    setStyleName(style);
  }
  
  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "custom";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
