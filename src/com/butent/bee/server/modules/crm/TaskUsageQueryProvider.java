package com.butent.bee.server.modules.crm;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.server.news.NewsHelper;
import com.butent.bee.server.news.UsageQueryProvider;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.time.DateTime;

public final class TaskUsageQueryProvider implements UsageQueryProvider {

  private static SqlSelect joinTasks(SqlSelect query, String table, String column) {
    return query.addFromInner(TBL_TASKS, SqlUtils.join(TBL_TASKS, COL_TASK_ID, table, column));
  }

  private static SqlSelect joinTasksToEvents(SqlSelect query) {
    return joinTasks(query, TBL_TASK_EVENTS, COL_TASK);
  }

  private static SqlSelect joinTasksToUsers(SqlSelect query) {
    return joinTasks(query, TBL_TASK_USERS, COL_TASK);
  }

  TaskUsageQueryProvider() {
    super();
  }

  @Override
  public SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId,
      DateTime startDate) {

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TASK_USERS, COL_TASK)
        .addMax(TBL_TASK_USERS, COL_LAST_ACCESS)
        .addFrom(TBL_TASK_USERS)
        .addGroup(TBL_TASK_USERS, COL_TASK);

    IsCondition where = SqlUtils.and(SqlUtils.equals(TBL_TASK_USERS, COL_USER, userId),
        SqlUtils.notNull(TBL_TASK_USERS, COL_LAST_ACCESS));

    switch (feed) {
      case TASKS_ASSIGNED:
        joinTasksToUsers(query).setWhere(SqlUtils.and(where,
            SqlUtils.equals(TBL_TASKS, COL_EXECUTOR, userId)));
        break;

      case TASKS_DELEGATED:
        joinTasksToUsers(query).setWhere(SqlUtils.and(where,
            SqlUtils.equals(TBL_TASKS, COL_OWNER, userId),
            SqlUtils.notEqual(TBL_TASKS, COL_EXECUTOR, userId)));
        break;
        
      case TASKS_OBSERVED:
        joinTasksToUsers(query).setWhere(SqlUtils.and(where,
            SqlUtils.notEqual(TBL_TASKS, COL_OWNER, userId),
            SqlUtils.notEqual(TBL_TASKS, COL_EXECUTOR, userId)));
        break;
        
      default:
        query.setWhere(where);
    }

    return query;
  }

  @Override
  public SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId,
      DateTime startDate) {

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TASK_EVENTS, COL_TASK)
        .addMax(TBL_TASK_EVENTS, COL_PUBLISH_TIME)
        .addFrom(TBL_TASK_EVENTS)
        .addGroup(TBL_TASK_EVENTS, COL_TASK);

    IsCondition where = SqlUtils.and(SqlUtils.notEqual(TBL_TASK_EVENTS, COL_PUBLISHER, userId),
        SqlUtils.more(TBL_TASK_EVENTS, COL_PUBLISH_TIME, NewsHelper.getStartTime(startDate)));

    switch (feed) {
      case TASKS_ASSIGNED:
        joinTasksToEvents(query).setWhere(SqlUtils.and(where,
            SqlUtils.equals(TBL_TASKS, COL_EXECUTOR, userId)));
        break;

      case TASKS_DELEGATED:
        joinTasksToEvents(query).setWhere(SqlUtils.and(where,
            SqlUtils.equals(TBL_TASKS, COL_OWNER, userId),
            SqlUtils.notEqual(TBL_TASKS, COL_EXECUTOR, userId)));
        break;
        
      case TASKS_OBSERVED:
        joinTasksToEvents(query).setWhere(SqlUtils.and(where,
            SqlUtils.notEqual(TBL_TASKS, COL_OWNER, userId),
            SqlUtils.notEqual(TBL_TASKS, COL_EXECUTOR, userId)));
        break;
        
      default:
        query.setWhere(where);
    }

    return query;
  }
}
