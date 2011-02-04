package com.butent.bee.shared.sql;

import java.util.Collection;

public interface IsFrom extends IsSql {

  String getAlias();

  String getJoinMode();

  Object getSource();

  Collection<String> getSources();
}
