package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.utils.HasCommand;
import com.butent.bee.egg.shared.HasId;

public class BeeImage extends Image implements HasId, HasCommand {
  private BeeCommand command = null;

  public BeeImage() {
    super();
    createId();
  }

  public BeeImage(Element element) {
    super(element);
    createId();
  }

  public BeeImage(ImageResource resource) {
    super(resource);
    createId();
  }

  public BeeImage(String url, int left, int top, int width, int height) {
    super(url, left, top, width, height);
    createId();
  }

  public BeeImage(String url) {
    super(url);
    createId();
  }

  public BeeImage(BeeCommand cmnd) {
    this();
    init(cmnd);
  }

  public BeeImage(ImageResource resource, BeeCommand cmnd) {
    this(resource);
    init(cmnd);
  }
  
  public void createId() {
    DomUtils.createId(this, "img");
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
  
  private void init(BeeCommand cmnd) {
    if (cmnd != null) {
      setCommand(cmnd);
      BeeKeeper.getBus().addClickHandler(this);
    }
  }
  
}
