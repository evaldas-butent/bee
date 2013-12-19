package com.butent.bee.client.modules.discussions;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

final class DiscussionIterceptorCache {

  private static BeeRowSet markTypesSet;
  private static final BeeLogger logger = LogUtils.getLogger(DiscussionIterceptorCache.class);

  public static DiscussionIterceptorCache getInstance() {
    return new DiscussionIterceptorCache();
  }

  public static void clearMarkTypes() {
    markTypesSet = null;
  }

  public static void getMarkTypes(final Callback<BeeRowSet> response) {

    if (isEmptyMarkTypes()) {
      logger.info("Getting mark types from view");

      Queries.getRowSet(VIEW_DISCUSSIONS_MARK_TYPES, Lists.newArrayList(COL_MARK_NAME,
          COL_MARK_RESOURCE), new RowSetCallback() {
        @Override
        public void onSuccess(final BeeRowSet result) {
          setMarkTypes(result);
          response.onSuccess(result);
        }
      });

    } else {
      logger.debug("Ussing mark types from cache");
      response.onSuccess(markTypesSet);
    }

  }

  public static boolean isEmptyMarkTypes() {
    if (markTypesSet == null) {
      return true;
    }

    return markTypesSet.isEmpty();
  }

  private static void setMarkTypes(BeeRowSet markTypes) {
    markTypesSet = markTypes;
  }

  private DiscussionIterceptorCache() {
    markTypesSet = null;
  }
}
