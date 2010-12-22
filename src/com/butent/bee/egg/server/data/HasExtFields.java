package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.data.BeeTable.BeeField;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeKey;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlCreate;

import java.util.Collection;

public interface HasExtFields {

  BeeField addExtField(String name, DataTypes type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade);

  BeeKey addExtKey(boolean unique, String... keyFields);

  SqlCreate extCreateTable(SqlCreate query, BeeField field);

  IsQuery extInsertField(IsQuery query, long rootId, BeeField field, Object newValue);

  String extJoinField(HasFrom<?> query, String tblAlias, BeeField field);

  IsQuery extUpdateField(IsQuery query, long rootId, BeeField field, Object newValue);

  BeeField getExtField(String fldName);

  Collection<BeeField> getExtFields();

  Collection<BeeForeignKey> getExtForeignKeys();

  Collection<BeeKey> getExtKeys();

  boolean removeExtField(BeeField field);

  boolean removeExtKey(BeeKey key);
}
