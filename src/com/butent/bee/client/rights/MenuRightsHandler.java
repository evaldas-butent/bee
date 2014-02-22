package com.butent.bee.client.rights;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

final class MenuRightsHandler extends RightsForm {

  private static final class MenuObject implements HasCaption {
    private static final String NAME_SEPARATOR = ".";

    private final int level;
    @SuppressWarnings("unused")
    private final String parent;

    private final String name;

    private final String caption;

    @SuppressWarnings("unused")
    private final Module module;

    private MenuObject(int level, String parent, Menu menu) {
      this.level = level;

      if (parent == null || parent.isEmpty()) {
        this.parent = null;
        this.name = menu.getName().trim();
      } else {
        this.parent = parent.trim();
        this.name = parent.trim() + NAME_SEPARATOR + menu.getName().trim();
      }

      this.caption = Localized.maybeTranslate(menu.getLabel());
      this.module = menu.getModule();
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  private final List<MenuObject> menuObjects = Lists.newArrayList();

  MenuRightsHandler() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new MenuRightsHandler();
  }

  @Override
  protected String getObjectCaption(String name) {
    for (MenuObject menuObject : menuObjects) {
      if (menuObject.name.equals(name)) {
        if (menuObject.level <= 0) {
          return menuObject.getCaption();
        } else {
          return BeeUtils.replicate(BeeConst.CHAR_NBSP, menuObject.level * 4)
              + menuObject.getCaption();
        }
      }
    }
    return name;
  }

  @Override
  protected RightsObjectType getObjectType() {
    return RightsObjectType.MENU;
  }

  @Override
  protected RightsState getRightsState() {
    return RightsState.VISIBLE;
  }

  @Override
  protected void initObjects(Consumer<List<String>> consumer) {
    if (!menuObjects.isEmpty()) {
      menuObjects.clear();
    }

    for (Menu root : BeeKeeper.getMenu().getRoots()) {
      addMenuObject(0, null, root);
    }

    List<String> names = Lists.newArrayList();
    for (MenuObject menuObject : menuObjects) {
      names.add(menuObject.name);
    }
    consumer.accept(names);
  }

  private void addMenuObject(int level, String parent, Menu menu) {
    menuObjects.add(new MenuObject(level, parent, menu));

    if (menu instanceof MenuEntry) {
      for (Menu child : ((MenuEntry) menu).getItems()) {
        addMenuObject(level + 1, menu.getName(), child);
      }
    }
  }
}
