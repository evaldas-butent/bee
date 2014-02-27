package com.butent.bee.client.rights;

import com.google.common.collect.Lists;

import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

final class ModuleRightsHandler extends MultiRoleForm {

  ModuleRightsHandler() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new ModuleRightsHandler();
  }

  @Override
  protected RightsObjectType getObjectType() {
    return RightsObjectType.MODULE;
  }

  @Override
  protected RightsState getRightsState() {
    return RightsState.VIEW;
  }

  @Override
  protected void initObjects(Consumer<List<RightsObject>> consumer) {
    List<RightsObject> result = Lists.newArrayList();
    for (Module module : Module.values()) {
      RightsObject mod = new RightsObject(module.getName(), module.getCaption(), module.getName());
      result.add(mod);

      if (!BeeUtils.isEmpty(module.getSubModules())) {
        for (SubModule subModule : module.getSubModules()) {
          result.add(new RightsObject(subModule.getName(), subModule.getCaption(),
              module.getName(subModule), 1, module.getName()));
        }
        mod.setHasChildren(true);
      }
    }
    consumer.accept(result);
  }
}
