package com.butent.bee.shared.html.builder;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class Element extends Node {

  private final String tag;

  private final List<Attribute> attributes = Lists.newArrayList();
  private final List<String> classes = Lists.newArrayList();
  
  protected Element() {
    this.tag = NameUtils.getClassName(getClass()).toLowerCase();
  }

  protected Element(String tag) {
    this.tag = tag;
  }
  
  public void addClassName(String clazz) {
    if (!BeeUtils.isEmpty(clazz) && !BeeUtils.containsSame(classes, clazz)) {
      classes.add(clazz.trim());
    }
  }

  public void data(String key, String value) {
    setAttribute(Attribute.DATA_PREFIX + key.trim(), value);
  }

  public String getAttribute(String name) {
    for (Attribute attribute : attributes) {
      if (BeeUtils.same(attribute.getName(), name)) {
        return attribute.getValue();
      }
    }
    return null;
  }

  public String getId() {
    return getAttribute(Attribute.ID);
  }

  public String getStyle() {
    return getAttribute(Attribute.STYLE);
  }

  public String getTag() {
    return tag;
  }

  public String getTitle() {
    return getAttribute(Attribute.TITLE);
  }
  
  public boolean removeAttribute(String name) {
    int index = BeeConst.UNDEF;

    for (int i = 0; i < attributes.size(); i++) {
      if (BeeUtils.same(attributes.get(i).getName(), name)) {
        index = i;
        break;
      }
    }

    if (BeeConst.isUndef(index)) {
      return false;
    } else {
      return attributes.remove(index) != null;
    }
  }

  public void setAccessKey(String accessKey) {
    setAttribute(Attribute.ACCESSKEY, accessKey);
  }

  public void setAttribute(String name, String value) {
    if (value == null) {
      removeAttribute(name);
    } else if (!BeeUtils.isEmpty(name)) {
      for (Attribute attribute : attributes) {
        if (BeeUtils.same(attribute.getName(), name)) {
          attribute.setValue(value);
          return;
        }
      }

      attributes.add(new Attribute(name, value));
    }
  }
  

  public void setClassName(String clazz) {
    classes.clear();
    addClassName(clazz);
  }

  public void setDir(String dir) {
    setAttribute(Attribute.DIR, dir);
  }

  public void setId(String id) {
    setAttribute(Attribute.ID, id);
  }

  public void setLang(String lang) {
    setAttribute(Attribute.LANG, lang);
  }
  
  public void setStyle(String style) {
    setAttribute(Attribute.STYLE, style);
  }

  public void setTabIndex(int tabIndex) {
    setAttribute(Attribute.TABINDEX, BeeUtils.toString(tabIndex));
  }

  public void setTitle(String title) {
    setAttribute(Attribute.TITLE, title);
  }
  
  @Override
  protected String write() {
    StringBuilder sb = new StringBuilder(writeOpen());
    sb.append(writeClose());
    return sb.toString();
  }
  
  protected String writeClose() {
    StringBuilder sb = new StringBuilder("</");
    sb.append(tag);
    sb.append(">");
    return sb.toString();
  }

  protected String writeOpen() {
    StringBuilder sb = new StringBuilder("<");
    sb.append(tag);
    
    if (!classes.isEmpty()) {
      Attribute cs = new Attribute(Attribute.CLASS, BeeUtils.join(BeeConst.STRING_SPACE, classes));
      sb.append(cs.write());
    }

    for (Attribute attr : attributes) {
      sb.append(attr.write());
    }

    sb.append(">");
    return sb.toString();
  }
}
