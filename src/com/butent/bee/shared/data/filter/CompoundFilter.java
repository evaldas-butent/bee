package com.butent.bee.shared.data.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.sql.CompoundCondition;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CompoundFilter extends Filter {

  private enum SerializationMembers {
    JOINTYPE, SUBFILTERS
  }

  private static Logger logger = Logger.getLogger(CompoundFilter.class.getName());

  public static Filter and(Filter... filters) {
    return new CompoundFilter(CompoundType.AND, filters);
  }

  public static Filter or(Filter... filters) {
    return new CompoundFilter(CompoundType.OR, filters);
  }

  private CompoundType joinType;
  private final List<Filter> subFilters = Lists.newArrayList();

  protected CompoundFilter() {
    super();
  }

  private CompoundFilter(CompoundType joinType, Filter... filters) {
    this.joinType = joinType;
    add(filters);
  }

  public Filter add(Filter... filters) {
    if (!BeeUtils.isEmpty(filters)) {
      for (Filter filter : filters) {
        if (!BeeUtils.isEmpty(filter)) {
          subFilters.add(filter);
        }
      }
    }
    return this;
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);

    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String xpr = arr[i];

      switch (member) {
        case JOINTYPE:
          joinType = CompoundType.valueOf(arr[0]);
          break;
        case SUBFILTERS:
          for (String flt : Codec.beeDeserialize(xpr)) {
            add(Filter.restore(flt));
          }
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CompoundFilter other = (CompoundFilter) obj;

    if (!joinType.equals(other.joinType)) {
      return false;
    }
    if (!subFilters.equals(other.subFilters)) {
      return false;
    }
    return true;
  }

  @Override
  public IsCondition getCondition(Map<String, String[]> columns) {
    CompoundCondition condition = null;

    if (!BeeUtils.isEmpty(subFilters)) {
      switch (joinType) {
        case AND:
          condition = SqlUtils.and();
          break;
        case OR:
          condition = SqlUtils.or();
          break;
        default:
          Assert.unsupported();
          break;
      }
      for (Filter subFilter : subFilters) {
        condition.add(subFilter.getCondition(columns));
      }
    }
    return condition;
  }

  public CompoundType getJoinType() {
    return joinType;
  }

  public List<Filter> getSubFilters() {
    return ImmutableList.copyOf(subFilters);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + joinType.hashCode();
    result = prime * result + subFilters.hashCode();
    return result;
  }

  @Override
  public boolean involvesColumn(String colName) {
    if (!BeeUtils.isEmpty(subFilters)) {
      for (Filter subFilter : subFilters) {
        if (subFilter.involvesColumn(colName)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    if (!BeeUtils.isEmpty(subFilters)) {
      for (Filter subFilter : subFilters) {
        boolean result = subFilter.isMatch(columns, row);

        if ((joinType == CompoundType.AND && !result) ||
            (joinType == CompoundType.OR && result)) {
          return result;
        }
      }
      return (joinType == CompoundType.AND);
    }
    return true;
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case JOINTYPE:
          arr[i++] = joinType;
          break;
        case SUBFILTERS:
          arr[i++] = subFilters;
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), arr);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (Filter wh : subFilters) {
      String expr = wh.transform();

      if (!BeeUtils.isEmpty(expr) && sb.length() > 0) {
        sb.append(joinType.toTextString());
      }
      sb.append(expr);
    }
    String flt = sb.toString();

    if (CompoundType.OR == joinType && !BeeUtils.isEmpty(flt)) {
      flt = BeeUtils.parenthesize(flt);
    }
    return flt;
  }
}
