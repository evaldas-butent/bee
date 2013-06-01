package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler.ParameterFormHandler;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.commons.CommonsConstants.ReminderMethod;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;

public class CommonsKeeper {

  private static class UserFormInterceptor extends AbstractFormInterceptor {
    @Override
    public void afterCreateWidget(String name, final IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (BeeUtils.same(name, "ChangePassword") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            PasswordService.changePassword(UiHelper.getForm(widget.asWidget()));
          }
        });
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return this;
    }
  }

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
    FormFactory.registerFormInterceptor("User", new UserFormInterceptor());
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

    GridFactory.registerGridInterceptor(GRID_PERSONS, new PersonsGridInterceptor());
    FormFactory.registerFormInterceptor(FORM_PERSON, new PersonFormInterceptor());

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
