package com.butent.bee.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.decorator.TuningFactory;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.modules.ModuleManager;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.LogUtils;

/**
 * The entry point class of the application, initializes <code>BeeKeeper</code> class.
 */

public class Bee implements EntryPoint {

  @Override
  public void onModuleLoad() {
    BeeConst.setClient();
    LogUtils.setLoggerFactory(new ClientLogManager());

    BeeKeeper bk =
        new BeeKeeper(RootLayoutPanel.get(), GWT.getModuleBaseURL() + GWT.getModuleName());

    bk.init();
    bk.start();

    if (GWT.isProdMode()) {
      GWT.setUncaughtExceptionHandler(new ExceptionHandler());
    }
    BeeKeeper.getScreen().start();

    BeeKeeper.getRpc().makeGetRequest(Service.LOGIN, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        ModuleManager.onLoad();

        BeeKeeper.getBus().dispatchService(Service.REFRESH_MENU);

        Global.getFavorites().load();

        Data.init();

        TuningFactory.getTools();

        BeeKeeper.getBus().registerExitHandler("Don't leave me this way");

        Global.getSearch().focus();
      }
    });
  }
}
