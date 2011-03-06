package com.butent.bee.shared.data;

import com.butent.bee.shared.ArraySequence;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.StringArray;
import com.butent.bee.shared.data.sort.SortInfo;
import com.butent.bee.shared.data.value.ValueType;

import java.util.Arrays;

public class StringMatrix<ColType extends IsColumn> extends AbstractTable<StringRow, ColType> {
  private ArraySequence<StringRow> rows = null;
  
  public StringMatrix(String[][] data, String... columnLabels) {
    super(columnLabels);
    rows = new ArraySequence<StringRow>(new StringRow[data.length]);
    for (int i = 0; i < data.length; i++) {
      rows.set(i, new StringRow(i + 1, new StringArray(data[i])));
    }
  }

  protected StringMatrix() {
    super();
    this.rows = new ArraySequence<StringRow>(new StringRow[0]);
  }
  
  protected StringMatrix(ColType... columns) {
    super(columns);
  }

  protected StringMatrix(String... columnLabels) {
    super(columnLabels);
  }

  private StringMatrix(ArraySequence<StringRow> rows) {
    super();
    this.rows = rows;
  }
  
  @Override
  public StringMatrix<ColType> clone() {
    StringMatrix<ColType> result = new StringMatrix<ColType>(rows);
    cloneTableDescription(result);
    return result;
  }

  @Override
  public IsTable<StringRow, ColType> create() {
    return new StringMatrix<ColType>();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public ColType createColumn(ValueType type, String label, String id) {
    return (ColType) new BeeColumn(type, label, id);
  }

  @Override
  public StringRow createRow(long id) {
    return new StringRow(id, new StringArray(0));
  }

  @Override
  public int getNumberOfRows() {
    return (rows == null) ? 0 : getRows().length();
  }

  @Override
  public StringRow getRow(int rowIndex) {
    assertRowIndex(rowIndex);
    return getRows().get(rowIndex);
  }

  public ArraySequence<StringRow> getRows() {
    return rows;
  }

  @Override
  public String getString(int rowIndex, int colIndex) {
    assertRowIndex(rowIndex);
    assertColumnIndex(colIndex);
    return getRows().get(rowIndex).getString(colIndex);
  }

  @Override
  public void removeRow(int rowIndex) {
    assertRowIndex(rowIndex);
    getRows().remove(rowIndex);
  }

  @Override
  public void setValue(int rowIndex, int colIndex, String value) {
    assertRowIndex(rowIndex);
    assertColumnIndex(colIndex);
    getRows().get(rowIndex).setValue(colIndex, value);
  }

  @Override
  public void sort(SortInfo... sortInfo) {
    if (getNumberOfRows() > 1) {
      StringRow[] arr = getRows().getArray();
      RowOrdering ord = new RowOrdering(sortInfo);
      Arrays.sort(arr, ord);
    }
  }

  @Override
  protected void assertRowIndex(int rowIndex) {
    Assert.isIndex(rows, rowIndex);
  }

  @Override
  protected void clearRows() {
    getRows().clear();
  }

  @Override
  protected void insertRow(int rowIndex, StringRow row) {
    Assert.betweenInclusive(rowIndex, 0, getNumberOfRows());
    getRows().insert(rowIndex, row);
  }

  protected void setRows(ArraySequence<StringRow> rows) {
    this.rows = rows;
  }
}
