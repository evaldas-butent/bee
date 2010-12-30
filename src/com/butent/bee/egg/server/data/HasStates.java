package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.data.BeeTable.BeeState;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.SqlSelect;

public interface HasStates {

  void checkState(SqlSelect query, String tblAlias, BeeState state, int user, int... roles);

  String getStateTable(String stateName);

  String joinState(HasFrom<?> query, String tblAlias, BeeState state);
}
