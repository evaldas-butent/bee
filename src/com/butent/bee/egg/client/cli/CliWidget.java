package com.butent.bee.egg.client.cli;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.widget.BeeTextBox;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class CliWidget extends BeeTextBox {
  public CliWidget() {
    super();
  }

  public CliWidget(Element element) {
    super(element);
  }

  @Override
  public boolean onBeeKey(KeyPressEvent event) {
    if (BeeUtils.isEmpty(getValue())) {
      return true;
    }

    String v = getValue().trim();
    String[] arr = BeeUtils.split(v, BeeConst.STRING_SPACE);
    Assert.notEmpty(arr);
    
    String z = arr[0].toLowerCase();
    
    if (z.equals("?")) {
      Worker.whereAmI();
    } else if (z.equals("clear")) {
      Worker.clearLog();
    } else if (BeeUtils.inList(z, "center", "east", "north", "south", "screen", "west")) {
      Worker.doScreen(arr);
    } else if (z.equals("eval")) {
      Worker.eval(v, arr);
    } else if (BeeUtils.inList(z, "f", "func")) {
      Worker.showFunctions(v, arr);
    } else if (z.equals("fields")) {
      Worker.showFields(arr);
    } else if (BeeUtils.inList(z, "file", "dir", "get")) {
      Worker.getResource(arr);
    } else if (z.equals("fs")) {
      Worker.getFs();
    } else if (z.equals("gwt")) {
      Worker.showGwt();
    } else if (z.equals("id")) {
      Worker.showElement(v, arr);
    } else if (z.equals("menu")) {
      Worker.showMenu();
    } else if (BeeUtils.inList(z, "p", "prop")) {
      Worker.showProperties(v, arr);
    } else if (z.equals("rpc")) {
      Worker.showRpc();
    } else if (z.equals("stack")) {
      Worker.showStack();

    } else {
      BeeGlobal.showDialog("wtf", v);
    }

    return true;
  }

}
