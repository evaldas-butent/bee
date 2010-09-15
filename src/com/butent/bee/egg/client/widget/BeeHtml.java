package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.utils.HasCommand;
import com.butent.bee.egg.shared.HasId;

public class BeeHtml extends HTML implements HasId, HasCommand {
  private BeeCommand command = null;

  public BeeHtml() {
    super();
    createId();
  }

  public BeeHtml(Element element) {
    super(element);
    createId();
  }

  public BeeHtml(String html) {
    super(html);
    createId();
  }

  public BeeHtml(String html, BeeCommand cmnd) {
    this(html);

    if (cmnd != null) {
      setCommand(cmnd);
      BeeKeeper.getBus().addClickHandler(this);
    }
  }

  public BeeHtml(String html, boolean wordWrap) {
    super(html, wordWrap);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, BeeDom.HTML_ID_PREFIX);
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setCommand(BeeCommand command) {
    this.command = command;
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
