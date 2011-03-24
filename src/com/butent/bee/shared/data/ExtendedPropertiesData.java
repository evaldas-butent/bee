package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.ExtendedProperty;

import java.util.List;

public class ExtendedPropertiesData extends RowList<TableRow, TableColumn> {
  
  private ExtendedPropertiesData() {
    super();
  }

  public ExtendedPropertiesData(List<ExtendedProperty> data, String... columnLabels) {
    super();

    int pc = columnLabels.length;
    String label;
    for (int i = 0; i < ExtendedProperty.COLUMN_COUNT; i++) {
      label = (pc > 0 && i < pc) ? columnLabels[i] : ExtendedProperty.COLUMN_HEADERS[i];
      addColumn(ValueType.TEXT, label);
    }
    
    for (ExtendedProperty property : data) {
      addRow(property.getName(), property.getSub(), property.getValue(),
          property.getDate().toTimeString());
    }
  }

  @Override
  public ExtendedPropertiesData clone() {
    ExtendedPropertiesData result = new ExtendedPropertiesData();
    cloneTableDescription(result);
    result.setRows(getRows());
    return result;
  }
  
  @Override
  public ExtendedPropertiesData create() {
    return new ExtendedPropertiesData();
  }

  @Override
  public TableColumn createColumn(ValueType type, String label, String id) {
    return new TableColumn(type, label, id);
  }

  @Override
  public TableRow createRow(long id) {
    return new TableRow(id);
  }
}
