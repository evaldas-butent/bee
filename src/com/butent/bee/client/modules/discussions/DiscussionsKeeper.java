package com.butent.bee.client.modules.discussions;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public final class DiscussionsKeeper {

  public static void register() {
    /* Form interceptors */
    FormFactory.registerFormInterceptor(FORM_NEW_DISCUSSION, new CreateDiscussionInterceptor());
    FormFactory.registerFormInterceptor(FORM_DISCUSSION, new DiscussionInterceptor());
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

  static ParameterList createArgs(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(DISCUSSIONS_MODULE);
    args.addQueryItem(DISCUSSIONS_METHOD, method);
    return args;
  }

  static ParameterList createDiscussionRpcParameters(DiscussionEvent event) {
    return createArgs(DISCUSSIONS_PREFIX + event.name());
  }

  public static void getDiscussionMarksData(final List<Long> marksIds,
      final Callback<SimpleRowSet> result) {
    if (marksIds == null || result == null) {
      return;
    }

    LogUtils.getRootLogger().debug("PROP MARKS", BeeUtils.joinLongs(marksIds));

    ParameterList params = createArgs(SVC_GET_DISCUSSION_MARKS_DATA);
    if (!marksIds.isEmpty()) {
      params.addDataItem(VAR_DISCUSSION_MARK, DataUtils.buildIdList(marksIds));
    }
    // params.addDataItem(VAR_DISCUSSION_COMMENT, BeeUtils.unbox(commentId));

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasResponse()) {
          BeeKeeper.getScreen().notifyInfo(BeeUtils.join(". ", response.getMessages()));
          result.onFailure(BeeUtils.join(". ", response.getMessages()));
          return;
        }

        if (response.hasResponse(SimpleRowSet.class)) {
          result.onSuccess(SimpleRowSet.restore(response.getResponseAsString()));
        } else {
          result.onFailure("Unknown response type:", response.getResponse().getClass().getName());
        }
      }

    });
  }

  private DiscussionsKeeper() {

  }
}
