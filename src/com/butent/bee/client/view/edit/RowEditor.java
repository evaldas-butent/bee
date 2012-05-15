package com.butent.bee.client.view.edit;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.HasActiveRow;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a data editing functionality for table rows.
 */

public class RowEditor extends FlexTable implements HasEditState, EditEndEvent.Handler,
    HasActiveRow {

  /**
   * Requires implementing classes to handle confirm and cancel situations.
   */

  public interface Callback {

    void onCancel();

    void onConfirm(IsRow row);
  }

  private class CancelCommand extends BeeCommand {
    @Override
    public void execute() {
      getCallback().onCancel();
    }
  }

  private class ConfirmCommand extends BeeCommand {
    @Override
    public void execute() {
      getCallback().onConfirm(getActiveRow());
    }
  }

  public static int defaultCellHeight = 25;
  public static int defaultCellWidth = 200;

  public static int maxCellHeight = 100;
  public static int minCellWidth = 25;

  private static final String STYLE_ROW_EDITOR = "bee-RowEditor";
  private static final String STYLE_CAPTION = "bee-RowEditorCaption";

  private static final String STYLE_LABEL = "bee-RowEditorLabel";
  private static final String STYLE_CELL = "bee-RowEditorCell";

  private static final String STYLE_ACTIVE_LABEL = "bee-RowEditorActiveLabel";
  private static final String STYLE_ACTIVE_CELL = "bee-RowEditorActiveCell";

  private static final String STYLE_CONFIRM = "bee-RowEditorConfirm";
  private static final String STYLE_CANCEL = "bee-RowEditorCancel";

  private final List<BeeColumn> dataColumns;

  private final List<EditableColumn> editableColumns;
  private final Callback callback;
  private final HasWidgets editorContainer;
  private final Element containerElement;

  private final Element editorBox;

  private IsRow row = null;

  private int startIndex = 0;
  private int activeIndex = BeeConst.UNDEF;
  private int editIndex = BeeConst.UNDEF;

  private boolean editing = false;

  public RowEditor(String caption, List<BeeColumn> dataColumns,
      List<EditableColumn> editableColumns, Callback callback,
      HasWidgets editorContainer, Element containerElement) {
    super();
    Assert.notEmpty(dataColumns);
    Assert.notEmpty(editableColumns);
    Assert.notNull(callback);
    Assert.notNull(editorContainer);
    Assert.notNull(containerElement);

    this.dataColumns = dataColumns;
    this.editableColumns = editableColumns;
    this.callback = callback;
    this.editorContainer = editorContainer;
    this.containerElement = containerElement;

    this.editorBox = Document.get().createDivElement();
    editorBox.getStyle().setPaddingLeft(0.5, Unit.EM);
    editorBox.getStyle().setPaddingRight(0.5, Unit.EM);

    create(caption);

    addStyleName(STYLE_ROW_EDITOR);
    sinkEvents(Event.ONCLICK + Event.ONKEYDOWN + Event.ONKEYPRESS);
  }

  public IsRow getActiveRow() {
    return row;
  }

  public boolean handleKeyboardNavigation(int keyCode, boolean hasModifiers) {
    if (getSize() <= 1) {
      return false;
    }
    int index = getActiveIndex();
    int size = getSize();

    switch (keyCode) {
      case KeyCodes.KEY_DOWN:
        index = (index < 0 || index >= size - 1) ? 0 : index + 1;
        break;

      case KeyCodes.KEY_UP:
        index = (index <= 0 || index >= size) ? size - 1 : index - 1;
        break;

      case KeyCodes.KEY_PAGEDOWN:
        index = size - 1;
        break;

      case KeyCodes.KEY_PAGEUP:
        index = 0;
        break;

      case KeyCodes.KEY_HOME:
        if (hasModifiers) {
          index = 0;
        }
        break;

      case KeyCodes.KEY_END:
        if (hasModifiers) {
          index = size - 1;
        }
        return true;

      case KeyCodes.KEY_LEFT:
      case KeyCodes.KEY_BACKSPACE:
        if (index > 0 && index <= size) {
          index--;
        }
        break;

      case KeyCodes.KEY_RIGHT:
      case KeyCodes.KEY_ENTER:
        if (index >= 0 && index < size - 1) {
          index++;
        }
        break;

      case KeyCodes.KEY_TAB:
        if (hasModifiers) {
          index = (index <= 0 || index >= size) ? size - 1 : index - 1;
        } else {
          index = (index < 0 || index >= size - 1) ? 0 : index + 1;
        }
        break;
    }

    if (index != getActiveIndex()) {
      activateCell(index);
      return true;
    } else {
      return false;
    }
  }

  public boolean isEditing() {
    return editing;
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if (isEditing()) {
      return;
    }

    String eventType = event.getType();
    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget)) {
      return;
    }
    Element target = Element.as(eventTarget);
    String col = DomUtils.getDataColumn(target);
    if (!BeeUtils.isDigit(col)) {
      return;
    }
    int index = BeeUtils.toInt(col);

    if (EventUtils.isKeyDown(eventType)) {
      if (!isCellActive(index)) {
        event.preventDefault();
        refocus();
        return;
      }

      int keyCode = event.getKeyCode();
      boolean hasModifiers = EventUtils.hasModifierKey(event);

      if (keyCode == KeyCodes.KEY_ESCAPE) {
        event.preventDefault();
        getCallback().onCancel();
      } else if (keyCode == KeyCodes.KEY_ENTER && hasModifiers) {
        event.preventDefault();
        getCallback().onConfirm(getActiveRow());
      } else if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DELETE) {
        event.preventDefault();
        startEdit(index, target, EditorFactory.getStartKey(keyCode));
      } else if (handleKeyboardNavigation(keyCode, hasModifiers)) {
        event.preventDefault();
      }

    } else if (EventUtils.isKeyPress(eventType)) {
      int charCode = event.getCharCode();
      event.preventDefault();
      startEdit(index, target, charCode);

    } else if (EventUtils.isClick(event)) {
      if (isCellActive(index)) {
        startEdit(index, target, EditorFactory.START_MOUSE_CLICK);
      } else {
        activateCell(index);
      }
    }
  }

  public void onEditEnd(EditEndEvent event, EditEndEvent.HasEditEndHandler source) {
    Assert.notNull(event);
    setEditing(false);
    refocus();

    if (!BeeUtils.equalsTrimRight(event.getOldValue(), event.getNewValue())) {
      updateCell(getEditIndex(), event.getColumn().getId(), event.getNewValue());
    }

    if (event.getKeyCode() != null) {
      int keyCode = BeeUtils.unbox(event.getKeyCode());
      if (BeeUtils.inList(keyCode, KeyCodes.KEY_TAB, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN,
          KeyCodes.KEY_ENTER)) {
        handleKeyboardNavigation(keyCode, event.hasModifiers());
      }
    }
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void start(IsRow rowValue) {
    Assert.notNull(rowValue);
    setRow(rowValue);
    renderRow();

    activateCell(0);
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        refocus();
      }
    });
  }

  private void activateCell(int index) {
    if (getActiveIndex() == index) {
      return;
    }

    Widget widget;
    if (isIndex(getActiveIndex())) {
      widget = getLabel(getActiveIndex());
      if (widget != null) {
        widget.removeStyleName(STYLE_ACTIVE_LABEL);
      }
      widget = getCell(getActiveIndex());
      if (widget != null) {
        widget.removeStyleName(STYLE_ACTIVE_CELL);
      }
    }

    setActiveIndex(index);

    if (isIndex(index)) {
      widget = getLabel(index);
      if (widget != null) {
        widget.addStyleName(STYLE_ACTIVE_LABEL);
      }
      widget = getCell(index);
      if (widget != null) {
        widget.addStyleName(STYLE_ACTIVE_CELL);
        widget.getElement().focus();
      }
    }
  }

  private void create(String caption) {
    BeeLabel label;
    int r = 0;

    if (!BeeUtils.isEmpty(caption)) {
      label = new BeeLabel(caption);
      label.addStyleName(STYLE_CAPTION);
      setWidget(r, 0, label);
      getFlexCellFormatter().setColSpan(r, 0, 2);
      r++;
    }
    setStartIndex(r);

    Html cell;
    for (int i = 0; i < getSize(); i++) {
      label = new BeeLabel(getEditableColumn(i).getCaption());
      label.addStyleName(STYLE_LABEL);

      if (!getEditableColumn(i).isNullable()) {
        label.addStyleName(StyleUtils.NAME_REQUIRED);
      }
      if (getEditableColumn(i).hasDefaults()) {
        label.addStyleName(StyleUtils.NAME_HAS_DEFAULTS);
      }

      setWidget(r, 0, label);
      getCellFormatter().setVerticalAlignment(r, 0, HasVerticalAlignment.ALIGN_TOP);

      cell = new Html();
      cell.setStyleName(STYLE_CELL);
      DomUtils.setDataColumn(cell, i);
      setCellSize(cell, i);
      if (ValueType.isNumeric(getEditableColumn(i).getDataType())
          && !getEditableColumn(i).hasRelation()) {
        StyleUtils.setTextAlign(cell, HasHorizontalAlignment.ALIGN_RIGHT);
      }
      cell.getElement().setTabIndex(0);

      setWidget(r, 1, cell);
      r++;
    }

    BeeImage confirm = new BeeImage(Global.getImages().ok(), new ConfirmCommand());
    BeeImage cancel = new BeeImage(Global.getImages().cancel(), new CancelCommand());
    confirm.addStyleName(STYLE_CONFIRM);
    cancel.addStyleName(STYLE_CANCEL);

    setWidget(r, 0, confirm);
    setWidget(r, 1, cancel);
    getCellFormatter().setHorizontalAlignment(r, 1, HasHorizontalAlignment.ALIGN_RIGHT);
  }

  private int getActiveIndex() {
    return activeIndex;
  }

  private Callback getCallback() {
    return callback;
  }

  private Widget getCell(int index) {
    if (isIndex(index)) {
      return getWidget(index + getStartIndex(), 1);
    } else {
      return null;
    }
  }

  private Element getContainerElement() {
    return containerElement;
  }

  private List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  private int getDataIndex(String columnId) {
    return DataUtils.getColumnIndex(columnId, getDataColumns());
  }

  private EditableColumn getEditableColumn(int index) {
    return getEditableColumns().get(index);
  }

  private List<EditableColumn> getEditableColumns() {
    return editableColumns;
  }

  private int getEditIndex() {
    return editIndex;
  }

  private Element getEditorBox() {
    return editorBox;
  }

  private HasWidgets getEditorContainer() {
    return editorContainer;
  }

  private Widget getLabel(int index) {
    if (isIndex(index)) {
      return getWidget(index + getStartIndex(), 0);
    } else {
      return null;
    }
  }

  private int getMaxCellWidth() {
    return BeeUtils.clamp(getContainerElement().getOffsetWidth() - 100, defaultCellWidth, 400);
  }

  private int getSize() {
    return getEditableColumns().size();
  }

  private int getStartIndex() {
    return startIndex;
  }

  private boolean isCellActive(int index) {
    return isIndex(index) && getActiveIndex() == index;
  }

  private boolean isIndex(int index) {
    return index >= 0 && index < getSize();
  }

  private void refocus() {
    if (isIndex(getActiveIndex())) {
      Widget cell = getCell(getActiveIndex());
      if (cell != null) {
        cell.getElement().focus();
      }
    }
  }

  private void renderCell(int index, String value) {
    Widget cell = getCell(index);

    if (BeeUtils.isEmpty(value)) {
      cell.getElement().setInnerHTML(BeeConst.STRING_EMPTY);
    } else {
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      getEditableColumn(index).getUiColumn().render(null, getActiveRow(), builder);
      cell.getElement().setInnerHTML(builder.toSafeHtml().asString());
    }
  }

  private void renderRow() {
    for (int i = 0; i < getSize(); i++) {
      renderCell(i, getActiveRow().getString(getEditableColumn(i).getColIndex()));
    }
  }

  private void setActiveIndex(int activeIndex) {
    this.activeIndex = activeIndex;
  }

  private void setCellSize(Widget cell, int index) {
    EditableColumn editableColumn = getEditableColumn(index);
    int precision = editableColumn.getDataColumn().getPrecision();
    int charWidth = Rulers.getIntPixels(1, Unit.EM);
    int maxCellWidth = getMaxCellWidth();

    int width = defaultCellWidth;
    int height = defaultCellHeight;

    switch (editableColumn.getDataType()) {
      case BOOLEAN:
        width = minCellWidth;
        break;

      case DATE:
        width = charWidth * 12;
        break;

      case DATETIME:
        width = defaultCellWidth;
        break;

      case INTEGER:
      case LONG:
      case NUMBER:
      case DECIMAL:
        width = defaultCellWidth;
        break;

      case TEXT:
      case TIMEOFDAY:
        if (precision > 200) {
          width = maxCellWidth;
          height = defaultCellHeight * 3;
        } else if (precision > 0) {
          width = (precision + 1) * charWidth;
        } else {
          width = defaultCellWidth;
        }
        break;
    }

    width = BeeUtils.clamp(width, minCellWidth, maxCellWidth);
    height = BeeUtils.clamp(height, defaultCellHeight, maxCellHeight);

    StyleUtils.setSize(cell, width, height);
  }

  private void setEditIndex(int editIndex) {
    this.editIndex = editIndex;
  }

  private void setRow(IsRow row) {
    this.row = row;
  }

  private void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  private void startEdit(int index, Element sourceElement, int charCode) {
    EditableColumn editableColumn = getEditableColumn(index);

    if (charCode == EditorFactory.START_KEY_DELETE) {
      if (!editableColumn.isNullable()) {
        return;
      }
      String oldValue = editableColumn.getOldValue(getActiveRow());
      if (BeeUtils.isEmpty(oldValue)) {
        return;
      }

      validateAndUpdate(editableColumn, index, oldValue, null, false);
      return;
    }

    if (ValueType.BOOLEAN.equals(editableColumn.getDataType())
        && BeeUtils.inList(charCode, EditorFactory.START_MOUSE_CLICK,
            EditorFactory.START_KEY_ENTER)) {

      String oldValue = editableColumn.getOldValue(getActiveRow());
      Boolean b = !BeeUtils.toBoolean(oldValue);
      if (!b && editableColumn.isNullable()) {
        b = null;
      }
      String newValue = BooleanValue.pack(b);

      validateAndUpdate(editableColumn, index, oldValue, newValue, true);
      return;
    }

    setEditing(true);
    setEditIndex(index);

    StyleUtils.copySize(sourceElement, getEditorBox());

    int left;
    int top;
    if (getContainerElement().isOrHasChild(sourceElement)) {
      left = DomUtils.getRelativeLeft(getContainerElement(), sourceElement);
      top = DomUtils.getRelativeTop(getContainerElement(), sourceElement);
    } else {
      left = sourceElement.getAbsoluteLeft() - getContainerElement().getAbsoluteLeft();
      top = sourceElement.getAbsoluteTop() - getContainerElement().getAbsoluteTop();
    }
    StyleUtils.setLeft(getEditorBox(), left);
    StyleUtils.setTop(getEditorBox(), top);

    sourceElement.blur();

    editableColumn.openEditor(getEditorContainer(), getEditorBox(), getContainerElement(),
        StyleUtils.getZIndex(this) + 1, getActiveRow(), BeeUtils.toChar(charCode), this);
  }

  private void updateCell(int index, String columnId, String value) {
    getActiveRow().setValue(getDataIndex(columnId), value);

    if (getEditableColumn(index).hasRelation()) {
      getEditableColumn(index).maybeUpdateRelation(getActiveRow(), false);
      renderRow();
    } else {
      renderCell(index, value);
    }
  }

  private boolean validateAndUpdate(EditableColumn editableColumn, int index, String oldValue,
      String newValue, boolean tab) {
    Boolean ok = editableColumn.validate(oldValue, newValue, getActiveRow());
    if (BeeUtils.isEmpty(ok)) {
      return false;
    }

    updateCell(index, editableColumn.getColumnId(), newValue);
    if (tab) {
      handleKeyboardNavigation(KeyCodes.KEY_TAB, false);
    }
    return true;
  }
}
