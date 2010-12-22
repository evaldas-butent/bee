package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsFrom;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlBuilderFactory;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlUpdate;
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
  public SqlCreate extCreateTable(SqlCreate query, BeeField field) {
    SqlCreate sc = null;

    if (!BeeUtils.isEmpty(extTable)) {
      if (BeeUtils.isEmpty(query)) {
        sc = new SqlCreate(extTable.getName(), false)
          .addLong(extTable.getIdName(), Keywords.NOT_NULL)
          .addLong(extTable.getLockName(), Keywords.NOT_NULL);
      } else {
        sc = query;
      }
      sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
          field.isNotNull() ? Keywords.NOT_NULL : null);
    }
    return sc;
  }

  @Override
  public IsQuery extInsertField(IsQuery query, long rootId, BeeField field, Object newValue) {
    SqlInsert si = null;

    if (!BeeUtils.isEmpty(extTable)) {
      if (BeeUtils.isEmpty(query)) {
        si = new SqlInsert(extTable.getName())
          .addConstant(extTable.getLockName(), System.currentTimeMillis())
          .addConstant(extTable.getIdName(), rootId);
      } else {
        si = (SqlInsert) query;
      }
      si.addConstant(field.getName(), newValue);
    }
    return si;
  }

  @Override
  public String extJoinField(HasFrom<?> query, String tblAlias, BeeField field) {
    String extAlias = null;

    if (!BeeUtils.isEmpty(extTable)) {
      String tblName = getName();
      String idName = getIdName();
      String alias = BeeUtils.ifString(tblAlias, tblName);
      String extName = extTable.getName();
      String extIdName = extTable.getIdName();

      for (IsFrom from : query.getFrom()) {
        Object src = from.getSource();

        if (src instanceof String && BeeUtils.same((String) src, extName)) {
          if (from.getSqlString(SqlBuilderFactory.getBuilder(), false).contains(alias)) {
            extAlias = BeeUtils.ifString(from.getAlias(), extName);
            break;
          }
        }
      }
      if (BeeUtils.isEmpty(extAlias)) {
        if (BeeUtils.same(alias, tblName)) {
          extAlias = extName;
        } else {
          extAlias = SqlUtils.uniqueName();
        }
        query.addFromLeft(extName, extAlias, SqlUtils.join(alias, idName, extAlias, extIdName));
      }
    }
    return extAlias;
  }

  @Override
  public IsQuery extUpdateField(IsQuery query, long rootId, BeeField field, Object newValue) {
    SqlUpdate su = null;

    if (!BeeUtils.isEmpty(extTable)) {
      if (BeeUtils.isEmpty(query)) {
        su = new SqlUpdate(extTable.getName())
          .addConstant(extTable.getLockName(), System.currentTimeMillis())
          .setWhere(SqlUtils.equal(extTable.getName(), extTable.getIdName(), rootId));
      } else {
        su = (SqlUpdate) query;
      }
      su.addConstant(field.getName(), newValue);
    }
    return su;
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
