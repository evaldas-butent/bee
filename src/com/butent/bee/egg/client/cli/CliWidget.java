package com.butent.bee.egg.client.cli;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.widget.BeeTextBox;
import com.butent.bee.egg.shared.utils.BeeUtils;

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

    if (BeeUtils.same(v, "fields")) {
      BeeGlobal.showFields();
    }  
    else if (BeeUtils.same(v, "clear")) {
      BeeKeeper.getLog().clear();
    }
    else {
      BeeGlobal.showDialog(v);
    }

    return true;
  }

}
