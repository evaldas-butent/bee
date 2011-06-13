package com.butent.bee.shared.data.view;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

/**
 * Implements sorting functionality in data objects.
 */

public class Order implements BeeSerializable, Transformable {

  public class Column implements BeeSerializable, Transformable {

    private final String name;
    private final String source;
    private boolean ascending;

    private Column(String name, String source) {
      this(name, source, true);
    }

    private Column(String name, String source, boolean ascending) {
      this.name = name;
      this.source = source;
      this.ascending = ascending;
    }

    public void deserialize(String s) {
      Assert.untouchable();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Column)) {
        return false;
      }
      return BeeUtils.same(name, ((Column) obj).name) && ascending == ((Column) obj).ascending;
    }

    public String getName() {
      return name;
    }

    public String getSource() {
      return source;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, ascending);
    }

    public boolean isAscending() {
      return ascending;
    }

    public String serialize() {
      return Codec.beeSerializeAll(name, source, ascending);
    }

    public void setAscending(boolean ascending) {
      this.ascending = ascending;
    }

    public String transform() {
      return BeeUtils.concat(1, name, source, ascending);
    }

    private boolean is(String id) {
      return BeeUtils.same(this.name, id);
    }
  }

  public static Order restore(String s) {
    Order order = new Order();
    order.deserialize(s);
    return order;
  }

  private final List<Column> columns = Lists.newArrayList();

  public Order() {
    super();
  }
  
  public Order(String name, boolean ascending) {
    this();
    add(name, name, ascending);
  }

  public void add(String name, String source, boolean ascending) {
    Assert.notEmpty(name);

    Column found = find(name);
    if (found != null) {
      columns.remove(found);
    }
    columns.add(new Column(name.trim(), source, ascending));
  }

  public void clear() {
    columns.clear();
  }

  public void deserialize(String s) {
    if (columns.size() > 0) {
      columns.clear();
    }
    if (BeeUtils.isEmpty(s)) {
      return;
    }

    for (String col : Codec.beeDeserialize(s)) {
      String[] arr = Codec.beeDeserialize(col);
      Assert.lengthEquals(arr, 3);
      add(BeeUtils.trim(arr[0]), BeeUtils.trim(arr[1]), BeeUtils.toBoolean(arr[2]));
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

  public int getIndex(String name) {
    Assert.notNull(name);
    for (int i = 0; i < getSize(); i++) {
      if (columns.get(i).is(name)) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  public int getSize() {
    return columns.size();
  }

  @Override
  public int hashCode() {
    return columns.hashCode();
  }

  public boolean isAscending(String name) {
    Assert.notNull(name);

    for (Column col : columns) {
      if (col.is(name)) {
        return col.isAscending();
      }
    }
    return false;
  }

  public boolean remove(String name) {
    Assert.notEmpty(name);

    Column found = find(name);
    if (found != null) {
      columns.remove(found);
      return true;
    } else {
      return false;
    }
  }

  public String serialize() {
    return Codec.beeSerialize(columns);
  }

  public void setAscending(String name, boolean ascending) {
    Assert.notNull(name);
    Column col = find(name);
    Assert.notNull(col);
    col.setAscending(ascending);
  }

  @Override
  public String toString() {
    return serialize();
  }

  public String transform() {
    return serialize();
  }

  private Column find(String name) {
    if (BeeUtils.isEmpty(name)) {
      return null;
    }
    for (Column col : columns) {
      if (col.is(name)) {
        return col;
      }
    }
    return null;
  }
}
