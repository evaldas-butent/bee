package com.butent.bee.client.view.edit;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasRelatedRow;
import com.butent.bee.client.data.RelationUtils;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.validation.CellValidationBus;
import com.butent.bee.client.validation.HasCellValidationHandlers;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.view.form.DisplayWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ActiveWidgetChangeEvent;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.RefreshType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class EditableWidget implements KeyDownHandler, ValueChangeHandler<String>, FocusHandler,
    BlurHandler, EditStopEvent.Handler, HasCellValidationHandlers, EditEndEvent.HasEditEndHandler {

  private final int dataIndex;
  private final BeeColumn dataColumn;

  private final WidgetDescription widgetDescription;
  private final DisplayWidget displayWidget;

  private final Evaluator editable;
  private final Evaluator validation;
  private final Evaluator carry;

  private final String minValue;
  private final String maxValue;

  private final boolean required;
  private final boolean readOnly;

  private final CellValidationBus cellValidationBus = new CellValidationBus();

  private EditEndEvent.Handler editEndHandler = null;
  private boolean initialized = false;

  private Editor editor = null;
  private FormView form = null;

  public EditableWidget(List<BeeColumn> dataColumns, int dataIndex,
      WidgetDescription widgetDescription, DisplayWidget displayWidget) {
    Assert.isIndex(dataColumns, dataIndex);
    Assert.notNull(widgetDescription);

    this.dataIndex = dataIndex;
    this.dataColumn = dataColumns.get(dataIndex);

    this.widgetDescription = widgetDescription;
    this.displayWidget = displayWidget;

    String source = this.dataColumn.getId();
    this.editable = Evaluator.create(widgetDescription.getEditable(), source, dataColumns);
    this.validation = Evaluator.create(widgetDescription.getValidation(), source, dataColumns);
    this.carry = Evaluator.create(widgetDescription.getCarry(), source, dataColumns);

    this.minValue = widgetDescription.getMinValue();
    this.maxValue = widgetDescription.getMaxValue();
    this.required = BeeUtils.isTrue(widgetDescription.getRequired());
    this.readOnly = BeeUtils.isTrue(widgetDescription.getReadOnly());
  }

  public HandlerRegistration addCellValidationHandler(CellValidateEvent.Handler handler) {
    return cellValidationBus.addCellValidationHandler(handler);
  }

  public void bind(Widget rootWidget, EditEndEvent.Handler handler, FormView formView) {
    if (isInitialized()) {
      return;
    }

    Assert.notNull(rootWidget);
    Assert.notNull(handler);

    setForm(formView);

    Widget widget = DomUtils.getChildQuietly(rootWidget, getWidgetId());
    if (widget instanceof Editor) {
      setEditor((Editor) widget);
      getEditor().setNullable(isNullable());

      if (formView != null) {
        getEditor().addFocusHandler(this);
        getEditor().addBlurHandler(this);
      }

      if (isFocusable()) {
        getEditor().addKeyDownHandler(this);
      } else {
        getEditor().addValueChangeHandler(this);
      }

      getEditor().addEditStopHandler(this);

      setEditEndHandler(handler);
      setInitialized(true);
    } else {
      BeeKeeper.getLog().warning("editable widget: no editor", getCaption(), getWidgetId());
    }
  }

  public boolean checkForUpdate(boolean reset) {
    return update(null, false, reset);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EditableWidget)) {
      return false;
    }
    return getWidgetDescription().equals(((EditableWidget) obj).getWidgetDescription());
  }

  public Boolean fireCellValidation(CellValidateEvent event) {
    return cellValidationBus.fireCellValidation(event);
  }

  public String getCaption() {
    return BeeUtils.ifString(getWidgetDescription().getCaption(), getDataColumn().getLabel());
  }

  public String getCarryValue(IsRow row) {
    if (row == null || getCarry() == null) {
      return null;
    }

    getCarry().update(row, BeeConst.UNDEF, getDataIndex(), getDataType(),
        row.getString(getDataIndex()));
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

  public BeeColumn getColumnForUpdate() {
    return getDataColumn();
  }

  public String getColumnId() {
    return getDataColumn().getId();
  }

  public BeeColumn getDataColumn() {
    return dataColumn;
  }

  public int getDataIndex() {
    return dataIndex;
  }

  public ValueType getDataType() {
    return getDataColumn().getType();
  }

  public Editor getEditor() {
    return editor;
  }

  public int getIndexForUpdate() {
    return getDataIndex();
  }

  public Relation getRelation() {
    return getWidgetDescription().getRelation();
  }

  public boolean getRowModeForUpdate() {
    if (getUpdateMode() == null) {
      return hasRelation();
    } else {
      return RefreshType.ROW.equals(getUpdateMode());
    }
  }

  public ValueType getTypeForUpdate() {
    return getColumnForUpdate().getType();
  }

  public String getWidgetId() {
    return getWidgetDescription().getWidgetId();
  }

  public boolean hasCarry() {
    return getCarry() != null;
  }

  @Override
  public int hashCode() {
    return getWidgetDescription().hashCode();
  }

  public boolean hasRelation() {
    return getRelation() != null;
  }

  public boolean isDisplay() {
    return getDisplayWidget() != null;
  }

  public boolean isEditable(IsRow row) {
    if (row == null) {
      return false;
    }
    if (getEditable() == null) {
      return true;
    }

    getEditable().update(row, BeeConst.UNDEF, getDataIndex(), getDataType(),
        row.getString(getDataIndex()));
    return BeeUtils.toBoolean(getEditable().evaluate());
  }

  public boolean isFocusable() {
    if (getWidgetDescription().getWidgetType() != null) {
      return getWidgetDescription().getWidgetType().isFocusable();
    }
    return false;
  }

  public boolean isNullable() {
    if (isRequired()) {
      return false;
    } else if (getDataColumn() != null) {
      return getDataColumn().isNullable();
    } else {
      return true;
    }
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean maybeUpdateRelation(String viewName, IsRow row, boolean updateColumn) {
    boolean ok = false;
    if (!BeeUtils.isEmpty(viewName) && row != null && getEditor() instanceof HasRelatedRow
        && getRelation() != null) {
      ok = RelationUtils.updateRow(viewName, getColumnId(), row, getRelation().getViewName(),
          ((HasRelatedRow) getEditor()).getRelatedRow(), updateColumn) > 0;
    }
    return ok;
  }

  public void onBlur(BlurEvent event) {
    getEditor().setEditing(false);
    getForm().onActiveWidgetChange(new ActiveWidgetChangeEvent(getWidgetId(), false));
  }

  public void onEditStop(EditStopEvent event) {
    if (event.isChanged()) {
      update(KeyCodes.KEY_TAB, false, true);

    } else if (event.isError()) {
      if (getForm() != null) {
        getForm().notifySevere(event.getMessage());
      }

    } else if (event.isCanceled()) {
      reset();

    } else if (event.isClosed()) {
      reset();
      end(event.getKeyCode(), event.hasModifiers());
    }
  }

  public void onFocus(FocusEvent event) {
    getEditor().setEditing(true);
    getForm().onActiveWidgetChange(new ActiveWidgetChangeEvent(getWidgetId(), true));
    
    if (event.getSource() instanceof HasTextBox) {
      TextBoxBase widget = ((HasTextBox) event.getSource()).getTextBox();
      String value = widget.getText();
      if (BeeUtils.isEmpty(value)) {
        return;
      }

      EditorAction action = getWidgetDescription().getOnFocus();
      if (action == null) {
        action = ((Editor) widget).getDefaultFocusAction();
        if (action == null) {
          return;
        }
      }

      switch (action) {
        case END:
          widget.setCursorPos(value.length());
          break;
        case HOME:
          widget.setCursorPos(0);
          break;
        case SELECT:
          UiHelper.selectDeferred(widget);
          break;
        default:
      }
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
        reset();
        break;

      case KeyCodes.KEY_ENTER:
      case KeyCodes.KEY_TAB:
      case KeyCodes.KEY_UP:
      case KeyCodes.KEY_DOWN:
        event.preventDefault();
        update(keyCode, EventUtils.hasModifierKey(nativeEvent), true);
        break;
    }
  }

  public void onValueChange(ValueChangeEvent<String> event) {
    update(null, false, true);
  }

  public void refresh(IsRow row) {
    if (getEditor() != null) {
      String value;
      if (row == null) {
        value = BeeConst.STRING_EMPTY;
      } else {
        value = BeeUtils.trimRight(row.getString(getDataIndex()));
      }
      getEditor().setValue(value);

      if (isDisplay()) {
        getDisplayWidget().refresh((Widget) getEditor(), row);
      }
    }
  }

  public boolean validate(boolean force) {
    String oldValue = getOldValueForUpdate();
    String newValue = getEditor().getNormalizedValue();

    if (!force && BeeUtils.equalsTrimRight(oldValue, newValue)) {
      return true;
    }
    return validate(oldValue, newValue, force);
  }

  private void end(Integer keyCode, boolean hasModifiers) {
    if (getEditEndHandler() != null) {
      getEditEndHandler().onEditEnd(new EditEndEvent(keyCode, hasModifiers, getWidgetId()), this);
    }
  }

  private Evaluator getCarry() {
    return carry;
  }

  private DisplayWidget getDisplayWidget() {
    return displayWidget;
  }

  private Evaluator getEditable() {
    return editable;
  }

  private EditEndEvent.Handler getEditEndHandler() {
    return editEndHandler;
  }

  private FormView getForm() {
    return form;
  }

  private String getMaxValue() {
    return maxValue;
  }

  private String getMinValue() {
    return minValue;
  }

  private String getOldValueForUpdate() {
    if (getRowValue() == null) {
      return null;
    }
    return getRowValue().getString(getIndexForUpdate());
  }

  private IsRow getRowValue() {
    if (getForm() == null) {
      return null;
    }
    return getForm().getActiveRow();
  }

  private RefreshType getUpdateMode() {
    return getWidgetDescription().getUpdateMode();
  }

  private Evaluator getValidation() {
    return validation;
  }

  private WidgetDescription getWidgetDescription() {
    return widgetDescription;
  }

  private boolean isInitialized() {
    return initialized;
  }

  private boolean isRequired() {
    return required;
  }

  private void reset() {
    refresh(getRowValue());
  }

  private void setEditEndHandler(EditEndEvent.Handler editEndHandler) {
    this.editEndHandler = editEndHandler;
  }

  private void setEditor(Editor editor) {
    this.editor = editor;
  }

  private void setForm(FormView form) {
    this.form = form;
  }

  private void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  private boolean update(Integer keyCode, boolean hasModifiers, boolean reset) {
    String oldValue = getOldValueForUpdate();
    String newValue = getEditor().getNormalizedValue();

    boolean eq = BeeUtils.equalsTrimRight(oldValue, newValue);
    if (eq && reset) {
      reset();
    }
    if (eq && keyCode == null) {
      return true;
    }

    if (!eq && !validate(oldValue, newValue, false)) {
      if (reset) {
        reset();
      }
      return false;
    }

    if (getEditEndHandler() != null) {
      getEditEndHandler().onEditEnd(new EditEndEvent(getRowValue(), getColumnForUpdate(),
          oldValue, newValue, getRowModeForUpdate(), hasRelation(), keyCode, hasModifiers,
          getWidgetId()), this);
    }
    return true;
  }

  private boolean validate(String oldValue, String newValue, boolean force) {
    CellValidation cellValidation = new CellValidation(oldValue, newValue, getValidation(),
        getRowValue(), getIndexForUpdate(), getTypeForUpdate(), isNullable(), getMinValue(),
        getMaxValue(), getCaption(), getForm(), force);

    return !BeeUtils.isEmpty(ValidationHelper.validateCell(cellValidation, this));
  }
}
