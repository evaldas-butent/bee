package com.butent.bee.client.modules.discussions;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
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
