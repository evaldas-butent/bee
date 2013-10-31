package com.butent.bee.shared.modules.discussions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;

public final class DiscussionsUtils {

  private static final BiMap<String, String> discussionPropertyToRelation = HashBiMap.create();

  public static String translateDiscussionPropertyToRelation(String propertyName) {
    return ensureDiscussionPropertyToRelation().get(propertyName);
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
