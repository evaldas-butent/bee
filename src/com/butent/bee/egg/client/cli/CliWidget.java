package com.butent.bee.egg.client.cli;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.communication.RpcList;
import com.butent.bee.egg.client.utils.BeeJs;
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
    } else if (BeeUtils.same(v, "clear")) {
      BeeKeeper.getLog().clear();
    } else if (BeeUtils.same(v, "stack")) {
      BeeKeeper.getLog().stack();

    } else if (BeeUtils.startsSame(v, "eval")) {
      String xpr = v.trim().substring("eval".length()).trim();
      if (!BeeUtils.isEmpty(xpr)) {
        BeeGlobal.showDialog(xpr, BeeJs.eval(xpr));
      }

    } else if (BeeUtils.same(v, "rpc")) {
      if (BeeKeeper.getRpc().getRpcList().isEmpty()) {
        BeeGlobal.showDialog("RpcList empty");
      } else {
        BeeKeeper.getUi().updateActivePanel(
            BeeGlobal.createSimpleGrid(RpcList.DEFAULT_INFO_COLUMNS, BeeKeeper
                .getRpc().getRpcList().getDefaultInfo()));
      }

    } else if (BeeUtils.same(v, "menu")) {
      BeeKeeper.getMenu().showMenu();

    } else {
      BeeGlobal.showDialog(v);
    }

    return true;
  }

}
