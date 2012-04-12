package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a user interface component that can contain arbitrary HTML code.
 */

public class Html extends HTML implements HasId, HasCommand {

  private Command command = null;

  public Html() {
    super();
    init();
  }

  public Html(Element element) {
    super(element);
    if (BeeUtils.isEmpty(element.getId())) {
      DomUtils.createId(this, getIdPrefix());
    }
  }

  public Html(String html) {
    this();
    if (!BeeUtils.isEmpty(html)) {
      setHTML(html);
    }
  }

  public Html(String html, Command cmnd) {
    this(html);

    if (cmnd != null) {
      setCommand(cmnd);
    }
  }

  public Html(String html, boolean wordWrap) {
    super(html, wordWrap);
    init();
  }

  public Command getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return DomUtils.HTML_ID_PREFIX;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isClick(event) && getCommand() != null) {
      getCommand().execute();
    }
    super.onBrowserEvent(event);
  }

  public void setCommand(Command command) {
    this.command = command;
    if (command != null) {
      sinkEvents(Event.ONCLICK);
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Html");
  }
}
