package com.butent.bee.client.widget;

public class InlineInternalLink extends InternalLink {

  public InlineInternalLink(String html) {
    this(html, null);
  }

  public InlineInternalLink(String html, String targetHistoryToken) {
    super(html, targetHistoryToken, null);
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
