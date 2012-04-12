package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
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

  private Command command = null;
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

  public BeeImage(Command cmnd) {
    this();
    initCommand(cmnd);
  }

  public BeeImage(ImageResource resource, Command cmnd) {
    this(resource);
    initCommand(cmnd);
  }

  public BeeImage(ImageResource resource, Command cmnd, String styleDisabled) {
    this(resource);
    initCommand(cmnd);
    this.styleDisabled = styleDisabled;
  }

  public Command getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "img";
  }
  
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

  public void setCommand(Command command) {
    this.command = command;
  }

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

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Image");
  }

  private void initCommand(Command cmnd) {
    if (cmnd != null) {
      setCommand(cmnd);
      sinkEvents(Event.ONCLICK);
    }
  }
}
