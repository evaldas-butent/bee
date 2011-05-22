package com.butent.bee.client.view.edit;

import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.widget.InputLong;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;

public class EditorFactory {
  
  public static Editor createEditor(BeeColumn column) {
    Assert.notNull(column);
    
    ValueType type = column.getType();
    if (type == null) {
      return new InputText();
    }

    int precision = column.getPrecision();
    int scale = column.getScale();
    
    Editor editor = null;
    
    switch (type) {
      case BOOLEAN:
        editor = new InputText();
        break;

      case DATE:
      case DATETIME:
        editor = new InputDate();
        break;

      case NUMBER:
        if (scale == 0) {
          if (precision > 10) {
            editor = new InputLong();
          } else {
            editor = new InputInteger();
          }
        } else {
          editor = new InputNumber();
        }
        break;

      case TEXT:
      case TIMEOFDAY:
        editor = new InputText();
        if (precision > 0) {
          ((InputText) editor).setMaxLength(precision);
        }
        break;

      default:
        Assert.untouchable();
    }
    
    editor.setNullable(column.isNullable());
    
    if (editor instanceof InputNumber) {
      ((InputNumber) editor).setPrecision(precision);
      ((InputNumber) editor).setScale(scale);
      if (precision > 0 && precision > scale) {
        ((InputNumber) editor).setMaxLength(precision + ((scale > 0) ? 2 : 1));
      }
    }
    
    return editor;
  }

  private EditorFactory() {
  }
}
