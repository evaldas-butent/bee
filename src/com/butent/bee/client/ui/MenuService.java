package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map.Entry;

/**
 * Implements asynchronous creation of menus.
 */
public class MenuService extends CompositeService {

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

  @Override
  protected boolean doStage(String stg, Object... params) {
    boolean ok = true;

    String rl = Global.getVarValue(MenuConstants.varMenuLayout(0));
    String il = Global.getVarValue(MenuConstants.varMenuLayout(1));

    if (MenuConstants.isValidLayout(rl) && MenuConstants.isValidLayout(il)) {
      BeeKeeper.getRpc().makePostRequest(Service.GET_MENU,
          XmlUtils.createString(Service.XML_TAG_DATA, "menu_name", "rootMenu",
              "root_layout", getLayout(rl), "item_layout", getLayout(il)),
          new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              Assert.notNull(response);

              if (response.hasResponse()) {
                UiComponent c = UiComponent.restore((String) response.getResponse());
                BeeKeeper.getScreen().updateActivePanel(buidComponentTree(c));
                BeeKeeper.getScreen().updateMenu((Widget) c.createInstance());
              }
            }
          });
    } else {
      Global.showError("Menu layouts not valid", rl, il);
      ok = false;
    }
    destroy();
    return ok;
  }

  @Override
  protected CompositeService getInstance() {
    return new MenuService();
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
}
