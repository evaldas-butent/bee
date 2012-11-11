package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.HasValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;

import java.util.List;

/**
 * Is an abstract class for grid column classes.
 */

public abstract class AbstractColumn<C> extends Column<IsRow, C> implements HasValueType,
    HasOptions {

  private List<String> searchBy = null;
  private List<String> sortBy = null;

  private String options = null;

  private String classes = null;
  
  private boolean instantKarma = false;

  public AbstractColumn(Cell<C> cell) {
    super(cell);
  }

  public String getClasses() {
    return classes;
  }

  public abstract ColType getColType();

  @Override
  public String getOptions() {
    return options;
  }

  public List<String> getSearchBy() {
    return searchBy;
  }

  public List<String> getSortBy() {
    return sortBy;
  }

  public abstract String getString(Context context, IsRow row);

  @Override
  public abstract C getValue(IsRow row);

  public boolean instantKarma(IsRow row) {
    return instantKarma && getValue(row) != null;
  }

  public void setClasses(String classes) {
    this.classes = classes;
  }

  public void setInstantKarma(boolean instantKarma) {
    this.instantKarma = instantKarma;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setSearchBy(List<String> searchBy) {
    this.searchBy = searchBy;
  }

  public void setSortBy(List<String> sortBy) {
    this.sortBy = sortBy;
  }
}
