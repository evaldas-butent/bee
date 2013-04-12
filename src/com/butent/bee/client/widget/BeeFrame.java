package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Frame;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;

import elemental.html.StyleElement;

import elemental.html.HeadElement;

import elemental.html.Window;

import elemental.dom.Document;

import elemental.html.IFrameElement;

import elemental.js.dom.JsElement;
import elemental.js.html.JsIFrameElement;

public class BeeFrame extends Frame implements IdentifiableWidget {

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
    return ((JsElement) getContentDocument().getBody()).cast();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }
  
  @Override
  public String getIdPrefix() {
    return "frame";
  }
  
  public void injectStyleSheet(String css) {
    Assert.notEmpty(css);
    
    Document document = getContentDocument();
    Assert.notNull(document);
    HeadElement head = document.getHead();
    Assert.notNull(head);
    
    StyleElement style = document.createStyleElement();
    style.setType("text/css");
    style.setInnerText(css);
    
    head.appendChild(style);
  }

  public boolean isEmpty() {
    if (getContentDocument() == null) {
      return true;
    }

    Element body = getBody();
    return body == null || body.getChildCount() <= 0;
  }
  
  public void print() {
    getContentWindow().print();
  }
  
  public void setBodyHtml(String html) {
    getContentDocument().getBody().setInnerHTML(html);
  }

  public void setHtml(String html) {
    Document document = getContentDocument();
    
    document.open();
    document.write(html);
    document.close();
  }
  
  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private Document getContentDocument() {
    return getIFrameElement().getContentDocument();
  }

  private Window getContentWindow() {
    return getIFrameElement().getContentWindow(); 
  }
  
  private IFrameElement getIFrameElement() {
    return (JsIFrameElement) getElement().cast();
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Frame");
  }
}
