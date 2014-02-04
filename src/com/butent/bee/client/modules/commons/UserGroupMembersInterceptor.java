package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

class UserGroupMembersInterceptor extends AbstractGridInterceptor {

  UserGroupMembersInterceptor() {
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    Relation relation = Relation.create();
    relation.setViewName(VIEW_USERS);

    relation.getChoiceColumns().add(COL_FIRST_NAME);
    relation.getChoiceColumns().add(COL_LAST_NAME);
    relation.getChoiceColumns().add(ALS_COMPANY_NAME);
    relation.getChoiceColumns().add(ALS_POSITION_NAME);

    relation.getSearchableColumns().addAll(relation.getChoiceColumns());

    final MultiSelector selector = MultiSelector.autonomous(relation,
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME));
    
    int width = presenter.getGridView().asWidget().getOffsetWidth();
    StyleUtils.setWidth(selector, BeeUtils.clamp(width - 50, 300, 600));

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

    Global.inputWidget(Localized.getConstants().userGroupAddMembers(), selector,
        new InputCallback() {
          @Override
          public void onSuccess() {
            List<Long> users = DataUtils.parseIdList(selector.getValue());
            if (!users.isEmpty()) {
              addMembers(users);
            }
          }
        }, null, presenter.getHeader().getElement());

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
          BeeRowSet rowSet = new BeeRowSet(getViewName(), columns);

          for (Long user : users) {
            rowSet.addRow(DataUtils.NEW_ROW_ID, DataUtils.NEW_ROW_VERSION,
                Queries.asList(group, user));
          }
          Queries.insertRows(rowSet);
        }
      }
    });
  }
}
