package com.butent.bee.client.view.search;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.ui.FilterSupplierType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class FilterSupplierFactory {
  
  public static AbstractFilterSupplier getSupplier(String viewName, BeeColumn column,
      FilterSupplierType type, List<String> renderColumns, List<String> orderColumns,
      String itemKey, Relation relation, String options) {
    
    Assert.notEmpty(viewName);
    
    AbstractFilterSupplier supplier = null;
    
    if (type != null) {
      switch (type) {
        case COMPARISON:
          supplier = new ComparisonFilterSupplier(viewName, column, options);
          break;

        case ENUM:
          supplier = new EnumFilterSupplier(viewName, column, options, itemKey);
          break;

        case ID:
          supplier = new IdFilterSupplier(viewName, column, options);
          break;
        
        case LIST:
          supplier = new ListFilterSupplier(viewName, column, renderColumns, orderColumns,
              relation, options);
          break;
        
        case RANGE:
          supplier = new RangeFilterSupplier(viewName, column, options);
          break;
        
        case STAR:
          supplier = new StarFilterSupplier(viewName, column, options);
          break;

        case VALUE:
          supplier = new ValueFilterSupplier(viewName, column, options);
          break;
          
        case WORD:
          supplier = new WordFilterSupplier(viewName, column, options);
          break;
      }
    }
    
    if (supplier == null) {
      if (!BeeUtils.isEmpty(itemKey)) {
        supplier = new EnumFilterSupplier(viewName, column, options, itemKey);
      } else if (relation != null) {
        supplier = new ListFilterSupplier(viewName, column, renderColumns, orderColumns,
            relation, options);
      }
    }
    
    if (supplier == null && column != null) {
      switch (column.getType()) {
        case BOOLEAN:
          supplier = new BooleanFilterSupplier(viewName, column, options);
          break;

        case DATE:
          supplier = new ValueFilterSupplier(viewName, column, options);
          break;
        
        case DATE_TIME:
          supplier = new DateTimeFilterSupplier(viewName, column, options);
          break;

        case DECIMAL:
        case INTEGER:
        case LONG:
        case NUMBER:
          supplier = new ValueFilterSupplier(viewName, column, options);
          break;

        case TEXT:
          supplier = new ValueFilterSupplier(viewName, column, options);
          break;

        case TIME_OF_DAY:
          supplier = new ValueFilterSupplier(viewName, column, options);
          break;
      }
    }
    
    return supplier;
  }
  
  private FilterSupplierFactory() {
  }
}
