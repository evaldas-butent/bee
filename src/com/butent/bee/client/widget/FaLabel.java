package com.butent.bee.client.widget;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.font.FontAwesome;

public class FaLabel extends Label {

  public FaLabel(char ch) {
    super();
    setChar(ch);
  }

  public FaLabel(char ch, boolean inline) {
    super(inline);
    setChar(ch);
  }
  
  @Override
  public String getIdPrefix() {
    return "fa";
  }

  public void setChar(char ch) {
    getElement().setInnerText(String.valueOf(ch));
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
