package com.butent.bee.egg.client.composite;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.layout.BeeSpan;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.widget.BeeRadioButton;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeField;
import com.butent.bee.egg.shared.HasService;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class RadioGroup extends BeeSpan implements HasService {
  public static int getValue(String name) {
    int v = BeeConst.SELECTION_UNKNOWN;
    if (BeeUtils.isEmpty(name)) {
      return v;
    }

    NodeList<Element> lst = BeeDom.getElementsByName(name);
    if (lst.getLength() <= 0) {
      return v;
    }

    Element el;
    InputElement inp;

    for (int i = 0; i < lst.getLength(); i++) {
      el = lst.getItem(i);
      if (!BeeDom.isInputElement(el)) {
        continue;
      }
      inp = InputElement.as(el);

      if (inp.isChecked()) {
        v = inp.getTabIndex();
        break;
      }
    }

    return v;
  }

  public RadioGroup() {
    super();
  }

  public RadioGroup(String fieldName) {
    this();

    BeeField fld = BeeGlobal.getField(fieldName);
    String v = fld.getValue();
    List<String> opt = fld.getItems();

    int value;

    if (BeeUtils.isEmpty(v)) {
      value = -1;
    } else {
      value = opt.indexOf(v);
    }

    addButtons(fieldName, value, opt.toArray(new String[0]));
  }

  public RadioGroup(String name, String... opt) {
    this();
    addButtons(name, -1, opt);
  }

  @Override
  public void createId() {
    BeeDom.createId(this, "rg");
  }

  public String getService() {
    return BeeDom.getService(this);
  }

  public void setService(String svc) {
    BeeDom.setService(this, svc);
  }

  private void addButtons(String name, int value, String... opt) {
    BeeRadioButton rb;
    int idx = 0;

    for (String s : opt) {
      if (BeeUtils.isEmpty(s)) {
        continue;
      }

      rb = new BeeRadioButton(name, s);
      add(rb);

      rb.setTabIndex(idx);
      BeeKeeper.getBus().addBoolVch(rb);

      if (idx == value) {
        rb.setValue(true);
        rb.addStyleName(BeeStyle.RADIO_BUTTON_SELECTED);
      }

      idx++;
    }
  }

}
