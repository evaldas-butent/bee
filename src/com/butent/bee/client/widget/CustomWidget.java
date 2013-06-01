package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.utils.BeeUtils;

public class CustomWidget extends Widget implements IdentifiableWidget, HasClickHandlers, HasHTML {

  public CustomWidget(Element element) {
    super();
    setElement(element);
    init();
  }

  public CustomWidget(Element element, String styleName) {
    this(element);
    if (!BeeUtils.isEmpty(styleName)) {
      setStyleName(styleName);
    }
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }
  
  @Override
  public String getHTML() {
    return getElement().getInnerHTML();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }
  
  @Override
  public String getIdPrefix() {
    return "custom";
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setHTML(String html) {
    getElement().setInnerHTML(html);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }

  protected void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
