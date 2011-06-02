package com.butent.bee.client.ui;

import com.butent.bee.client.dom.Font;
import com.butent.bee.shared.ui.StyleDeclaration;

public class StyleDescriptor extends StyleDeclaration {
  
  public static StyleDescriptor copyOf(StyleDeclaration src) {
    if (src == null) {
      return null;
    }
    return new StyleDescriptor(src.getClassName(), src.getInline(), src.getFontDeclaration());
  }
  
  private Font font = null;

  public StyleDescriptor(String className) {
    super(className);
  }

  public StyleDescriptor(String className, String inline, String fontDeclaration) {
    super(className, inline, fontDeclaration);
  }

  public Font getFont() {
    return font;
  }

  @Override
  protected void setFontDeclaration(String fontDeclaration) {
    super.setFontDeclaration(fontDeclaration);
    setFont(Font.parse(fontDeclaration));
  }
  
  private void setFont(Font font) {
    this.font = font;
  }
}
