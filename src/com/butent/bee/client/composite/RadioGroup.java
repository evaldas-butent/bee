package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.widget.BeeRadioButton;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables to use a user interface component, consisting of a group of radio buttons.
 */
public class RadioGroup extends Span {
  
  public static int getValue(String name) {
    int v = BeeConst.SELECTION_UNKNOWN;
    if (BeeUtils.isEmpty(name)) {
      return v;
    }

    NodeList<Element> lst = DomUtils.getElementsByName(name);
    if (lst.getLength() <= 0) {
      return v;
    }

    Element el;
    InputElement inp;

    for (int i = 0; i < lst.getLength(); i++) {
      el = lst.getItem(i);
      if (!DomUtils.isInputElement(el)) {
        continue;
      }
      inp = InputElement.as(el);

      if (inp.isChecked()) {
        v = BeeUtils.toInt(inp.getValue());
        break;
      }
    }
    return v;
  }

  private final String name;
  private final boolean vertical;
  
  public RadioGroup(String name, boolean vertical) {
    super();
    Assert.notEmpty(name);
    this.name = name;
    this.vertical = vertical;
  }

  public RadioGroup(Variable var) {
    this(var, false);
  }

  public RadioGroup(Variable var, boolean vertical) {
    this(Global.getVarName(var), vertical);

    List<String> opt = var.getItems();

    String z = var.getValue();
    int value = BeeUtils.isEmpty(z) ? BeeConst.UNDEF : opt.indexOf(z);

    addButtons(opt, value);
  }

  public RadioGroup(String name, List<String> opt) {
    this(name, false, opt);
  }

  public RadioGroup(String name, boolean vertical, List<String> opt) {
    this(name, vertical, BeeConst.UNDEF, opt);
  }

  public RadioGroup(String name, int value, List<String> opt) {
    this(name, false, value, opt);
  }

  public RadioGroup(String name, boolean vertical, int value, List<String> opt) {
    this(name, vertical);
    addButtons(opt, value);
  }

  public RadioGroup(String name, Enum<?> value, Enum<?>[] values) {
    this(name, false, value, values);
  }

  public RadioGroup(String name, boolean vertical, Enum<?> value, Enum<?>[] values) {
    this(name, vertical);

    List<String> opt = Lists.newArrayList();
    for (int i = 0; i < values.length; i++) {
      opt.add(BeeUtils.proper(values[i].name(), BeeConst.CHAR_UNDER));
    }

    int z = (value == null) ? BeeConst.UNDEF : value.ordinal();
    addButtons(opt, z);
  }

  public void addOption(String label) {
    addOption(label, false, false);
  }

  public void addOption(String label, boolean asHtml) {
    addOption(label, asHtml, false);
  }
  
  public void addOption(String label, boolean asHtml, boolean selected) {
    Assert.notEmpty(label);
    int index = getWidgetCount();

    BeeRadioButton rb = new BeeRadioButton(getName(), label, asHtml);
    add(rb);
    
    rb.setFormValue(BeeUtils.toString(index));
    BeeKeeper.getBus().addBoolVch(rb);

    rb.addStyleDependentName(isVertical() ? StyleUtils.NAME_VERTICAL : StyleUtils.NAME_HORIZONTAL);
    if (selected) {
      rb.setValue(true);
    }
  }
  
  @Override
  public void createId() {
    DomUtils.createId(this, "rg");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-RadioGroup";
  }

  public String getName() {
    return name;
  }

  public boolean isVertical() {
    return vertical;
  }

  private void addButtons(List<String> opt, int value) {
    Assert.notNull(opt);

    int idx = 0;
    for (String s : opt) {
      addOption(s, false, idx++ == value);
    }
  }
}
