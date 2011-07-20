package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a user interface component that can contain arbitrary HTML code.
 */

public class Html extends HTML implements HasId, HasCommand {

  private BeeCommand command = null;

  public Html() {
    super();
    init();
  }

  public Html(Element element) {
    super(element);
    if (element == null || BeeUtils.isEmpty(element.getId())) {
      DomUtils.createId(this, getIdPrefix());
    }
  }

  public Html(String html) {
    super(html);
    init();
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
    init();
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return DomUtils.HTML_ID_PREFIX;
  }

  public void setCommand(BeeCommand command) {
    this.command = command;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Html");
  }
}
