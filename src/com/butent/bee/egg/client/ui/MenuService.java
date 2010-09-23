package com.butent.bee.egg.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.tree.BeeTree;
import com.butent.bee.egg.client.tree.BeeTreeItem;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.client.widget.BeeListBox;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.menu.MenuConst;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map.Entry;

public class MenuService extends CompositeService {

  private enum Stages {
    REQUEST_MENU, SHOW_MENU
  };

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

  public MenuService() {
  }

  public MenuService(String serviceId) {
    super(serviceId);
    nextStage();
  }

  @Override
  public CompositeService createInstance(String serviceId) {
    Assert.notEmpty(serviceId);

    return new MenuService(serviceId);
  }

  @Override
  public boolean doService(Object... params) {
    Assert.notNull(stage);
    boolean ok = true;

    switch (stage) {
      case REQUEST_MENU:
        String rl = BeeGlobal.getFieldValue(MenuConst.fieldMenuLayout(0));
        String il = BeeGlobal.getFieldValue(MenuConst.fieldMenuLayout(1));

        if (MenuConst.isValidLayout(rl) && MenuConst.isValidLayout(il)) {
          BeeKeeper.getRpc().makePostRequest(
              appendId("rpc_ui_menu"),
              BeeXml.createString(BeeService.XML_TAG_DATA, "menu_name",
                  "rootMenu", "root_layout", getLayout(rl), "item_layout",
                  getLayout(il)));
        } else {
          BeeGlobal.showError("Menu layouts not valid", rl, il);
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
        BeeGlobal.showError("Unhandled stage: " + stage);
        ok = false;
        break;
    }

    if (ok) {
      nextStage();
    } else {
      BeeGlobal.unregisterService(serviceId);
    }
    return ok;
  }

  private String getLayout(String layout) {
    String l = "UiMenuHorizontal";

    if (BeeUtils.same(layout, MenuConst.LAYOUT_MENU_VERT)) {
      l = "UiMenuVertical";
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_STACK)) {
      l = "UiStack";
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_LIST)) {
      l = "UiListBox";
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TREE)) {
      l = "UiTree";
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TAB)) {
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
      BeeGlobal.unregisterService(serviceId);
    }
  }

  private void showMenu(UiComponent c) {
    Widget root = buidComponentTree(c);
    BeeKeeper.getUi().updateActivePanel(root);
  }
}
