package com.butent.bee.client.widget;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Hyperlink;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables using internal link user interface component.
 */

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

  public void update(String value) {
    update(value, BeeConst.DEFAULT_VALUE_SEPARATOR);
  }

  public void update(String value, Object separator) {
    Assert.notEmpty(value);
    String sep = BeeUtils.normSep(separator, BeeConst.DEFAULT_VALUE_SEPARATOR);
    String html;
    String token;

    if (BeeUtils.context(sep, value)) {
      html = BeeUtils.getPrefix(value, sep);
      token = BeeUtils.getSuffix(value, sep);
    } else {
      html = value;
      token = value;
    }
    setHTML(BeeUtils.trim(html));
    setTargetHistoryToken(BeeUtils.trim(token));
  }

  protected String getDefaultStyleName() {
    return "bee-InternalLink";
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName(getDefaultStyleName());
  }
}
