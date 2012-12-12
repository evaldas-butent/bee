package com.butent.bee.client.modules.commons;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler.ParameterFormHandler;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants.ReminderMethod;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.utils.BeeUtils;

public class CommonsKeeper {

  public static void register() {
    FormFactory.registerFormInterceptor("Item", new ItemFormHandler());

    BeeKeeper.getMenu().registerMenuCallback("items", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Items", new ItemGridHandler(BeeUtils.startsSame(parameters, "s")));
      }
    });

    FormFactory.registerFormInterceptor("Parameter", new ParameterFormHandler());

    BeeKeeper.getMenu().registerMenuCallback("system_parameters", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Parameters", new ParametersHandler(parameters));
      }
    });

    SelectorEvent.register(new CommonsSelectorHandler());

    Global.registerCaptions(RightsObjectType.class);
    Global.registerCaptions(RightsState.class);
    Global.registerCaptions(ParameterType.class);
    
    String key = Global.registerCaptions(ReminderMethod.class);
    Data.registerCaptionKey(CommonsConstants.VIEW_REMINDER_TYPES,
        CommonsConstants.COL_REMINDER_METHOD, key);
  }
  
  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CommonsConstants.COMMONS_MODULE);
    args.addQueryItem(CommonsConstants.COMMONS_METHOD, name);
    return args;
  }
  
  private CommonsKeeper() {
  }
}
