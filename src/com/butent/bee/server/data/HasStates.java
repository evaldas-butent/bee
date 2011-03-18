package com.butent.bee.server.data;

import com.butent.bee.shared.sql.HasFrom;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.SqlCreate;
import com.butent.bee.shared.sql.SqlInsert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUpdate;

import java.util.Map;

public interface HasStates {

  SqlCreate createStateTable(SqlCreate query, BeeState state);

  IsCondition checkState(String stateAlias, BeeState state, boolean mdRole, long... bits);

  String getStateField(BeeState state);

  String getStateTable(BeeState state);

  SqlInsert insertState(long id, BeeState state, Map<Long, Boolean> bits);

  String joinState(HasFrom<?> query, String tblAlias, BeeState state);

  void setStateActive(BeeState state, boolean active);

  SqlUpdate updateState(long id, BeeState state, Map<Long, Boolean> bits);

  void verifyState(SqlSelect query, String tblAlias, BeeState state, long user, long... roles);
}
