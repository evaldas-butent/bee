package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RowFilter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

/**
 * Is an abstract class for all specific filter applying classes, determines which type of filter to
 * apply.
 */

public abstract class Filter implements BeeSerializable, Transformable, RowFilter {

  public static Filter and(Filter f1, Filter f2) {
    if (f1 == null) {
      return f2;
    } else if (f2 == null) {
      return f1;
    } else {
      return new CompoundFilter(CompoundType.AND, f1, f2);
    }
  }

  public static CompoundFilter and() {
    return new CompoundFilter(CompoundType.AND);
  }

  public static Filter and(Collection<Filter> filters) {
    if (filters == null || filters.isEmpty()) {
      return null;
    } else {
      Filter[] arr = filters.toArray(new Filter[0]);
      int size = arr.length;

      switch (size) {
        case 1:
          return arr[0];
        case 2:
          return and(arr[0], arr[1]);
        default:
          return new CompoundFilter(CompoundType.AND, arr);
      }
    }
  }

  public static Filter anyContains(Collection<String> columns, String value) {
    Assert.notEmpty(columns);
    Assert.notEmpty(value);

    CompoundFilter filter = Filter.or();
    for (String column : columns) {
      filter.add(ComparisonFilter.contains(column, value));
    }
    return filter;
  }

  public static Filter idIn(Collection<Long> values) {
    Assert.notNull(values);
    if (values.isEmpty()) {
      return null;
    }

    CompoundFilter filter = or();
    for (Long value : values) {
      filter.add(ComparisonFilter.compareId(value));
    }
    return filter;
  }

  public static Filter in(String column, Collection<Long> values) {
    Assert.notEmpty(column);
    Assert.notNull(values);

    if (values.isEmpty()) {
      return null;
    }

    CompoundFilter filter = or();
    for (Long value : values) {
      filter.add(ComparisonFilter.isEqual(column, new LongValue(value)));
    }
    return filter;
  }

  public static Filter isEmpty(String column) {
    Assert.notEmpty(column);
    return new ColumnIsEmptyFilter(column);
  }

  public static Filter isFalse() {
    return new IsFalseFilter();
  }

  public static Filter isNot(Filter filter) {
    Assert.notNull(filter);
    return new CompoundFilter(CompoundType.NOT, filter);
  }

  public static Filter isTrue() {
    return new IsTrueFilter();
  }

  public static Filter notEmpty(String column) {
    Assert.notEmpty(column);
    return new ColumnNotEmptyFilter(column);
  }

  public static Filter or(Filter f1, Filter f2) {
    if (f1 == null) {
      return f2;
    } else if (f2 == null) {
      return f1;
    } else {
      return new CompoundFilter(CompoundType.OR, f1, f2);
    }
  }

  public static Filter or(Collection<Filter> filters) {
    if (filters == null || filters.isEmpty()) {
      return null;
    } else {
      Filter[] arr = filters.toArray(new Filter[0]);
      int size = arr.length;

      switch (size) {
        case 1:
          return arr[0];
        case 2:
          return or(arr[0], arr[1]);
        default:
          return new CompoundFilter(CompoundType.OR, arr);
      }
    }
  }

  public static CompoundFilter or() {
    return new CompoundFilter(CompoundType.OR);
  }

  public static Filter restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }

    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);
    String clazz = arr[0];
    String data = arr[1];
    Filter flt = null;

    if (data != null) {
      flt = Filter.getFilter(clazz);
      flt.deserialize(data);
    }
    return flt;
  }

  private static Filter getFilter(String clazz) {
    Filter flt = null;

    if (NameUtils.getClassName(ColumnValueFilter.class).equals(clazz)) {
      flt = new ColumnValueFilter();

    } else if (NameUtils.getClassName(ColumnColumnFilter.class).equals(clazz)) {
      flt = new ColumnColumnFilter();

    } else if (NameUtils.getClassName(ColumnIsEmptyFilter.class).equals(clazz)) {
      flt = new ColumnIsEmptyFilter();

    } else if (NameUtils.getClassName(ColumnNotEmptyFilter.class).equals(clazz)) {
      flt = new ColumnNotEmptyFilter();

    } else if (NameUtils.getClassName(CompoundFilter.class).equals(clazz)) {
      flt = new CompoundFilter();

    } else if (NameUtils.getClassName(IdFilter.class).equals(clazz)) {
      flt = new IdFilter();

    } else if (NameUtils.getClassName(VersionFilter.class).equals(clazz)) {
      flt = new VersionFilter();

    } else if (NameUtils.getClassName(IsFalseFilter.class).equals(clazz)) {
      flt = new IsFalseFilter();

    } else if (NameUtils.getClassName(IsTrueFilter.class).equals(clazz)) {
      flt = new IsTrueFilter();

    } else {
      Assert.unsupported("Unsupported class name: " + clazz);
    }
    return flt;
  }

  private boolean safe = true;

  protected Filter() {
    this.safe = false;
  }

  public abstract boolean involvesColumn(String colName);

  @Override
  public abstract boolean isMatch(List<? extends IsColumn> columns, IsRow row);

  @Override
  public String transform() {
    return toString();
  }

  protected int getColumnIndex(String colName, List<? extends IsColumn> columns) {
    return DataUtils.getColumnIndex(colName, columns);
  }

  protected String serialize(Object obj) {
    return Codec.beeSerialize(new Object[] {NameUtils.getClassName(this.getClass()), obj});
  }

  protected void setSafe() {
    Assert.isFalse(safe);
    this.safe = true;
  }
}
