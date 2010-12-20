package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsExpression;
import com.butent.bee.egg.shared.sql.IsFrom;
import com.butent.bee.egg.shared.sql.SqlBuilderFactory;
import com.butent.bee.egg.shared.sql.SqlCreate;
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
  public SqlCreate extCreateTable(SqlCreate query, BeeField field) {
    SqlCreate sc = null;

    if (!BeeUtils.isEmpty(extTable)) {
      if (BeeUtils.isEmpty(query)) {
        sc = new SqlCreate(extTable.getName(), false);
      } else {
        sc = query;
      }
      sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
          field.isNotNull() ? Keywords.NOT_NULL : null);

      if (!sc.hasField(extTable.getLockName())) {
        sc.addLong(extTable.getLockName(), Keywords.NOT_NULL);
      }
      if (!sc.hasField(extTable.getIdName())) {
        sc.addLong(extTable.getIdName(), Keywords.NOT_NULL);
      }
    }
    return sc;
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

  public String extLockName(SqlSelect ss, String tblAlias) {
    String fldName = null;

    if (!BeeUtils.isEmpty(extTable)) {
      String alias = BeeUtils.ifString(tblAlias, getName());
      fldName = extTable.getLockName();
      String xpr = SqlUtils.field(alias, fldName).getValue();
      boolean exists = false;

      for (IsExpression[] fldExpr : ss.getFields()) {
        if (BeeUtils.same(fldExpr[0].getValue(), xpr)) {
          if (!BeeUtils.isEmpty(fldExpr[1])) {
            fldName = fldExpr[1].getValue();
          }
          exists = true;
          break;
        }
      }
      if (!exists) {
        ss.addFields(alias, fldName);
      }
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
