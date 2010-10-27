package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

abstract class FromJoin extends FromSingle {

  private IsCondition on;

  public FromJoin(String source, IsCondition on) {
    this(source, null, on);
  }

  public FromJoin(String source, String alias, IsCondition on) {
    super(source, alias);

    Assert.notNull(on);
    this.on = on;
  }

  public FromJoin(SqlSelect source, String alias, IsCondition on) {
    super(source, alias);

    Assert.notNull(on);
    this.on = on;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = super.getSqlParams();
    List<Object> qp = on.getSqlParams();

    if (!BeeUtils.isEmpty(qp)) {
      if (BeeUtils.isEmpty(paramList)) {
        paramList = qp;
      } else {
        paramList.addAll(qp);
      }
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean queryMode) {
    StringBuilder from = new StringBuilder(super.getSqlString(builder,
        queryMode));

    from.append(" ON ").append(on.getSqlString(builder, queryMode));

    return from.toString();
  }
}
