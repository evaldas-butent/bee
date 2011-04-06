package com.butent.bee.client.composite;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.widget.BeeRadioButton;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RadioGroup extends Span implements HasService {
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

  public RadioGroup() {
    super();
  }

  public RadioGroup(Variable var) {
    this(var, false);
  }
  
  public RadioGroup(Variable var, boolean vertical) {
    this();

    String z = var.getValue();
    List<String> opt = var.getItems();

    int value;

    if (BeeUtils.isEmpty(z)) {
      value = -1;
    } else {
      value = opt.indexOf(z);
    }

    addButtons(Global.getVarName(var), vertical, value, opt.toArray(new String[0]));
  }

  public RadioGroup(String name, String... opt) {
    this(name, false, -1, opt);
  }

  public RadioGroup(String name, boolean vertical, String... opt) {
    this(name, vertical, -1, opt);
  }
  
  public RadioGroup(String name, int value, String... opt) {
    this(name, false, value, opt);
  }
  
  public RadioGroup(String name, boolean vertical, int value, String... opt) {
    this();
    addButtons(name, vertical, value, opt);
  }

  public RadioGroup(String name, Enum<?> value, Enum<?>[] values) {
    this(name, false, value, values);
  }
  
  public RadioGroup(String name, boolean vertical, Enum<?> value, Enum<?>[] values) {
    this();
    
    String[] opt = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      opt[i] = BeeUtils.proper(values[i].name(), BeeConst.CHAR_UNDER);
    }
    
    int z = (value == null) ? -1 : value.ordinal();
    addButtons(name, vertical, z, opt);
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "rg");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-RadioGroup";
  }

  public String getService() {
    return DomUtils.getService(this);
  }

  public void setService(String svc) {
    DomUtils.setService(this, svc);
  }

  private void addButtons(String name, boolean vertical, int value, String... opt) {
    BeeRadioButton rb;
    int idx = 0;

    for (String s : opt) {
      if (BeeUtils.isEmpty(s)) {
        continue;
      }

      rb = new BeeRadioButton(name, s);
      add(rb);

      rb.setFormValue(BeeUtils.toString(idx));
      BeeKeeper.getBus().addBoolVch(rb);
      
      rb.addStyleDependentName(vertical ? StyleUtils.NAME_VERTICAL : StyleUtils.NAME_HORIZONTAL);

      if (idx == value) {
        rb.setValue(true);
      }
      idx++;
    }
  }
}
