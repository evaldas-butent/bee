package com.butent.bee.client.ui;

import com.google.gwt.safecss.shared.SafeStyles;

import com.butent.bee.client.dom.Font;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.shared.ui.StyleDeclaration;
import com.butent.bee.shared.utils.BeeUtils;

public class StyleDescriptor extends StyleDeclaration {
  
  public static StyleDescriptor copyOf(StyleDeclaration src) {
    if (src == null) {
      return null;
    }
    return new StyleDescriptor(src.getClassName(), src.getInline(), src.getFontDeclaration());
  }
  
  private Font font = null;
  private SafeStyles safeStyles = null;

  public StyleDescriptor(String className) {
    super(className);
  }

  public StyleDescriptor(String className, String inline, String fontDeclaration) {
    super(className, inline, fontDeclaration);
  }

  public Font getFont() {
    return font;
  }
  
  public SafeStyles getSafeStyles() {
    return safeStyles;
  }

  @Override
  public void setFontDeclaration(String fontDeclaration) {
    super.setFontDeclaration(fontDeclaration);
    setFont(Font.parse(fontDeclaration));
  }
  
  @Override
  protected void setInline(String inline) {
    super.setInline(inline);
    if (BeeUtils.isEmpty(inline)) {
      setSafeStyles(null);
    } else {
      setSafeStyles(StyleUtils.toSafeStyles(inline));
    }
  }

  private void setFont(Font font) {
    this.font = font;
  }

  private void setSafeStyles(SafeStyles safeStyles) {
    this.safeStyles = safeStyles;
  }
}
