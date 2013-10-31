package com.butent.bee.server.modules.discussions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class DiscussionsModuleBean implements BeeModule {

  private static final int MAX_NUMBERS_OF_ROWS = 100;

  private static BeeLogger logger = LogUtils.getLogger(DiscussionsModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @Resource
  EJBContext ctx;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result =
        qs.getSearchResults(VIEW_DISCUSSIONS, Filter.anyContains(Sets.newHashSet(COL_SUBJECT,
            COL_DESCRIPTION,
            COL_PUBLISHER, COL_CAPTION), query));

    return result;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;

    String svc = reqInfo.getParameter(DISCUSSIONS_METHOD);

    if (BeeUtils.isPrefix(svc, DISCUSSIONS_PREFIX)) {
      response = doDiscussionEvent(BeeUtils.removePrefix(svc, DISCUSSIONS_PREFIX), reqInfo);
    } else {
      String message = BeeUtils.joinWords("Discussion service not recognized:", svc);
      logger.warning(message);
      response = ResponseObject.error(message);
    }

    return response;
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

  private ResponseObject createDiscussionRelations(long discussionId,
      Map<String, String> properties) {
    int count = 0;

    if (BeeUtils.isEmpty(properties)) {
      return ResponseObject.response(count);
    }
    
    ResponseObject response = new ResponseObject();
    List<RowChildren> children = Lists.newArrayList();

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String relation = DiscussionsUtils.translateDiscussionPropertyToRelation(entry.getKey());

      if (BeeUtils.allNotEmpty(relation, entry.getValue())) {
        children.add(RowChildren.create(CommonsConstants.TBL_RELATIONS, COL_DISCUSSION, null,
            relation, entry.getValue()));
      }
    }

    if (!BeeUtils.isEmpty(children)) {
      count = deb.commitChildren(discussionId, children, response);
    }

    return response.setResponse(count);
  }

  private ResponseObject createDiscussionUser(long discussionId, long userId, Long time,
      boolean isMember) {
    SqlInsert insert = new SqlInsert(TBL_DISCUSSIONS_USERS)
        .addConstant(COL_DISCUSSION, discussionId)
        .addConstant(CommonsConstants.COL_USER, userId)
        .addConstant(COL_MEMBER, isMember);

    if (time != null) {
      insert.addConstant(COL_LAST_ACCESS, time);
    }

    return qs.insertDataWithResponse(insert);
  }

  private ResponseObject doDiscussionEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;
    DiscussionEvent event = NameUtils.getEnumByName(DiscussionEvent.class, svc);

    if (event == null) {
      String message = BeeUtils.joinWords("Discussion service not recognized:", svc);
      logger.warning(message);
      response = ResponseObject.error(message);
      return response;
    }

    String dataParam = reqInfo.getParameter(VAR_DISCUSSION_DATA);

    if (BeeUtils.isEmpty(dataParam)) {
      String msg = BeeUtils.joinWords("Discussion data not received:", svc, event);
      logger.warning(msg);
      response = ResponseObject.error(msg);
      return response;
    }

    BeeRowSet discussData = BeeRowSet.restore(dataParam);
    BeeRow discussRow = discussData.getRow(0);

    long discussionId = discussRow.getId();

    long currentUser = usr.getCurrentUserId();
    long now = System.currentTimeMillis();

    /*
     * Set<Long> oldMembers = DataUtils.parseIdSet(reqInfo.getParameter(VAR_DISCUSSION_USERS));
     * Set<String> updateRelations = NameUtils.toSet(reqInfo.getParameter(VAR_DISCUSSION_USERS));
     */

    switch (event) {
      case CREATE:
        Map<String, String> properties = discussRow.getProperties();
        List<Long> members = DataUtils.parseIdList(properties.get(PROP_MEMBERS));
        List<Long> discussions = Lists.newArrayList();

        BeeRow newRow = DataUtils.cloneRow(discussRow);
        DiscussionStatus status = DiscussionStatus.ACTIVE;

        newRow.setValue(discussData.getColumnIndex(COL_STATUS), status.ordinal());
        discussData.clearRows();
        discussData.addRow(newRow);
        response = deb.commitRow(discussData, false);

        if (response.hasErrors()) {
          break;
        }

        discussionId = ((BeeRow) response.getResponse()).getId();

        if (!response.hasErrors()) {
          response = createDiscussionUser(discussionId, currentUser, now, true);
        }

        if (!response.hasErrors()) {
          for (long memberId : members) {
            if (memberId != currentUser) {
              response = createDiscussionUser(discussionId, memberId, now, true);
              if (response.hasErrors()) {
                break;
              }
            }
          }
        }

        if (!response.hasErrors()) {
          response = createDiscussionRelations(discussionId, properties);
        }

        if (response.hasErrors()) {
          break;
        }

        if (!response.hasErrors()) {
          if (discussions.isEmpty()) {
            response = ResponseObject.error(usr.getLocalizableConstants().discussNotCreated());
          } else {
            response = ResponseObject.response(DataUtils.buildIdList(discussions));
          }
        }
        break;
      case ACTIVATE:
        break;
      case CLOSE:
        break;
      case COMMENT:
        break;
      case DEACTIVATE:
        break;
      case MARK:
        break;
      case MODIFY:
        break;
      case REPLY:
        break;
      case VISIT:
        break;
      default:
        break;
    }

    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }

    return response;
  }
}
