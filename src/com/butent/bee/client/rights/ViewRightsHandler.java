package com.butent.bee.client.rights;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.RightsUtils;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class ViewRightsHandler extends MultiStateForm {

  ViewRightsHandler() {
  }

  @Override
  protected List<RightsState> getRightsStates() {
    return Lists.newArrayList(RightsState.CREATE, RightsState.VIEW, RightsState.EDIT, 
        RightsState.DELETE);
  }

  @Override
  protected RightsObjectType getObjectType() {
    return RightsObjectType.DATA;
  }

  @Override
  protected void initObjects(Consumer<List<RightsObject>> consumer) {
    List<RightsObject> result = Lists.newArrayList();
    
    Multimap<Module, SubModule> modules = HashMultimap.create();  

    Collection<DataInfo> views = Data.getDataInfoProvider().getViews();
    for (DataInfo view : views) {
      Pair<Module, SubModule> vm = Module.parse(view.getModule());
      if (vm == null) {
//        warning("view", view.getViewName(), "module", view.getModule(), "not recognized");
        continue;
      }
      
      Module module = vm.getA();
      SubModule subModule = vm.getB();
      
      String moduleName = module.getName();
      
      if (!modules.containsKey(module)) {
        modules.put(module, null);

        RightsObject moduleObject = new RightsObject(moduleName, module.getCaption(), moduleName);
        moduleObject.setHasChildren(true);
        result.add(moduleObject);
      }
      
      if (subModule != null && !modules.containsEntry(module, subModule)) {
        modules.put(module, subModule);
        
        RightsObject subModuleObject = new RightsObject(subModule.getName(),
            subModule.getCaption(), moduleName, 1, moduleName);
        subModuleObject.setHasChildren(true);
        result.add(subModuleObject);
      }
      
      int level;
      String parent;
      
      if (subModule == null) {
        level = 1;
        parent = moduleName;
      } else {
        level = 2;
        parent = RightsUtils.buildName(moduleName, subModule.getName());
      }
      
      String viewName = view.getViewName();
      String caption = BeeUtils.notEmpty(Localized.maybeTranslate(view.getCaption()), viewName);
      
      RightsObject viewObject = new RightsObject(viewName, caption, parent, level, parent);
      result.add(viewObject);
    }
    
    Collections.sort(result);
    consumer.accept(result);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ViewRightsHandler();
  }
}
