package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.data.BeeTable.BeeField;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlUpdate;

public interface HasExtFields {

  SqlCreate createExtTable(SqlCreate query, BeeField field);

  String getExtTable(String fldName);

  SqlInsert insertExtField(SqlInsert query, long rootId, BeeField field, Object newValue);

  String joinExtField(HasFrom<?> query, String tblAlias, BeeField field);

  SqlUpdate updateExtField(SqlUpdate query, long rootId, BeeField field, Object newValue);
}
