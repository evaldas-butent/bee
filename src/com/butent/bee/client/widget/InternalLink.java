package com.butent.bee.client.widget;

import com.butent.bee.shared.utils.BeeUtils;

public class InternalLink extends Html {

  public InternalLink(String html) {
    super();
    if (!BeeUtils.isEmpty(html)) {
      setHTML(html);
    }
  }

  @Override
  public String getIdPrefix() {
    return "internal-link";
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InternalLink";
  }
}
