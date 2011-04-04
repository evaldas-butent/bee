package com.butent.bee.client.data;

import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class DataProvider extends AbstractDataProvider<IsRow> {
  private IsTable<?, ?> table;

  public DataProvider(IsTable<?, ?> table) {
    super();
    this.table = table;
  }

  public DataProvider(IsTable<?, ?> table, ProvidesKey<IsRow> keyProvider) {
    super(keyProvider);
    this.table = table;
  }
  
  @Override
  public void addDataDisplay(HasData<IsRow> display) {
    super.addDataDisplay(display);
    display.setRowCount(table.getNumberOfRows(), true);
  }

  public IsTable<?, ?> getTable() {
    return table;
  }
  
  public void refreshDisplays() {
    for (HasData<IsRow> display : getDataDisplays()) {
      updateDisplay(display);
    }
  }

  @Override
  protected void onRangeChanged(HasData<IsRow> display) {
    updateDisplay(display);
  }
  
  private List<? extends IsRow> getRowList() {
    return table.getRows().getList();
  }

  private void updateDisplay(HasData<IsRow> display) {
    Range range = display.getVisibleRange();
    int start = range.getStart();
    int length = range.getLength();
    int rowCount = table.getNumberOfRows();
    
    if (start == 0 && length == rowCount) {
      display.setRowData(start, getRowList());
    } else if (start >= 0 && start < rowCount && length > 0) {
      display.setRowData(start, getRowList().subList(start, 
          BeeUtils.min(start + length, rowCount)));
    }
  }
}
