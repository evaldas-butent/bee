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
  private final List<Style> styles = Lists.newArrayList();

  protected Element() {
    super();
    this.tag = NameUtils.getClassName(getClass()).toLowerCase();
  }

  public void addClassName(String clazz) {
    if (!BeeUtils.isEmpty(clazz) && !BeeUtils.containsSame(classes, clazz)) {
      classes.add(clazz.trim());
    }
  }

  public String getAttribute(String name) {
    Attribute attribute = findAttribute(name);
    return (attribute == null) ? null : attribute.getValue();
  }

  public String getData(String key) {
    return BeeUtils.isEmpty(key) ? null : getAttribute(Attribute.DATA_PREFIX + key.trim());
  }

  public String getId() {
    return getAttribute(Attribute.ID);
  }

  public String getStyle(String name) {
    Style style = findStyle(name);
    return (style == null) ? null : style.getValue();
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

  public boolean removeStyle(String name) {
    int index = BeeConst.UNDEF;

    for (int i = 0; i < styles.size(); i++) {
      if (BeeUtils.same(styles.get(i).getName(), name)) {
        index = i;
        break;
      }
    }

    if (BeeConst.isUndef(index)) {
      return false;
    } else {
      return styles.remove(index) != null;
    }
  }

  public void setAccessKey(String accessKey) {
    setAttribute(Attribute.ACCESSKEY, accessKey);
  }

  public void setAttribute(String name, boolean value) {
    if (!value) {
      removeAttribute(name);

    } else if (!BeeUtils.isEmpty(name)) {
      Attribute attribute = findAttribute(name);
      if (attribute == null) {
        attributes.add(new BooleanAttribute(name, value));
      } else {
        attribute.setValue(name);
      }
    }
  }

  public void setAttribute(String name, double value) {
    setAttribute(name, BeeUtils.toString(value));
  }

  public void setAttribute(String name, int value) {
    setAttribute(name, Integer.toString(value));
  }

  public void setAttribute(String name, String value) {
    if (value == null) {
      removeAttribute(name);

    } else if (!BeeUtils.isEmpty(name)) {
      Attribute attribute = findAttribute(name);
      if (attribute == null) {
        attributes.add(new Attribute(name, value));
      } else {
        attribute.setValue(value);
      }
    }
  }

  public void setClassName(String clazz) {
    classes.clear();
    addClassName(clazz);
  }

  public void setContentEditable(Boolean editable) {
    if (editable == null) {
      removeAttribute(Attribute.CONTENTEDITABLE);
    } else if (editable) {
      setAttribute(Attribute.CONTENTEDITABLE, Keywords.CONTENT_IS_EDITABLE);
    } else {
      setAttribute(Attribute.CONTENTEDITABLE, Keywords.CONTENT_NOT_EDITABLE);
    }
  }

  public void setContextMenu(String contextMenu) {
    setAttribute(Attribute.CONTEXTMENU, contextMenu);
  }

  public void setData(String key, String value) {
    if (!BeeUtils.isEmpty(key)) {
      setAttribute(Attribute.DATA_PREFIX + key.trim(), value);
    }
  }

  public void setDirAuto() {
    setAttribute(Attribute.DIR, Keywords.DIR_AUTO);
  }

  public void setDirLtr() {
    setAttribute(Attribute.DIR, Keywords.DIR_LTR);
  }

  public void setDirRtl() {
    setAttribute(Attribute.DIR, Keywords.DIR_RTL);
  }

  public void setDraggable(Boolean draggable) {
    if (draggable == null) {
      removeAttribute(Attribute.DRAGGABLE);
    } else if (draggable) {
      setAttribute(Attribute.DRAGGABLE, Keywords.IS_DRAGGABLE);
    } else {
      setAttribute(Attribute.DRAGGABLE, Keywords.NOT_DRAGGABLE);
    }
  }

  public void setDropZone(String dropZone) {
    setAttribute(Attribute.DROPZONE, dropZone);
  }

  public void setHidden(boolean hidden) {
    setAttribute(Attribute.HIDDEN, hidden);
  }

  public void setId(String id) {
    setAttribute(Attribute.ID, id);
  }

  public void setInert(boolean inert) {
    setAttribute(Attribute.INERT, inert);
  }

  public void setItemId(String itemId) {
    setAttribute(Attribute.ITEMID, itemId);
  }

  public void setItemProp(String itemProp) {
    setAttribute(Attribute.ITEMPROP, itemProp);
  }

  public void setItemRef(String itemRef) {
    setAttribute(Attribute.ITEMREF, itemRef);
  }

  public void setItemScope(boolean itemScope) {
    setAttribute(Attribute.ITEMSCOPE, itemScope);
  }

  public void setItemType(String itemType) {
    setAttribute(Attribute.ITEMTYPE, itemType);
  }

  public void setLang(String lang) {
    setAttribute(Attribute.LANG, lang);
  }

  public void setSpellCheck(Boolean spellCheck) {
    if (spellCheck == null) {
      removeAttribute(Attribute.SPELLCHECK);
    } else if (spellCheck) {
      setAttribute(Attribute.SPELLCHECK, Keywords.SPELL_CHECK_ENABLED);
    } else {
      setAttribute(Attribute.SPELLCHECK, Keywords.SPELL_CHECK_DISABLED);
    }
  }

  public void setStyle(String name, String value) {
    if (value == null) {
      removeStyle(name);

    } else if (!BeeUtils.isEmpty(name)) {
      Style style = findStyle(name);
      if (style == null) {
        styles.add(new Style(name, value));
      } else {
        style.setValue(value);
      }
    }
  }

  public void setTabIndex(int tabIndex) {
    setAttribute(Attribute.TABINDEX, BeeUtils.toString(tabIndex));
  }

  public void setTitle(String title) {
    setAttribute(Attribute.TITLE, title);
  }

  public void setTranslate(Boolean translate) {
    if (translate == null) {
      removeAttribute(Attribute.TRANSLATE);
    } else if (translate) {
      setAttribute(Attribute.TRANSLATE, Keywords.TRANSLATION_ENABLED);
    } else {
      setAttribute(Attribute.TRANSLATE, Keywords.TRANSLATION_DISABLED);
    }
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

    for (Attribute attribute : attributes) {
      sb.append(attribute.write());
    }

    if (!classes.isEmpty()) {
      Attribute cs = new Attribute(Attribute.CLASS, BeeUtils.join(BeeConst.STRING_SPACE, classes));
      sb.append(cs.write());
    }

    if (!styles.isEmpty()) {
      Attribute st = new Attribute(Attribute.STYLE, BeeUtils.join(BeeConst.STRING_SPACE, styles));
      sb.append(st.write());
    }

    sb.append(">");
    return sb.toString();
  }

  private Attribute findAttribute(String name) {
    for (Attribute attribute : attributes) {
      if (BeeUtils.same(attribute.getName(), name)) {
        return attribute;
      }
    }
    return null;
  }

  private Style findStyle(String name) {
    for (Style style : styles) {
      if (BeeUtils.same(style.getName(), name)) {
        return style;
      }
    }
    return null;
  }
}
