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

public class Style implements BeeSerializable, HasInfo {
  
  public static final String TAG_CLASS = "class";
  public static final String TAG_INLINE = "inline";
  public static final String TAG_FONT = "font";
  
  public static Style restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Style style = new Style();
    style.deserialize(s);
    return style;
  }
  
  private String className = null;
  private String inline = null;
  private String font = null;
  
  public Style(String className, String inline, String font) {
    this.className = className;
    this.inline = inline;
    this.font = font;
  }

  private Style() {
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, 3);
    
    setClassName(arr[0]);
    setInline(arr[1]);
    setFont(arr[2]);
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
    if (!BeeUtils.isEmpty(getFont())) {
      info.add(new Property("Font", getFont()));
    }
    return info;
  }

  public boolean isEmpty() {
    return BeeUtils.allEmpty(getClassName(), getInline(), getFont());
  }

  public String serialize() {
    return Codec.beeSerializeAll(getClassName(), getInline(), getFont());
  }

  private String getClassName() {
    return className;
  }

  private String getFont() {
    return font;
  }

  private String getInline() {
    return inline;
  }

  private void setClassName(String className) {
    this.className = className;
  }

  private void setFont(String font) {
    this.font = font;
  }
  
  private void setInline(String inline) {
    this.inline = inline;
  }
}
