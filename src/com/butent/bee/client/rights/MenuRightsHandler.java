package com.butent.bee.client.rights;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.rights.ModuleAndSub;

import java.util.List;

final class MenuRightsHandler extends MultiRoleForm {

  MenuRightsHandler() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new MenuRightsHandler();
  }

  @Override
  protected RightsObjectType getObjectType() {
    return RightsObjectType.MENU;
  }

  @Override
  protected RightsState getRightsState() {
    return RightsState.VIEW;
  }

  @Override
  protected void initObjects(Consumer<List<RightsObject>> consumer) {
    List<RightsObject> result = Lists.newArrayList();

    for (Menu root : BeeKeeper.getMenu().getRoots()) {
      addMenuObject(result, 0, null, root);
    }
    consumer.accept(result);
  }

  private boolean addMenuObject(List<RightsObject> result, int level, String parent, Menu menu) {
    ModuleAndSub ms = ModuleAndSub.parse(menu.getModule());
    if (ms != null && !ms.isEnabled()) {
      return false;
    }
    
    RightsObject object = new RightsObject(menu.getName(),
        Localized.maybeTranslate(menu.getLabel()), ms, level, parent);

    result.add(object);

    if (menu instanceof MenuEntry) {
      int count = 0;

      for (Menu child : ((MenuEntry) menu).getItems()) {
        if (addMenuObject(result, level + 1, object.getName(), child)) {
          count++;
        }
      }
      
      if (count > 0) {
        object.setHasChildren(true);
      }
    }
    
    return true;
  }
}
