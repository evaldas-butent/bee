package com.butent.bee.client.view.edit;

import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.client.widget.InputLong;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

public class EditorFactory {
  
  public static final int START_MOUSE_CLICK = 1;
  public static final int START_KEY_ENTER = 2;
  
  private enum EditorType {
    LIST("list"),
    AREA("area"),
    DATE("date"),
    TEXT("text"),
    RICH("rich"),
    NUMBER("number"),
    INTEGER("integer"),
    SLIDER("slider"),
    SPINNER("spinner"),
    LONG("long"),
    SUGGEST("suggest"),
    TOGGLE("toggle");
    
    private static EditorType getByTypeCode(String code) {
      if (code == null || code.isEmpty()) {
        return null;
      }
      for (EditorType type : EditorType.values()) {
        if (BeeUtils.same(type.getTypeCode(), code)) {
          return type;
        }
      }
      return null;
    }
    
    private final String typeCode;

    private EditorType(String typeCode) {
      this.typeCode = typeCode;
    }

    private String getTypeCode() {
      return typeCode;
    }
  }
  
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
        editor = new Toggle();
        break;

      case DATE:
      case DATETIME:
        editor = new InputDate(type);
        break;
        
      case INTEGER:
        editor = new InputInteger();
        break;
        
      case LONG:
        editor = new InputLong();
        break;

      case NUMBER:
      case DECIMAL:
        editor = new InputNumber();
        break;

      case TEXT:
      case TIMEOFDAY:
        editor = new InputText();
        if (precision > 0) {
          ((InputText) editor).setMaxLength(precision);
        }
        break;
    }
    Assert.notNull(editor);
    
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
