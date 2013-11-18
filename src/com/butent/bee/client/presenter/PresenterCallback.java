package com.butent.bee.client.presenter;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.style.StyleUtils;

public interface PresenterCallback {

  PresenterCallback SHOW_IN_ACTIVE_PANEL = new PresenterCallback() {
    @Override
    public void onCreate(Presenter presenter) {
      if (presenter != null) {
        BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
      }
    }
  };

  PresenterCallback SHOW_IN_NEW_TAB = new PresenterCallback() {
    @Override
    public void onCreate(Presenter presenter) {
      if (presenter != null) {
        BeeKeeper.getScreen().showWidget(presenter.getWidget(), true);
      }
    }
  };

  PresenterCallback SHOW_IN_POPUP = new PresenterCallback() {
    @Override
    public void onCreate(Presenter presenter) {
      if (presenter != null) {
        StyleUtils.setSize(presenter.getWidget().getElement(), 800, 600);

        DialogBox dialog = DialogBox.create(null);
        dialog.setWidget(presenter.getWidget());
        dialog.setAnimationEnabled(true);
        dialog.setHideOnEscape(true);
        dialog.center();
      }
    }
  };

  void onCreate(Presenter presenter);
}
