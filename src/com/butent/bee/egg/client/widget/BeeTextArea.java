package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.TextArea;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasBeeKeyHandler;
import com.butent.bee.egg.client.event.HasBeeValueChangeHandler;
import com.butent.bee.egg.shared.BeeResource;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeTextArea extends TextArea implements HasId, HasBeeKeyHandler,
    HasBeeValueChangeHandler<String> {

  private String fieldName = null;
  private BeeResource resource = null;

  public BeeTextArea() {
    super();
    init();
  }

  public BeeTextArea(Element element) {
    super(element);
    init();
  }

  public BeeTextArea(String fieldName) {
    this();
    this.fieldName = fieldName;

    String v = BeeGlobal.getFieldValue(fieldName);
    if (!BeeUtils.isEmpty(v)) {
      setValue(v);
    }
  }

  public BeeTextArea(BeeResource resource) {
    this();
    this.resource = resource;
    
    setValue(resource.getContent());
  }

  public void createId() {
    DomUtils.createId(this, "area");
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public BeeResource getResource() {
    return resource;
  }

  public boolean onBeeKey(KeyPressEvent event) {
    return true;
  }

  public boolean onValueChange(String value) {
    if (!BeeUtils.isEmpty(getFieldName())) {
      BeeGlobal.setFieldValue(getFieldName(), value);
    }

    return true;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setResource(BeeResource resource) {
    this.resource = resource;
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addKeyHandler(this);
    BeeKeeper.getBus().addStringVch(this);
  }

  private void init() {
    createId();
    addDefaultHandlers();
  }
  
}
