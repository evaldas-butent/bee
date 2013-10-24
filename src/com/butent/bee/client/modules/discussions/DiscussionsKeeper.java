package com.butent.bee.client.modules.discussions;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.shared.ui.Captions;

public final class DiscussionsKeeper {

  public static void register() {
    /* Menu */
    BeeKeeper.getMenu().registerMenuCallback(MENU_SERVICE_DISCUSSIONS_LIST,
        new MenuManager.MenuCallback() {

          @Override
          public void onSelection(String parameters) {
            DiscussionsList.open(parameters);
          }
        });

    /* Captions */
    String key = Captions.register(DiscussionStatus.class);
    Captions.registerColumn(VIEW_DISCUSSIONS, COL_STATUS, key);

    /* Row handlers */
    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);
  }

  private DiscussionsKeeper() {

  }
}
