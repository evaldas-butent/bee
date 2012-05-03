package com.butent.bee.client.view.edit;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.StringPicker;
import com.butent.bee.client.composite.TextEditor;
import com.butent.bee.client.richtext.RichTextEditor;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.utils.BeeCommand;
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
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.HasNumberStep;
import com.butent.bee.shared.HasPrecision;
import com.butent.bee.shared.HasScale;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.ui.HasTextDimensions;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.ui.HasVisibleLines;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Creates user interface components for editing values depending on a type of data needed to edit.
 */

public class EditorFactory {

  public static final int START_MOUSE_CLICK = 1;
  public static final int START_KEY_ENTER = 2;
  public static final int START_KEY_DELETE = 3;

  /**
   * Executes edit stop event.
   */

  private static class StopCommand extends BeeCommand {
    private final HasHandlers editor;
    private final State state;

    private StopCommand(HasHandlers editor, State state) {
      super();
      this.editor = editor;
      this.state = state;
    }

    public void execute() {
      if (editor != null) {
        editor.fireEvent(new EditStopEvent(state));
      }
    }
  }

  /**
   * Changes the editor state to changed.
   */

  public static class Accept extends StopCommand {
    public Accept(HasHandlers editor) {
      super(editor, State.CHANGED);
    }
  }

  /**
   * Changes the editor state to canceled.
   */

  public static class Cancel extends StopCommand {
    public Cancel(HasHandlers editor) {
      super(editor, State.CANCELED);
    }
  }

  public static Editor createEditor(BeeColumn column) {
    Assert.notNull(column);
    return createEditor(column, column.isNullable());
  }

  public static Editor createEditor(BeeColumn column, boolean nullable) {
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
        if (precision > 200) {
          editor = new InputArea();
        } else {
          editor = new InputText();
          if (precision > 0) {
            ((InputText) editor).setMaxLength(precision);
          }
        }
        break;

      case TIMEOFDAY:
        editor = new InputText();
        if (precision > 0) {
          ((InputText) editor).setMaxLength(precision);
        }
        break;
    }
    Assert.notNull(editor);
    editor.setNullable(nullable);

    if (editor instanceof HasPrecision) {
      ((HasPrecision) editor).setPrecision(precision);
    }
    if (editor instanceof HasScale) {
      ((HasScale) editor).setScale(scale);
    }
    if (editor instanceof InputNumber && precision > 0 && precision > scale) {
      ((InputNumber) editor).setMaxLength(precision + (precision - scale) / 3
          + ((scale > 0) ? 2 : 1));
    }

    return editor;
  }

  public static Editor getEditor(EditorDescription description, String itemKey, ValueType valueType,
      boolean nullable, Relation relation) {
    Assert.notNull(description);
    EditorType editorType = description.getType();
    Assert.notNull(editorType);

    Editor editor = null;
    switch (editorType) {
      case AREA:
        editor = new InputArea();
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
        ((BeeListBox) editor).setValueNumeric(ValueType.isNumeric(valueType));
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
        editor = new RichTextEditor();
        break;

      case SLIDER:
        editor = new InputSlider();
        break;

      case SPINNER:
        editor = new InputSpinner();
        break;

      case SELECTOR:
        if (relation != null) {
          editor = new DataSelector(relation, false);
        }
        break;

      case STRING:
        editor = new InputText();
        break;

      case TEXT:
        editor = new TextEditor();
        break;

      case TOGGLE:
        editor = new Toggle();
        break;
    }
    
    Assert.notNull(editor, "cannot create editor");
    editor.setNullable(nullable);

    if (editor instanceof HasValueStartIndex && description.getValueStartIndex() != null) {
      ((HasValueStartIndex) editor).setValueStartIndex(description.getValueStartIndex());
    }
    if (editor instanceof HasNumberStep && description.getStepValue() != null) {
      ((HasNumberStep) editor).setStepValue(description.getStepValue());
    }

    if (editor instanceof HasItems && description.getItems() != null) {
      ((HasItems) editor).setItems(description.getItems());
    }
    if (editor instanceof AcceptsCaptions && !BeeUtils.isEmpty(itemKey)) {
      ((AcceptsCaptions) editor).addCaptions(itemKey);
    }

    if (editor instanceof HasVisibleLines && BeeUtils.isPositive(description.getVisibleLines())) {
      ((HasVisibleLines) editor).setVisibleLines(description.getVisibleLines());
    }
    if (editor instanceof HasTextDimensions 
        && BeeUtils.isPositive(description.getCharacterWidth())) {
      ((HasTextDimensions) editor).setCharacterWidth(description.getCharacterWidth());
    }

    return editor;
  }

  public static int getStartKey(int keyCode) {
    int startKey;
    switch (keyCode) {
      case KeyCodes.KEY_ENTER:
        startKey = START_KEY_ENTER;
        break;
      case KeyCodes.KEY_DELETE:
        startKey = START_KEY_DELETE;
        break;
      default:
        startKey = BeeConst.UNDEF;
    }
    return startKey;
  }

  private EditorFactory() {
  }
}
