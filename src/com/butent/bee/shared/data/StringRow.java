package com.butent.bee.shared.data;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Sequence;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Extends {@code AbstractRow} class, contains it's information in string sequences.
 */

public class StringRow extends AbstractRow {

  private Sequence<String> values;

  public StringRow(long id, Sequence<String> values) {
    super(id);
    this.values = values;
  }

  @Override
  public void addCell(IsCell cell) {
    Assert.notNull(cell);
    values.add(cell.getValue().getString());
  }

  @Override
  public void clearCell(int index) {
    assertIndex(index);
    values.set(index, BeeConst.STRING_EMPTY);
  }

  @Override
  public StringRow clone() {
    StringRow result = new StringRow(getId(), values.clone());
    result.setVersion(getVersion());
    cloneProperties(result);
    return result;
  }

  @Override
  public Boolean getBoolean(int col) {
    return BeeUtils.toBooleanOrNull(getString(col));
  }

  @Override
  public IsCell getCell(int index) {
    assertIndex(index);
    return new TableCell(new TextValue(values.get(index)));
  }

  @Override
  public List<IsCell> getCells() {
    List<IsCell> lst = Lists.newArrayList();
    for (int i = 0; i < values.getLength(); i++) {
      lst.add(getCell(i));
    }
    return lst;
  }

  @Override
  public JustDate getDate(int col) {
    if (isNull(col)) {
      return null;
    }
    return new JustDate(getInteger(col));
  }

  @Override
  public DateTime getDateTime(int col) {
    if (isNull(col)) {
      return null;
    }
    return new DateTime(getLong(col));
  }

  @Override
  public BigDecimal getDecimal(int col) {
    return BeeUtils.toDecimalOrNull(getString(col));
  }

  @Override
  public Double getDouble(int col) {
    return BeeUtils.toDoubleOrNull(getString(col));
  }

  public Integer getInteger(int col) {
    return BeeUtils.toIntOrNull(getString(col));
  }

  public Long getLong(int col) {
    return BeeUtils.toLongOrNull(getString(col));
  }

  @Override
  public int getNumberOfCells() {
    return values.getLength();
  }

  public String getString(int index) {
    assertIndex(index);
    return values.get(index);
  }

  public String[] getValueArray() {
    Pair<String[], Integer> pair = getValues().getArray(BeeConst.EMPTY_STRING_ARRAY);
    int len = pair.getB();
    if (len == pair.getA().length) {
      return pair.getA();
    } else {
      String[] arr = new String[len];
      System.arraycopy(pair.getA(), 0, arr, 0, len);
      return arr;
    }
  }

  public Sequence<String> getValues() {
    return values;
  }

  @Override
  public void insertCell(int index, IsCell cell) {
    Assert.betweenInclusive(index, 0, getNumberOfCells());
    values.insert(index, cell.getValue().getString());
  }

  @Override
  public boolean isNull(int index) {
    assertIndex(index);
    return values.get(index) == null;
  }

  @Override
  public void removeCell(int index) {
    assertIndex(index);
    values.remove(index);
  }

  @Override
  public void setCell(int index, IsCell cell) {
    assertIndex(index);
    values.set(index, cell.getValue().getString());
  }

  @Override
  public void setCells(List<IsCell> cells) {
    for (int i = 0; i < cells.size(); i++) {
      setCell(i, cells.get(i));
    }
  }

  public void setValue(int index, String value) {
    assertIndex(index);
    values.set(index, value);
  }

  public void setValues(Sequence<String> values) {
    this.values = values;
  }

  @Override
  protected void assertIndex(int index) {
    Assert.isIndex(values, index);
  }
}
