package com.butent.bee.client.layout;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTMLPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Manages a panel with HTML capabilities.
 */

public class HtmlPanel extends HTMLPanel implements HasId {

  public HtmlPanel(SafeHtml safeHtml) {
    super(safeHtml);
    init();
  }

  public HtmlPanel(String tag, String html) {
    super(tag, html);
    init();
  }

  public HtmlPanel(String html) {
    super(html);
    init();
  }

  public void createId() {
    DomUtils.createId(this, "html-panel");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    createId();
  }
}
