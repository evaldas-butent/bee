package com.butent.bee.shared.modules.crm;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

public enum TaskType implements HasCaption {
  ASSIGNED(Localized.getConstants().crmTasksAssignedTasks(), null /* Feed.TASKS_ASSIGNED */) {
    @Override
    public Filter getFilter(LongValue userValue) {
      return Filter.isEqual(COL_EXECUTOR, userValue);
    }
  },

  DELEGATED(Localized.getConstants().crmTasksDelegatedTasks(), null /* Feed.TASKS_DELEGATED */) {
    @Override
    public Filter getFilter(LongValue userValue) {
      return Filter.and(Filter.isEqual(COL_OWNER, userValue),
          Filter.isNotEqual(COL_EXECUTOR, userValue));
    }
  },

  OBSERVED(Localized.getConstants().crmTasksObservedTasks(), null /* Feed.TASKS_OBSERVED */) {
    @Override
    public Filter getFilter(LongValue userValue) {
      return Filter.and(Filter.isNotEqual(COL_OWNER, userValue),
          Filter.isNotEqual(COL_EXECUTOR, userValue),
          Filter.in(COL_TASK_ID, VIEW_TASK_USERS, COL_TASK, Filter.isEqual(COL_USER, userValue)));
    }
  },

  ALL(Localized.getConstants().crmTasksAll(), null /* Feed.TASKS_ALL */) {
    @Override
    public Filter getFilter(LongValue userValue) {
      return null;
    }
  };

  public static TaskType getByFeed(Feed input) {
    for (TaskType type : values()) {
      if (type.feed == input) {
        return type;
      }
    }
    return null;
  }

  public static TaskType getByPrefix(String input) {
    for (TaskType type : values()) {
      if (BeeUtils.startsSame(type.name(), input)) {
        return type;
      }
    }
    return null;
  }
  
  private final String caption;
  private final Feed feed;

  private TaskType(String caption, Feed feed) {
    this.caption = caption;
    this.feed = feed;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public abstract Filter getFilter(LongValue userValue);
}
