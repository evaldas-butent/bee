package com.butent.bee.client.view.edit;

import com.butent.bee.client.composite.DateBox;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;

public class EditorFactory {
  
  public static Editor createEditor(BeeColumn column) {
    Assert.notNull(column);
    
    ValueType type = column.getType();
    if (type == null) {
      return new BeeTextBox();
    }
    int precision = column.getPrecision();
    int scale = column.getScale();
    
    Editor editor = null;
    
    switch (type) {
      case BOOLEAN:
        editor = new BeeTextBox();
        break;

      case DATE:
      case DATETIME:
        editor = new DateBox();
        break;

      case NUMBER:
        editor = new InputInteger();
        break;

      case TEXT:
      case TIMEOFDAY:
        editor = new BeeTextBox();
        if (precision > 0) {
          ((BeeTextBox) editor).setMaxLength(precision);
        }
        break;
      default:
        Assert.untouchable();
    }
    
    return editor;
  }

  private EditorFactory() {
  }
}
