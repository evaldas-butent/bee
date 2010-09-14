package com.butent.bee.egg.shared.menu;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
  public static List<MenuEntry> getChildren(List<MenuEntry> entries, String id,
      boolean isOrdered) {
    Assert.notNull(entries);
    Assert.notEmpty(id);

    List<MenuEntry> lst = new ArrayList<MenuEntry>();
    boolean tg = false;

    for (MenuEntry entry : entries) {
      if (BeeUtils.same(entry.getParent(), id)) {
        lst.add(entry);
        if (isOrdered) {
          tg = true;
        }
      } else if (tg) {
        break;
      }
    }

    return lst;
  }

}
