package com.butent.bee.shared.sql;

import com.butent.bee.shared.BeeSerializable;

public interface IsExpression extends IsSql, BeeSerializable {

  Object getValue();
}
