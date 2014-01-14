package com.butent.bee.server.modules.discussions;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.ParameterEvent;
import com.butent.bee.server.modules.ParameterEventHandler;
import com.butent.bee.server.modules.commons.ExtensionIcons;
import com.butent.bee.server.sql.IsCondition;
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
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
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
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

@Stateless
@LocalBean
public class DiscussionsModuleBean implements BeeModule {

  private static final int MAX_NUMBERS_OF_ROWS = 100;
  private static final String IMG_LINK = "link";

  private static BeeLogger logger = LogUtils.getLogger(DiscussionsModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  ParamHolderBean prm;
  @Resource
  EJBContext ctx;

  @Resource
  TimerService timerService;

  private Timer discussTimer;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> result =
        qs.getSearchResults(VIEW_DISCUSSIONS, Filter.anyContains(Sets.newHashSet(COL_SUBJECT,
            COL_DESCRIPTION, ALS_OWNER_FIRST_NAME, ALS_OWNER_LAST_NAME), query));

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
    List<BeeParameter> params = Lists.newArrayList(
        BeeParameter.createText(DISCUSSIONS_MODULE, PRM_DISCUSS_ADMIN, false, ""),
        BeeParameter.createBoolean(DISCUSSIONS_MODULE, PRM_ALLOW_DELETE_OWN_COMMENTS, false, null),
        BeeParameter.createNumber(DISCUSSIONS_MODULE, PRM_DISCUSS_INACTIVE_TIME_IN_DAYS, false,
            null),
        BeeParameter.createText(DISCUSSIONS_MODULE, PRM_FORBIDDEN_FILES_EXTENTIONS, false, ""),
        BeeParameter.createNumber(DISCUSSIONS_MODULE, PRM_MAX_UPLOAD_FILE_SIZE, false, null)
        );

    return params;
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
    initTimer();

    prm.registerParameterEventHandler(new ParameterEventHandler() {
      @Subscribe
      public void initTimers(ParameterEvent event) {
        if (BeeUtils.same(event.getParameter(), PRM_DISCUSS_INACTIVE_TIME_IN_DAYS)) {
          initTimer();
        }
      }
    });

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
            int accessIndex = discussUsersData.getColumnIndex(COL_LAST_ACCESS);
            int starIndex = discussUsersData.getColumnIndex(COL_STAR);

            for (SimpleRow discussUserRow : discussUsersData) {
              long discussionId = discussUserRow.getLong(discussIndex);
              BeeRow row = rowSet.getRowById(discussionId);

              if (row == null) {
                continue;
              }

              row.setProperty(PROP_USER, BeeConst.STRING_PLUS);

              if (discussUserRow.getValue(accessIndex) != null) {
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

            Map<Long, Integer> markCounts = getDiscussionsMarksCount(discussionsIds);
            Map<Long, String> lastCommentData = getDisscussionsLastComment(discussionsIds);
            for (BeeRow row : rowSet.getRows()) {
              String markValue =
                  markCounts.get(row.getId()) != null ? BeeUtils
                      .toString(markCounts.get(row.getId())) : "0";
              row.setProperty(PROP_MARK_COUNT, markValue);

              String lastCommentVal = lastCommentData.get(row.getId());

              row.setProperty(PROP_LAST_COMMENT_DATA, lastCommentVal);
              
              String filesCountVal = "";
              if (BeeUtils.isPositive(rowSet.getColumnIndex(ALS_FILES_COUNT))) {
                filesCountVal = row.getString(rowSet.getColumnIndex(ALS_FILES_COUNT));
              }
              
              row.setProperty(PROP_FILES_COUNT, filesCountVal);

              String relValue = "";

              if (BeeUtils.isPositive(rowSet.getColumnIndex(ALS_RELATIONS_COUNT))) {
                int rc =
                    BeeUtils.unbox(row.getInteger(rowSet.getColumnIndex(ALS_RELATIONS_COUNT)));

                if (rc > 0) {
                  relValue += IMG_LINK;
                }
              }

              row.setProperty(PROP_RELATIONS_COUNT, relValue);
              
              if (BeeUtils.isEmpty(row.getProperty(PROP_USER))) {
                row.setProperty(PROP_USER, BeeConst.STRING_PLUS);
                createDiscussionUser(row.getId(), usr.getCurrentUserId(), null, false);
              }
            }
          }
        }
      }

      @Subscribe
      public void setMarkTypesRowProperties(ViewQueryEvent event) {
        if (event.isBefore()) {
          return;
        }

        if (BeeUtils.same(event.getTargetName(), VIEW_DISCUSSIONS_MARK_TYPES)) {
          BeeRowSet rowSet = event.getRowset();

          if (!rowSet.isEmpty()) {

            if (rowSet.getNumberOfRows() < MAX_NUMBERS_OF_ROWS) {
              for (BeeRow row : rowSet.getRows()) {
                row.setProperty(PROP_PREVIEW_IMAGE,
                    row.getString(rowSet.getColumnIndex(COL_IMAGE_RESOURCE_NAME)));
              }
            }
          }
        }
      }
    });

  }

  private void addDiscussionProperties(BeeRow row, List<BeeColumn> columns,
      Collection<Long> discussionUsers, Collection<Long> discussionMarks, Long commentId) {
    long discussionId = row.getId();

    if (!BeeUtils.isEmpty(discussionUsers)) {
      discussionUsers.remove(row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns)));

      if (!discussionUsers.isEmpty()) {
        row.setProperty(PROP_MEMBERS, DataUtils.buildIdList(discussionUsers));
      }
    }

    if (!BeeUtils.isEmpty(discussionMarks)) {
      row.setProperty(PROP_MARKS, DataUtils.buildIdList(discussionMarks));
    }

    SimpleRowSet markData = getDiscussionMarksData((List<Long>) discussionMarks);
    row.setProperty(PROP_MARK_DATA, markData.serialize());

    Multimap<String, Long> discussionRelations = getDiscussionRelations(discussionId);
    for (String property : discussionRelations.keySet()) {
      row.setProperty(property, DataUtils.buildIdList(discussionRelations.get(property)));
    }

    List<StoredFile> files = getDiscussionFiles(discussionId);
    if (!files.isEmpty()) {
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

    BeeRowSet rs = getDiscussionMarkTypeData();
    if (!rs.isEmpty()) {
      row.setProperty(PROP_MARK_TYPES, rs.serialize());
    }

    Collection<BeeParameter> discussModuleParams = prm.getModuleParameters(DISCUSSIONS_MODULE);
    if (!discussModuleParams.isEmpty()) {
      Map<String, String> paramsMap = Maps.newHashMap();

      for (BeeParameter p : discussModuleParams) {
        paramsMap.put(p.getName(), p.getValue());
      }

      row.setProperty(PROP_PARAMETERS, Codec.beeSerialize(paramsMap));
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

    SqlUpdate update = new SqlUpdate(TBL_DISCUSSIONS_USERS).addConstant(COL_MEMBER, isMember)
            .setWhere(
                SqlUtils.and(SqlUtils.equals(TBL_DISCUSSIONS_USERS, CommonsConstants.COL_USER,
                    userId), SqlUtils.equals(TBL_DISCUSSIONS_USERS, COL_DISCUSSION, discussionId)));

    if (time != null) {
      update.addConstant(COL_LAST_ACCESS, time);
    }

    ResponseObject resp = qs.updateDataWithResponse(update);

    if (!resp.hasResponse() || BeeUtils.unbox((Integer) resp.getResponse()) <= 0) {

      SqlInsert insert = new SqlInsert(TBL_DISCUSSIONS_USERS)
          .addConstant(COL_DISCUSSION, discussionId)
          .addConstant(CommonsConstants.COL_USER, userId)
          .addConstant(COL_MEMBER, isMember);

      if (time != null) {
        insert.addConstant(COL_LAST_ACCESS, time);
      }
      return qs.insertDataWithResponse(insert);
    }

    return resp;
  }

  private ResponseObject commitDiscussionComment(long discussionId, long userId,
      Long parentCommentId,
      String comment, long mills) {
    SqlInsert si = new SqlInsert(TBL_DISCUSSIONS_COMMENTS)
        .addConstant(COL_DISCUSSION, discussionId)
        .addConstant(COL_PUBLISHER, userId)
        .addConstant(COL_PARENT_COMMENT, parentCommentId)
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
        addDiscussionProperties((BeeRow) response.getResponse(), data.getColumns(), newUsers, null,
            commentId);
      }
    } else {
      response = getDiscussionData(row.getId(), commentId);
    }

    return response;
  }

  private ResponseObject deleteDiscussionComment(long discussionId, long commentId) {

    String reasonText =
        BeeUtils.joinWords("<i style=\"font-size: smaller; color:red\">(", usr
            .getLocalizableConstants().discussEventCommentDeleted()
            + " )</i>:", new DateTime().toString() + ",",
            usr.getCurrentUserData().getFirstName(), usr.getCurrentUserData().getLastName());

    SqlUpdate update =
        new SqlUpdate(TBL_DISCUSSIONS_COMMENTS)
            .addConstant(COL_REASON, reasonText)
            .addConstant(COL_DELETED, true)
            .setWhere(SqlUtils.and(SqlUtils.equals(
                TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION, discussionId), SqlUtils.equals(
                TBL_DISCUSSIONS_COMMENTS, sys.getIdName(TBL_DISCUSSIONS_COMMENTS), commentId)));

    return qs.updateDataWithResponse(update);
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
    Long parentComment = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_DISCUSSION_PARENT_COMMENT));
    Long deleteComment =
        BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_DISCUSSION_DELETED_COMMENT));
    Long markId = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_DISCUSSION_MARK));
    Long markedComment = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_DISCUSSION_MARKED_COMMENT));

    Set<Long> oldMembers = DataUtils.parseIdSet(reqInfo.getParameter(VAR_DISCUSSION_USERS));
    Set<String> updatedRelations = NameUtils.toSet(reqInfo.getParameter(VAR_DISCUSSION_USERS));
    switch (event) {
      case CREATE:
        Map<String, String> properties = discussRow.getProperties();

        if (properties == null) {
          properties = new HashMap<>();
        }

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

      case DEACTIVATE:
        break;
      case VISIT:
        if (oldMembers.contains(currentUser)) {
          response = registerDiscussionVisit(discussionId, currentUser, now);
        }

        if (response == null || !response.hasErrors()) {
          response =
              commitDiscussionData(discussData, oldMembers, false, updatedRelations, commentId);
        }

        break;
      case COMMENT_DELETE:
      case ACTIVATE:
      case CLOSE:
      case COMMENT:
      case MARK:
      case MODIFY:
      case REPLY:
        if (!BeeUtils.isEmpty(commentText)) {
          response =
              commitDiscussionComment(discussionId, currentUser, parentComment, commentText, now);
        }

        if (response != null) {
          if (response.hasResponse(Long.class)) {
            commentId = (Long) response.getResponse();
          }
        }

        if ((response == null || !response.hasErrors()) && DataUtils.isId(deleteComment)) {
          response = deleteDiscussionComment(discussionId, deleteComment);
        }

        if ((response == null || !response.hasErrors()) && DataUtils.isId(markId)) {
          response = doMark(discussionId, markedComment, markId, currentUser);
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

  @Timeout
  private void doInactiveDiscussions() {
    Long days = prm.getLong(PRM_DISCUSS_INACTIVE_TIME_IN_DAYS);

    if (!BeeUtils.isPositive(days)) {
      logger.info("No value set for discussion deactyvation");
      return;
    }
    SqlSelect select =
        new SqlSelect().addField(TBL_DISCUSSIONS, sys.getIdName(TBL_DISCUSSIONS), sys
            .getIdName(TBL_DISCUSSIONS))
            .addMax(TBL_DISCUSSIONS_COMMENTS, COL_PUBLISH_TIME, ALS_LAST_COMMET)
            .addFrom(TBL_DISCUSSIONS)
            .addFromLeft(TBL_DISCUSSIONS_COMMENTS,
                sys.joinTables(TBL_DISCUSSIONS, TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION))
            .setWhere(SqlUtils.and(
                SqlUtils.equals(TBL_DISCUSSIONS, COL_STATUS, DiscussionStatus.ACTIVE.ordinal()),
                SqlUtils.notNull(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION)))
            .addGroup(TBL_DISCUSSIONS, sys.getIdName(TBL_DISCUSSIONS));

    SimpleRowSet activeDiscussions = qs.getData(select);

    if (activeDiscussions.isEmpty()) {
      logger.info("No value set for discussion deactyvation");
      return;
    }

    JustDate dacyvationTime =
        new JustDate(System.currentTimeMillis() - (days * TimeUtils.MILLIS_PER_DAY));

    int count = 0;
    for (int i = 0; i < activeDiscussions.getNumberOfRows(); i++) {

      if (activeDiscussions.getDate(i, ALS_LAST_COMMET).compareTo(dacyvationTime) < 0) {
        SqlUpdate update =
            new SqlUpdate(TBL_DISCUSSIONS).addConstant(COL_STATUS,
                DiscussionStatus.INACTIVE.ordinal())
                .setWhere(SqlUtils.equals(TBL_DISCUSSIONS, sys.getIdName(TBL_DISCUSSIONS),
                    activeDiscussions.getLong(i, sys.getIdName(TBL_DISCUSSIONS))));

        count += qs.updateData(update);
      }
    }

    logger.info("Setting inactive discussions", count);
  }

  private ResponseObject doMark(Long discussionId, Long commentId, Long markId, Long userId) {
    SqlInsert insert =
        new SqlInsert(TBL_DISCUSSIONS_COMMENTS_MARKS).addConstant(COL_DISCUSSION, discussionId)
            .addConstant(COL_COMMENT, commentId)
            .addConstant(COL_MARK, markId)
            .addConstant(CommonsConstants.COL_USER, userId);

    return qs.insertDataWithResponse(insert);
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
        getDiscussionMarks(discussionId),
        commentId);

    return ResponseObject.response(data);
  }

  private List<StoredFile> getDiscussionFiles(long discussionId) {
    List<StoredFile> result = Lists.newArrayList();

    BeeRowSet rowSet =
        qs.getViewData(VIEW_DISCUSSIONS_FILES, ComparisonFilter.isEqual(COL_DISCUSSION,
            new LongValue(discussionId)));

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

  private BeeRowSet getDiscussionMarkTypeData() {
    return qs.getViewData(VIEW_DISCUSSIONS_MARK_TYPES);
  }

  private List<Long> getDiscussionMarks(long discussionId) {
    if (!DataUtils.isId(discussionId)) {
      return Lists.newArrayList();
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_DISCUSSIONS_COMMENTS_MARKS, sys.getIdName(TBL_DISCUSSIONS_COMMENTS_MARKS))
        .addFrom(TBL_DISCUSSIONS_COMMENTS_MARKS)
        .setWhere(
            SqlUtils.equals(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_DISCUSSION,
                discussionId)).addOrder(
            TBL_DISCUSSIONS_COMMENTS_MARKS, sys.getIdName(TBL_DISCUSSIONS_COMMENTS_MARKS));

    Long[] result = qs.getLongColumn(query);
    List<Long> markList = Lists.newArrayList();

    if (BeeUtils.isPositive(result.length)) {
      markList = Lists.newArrayList(result);
    }

    return markList;
  }

  private Map<Long, String> getDisscussionsLastComment(Set<Long> discussionIds) {
    Map<Long, String> ls = Maps.newHashMap();

    SqlSelect select =
        new SqlSelect()
            .addField(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION, COL_DISCUSSION)
            .addMax(TBL_DISCUSSIONS_COMMENTS, sys.getIdName(TBL_DISCUSSIONS_COMMENTS),
                ALS_LAST_COMMET)

            .addFrom(TBL_DISCUSSIONS)
            .addFromLeft(TBL_DISCUSSIONS_COMMENTS,
                sys.joinTables(TBL_DISCUSSIONS, TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION))
            .setWhere(
                SqlUtils
                    .and(SqlUtils.notNull(TBL_DISCUSSIONS_COMMENTS, sys
                        .getIdName(TBL_DISCUSSIONS_COMMENTS)),
                        SqlUtils.inList(TBL_DISCUSSIONS, sys.getIdName(TBL_DISCUSSIONS),
                            discussionIds)))
            .addGroup(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION);

    SimpleRowSet maxComments = qs.getData(select);

    if (maxComments.isEmpty()) {
      return ls;
    }

    select =
        new SqlSelect()
            .addField(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION, COL_DISCUSSION)
            .addField(TBL_DISCUSSIONS_COMMENTS, COL_PUBLISH_TIME, COL_PUBLISH_TIME)
            .addField(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_FIRST_NAME,
                CommonsConstants.COL_FIRST_NAME)
            .addField(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_LAST_NAME,
                CommonsConstants.COL_LAST_NAME)
            .addFrom(TBL_DISCUSSIONS)
            .addFromLeft(TBL_DISCUSSIONS_COMMENTS,
                sys.joinTables(TBL_DISCUSSIONS, TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION))
            .addFromLeft(
                CommonsConstants.TBL_USERS,
                sys.joinTables(CommonsConstants.TBL_USERS, TBL_DISCUSSIONS_COMMENTS,
                    COL_PUBLISHER))
            .addFromLeft(
                CommonsConstants.TBL_COMPANY_PERSONS,
                sys.joinTables(CommonsConstants.TBL_COMPANY_PERSONS, CommonsConstants.TBL_USERS,
                    CommonsConstants.COL_COMPANY_PERSON))
            .addFromLeft(
                CommonsConstants.TBL_PERSONS,
                sys.joinTables(CommonsConstants.TBL_PERSONS, CommonsConstants.TBL_COMPANY_PERSONS,
                    CommonsConstants.COL_PERSON))
            .setWhere(
                SqlUtils
                    .and(SqlUtils.notNull(TBL_DISCUSSIONS_COMMENTS, sys
                        .getIdName(TBL_DISCUSSIONS_COMMENTS)),
                        SqlUtils.inList(TBL_DISCUSSIONS_COMMENTS,
                            sys.getIdName(TBL_DISCUSSIONS_COMMENTS),
                            (Object[]) maxComments.getColumn(maxComments
                                .getColumnIndex(ALS_LAST_COMMET))),
                        SqlUtils.inList(TBL_DISCUSSIONS, sys.getIdName(TBL_DISCUSSIONS),
                            (Object[]) maxComments.getColumn(maxComments
                                .getColumnIndex(COL_DISCUSSION)))));
    /* .addGroup(TBL_DISCUSSIONS_COMMENTS, COL_DISCUSSION); */

    SimpleRowSet rs = qs.getData(select);

    for (String[] row : rs.getRows()) {
      ls.put(BeeUtils.toLong(row[rs.getColumnIndex(COL_DISCUSSION)]),
          BeeUtils.joinWords(TimeUtils.renderDateTime(BeeUtils.unbox(BeeUtils.toLong(row[rs
              .getColumnIndex(COL_PUBLISH_TIME)])))
              + ",", row[rs
              .getColumnIndex(CommonsConstants.COL_FIRST_NAME)], row[rs
              .getColumnIndex(CommonsConstants.COL_LAST_NAME)]));
    }

    return ls;
  }

  private Map<Long, Integer> getDiscussionsMarksCount(Set<Long> discussionIds) {
    SqlSelect select = new SqlSelect()
        .addField(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_DISCUSSION, COL_DISCUSSION)
        .addCount(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_MARK, PROP_MARK_COUNT)
        .addFrom(TBL_DISCUSSIONS_COMMENTS_MARKS)
        .setWhere(SqlUtils.and(SqlUtils.isNull(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_COMMENT),
            SqlUtils.inList(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_DISCUSSION, discussionIds)))
        .addGroup(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_DISCUSSION);

    SimpleRowSet rs = qs.getData(select);
    Map<Long, Integer> ls = Maps.newHashMap();

    for (String[] row : rs.getRows()) {
      ls.put(BeeUtils.toLong(row[rs.getColumnIndex(COL_DISCUSSION)]),
          BeeUtils.toInt(row[rs.getColumnIndex(PROP_MARK_COUNT)]));
    }

    return ls;
  }

  private SimpleRowSet getDiscussionMarksData(List<Long> filter) {
    SqlSelect select = new SqlSelect()
        .addField(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_DISCUSSION, COL_DISCUSSION)
        .addField(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_COMMENT, COL_COMMENT)
        .addField(TBL_DISCUSSIONS_COMMENTS_MARKS, COL_MARK, COL_MARK)
        .addField(TBL_DISCUSSIONS_COMMENTS_MARKS, CommonsConstants.COL_USER,
            CommonsConstants.COL_USER)
        .addField(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_FIRST_NAME,
            CommonsConstants.COL_FIRST_NAME)
        .addField(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_LAST_NAME,
            CommonsConstants.COL_LAST_NAME)
        .addFrom(TBL_DISCUSSIONS_COMMENTS_MARKS)
        .addFromLeft(CommonsConstants.TBL_USERS,
            sys.joinTables(CommonsConstants.TBL_USERS, TBL_DISCUSSIONS_COMMENTS_MARKS,
                CommonsConstants.COL_USER))
        .addField(TBL_COMMENTS_MARK_TYPES, COL_MARK_NAME, COL_MARK_NAME)
        .addFromLeft(CommonsConstants.TBL_COMPANY_PERSONS,
            sys.joinTables(CommonsConstants.TBL_COMPANY_PERSONS, CommonsConstants.TBL_USERS,
                CommonsConstants.COL_COMPANY_PERSON))
        .addFromLeft(CommonsConstants.TBL_PERSONS,
            sys.joinTables(CommonsConstants.TBL_PERSONS, CommonsConstants.TBL_COMPANY_PERSONS,
                CommonsConstants.COL_PERSON))
        .addFromLeft(TBL_COMMENTS_MARK_TYPES,
            sys.joinTables(TBL_COMMENTS_MARK_TYPES, TBL_DISCUSSIONS_COMMENTS_MARKS, COL_MARK));

    select.setWhere(SqlUtils.sqlTrue());

    if (!BeeUtils.isEmpty(filter)) {
      select.setWhere(SqlUtils.and(select.getWhere(), SqlUtils.inList(
          TBL_DISCUSSIONS_COMMENTS_MARKS, sys
              .getIdName(TBL_DISCUSSIONS_COMMENTS_MARKS),
          filter)));
    } else {
      select.setWhere(SqlUtils.sqlFalse());
    }

    SimpleRowSet rs = qs.getData(select);

    return rs;
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

  private void initTimer() {
    Integer days = prm.getInteger(PRM_DISCUSS_INACTIVE_TIME_IN_DAYS);

    boolean timerExists = discussTimer != null;

    if (timerExists) {
      try {
        discussTimer.cancel();
      } catch (NoSuchObjectLocalException er) {
        logger.error(er);
      }

      discussTimer = null;
    }

    if (BeeUtils.isPositive(days)) {
      discussTimer =
          timerService.createIntervalTimer(DEFAUT_DISCCUSS_TIMER_TIMEOUT,
              DEFAUT_DISCCUSS_TIMER_TIMEOUT, new TimerConfig(null, false));

      logger.info("Created DISCUSSION refresh timer starting at",
          discussTimer.getNextTimeout());
    } else {
      if (timerExists) {
        logger.info("Removed DISCUSSION timer");
      }
    }

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
      qs.updateData(new SqlUpdate(TBL_DISCUSSIONS_USERS).addConstant(COL_MEMBER, false).setWhere(
          condition));
    }
  }

}
