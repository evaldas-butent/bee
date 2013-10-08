package com.butent.bee.shared.html.builder;

public class Attribute {

  public static final String ACCESS_KEY = "accesskey";
  public static final String CLASS = "class";
  public static final String CONTENT_EDITABLE = "contenteditable";
  public static final String CONTEXT_MENU = "contextmenu";
  public static final String DATA_PREFIX = "data-";
  public static final String DIR = "dir";
  public static final String DRAGGABLE = "draggable";
  public static final String DROP_ZONE = "dropzone";
  public static final String HIDDEN = "hidden";
  public static final String ID = "id";
  public static final String LANG = "lang";
  public static final String SPELL_CHECK = "spellcheck";
  public static final String STYLE = "style";
  public static final String TAB_INDEX = "tabindex";
  public static final String TITLE = "title";
  public static final String TRANSLATE = "translate";

  private String name;
  private String value;

  public Attribute(String name) {
    this.name = name;
    this.value = null;
  }

  public Attribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return write();
  }

  public String write() {
    StringBuilder sb = new StringBuilder(" ");
    sb.append(name);

    if (value != null) {
      sb.append("=\"");
      sb.append(value);
      sb.append("\"");
    }

    return sb.toString();
  }
}
