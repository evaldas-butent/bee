package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

class UserGroupMembersInterceptor extends AbstractGridInterceptor {

  UserGroupMembersInterceptor() {
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    final MultiSelector selector = MultiSelector.autonomous(VIEW_USERS,
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME));

    List<? extends IsRow> data = presenter.getGridView().getRowData();

    if (!BeeUtils.isEmpty(data)) {
      Set<Long> members = Sets.newHashSet();
      int userIndex = getDataIndex(COL_UG_USER);

      for (IsRow row : data) {
        Long member = row.getLong(userIndex);
        if (member != null) {
          members.add(member);
        }
      }

      if (!members.isEmpty()) {
        selector.getOracle().setExclusions(members);
      }
    }

    Global.inputWidget(Localized.getConstants().users(), selector, new InputCallback() {
      @Override
      public void onSuccess() {
        List<Long> users = DataUtils.parseIdList(selector.getValue());
        if (!users.isEmpty()) {
          addMembers(users);
        }
      }
    }, "bee-" + GRID_USER_GROUP_MEMBERS + "-add", presenter.getHeader().getElement());

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new UserGroupMembersInterceptor();
  }

  private void addMembers(final List<Long> users) {
    getGridView().ensureRelId(new IdCallback() {
      @Override
      public void onSuccess(final Long group) {
        if (DataUtils.isId(group)) {
          List<BeeColumn> columns = DataUtils.getColumns(getDataColumns(),
              Lists.newArrayList(COL_UG_GROUP, COL_UG_USER));

          for (Long user : users) {
            Queries.insert(getViewName(), columns, Queries.asList(group, user), null,
                new RowCallback() {
                  @Override
                  public void onSuccess(BeeRow row) {
                    RowInsertEvent.fire(BeeKeeper.getBus(), getViewName(), row, null);
                  }
                });
          }
        }
      }
    });
  }
}
