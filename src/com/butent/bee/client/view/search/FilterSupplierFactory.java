package com.butent.bee.client.view.search;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.ui.FilterSupplierType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public final class FilterSupplierFactory {

  public static AbstractFilterSupplier getSupplier(String viewName, List<BeeColumn> dataColumns,
      String idColumnName, String versionColumnName, int sourceIndex, String label,
      List<String> searchColumns, FilterSupplierType type,
      List<String> renderColumns, List<String> orderColumns,
      String enumKey, Relation relation, String options) {

    BeeColumn sourceColumn = BeeUtils.getQuietly(dataColumns, sourceIndex);
    BeeColumn filterColumn = BeeUtils.isEmpty(searchColumns)
        ? sourceColumn : DataUtils.getColumn(searchColumns.get(0), dataColumns);

    AbstractFilterSupplier supplier = null;

    if (type != null) {
      switch (type) {
        case VALUE:
          supplier =
              new ValueFilterSupplier(viewName, dataColumns, idColumnName, versionColumnName,
                  filterColumn, label, searchColumns, options);
          break;

        case RANGE:
          supplier = new RangeFilterSupplier(viewName, filterColumn, label, options);
          break;

        case LIST:
          supplier = new ListFilterSupplier(viewName, sourceColumn, filterColumn, label,
              renderColumns, orderColumns, relation, options);
          break;

        case ENUM:
          supplier = new EnumFilterSupplier(viewName, filterColumn, options, label, enumKey);
          break;

        case ID:
          supplier = new IdFilterSupplier(viewName, BeeColumn.forRowId(idColumnName), label,
              options);
          break;

        case VERSION:
          supplier =
              new VersionFilterSupplier(viewName, BeeColumn.forRowVersion(versionColumnName),
                  label, options);
          break;
      }
    }

    if (supplier == null) {
      if (!BeeUtils.isEmpty(enumKey)) {
        supplier = new EnumFilterSupplier(viewName, filterColumn, options, label, enumKey);
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
          supplier = new RangeFilterSupplier(viewName, filterColumn, label, options);
          break;

        case TEXT:
        case BLOB:
        case TIME_OF_DAY:
          supplier =
              new ValueFilterSupplier(viewName, dataColumns, idColumnName, versionColumnName,
                  filterColumn, label, searchColumns, options);
          break;
      }
    }

    return supplier;
  }

  private FilterSupplierFactory() {
  }
}
