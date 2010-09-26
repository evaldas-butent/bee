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
      CliWorker.whereAmI();
    } else if (z.equals("clear")) {
      CliWorker.clearLog();
    } else if (BeeUtils.inList(z, "center", "east", "north", "south", "screen", "west")) {
      CliWorker.doScreen(arr);
    } else if (z.equals("charset")) {
      CliWorker.getCharsets();
    } else if (BeeUtils.inList(z, "dir", "file", "get")) {
      CliWorker.getResource(arr);
    } else if (z.equals("eval")) {
      CliWorker.eval(v, arr);
    } else if (BeeUtils.inList(z, "f", "func")) {
      CliWorker.showFunctions(v, arr);
    } else if (z.equals("fields")) {
      CliWorker.showFields(arr);
    } else if (z.equals("fs")) {
      CliWorker.getFs();
    } else if (z.equals("gwt")) {
      CliWorker.showGwt();
    } else if (z.equals("id")) {
      CliWorker.showElement(v, arr);
    } else if (z.equals("menu")) {
      CliWorker.doMenu(arr);
    } else if (z.equals("md5")) {
      CliWorker.digest(v);
    } else if (BeeUtils.inList(z, "p", "prop")) {
      CliWorker.showProperties(v, arr);
    } else if (z.equals("rpc")) {
      CliWorker.showRpc();
    } else if (z.equals("stack")) {
      CliWorker.showStack();
    } else if (z.equals("style")) {
      CliWorker.style(v, arr);

    } else {
      BeeGlobal.showDialog("wtf", v);
    }

    return true;
  }

}
