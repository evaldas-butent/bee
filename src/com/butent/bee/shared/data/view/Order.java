package com.butent.bee.shared.data.view;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

/**
 * Implements sorting functionality in data objects.
 */

public class Order implements BeeSerializable {

  public static class Column implements BeeSerializable {

    private final String name;
    private final List<String> sources;
    private boolean ascending;

    private Column(String name, List<String> sources, boolean ascending) {
      this.name = name;
      this.sources = sources;
      this.ascending = ascending;
    }

    private Column(String name, String source) {
      this(name, source, true);
    }

    private Column(String name, String source, boolean ascending) {
      this(name, Lists.newArrayList(source), ascending);
    }

    @Override
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
      return BeeUtils.same(getName(), ((Column) obj).getName())
          && isAscending() == ((Column) obj).isAscending();
    }

    public String getName() {
      return name;
    }

    public List<String> getSources() {
      return sources;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(getName(), isAscending());
    }

    public boolean isAscending() {
      return ascending;
    }

    @Override
    public String serialize() {
      Object[] arr = new Object[getSources().size() + 2];
      arr[0] = getName();
      arr[1] = isAscending();
      for (int i = 0; i < getSources().size(); i++) {
        arr[i + 2] = getSources().get(i);
      }
      return Codec.beeSerialize(arr);
    }

    public void setAscending(boolean ascending) {
      this.ascending = ascending;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(getName());
      if (getSources().size() != 1 || !BeeUtils.same(getName(), getSources().get(0))) {
        sb.append(BeeConst.CHAR_EQ).append(getSources());
      }
      if (!isAscending()) {
        sb.append(BeeConst.CHAR_SPACE).append(SORT_DESCENDING);
      }
      return sb.toString();
    }

    private boolean is(String id) {
      return BeeUtils.same(name, id);
    }
  }
  public static final String SORT_ASCENDING = "ascending";

  public static final String SORT_DESCENDING = "descending";

  private static BeeLogger logger = LogUtils.getLogger(Order.class);

  private static final Splitter ITEM_SPLITTER =
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();

  public static boolean isSortAscending(String s) {
    return BeeUtils.inListSame(s, SORT_ASCENDING, BeeConst.STRING_PLUS)
        || BeeUtils.isPrefix(SORT_ASCENDING, s);
  }

  public static boolean isSortDescending(String s) {
    return BeeUtils.inListSame(s, SORT_DESCENDING, BeeConst.STRING_MINUS)
        || BeeUtils.isPrefix(SORT_DESCENDING, s);
  }

  public static Order parse(String input, Collection<String> colNames) {
    Assert.notEmpty(input);
    Assert.notEmpty(colNames);

    Order order = new Order();
    String name;
    boolean asc;
    String w1;
    String w2;

    for (String item : ITEM_SPLITTER.split(input)) {
      if (item.indexOf(BeeConst.CHAR_SPACE) > 0) {
        w1 = BeeUtils.getPrefix(item, BeeConst.CHAR_SPACE);
        w2 = BeeUtils.getSuffix(item, BeeConst.CHAR_SPACE);

        if (isSortAscending(w2)) {
          name = w1;
          asc = true;
        } else if (isSortDescending(w2)) {
          name = w1;
          asc = false;
        } else if (isSortAscending(w1) && NameUtils.isIdentifier(w2)) {
          name = w2;
          asc = true;
        } else if (isSortDescending(w1) && NameUtils.isIdentifier(w2)) {
          name = w2;
          asc = false;
        } else {
          name = null;
          asc = false;
        }

      } else if (BeeUtils.isPrefixOrSuffix(item, BeeConst.CHAR_PLUS)) {
        name = BeeUtils.removePrefixAndSuffix(item, BeeConst.CHAR_PLUS);
        asc = true;
      } else if (BeeUtils.isPrefixOrSuffix(item, BeeConst.CHAR_MINUS)) {
        name = BeeUtils.removePrefixAndSuffix(item, BeeConst.CHAR_MINUS);
        asc = false;

      } else {
        name = item;
        asc = true;
      }

      String colName = BeeUtils.getSame(colNames, name);
      if (BeeUtils.isEmpty(colName)) {
        logger.warning("cannot parse order item: " + item);
        continue;
      }

      order.add(colName, asc);
    }

    if (order.isEmpty()) {
      return null;
    }
    return order;
  }

  public static Order restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
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
    add(name, ascending);
  }

  public void add(String name, boolean ascending) {
    add(name, Lists.newArrayList(name), ascending);
  }

  public void add(String name, List<String> sources, boolean ascending) {
    Assert.notEmpty(name);

    Column found = find(name);
    if (found != null) {
      columns.remove(found);
    }
    columns.add(new Column(name.trim(), sources, ascending));
  }

  public void clear() {
    columns.clear();
  }

  @Override
  public void deserialize(String s) {
    if (!columns.isEmpty()) {
      columns.clear();
    }
    String[] cols = Codec.beeDeserializeCollection(s);
    if (ArrayUtils.isEmpty(cols)) {
      return;
    }

    for (String col : cols) {
      String[] arr = Codec.beeDeserializeCollection(col);
      Assert.minLength(ArrayUtils.length(arr), 3);

      List<String> lst = Lists.newArrayList(ArrayUtils.slice(arr, 2));
      add(arr[0], lst, BeeUtils.toBoolean(arr[1]));
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

  public boolean isEmpty() {
    return columns.isEmpty();
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

  @Override
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
    return BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, getColumns());
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
