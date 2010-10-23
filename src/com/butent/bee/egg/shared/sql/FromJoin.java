package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

abstract class FromJoin extends FromSingle {

  private Condition on;

  public FromJoin(SqlSelect source, String alias, Condition on) {
    super(source, alias);

    Assert.notNull(on);
    this.on = on;
  }

  public FromJoin(String source, Condition on) {
    this(source, null, on);
  }

  public FromJoin(String source, String alias, Condition on) {
    super(source, alias);

    Assert.notNull(on);
    this.on = on;
  }

  @Override
  public String getFrom(SqlBuilder builder, boolean queryMode) {
    StringBuilder from = new StringBuilder(super.getFrom(builder,
        queryMode));

    from.append(" ON ").append(on.getCondition(builder, queryMode));

    return from.toString();
  }

  @Override
  public List<Object> getParameters() {
    List<Object> paramList = super.getParameters();
    List<Object> qp = on.getParameters();

    if (!BeeUtils.isEmpty(qp)) {
      if (BeeUtils.isEmpty(paramList)) {
        paramList = qp;
      } else {
        paramList.addAll(qp);
      }
    }
    return paramList;
  }
}
