package com.butent.bee.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.decorator.TuningFactory;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.modules.ModuleManager;
import com.butent.bee.client.view.grid.GridSettings;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
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
  }

  @Override
  public void onModuleLoad() {
    BeeConst.setClient();
    LogUtils.setLoggerFactory(new ClientLogManager());

    Localized.setConstants((LocalizableConstants) GWT.create(LocalizableConstants.class));
    Localized.setMessages((LocalizableMessages) GWT.create(LocalizableMessages.class));

    Bee.keeper = new BeeKeeper(RootLayoutPanel.get(), GWT.getModuleBaseURL() + GWT.getModuleName());

    Bee.keeper.init();
    Bee.keeper.start();

    if (GWT.isProdMode()) {
      GWT.setUncaughtExceptionHandler(new ExceptionHandler());
    }
    BeeKeeper.getScreen().start();

    BeeKeeper.getRpc().makeGetRequest(Service.LOGIN, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        load(Codec.beeDeserializeMap((String) response.getResponse()));
        start();
      }
    });
  }

  private void load(Map<String, String> data) {
    final UserData userData = UserData.restore(data.get(Service.LOGIN));
    BeeKeeper.getUser().setUserData(userData);

    ParameterList params = BeeKeeper.getRpc().createParameters(CommonsConstants.COMMONS_MODULE);
    params.addQueryItem(CommonsConstants.COMMONS_METHOD, CommonsConstants.SVC_USER_INFO);
    params.addQueryItem(CommonsConstants.VAR_USER_ID, userData.getUserId());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (response != null) {
          if (response.hasResponse(SimpleRowSet.class)) {
            SimpleRowSet personData = SimpleRowSet.restore((String) response.getResponse());
            Long photoId =
                personData.getLong(0, personData.getColumnIndex(CommonsConstants.COL_PHOTO));
            BeeKeeper.updateUserSignature(userData.getUserSign(), photoId);
          } else {
            BeeKeeper.updateUserSignature(userData.getUserSign());
          }
        } else {
          BeeKeeper.updateUserSignature(userData.getUserSign());
        }
      }

    });

    BeeKeeper.getMenu().restore(data.get(Service.LOAD_MENU));

    Data.getDataInfoProvider().restore(data.get(Service.GET_DATA_INFO));

    Global.getFavorites().load(data.get(CommonsConstants.TBL_FAVORITES));
    Global.getFilters().load(data.get(CommonsConstants.TBL_FILTERS));

    TuningFactory.parseDecorators(data.get(Service.GET_DECORATORS));

    GridSettings.load(data.get(GridDescription.VIEW_GRID_SETTINGS),
        data.get(ColumnDescription.VIEW_COLUMN_SETTINGS));
  }

  private void start() {
    ModuleManager.onLoad();

    Data.init();

    Historian.start();

    Global.getSearch().focus();

    BeeKeeper.getBus().registerExitHandler("Don't leave me this way");
  }
}
