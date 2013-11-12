package com.butent.bee.shared.modules.discussions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;

import java.util.List;
import java.util.Set;

public final class DiscussionsUtils {

  private static final BiMap<String, String> discussionPropertyToRelation = HashBiMap.create();
  
  public static List<Long> getDiscussionMembers(IsRow row, List<BeeColumn> columns) {
    List<Long> users = Lists.newArrayList();

    Long owner = row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns));
    if (owner != null) {
      users.add(owner);
    }

    List<Long> members = DataUtils.parseIdList(row.getProperty(PROP_MEMBERS));

    LogUtils.getRootLogger().debug("PROP_MEMBERS: ", row.getProperty(PROP_MEMBERS));
    for (Long member : members) {
      if (!users.contains(member)) {
        users.add(member);
      }
    }

    return users;
  }

  public static Set<String> getRelations() {
    return ensureDiscussionPropertyToRelation().inverse().keySet();
  }

  public static boolean sameMembers(IsRow oldRow, IsRow newRow) {
    if (oldRow == null || newRow == null) {
      return false;
    } else {
      return DataUtils
          .sameIdSet(oldRow.getProperty(PROP_MEMBERS), newRow.getProperty(PROP_MEMBERS));
    }
  }

  public static String translateDiscussionPropertyToRelation(String propertyName) {
    return ensureDiscussionPropertyToRelation().get(propertyName);
  }

  public static String translateRelationToDiscussionProperty(String relation) {
    return ensureDiscussionPropertyToRelation().inverse().get(relation);
  }

  private static BiMap<String, String> ensureDiscussionPropertyToRelation() {
    if (discussionPropertyToRelation.isEmpty()) {
      discussionPropertyToRelation.put(PROP_COMPANIES, CommonsConstants.COL_COMPANY);
      discussionPropertyToRelation.put(PROP_PERSONS, CommonsConstants.COL_PERSON);
      discussionPropertyToRelation.put(PROP_APPOINTMENTS, CalendarConstants.COL_APPOINTMENT);
      discussionPropertyToRelation.put(PROP_TASKS, CrmConstants.COL_TASK);
      discussionPropertyToRelation.put(PROP_DOCUMENTS, CrmConstants.COL_DOCUMENT);
    }

    return discussionPropertyToRelation;
  }

  private DiscussionsUtils() {
  }
}
