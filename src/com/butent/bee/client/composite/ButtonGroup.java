package com.butent.bee.client.composite;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.HasStage;
import com.butent.bee.shared.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Enables an interface element, consisting of few buttons grouped into one component.
 */

public class ButtonGroup extends Span implements HasService, HasStage {
  private List<BeeButton> buttons = new ArrayList<BeeButton>();

  public ButtonGroup() {
    super();
  }

  public ButtonGroup(String... p) {
    this();
    Assert.notNull(p);

    String svc;
    for (int i = 0; i < p.length; i += 2) {
      if (i < p.length - 1) {
        svc = p[i + 1];
      } else {
        svc = null;
      }
      addButton(p[i], svc);
    }
  }

  public void addButton(String cap) {
    add(new BeeButton(cap));
  }

  public void addButton(String cap, Stage bst) {
    add(new BeeButton(cap, bst));
  }

  public void addButton(String cap, String svc) {
    add(new BeeButton(cap, svc));
  }

  public void addButton(String cap, String svc, String stg) {
    add(new BeeButton(cap, svc, stg));
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "bg");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-ButtonGroup";
  }

  public String getService() {
    return DomUtils.getService(this);
  }

  public String getStage() {
    return DomUtils.getStage(this);
  }

  public void setService(String svc) {
    DomUtils.setService(this, svc);
  }

  public void setStage(String stg) {
    DomUtils.setStage(this, stg);
  }

  private void add(BeeButton b) {
    super.add(b);
    buttons.add(b);
  }

}
