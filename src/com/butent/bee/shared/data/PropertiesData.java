package com.butent.bee.shared.data;

import com.butent.bee.shared.ListSequence;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.Property;

import java.util.List;

public class PropertiesData extends RowList<StringRow, TableColumn> {
  
  private PropertiesData() {
    super();
  }

  public PropertiesData(List<Property> data, String... columnLabels) {
    super();
    
    int pc = columnLabels.length;
    String label;
    for (int i = 0; i < Property.HEADER_COUNT; i++) {
      label = (pc > 0 && i < pc) ? columnLabels[i] : Property.HEADERS[i];
      addColumn(ValueType.TEXT, label);
    }

    for (Property property : data) {
      addRow(property.getName(), property.getValue());
    }
  }

  @Override
  public PropertiesData clone() {
    PropertiesData result = new PropertiesData();
    cloneTableDescription(result);
    result.setRows(getRows());
    return result;
  }

  @Override
  public PropertiesData create() {
    return new PropertiesData();
  }

  @Override
  public TableColumn createColumn(ValueType type, String label, String id) {
    return new TableColumn(type, label, id);
  }

  @Override
  public StringRow createRow(long id) {
    return new StringRow(id, new ListSequence<String>(0));
  }
}
