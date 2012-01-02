package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.FileUpload;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class InputFile extends FileUpload implements HasId {

  public InputFile() {
    super();
    init();
  }

  public InputFile(boolean multiple) {
    this();
    if (multiple) {
      getElement().setPropertyBoolean("multiple", true);
    }
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "upload";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
