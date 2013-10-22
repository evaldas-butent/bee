package com.butent.bee.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.decorator.TuningFactory;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.modules.ModuleManager;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.client.utils.LayoutEngine;
import com.butent.bee.client.view.grid.GridSettings;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;

/**
 * The entry point class of the application, initializes <code>BeeKeeper</code> class.
 */

public class Bee implements EntryPoint {

  private static BeeKeeper keeper;

  public static void exit() {
    Bee.keeper.exit();
    BeeKeeper.getRpc().makeGetRequest(Service.LOGOUT);
    BodyPanel.get().clear();
  }

  @Override
  public void onModuleLoad() {
    BeeConst.setClient();
    LogUtils.setLoggerFactory(new ClientLogManager());

    Localized.setConstants((LocalizableConstants) GWT.create(LocalizableConstants.class));
    Localized.setMessages((LocalizableMessages) GWT.create(LocalizableMessages.class));

    LayoutEngine layoutEngine = LayoutEngine.detect();
    if (layoutEngine != null && layoutEngine.hasStyleSheet()) {
      DomUtils.injectExternalStyle(layoutEngine.getStyleSheet());
    }

    Bee.keeper = new BeeKeeper();

    Bee.keeper.init();
    Bee.keeper.start();

    if (GWT.isProdMode()) {
      GWT.setUncaughtExceptionHandler(new ExceptionHandler());
    }

    BeeKeeper.getScreen().start();
    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        BeeKeeper.getScreen().getScreenPanel().onResize();
      }
    });

    ParameterList params = BeeKeeper.getRpc().createParameters(Service.LOGIN);
    params.addQueryItem(Service.VAR_UI, BeeKeeper.getScreen().getUserInterface().getShortName());

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        load(Codec.beeDeserializeMap((String) response.getResponse()));
        start();
      }
    });
  }

  private static void load(Map<String, String> data) {
    UserData userData = UserData.restore(data.get(Service.VAR_USER));
    BeeKeeper.getUser().setUserData(userData);
    BeeKeeper.getScreen().updateUserData(userData);

    for (UserInterface.Component component : UserInterface.Component.values()) {
      String serialized = data.get(component.key());

      if (!BeeUtils.isEmpty(serialized)) {
        switch (component) {
          case DATA_INFO:
            Data.getDataInfoProvider().restore(serialized);
            break;

          case DICTIONARY:
            Localized.setDictionary(Codec.beeDeserializeMap(serialized));
            break;

          case DECORATORS:
            TuningFactory.parseDecorators(serialized);
            break;

          case FAVORITES:
            Global.getFavorites().load(serialized);
            break;

          case FILTERS:
            Global.getFilters().load(serialized);
            break;

          case GRIDS:
            Pair<String, String> settings = Pair.restore(serialized);
            GridSettings.load(settings.getA(), settings.getB());
            break;

          case MENU:
            BeeKeeper.getMenu().restore(serialized);
            break;
        }
      }
    }
  }

  private static void start() {
    BeeKeeper.getScreen().onLoad();

    ModuleManager.onLoad();

    Data.init();

    Historian.start();

    BeeKeeper.getBus().registerExitHandler("Don't leave me this way");
  }
}
