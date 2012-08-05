package com.butent.bee.client.widget;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables using hyperlink user interface component with {@code_blank} parameter.
 */

public class Link extends Anchor implements HasId {

  private static final String DEFAULT_TARGET = "_blank";

  public Link() {
    super();
    init();
  }

  public Link(boolean useDefaultHref) {
    super(useDefaultHref);
    init();
  }

  public Link(SafeHtml html, String href, String target) {
    super(html, href, target);
    init();
  }

  public Link(SafeHtml html, String href) {
    super(html, href);
    init();
  }

  public Link(SafeHtml html) {
    super(html);
    init();
  }

  public Link(String text, boolean asHtml, String href, String target) {
    super(text, asHtml, href, target);
    init();
  }

  public Link(String text, boolean asHTML, String href) {
    super(text, asHTML, href);
    init();
  }

  public Link(String text, boolean asHtml) {
    super(text, asHtml);
    init();
  }

  public Link(String text, String href, String target) {
    super(text, href, target);
    init();
  }

  public Link(String text, String href) {
    super(text, href);
    init();
  }

  public Link(String text) {
    super(text);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "link";
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
    String href;

    if (BeeUtils.containsSame(value, sep)) {
      html = BeeUtils.getPrefix(value, sep);
      href = BeeUtils.getSuffix(value, sep);
    } else {
      html = value;
      href = value;
    }
    setHTML(BeeUtils.trim(html));
    setHref(BeeUtils.trim(href));
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Link");

    if (BeeUtils.isEmpty(getTarget())) {
      setTarget(DEFAULT_TARGET);
    }
  }
}
