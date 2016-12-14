package com.butent.bee.client.view.search;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.data.value.ValueType;

import java.util.List;

public class CustomFilterSupplier extends ValueFilterSupplier {
  private final ValueType columnType;
  private final String columnId;

  public CustomFilterSupplier(String viewName, List<BeeColumn> columns, String idColumnName,
      String versionColumnName, String columnId, ValueType columnType, String label,
      List<BeeColumn> searchColumns, String options) {

    super(viewName, columns, idColumnName, versionColumnName, null, label, searchColumns,
        options);

    this.columnId = columnId;
    this.columnType = columnType;
  }

  @Override
  public String getColumnId() {
    return columnId;
  }

  @Override
  public ValueType getColumnType() {
    return columnType;
  }

  @Override
  public Filter parse(FilterValue input) {
    if (input == null || !input.hasValue()) {
      return null;
    }
    return Filter.custom(getOptions(), getColumnId(), input.getValue());
  }

  @Override
  protected boolean validate(String input) {
    return true;
  }
}
