package com.butent.bee.client.modules.discussions;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.modules.discussions.DiscussionsList.ListType;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.menu.MenuHandler;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsConstants.DiscussionEvent;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.rights.Module;

public final class DiscussionsKeeper {

  public static void register() {
    /* Form interceptors */
    FormFactory.registerFormInterceptor(FORM_NEW_DISCUSSION, new CreateDiscussionInterceptor());
    FormFactory.registerFormInterceptor(FORM_DISCUSSION, new DiscussionInterceptor());
    FormFactory.registerFormInterceptor(FORM_ANNOUNCEMENTS_BOARD,
        new AnnouncementsBoardInterceptor());

    /* Menu */
    MenuService.DISCUSS_LIST.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        DiscussionsList.open(parameters);
      }
    });

    /* Row handlers */
    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);

    Global.getNewsAggregator().registerFilterHandler(Feed.ANNOUNCEMENTS,
        getAnnouncementsFilterHandler());
  }

  static ParameterList createArgs(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(Module.DISCUSSIONS.getName());
    args.addQueryItem(AdministrationConstants.METHOD, method);
    return args;
  }

  static ParameterList createDiscussionRpcParameters(DiscussionEvent event) {
    return createArgs(DISCUSSIONS_PREFIX + event.name());
  }

  static Consumer<GridOptions> getAnnouncementsFilterHandler() {
    Consumer<GridOptions> consumer = new Consumer<GridOptions>() {

      @Override
      public void accept(GridOptions input) {
        GridFactory.openGrid(GRID_DISCUSSIONS, new DiscussionsGridHandler(ListType.ALL), input);
      }
    };

    return consumer;
  }

  private DiscussionsKeeper() {

  }
}
