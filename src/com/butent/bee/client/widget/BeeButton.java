package com.butent.bee.client.widget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.HasStage;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a push button user interface component.
 */

public class BeeButton extends Button implements HasId, HasService, HasStage, HasCommand {

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

  public BeeButton(String html, Stage bst) {
    this(html, bst.getService(), bst.getStage());
  }

  public BeeButton(String html, ClickHandler handler) {
    super(html, handler);
    init();
  }

  public BeeButton(String html, String svc) {
    this(html);
    if (!BeeUtils.isEmpty(svc)) {
      setService(svc);
    }
  }

  public BeeButton(String html, String svc, String stg) {
    this(html, svc);
    if (!BeeUtils.isEmpty(stg)) {
      setStage(stg);
    }
  }

  public Scheduler.ScheduledCommand getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "b";
  }

  public String getService() {
    return DomUtils.getService(this);
  }

  public String getStage() {
    return DomUtils.getStage(this);
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isClick(event)) {
      if (getCommand() != null) {
        getCommand().execute();
      }
      if (!BeeUtils.isEmpty(getService())) {
        BeeKeeper.getBus().dispatchService(getService(), getStage(), this);
      }
    }

    super.onBrowserEvent(event);
  }
  
  public void setCommand(Scheduler.ScheduledCommand command) {
    this.command = command;
    if (command != null) {
      initEvents();
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setService(String svc) {
    DomUtils.setService(this, svc);
    if (!BeeUtils.isEmpty(svc)) {
      initEvents();
    }
  }

  public void setStage(String stg) {
    DomUtils.setStage(this, stg);
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Button");
  }
  
  private void initEvents() {
    sinkEvents(Event.ONCLICK);
  }
}
