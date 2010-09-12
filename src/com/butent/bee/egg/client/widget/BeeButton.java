package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.BeeBus;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.HasService;
import com.butent.bee.egg.shared.HasStage;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class BeeButton extends Button implements HasId, HasService, HasStage {

  public BeeButton() {
    super();
    createId();
  }

  public BeeButton(Element element) {
    super(element);
    createId();
  }

  public BeeButton(String html, ClickHandler handler) {
    super(html, handler);
    createId();
  }

  public BeeButton(String html) {
    super(html);
    createId();
  }

  public BeeButton(String html, String svc) {
    this(html);

    if (!BeeUtils.isEmpty(svc)) {
      setService(svc);
      BeeBus.addClickHandler(this);
    }
  }

  public BeeButton(String html, String svc, String stg) {
    this(html, svc);

    if (!BeeUtils.isEmpty(stg)) {
      setStage(stg);
    }
  }

  public BeeButton(String html, BeeStage bst) {
    this(html, bst.getService(), bst.getStage());
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public String getService() {
    return BeeDom.getService(this);
  }

  public void setService(String svc) {
    BeeDom.setService(this, svc);
  }

  public String getStage() {
    return BeeDom.getStage(this);
  }

  public void setStage(String stg) {
    BeeDom.setStage(this, stg);
  }

  public void createId() {
    BeeDom.createId(this, "b");
  }

}
