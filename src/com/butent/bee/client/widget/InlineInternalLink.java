package com.butent.bee.client.widget;

import com.google.gwt.safehtml.shared.SafeHtml;

public class InlineInternalLink extends InternalLink {

  public InlineInternalLink(SafeHtml html, String targetHistoryToken) {
    this(html.asString(), true, targetHistoryToken);
  }

  public InlineInternalLink(String text, boolean asHTML, String targetHistoryToken) {
    this();
    directionalTextHelper.setTextOrHtml(text, asHTML);
    setTargetHistoryToken(targetHistoryToken);
  }

  public InlineInternalLink(String text, String targetHistoryToken) {
    this(text, false, targetHistoryToken);
  }

  private InlineInternalLink() {
    super(null);
  }

  @Override
  public String getIdPrefix() {
    return "inline-internal";
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InlineInternalLink";
  }
}
