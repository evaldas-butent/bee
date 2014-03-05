package com.butent.bee.server.data;

import com.butent.bee.server.sql.HasFrom;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.shared.modules.administration.AdministrationConstants.RightsState;

import java.util.Collection;
import java.util.Map;

/**
 * Contains required methods for classes that can have different states when they are used.
 */

public interface HasStates {

  boolean activateState(RightsState state, Collection<Long> bits);

  IsCondition checkState(String stateAlias, RightsState state, boolean checkedByDefault,
      long... bits);

  SqlCreate createStateTable(SqlCreate query, RightsState state);

  String getStateTable(RightsState state);

  void initState(RightsState state, Collection<String> flds);

  SqlInsert insertState(long id, RightsState state, Map<Long, Boolean> bits);

  String joinState(HasFrom<?> query, String tblAlias, RightsState state);

  SqlUpdate updateState(long id, RightsState state, Map<Long, Boolean> bits);

  SqlSelect verifyState(SqlSelect query, String tblAlias, RightsState state,
      boolean checkedByDefault, long... bits);
}
