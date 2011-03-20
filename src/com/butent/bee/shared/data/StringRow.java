package com.butent.bee.shared.data;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.Sequence;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class StringRow extends AbstractRow {
  private Sequence<String> values;
  
  public StringRow(long id, Sequence<String> values) {
    super(id);
    this.values = values;
  }

  @Override
  public void addCell(IsCell cell) {
    addCell(cell.getValue().getString());
  }

  @Override
  public void addCell(String value) {
    values.insert(values.length(), value);
  }

  @Override
  public void clearCell(int index) {
    assertIndex(index);
    values.set(index, BeeConst.STRING_EMPTY);
  }
  
  @Override
  public StringRow clone() {
    StringRow result = new StringRow(getId(), values);
    cloneProperties(result);
    return result;
  }

  @Override
  public Boolean getBoolean(int col) {
    return BeeUtils.toBoolean(getString(col));
  }

  @Override
  public IsCell getCell(int index) {
    assertIndex(index);
    return new TableCell(values.get(index));
  }
  
  @Override
  public List<IsCell> getCells() {
    List<IsCell> lst = Lists.newArrayList();
    for (int i = 0; i < values.length(); i++) {
      lst.add(getCell(i));
    }
    return lst;
  }

  @Override
  public JustDate getDate(int col) {
    return new JustDate(getInt(col));
  }

  @Override
  public DateTime getDateTime(int col) {
    return new DateTime(getLong(col));
  }
  
  @Override
  public Double getDouble(int col) {
    return BeeUtils.toDouble(getString(col));
  }

  public int getInt(int col) {
    return BeeUtils.toInt(getString(col));
  }
  
  public long getLong(int col) {
    return BeeUtils.toLong(getString(col));
  }
  
  @Override
  public int getNumberOfCells() {
    return values.length(); 
  }
  
  public String getString(int index) {
    assertIndex(index);
    return values.get(index);
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
