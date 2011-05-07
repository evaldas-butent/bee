package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TextArea;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasAfterSaveHandler;
import com.butent.bee.client.event.HasBeeKeyHandler;
import com.butent.bee.client.event.HasBeeValueChangeHandler;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a text box that allows multiple lines of text to be entered.
 */

public class BeeTextArea extends TextArea implements HasId, HasBeeKeyHandler,
    HasBeeValueChangeHandler<String>, HasAfterSaveHandler {
  private HasStringValue source = null;
  private BeeResource resource = null;
  private String digest = null;

  public BeeTextArea() {
    super();
    init();
  }

  public BeeTextArea(BeeResource resource) {
    this();
    this.resource = resource;

    setValue(resource.getContent());
    if (resource.isReadOnly()) {
      setReadOnly(true);
    }
  }

  public BeeTextArea(Element element) {
    super(element);
    init();
  }

  public BeeTextArea(HasStringValue source) {
    this();
    setSource(source);

    String v = source.getString();
    if (!BeeUtils.isEmpty(v)) {
      setValue(v);
    }
  }

  public void createId() {
    DomUtils.createId(this, "area");
  }

  public String getDigest() {
    return digest;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public BeeResource getResource() {
    return resource;
  }

  public HasStringValue getSource() {
    return source;
  }

  public boolean isValueChanged() {
    String v = getValue();
    String d = getDigest();

    if (BeeUtils.isEmpty(v)) {
      return !BeeUtils.isEmpty(d);
    } else if (BeeUtils.isEmpty(d)) {
      return true;
    } else {
      return !d.equals(JsUtils.md5(v));
    }
  }

  public void onAfterSave(String opt) {
    if (BeeUtils.isEmpty(opt)) {
      updateDigest();
    } else {
      setDigest(opt);
    }
  }

  public boolean onBeeKey(KeyPressEvent event) {
    return true;
  }

  public boolean onValueChange(String value) {
    if (getSource() != null) {
      getSource().setValue(value);
    }

    return true;
  }

  public void setDigest(String digest) {
    this.digest = digest;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setResource(BeeResource resource) {
    this.resource = resource;
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }

  @Override
  public void setValue(String value) {
    super.setValue(value);
    updateDigest(getValue());
  }

  public String updateDigest() {
    return updateDigest(getValue());
  }

  public String updateDigest(String value) {
    if (BeeUtils.isEmpty(value)) {
      setDigest(null);
    } else {
      setDigest(JsUtils.md5(value));
    }

    return getDigest();
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addKeyHandler(this);
    BeeKeeper.getBus().addStringVch(this);
  }

  private void init() {
    setStyleName("bee-TextArea");
    createId();
    addDefaultHandlers();
  }
}
