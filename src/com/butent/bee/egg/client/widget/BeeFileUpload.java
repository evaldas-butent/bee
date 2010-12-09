package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FileUpload;

import com.butent.bee.egg.client.Global;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasBeeChangeHandler;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeFileUpload extends FileUpload implements HasId,
    HasBeeChangeHandler {
  private String fieldName = null;

  public BeeFileUpload() {
    super();
    createId();
    addDefaultHandlers();
  }

  public BeeFileUpload(Element element) {
    super(element);
    createId();
    addDefaultHandlers();
  }

  public BeeFileUpload(String fieldName) {
    this();
    this.fieldName = fieldName;
  }

  public void createId() {
    DomUtils.createId(this, "upload");
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public boolean onChange() {
    if (!BeeUtils.isEmpty(getFieldName())) {
      Global.setFieldValue(getFieldName(), getFilename());
    }
    return true;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addVch(this);
  }

}
