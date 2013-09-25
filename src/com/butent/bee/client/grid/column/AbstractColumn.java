package com.butent.bee.client.grid.column;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.client.style.HasWhiteSpace;
import com.butent.bee.client.style.StyleUtils.WhiteSpace;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.HasValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Is an abstract class for grid column classes.
 */

public abstract class AbstractColumn<C> extends Column<IsRow, C> implements HasValueType,
    HasOptions, HasWhiteSpace {

  private List<String> searchBy;
  private List<String> sortBy;
  
  private WhiteSpace whiteSpace;

  private String options;

  private final List<String> classes = Lists.newArrayList();
  
  private boolean instantKarma;

  public AbstractColumn(Cell<C> cell) {
    super(cell);
  }

  public void addClass(String className) {
    if (!BeeUtils.isEmpty(className)) {
      this.classes.add(className);
    }
  }

  public List<String> getClasses() {
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

  public abstract String getStyleSuffix();

  public abstract String getString(Context context, IsRow row);

  @Override
  public abstract C getValue(IsRow row);

  @Override
  public WhiteSpace getWhiteSpace() {
    return whiteSpace;
  }

  public boolean instantKarma(IsRow row) {
    return instantKarma && getValue(row) != null;
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

  @Override
  public void setWhiteSpace(WhiteSpace whiteSpace) {
    this.whiteSpace = whiteSpace;
  }
}
