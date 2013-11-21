package com.butent.bee.server.modules.discussions;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
import com.butent.bee.server.modules.commons.ExtensionIcons;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.HashMap;
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
    } else if (BeeUtils.same(svc, SVC_GET_DISCUSSION_DATA)) {
      response = getDiscussionData(reqInfo);
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
  
  private void addDiscussionProperties(BeeRow row, List<BeeColumn> columns,
      Collection<Long> discussionUsers, Long commentId) {
    long discussionId = row.getId();
    
    if (!BeeUtils.isEmpty(discussionUsers)) {
      discussionUsers.remove(row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns)));

      if (!discussionUsers.isEmpty()) {
        row.setProperty(PROP_MEMBERS, DataUtils.buildIdList(discussionUsers));
      }
    }

    Multimap<String, Long> discussionRelations = getDiscussionRelations(discussionId);
    for (String property : discussionRelations.keySet()) {
      row.setProperty(property, DataUtils.buildIdList(discussionRelations.get(property)));
    }

    List<StoredFile> files = getDiscussionFiles(discussionId);
    logger.info("GETTING FILES");
    if (!files.isEmpty()) {
      logger.info("GET FILES: ", Codec.beeSerialize(files));
      row.setProperty(PROP_FILES, Codec.beeSerialize(files));
    }
    
    BeeRowSet comments =
        qs.getViewData(VIEW_DISCUSSIONS_COMMENTS, ComparisonFilter.isEqual(COL_DISCUSSION,
            new LongValue(discussionId)));
    if (!DataUtils.isEmpty(comments)) {
      row.setProperty(PROP_COMMENTS, comments.serialize());
    }

    if (commentId != null) {
      row.setProperty(PROP_LAST_COMMENT, BeeUtils.toString(commentId));
    }
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

  private ResponseObject commitDiscussionComemnt(long discussionId, long userId,
      String comment, long mills) {
    SqlInsert si = new SqlInsert(TBL_DISCUSSIONS_COMMENTS)
        .addConstant(COL_DISCUSSION, discussionId)
        .addConstant(COL_PUBLISHER, userId)
        .addConstant(COL_PUBLISH_TIME, mills);

    if (!BeeUtils.isEmpty(comment)) {
      si.addConstant(COL_COMMENT_TEXT, comment);
    }

    return qs.insertDataWithResponse(si);
  }

  private ResponseObject commitDiscussionData(BeeRowSet data, Collection<Long> oldUsers,
      boolean checkUsers, Set<String> updatedRelations, Long commentId) {

    ResponseObject response;
    BeeRow row = data.getRow(0);

    List<Long> newUsers;

    if (checkUsers) {
      newUsers = DiscussionsUtils.getDiscussionMembers(row, data.getColumns());

      if (!BeeUtils.sameElements(oldUsers, newUsers)) {
        updateDiscussionUsers(row.getId(), oldUsers, newUsers);
      }
    } else {
      newUsers = Lists.newArrayList(oldUsers);
    }

    if (!BeeUtils.isEmpty(updatedRelations)) {
      updateDiscussionRelations(row.getId(), updatedRelations, row);
    }

    Map<Integer, String> shadow = row.getShadow();
    if (shadow != null && !shadow.isEmpty()) {
      List<BeeColumn> columns = Lists.newArrayList();
      List<String> oldValues = Lists.newArrayList();
      List<String> newValues = Lists.newArrayList();
      
      for (Map.Entry<Integer, String> entry : shadow.entrySet()) {
        columns.add(data.getColumn(entry.getKey()));

        oldValues.add(entry.getValue());
        newValues.add(row.getString(entry.getKey()));
      }

      BeeRow updRow = new BeeRow(row.getId(), row.getVersion(), oldValues);
      for (int i = 0; i < columns.size(); i++) {
        updRow.preliminaryUpdate(i, newValues.get(i));
      }

      BeeRowSet updated = new BeeRowSet(data.getViewName(), columns);
      updated.addRow(updRow);

      response = deb.commitRow(updated, true);
      if (!response.hasErrors() && response.hasResponse(BeeRow.class)) {
        addDiscussionProperties((BeeRow) response.getResponse(), data.getColumns(), newUsers,
            commentId);
      }
    } else {
      response = getDiscussionData(row.getId(), commentId);
    }

    return response;
  }

  private ResponseObject doDiscussionEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;
    DiscussionEvent event = EnumUtils.getEnumByName(DiscussionEvent.class, svc);

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

    Long commentId = null;

    String commentText = reqInfo.getParameter(VAR_DISCUSSION_COMMENT);

    Set<Long> oldMembers = DataUtils.parseIdSet(reqInfo.getParameter(VAR_DISCUSSION_USERS));
    Set<String> updatedRelations = NameUtils.toSet(reqInfo.getParameter(VAR_DISCUSSION_USERS));

    switch (event) {
      case CREATE:
        Map<String, String> properties = discussRow.getProperties();
        
        if (properties == null) {
          properties = new HashMap<>();
        }
        /*
         * logger.info("ROW", discussRow);
         */
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

        if (!response.hasErrors()) {
          discussions.add(discussionId);
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
      case DEACTIVATE:
        break;
      case MARK:
        break;

      case REPLY:
        break;
      case COMMENT:
      case MODIFY:
      case VISIT:
        if (oldMembers.contains(currentUser)) {
          response = registerDiscussionVisit(discussionId, currentUser, now);
        }

        if (!BeeUtils.isEmpty(commentText)) {
          response = commitDiscussionComemnt(discussionId, currentUser, commentText, now);
        }

        if (response == null || !response.hasErrors()) {
          response =
              commitDiscussionData(discussData, oldMembers, true, updatedRelations, commentId);
        }
        break;
      default:
        break;
    }

    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }

    return response;
  }

  private ResponseObject getDiscussionData(RequestInfo reqInfo) {
    long discussionId = BeeUtils.toLong(reqInfo.getParameter(VAR_DISCUSSION_ID));
    if (!DataUtils.isId(discussionId)) {
      String msg = BeeUtils.joinWords(SVC_GET_DISCUSSION_DATA, "discussion id not received");
      logger.warning(msg);
      return ResponseObject.error(msg);
    }

    return getDiscussionData(discussionId, null);
  }

  private ResponseObject getDiscussionData(long discussionId, Long commentId) {
    BeeRowSet rowSet = qs.getViewData(VIEW_DISCUSSIONS, ComparisonFilter.compareId(discussionId));
    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject.emptyResponse();
    }

    BeeRow data = rowSet.getRow(0);
    addDiscussionProperties(data, rowSet.getColumns(), getDiscussionMembers(discussionId),
        commentId);

    return ResponseObject.response(data);
  }

  private List<StoredFile> getDiscussionFiles(long discussionId) {
    List<StoredFile> result = Lists.newArrayList();

    BeeRowSet rowSet =
        qs.getViewData(VIEW_DISCUSSIONS_FILES, ComparisonFilter.isEqual(COL_DISCUSSION,
            new LongValue(discussionId)));

    /*
     * logger.debug("ROWSET", rowSet); logger.debug("DiscussionId:", discussionId);
     */

    if (rowSet == null || rowSet.isEmpty()) {
      return result;
    }

    for (BeeRow row : rowSet.getRows()) {
      StoredFile sf =
          new StoredFile(DataUtils.getLong(rowSet, row, COL_FILE), DataUtils.getString(rowSet, row,
              COL_FILE_NAME), DataUtils.getLong(rowSet, row,
              COL_FILE_SIZE), DataUtils.getString(rowSet, row,
              COL_FILE_TYPE));

      Long commentId = DataUtils.getLong(rowSet, row, COL_COMMENT);

      if (commentId != null) {
        sf.setRelatedId(commentId);
      }

      String caption = DataUtils.getString(rowSet, row, COL_CAPTION);

      if (!BeeUtils.isEmpty(caption)) {
        sf.setCaption(caption);
      }

      sf.setIcon(ExtensionIcons.getIcon(sf.getName()));
      result.add(sf);
    }

    return result;
  }

  private List<Long> getDiscussionMembers(long discussionId) {
    if (!DataUtils.isId(discussionId)) {
      return Lists.newArrayList();
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_DISCUSSIONS_USERS, CommonsConstants.COL_USER)
        .addFrom(TBL_DISCUSSIONS_USERS)
        .setWhere(
            SqlUtils.and(SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_DISCUSSION, discussionId),
                SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_MEMBER, VALUE_MEMBER))).addOrder(
            TBL_DISCUSSIONS_USERS, sys.getIdName(TBL_DISCUSSIONS_USERS));
    return Lists.newArrayList(qs.getLongColumn(query));
  }

  private Multimap<String, Long> getDiscussionRelations(long discussionId) {
    Multimap<String, Long> res = HashMultimap.create();

    for (String relation : DiscussionsUtils.getRelations()) {
      Long[] ids =
          qs.getRelatedValues(CommonsConstants.TBL_RELATIONS, COL_DISCUSSION, discussionId,
              relation);

      if (ids != null) {
        String property = DiscussionsUtils.translateRelationToDiscussionProperty(relation);

        for (Long id : ids) {
          res.put(property, id);
        }
      }
    }

    return res;
  }

  private ResponseObject registerDiscussionVisit(long discussionId, long userId, long mills) {
    IsCondition where =
        SqlUtils.and(
        SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_DISCUSSION, discussionId,
                CommonsConstants.COL_USER, userId), SqlUtils.equals(TBL_DISCUSSIONS_USERS,
                COL_MEMBER, new Integer(1)));

    return qs.updateDataWithResponse(new SqlUpdate(TBL_DISCUSSIONS_USERS).addConstant(
        COL_LAST_ACCESS, mills)
        .setWhere(where));
  }

  private ResponseObject updateDiscussionRelations(long discussionId, Set<String> updatedRelations,
      BeeRow row) {
    ResponseObject response = new ResponseObject();
    List<RowChildren> children = Lists.newArrayList();

    for (String property : updatedRelations) {
      String relation = DiscussionsUtils.translateDiscussionPropertyToRelation(property);

      if (!BeeUtils.isEmpty(relation)) {
        children.add(RowChildren.create(CommonsConstants.TBL_RELATIONS, COL_DISCUSSION,
            discussionId, relation, row.getProperty(property)));
      }
    }

    int count = 0;

    if (!BeeUtils.isEmpty(children)) {
      count = deb.commitChildren(discussionId, children, response);
    }

    return response.setResponse(count);
  }

  private void updateDiscussionUsers(long discussionId, Collection<Long> oldUsers,
      Collection<Long> newUsers) {
    List<Long> insert = Lists.newArrayList(newUsers);
    insert.removeAll(oldUsers);

    List<Long> delete = Lists.newArrayList(oldUsers);
    delete.removeAll(newUsers);

    for (Long user : insert) {
      createDiscussionUser(discussionId, user, null, true);
    }

    for (Long user : delete) {
      IsCondition condition =
          SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_DISCUSSION, discussionId,
              CommonsConstants.COL_USER, user);
      qs.updateData(new SqlDelete(TBL_DISCUSSIONS_USERS).setWhere(condition));
    }
  }
}
