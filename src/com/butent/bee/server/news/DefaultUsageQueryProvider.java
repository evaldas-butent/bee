package com.butent.bee.server.news;

import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

public class DefaultUsageQueryProvider implements UsageQueryProvider {

  @Override
  public SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId,
      DateTime startDate) {
    Assert.notNull(feed);
    
    String usageTable = feed.getUsageTable();
    
    if (BeeUtils.anyEmpty(usageTable, relationColumn)) {
      return null;
    } else {
      return NewsHelper.getAccessQuery(userId, usageTable, relationColumn);
    }
  }

  @Override
  public SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId,
      DateTime startDate) {
    Assert.notNull(feed);
    
    String usageTable = feed.getUsageTable();
    
    if (BeeUtils.anyEmpty(usageTable, relationColumn)) {
      return null;
    } else {
      return NewsHelper.getUpdatesQuery(userId, usageTable, relationColumn, startDate);
    }
  }
}
