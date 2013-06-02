package com.butent.bee.client.view.search;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.ui.FilterSupplierType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class FilterSupplierFactory {

  public static AbstractFilterSupplier getSupplier(String viewName, List<BeeColumn> dataColumns,
      int sourceIndex, String label, List<String> searchColumns, FilterSupplierType type,
      List<String> renderColumns, List<String> orderColumns,
      String itemKey, Relation relation, String options) {

    Assert.notEmpty(viewName);

    BeeColumn sourceColumn = BeeUtils.getQuietly(dataColumns, sourceIndex);
    BeeColumn filterColumn = BeeUtils.isEmpty(searchColumns) 
        ? sourceColumn : DataUtils.getColumn(searchColumns.get(0), dataColumns);

    AbstractFilterSupplier supplier = null;

    if (type != null) {
      switch (type) {
        case ENUM:
          supplier = new EnumFilterSupplier(viewName, filterColumn, options, label, itemKey);
          break;

        case ID:
          supplier = new IdFilterSupplier(viewName, filterColumn, label, options);
          break;

        case LIST:
          supplier = new ListFilterSupplier(viewName, sourceColumn, filterColumn, label,
              renderColumns, orderColumns, relation, options);
          break;

        case VALUE:
          supplier = new ValueFilterSupplier(viewName, filterColumn, label, searchColumns, options);
          break;
      }
    }

    if (supplier == null) {
      if (!BeeUtils.isEmpty(itemKey)) {
        supplier = new EnumFilterSupplier(viewName, filterColumn, options, label, itemKey);
      } else if (relation != null) {
        supplier = new ListFilterSupplier(viewName, sourceColumn, filterColumn, label,
            renderColumns, orderColumns, relation, options);
      }
    }

    if (supplier == null && filterColumn != null) {
      switch (filterColumn.getType()) {
        case BOOLEAN:
          supplier = new BooleanFilterSupplier(viewName, filterColumn, label, options);
          break;

        case DATE:
          supplier = new DateFilterSupplier(viewName, filterColumn, label, options);
          break;

        case DATE_TIME:
          supplier = new DateTimeFilterSupplier(viewName, filterColumn, label, options);
          break;

        case DECIMAL:
        case INTEGER:
        case LONG:
        case NUMBER:
        case TEXT:
        case TIME_OF_DAY:
          supplier = new ValueFilterSupplier(viewName, filterColumn, label, searchColumns, options);
          break;
      }
    }

    return supplier;
  }

  private FilterSupplierFactory() {
  }
}
