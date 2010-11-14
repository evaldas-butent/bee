package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class SqlCommand extends SqlQuery<SqlCommand> {

  private final Keywords command;
  private final Object[] parameters;

  public SqlCommand(Keywords command, Object... parameters) {
    Assert.notEmpty(command);

    this.command = command;
    this.parameters = parameters;
  }

  public Keywords getCommand() {
    return command;
  }

  public Object[] getParameters() {
    return parameters;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    for (Object param : parameters) {
      if (param instanceof IsSql) {
        SqlUtils.addParams(paramList, ((IsSql) param).getSqlParams());
      }
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getCommand(this, paramMode);
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(command);
  }

  @Override
  protected SqlCommand getReference() {
    return this;
  }
}
