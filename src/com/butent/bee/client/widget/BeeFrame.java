package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Frame;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;

import elemental.js.dom.JsDocument;
import elemental.js.dom.JsElement;
import elemental.js.html.JsIFrameElement;
import elemental.js.html.JsWindow;

public class BeeFrame extends Frame implements HasId {

  public BeeFrame() {
    super();
    init();
  }

  public BeeFrame(String url) {
    super(url);
    init();
  }
  
  public void clear() {
    if (!isEmpty()) {
      setBodyHtml(BeeConst.STRING_EMPTY);
    }
  }

  public void focus() {
    getContentWindow().focus();
  }
  
  public Element getBody() {
    return getContentDocument().getBody().cast();
  }

  public String getId() {
    return DomUtils.getId(this);
  }
  
  public String getIdPrefix() {
    return "frame";
  }

  public boolean isEmpty() {
    if (getContentDocument() == null) {
      return true;
    }

    JsElement body = getContentDocument().getBody();
    return body == null || body.getChildElementCount() <= 0;
  }
  
  public void print() {
    getContentWindow().print();
  }
  
  public void setBodyHtml(String html) {
    getContentDocument().getBody().setInnerHTML(html);
  }

  public void setHtml(String html) {
    JsDocument document = getContentDocument();
    
    document.open();
    document.write(html);
    document.close();
  }
  
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private JsDocument getContentDocument() {
    return getIFrameElement().getContentDocument();
  }

  private JsWindow getContentWindow() {
    return getIFrameElement().getContentWindow(); 
  }
  
  private JsIFrameElement getIFrameElement() {
    return getElement().cast();
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Frame");
  }
}
