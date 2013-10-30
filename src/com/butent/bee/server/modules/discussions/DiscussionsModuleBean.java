package com.butent.bee.server.modules.discussions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class DiscussionsModuleBean implements BeeModule {

  private static final int MAX_NUMBERS_OF_ROWS = 100;

  @SuppressWarnings("unused")
  private static BeeLogger logger = LogUtils.getLogger(DiscussionsModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    return null;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public String getName() {
    return DISCUSSIONS_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {

      @Subscribe
      public void setRowProperties(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (BeeUtils.same(event.getTargetName(), VIEW_DISCUSSIONS)) {
          BeeRowSet rowSet = event.getRowset();

          if (!rowSet.isEmpty()) {
            Set<Long> discussionsIds = Sets.newHashSet();

            if (rowSet.getNumberOfRows() < MAX_NUMBERS_OF_ROWS) {
              for (BeeRow row : rowSet.getRows()) {
                discussionsIds.add(row.getId());
              }
            }

            SqlSelect discussUsers = new SqlSelect()
                .addFields(TBL_DISCUSSIONS_USERS, COL_DISCUSSION, COL_LAST_ACCESS, COL_STAR)
                .addFrom(TBL_DISCUSSIONS_USERS);

            IsCondition usersWhere =
                SqlUtils.equals(TBL_DISCUSSIONS_USERS, CommonsConstants.COL_USER, usr
                    .getCurrentUserId());

            discussUsers.setWhere(usersWhere);

            if (!discussionsIds.isEmpty()) {
              discussUsers.setWhere(SqlUtils.and(usersWhere, SqlUtils.inList(TBL_DISCUSSIONS_USERS,
                  COL_DISCUSSION, discussionsIds)));
            }

            SimpleRowSet discussUsersData = qs.getData(discussUsers);

            int discussIndex = discussUsersData.getColumnIndex(COL_DISCUSSION);
            int acessIndex = discussUsersData.getColumnIndex(COL_LAST_ACCESS);
            int starIndex = discussUsersData.getColumnIndex(COL_STAR);
            
            for (SimpleRow discussUserRow : discussUsersData) {
              long discussionId = discussUserRow.getLong(discussIndex);
              BeeRow row = rowSet.getRowById(discussionId);
              
              if (row == null) {
                continue;
              }

              row.setProperty(PROP_USER, BeeConst.STRING_PLUS);
              
              if (discussUserRow.getValue(acessIndex) != null) {
                row.setProperty(PROP_LAST_ACCESS, discussUserRow.getValue(starIndex));
              }
              if (discussUserRow.getValue(starIndex) != null) {
                row.setProperty(PROP_STAR, discussUserRow.getValue(starIndex));
              }
            }
            
            SqlSelect discussionsEvents = new SqlSelect()
                .addFields(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION)
                .addMax(TBL_DISCUSSIONS_COMMENTS, COL_PUBLISH_TIME)
                .addFrom(TBL_DISCUSSIONS_COMMENTS)
                .addGroup(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION);

            if (!discussionsIds.isEmpty()) {
              discussionsEvents.setWhere(SqlUtils.inList(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION,
                  discussionsIds));
            }

            SimpleRowSet discussEventsData = qs.getData(discussionsEvents);
            discussIndex = discussEventsData.getColumnIndex(COL_DISCUSSION);
            int publishIndex = discussEventsData.getColumnIndex(COL_PUBLISH_TIME);

            for (SimpleRow discussEventRow : discussEventsData) {
              long discussionId = discussEventRow.getLong(discussIndex);
              BeeRow row = rowSet.getRowById(discussionId);
              
              if (discussEventRow.getValue(publishIndex) != null) {
                row.setProperty(PROP_LAST_PUBLISH, discussEventRow.getValue(publishIndex));
              }
            }
          }
        }
      }
    });
  }

}
