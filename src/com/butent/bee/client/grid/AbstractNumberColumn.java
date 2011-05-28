package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public abstract class AbstractNumberColumn<C extends Number> extends CellColumn<C> implements
    HasNumberFormat {

  public AbstractNumberColumn(Cell<C> cell, int index, IsColumn dataColumn) {
    super(cell, index, dataColumn);
    setHorizontalAlignment(ALIGN_RIGHT);
  }

  public AbstractNumberColumn(NumberFormat format, int index, IsColumn dataColumn) {
    this(new NumberCell<C>(format), index, dataColumn);
  }

  public NumberFormat getNumberFormat() {
    if (getCell() instanceof HasNumberFormat) {
      return ((HasNumberFormat) getCell()).getNumberFormat();
    }
    return null;
  }

  @Override
  public C getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return getValue(row, getIndex());
  }

  public void setNumberFormat(NumberFormat format) {
    if (getCell() instanceof HasNumberFormat) {
      ((HasNumberFormat) getCell()).setNumberFormat(format);
    }
  }

  protected abstract C getValue(IsRow row, int colIndex);
}
