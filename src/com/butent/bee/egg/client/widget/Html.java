package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.utils.HasCommand;
import com.butent.bee.egg.shared.HasId;

public class Html extends HTML implements HasId, HasCommand {
  private BeeCommand command = null;

  public Html() {
    super();
    createId();
  }

  public Html(Element element) {
    super(element);
    createId();
  }

  public Html(String html) {
    super(html);
    createId();
  }

  public Html(String html, BeeCommand cmnd) {
    this(html);

    if (cmnd != null) {
      setCommand(cmnd);
      BeeKeeper.getBus().addClickHandler(this);
    }
  }

  public Html(String html, boolean wordWrap) {
    super(html, wordWrap);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, DomUtils.HTML_ID_PREFIX);
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setCommand(BeeCommand command) {
    this.command = command;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
