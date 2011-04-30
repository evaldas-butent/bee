package com.butent.bee.server.sql;

import com.butent.bee.server.sql.BeeConstants.Keyword;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Enables to form any SQL statement with specified parameters, mainly used for rarer SQL commands
 * like dropping indexes.
 */

class SqlCommand extends SqlQuery<SqlCommand> {

  private final Keyword command;
  private final Map<String, Object> parameters;

  public SqlCommand(Keyword command, Map<String, Object> parameters) {
    Assert.notEmpty(command);

    this.command = command;
    this.parameters = parameters;
  }

  public Keyword getCommand() {
    return command;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    if (!BeeUtils.isEmpty(parameters)) {
      for (Object prm : parameters.values()) {
        if (prm instanceof HasSource) {
          sources = SqlUtils.addCollection(sources, ((HasSource) prm).getSources());
        }
      }
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    return null;
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
