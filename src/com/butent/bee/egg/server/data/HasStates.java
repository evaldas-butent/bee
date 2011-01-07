package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.data.BeeTable.BeeState;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;

import java.util.Collection;
import java.util.Map;

public interface HasStates {

  SqlCreate createStateTable(SqlCreate query, BeeState state,
      Collection<Integer> users, Collection<Integer> roles);

  IsCondition checkState(String stateAlias, BeeState state, boolean mdRole, int... bits);

  String getStateField(String stateName);

  String getStateTable(String stateName);

  SqlInsert insertState(long id, BeeState state, boolean mdRole, Map<Integer, Boolean> bits);

  String joinState(HasFrom<?> query, String tblAlias, BeeState state);

  SqlUpdate updateState(long id, BeeState state, boolean mdRole, Map<Integer, Boolean> bits);

  void verifyState(SqlSelect query, String tblAlias, BeeState state, int user, int... roles);
}
