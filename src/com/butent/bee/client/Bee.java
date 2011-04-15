package com.butent.bee.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import com.butent.bee.shared.BeeConst;

public class Bee implements EntryPoint {
  public void onModuleLoad() {
    BeeConst.setClient();

    BeeKeeper bk = new BeeKeeper(RootLayoutPanel.get(), GWT.getModuleBaseURL() + "bee");

    bk.init();
    bk.start();

    if (GWT.isProdMode()) {
      GWT.setUncaughtExceptionHandler(new ExceptionHandler());
    }
  }
}
