package com.butent.bee.egg.client.cli;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.widget.BeeTextBox;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;

public class CliWidget extends BeeTextBox {

  public CliWidget() {
    super();
  }

  public CliWidget(Element element) {
    super(element);
  }

  @Override
  public boolean onBeeKey(KeyPressEvent event) {
    String v = getValue();

    if (v.equals("fields"))
      BeeGlobal.showFields();
    else
      BeeGlobal.showDialog(v);

    return true;
  }

}
