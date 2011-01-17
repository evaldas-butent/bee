package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FileUpload;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasBeeChangeHandler;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.HasStringValue;

public class BeeFileUpload extends FileUpload implements HasId, HasBeeChangeHandler {
  private HasStringValue source = null;

  public BeeFileUpload() {
    super();
    init();
  }

  public BeeFileUpload(Element element) {
    super(element);
    init();
  }

  public BeeFileUpload(HasStringValue source) {
    this();
    this.source = source;
  }

  public void createId() {
    DomUtils.createId(this, "upload");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public HasStringValue getSource() {
    return source;
  }

  public boolean onChange() {
    if (getSource() != null) {
      getSource().setValue(getFilename());
    }
    return true;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }
  
  private void addDefaultHandlers() {
    BeeKeeper.getBus().addVch(this);
  }

  private void init() {
    createId();
    addDefaultHandlers();
  }
}
