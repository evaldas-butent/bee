package com.butent.bee.egg.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.menu.MenuConst;
import com.butent.bee.egg.shared.menu.MenuEntry;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.butent.bee.egg.shared.utils.StringProp;

@Singleton
@Startup
@Lock(LockType.READ)
public class MenuBean {
  private static String NOT_AVAIL = "menu not available";
  private static Logger logger = Logger.getLogger(MenuBean.class.getName());
  private MenuEntry[] menu = null;

  public MenuEntry[] getMenu() {
    return menu;
  }

  public void setMenu(MenuEntry[] menu) {
    this.menu = menu;
  }

  public boolean loadXml(String fileName) {
    Assert.notEmpty(fileName);
    long start = System.currentTimeMillis();
    boolean ok = false;

    URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
    if (url == null) {
      LogUtils.warning(logger, "resource", fileName, "not found");
      return ok;
    }
    
    StringProp[][] arr = XmlUtils.getAttributesFromFile(url.getFile(), "menu");
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
      
      for (StringProp attr : arr[i]) {
        name = attr.getName();
        value = attr.getValue();
        
        if (! BeeUtils.allNotEmpty(name, value)) {
          continue;
        }
        
        if (BeeUtils.same(name, "id")) {
          menu[i].setId(value);
        }
        else if (BeeUtils.same(name, "parent")) {
          menu[i].setParent(value);
        }
        else if (BeeUtils.same(name, "order")) {
          menu[i].setOrder(BeeUtils.toInt(value));
        }
        else if (BeeUtils.same(name, "separators")) {
          menu[i].setSeparators(MenuConst.SEPARATOR_BEFORE);
        }
        else if (BeeUtils.same(name, "text")) {
          menu[i].setText(value);
        }
        else if (BeeUtils.same(name, "service")) {
          menu[i].setService(value);
        }
        else if (BeeUtils.same(name, "parameters")) {
          menu[i].setParameters(value);
        }
        else if (BeeUtils.same(name, "keyname")) {
          menu[i].setKeyName(value);
        }
        else if (BeeUtils.same(name, "type")) {
          menu[i].setType(value);
        }
        else if (BeeUtils.same(name, "style")) {
          menu[i].setStyle(value);
        }
        else if (BeeUtils.same(name, "visible")) {
          menu[i].setVisible(BeeUtils.toBoolean(value));
        }
      }
      
      if (menu[i].isValid()) {
        cnt++;
      }
    }
    
    if (cnt == 0) {
      LogUtils.severe(logger, "no valid menu items", BeeUtils.bracket(r), "found in", url);
    } else {
      LogUtils.info(logger, "loaded", r, "menu items from", url, BeeUtils.elapsedSeconds(start));
      if (cnt < r) {
        LogUtils.warning(logger, "only", cnt, "from", r, "menu items are valid");
      }
    }
    
    ok = (cnt > 0);
    
    return ok;
  }
  
  public void getMenu(ResponseBuffer buff) {
    Assert.notNull(buff);
    
    if (BeeUtils.isEmpty(getMenu())) {
      LogUtils.severe(logger, NOT_AVAIL);
      buff.addSevere(NOT_AVAIL);
      return;
    }
    
    List<MenuEntry> lst = new ArrayList<MenuEntry>();
    int rootCnt = 0;
    
    for (MenuEntry entry : getMenu()) {
      if (entry.isValid() && entry.isVisible() && isParentVisible(entry)) {
        lst.add(entry);
        if (isRoot(entry)) {
          rootCnt++;
        }
      }
    }
    
    if (lst.size() == 0) {
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
      Collections.sort(lst, MenuConst.MENU_COMPARATOR);
    }
    
    for (MenuEntry entry : lst) {
      buff.add(entry.serialize());
    }
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    loadXml("/com/butent/bee/egg/server/menu.xml");
  }
  
  private MenuEntry getEntry(String id) {
    for (MenuEntry entry : getMenu()) {
      if (entry.getId().equals(id)) {
        return entry;
      }
    }
    
    return null;
  }
  
  private boolean isVisible(String id) {
    return getEntry(id).isVisible();
  }
  
  private boolean isParentVisible(MenuEntry entry) {
    String p = entry.getParent();

    if (BeeUtils.isEmpty(p)) {
      return true;
    }
    else {
      return isVisible(p);
    }
  }
  
  private boolean isRoot(MenuEntry entry) {
    return BeeUtils.isEmpty(entry.getParent());
  }

}
