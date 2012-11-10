package com.butent.bee.client.widget;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

public class InternalLink extends Widget implements HasId, HasHTML, HasClickHandlers {

  private static final String DEFAULT_HREF = "javascript:;";
  
  private final Element anchorElem = DOM.createAnchor();

  private String targetHistoryToken = null;

  public InternalLink(String html) {
    this(html, null);
  }

  public InternalLink(String html, String targetHistoryToken) {
    this(html, targetHistoryToken, null);
  }
  
  public InternalLink(String html, String targetHistoryToken, Element elem) {
    if (elem == null) {
      setElement(anchorElem);
    } else {
      setElement(elem);
      DOM.appendChild(getElement(), anchorElem);
    }

    if (!BeeUtils.isEmpty(html)) {
      setHTML(html);
    }
    setTargetHistoryToken(targetHistoryToken);

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

  public String getTargetHistoryToken() {
    return targetHistoryToken;
  }

  @Override
  public String getText() {
    return anchorElem.getInnerText();
  }
  
  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if (EventUtils.isClick(event) && !BeeUtils.isEmpty(getTargetHistoryToken())) {
      History.newItem(getTargetHistoryToken());
      event.preventDefault();
    }
  }

  @Override
  public void setHTML(String html) {
    anchorElem.setInnerHTML(html);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setTargetHistoryToken(String targetHistoryToken) {
    this.targetHistoryToken = targetHistoryToken;

    String href = BeeUtils.isEmpty(targetHistoryToken) ?
        DEFAULT_HREF : BeeUtils.trim(targetHistoryToken);
    AnchorElement.as(anchorElem).setHref(href);
  }

  @Override
  public void setText(String text) {
    anchorElem.setInnerText(text);
  }

  public void update(String value) {
    update(value, BeeConst.DEFAULT_VALUE_SEPARATOR);
  }

  public void update(String value, String separator) {
    if (BeeUtils.isEmpty(value)) {
      return;
    }

    String sep = BeeUtils.notEmpty(separator, BeeConst.DEFAULT_VALUE_SEPARATOR);
    String html;
    String token;

    if (BeeUtils.containsSame(value, sep)) {
      html = BeeUtils.getPrefix(value, sep);
      token = BeeUtils.getSuffix(value, sep);
    } else {
      html = value;
      token = null;
    }
    
    if (!BeeUtils.isEmpty(html)) {
      setHTML(BeeUtils.trim(html));
    }
    if (!BeeUtils.isEmpty(token)) {
      setTargetHistoryToken(BeeUtils.trim(token));
    }
  }

  protected String getDefaultStyleName() {
    return "bee-InternalLink";
  }
}
