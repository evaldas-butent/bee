package com.butent.bee.client.widget;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.font.FontAwesome;

public class FaLabel extends Label {

  public FaLabel(FontAwesome fa) {
    super();
    setChar(fa);
  }

  public FaLabel(FontAwesome fa, boolean inline) {
    super(inline);
    setChar(fa);
  }
  
  @Override
  public String getIdPrefix() {
    return "fa";
  }

  public void setChar(FontAwesome fa) {
    if (fa == null) {
      clear();
    } else {
      getElement().setInnerText(String.valueOf(fa.getCode()));
    }
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-fa-label";
  }

  @Override
  protected void init() {
    super.init();
    StyleUtils.setFontFamily(this, FontAwesome.FAMILY);
  }
}
