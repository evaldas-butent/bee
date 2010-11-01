package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.WRITE)
public class IdGeneratorBean {

  private static final int NEXT_ID = 0;
  private static final int LAST_ID = 1;

  @EJB
  QueryServiceBean qs;

  private int idChunk = 50;
  private Map<String, long[]> idCache = new HashMap<String, long[]>();

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
    String tbl = "fw_tables";
    String fld = "last_id";

    IsCondition wh = SqlUtils.equal(tbl, "table_name", source);

    SqlUpdate su = new SqlUpdate(tbl);
    su.addField(
        fld,
        SqlUtils.expression(SqlUtils.field(fld), "+",
            SqlUtils.constant(idChunk))).setWhere(wh);

    int cnt = qs.processUpdate(su);

    if (BeeUtils.isEmpty(cnt)) {
      return null;
    }

    SqlSelect ss = new SqlSelect();
    ss.addFields(tbl, fld).addFrom(tbl).setWhere(wh);

    List<Object[]> result = qs.getData(ss);
    long lastId = (Long) result.get(0)[0];

    long[] ids = new long[2];
    ids[NEXT_ID] = lastId - idChunk;
    ids[LAST_ID] = lastId;

    idCache.put(source, ids);

    return ids;
  }
}
