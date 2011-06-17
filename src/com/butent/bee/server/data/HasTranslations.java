package com.butent.bee.server.data;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.sql.HasFrom;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlUpdate;

/**
 * Contains requirements for classes, which can have translations implemented on them.
 */

public interface HasTranslations {

  SqlCreate createTranslationTable(SqlCreate query, BeeField field);

  String getTranslationField(BeeField field, String locale);

  String getTranslationTable(BeeField field);

  SqlInsert insertTranslationField(SqlInsert query, long rootId, BeeField field, String locale,
      Object newValue);

  String joinTranslationField(HasFrom<?> query, String tblAlias, BeeField field, String locale);

  SqlUpdate updateTranslationField(SqlUpdate query, long rootId, BeeField field, String locale,
      Object newValue);
}
