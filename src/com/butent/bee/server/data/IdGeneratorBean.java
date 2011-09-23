package com.butent.bee.server.data;

import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * Generates new ID values for database fields, ensuring integrity and uniqueness requirements of a
 * database.
 */

@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class IdGeneratorBean {

  private static Logger logger = Logger.getLogger(IdGeneratorBean.class.getName());

  private static final String ID_TABLE = "Sequences";
  private static final String ID_KEY = "Name";
  private static final String ID_LAST = "LastValue";

  private static final int NEXT_ID_INDEX = 0;
  private static final int LAST_ID_INDEX = 1;

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  private int idChunk = 50;
  private Map<String, long[]> idCache = new HashMap<String, long[]>();

  @PreDestroy
  public void destroy() {
    for (Entry<String, long[]> entry : idCache.entrySet()) {
      String source = entry.getKey();
      IsCondition wh = SqlUtils.equal(ID_TABLE, ID_KEY, source);

      long lastId = BeeUtils.unbox(
          qs.getLong(new SqlSelect().addFields(ID_TABLE, ID_LAST).addFrom(ID_TABLE).setWhere(wh)));

      if (entry.getValue()[LAST_ID_INDEX] == lastId) {
        String idFld = sys.getIdName(source);

        lastId = BeeUtils.unbox(qs.getLong(new SqlSelect().addMax(source, idFld).addFrom(source)));

        qs.updateData(new SqlUpdate(ID_TABLE)
            .addConstant(sys.getVersionName(ID_TABLE), System.currentTimeMillis())
            .addConstant(ID_LAST, lastId)
            .setWhere(wh));
      }
    }
    idCache.clear();
    LogUtils.infoNow(logger, getClass().getSimpleName(), "destroy end");
  }

  public long getId(String source) {
    Assert.state(sys.isTable(source));

    long[] ids = idCache.get(source);

    if (BeeUtils.isEmpty(ids) || ids[NEXT_ID_INDEX] >= ids[LAST_ID_INDEX]) {
      ids = prepareId(source);
    }
    return ++ids[NEXT_ID_INDEX];
  }

  private long[] prepareId(String source) {
    IsCondition wh = SqlUtils.equal(ID_TABLE, ID_KEY, source);

    SqlUpdate su = new SqlUpdate(ID_TABLE)
        .addConstant(sys.getVersionName(ID_TABLE), System.currentTimeMillis())
        .addExpression(ID_LAST, SqlUtils.plus(SqlUtils.name(ID_LAST), SqlUtils.constant(idChunk)))
        .setWhere(wh);

    long lastId;

    if (BeeUtils.isEmpty(qs.updateData(su))) {
      String idFld = sys.getIdName(source);

      lastId = BeeUtils.unbox(qs.getLong(new SqlSelect().addMax(source, idFld).addFrom(source)));

      SqlInsert si = new SqlInsert(ID_TABLE);

      if (BeeUtils.same(source, ID_TABLE)) {
        si.addConstant(idFld, ++lastId);
      } else {
        si.addConstant(sys.getIdName(ID_TABLE), getId(ID_TABLE));
      }
      si.addConstant(ID_KEY, source).addConstant(ID_LAST, lastId += idChunk);
      qs.insertData(si);
    } else {
      lastId = BeeUtils.unbox(
          qs.getLong(new SqlSelect().addFields(ID_TABLE, ID_LAST).addFrom(ID_TABLE).setWhere(wh)));
    }
    long[] ids = new long[2];
    ids[NEXT_ID_INDEX] = lastId - idChunk;
    ids[LAST_ID_INDEX] = lastId;

    idCache.put(source, ids);
    return ids;
  }
}
