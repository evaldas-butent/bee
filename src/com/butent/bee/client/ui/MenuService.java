package com.butent.bee.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map.Entry;

class MenuService extends CompositeService {

  private enum Stages {
    REQUEST_MENU, SHOW_MENU
  }

  public static Widget buidComponentTree(UiComponent c) {
    BeeTree root = new BeeTree();
    BeeTreeItem item = new BeeTreeItem(c.getId());
    fillBranch(item, c);

    root.addItem(item);

    return root;
  }

  private static void fillBranch(BeeTreeItem item, UiComponent c) {
    item.addItem("Class = " + c.getClass().getName());
    item.addItem("Caption = " + c.getCaption());

    if (!BeeUtils.isEmpty(c.getProperties())) {
      BeeTreeItem prp = new BeeTreeItem("Properties");
      BeeListBox lst = new BeeListBox();

      for (Entry<String, String> entry : c.getProperties().entrySet()) {
        lst.addItem(entry.getKey() + " = " + entry.getValue());
      }
      lst.setAllVisible();
      prp.addItem(lst);
      item.addItem(prp);
    }

    if (c.hasChilds()) {
      BeeTreeItem cc = new BeeTreeItem("Childs");

      for (UiComponent chld : c.getChilds()) {
        BeeTreeItem itm = new BeeTreeItem(chld.getId());
        fillBranch(itm, chld);
        cc.addItem(itm);
      }
      item.addItem(cc);
    }
  }

  private Stages stage = null;

  protected MenuService(String... serviceId) {
    super(serviceId);
    nextStage();
  }

  @Override
  protected CompositeService create(String svcId) {
    return new MenuService(self(), svcId);
  }

  @Override
  protected boolean doStage(Object... params) {
    Assert.notNull(stage);
    boolean ok = true;

    switch (stage) {
      case REQUEST_MENU:
        String rl = Global.getVarValue(MenuConstants.varMenuLayout(0));
        String il = Global.getVarValue(MenuConstants.varMenuLayout(1));

        if (MenuConstants.isValidLayout(rl) && MenuConstants.isValidLayout(il)) {
          BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_menu"),
              XmlUtils.createString(BeeService.XML_TAG_DATA, "menu_name", "rootMenu", "root_layout",
                  getLayout(rl), "item_layout", getLayout(il)));
        } else {
          Global.showError("Menu layouts not valid", rl, il);
          ok = false;
        }
        break;

      case SHOW_MENU:
        JsArrayString fArr = (JsArrayString) params[0];
        UiComponent c = UiComponent.restore(fArr.get(0));

        showMenu(c);
        BeeKeeper.getUi().updateMenu((Widget) c.createInstance());
        break;

      default:
        Global.showError("Unhandled stage: " + stage);
        ok = false;
        break;
    }

    if (ok) {
      nextStage();
    } else {
      unregister();
    }
    return ok;
  }

  private String getLayout(String layout) {
    String l = "UiMenuHorizontal";

    if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_VERT)) {
      l = "UiMenuVertical";
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_STACK)) {
      l = "UiStack";
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_LIST)) {
      l = "UiListBox";
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
      l = "UiTree";
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TAB)) {
      l = "UiTab";
    }
    return l;
  }

  private void nextStage() {
    int x = 0;

    if (!BeeUtils.isEmpty(stage)) {
      x = stage.ordinal() + 1;
    }

    if (x < Stages.values().length) {
      stage = Stages.values()[x];
    } else {
      unregister();
    }
  }

  private void showMenu(UiComponent c) {
    Widget root = buidComponentTree(c);
    BeeKeeper.getUi().updateActivePanel(root);
  }
}
