package com.butent.bee.shared.menu;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
  public static List<MenuEntry> getChildren(List<MenuEntry> entries, String id,
      boolean isOrdered, int limit) {
    Assert.notNull(entries);
    Assert.notEmpty(id);

    List<MenuEntry> lst = new ArrayList<MenuEntry>();
    boolean tg = false;

    for (MenuEntry entry : entries) {
      if (BeeUtils.same(entry.getParent(), id)) {
        lst.add(entry);
        if (limit > 0 && lst.size() >= limit) {
          break;
        }
        if (isOrdered) {
          tg = true;
        }
      } else if (tg) {
        break;
      }
    }

    return lst;
  }

  public static List<MenuEntry> limitEntries(List<MenuEntry> entries, int limit) {
    Assert.notNull(entries);
    int n = entries.size();

    if (limit <= 0 || limit >= n) {
      return entries;
    } else {
      return entries.subList(0, limit);
    }
  }
  
}
