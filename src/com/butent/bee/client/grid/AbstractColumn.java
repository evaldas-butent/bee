package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.HasValueType;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;

import java.util.List;

/**
 * Is an abstract class for column classes, requires to have methods for get column type, value type
 * and value.
 */

public abstract class AbstractColumn<C> extends Column<IsRow, C> implements HasValueType {

  private List<String> searchBy = null;
  private List<String> sortBy = null;
  
  public AbstractColumn(Cell<C> cell) {
    super(cell);
  }

  public abstract ColType getColType();

  public List<String> getSearchBy() {
    return searchBy;
  }

  public List<String> getSortBy() {
    return sortBy;
  }

  public abstract String getString(Context context, IsRow row);

  @Override
  public abstract C getValue(IsRow row);

  public abstract ValueType getValueType();

  public void setSearchBy(List<String> searchBy) {
    this.searchBy = searchBy;
  }

  public void setSortBy(List<String> sortBy) {
    this.sortBy = sortBy;
  }
}
