package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public abstract class FromJoin extends FromSingle {

  private Condition on;

  public FromJoin(QueryBuilder source, String alias, Condition on) {
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
  public String getCondition(boolean queryMode) {
    StringBuilder from = new StringBuilder(super.getCondition(queryMode));

    from.append(" ON ").append(on.getCondition(queryMode));

    return from.toString();
  }

  @Override
  public List<Object> getQueryParameters() {
    List<Object> paramList = super.getQueryParameters();
    List<Object> qp = on.getQueryParameters();

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
