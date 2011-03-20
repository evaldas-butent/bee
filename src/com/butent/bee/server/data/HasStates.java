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

  IsCondition checkState(String stateAlias, BeeState state, long... bits);

  String getStateTable(BeeState state);

  SqlInsert insertState(long id, BeeState state, Map<Long, Boolean> bits);

  String joinState(HasFrom<?> query, String tblAlias, BeeState state);

  void setStateActive(BeeState state, String... fields);

  SqlUpdate updateState(long id, BeeState state, Map<Long, Boolean> bits);

  boolean updateStateActive(BeeState state, long... bits);

  SqlSelect verifyState(SqlSelect query, String tblAlias, BeeState state, long... bits);
}
