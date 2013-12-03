package com.butent.bee.client.modules.discussions;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.crm.CrmConstants.COL_USER;
import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

final class DiscussionsList {

  public enum ListType implements HasCaption {

    ALL(Localized.getConstants().discussAll()) {
      @Override
      Filter getFilter(LongValue userId) {
        return null;
      }
    },
    ACTIVE(Localized.getConstants().discussActive()) {
      @Override
      Filter getFilter(LongValue userId) {
        return ComparisonFilter.isEqual(COL_STATUS, Value.getValue(DiscussionStatus.ACTIVE
            .ordinal()));
      }
    },
    CLOSED(Localized.getConstants().discussClosed()) {
      @Override
      Filter getFilter(LongValue userId) {
        return ComparisonFilter.isEqual(COL_STATUS, Value.getValue(DiscussionStatus.CLOSED
            .ordinal()));
      }
    },
    OBSERVED(Localized.getConstants().discussObserved()) {
      @Override
      Filter getFilter(LongValue userId) {
        Filter activeStatusFilter = ComparisonFilter.isEqual(COL_STATUS,
            Value.getValue(DiscussionStatus.ACTIVE.ordinal()));
        Filter notOwnerFilter = ComparisonFilter.isNotEqual(COL_OWNER, userId);
        Filter isMemberFilter = ComparisonFilter.isEqual(COL_MEMBER, Value.getValue(true));
        Filter isUserFilter = ComparisonFilter.isEqual(COL_USER, userId);
        Filter discussUsersFilter = Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS),
            VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
            ComparisonFilter.and(isUserFilter, isMemberFilter));

        return ComparisonFilter.and(activeStatusFilter, notOwnerFilter, discussUsersFilter);
      }
    },
    STARRED(Localized.getConstants().discussClosed()) {
      @Override
      Filter getFilter(LongValue userId) {
        Filter isMemberFilter = ComparisonFilter.isEqual(COL_MEMBER, Value.getValue(true));
        Filter isUserFilter = ComparisonFilter.isEqual(COL_USER, userId);
        Filter isStarred = ComparisonFilter.isEqual(COL_STAR, Value.getValue(true));
        Filter discussUsersFilter = Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS),
            VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
            ComparisonFilter.and(isUserFilter, isMemberFilter, isStarred));

        return discussUsersFilter;
      }
    };

    private final String caption;

    private ListType(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    abstract Filter getFilter(LongValue userId);
  }

  public static void open(String arg) {
    ListType list = null;

    for (ListType t : ListType.values()) {
      if (BeeUtils.startsSame(arg, t.name())) {
        list = t;
        break;
      }
    }

    if (list == null) {
      Global.showError(Lists.newArrayList(GRID_DISCUSSIONS, "Type not recognized:", arg));
    } else {
      GridFactory.openGrid(GRID_DISCUSSIONS, new DiscussionsGridHandler(list));
    }
  }

  private DiscussionsList() {
    super();
  }
}
