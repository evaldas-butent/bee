package com.butent.bee.server.news;

import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.news.Channel;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.Headline;
import com.butent.bee.shared.news.HeadlineProducer;
import com.butent.bee.shared.news.NewsUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class NewsHelper {

  private static BeeLogger logger = LogUtils.getLogger(NewsHelper.class);

  private static final Map<String, String> usageRelationColumns = new ConcurrentHashMap<>();

  private static final Map<Feed, Channel> registeredChannels = new ConcurrentHashMap<>();
  private static final Map<Feed, UsageQueryProvider> registeredQueryProviders =
      new ConcurrentHashMap<>();
  private static final Map<Feed, HeadlineProducer> registeredHeadlineProducers =
      new ConcurrentHashMap<>();

  static SqlSelect getAccessQuery(long userId, String usageTable, String relationColumn) {
    if (BeeUtils.anyEmpty(usageTable, relationColumn)) {
      return null;

    } else {
      return new SqlSelect()
          .addFields(usageTable, relationColumn)
          .addMax(usageTable, NewsUtils.COL_USAGE_ACCESS)
          .addFrom(usageTable)
          .setWhere(SqlUtils.equals(usageTable, NewsUtils.COL_USAGE_USER, userId))
          .addGroup(usageTable, relationColumn);
    }
  }

  static Headline getHeadline(Feed feed, long userId, BeeRowSet rowSet, IsRow row, boolean isNew) {
    return registeredHeadlineProducers.get(feed).produce(feed, userId, rowSet, row, isNew);
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

  static IsCondition getUpdatesCondition(String usageTable, long userId, DateTime startDate) {
    return SqlUtils.and(SqlUtils.notEqual(usageTable, NewsUtils.COL_USAGE_USER, userId),
        SqlUtils.more(usageTable, NewsUtils.COL_USAGE_UPDATE, NewsUtils.getStartTime(startDate)));
  }
  
  static SqlSelect getUpdatesQuery(long userId, String usageTable, String relationColumn,
      DateTime startDate) {
    if (BeeUtils.anyEmpty(usageTable, relationColumn)) {
      return null;

    } else {
      return new SqlSelect()
          .addFields(usageTable, relationColumn)
          .addMax(usageTable, NewsUtils.COL_USAGE_UPDATE)
          .addFrom(usageTable)
          .setWhere(getUpdatesCondition(usageTable, userId, startDate))
          .addGroup(usageTable, relationColumn);
    }
  }

  static String getUsageRelationColumn(String usageTable) {
    return usageRelationColumns.get(usageTable);
  }

  static boolean hasChannel(Feed feed) {
    return feed != null && registeredChannels.containsKey(feed);
  }

  static boolean hasHeadlineProducer(Feed feed) {
    return feed != null && registeredHeadlineProducers.containsKey(feed);
  }

  static boolean hasUsageQueryProvider(Feed feed) {
    return feed != null && registeredQueryProviders.containsKey(feed);
  }

  static void putUsageRelationColumn(String usageTable, String relationColumn) {
    usageRelationColumns.put(usageTable, relationColumn);
  }

  static void registerChannel(Feed feed, Channel channel) {
    registeredChannels.put(feed, channel);
    logger.debug("registered channel for feed", feed.name());
  }

  static void registerHeadlineProducer(Feed feed, HeadlineProducer headlineProducer) {
    registeredHeadlineProducers.put(feed, headlineProducer);
    logger.debug("registered headline producer for feed", feed.name());
  }

  static void registerUsageQueryProvider(Feed feed, UsageQueryProvider usageQueryProvider) {
    registeredQueryProviders.put(feed, usageQueryProvider);
    logger.debug("registered usage query provider for feed", feed.name());
  }

  private NewsHelper() {
  }
}
