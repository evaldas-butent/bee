package com.butent.bee.client.modules.discussions;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.ui.FormFactory;

public final class DiscussionsKeeper {

  public static void register() {
    /* Form interceptors */
    FormFactory.registerFormInterceptor(FORM_NEW_DISCUSSION, new CreateDiscussionInterceptor());
    /* Menu */
    BeeKeeper.getMenu().registerMenuCallback(MENU_SERVICE_DISCUSSIONS_LIST,
        new MenuManager.MenuCallback() {

          @Override
          public void onSelection(String parameters) {
            DiscussionsList.open(parameters);
          }
        });

    /* Row handlers */
    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);
  }

  private DiscussionsKeeper() {

  }
}
