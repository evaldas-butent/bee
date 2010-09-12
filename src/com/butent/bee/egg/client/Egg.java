package com.butent.bee.egg.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;

import com.google.gwt.user.client.ui.RootLayoutPanel;

public class Egg implements EntryPoint {

  public void onModuleLoad() {
    BeeKeeper bk = new BeeKeeper(RootLayoutPanel.get(),
        new HandlerManager(null), GWT.getModuleBaseURL() + "bee");

    bk.init();
    bk.start();
    
    if (GWT.isProdMode()) {
      GWT.setUncaughtExceptionHandler(new BeeExceptionHandler());
    }
  }

}
