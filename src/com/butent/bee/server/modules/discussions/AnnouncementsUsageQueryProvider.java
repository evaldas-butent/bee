package com.butent.bee.server.modules.discussions;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.server.news.NewsHelper;
import com.butent.bee.server.news.UsageQueryProvider;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.time.DateTime;

public class AnnouncementsUsageQueryProvider implements UsageQueryProvider {

  @Override
  public SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId,
      DateTime startDate) {
    DateTime nowStart = new DateTime();
    nowStart.setHour(0);
    nowStart.setMinute(0);
    nowStart.setSecond(0);
    nowStart.setMillis(0);
    
    DateTime nowFinish = new DateTime();
    nowFinish.setHour(23);
    nowFinish.setMinute(59);
    nowFinish.setSecond(59);
    nowFinish.setMillis(999);

    SqlSelect select = new SqlSelect()
    .addFields(TBL_DISCUSSIONS_USAGE, COL_DISCUSSION)
        .addMax(TBL_DISCUSSIONS_USAGE, NewsConstants.COL_USAGE_ACCESS)
        .addFrom(TBL_DISCUSSIONS_USAGE)
            .addFromInner(
                TBL_DISCUSSIONS,
                SqlUtils.join(TBL_DISCUSSIONS, COL_DISCUSSION_ID, TBL_DISCUSSIONS_USAGE,
                    COL_DISCUSSION))
            .addFromInner(
                TBL_DISCUSSIONS_USERS,
                SqlUtils.join(TBL_DISCUSSIONS, COL_DISCUSSION_ID, TBL_DISCUSSIONS_USERS,
                    COL_DISCUSSION))
        .addGroup(TBL_DISCUSSIONS_USAGE, COL_DISCUSSION)
        .setWhere(SqlUtils.and(
            SqlUtils.equals(TBL_DISCUSSIONS_USAGE, CommonsConstants.COL_USER, userId),
            SqlUtils.notNull(TBL_DISCUSSIONS_USAGE, NewsConstants.COL_USAGE_ACCESS),
            SqlUtils.or(
                SqlUtils.equals(TBL_DISCUSSIONS, COL_ACCESSIBILITY, true),
                SqlUtils.and(
                    SqlUtils.equals(TBL_DISCUSSIONS_USERS, CommonsConstants.COL_USER, userId),
                    SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_MEMBER, true))),
            SqlUtils.notNull(TBL_DISCUSSIONS, COL_TOPIC),
            SqlUtils.or(
                SqlUtils.and(
                    SqlUtils.moreEqual(TBL_DISCUSSIONS, COL_VISIBLE_TO, System
                        .currentTimeMillis()),
                    SqlUtils.lessEqual(TBL_DISCUSSIONS, COL_VISIBLE_FROM, System
                        .currentTimeMillis())
                    ),
                SqlUtils.or(
                    SqlUtils.equals(TBL_DISCUSSIONS, COL_VISIBLE_TO, nowStart),
                    SqlUtils.equals(TBL_DISCUSSIONS, COL_VISIBLE_FROM, nowStart)
                    ),
                SqlUtils.and(
                    SqlUtils.lessEqual(TBL_DISCUSSIONS, COL_VISIBLE_FROM, System
                        .currentTimeMillis()),
                    SqlUtils.isNull(TBL_DISCUSSIONS, COL_VISIBLE_TO)
                    ),
                SqlUtils.and(
                    SqlUtils.isNull(TBL_DISCUSSIONS, COL_VISIBLE_FROM),
                    SqlUtils.moreEqual(TBL_DISCUSSIONS, COL_VISIBLE_TO, nowFinish)
                    )
                )
            ));
        
    return select;
  }

  @Override
  public SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId,
      DateTime startDate) {
    
    DateTime nowStart = new DateTime();
    nowStart.setHour(0);
    nowStart.setMinute(0);
    nowStart.setSecond(0);
    nowStart.setMillis(0);

    DateTime nowFinish = new DateTime();
    nowFinish.setHour(23);
    nowFinish.setMinute(59);
    nowFinish.setSecond(59);
    nowFinish.setMillis(999);

    SqlSelect select = new SqlSelect()
    .addFields(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION)
        .addMax(TBL_DISCUSSIONS_COMMENTS, COL_PUBLISH_TIME, NewsConstants.COL_USAGE_UPDATE)
        .addFrom(TBL_DISCUSSIONS_COMMENTS)
        .addFromInner(
            TBL_DISCUSSIONS,
            SqlUtils.join(TBL_DISCUSSIONS, COL_DISCUSSION_ID, TBL_DISCUSSIONS_COMMENTS,
                COL_DISCUSSION))
        .addFromInner(
            TBL_DISCUSSIONS_USERS,
            SqlUtils.join(TBL_DISCUSSIONS, COL_DISCUSSION_ID, TBL_DISCUSSIONS_USERS,
                COL_DISCUSSION))
        .addGroup(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION)
        .setWhere(SqlUtils.and(
            SqlUtils.notEqual(TBL_DISCUSSIONS_COMMENTS, COL_PUBLISHER, userId),
            SqlUtils.more(TBL_DISCUSSIONS_COMMENTS, COL_PUBLISH_TIME,
                NewsHelper.getStartTime(startDate)),
            SqlUtils.or(
                SqlUtils.equals(TBL_DISCUSSIONS, COL_ACCESSIBILITY, true),
                SqlUtils.and(
                    SqlUtils.equals(TBL_DISCUSSIONS_USERS, CommonsConstants.COL_USER, userId),
                    SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_MEMBER, true))),
            SqlUtils.notNull(TBL_DISCUSSIONS, COL_TOPIC),
            SqlUtils.or(
                SqlUtils.and(
                    SqlUtils.moreEqual(TBL_DISCUSSIONS, COL_VISIBLE_TO, System
                        .currentTimeMillis()),
                    SqlUtils.lessEqual(TBL_DISCUSSIONS, COL_VISIBLE_FROM, System
                        .currentTimeMillis())
                    ),
                SqlUtils.or(
                    SqlUtils.equals(TBL_DISCUSSIONS, COL_VISIBLE_TO, nowStart),
                    SqlUtils.equals(TBL_DISCUSSIONS, COL_VISIBLE_FROM, nowStart)
                    ),
                SqlUtils.and(
                    SqlUtils.lessEqual(TBL_DISCUSSIONS, COL_VISIBLE_FROM, System
                        .currentTimeMillis()),
                    SqlUtils.isNull(TBL_DISCUSSIONS, COL_VISIBLE_TO)
                    ),
                SqlUtils.and(
                    SqlUtils.isNull(TBL_DISCUSSIONS, COL_VISIBLE_FROM),
                    SqlUtils.moreEqual(TBL_DISCUSSIONS, COL_VISIBLE_TO, nowFinish)
                    )
                )
            ));
    
    
    SqlSelect select2 = new SqlSelect()
        .addFields(TBL_DISCUSSIONS_USERS, COL_DISCUSSION)
        .addMax(TBL_DISCUSSIONS, COL_CREATED, NewsConstants.COL_USAGE_UPDATE)
      .addFrom(TBL_DISCUSSIONS)
      .addFromInner(
            TBL_DISCUSSIONS_USERS,
            SqlUtils.join(TBL_DISCUSSIONS, COL_DISCUSSION_ID, TBL_DISCUSSIONS_USERS,
                COL_DISCUSSION))
        .addGroup(TBL_DISCUSSIONS_USERS, COL_DISCUSSION)
        .setWhere(SqlUtils.and(
            SqlUtils.notEqual(TBL_DISCUSSIONS, COL_OWNER, userId),
            SqlUtils.more(TBL_DISCUSSIONS, COL_CREATED,
                NewsHelper.getStartTime(startDate)),
            SqlUtils.or(
                SqlUtils.equals(TBL_DISCUSSIONS, COL_ACCESSIBILITY, true),
                SqlUtils.and(
                    SqlUtils.equals(TBL_DISCUSSIONS_USERS, CommonsConstants.COL_USER, userId),
                    SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_MEMBER, true))),
            SqlUtils.notNull(TBL_DISCUSSIONS, COL_TOPIC),
            SqlUtils.or(
                SqlUtils.and(
                    SqlUtils.moreEqual(TBL_DISCUSSIONS, COL_VISIBLE_TO, System
                        .currentTimeMillis()),
                    SqlUtils.lessEqual(TBL_DISCUSSIONS, COL_VISIBLE_FROM, System
                        .currentTimeMillis())
                    ),
                SqlUtils.or(
                    SqlUtils.equals(TBL_DISCUSSIONS, COL_VISIBLE_TO, nowStart),
                    SqlUtils.equals(TBL_DISCUSSIONS, COL_VISIBLE_FROM, nowStart)
                    ),
                SqlUtils.and(
                    SqlUtils.lessEqual(TBL_DISCUSSIONS, COL_VISIBLE_FROM, System
                        .currentTimeMillis()),
                    SqlUtils.isNull(TBL_DISCUSSIONS, COL_VISIBLE_TO)
                    ),
                SqlUtils.and(
                    SqlUtils.isNull(TBL_DISCUSSIONS, COL_VISIBLE_FROM),
                    SqlUtils.moreEqual(TBL_DISCUSSIONS, COL_VISIBLE_TO, nowFinish)
                    )
                )
            ));
    
    select.setUnionAllMode(true).addUnion(select2);

    String alias = SqlUtils.uniqueName();
    
    return new SqlSelect()
        .addFields(alias, COL_DISCUSSION)
        .addMax(alias, NewsConstants.COL_USAGE_UPDATE)
        .addFrom(select, alias)
        .addGroup(alias, COL_DISCUSSION);

  }

}
