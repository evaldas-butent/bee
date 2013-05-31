package com.butent.bee.client.widget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a user interface component that can contain arbitrary HTML code.
 */

public class Html extends CustomWidget implements HasCommand {

  private Scheduler.ScheduledCommand command = null;

  public Html() {
    super(Document.get().createDivElement());
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

  public Html(String html, Scheduler.ScheduledCommand cmnd) {
    this(html);

    if (cmnd != null) {
      setCommand(cmnd);
    }
  }

  @Override
  public Scheduler.ScheduledCommand getCommand() {
    return command;
  }

  @Override
  public String getIdPrefix() {
    return "html";
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isClick(event) && getCommand() != null) {
      getCommand().execute();
    }
    super.onBrowserEvent(event);
  }

  @Override
  public void setCommand(Scheduler.ScheduledCommand command) {
    this.command = command;
    if (command != null) {
      sinkEvents(Event.ONCLICK);
    }
  }

  @Override
  protected void init() {
    super.init();
    setStyleName("bee-Html");
  }
}
