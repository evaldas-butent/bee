package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler.ParameterFormHandler;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;

public class CommonsKeeper {

  private static class RowTransformHandler implements RowTransformEvent.Handler {
    @Override
    public void onRowTransform(RowTransformEvent event) {
      if (event.hasView(VIEW_USERS)) {
        event.setResult(DataUtils.join(Data.getDataInfo(VIEW_USERS), event.getRow(),
            Lists.newArrayList(COL_LOGIN, COL_FIRST_NAME, COL_LAST_NAME, ALS_COMPANY_NAME),
            BeeConst.STRING_SPACE));
      }
    }
  }  
  
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

    Captions.register(RightsObjectType.class);
    Captions.register(RightsState.class);
    Captions.register(ParameterType.class);
    
    String key = Captions.register(ReminderMethod.class);
    Captions.registerColumn(VIEW_REMINDER_TYPES, COL_REMINDER_METHOD, key);
    
    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);
  }
  
  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(COMMONS_MODULE);
    args.addQueryItem(COMMONS_METHOD, name);
    return args;
  }
  
  private CommonsKeeper() {
  }
}
