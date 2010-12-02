package com.butent.bee.egg.server.data;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Singleton
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class IdGeneratorBean {

  private static final String ID_TABLE = "bee_Sequence";
  private static final String ID_KEY = "SequenceName";
  private static final String ID_LAST = "SequenceValue";

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
    if (qs.isDbTable(ID_TABLE)) {
      for (Entry<String, long[]> entry : idCache.entrySet()) {
        String source = entry.getKey();
        IsCondition wh = SqlUtils.equal(ID_TABLE, ID_KEY, source);

        SqlSelect ss = new SqlSelect();
        ss.addFields(ID_TABLE, ID_LAST).addFrom(ID_TABLE).setWhere(wh);
        BeeRowSet rs = qs.getData(ss);

        if (rs.getRowCount() == 1) {
          long lastId = rs.getRow(0).getLong(ID_LAST);

          if (entry.getValue()[LAST_ID_INDEX] == lastId) {
            String idFld = sys.getIdName(source);

            ss = new SqlSelect();
            ss.addMax(source, idFld).addFrom(source);

            lastId = qs.getSingleRow(ss).getLong(idFld);

            SqlUpdate su = new SqlUpdate(ID_TABLE);
            su.addConstant(ID_LAST, lastId).setWhere(wh);
            qs.updateData(su);
          }
        }
      }
    }
    idCache.clear();
  }

  public long getId(String source) {
    Assert.state(sys.isTable(source));

    long[] ids = idCache.get(source);

    if (BeeUtils.isEmpty(ids) || ids[NEXT_ID_INDEX] == ids[LAST_ID_INDEX]) {
      ids = prepareId(source);
    }
    return ++ids[NEXT_ID_INDEX];
  }

  private long[] prepareId(String source) {
    int cnt = 0;
    IsCondition wh = SqlUtils.equal(ID_TABLE, ID_KEY, source);

    if (!qs.isDbTable(ID_TABLE)) {
      SqlCreate sc = new SqlCreate(ID_TABLE, false);
      sc.addString(ID_KEY, 30, Keywords.NOT_NULL);
      sc.addLong(ID_LAST, Keywords.NOT_NULL);
      qs.updateData(sc);

      IsQuery index = SqlUtils.createPrimaryKey(ID_TABLE,
          BeeTable.PRIMARY_KEY_PREFIX + ID_TABLE, ID_KEY);
      qs.updateData(index);
    } else {
      SqlUpdate su = new SqlUpdate(ID_TABLE);
      su.addExpression(ID_LAST,
          SqlUtils.expression(SqlUtils.name(ID_LAST), "+", SqlUtils.constant(idChunk)))
        .setWhere(wh);
      cnt = qs.updateData(su);
    }

    if (BeeUtils.isEmpty(cnt)) {
      String idFld = sys.getIdName(source);

      SqlSelect ss = new SqlSelect();
      ss.addMax(source, sys.getIdName(source)).addFrom(source);

      long lastId = qs.getSingleRow(ss).getLong(idFld);

      SqlInsert si = new SqlInsert(ID_TABLE);
      si.addConstant(ID_KEY, source).addConstant(ID_LAST, lastId + idChunk);
      cnt = qs.updateData(si);
    }
    SqlSelect ss = new SqlSelect();
    ss.addFields(ID_TABLE, ID_LAST).addFrom(ID_TABLE).setWhere(wh);

    long lastId = qs.getSingleRow(ss).getLong(ID_LAST);

    long[] ids = new long[2];
    ids[NEXT_ID_INDEX] = lastId - idChunk;
    ids[LAST_ID_INDEX] = lastId;

    idCache.put(source, ids);
    return ids;
  }
}
