package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class FromJoin extends FromSource {

  public enum JoinMode {
    LIST(", "),
    INNER(" INNER JOIN "),
    LEFT(" LEFT JOIN "),
    RIGHT(" RIGHT JOIN "),
    FULL(" FULL JOIN ");

    private String sqlString;

    JoinMode(String sqlString) {
      this.sqlString = sqlString;
    }

    public String toSqlString() {
      return sqlString;
    }
  }

  public static FromJoin fromFull(SqlSelect source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.FULL, source, alias, on);
  }

  public static FromJoin fromFull(String source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.FULL, source, alias, on);
  }

  public static FromJoin fromInner(SqlSelect source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.INNER, source, alias, on);
  }

  public static FromJoin fromInner(String source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.INNER, source, alias, on);
  }

  public static FromJoin fromLeft(SqlSelect source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.LEFT, source, alias, on);
  }

  public static FromJoin fromLeft(String source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.LEFT, source, alias, on);
  }

  public static FromJoin fromList(SqlSelect source, String alias) {
    return new FromJoin(JoinMode.LIST, source, alias, null);
  }

  public static FromJoin fromList(String source, String alias) {
    return new FromJoin(JoinMode.LIST, source, alias, null);
  }

  public static FromJoin fromRight(SqlSelect source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.RIGHT, source, alias, on);
  }

  public static FromJoin fromRight(String source, String alias, IsCondition on) {
    Assert.notNull(on);
    return new FromJoin(JoinMode.RIGHT, source, alias, on);
  }

  public static FromSource fromSingle(SqlSelect source, String alias) {
    return new FromSource(source, alias);
  }

  public static FromSource fromSingle(String source, String alias) {
    return new FromSource(source, alias);
  }

  private final JoinMode join;
  private final IsCondition on;

  private FromJoin(JoinMode join, SqlSelect source, String alias, IsCondition on) {
    super(source, alias);
    this.join = join;
    this.on = on;
  }

  private FromJoin(JoinMode join, String source, String alias, IsCondition on) {
    super(source, alias);
    this.join = join;
    this.on = on;
  }

  public JoinMode getJoinMode() {
    return join;
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = super.getSources();

    if (!BeeUtils.isEmpty(on)) {
      sources = SqlUtils.addCollection(sources, on.getSources());
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> params = super.getSqlParams();

    if (!BeeUtils.isEmpty(on)) {
      params = (List<Object>) SqlUtils.addCollection(params, on.getSqlParams());
    }
    return params;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean queryMode) {
    StringBuilder from = new StringBuilder(join.toSqlString())
        .append(super.getSqlString(builder, queryMode));

    if (!BeeUtils.isEmpty(on)) {
      from.append(" ON ").append(on.getSqlString(builder, queryMode));
    }
    return from.toString();
  }
}
