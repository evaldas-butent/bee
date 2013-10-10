package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;

public class Menuitem extends Element {

  private static final String TYPE_CHECK_BOX = "checkbox";
  private static final String TYPE_COMMAND = "command";
  private static final String TYPE_RADIO = "radior";

  public Menuitem() {
    super();
  }

  public Menuitem addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Menuitem checked() {
    setAttribute(Attribute.CHECKED, true);
    return this;
  }

  public Menuitem command(String value) {
    setAttribute(Attribute.COMMAND, value);
    return this;
  }

  

  public Menuitem disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Menuitem enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Menuitem htmlDefault() {
    setAttribute(Attribute.DEFAULT, true);
    return this;
  }

  public Menuitem icon(String value) {
    setAttribute(Attribute.ICON, value);
    return this;
  }

  public Menuitem id(String value) {
    setId(value);
    return this;
  }

  public Menuitem label(String value) {
    setAttribute(Attribute.LABEL, value);
    return this;
  }

  public Menuitem lang(String value) {
    setLang(value);
    return this;
  }

  public Menuitem radioGroup(String value) {
    setAttribute(Attribute.RADIOGROUP, value);
    return this;
  }

  public Menuitem title(String value) {
    setTitle(value);
    return this;
  }

  public Menuitem typeCheckBox() {
    setAttribute(Attribute.TYPE, TYPE_CHECK_BOX);
    return this;
  }

  public Menuitem typeCommand() {
    setAttribute(Attribute.TYPE, TYPE_COMMAND);
    return this;
  }

  public Menuitem typeRadio() {
    setAttribute(Attribute.TYPE, TYPE_RADIO);
    return this;
  }
}
