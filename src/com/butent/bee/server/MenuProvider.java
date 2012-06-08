package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Manages rpc_load_menu service request, creates logical structure of application menu.
 */

@Singleton
@Startup
@DependsOn("SystemBean")
@Lock(LockType.READ)
public class MenuProvider {

  private static final String NOT_AVAIL = "menu not available";
  private static Logger logger = Logger.getLogger(MenuProvider.class.getName());

  @EJB
  UserServiceBean usr;

  private String resource = "menu.xml";
  private String transformation = null;

  private MenuEntry[] menu = null;

  public MenuEntry[] getMenu() {
    return menu;
  }

  public void getMenu(RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notNull(reqInfo);
    Assert.notNull(buff);

    String mode = reqInfo.getParameter(0);

    if (!BeeUtils.isEmpty(mode)) {
      if (BeeUtils.inListSame(mode, BeeConst.STRING_MINUS, BeeConst.STRING_ZERO,
          BeeConst.STRING_FALSE)) {
        reload(false);
      } else {
        reload();
      }
    }

    if (BeeUtils.isEmpty(getMenu())) {
      LogUtils.severe(logger, NOT_AVAIL);
      buff.addSevere(NOT_AVAIL);
      return;
    }

    List<MenuEntry> lst = new ArrayList<MenuEntry>();
    int rootCnt = 0;

    for (MenuEntry entry : getMenu()) {
      if (entry.isValid() && isVisible(entry) && isParentVisible(entry)) {
        lst.add(entry);
        if (isRoot(entry)) {
          rootCnt++;
        }
      }
    }

    if (lst.isEmpty()) {
      String msg = "no visible menu items found";
      LogUtils.severe(logger, msg);
      buff.addSevere(msg);
      return;
    }
    if (rootCnt == 0) {
      String msg = "no root menu items found";
      LogUtils.severe(logger, msg);
      buff.addSevere(msg);
      return;
    }

    if (lst.size() > 1) {
      Collections.sort(lst, MenuConstants.MENU_COMPARATOR);
    }
    for (MenuEntry entry : lst) {
      buff.add(entry.serialize());
    }
  }

  public String getResource() {
    return resource;
  }

  public String getTransformation() {
    return transformation;
  }

  public boolean loadXml(String src, String xsl) {
    Assert.notEmpty(src);
    long start = System.currentTimeMillis();
    boolean ok = false;

    String xmlPath = Config.getPath(src);
    if (xmlPath == null) {
      return ok;
    }

    String xslPath = null;
    if (!BeeUtils.isEmpty(xsl)) {
      xslPath = Config.getPath(xsl);
    }

    Property[][] arr = XmlUtils.getAttributesFromFile(xmlPath, xslPath, "item");
    if (arr == null) {
      return ok;
    }

    int r = arr.length;
    menu = new MenuEntry[r];

    String name, value;
    int cnt = 0;

    for (int i = 0; i < r; i++) {
      menu[i] = new MenuEntry();

      if (arr[i] == null) {
        LogUtils.warning(logger, "menu item", i, "not initialized");
        continue;
      }

      for (Property attr : arr[i]) {
        name = attr.getName();
        value = attr.getValue();

        if (!BeeUtils.allNotEmpty(name, value)) {
          continue;
        }

        if (BeeUtils.same(name, "id")) {
          menu[i].setId(value);
        } else if (BeeUtils.same(name, "parent")) {
          menu[i].setParent(value);
        } else if (BeeUtils.same(name, "order")) {
          menu[i].setOrder(BeeUtils.toInt(value));
        } else if (BeeUtils.same(name, "separators")) {
          menu[i].setSeparators(MenuConstants.SEPARATOR_BEFORE);
        } else if (BeeUtils.same(name, "text")) {
          menu[i].setText(value);
        } else if (BeeUtils.same(name, "service")) {
          menu[i].setService(value);
        } else if (BeeUtils.same(name, "parameters")) {
          menu[i].setParameters(value);
        } else if (BeeUtils.same(name, "keyname")) {
          menu[i].setKeyName(value);
        } else if (BeeUtils.same(name, "type")) {
          menu[i].setType(value);
        } else if (BeeUtils.same(name, "style")) {
          menu[i].setStyle(value);
        } else if (BeeUtils.same(name, "visible")) {
          menu[i].setVisible(BeeUtils.toBoolean(value));
        }
      }

      if (menu[i].isValid()) {
        cnt++;
      }
    }

    if (cnt == 0) {
      LogUtils.severe(logger, "no valid menu items", BeeUtils.bracket(r),
          "found in", xmlPath, xslPath);
    } else {
      LogUtils.infoNow(logger, "loaded", r, "menu items from", xmlPath,
          xslPath, BeeUtils.elapsedSeconds(start));
      if (cnt < r) {
        LogUtils.warning(logger, "only", cnt, "from", r, "menu items are valid");
      }
    }

    ok = (cnt > 0);

    return ok;
  }

  public void reload() {
    reload(true);
  }

  public void reload(boolean withTransform) {
    if (withTransform) {
      loadXml(resource, transformation);
    } else {
      loadXml(resource, null);
    }
  }

  public void reload(String src) {
    loadXml(src, transformation);
  }

  public void reload(String src, String xsl) {
    loadXml(src, xsl);
  }

  public void setMenu(MenuEntry[] menu) {
    this.menu = menu;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public void setTransformation(String transformation) {
    this.transformation = transformation;
  }

  private MenuEntry getEntry(String id) {
    for (MenuEntry entry : getMenu()) {
      if (entry.getId().equals(id)) {
        return entry;
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    loadXml(resource, transformation);
  }

  private boolean isParentVisible(MenuEntry entry) {
    String p = entry.getParent();

    if (BeeUtils.isEmpty(p)) {
      return true;
    } else {
      return isVisible(getEntry(p));
    }
  }

  private boolean isRoot(MenuEntry entry) {
    return BeeUtils.isEmpty(entry.getParent());
  }

  private boolean isVisible(MenuEntry entry) {
    MenuEntry item = getEntry(entry.getParent());
    String ref = entry.getId();

    while (item != null) {
      ref = item.getId() + "." + ref;
      item = getEntry(item.getParent());
    }
    return usr.hasMenuRight(ref, "Visible");
  }
}
