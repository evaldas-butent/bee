package com.butent.bee.server.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.sql.BeeConstants.Keywords;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.SqlCreate;
import com.butent.bee.shared.sql.SqlInsert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUpdate;
import com.butent.bee.shared.sql.SqlUtils;
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

@Singleton
// TODO: waiting for JBoss bugfix http://community.jboss.org/thread/161844
// @TransactionAttribute(TransactionAttributeType.MANDATORY)
public class IdGeneratorBean {

  private static Logger logger = Logger.getLogger(IdGeneratorBean.class.getName());

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
  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  public void destroy() {
    if (qs.isDbTable(sys.getDbName(), sys.getDbSchema(), ID_TABLE)) {
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
    LogUtils.infoNow(logger, getClass().getSimpleName(), "destroy end");
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

    if (!qs.isDbTable(sys.getDbName(), sys.getDbSchema(), ID_TABLE)) {
      SqlCreate sc = new SqlCreate(ID_TABLE, false);
      sc.addString(ID_KEY, 30, Keywords.NOT_NULL);
      sc.addLong(ID_LAST, Keywords.NOT_NULL);
      qs.updateData(sc);
      qs.updateData(SqlUtils.createUniqueIndex(ID_TABLE, ID_KEY));
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
