package com.butent.bee.server.news;

import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.time.DateTime;

public interface UsageQueryProvider {

  SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId, DateTime startDate);

  SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId, DateTime startDate);
}
