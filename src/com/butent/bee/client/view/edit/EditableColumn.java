package com.butent.bee.client.view.edit;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasNumberBounds;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;

/**
 * Enables using data editing in grids, ensures validations, contains information about editor type
 * etc.
 */

public class EditableColumn implements KeyDownHandler, BlurHandler, EditStopEvent.Handler {

  private static final String STYLE_EDITOR = "bee-CellGridEditor";

  private final int colIndex;
  private final BeeColumn dataColumn;
  private final RelationInfo relationInfo;
  private final String caption;
  private final AbstractColumn<?> uiColumn;

  private final Evaluator editable;
  private final Evaluator validation;
  private final Evaluator carry;

  private final String minValue;
  private final String maxValue;

  private final EditorDescription editorDescription;

  private Editor editor = null;
  private IsRow rowValue = null;

  private State state = State.PENDING;

  private NotificationListener notificationListener = null;

  private EditEndEvent.Handler closeHandler = null;

  public EditableColumn(List<BeeColumn> dataColumns, int colIndex, RelationInfo relationInfo,
      AbstractColumn<?> uiColumn, String caption, ColumnDescription columnDescr) {
    Assert.isIndex(dataColumns, colIndex);
    Assert.notNull(uiColumn);
    this.colIndex = colIndex;
    this.dataColumn = dataColumns.get(colIndex);
    this.relationInfo = relationInfo;
    this.uiColumn = uiColumn;
    this.caption = caption;

    if (columnDescr == null) {
      this.editable = null;
      this.validation = null;
      this.carry = null;
      this.minValue = null;
      this.maxValue = null;
      this.editorDescription = null;
    } else {
      String source = this.dataColumn.getId();
      this.editable = Evaluator.create(columnDescr.getEditable(), source, dataColumns);
      this.validation = Evaluator.create(columnDescr.getValidation(), source, dataColumns);
      this.carry = Evaluator.create(columnDescr.getCarry(), source, dataColumns);
      this.minValue = columnDescr.getMinValue();
      this.maxValue = columnDescr.getMaxValue();
      this.editorDescription = columnDescr.getEditor();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EditableColumn)) {
      return false;
    }
    return getColIndex() == ((EditableColumn) obj).getColIndex();
  }

  public String getCaption() {
    return caption;
  }

  public String getCarryValue(IsRow row) {
    if (row == null || getCarry() == null) {
      return null;
    }

    getCarry().update(row, BeeConst.UNDEF, getColIndex(), getDataType(),
        row.getString(getColIndex()));
    String value = getCarry().evaluate();
    if (BeeUtils.isEmpty(value)) {
      return null;
    }

    switch (getDataType()) {
      case BOOLEAN:
        return BooleanValue.pack(BeeUtils.toBooleanOrNull(value));

      case DATE:
        if (BeeUtils.isLong(value)) {
          return BeeUtils.toString(new JustDate(TimeUtils.toDateTimeOrNull(value)).getDay());
        } else {
          return null;
        }

      case DATETIME:
        return BeeUtils.isLong(value) ? value.trim() : null;

      case DECIMAL:
        return BeeUtils.isDecimal(value) ? value.trim() : null;

      case INTEGER:
        return BeeUtils.isInt(value) ? value.trim() : null;

      case LONG:
        return BeeUtils.isLong(value) ? value.trim() : null;

      case NUMBER:
        return BeeUtils.isDouble(value) ? value.trim() : null;

      case TEXT:
      case TIMEOFDAY:
        return BeeUtils.trimRight(value);
    }
    return null;
  }

  public int getColIndex() {
    return colIndex;
  }

  public BeeColumn getColumnForUpdate() {
    return (getRelationInfo() == null) ? getDataColumn() : getRelationInfo().getDataColumn();
  }

  public String getColumnId() {
    return getDataColumn().getId();
  }

  public BeeColumn getDataColumn() {
    return dataColumn;
  }

  public ValueType getDataType() {
    return getDataColumn().getType();
  }

  public Editor getEditor() {
    return editor;
  }

  public EditorDescription getEditorDescription() {
    return editorDescription;
  }

  public String getOldValueForUpdate(IsRow row) {
    int index = (getRelationInfo() == null) ? getColIndex() : getRelationInfo().getDataIndex();
    return row.getString(index);
  }

  public RelationInfo getRelationInfo() {
    return relationInfo;
  }

  public boolean getRowModeForUpdate() {
    return getRelationInfo() != null;
  }

  public AbstractColumn<?> getUiColumn() {
    return uiColumn;
  }

  public boolean hasCarry() {
    return getCarry() != null;
  }

  @Override
  public int hashCode() {
    return getColIndex();
  }

  public boolean isCellEditable(IsRow row, boolean warn) {
    if (row == null) {
      return false;
    }
    if (getEditable() == null) {
      return true;
    }

    getEditable().update(row, BeeConst.UNDEF, getColIndex(), getDataType(),
        row.getString(getColIndex()));
    boolean ok = BeeUtils.toBoolean(getEditable().evaluate());

    if (!ok && warn && getNotificationListener() != null) {
      getNotificationListener().notifyWarning("Cell is read only:", getEditable().transform());
    }
    return ok;
  }

  public boolean isNullable() {
    if (getRelationInfo() != null) {
      return getRelationInfo().isNullable();
    } else if (getDataColumn() != null) {
      return getDataColumn().isNullable();
    } else {
      return true;
    }
  }

  public void onBlur(BlurEvent event) {
    if (State.OPEN.equals(getState())) {
      closeEditor(null, false);
    }
  }

  public void onEditStop(EditStopEvent event) {
    if (event.isFinished()) {
      endEdit(null, false);
    } else if (event.isError()) {
      if (getNotificationListener() != null) {
        getNotificationListener().notifySevere(event.getMessage());
      }
    } else {
      closeEditor(null, false);
    }
  }

  public void onKeyDown(KeyDownEvent event) {
    int keyCode = event.getNativeKeyCode();
    if (getEditor() == null || getEditor().handlesKey(keyCode)) {
      return;
    }

    NativeEvent nativeEvent = event.getNativeEvent();

    switch (keyCode) {
      case KeyCodes.KEY_ESCAPE:
        EventUtils.eatEvent(nativeEvent);
        closeEditor(keyCode, EventUtils.hasModifierKey(nativeEvent));
        break;

      case KeyCodes.KEY_ENTER:
      case KeyCodes.KEY_TAB:
      case KeyCodes.KEY_UP:
      case KeyCodes.KEY_DOWN:
        EventUtils.eatEvent(nativeEvent);
        endEdit(keyCode, EventUtils.hasModifierKey(nativeEvent));
        break;
    }
  }

  public void openEditor(HasWidgets editorContainer, Element sourceElement,
      Element adjustElement, int zIndex, IsRow row, char charCode, EditEndEvent.Handler handler) {
    Assert.notNull(handler);

    setCloseHandler(handler);
    setRowValue(row);
    setState(State.OPEN);

    ensureEditor(editorContainer);
    Element editorElement = getEditor().asWidget().getElement();

    if (sourceElement != null) {
      adjustEditor(sourceElement, editorElement, adjustElement);
    }

    StyleUtils.setZIndex(editorElement, zIndex);
    StyleUtils.unhideDisplay(editorElement);
    getEditor().setFocus(true);

    getEditor().setEditing(true);

    String oldValue = (row == null) ? null : row.getString(getColIndex());
    EditorAction onEntry =
        (getEditorDescription() == null) ? null : getEditorDescription().getOnEntry();

    getEditor().startEdit(oldValue, charCode, onEntry);
  }

  public void setNotificationListener(NotificationListener notificationListener) {
    this.notificationListener = notificationListener;
  }

  private void adjustEditor(Element sourceElement, Element editorElement, Element adjustElement) {
    if (sourceElement != null) {
      if (getEditor() instanceof AdjustmentListener) {
        ((AdjustmentListener) getEditor()).adjust(sourceElement);
      } else {
        StyleUtils.copyBox(sourceElement, editorElement);
        StyleUtils.copyFont(sourceElement, editorElement);
      }
    }

    int left = StyleUtils.getLeft(editorElement);
    int width = StyleUtils.getWidth(editorElement);

    int top = StyleUtils.getTop(editorElement);
    int height = StyleUtils.getHeight(editorElement);

    int horMargins = 10;
    int vertMargins = 10;

    if (getEditorDescription() != null) {
      int editorWidth = BeeConst.UNDEF;
      int editorHeight = BeeConst.UNDEF;
      int editorMinWidth = BeeConst.UNDEF;
      int editorMinHeight = BeeConst.UNDEF;

      EditorType editorType = getEditorDescription().getType();
      if (editorType != null) {
        if (BeeUtils.isPositive(editorType.getDefaultWidth())) {
          editorWidth = editorType.getDefaultWidth();
        }
        if (BeeUtils.isPositive(editorType.getDefaultHeight())) {
          editorHeight = editorType.getDefaultHeight();
        }
        if (BeeUtils.isPositive(editorType.getMinWidth())) {
          editorMinWidth = editorType.getMinWidth();
        }
        if (BeeUtils.isPositive(editorType.getMinHeight())) {
          editorMinHeight = editorType.getMinHeight();
        }
      }

      if (BeeUtils.isPositive(getEditorDescription().getWidth())) {
        editorWidth = getEditorDescription().getWidth();
      }
      if (BeeUtils.isPositive(getEditorDescription().getHeight())) {
        editorHeight = getEditorDescription().getHeight();
      }
      if (BeeUtils.isPositive(getEditorDescription().getMinWidth())) {
        editorMinWidth = getEditorDescription().getMinWidth();
      }
      if (BeeUtils.isPositive(getEditorDescription().getMinHeight())) {
        editorMinHeight = getEditorDescription().getMinHeight();
      }

      if (editorWidth > width) {
        StyleUtils.setWidth(editorElement, editorWidth);
        width = editorWidth;
      } else if (editorMinWidth > width) {
        StyleUtils.setWidth(editorElement, editorMinWidth);
        width = editorMinWidth;
      }

      if (editorHeight > height) {
        StyleUtils.setHeight(editorElement, editorHeight);
        height = editorHeight;
      } else if (editorMinHeight > height) {
        StyleUtils.setHeight(editorElement, editorMinHeight);
        height = editorMinHeight;
      }
    }

    if (adjustElement != null) {

      int x = adjustElement.getScrollLeft();
      int maxWidth = adjustElement.getClientWidth();
      if (x > 0 && adjustElement == editorElement.getParentElement()) {
        maxWidth += x;
        x = 0;
      }

      if (x > 0 || left + width + horMargins > maxWidth) {
        left -= x;
        int newWidth = width;
        if (left < 0) {
          newWidth += left;
          left = 0;
        }
        if (left + newWidth + horMargins > maxWidth) {
          if (left > 0) {
            left = Math.max(0, maxWidth - newWidth - horMargins);
          }
          if (left + newWidth + horMargins > maxWidth) {
            newWidth = maxWidth - left - horMargins;
          }
        }
        StyleUtils.setLeft(editorElement, left);
        if (newWidth > 0 && newWidth != width) {
          StyleUtils.setWidth(editorElement, newWidth);
        }
      }

      int y = adjustElement.getScrollTop();
      int maxHeight = adjustElement.getClientHeight();
      if (y > 0 && adjustElement == editorElement.getParentElement()) {
        maxHeight += y;
        y = 0;
      }

      if (y > 0 || top + height + vertMargins > maxHeight) {
        top -= y;
        int newHeight = height;
        if (top < 0) {
          newHeight += top;
          top = 0;
        }
        if (top + newHeight + vertMargins > maxHeight) {
          if (top > 0) {
            top = Math.max(0, maxHeight - newHeight - vertMargins);
          }
          if (top + newHeight + vertMargins > maxHeight) {
            newHeight = maxHeight - top - vertMargins;
          }
        }
        StyleUtils.setTop(editorElement, top);
        if (newHeight > 0 && newHeight != height) {
          StyleUtils.setHeight(editorElement, newHeight);
        }
      }
    }
  }

  private void closeEditor(Integer keyCode, boolean hasModifiers) {
    closeEditor(null, null, null, false, keyCode, hasModifiers);
  }

  private void closeEditor(IsColumn column, String oldValue, String newValue,
      boolean rowMode, Integer keyCode, boolean hasModifiers) {
    setState(State.CLOSED);
    getEditor().setEditing(false);
    StyleUtils.hideDisplay(getEditor().asWidget());

    getCloseHandler().onEditEnd(new EditEndEvent(getRowValue(), column, oldValue, newValue,
        rowMode, keyCode, hasModifiers));
  }

  private boolean endEdit(Integer keyCode, boolean hasModifiers) {
    if (State.OPEN.equals(getState())) {
      String oldValue = getOldValueForUpdate(getRowValue());
      String editorValue = getEditor().getValue();

      if (BeeUtils.equalsTrimRight(oldValue, editorValue)) {
        closeEditor(keyCode, hasModifiers);
        return true;
      }

      String errorMessage = getEditor().validate();
      if (!BeeUtils.isEmpty(errorMessage)) {
        if (getNotificationListener() != null) {
          getNotificationListener().notifySevere(editorValue, errorMessage);
        }
        return false;
      }

      String newValue = getEditor().getNormalizedValue();
      if (!validate(oldValue, newValue)) {
        return false;
      }

      closeEditor(getColumnForUpdate(), oldValue, newValue, getRowModeForUpdate(),
          keyCode, hasModifiers);
      return true;
    }
    return false;
  }

  private void ensureEditor(HasWidgets editorContainer) {
    if (getEditor() != null) {
      return;
    }

    String format = null;
    if (getEditorDescription() != null) {
      setEditor(EditorFactory.getEditor(getEditorDescription(), isNullable(), getRelationInfo()));
      format = getEditorDescription().getFormat();
    } else if (getRelationInfo() != null) {
      setEditor(new DataSelector(getRelationInfo()));
      getEditor().setNullable(isNullable());
    } else {
      setEditor(EditorFactory.createEditor(getDataColumn(), isNullable()));
    }

    getEditor().asWidget().addStyleName(STYLE_EDITOR);

    if (BeeUtils.isEmpty(format)) {
      if (getUiColumn() instanceof HasDateTimeFormat) {
        LocaleUtils.copyDateTimeFormat(getUiColumn(), getEditor());
      }
      if (getUiColumn() instanceof HasNumberFormat) {
        LocaleUtils.copyNumberFormat(getUiColumn(), getEditor());
      }
    } else {
      Format.setFormat(getEditor(), getDataType(), format);
    }

    initEditor();

    Assert.notNull(editorContainer);
    editorContainer.add(getEditor().asWidget());
  }

  private Evaluator getCarry() {
    return carry;
  }

  private EditEndEvent.Handler getCloseHandler() {
    return closeHandler;
  }

  private Evaluator getEditable() {
    return editable;
  }

  private String getMaxValue() {
    return maxValue;
  }

  private String getMinValue() {
    return minValue;
  }

  private NotificationListener getNotificationListener() {
    return notificationListener;
  }

  private IsRow getRowValue() {
    return rowValue;
  }

  private State getState() {
    return state;
  }

  private Evaluator getValidation() {
    return validation;
  }

  private void initEditor() {
    if (getEditor() == null) {
      return;
    }
    getEditor().addKeyDownHandler(this);
    getEditor().addBlurHandler(this);
    getEditor().addEditStopHandler(this);

    if (getEditor() instanceof HasNumberBounds) {
      if (BeeUtils.isDouble(getMinValue())) {
        ((HasNumberBounds) getEditor()).setMinValue(BeeUtils.toDoubleOrNull(getMinValue()));
      }
      if (BeeUtils.isDouble(getMaxValue())) {
        ((HasNumberBounds) getEditor()).setMaxValue(BeeUtils.toDoubleOrNull(getMaxValue()));
      }
    }
  }

  private void setCloseHandler(EditEndEvent.Handler closeHandler) {
    this.closeHandler = closeHandler;
  }

  private void setEditor(Editor editor) {
    this.editor = editor;
  }

  private void setRowValue(IsRow rowValue) {
    this.rowValue = rowValue;
  }

  private void setState(State state) {
    this.state = state;
  }

  private boolean validate(String oldValue, String newValue) {
    if (BeeUtils.equalsTrimRight(oldValue, newValue)) {
      return true;
    }
    String errorMessage = null;

    if (getValidation() != null) {
      getValidation().update(getRowValue(), BeeConst.UNDEF, getColIndex(),
          getDataType(), oldValue, newValue);
      String msg = getValidation().evaluate();
      if (!BeeUtils.isEmpty(msg)) {
        errorMessage = msg;
      }
    }

    if (errorMessage == null
        && (!BeeUtils.isEmpty(getMinValue()) || !BeeUtils.isEmpty(getMaxValue()))) {
      ValueType type = getDataType();
      Value value = Value.parseValue(type, newValue, false);

      if (!BeeUtils.isEmpty(getMinValue())
          && value.compareTo(Value.parseValue(type, getMinValue(), true)) < 0) {
        errorMessage = BeeUtils.concat(1, errorMessage, "Min value:", getMinValue());
      }
      if (!BeeUtils.isEmpty(getMaxValue())
          && value.compareTo(Value.parseValue(type, getMaxValue(), true)) > 0) {
        errorMessage = BeeUtils.concat(1, errorMessage, "Max value:", getMaxValue());
      }
    }

    if (errorMessage == null) {
      return true;
    } else {
      if (getNotificationListener() != null) {
        getNotificationListener().notifySevere(errorMessage);
      }
      return false;
    }
  }
}
