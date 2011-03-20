package com.butent.bee.shared.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.data.JsData;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;

import java.util.List;

public class DataUtils {
  
  @SuppressWarnings("unchecked")
  public static IsTable<?, ?> createTable(Object data, String... columnLabels) {
    Assert.notNull(data);
    IsTable<?, ?> table = null;
    
    if (data instanceof IsTable) {
      table = (IsTable<?, ?>) data;
    } else if (data instanceof String[][]) {
      table = new StringMatrix<TableColumn>((String[][]) data, columnLabels);
    } else if (data instanceof JsArrayString) {
      table = new JsData<TableColumn>((JsArrayString) data, columnLabels);
    } else if (data instanceof List) {
      Object el = BeeUtils.listGetQuietly((List<?>) data, 0);
      
      if (el instanceof ExtendedProperty) {
        table = new ExtendedPropertiesData((List<ExtendedProperty>) data, columnLabels);
      } else if (el instanceof Property) {
        table = new PropertiesData((List<Property>) data, columnLabels);
      }
    }
    
    Assert.notNull(table);
    return table;
  }
  
  public static String defaultColumnId(int index) {
    if (BeeUtils.betweenExclusive(index, 0, 1000)) {
      return "col" + BeeUtils.toLeadingZeroes(index, 3);
    } else {
      return "col" + index;  
    }
  }

  public static String defaultColumnLabel(int index) {
    return "Column " + index;  
  }
  
  private DataUtils() {
  }
}
