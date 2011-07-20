package com.butent.bee.client.widget;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Hyperlink;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class InternalLink extends Hyperlink implements HasId {

  public InternalLink(SafeHtml html, String targetHistoryToken) {
    super(html, targetHistoryToken);
    init();
  }

  public InternalLink(String text, boolean asHTML, String targetHistoryToken) {
    super(text, asHTML, targetHistoryToken);
    init();
  }

  public InternalLink(String text, String targetHistoryToken) {
    super(text, targetHistoryToken);
    init();
  }

  protected InternalLink(Element elem) {
    super(elem);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "internal-link";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  protected String getDefaultStyleName() {
    return "bee-InternalLink";
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName(getDefaultStyleName());
  }
}
