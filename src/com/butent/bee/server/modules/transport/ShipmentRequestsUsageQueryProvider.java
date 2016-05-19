package com.butent.bee.server.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.Invocation;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.news.NewsHelper;
import com.butent.bee.server.news.UsageQueryProvider;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.time.DateTime;

public class ShipmentRequestsUsageQueryProvider implements UsageQueryProvider {

  @Override
  public SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId,
      DateTime startDate) {

    String usage = feed.getUsageTable();
    NewsBean news = Invocation.locateRemoteBean(NewsBean.class);

    return new SqlSelect()
        .addFields(usage, relationColumn, NewsConstants.COL_USAGE_ACCESS)
        .addFrom(usage)
        .addFromInner(TBL_SHIPMENT_REQUESTS, SqlUtils.and(news.joinUsage(TBL_SHIPMENT_REQUESTS),
            SqlUtils.or(SqlUtils.notEqual(TBL_SHIPMENT_REQUESTS, COL_QUERY_STATUS,
                    TransportConstants.ShipmentRequestStatus.LOST),
                SqlUtils.notNull(TBL_SHIPMENT_REQUESTS, COL_QUERY_REASON))))
        .setWhere(SqlUtils.and(SqlUtils.equals(usage, NewsConstants.COL_USAGE_USER, userId),
            SqlUtils.equals(TBL_SHIPMENT_REQUESTS, COL_QUERY_MANAGER, userId),
            feed.in(Feed.SHIPMENT_REQUESTS_UNREGISTERED_MY)
                ? SqlUtils.isNull(TBL_SHIPMENT_REQUESTS, ClassifierConstants.COL_COMPANY_PERSON)
                : SqlUtils.notNull(TBL_SHIPMENT_REQUESTS, ClassifierConstants.COL_COMPANY_PERSON)));
  }

  @Override
  public SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId,
      DateTime startDate) {

    String usage = feed.getUsageTable();
    NewsBean news = Invocation.locateRemoteBean(NewsBean.class);
    SystemBean sys = Invocation.locateRemoteBean(SystemBean.class);

    IsCondition lost = SqlUtils.and(SqlUtils.equals(TBL_SHIPMENT_REQUESTS, COL_QUERY_STATUS,
        ShipmentRequestStatus.LOST), SqlUtils.isNull(TBL_SHIPMENT_REQUESTS,
        COL_QUERY_REASON));

    return new SqlSelect()
        .addFields(TBL_SHIPMENT_REQUESTS, sys.getIdName(TBL_SHIPMENT_REQUESTS))
        .addMax(SqlUtils.sqlIf(lost, SqlUtils.constant(System.currentTimeMillis()),
                SqlUtils.field(usage, NewsConstants.COL_USAGE_UPDATE)),
            NewsConstants.COL_USAGE_UPDATE)
        .addFrom(TBL_SHIPMENT_REQUESTS)
        .addFromLeft(usage, news.joinUsage(TBL_SHIPMENT_REQUESTS))
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_SHIPMENT_REQUESTS, COL_QUERY_MANAGER, userId),
            feed.in(Feed.SHIPMENT_REQUESTS_UNREGISTERED_MY)
                ? SqlUtils.isNull(TBL_SHIPMENT_REQUESTS, ClassifierConstants.COL_COMPANY_PERSON)
                : SqlUtils.notNull(TBL_SHIPMENT_REQUESTS, ClassifierConstants.COL_COMPANY_PERSON),
            SqlUtils.or(lost, NewsHelper.getUpdatesCondition(usage, userId, startDate))))
        .addGroup(TBL_SHIPMENT_REQUESTS, sys.getIdName(TBL_SHIPMENT_REQUESTS));
  }
}
