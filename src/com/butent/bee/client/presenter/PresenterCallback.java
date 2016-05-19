package com.butent.bee.client.presenter;

import com.butent.bee.client.BeeKeeper;

@FunctionalInterface
public interface PresenterCallback {

  PresenterCallback SHOW_IN_ACTIVE_PANEL = presenter -> {
    if (presenter != null) {
      BeeKeeper.getScreen().updateActivePanel(presenter.getMainView());
    }
  };

  PresenterCallback SHOW_IN_NEW_TAB = presenter -> {
    if (presenter != null) {
      BeeKeeper.getScreen().showInNewPlace(presenter.getMainView());
    }
  };

  void onCreate(Presenter presenter);
}
