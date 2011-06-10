package com.butent.bee.client.view.edit;

import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.StringPicker;
import com.butent.bee.client.composite.SuggestBox;
import com.butent.bee.client.richtext.RichText;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.client.widget.InputLong;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.InputSlider;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasAcceptableValues;
import com.butent.bee.shared.HasNumberStep;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Creates user interface components for editing values depending on a type of data needed to edit.
 */

public class EditorFactory {

  public static final int START_MOUSE_CLICK = 1;
  public static final int START_KEY_ENTER = 2;
  public static final int START_KEY_DELETE = 3;
  
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

  public static Editor getEditor(EditorDescription description) {
    Assert.notNull(description);
    EditorType type = description.getType();
    Assert.notNull(type);

    Editor editor = null;
    switch (type) {
      case AREA:
        editor = new InputArea();
        if (BeeUtils.isPositive(description.getCharacterWidth())) {
          ((InputArea) editor).setCharacterWidth(description.getCharacterWidth());
        }
        if (BeeUtils.isPositive(description.getVisibleLines())) {
          ((InputArea) editor).setVisibleLines(description.getVisibleLines());
        }
        break;

      case DATE:
        editor = new InputDate(ValueType.DATE);
        break;

      case DATETIME:
        editor = new InputDate(ValueType.DATETIME);
        break;

      case INTEGER:
        editor = new InputInteger();
        break;

      case LIST:
        editor = new BeeListBox();
        break;

      case LONG:
        editor = new InputLong();
        break;

      case NUMBER:
        editor = new InputNumber();
        break;

      case PICKER:
        editor = new StringPicker();
        break;

      case RICH:
        editor = new RichText();
        break;

      case SLIDER:
        editor = new InputSlider();
        break;

      case SPINNER:
        editor = new InputSpinner();
        break;

      case SUGGEST:
        editor = new SuggestBox();
        break;

      case TEXT:
        editor = new InputText();
        break;

      case TOGGLE:
        editor = new Toggle();
        break;
    }

    if (editor instanceof HasNumberStep && description.getStepValue() != null) {
      ((HasNumberStep) editor).setStepValue(description.getStepValue());
    }
    if (editor instanceof HasAcceptableValues && description.getItems() != null) {
      ((HasAcceptableValues) editor).setAcceptableValues(description.getItems());
    }
    return editor;
  }

  private EditorFactory() {
  }
}
