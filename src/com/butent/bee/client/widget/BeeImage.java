package com.butent.bee.client.widget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.HasId;

/**
 * Implements an image holding user interface component, that displays the image at a given URL.
 */
public class BeeImage extends Image implements HasEnabled, HasCommand, HasId {

  private Scheduler.ScheduledCommand command = null;
  private boolean enabled = true;
  private String styleDisabled = null;

  public BeeImage() {
    super();
    init();
  }

  public BeeImage(Element element) {
    super(element);
    init();
  }

  public BeeImage(ImageResource resource) {
    super(resource);
    init();
  }

  public BeeImage(String url, int left, int top, int width, int height) {
    super(url, left, top, width, height);
    init();
  }

  public BeeImage(String url) {
    super(url);
    init();
  }

  public BeeImage(Scheduler.ScheduledCommand cmnd) {
    this();
    initCommand(cmnd);
  }

  public BeeImage(ImageResource resource, Scheduler.ScheduledCommand cmnd) {
    this(resource);
    initCommand(cmnd);
  }

  public BeeImage(ImageResource resource, Scheduler.ScheduledCommand cmnd, String styleDisabled) {
    this(resource);
    initCommand(cmnd);
    this.styleDisabled = styleDisabled;
  }

  public BeeImage(SafeUri url) {
    super(url);
    init();
  }

  public BeeImage(SafeUri url, Scheduler.ScheduledCommand cmnd) {
    this(url);
    initCommand(cmnd);
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
    return "img";
  }
  
  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (isEnabled()) {
      if (EventUtils.isClick(event) && getCommand() != null) {
        getCommand().execute();
      }
      super.onBrowserEvent(event);
    }
  }

  @Override
  public void setCommand(Scheduler.ScheduledCommand command) {
    this.command = command;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (this.enabled == enabled) {
      return;
    }
    this.enabled = enabled;

    if (styleDisabled != null) {
      if (enabled) {
        getElement().removeClassName(styleDisabled);
      } else {
        getElement().addClassName(styleDisabled);
      }
    }
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Image");
  }

  private void initCommand(Scheduler.ScheduledCommand cmnd) {
    if (cmnd != null) {
      setCommand(cmnd);
      sinkEvents(Event.ONCLICK);
    }
  }
}
