package com.butent.bee.client.modules.discussions;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.crm.CrmConstants.COL_USER;
import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.discussions.DiscussionsConstants.DiscussionStatus;
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
        Filter isMemberFilter = Filter.isEqual(COL_MEMBER, BooleanValue.TRUE);
        Filter isOwner = Filter.isEqual(COL_OWNER, userId);
        Filter isPublic = Filter.notNull(COL_ACCESSIBILITY);
        Filter isUserFilter = Filter.isEqual(COL_USER, userId);
        Filter isActive = Filter.isEqual(COL_STATUS, IntegerValue.of(DiscussionStatus.ACTIVE));

        Filter discussUsersFilter = Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS),
            VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
            Filter.and(isUserFilter, isMemberFilter));
        return Filter.and(Filter.or(
            Filter.or(discussUsersFilter, isOwner), isPublic),
            isActive);
      }
    },
    CLOSED(Localized.getConstants().discussClosed()) {
      @Override
      Filter getFilter(LongValue userId) {
        Filter isMemberFilter = Filter.isEqual(COL_MEMBER, BooleanValue.TRUE);
        Filter isOwner = Filter.isEqual(COL_OWNER, userId);
        Filter isPublic = Filter.notNull(COL_ACCESSIBILITY);
        Filter isUserFilter = Filter.isEqual(COL_USER, userId);
        Filter isActive = Filter.isEqual(COL_STATUS, IntegerValue.of(DiscussionStatus.CLOSED));

        Filter discussUsersFilter = Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS),
            VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
            Filter.and(isUserFilter, isMemberFilter));
        return Filter.and(Filter.or(
            Filter.or(discussUsersFilter, isOwner), isPublic),
            isActive);
      }
    },
    OBSERVED(Localized.getConstants().discussObserved()) {
      @Override
      Filter getFilter(LongValue userId) {
        Filter activeStatusFilter = Filter.isEqual(COL_STATUS,
            IntegerValue.of(DiscussionStatus.ACTIVE));
        Filter notOwnerFilter = Filter.isNotEqual(COL_OWNER, userId);
        Filter isMemberFilter = Filter.isEqual(COL_MEMBER, BooleanValue.TRUE);
        Filter isUserFilter = Filter.isEqual(COL_USER, userId);
        Filter discussUsersFilter = Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS),
            VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
            Filter.and(isUserFilter, isMemberFilter));

        return Filter.and(activeStatusFilter, notOwnerFilter, discussUsersFilter);
      }
    },
    STARRED(Localized.getConstants().discussStarred()) {
      @Override
      Filter getFilter(LongValue userId) {
        Filter isPublic = Filter.notNull(COL_ACCESSIBILITY);
        Filter isMember =
            Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS), VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
                Filter.and(Filter.isEqual(COL_USER, userId),
                Filter.isEqual(COL_MEMBER, BooleanValue.TRUE)));
        
        Filter isStarred =
            Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS), VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
                Filter.and(Filter.notNull(COL_STAR),
                    Filter.isEqual(COL_USER, userId)));

        Filter notPublicIsMember = Filter.and(Filter.isNot(isPublic), isMember);
        Filter isPublicNotMember = Filter.and(isPublic, Filter.isNot(isMember));
        Filter isPublicIsMember = Filter.and(isPublic, isMember);

        return Filter.and(isStarred, Filter.or(Lists.newArrayList(notPublicIsMember,
            isPublicNotMember,
            isPublicIsMember)));
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
