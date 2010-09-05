package com.butent.bee.egg.shared.menu;

import java.util.Comparator;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class MenuConst {
  public static final String LAYOUT_HORIZONTAL = "horizontal";
  public static final String LAYOUT_VERTICAL = "vertical";
  public static final String LAYOUT_STACK = "stack";
  public static final String LAYOUT_TREE = "tree";
  public static final String LAYOUT_CELL = "cell tree";
  public static final String LAYOUT_LIST = "list";
  public static final String LAYOUT_TAB = "tab";

  public static final String FIELD_ROOT_LAYOUT = "menu_root_layout";
  public static final String FIELD_ITEM_LAYOUT = "menu_item_layout";

  public static String DEFAULT_ROOT_LAYOUT = LAYOUT_HORIZONTAL;
  public static String DEFAULT_ITEM_LAYOUT = LAYOUT_VERTICAL;
  
  public static final int SEPARATOR_BEFORE = 1;
  public static final int SEPARATOR_AFTER = 2;

  public static MenuComparator MENU_COMPARATOR = new MenuComparator();
  
  public static boolean isValidLayout(String layout) {
    if (BeeUtils.isEmpty(layout)) {
      return false;
    }
    
    return BeeUtils.inListSame(layout, LAYOUT_HORIZONTAL, LAYOUT_VERTICAL,
        LAYOUT_STACK, LAYOUT_TREE, LAYOUT_CELL, LAYOUT_LIST, LAYOUT_TAB);
  }
  
  public static boolean isSeparatorBefore(int sep) {
    return (sep & SEPARATOR_BEFORE) != 0;
  }
  public static boolean isSeparatorAfter(int sep) {
    return (sep & SEPARATOR_AFTER) != 0;
  }

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
