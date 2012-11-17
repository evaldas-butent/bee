package com.butent.bee.client.widget;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.utils.BeeUtils;

public class InternalLink extends Widget implements IdentifiableWidget, HasHTML, HasClickHandlers {

  private static final String DEFAULT_HREF = "javascript:;";
  
  private final Element anchorElem = DOM.createAnchor();

  public InternalLink(String html) {
    this(html, null);
  }

  public InternalLink(String html, Element elem) {
    if (elem == null) {
      setElement(anchorElem);
    } else {
      setElement(elem);
      DOM.appendChild(getElement(), anchorElem);
    }

    if (!BeeUtils.isEmpty(html)) {
      setHTML(html);
    }
    setHref(DEFAULT_HREF);

    sinkEvents(Event.ONCLICK);
    setStyleName(getDefaultStyleName());
    DomUtils.createId(this, getIdPrefix());
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addHandler(handler, ClickEvent.getType());
  }

  @Override
  public String getHTML() {
    return anchorElem.getInnerHTML();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "internal-link";
  }

  @Override
  public String getText() {
    return anchorElem.getInnerText();
  }
  
  public void setHref(String href) {
    AnchorElement.as(anchorElem).setHref(BeeUtils.notEmpty(href, DEFAULT_HREF));
  }

  @Override
  public void setHTML(String html) {
    anchorElem.setInnerHTML(html);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setText(String text) {
    anchorElem.setInnerText(text);
  }

  protected String getDefaultStyleName() {
    return "bee-InternalLink";
  }
}
