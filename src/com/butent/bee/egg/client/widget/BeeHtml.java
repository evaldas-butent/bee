package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;

public class BeeHtml extends HTML implements HasId {

  public BeeHtml() {
    super();
    createId();
  }

  public BeeHtml(Element element) {
    super(element);
    createId();
  }

  public BeeHtml(String html, boolean wordWrap) {
    super(html, wordWrap);
    createId();
  }

  public BeeHtml(String html) {
    super(html);
    createId();
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public void createId() {
    BeeDom.createId(this, "html");
  }

}
