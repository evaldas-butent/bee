package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Singleton
@Lock(LockType.WRITE)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class IdGeneratorBean {

  private static final String ID_TABLE = "fw_tables";
  private static final String ID_FIELD = "last_id";
  private static final int NEXT_ID = 0;
  private static final int LAST_ID = 1;

  @EJB
  QueryServiceBean qs;

  private int idChunk = 50;
  private Map<String, long[]> idCache = new HashMap<String, long[]>();

  @PreDestroy
  public void destroy() {
    int oldLevel = qs.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);

    for (Entry<String, long[]> entry : idCache.entrySet()) {
      String source = entry.getKey();
      IsCondition wh = SqlUtils.equal(ID_TABLE, "table_name", source);

      SqlSelect ss = new SqlSelect();
      ss.addFields(ID_TABLE, ID_FIELD).addFrom(ID_TABLE).setWhere(wh);

      long lastId = qs.getSingleRow(ss).getLong(ID_FIELD);

      if (entry.getValue()[LAST_ID] == lastId) {
        ss = new SqlSelect();
        ss.addMax(source, "id").addFrom(source);

        lastId = qs.getSingleRow(ss).getLong("id");

        SqlUpdate su = new SqlUpdate(ID_TABLE);
        su.addField(ID_FIELD, lastId).setWhere(wh);

        qs.updateData(su);
      }
    }
    qs.setIsolationLevel(oldLevel);
    idCache.clear();
  }

  public long getId(String source) {
    long newId = 0;

    if (!BeeUtils.isEmpty(source)) {
      long[] ids = idCache.get(source);

      if (BeeUtils.isEmpty(ids) || ids[NEXT_ID] == ids[LAST_ID]) {
        ids = prepareId(source);
      }
      if (!BeeUtils.isEmpty(ids)) {
        newId = ++ids[NEXT_ID];
      }
    }
    return newId;
  }

  private long[] prepareId(String source) {
    int oldLevel = qs.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);

    long[] ids = null;
    IsCondition wh = SqlUtils.equal(ID_TABLE, "table_name", source);

    SqlUpdate su = new SqlUpdate(ID_TABLE);
    su.addField(ID_FIELD,
        SqlUtils.expression(SqlUtils.field(ID_FIELD), "+",
            SqlUtils.constant(idChunk))).setWhere(wh);

    int cnt = qs.updateData(su);

    if (!BeeUtils.isEmpty(cnt)) {
      SqlSelect ss = new SqlSelect();
      ss.addFields(ID_TABLE, ID_FIELD).addFrom(ID_TABLE).setWhere(wh);

      long lastId = qs.getSingleRow(ss).getLong(ID_FIELD);

      ids = new long[2];
      ids[NEXT_ID] = lastId - idChunk;
      ids[LAST_ID] = lastId;

      idCache.put(source, ids);
    }
    qs.setIsolationLevel(oldLevel);

    return ids;
  }
}
