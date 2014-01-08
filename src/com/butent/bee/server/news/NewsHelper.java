package com.butent.bee.server.news;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeRelation;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.news.Channel;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.Headline;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NewsHelper {

  static final String COL_USAGE_USER = "User";
  static final String COL_USAGE_ACCESS = "Access";
  static final String COL_USAGE_UPDATE = "Update";

  private static BeeLogger logger = LogUtils.getLogger(NewsHelper.class);

  private static final Map<String, String> usageRelationColumns = new ConcurrentHashMap<>();

  private static final Map<Feed, Channel> registeredChannels = new ConcurrentHashMap<>();
  private static final Map<Feed, UsageQueryProvider> registeredQueryProviders =
      new ConcurrentHashMap<>();

  public static long getStartTime(DateTime startDate) {
    return (startDate == null) ? 0L : startDate.getTime();
  }

  public static void registerChannel(Feed feed, Channel channel) {
    Assert.notNull(feed);
    Assert.notNull(channel);

    logger.debug("registered channel for feed", feed.name());
    registeredChannels.put(feed, channel);
  }

  public static void registerUsageQueryProvider(Feed feed, UsageQueryProvider usageQueryProvider) {
    Assert.notNull(feed);
    Assert.notNull(usageQueryProvider);

    logger.debug("registered usage query provider for feed", feed.name());
    registeredQueryProviders.put(feed, usageQueryProvider);
  }

  static SqlSelect getAccessQuery(long userId, String usageTable, String relationColumn) {
    if (BeeUtils.anyEmpty(usageTable, relationColumn)) {
      return null;
    
    } else {
      return new SqlSelect()
          .addFields(usageTable, relationColumn)
          .addMax(usageTable, COL_USAGE_ACCESS)
          .addFrom(usageTable)
          .setWhere(SqlUtils.equals(usageTable, COL_USAGE_USER, userId))
          .addGroup(usageTable, relationColumn);
    }
  }

  static List<Headline> getHeadlines(Feed feed, long userId, DateTime startDate) {
    return registeredChannels.get(feed).getHeadlines(feed, userId, startDate);
  }

  static SqlSelect getQueryForAccess(Feed feed, String usageTable, String relationColumn,
      long userId, DateTime startDate) {

    if (hasUsageQueryProvider(feed)) {
      return registeredQueryProviders.get(feed).getQueryForAccess(feed, relationColumn, userId,
          startDate);
    } else {
      return getAccessQuery(userId, usageTable, relationColumn);
    }
  }

  static SqlSelect getQueryForUpdates(Feed feed, String usageTable, String relationColumn,
      long userId, DateTime startDate) {
    
    if (hasUsageQueryProvider(feed)) {
      return registeredQueryProviders.get(feed).getQueryForUpdates(feed, relationColumn, userId,
          startDate);
    } else {
      return getUpdatesQuery(userId, usageTable, relationColumn, startDate);
    }
  }

  static SqlSelect getUpdatesQuery(long userId, String usageTable, String relationColumn,
      DateTime startDate) {
    if (BeeUtils.anyEmpty(usageTable, relationColumn)) {
      return null;

    } else {
      return new SqlSelect()
          .addFields(usageTable, relationColumn)
          .addMax(usageTable, COL_USAGE_UPDATE)
          .addFrom(usageTable)
          .setWhere(SqlUtils.and(SqlUtils.notEqual(usageTable, COL_USAGE_USER, userId),
              SqlUtils.more(usageTable, COL_USAGE_UPDATE, getStartTime(startDate))))
          .addGroup(usageTable, relationColumn);
    }
  }

  static String getUsageRelationColumn(String table, String usageTable, SystemBean sys) {
    if (BeeUtils.anyEmpty(table, usageTable)) {
      return null;
    }

    String relationColumn = usageRelationColumns.get(usageTable);

    if (BeeUtils.isEmpty(relationColumn)) {
      Collection<BeeField> fields = (sys == null) ? null : sys.getTableFields(usageTable);

      if (fields != null) {
        for (BeeField field : fields) {
          if (field instanceof BeeRelation && table.equals(((BeeRelation) field).getRelation())) {
            relationColumn = field.getName();
            break;
          }
        }
      }

      if (BeeUtils.isEmpty(relationColumn)) {
        logger.severe("table", table, "usage table", usageTable, "relation column not found");
      } else {
        usageRelationColumns.put(usageTable, relationColumn);
      }
    }

    return relationColumn;
  }

  static boolean hasChannel(Feed feed) {
    return feed != null && registeredChannels.containsKey(feed);
  }

  static boolean hasUsageQueryProvider(Feed feed) {
    return feed != null && registeredQueryProviders.containsKey(feed);
  }

  private NewsHelper() {
  }
}
