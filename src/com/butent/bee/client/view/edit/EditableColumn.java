package com.butent.bee.client.view.edit;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.HasRelatedRow;
import com.butent.bee.client.data.RelationUtils;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.validation.CellValidationBus;
import com.butent.bee.client.validation.HasCellValidationHandlers;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasNumberBounds;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.ui.RefreshType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables using data editing in grids, ensures validations, contains information about editor type
 * etc.
 */

public class EditableColumn implements KeyDownHandler, BlurHandler, EditStopEvent.Handler,
    HasCellValidationHandlers, HasViewName, EditEndEvent.HasEditEndHandler {

  private static final String STYLE_EDITOR = "bee-CellGridEditor";

  private final String viewName;

  private final int colIndex;
  private final BeeColumn dataColumn;
  private final String caption;
  private final AbstractColumn<?> uiColumn;

  private final Evaluator editable;
  private final Evaluator validation;
  private final Evaluator carry;

  private final String minValue;
  private final String maxValue;
  private final Boolean required;

  private final EditorDescription editorDescription;
  private final String itemKey;

  private final RefreshType updateMode;
  private final Relation relation;

  private final CellValidationBus cellValidationBus = new CellValidationBus();

  private Editor editor = null;
  private IsRow rowValue = null;

  private State state = State.PENDING;

  private NotificationListener notificationListener = null;

  private EditEndEvent.Handler closeHandler = null;

  public EditableColumn(String viewName, List<BeeColumn> dataColumns, int colIndex,
      AbstractColumn<?> uiColumn, String caption, ColumnDescription columnDescr) {
    Assert.isIndex(dataColumns, colIndex);
    Assert.notNull(uiColumn);
    Assert.notNull(columnDescr);

    this.viewName = viewName;
    this.colIndex = colIndex;
    this.dataColumn = dataColumns.get(colIndex);
    this.uiColumn = uiColumn;
    this.caption = caption;

    String source = this.dataColumn.getId();
    this.editable = Evaluator.create(columnDescr.getEditable(), source, dataColumns);
    this.validation = Evaluator.create(columnDescr.getValidation(), source, dataColumns);
    this.carry = Evaluator.create(columnDescr.getCarry(), source, dataColumns);

    this.minValue = columnDescr.getMinValue();
    this.maxValue = columnDescr.getMaxValue();
    this.required = columnDescr.isRequired();

    this.editorDescription = columnDescr.getEditor();
    this.itemKey = columnDescr.getItemKey();

    this.updateMode = columnDescr.getUpdateMode();
    this.relation = columnDescr.getRelation();
  }

  public HandlerRegistration addCellValidationHandler(CellValidateEvent.Handler handler) {
    return cellValidationBus.addCellValidationHandler(handler);
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

  public Boolean fireCellValidation(CellValidateEvent event) {
    return cellValidationBus.fireCellValidation(event);
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
          return BeeUtils.toString(new JustDate(TimeUtils.toDateTimeOrNull(value)).getDays());
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
    return getDataColumn();
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

  public int getIndexForUpdate() {
    return getColIndex();
  }

  public String getMaxValue() {
    return maxValue;
  }

  public String getMinValue() {
    return minValue;
  }

  public String getOldValueForUpdate(IsRow row) {
    return row.getString(getIndexForUpdate());
  }

  public boolean getRowModeForUpdate() {
    if (updateMode == null) {
      return hasRelation();
    } else {
      return RefreshType.ROW.equals(updateMode);
    }
  }

  public ValueType getTypeForUpdate() {
    return getColumnForUpdate().getType();
  }

  public AbstractColumn<?> getUiColumn() {
    return uiColumn;
  }

  public Evaluator getValidation() {
    return validation;
  }

  public String getViewName() {
    return viewName;
  }

  public boolean hasCarry() {
    return getCarry() != null;
  }

  @Override
  public int hashCode() {
    return getColIndex();
  }

  public boolean hasRelation() {
    return getRelation() != null;
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
    if (BeeUtils.unbox(isRequired())) {
      return false;
    } else {
      return getDataColumn().isNullable();
    }
  }

  public boolean isWritable() {
    return getDataColumn().isWritable();
  }

  public boolean maybeUpdateRelation(IsRow row, boolean updateColumn) {
    boolean ok = false;
    if (row != null && getEditor() instanceof HasRelatedRow && getRelation() != null) {
      ok = RelationUtils.updateRow(getViewName(), getColumnId(), row, getRelation().getViewName(),
          ((HasRelatedRow) getEditor()).getRelatedRow(), updateColumn) > 0;
    }
    return ok;
  }

  public void onBlur(BlurEvent event) {
    if (State.OPEN.equals(getState())) {
      closeEditor(null, false);
    }
  }

  public void onEditStop(EditStopEvent event) {
    if (event.isChanged()) {
      endEdit(event.getKeyCode(), event.hasModifiers());
    } else if (event.isError()) {
      if (getNotificationListener() != null) {
        getNotificationListener().notifySevere(event.getMessage());
      }
    } else {
      closeEditor(event.getKeyCode(), event.hasModifiers());
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
        event.preventDefault();
        closeEditor(keyCode, EventUtils.hasModifierKey(nativeEvent));
        break;

      case KeyCodes.KEY_ENTER:
      case KeyCodes.KEY_TAB:
      case KeyCodes.KEY_UP:
      case KeyCodes.KEY_DOWN:
        event.preventDefault();
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
    Stacking.ensureLevel(zIndex);

    StyleUtils.unhideDisplay(editorElement);
    getEditor().setFocus(true);

    getEditor().setEditing(true);

    String oldValue = (row == null) ? null : getOldValueForUpdate(row);
    EditorAction onEntry =
        (getEditorDescription() == null) ? null : getEditorDescription().getOnEntry();

    getEditor().startEdit(oldValue, charCode, onEntry, sourceElement);
  }

  public void setNotificationListener(NotificationListener notificationListener) {
    this.notificationListener = notificationListener;
  }

  public Boolean validate(String oldValue, String newValue, IsRow row, boolean force) {
    CellValidation cellValidation = new CellValidation(oldValue, newValue, getValidation(),
        row, getIndexForUpdate(), getTypeForUpdate(), isNullable(), getMinValue(),
        getMaxValue(), getCaption(), getNotificationListener(), force);

    return ValidationHelper.validateCell(cellValidation, this);
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
        rowMode, hasRelation(), keyCode, hasModifiers, getEditor().getId()), this);
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
      Boolean ok = validate(oldValue, newValue, getRowValue(), false);

      if (BeeUtils.isEmpty(ok)) {
        if (ok == null) {
          closeEditor(keyCode, hasModifiers);
        }
        return false;
      }

      closeEditor(getColumnForUpdate(), oldValue, newValue, getRowModeForUpdate(), keyCode,
          hasModifiers);
      return true;
    }
    return false;
  }

  private void ensureEditor(HasWidgets editorContainer) {
    if (getEditor() != null) {
      if (editorContainer != null && getEditor().asWidget().getParent() != editorContainer) {
        BeeKeeper.getLog().debug(getCaption(), "editor moved");
        editorContainer.add(getEditor().asWidget());
      }
      return;
    }

    String format = null;
    if (getEditorDescription() != null) {
      setEditor(EditorFactory.getEditor(getEditorDescription(), getItemKey(), getDataType(),
          isNullable(), getRelation()));
      format = getEditorDescription().getFormat();

    } else if (getRelation() != null) {
      setEditor(new DataSelector(getRelation(), false));
      getEditor().setNullable(isNullable());

    } else if (!BeeUtils.isEmpty(getItemKey())) {
      BeeListBox listBox = new BeeListBox();
      listBox.setValueNumeric(ValueType.isNumeric(getDataType()));
      listBox.addCaptions(getItemKey());
      listBox.setNullable(isNullable());
      setEditor(listBox);

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

  private String getItemKey() {
    return itemKey;
  }

  private NotificationListener getNotificationListener() {
    return notificationListener;
  }

  private Relation getRelation() {
    return relation;
  }

  private IsRow getRowValue() {
    return rowValue;
  }

  private State getState() {
    return state;
  }

  private void initEditor() {
    if (getEditor() == null) {
      return;
    }
    getEditor().addKeyDownHandler(this);
    getEditor().addBlurHandler(this);
    getEditor().addEditStopHandler(this);

    if (getEditor() instanceof HasNumberBounds) {
      UiHelper.setNumberBounds((HasNumberBounds) getEditor(), getMinValue(), getMaxValue());
    }
  }

  private Boolean isRequired() {
    return required;
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
}
