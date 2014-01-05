package com.butent.bee.server.data;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeRelation;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.news.Feed;
import com.butent.bee.shared.data.news.Headline;
import com.butent.bee.shared.data.news.Subscription;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class NewsBean {

  private static BeeLogger logger = LogUtils.getLogger(NewsBean.class);

  private static final String TBL_USER_FEEDS = "UserFeeds";

  private static final String COL_UF_USER = "User";
  private static final String COL_UF_FEED = CommonsConstants.COL_FEED;

  private static final String COL_UF_SUBSCRIPTION_DATE = "SubscriptionDate";
  private static final String COL_UF_ORDINAL = "Ordinal";

  private static final String COL_USAGE_USER = "User";

  private static final String COL_USAGE_ACCESS = "Access";
  private static final String COL_USAGE_UPDATE = "Update";

  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  public ResponseObject getNews() {
    Long userId = usr.getCurrentUserId();
    if (!DataUtils.isId(userId)) {
      return ResponseObject.error("user id not available");
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_USER_FEEDS, COL_UF_FEED, COL_UF_SUBSCRIPTION_DATE)
        .addFrom(TBL_USER_FEEDS)
        .setWhere(SqlUtils.equals(TBL_USER_FEEDS, COL_UF_USER, userId))
        .addOrder(TBL_USER_FEEDS, COL_UF_ORDINAL, COL_UF_FEED);

    SimpleRowSet userFeeds = qs.getData(query);
    if (DataUtils.isEmpty(userFeeds)) {
      return ResponseObject.emptyResponse();
    }

    List<Subscription> subscriptions = Lists.newArrayList();
    int countHeadlines = 0;

    for (SimpleRow row : userFeeds) {
      Feed feed = EnumUtils.getEnumByIndex(Feed.class, row.getValue(COL_UF_FEED));
      if (feed == null) {
        logger.warning("invalid user feed value", row.getValue(COL_UF_FEED));
        break;
      }

      DateTime date = row.getDateTime(COL_UF_SUBSCRIPTION_DATE);
      Subscription subscription = new Subscription(feed, date);

      List<Headline> headlines = getHeadlines(userId, feed, date);
      if (!headlines.isEmpty()) {
        subscription.getHeadlines().addAll(headlines);
        countHeadlines += headlines.size();
      }

      subscriptions.add(subscription);
    }

    logger.info("user", userId, "subscriptions", subscriptions.size(), "headlines", countHeadlines);

    return ResponseObject.response(subscriptions);
  }

  public void onUpdate(String table, Long rowId) {
    Feed feed = Feed.findFeedWithUsageTable(table);
    if (feed == null) {
      return;
    }

    if (!DataUtils.isId(rowId)) {
      return;
    }

    String relationColumn = getUsageRelationColumn(feed);
    if (BeeUtils.isEmpty(relationColumn)) {
      return;
    }

    Long userId = usr.getCurrentUserId();
    if (!DataUtils.isId(userId)) {
      return;
    }

    String usageTable = feed.getUsageTable();
    long now = System.currentTimeMillis();

    IsCondition where = SqlUtils.equals(usageTable, relationColumn, rowId, COL_USAGE_USER, userId);

    if (qs.sqlExists(usageTable, where)) {
      qs.updateData(new SqlUpdate(usageTable)
          .addConstant(COL_USAGE_ACCESS, now)
          .addConstant(COL_USAGE_UPDATE, now)
          .setWhere(where));

    } else {
      qs.insertData(new SqlInsert(usageTable)
          .addFields(relationColumn, COL_USAGE_USER, COL_USAGE_ACCESS, COL_USAGE_UPDATE)
          .addValues(rowId, userId, now, now));
    }
  }

  public ResponseObject onReadFeed(RequestInfo reqInfo) {
    Assert.notNull(reqInfo);

    Long userId = usr.getCurrentUserId();
    if (!DataUtils.isId(userId)) {
      return ResponseObject.error(reqInfo.getService(), "user id not available");
    }

    Feed feed = EnumUtils.getEnumByIndex(Feed.class, reqInfo.getParameter(Service.VAR_FEED));
    if (feed == null) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), Service.VAR_FEED);
    }

    Long dataId = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_ID));
    if (!DataUtils.isId(dataId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), Service.VAR_ID);
    }

    String usageTable = feed.getUsageTable();
    if (BeeUtils.isEmpty(usageTable)) {
      return ResponseObject.error(reqInfo.getService(), "feed", feed.name(), "has no usage table");
    }

    String relationColumn = getUsageRelationColumn(feed);
    if (BeeUtils.isEmpty(relationColumn)) {
      return ResponseObject.error(reqInfo.getService(), "relation column not found for");
    }

    IsCondition where = SqlUtils.equals(usageTable, relationColumn, dataId, COL_USAGE_USER, userId);

    if (qs.sqlExists(usageTable, where)) {
      return qs.updateDataWithResponse(new SqlUpdate(usageTable)
          .addConstant(COL_USAGE_ACCESS, System.currentTimeMillis())
          .setWhere(where));

    } else {
      return qs.insertDataWithResponse(new SqlInsert(usageTable)
          .addFields(relationColumn, COL_USAGE_USER, COL_USAGE_ACCESS)
          .addValues(dataId, userId, System.currentTimeMillis()));
    }
  }

  public ResponseObject subscribe(RequestInfo reqInfo) {
    Assert.notNull(reqInfo);

    Long userId = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_USER));
    if (!DataUtils.isId(userId)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), Service.VAR_USER);
    }

    String feedList = reqInfo.getParameter(Service.VAR_FEED);
    if (BeeUtils.isEmpty(feedList)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), Service.VAR_FEED);
    }

    Splitter splitter =
        Splitter.on(CharMatcher.inRange(BeeConst.CHAR_ZERO, BeeConst.CHAR_NINE).negate())
            .omitEmptyStrings().trimResults();

    Set<Integer> feeds = Sets.newHashSet();
    for (String s : splitter.split(feedList)) {
      Feed feed = EnumUtils.getEnumByIndex(Feed.class, s);
      if (feed != null) {
        feeds.add(feed.ordinal());
      }
    }

    if (feeds.isEmpty()) {
      return ResponseObject.error(reqInfo.getService(), "cannot parse feeds", feedList);
    }

    long time = TimeUtils.nowMinutes().getTime();

    for (Integer feed : feeds) {
      SqlInsert insert = new SqlInsert(TBL_USER_FEEDS)
          .addFields(COL_UF_USER, COL_UF_FEED, COL_UF_SUBSCRIPTION_DATE)
          .addValues(userId, feed, time);

      ResponseObject response = qs.insertDataWithResponse(insert);
      if (response.hasErrors()) {
        return response;
      }
    }

    logger.info(reqInfo.getService(), "user", userId, "subscribed to", feeds);

    return ResponseObject.response(feeds.size());
  }

  private List<Headline> getHeadlines(Long userId, Feed feed, DateTime startDate) {
    List<Headline> result = Lists.newArrayList();

    String tableName = feed.getTable();
    String usageTableName = feed.getUsageTable();

    if (!BeeUtils.anyEmpty(tableName, usageTableName)) {
      if (!sys.isTable(tableName)) {
        logger.severe("feed", feed.name(), "table not recognized", tableName);
        return result;
      }

      if (!sys.isTable(usageTableName)) {
        logger.severe("feed", feed.name(), "usage table not recognized", usageTableName);
        return result;
      }

      String relationColumn = getUsageRelationColumn(feed);
      if (BeeUtils.isEmpty(relationColumn)) {
        return result;
      }

      long startTime = (startDate == null) ? 0L : startDate.getTime();

      Map<Long, Long> otherUpdates = Maps.newHashMap();

      SqlSelect otherQuery = new SqlSelect()
          .addFields(usageTableName, relationColumn)
          .addMax(usageTableName, COL_USAGE_UPDATE)
          .addFrom(usageTableName)
          .setWhere(SqlUtils.and(SqlUtils.notEqual(usageTableName, COL_USAGE_USER, userId),
              SqlUtils.more(usageTableName, COL_USAGE_UPDATE, startTime)))
          .addGroup(usageTableName, relationColumn);

      SimpleRowSet otherData = qs.getData(otherQuery);
      if (!DataUtils.isEmpty(otherData)) {
        for (SimpleRow row : otherData) {
          otherUpdates.put(row.getLong(0), row.getLong(1));
        }
      }

      if (otherUpdates.isEmpty()) {
        return result;
      }

      Map<Long, Long> userAccess = Maps.newHashMap();

      SqlSelect userQuery = new SqlSelect()
          .addFields(usageTableName, relationColumn)
          .addMax(usageTableName, COL_USAGE_ACCESS)
          .addFrom(usageTableName)
          .setWhere(SqlUtils.equals(usageTableName, COL_USAGE_USER, userId))
          .addGroup(usageTableName, relationColumn);

      SimpleRowSet userData = qs.getData(userQuery);
      if (!DataUtils.isEmpty(userData)) {
        for (SimpleRow row : userData) {
          userAccess.put(row.getLong(0), row.getLong(1));
        }
      }

      if (userAccess.isEmpty()) {
        for (Long id : otherUpdates.keySet()) {
          result.add(Headline.forInsert(id, BeeUtils.toString(id)));
        }
   
      } else {
        for (Map.Entry<Long, Long> updateEntry : otherUpdates.entrySet()) {
          Long id = updateEntry.getKey();

          Long access = userAccess.get(id);
          if (access == null) {
            result.add(Headline.forInsert(id, BeeUtils.toString(id)));
          } else if (access < updateEntry.getValue()) {
            result.add(Headline.forUpdate(id, BeeUtils.toString(id)));
          }
        }
      }
    }

    return result;
  }

  private String getUsageRelationColumn(Feed feed) {
    Collection<BeeField> fields = sys.getTableFields(feed.getUsageTable());

    if (fields != null) {
      for (BeeField field : fields) {
        if (field instanceof BeeRelation
            && feed.getTable().equals(((BeeRelation) field).getRelation())) {
          return field.getName();
        }
      }
    }

    logger.severe("feed", feed.name(), "table", feed.getTable(),
        "usage table", feed.getUsageTable(), "relation column not found");
    return null;
  }
}
