package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;

public class BeeExtTable extends BeeTable {

  private static final String EXT_TABLE_SUFFIX = "_EXT";

  private BeeTable extTable;

  BeeExtTable(String name, String idName, String lockName) {
    super(name, idName, lockName);
  }

  @Override
  public BeeField addExtField(String name, DataTypes type, int precision, int scale,
      boolean notNull, boolean unique, String relation, boolean cascade) {

    Assert.state(!hasField(name), "Dublicate field name: " + getName() + " " + name);
    return createExtTable()
      .addField(name, type, precision, scale, notNull, unique, relation, cascade)
      .setExtended();
  }

  @Override
  public BeeKey addExtKey(boolean unique, String... keyFields) {
    return createExtTable()
      .addKey(unique, keyFields)
      .setExtended();
  }

  @Override
  public String appendExtJoin(HasFrom<?> query, String tblAlias) {
    String extAlias = null;

    if (!BeeUtils.isEmpty(extTable)) {
      String tblName = getName();
      String idName = getIdName();
      String extName = extTable.getName();
      String extIdName = extTable.getIdName();

      if (BeeUtils.isEmpty(tblAlias) || BeeUtils.same(tblAlias, tblName)) {
        extAlias = extName;
        query.addFromLeft(extName, SqlUtils.join(tblName, idName, extName, extIdName));
      } else {
        extAlias = SqlUtils.uniqueName();
        query.addFromLeft(extName, extAlias, SqlUtils.join(tblAlias, idName, extAlias, extIdName));
      }
    }
    return extAlias;
  }

  public String appendExtLockName(SqlSelect ss, String tblAlias) {
    String fldName = null;

    if (!BeeUtils.isEmpty(extTable)) {
      fldName = extTable.getLockName();
      ss.addFields(tblAlias, fldName);
    }
    return fldName;
  }

  @Override
  public BeeField getExtField(String fldName) {
    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getField(fldName);
    }
    return null;
  }

  @Override
  public Collection<BeeField> getExtFields() {
    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getFields();
    }
    return new ArrayList<BeeField>();
  }

  @Override
  public Collection<BeeForeignKey> getExtForeignKeys() {
    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getForeignKeys();
    }
    return new ArrayList<BeeForeignKey>();
  }

  @Override
  public Collection<BeeKey> getExtKeys() {
    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getKeys();
    }
    return new ArrayList<BeeKey>();
  }

  @Override
  public boolean removeExtField(BeeField field) {
    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.dropField(field);
    }
    return false;
  }

  @Override
  public boolean removeExtKey(BeeKey key) {
    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.dropKey(key);
    }
    return false;
  }

  private BeeTable createExtTable() {
    if (BeeUtils.isEmpty(extTable)) {
      String tblName = getName();

      extTable = new BeeExtTable(
          tblName + EXT_TABLE_SUFFIX,
          tblName + getIdName(),
          tblName + getLockName());
      extTable.addForeignKey(extTable.getIdName(), tblName, Keywords.CASCADE);
    }
    return extTable;
  }
}
