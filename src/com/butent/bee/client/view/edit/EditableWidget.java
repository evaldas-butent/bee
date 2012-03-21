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
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ActiveWidgetChangeEvent;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;

public class EditableWidget implements KeyDownHandler, ValueChangeHandler<String>, FocusHandler,
    BlurHandler, EditStopEvent.Handler {

  private final int dataIndex;
  private final BeeColumn dataColumn;
  private final RelationInfo relationInfo;

  private final WidgetDescription widgetDescription;

  private final Evaluator editable;
  private final Evaluator validation;
  private final Evaluator carry;

  private final String minValue;
  private final String maxValue;

  private final boolean required;
  private final boolean readOnly;

  private EditEndEvent.Handler editEndHandler = null;
  private boolean initialized = false;

  private Editor editor = null;
  private FormView form = null;

  public EditableWidget(List<BeeColumn> dataColumns, int dataIndex, RelationInfo relationInfo,
      WidgetDescription widgetDescription) {
    Assert.isIndex(dataColumns, dataIndex);
    Assert.notNull(widgetDescription);

    this.dataIndex = dataIndex;
    this.dataColumn = dataColumns.get(dataIndex);
    this.relationInfo = relationInfo;
    this.widgetDescription = widgetDescription;

    String source = this.dataColumn.getId();
    this.editable = Evaluator.create(widgetDescription.getEditable(), source, dataColumns);
    this.validation = Evaluator.create(widgetDescription.getValidation(), source, dataColumns);
    this.carry = Evaluator.create(widgetDescription.getCarry(), source, dataColumns);

    this.minValue = widgetDescription.getMinValue();
    this.maxValue = widgetDescription.getMaxValue();
    this.required = BeeUtils.isTrue(widgetDescription.isRequired());
    this.readOnly = BeeUtils.isTrue(widgetDescription.isReadOnly());
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
    boolean ok = update(null, false);
    if (!ok && reset) {
      reset();
    }
    return ok;
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

  public String getCaption() {
    return BeeUtils.ifString(getWidgetDescription().getCaption(),
        getDataColumn().getLabel());
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

  public BeeColumn getColumnForUpdate() {
    return isForeign() ? getRelationInfo().getDataColumn() : getDataColumn();
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
    return isForeign() ? getRelationInfo().getDataIndex() : getDataIndex();
  }

  public RelationInfo getRelationInfo() {
    return relationInfo;
  }

  public boolean getRowModeForUpdate() {
    return isForeign();
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

  public boolean isForeign() {
    return getRelationInfo() != null;
  }

  public boolean isNullable() {
    if (isRequired()) {
      return false;
    } else if (isForeign()) {
      return getRelationInfo().isNullable();
    } else if (getDataColumn() != null) {
      return getDataColumn().isNullable();
    } else {
      return true;
    }
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void onBlur(BlurEvent event) {
    getEditor().setEditing(false);
    getForm().onActiveWidgetChange(new ActiveWidgetChangeEvent(getWidgetId(), false));
  }

  public void onEditStop(EditStopEvent event) {
    if (event.isFinished()) {
      update(KeyCodes.KEY_TAB, false);
    } else if (event.isError()) {
      if (getForm() != null) {
        getForm().notifySevere(event.getMessage());
      }
    } else if (event.isCanceled()) {
      reset();
    }
  }
  
  public void onFocus(FocusEvent event) {
    getEditor().setEditing(true);
    getForm().onActiveWidgetChange(new ActiveWidgetChangeEvent(getWidgetId(), true));
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
        update(keyCode, EventUtils.hasModifierKey(nativeEvent));
        break;
    }
  }

  public void onValueChange(ValueChangeEvent<String> event) {
    if (!update(null, false)) {
      reset();
    }
  }

  public void setValue(IsRow row) {
    if (getEditor() != null) {
      String value;
      if (row == null) {
        value = BeeConst.STRING_EMPTY;
      } else {
        value = BeeUtils.trimRight(row.getString(getDataIndex()));
      }

      getEditor().setValue(value);

      if (isForeign()) {
        if (row == null) {
          value = null;
        } else {
          value = row.getString(getRelationInfo().getDataIndex());
        }

        if (getEditor() instanceof DataSelector) {
          ((DataSelector) getEditor()).setSelectedValue(value);
        }
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

  private Evaluator getCarry() {
    return carry;
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
    return getForm().getRow();
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
    setValue(getRowValue());
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

  private boolean update(Integer keyCode, boolean hasModifiers) {
    String oldValue = getOldValueForUpdate();
    String newValue = getEditor().getNormalizedValue();

    boolean eq = BeeUtils.equalsTrimRight(oldValue, newValue);
    if (eq && keyCode == null) {
      return true;
    }
    if (!eq && !validate(oldValue, newValue, false)) {
      return false;
    }

    if (getEditEndHandler() != null) {
      getEditEndHandler().onEditEnd(new EditEndEvent(getRowValue(), getColumnForUpdate(),
          oldValue, newValue, getRowModeForUpdate(), keyCode, hasModifiers, getWidgetId()));
    }
    return true;
  }

  private boolean validate(String oldValue, String newValue, boolean force) {
    return UiHelper.validateCell(oldValue, newValue, getValidation(), getRowValue(),
        getIndexForUpdate(), getTypeForUpdate(), isNullable(), getMinValue(), getMaxValue(),
        getCaption(), getForm(), force);
  }
}
