package com.butent.bee.client.widget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.HasCommand;

/**
 * Implements a push button user interface component.
 */

public class BeeButton extends Button implements IdentifiableWidget, HasCommand {

  private Scheduler.ScheduledCommand command = null;

  public BeeButton() {
    super();
    init();
  }

  public BeeButton(Element element) {
    super(element);
    init();
  }

  public BeeButton(String html) {
    super(html);
    init();
  }

  public BeeButton(String html, Scheduler.ScheduledCommand cmnd) {
    this(html);
    setCommand(cmnd);
  }

  public BeeButton(String html, ClickHandler handler) {
    super(html, handler);
    init();
  }

  @Override
  public Scheduler.ScheduledCommand getCommand() {
    return command;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "b";
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isClick(event)) {
      if (getCommand() != null) {
        getCommand().execute();
      }
    }

    super.onBrowserEvent(event);
  }
  
  @Override
  public void setCommand(Scheduler.ScheduledCommand command) {
    this.command = command;
    if (command != null) {
      initEvents();
    }
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Button");
  }
  
  private void initEvents() {
    sinkEvents(Event.ONCLICK);
  }
}
