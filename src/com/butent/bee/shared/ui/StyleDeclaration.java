package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class StyleDeclaration implements BeeSerializable, HasInfo {
  
  public static final String TAG_CLASS = "class";
  public static final String TAG_INLINE = "inline";
  public static final String TAG_FONT = "font";
  
  public static StyleDeclaration restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    StyleDeclaration style = new StyleDeclaration();
    style.deserialize(s);
    return style;
  }
  
  private String className = null;
  private String inline = null;
  private String fontDeclaration = null;
  
  public StyleDeclaration(String className) {
    this(className, null, null);
  }

  public StyleDeclaration(String className, String inline, String fontDeclaration) {
    this.className = className;
    this.inline = inline;
    setFontDeclaration(fontDeclaration);
  }

  private StyleDeclaration() {
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, 3);
    
    setClassName(arr[0]);
    setInline(arr[1]);
    setFontDeclaration(arr[2]);
  }

  public String getClassName() {
    return className;
  }

  public String getFontDeclaration() {
    return fontDeclaration;
  }

  public List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();
    
    if (isEmpty()) {
      PropertyUtils.addWhenEmpty(info, getClass());
      return info;
    }
    
    if (!BeeUtils.isEmpty(getClassName())) {
      info.add(new Property("Class Name", getClassName()));
    }
    if (!BeeUtils.isEmpty(getInline())) {
      info.add(new Property("Inline", getInline()));
    }
    if (!BeeUtils.isEmpty(getFontDeclaration())) {
      info.add(new Property("Font Declaration", getFontDeclaration()));
    }
    return info;
  }

  public String getInline() {
    return inline;
  }

  public boolean isEmpty() {
    return BeeUtils.allEmpty(getClassName(), getInline(), getFontDeclaration());
  }

  public String serialize() {
    return Codec.beeSerializeAll(getClassName(), getInline(), getFontDeclaration());
  }

  protected void setFontDeclaration(String fontDeclaration) {
    this.fontDeclaration = fontDeclaration;
  }

  private void setClassName(String className) {
    this.className = className;
  }
  
  private void setInline(String inline) {
    this.inline = inline;
  }
}
