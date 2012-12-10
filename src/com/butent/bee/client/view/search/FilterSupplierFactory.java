package com.butent.bee.client.view.search;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.ui.FilterSupplierType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class FilterSupplierFactory {
  
  public static AbstractFilterSupplier getSupplier(String viewName, Filter immutableFilter,
      BeeColumn column, FilterSupplierType type, List<String> renderColumns,
      List<String> orderColumns, String itemKey, Relation relation, String options) {
    
    Assert.notEmpty(viewName);
    Assert.notNull(column);
    
    AbstractFilterSupplier supplier = null;
    
    if (type != null) {
      switch (type) {
        case COMPARISON:
          supplier = new ComparisonFilterSupplier(viewName, immutableFilter, column, options);
          break;

        case ENUM:
          supplier = new EnumFilterSupplier(viewName, immutableFilter, column, options, itemKey);
          break;

        case FLAG:
          supplier = new FlagFilterSupplier(viewName, immutableFilter, column, options);
          break;
        
        case LIST:
          supplier = new ListFilterSupplier(viewName, immutableFilter, column,
              renderColumns, orderColumns, relation, options);
          break;
        
        case RANGE:
          supplier = new RangeFilterSupplier(viewName, immutableFilter, column, options);
          break;
        
        case STAR:
          supplier = new StarFilterSupplier(viewName, immutableFilter, column, options);
          break;

        case VALUE:
          supplier = new ValueFilterSupplier(viewName, immutableFilter, column, options);
          break;
          
        case WORD:
          supplier = new WordFilterSupplier(viewName, immutableFilter, column, options);
          break;
      }
    }
    
    if (supplier == null) {
      if (!BeeUtils.isEmpty(itemKey)) {
        supplier = new EnumFilterSupplier(viewName, immutableFilter, column, options, itemKey);
      } else if (relation != null) {
        supplier = new ListFilterSupplier(viewName, immutableFilter, column,
            renderColumns, orderColumns, relation, options);
      }
    }
    
    if (supplier == null) {
      switch (column.getType()) {
        case BOOLEAN:
          break;

        case DATE:
          supplier = new ListFilterSupplier(viewName, immutableFilter, column,
              renderColumns, orderColumns, relation, options);
          break;
        
        case DATETIME:
          supplier = new RangeFilterSupplier(viewName, immutableFilter, column, options);
          break;

        case DECIMAL:
        case INTEGER:
        case LONG:
        case NUMBER:
          supplier = new ComparisonFilterSupplier(viewName, immutableFilter, column, options);
          break;

        case TEXT:
          supplier = new ValueFilterSupplier(viewName, immutableFilter, column, options);
          break;

        case TIMEOFDAY:
          supplier = new ListFilterSupplier(viewName, immutableFilter, column,
              renderColumns, orderColumns, relation, options);
          break;
      }
    }
    
    return supplier;
  }

  private FilterSupplierFactory() {
  }
}
