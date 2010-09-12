package com.butent.bee.egg.shared.menu;

import java.util.Comparator;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class MenuConst {
  public static int MAX_MENU_DEPTH = 3;

  public static final String FIELD_ROOT_LIMIT = "menu_root_limit";
  public static final String FIELD_ITEM_LIMIT = "menu_item_limit";

  public static int DEFAULT_ROOT_LIMIT = 7;
  public static int DEFAULT_ITEM_LIMIT = 18;

  public static final String LAYOUT_MENU_HOR = "menu horizontal";
  public static final String LAYOUT_MENU_VERT = "menu vertical";
  public static final String LAYOUT_STACK = "stack panel";
  public static final String LAYOUT_TREE = "simple tree";
  public static final String LAYOUT_CELL_TREE = "cell tree";
  public static final String LAYOUT_CELL_BROWSER = "cell browser";
  public static final String LAYOUT_LIST = "simple list";
  public static final String LAYOUT_CELL_LIST = "cell list";
  public static final String LAYOUT_TAB = "tab panel";
  public static final String LAYOUT_RADIO_HOR = "radio horizontal";
  public static final String LAYOUT_RADIO_VERT = "radio vertical";
  public static final String LAYOUT_BUTTONS_HOR = "buttons horizontal";
  public static final String LAYOUT_BUTTONS_VERT = "buttons vertical";

  public static String DEFAULT_ROOT_LAYOUT = LAYOUT_MENU_HOR;
  public static String DEFAULT_ITEM_LAYOUT = LAYOUT_MENU_VERT;

  public static final int SEPARATOR_BEFORE = 1;
  public static final int SEPARATOR_AFTER = 2;

  public static MenuComparator MENU_COMPARATOR = new MenuComparator();

  public static boolean isValidLayout(String layout) {
    if (BeeUtils.isEmpty(layout)) {
      return false;
    }

    return BeeUtils.inListSame(layout, LAYOUT_MENU_HOR, LAYOUT_MENU_VERT,
        LAYOUT_STACK, LAYOUT_TREE, LAYOUT_CELL_TREE, LAYOUT_CELL_BROWSER,
        LAYOUT_LIST, LAYOUT_CELL_LIST, LAYOUT_TAB, LAYOUT_RADIO_HOR,
        LAYOUT_RADIO_VERT, LAYOUT_BUTTONS_HOR, LAYOUT_BUTTONS_VERT);
  }

  public static boolean isSeparatorBefore(int sep) {
    return (sep & SEPARATOR_BEFORE) != 0;
  }

  public static boolean isSeparatorAfter(int sep) {
    return (sep & SEPARATOR_AFTER) != 0;
  }

  public static final String fieldMenuLayout(int idx) {
    return "menu_layout_" + idx;
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
