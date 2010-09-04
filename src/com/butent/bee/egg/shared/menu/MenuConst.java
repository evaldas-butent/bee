package com.butent.bee.egg.shared.menu;

import java.util.Comparator;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class MenuConst {
  public static final int SEPARATOR_BEFORE = 1;
  public static final int SEPARATOR_AFTER = 2;

  public static final MenuEntry[] EMPTY_MENU_ARR = new MenuEntry[0];

  public static MenuComparator MENU_COMPARATOR = new MenuComparator();

  private static class MenuComparator implements Comparator<MenuEntry> {
    public int compare(MenuEntry m1, MenuEntry m2) {
      int z = BeeUtils.compare(m1.getParent(), m2.getParent());

      if (z == BeeConst.COMPARE_EQUAL) {
        z = BeeUtils.compare(m1.getOrder(), m2.getOrder());

        if (z == BeeConst.COMPARE_EQUAL) {
          z = BeeUtils.compare(m1.getId(), m2.getId());
        }  
      }

      return z;
    }
  }
}
