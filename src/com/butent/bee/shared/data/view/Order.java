package com.butent.bee.shared.data.view;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class Order implements BeeSerializable, Transformable {

  public class Column implements Transformable {
    private final String label;
    private final boolean ascending;

    private Column(String label) {
      this(label, true);
    }

    private Column(String label, boolean ascending) {
      this.label = label;
      this.ascending = ascending;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Column)) {
        return false;
      }
      return BeeUtils.same(label, ((Column) obj).label) && ascending == ((Column) obj).ascending;
    }

    public String getLabel() {
      return label;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(label, ascending);
    }

    public boolean isAscending() {
      return ascending;
    }

    public String transform() {
      if (ascending) {
        return BeeUtils.transform(label);
      } else {
        return BeeUtils.transform(label) + DESC;
      }
    }
  }

  private static final char DESC = '-';
  private static final char COL_SEP = ' ';

  public static Order restore(String s) {
    Order order = new Order();
    order.deserialize(s);
    return order;
  }

  private final List<Column> columns = Lists.newArrayList();

  public void add(String label, boolean ascending) {
    Assert.notEmpty(label);

    Column found = find(label);
    if (found != null) {
      columns.remove(found);
    }
    columns.add(new Column(label.trim(), ascending));
  }
  
  public void deserialize(String s) {
    if (columns.size() > 0) {
      columns.clear();
    }
    if (BeeUtils.isEmpty(s)) {
      return;
    }

    String[] arr = BeeUtils.split(s, COL_SEP);
    String lbl;
    boolean asc;
    for (String z : arr) {
      if (BeeUtils.isPrefixOrSuffix(z, DESC)) {
        lbl = BeeUtils.removePrefixAndSuffix(z, DESC);
        asc = false;
      } else {
        lbl = z;
        asc = true;
      }
      add(lbl, asc);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } 
    if (!(obj instanceof Order)) {
      return false;
    }
    return columns.equals(((Order) obj).columns);
  }

  public List<Column> getColumns() {
    return columns;
  }

  @Override
  public int hashCode() {
    return columns.hashCode();
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder();
    for (Column col : columns) {
      if (sb.length() > 0) {
        sb.append(COL_SEP);
      }
      sb.append(col.transform());
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return serialize();
  }
  
  public String transform() {
    return serialize();
  }

  private Column find(String label) {
    if (BeeUtils.isEmpty(label)) {
      return null;
    }
    for (Column col : columns) {
      if (BeeUtils.same(col.label, label)) {
        return col;
      }
    }
    return null;
  }
}
