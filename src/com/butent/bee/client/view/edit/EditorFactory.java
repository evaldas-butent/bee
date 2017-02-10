package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.client.composite.ColorEditor;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.TextEditor;
import com.butent.bee.client.dom.Features;
import com.butent.bee.client.richtext.RichTextEditor;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.client.widget.InputLong;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.InputRange;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.InputTime;
import com.butent.bee.client.widget.InputTimeOfDay;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasIntStep;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.HasPrecision;
import com.butent.bee.shared.HasScale;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.HasPercentageTag;
import com.butent.bee.shared.data.HasRelatedCurrency;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.ui.HasCapsLock;
import com.butent.bee.shared.ui.HasMaxLength;
import com.butent.bee.shared.ui.HasTextDimensions;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.ui.HasVisibleLines;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Creates user interface components for editing values depending on a type of data needed to edit.
 */

public final class EditorFactory {

  /**
   * Executes edit stop event.
   */

  private static class StopCommand extends Command {
    private final HasHandlers editor;
    private final State state;

    protected StopCommand(HasHandlers editor, State state) {
      super();
      this.editor = editor;
      this.state = state;
    }

    @Override
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

  public static Editor createEditor(BeeColumn column, boolean isText) {
    Assert.notNull(column);

    ValueType type = column.getType();
    if (type == null) {
      return new InputText();
    }

    Editor editor = null;

    switch (type) {
      case BOOLEAN:
        editor = new Toggle();
        break;

      case DATE:
        editor = new InputDate();
        break;

      case DATE_TIME:
        editor = new InputDateTime();
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
      case BLOB:
        if (isText) {
          editor = new InputArea();
        } else {
          editor = new InputText();
        }
        break;

      case TIME_OF_DAY:
        editor = new InputTimeOfDay();
        break;
    }

    if (editor instanceof HasPrecision) {
      ((HasPrecision) editor).setPrecision(column.getPrecision());
    }
    if (editor instanceof HasScale) {
      ((HasScale) editor).setScale(column.getScale());
    }

    if (editor instanceof HasMaxLength) {
      int maxLength = UiHelper.getMaxLength(column);
      if (maxLength > 0) {
        ((HasMaxLength) editor).setMaxLength(maxLength);
      }
    }

    return editor;
  }

  public static Editor createEditor(EditorDescription description, BeeColumn column,
      String enumKey, ValueType valueType, Relation relation, boolean embedded) {

    Assert.notNull(description);
    EditorType editorType = description.getType();
    Assert.notNull(editorType);

    Editor editor = null;
    switch (editorType) {
      case AREA:
        editor = new InputArea();
        break;

      case DATE:
        editor = new InputDate();
        break;

      case DATE_TIME:
        editor = new InputDateTime();
        break;

      case COLOR:
        if (Features.supportsInputColor()) {
          editor = new ColorEditor();
        } else {
          editor = new InputText();
        }
        break;

      case INTEGER:
        editor = new InputInteger();
        break;

      case LIST:
        editor = new ListBox();
        ((ListBox) editor).setValueNumeric(ValueType.isNumeric(valueType));
        break;

      case LONG:
        editor = new InputLong();
        break;

      case NUMBER:
        editor = new InputNumber();
        break;

      case RICH:
        editor = new RichTextEditor(embedded);
        break;

      case SLIDER:
        editor = new InputRange();
        break;

      case SPINNER:
        editor = new InputSpinner();
        break;

      case SELECTOR:
        if (relation != null) {
          editor = new DataSelector(relation, embedded);
        }
        break;

      case STRING:
        editor = new InputText();
        break;

      case TEXT:
        editor = new TextEditor();
        break;

      case TIME:
        editor = new InputTime();
        break;

      case TIME_OF_DAY:
        editor = new InputTimeOfDay();
        break;

      case TOGGLE:
        editor = new Toggle();
        break;
    }

    Assert.notNull(editor, "cannot create editor");

    if (editor instanceof HasValueStartIndex && description.getValueStartIndex() != null) {
      ((HasValueStartIndex) editor).setValueStartIndex(description.getValueStartIndex());
    }
    if (editor instanceof HasIntStep && description.getStepValue() != null) {
      ((HasIntStep) editor).setStepValue(description.getStepValue());
    }

    if (editor instanceof HasItems && description.getItems() != null) {
      ((HasItems) editor).setItems(Localized.maybeTranslate(description.getItems()));
    }
    if (editor instanceof AcceptsCaptions && !BeeUtils.isEmpty(enumKey)) {
      ((AcceptsCaptions) editor).setCaptions(enumKey);
    }

    if (editor instanceof HasVisibleLines && BeeUtils.isPositive(description.getVisibleLines())) {
      ((HasVisibleLines) editor).setVisibleLines(description.getVisibleLines());
    }

    if (BeeUtils.isPositive(description.getCharacterWidth())) {
      if (editor instanceof HasTextDimensions) {
        ((HasTextDimensions) editor).setCharacterWidth(description.getCharacterWidth());
      } else if (editor instanceof HasMaxLength) {
        ((HasMaxLength) editor).setMaxLength(description.getCharacterWidth());
      }

    } else if (editor instanceof HasMaxLength && column != null) {
      int maxLength = UiHelper.getMaxLength(column);
      if (maxLength > 0) {
        ((HasMaxLength) editor).setMaxLength(maxLength);
      }
    }

    if (editor instanceof HasCapsLock && description.isUpperCase()) {
      ((HasCapsLock) editor).setUpperCase(true);
    }

    if (editor instanceof HasRelatedCurrency
        && !BeeUtils.isEmpty(description.getCurrencySource())) {

      ((HasRelatedCurrency) editor).setCurrencySource(description.getCurrencySource());
    }

    if (editor instanceof HasPercentageTag && !BeeUtils.isEmpty(description.getPercentageTag())) {
      ((HasPercentageTag) editor).setPercentageTag(description.getPercentageTag());
    }

    if (column != null) {
      if (editor instanceof HasPrecision) {
        ((HasPrecision) editor).setPrecision(column.getPrecision());
      }
      if (editor instanceof HasScale) {
        ((HasScale) editor).setScale(column.getScale());
      }
    }

    return editor;
  }

  private EditorFactory() {
  }
}
