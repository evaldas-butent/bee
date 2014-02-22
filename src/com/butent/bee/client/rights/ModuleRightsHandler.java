package com.butent.bee.client.rights;

import com.google.common.collect.Lists;

import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

final class ModuleRightsHandler extends RightsForm {

  ModuleRightsHandler() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new ModuleRightsHandler();
  }

  @Override
  protected String getObjectCaption(String name) {
    Module module = EnumUtils.getEnumByName(Module.class, name);
    return (module == null) ? name : module.getCaption();
  }

  @Override
  protected RightsObjectType getObjectType() {
    return RightsObjectType.MODULE;
  }

  @Override
  protected RightsState getRightsState() {
    return RightsState.VISIBLE;
  }

  @Override
  protected void initObjects(Consumer<List<String>> consumer) {
    List<String> names = Lists.newArrayList();
    for (Module module : Module.values()) {
      names.add(module.name());
    }
    consumer.accept(names);
  }
}
