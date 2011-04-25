package com.butent.bee.client.grid;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.shared.data.IsColumn;

public class ColumnFooter extends Header<String> {
  private final IsColumn dataColumn;
  private String value = null; 

  public ColumnFooter(IsColumn dataColumn, final ValueUpdater<String> valueUpdater) {
    super(new FooterCell());
    this.dataColumn = dataColumn;
    
    setUpdater(new ValueUpdater<String>() {
      public void update(String newValue) {
        setValue(newValue);
        if (valueUpdater != null) {
          valueUpdater.update(newValue);
        }  
      }
    });
  }

  public IsColumn getDataColumn() {
    return dataColumn;
  }
  
  @Override
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
