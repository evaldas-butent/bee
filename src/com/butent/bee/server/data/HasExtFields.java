package com.butent.bee.server.data;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.shared.sql.HasFrom;
import com.butent.bee.shared.sql.SqlCreate;
import com.butent.bee.shared.sql.SqlInsert;
import com.butent.bee.shared.sql.SqlUpdate;

public interface HasExtFields {

  SqlCreate createExtTable(SqlCreate query, BeeField field);

  String getExtTable(String fldName);

  SqlInsert insertExtField(SqlInsert query, long rootId, BeeField field, Object newValue);

  String joinExtField(HasFrom<?> query, String tblAlias, BeeField field);

  SqlUpdate updateExtField(SqlUpdate query, long rootId, BeeField field, Object newValue);
}
