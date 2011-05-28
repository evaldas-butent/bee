package com.butent.bee.client.widget;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TextArea;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasAfterSaveHandler;
import com.butent.bee.client.event.HasBeeValueChangeHandler;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a text area that allows multiple lines of text to be entered.
 */

public class InputArea extends TextArea implements Editor, HasBeeValueChangeHandler<String>, HasAfterSaveHandler {

  private HasStringValue source = null;

  private BeeResource resource = null;
  
  private String digest = null;

  private boolean nullable = true;
  
  public InputArea() {
    super();
    init();
  }

  public InputArea(BeeResource resource) {
    this();
    this.resource = resource;

    setValue(resource.getContent());
    if (resource.isReadOnly()) {
      setReadOnly(true);
    }
  }

  public InputArea(Element element) {
    super(element);
    init();
  }

  public InputArea(HasStringValue source) {
    this();
    setSource(source);

    String v = source.getString();
    if (!BeeUtils.isEmpty(v)) {
      setValue(v);
    }
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
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

  public String getNormalizedValue() {
    String v = getValue();
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    } else {
      return BeeUtils.trimRight(v);
    }
  }

  public BeeResource getResource() {
    return resource;
  }
  
  public HasStringValue getSource() {
    return source;
  }

  public boolean isNullable() {
    return nullable;
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

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
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

  public void startEdit(String oldValue, char charCode) {
    if (Character.isLetterOrDigit(charCode) && BeeUtils.length(oldValue) < 20) {
      setValue(BeeUtils.toString(charCode));
    } else {
      String v = BeeUtils.trimRight(oldValue);
      setValue(v);
      if (!BeeUtils.isEmpty(v)) {
        setSelectionRange(0, v.length());
      }
    }
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

  public String validate() {
    return null;
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addStringVch(this);
  }

  private void init() {
    setStyleName("bee-InputArea");
    createId();
    addDefaultHandlers();
  }
}
