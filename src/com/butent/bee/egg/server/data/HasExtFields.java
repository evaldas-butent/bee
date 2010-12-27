package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.data.BeeTable.BeeField;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlCreate;

public interface HasExtFields {

  SqlCreate createExtTable(SqlCreate query, BeeField field);

  String getExtName();

  IsQuery insertExtField(IsQuery query, long rootId, BeeField field, Object newValue);

  String joinExtField(HasFrom<?> query, String tblAlias, BeeField field);

  IsQuery updateExtField(IsQuery query, long rootId, BeeField field, Object newValue);
}
