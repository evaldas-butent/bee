package com.butent.bee.egg.client.composite;

import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.client.layout.BeeSpan;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.HasService;
import com.butent.bee.egg.shared.HasStage;

public class ButtonGroup extends BeeSpan implements HasService, HasStage {
  private List<BeeButton> buttons = new ArrayList<BeeButton>();

  public ButtonGroup() {
    super();
  }

  public ButtonGroup(String... p) {
    this();
    String svc;

    for (int i = 0; i < p.length; i += 2) {
      if (i < p.length - 1)
        svc = p[i + 1];
      else
        svc = null;

      addButton(p[i], svc);
    }
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

  public void addButton(String cap) {
    add(new BeeButton(cap));
  }

  public void addButton(String cap, String svc) {
    add(new BeeButton(cap, svc));
  }

  public void addButton(String cap, String svc, String stg) {
    add(new BeeButton(cap, svc, stg));
  }

  public void addButton(String cap, BeeStage bst) {
    add(new BeeButton(cap, bst));
  }

  @Override
  public void createId() {
    BeeDom.createId(this, "bg");
  }

  private void add(BeeButton b) {
    super.add(b);
    buttons.add(b);
  }

}
