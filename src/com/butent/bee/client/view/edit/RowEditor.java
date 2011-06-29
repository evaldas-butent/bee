package com.butent.bee.client.view.edit;

import com.google.common.collect.Lists;
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
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.NotificationListener;
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
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

public class RowEditor extends FlexTable implements HasEditState, EditEndEvent.Handler {

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
      getCallback().onConfirm(getRow());
    }
  }

  public static int defaultCellHeight = 25;
  public static int defaultCellWidth = 200;

  public static int maxCellHeight = 100;
  public static int minCellWidth = 25;
  
  private static final String STYLE_ROW_EDITOR = "bee-RowEditor";
  private static final String STYLE_LABEL = "bee-RowEditorLabel";

  private static final String STYLE_CELL = "bee-RowEditorCell";
  private static final String STYLE_ACTIVE_LABEL = "bee-RowEditorActiveLabel";

  private static final String STYLE_ACTIVE_CELL = "bee-RowEditorActiveCell";
  private static final String STYLE_CONFIRM = "bee-RowEditorConfirm";
  private static final String STYLE_CANCEL = "bee-RowEditorCancel";

  private final List<BeeColumn> dataColumns;
  private final Set<RelationInfo> relations;

  private final List<EditableColumn> editableColumns;
  private final Callback callback;
  private final HasWidgets editorContainer;
  private final Element containerElement;

  private final NotificationListener notificationListener;

  private final Element editorBox;

  private IsRow row = null;

  private int activeIndex = BeeConst.UNDEF;

  private boolean editing = false;
  
  public RowEditor(List<BeeColumn> dataColumns, Set<RelationInfo> relations,
      List<EditableColumn> editableColumns, Callback callback,
      HasWidgets editorContainer, Element containerElement,
      NotificationListener notificationListener) {
    super();
    Assert.notEmpty(dataColumns);
    Assert.notEmpty(editableColumns);
    Assert.notNull(callback);
    Assert.notNull(editorContainer);
    Assert.notNull(containerElement);

    this.dataColumns = dataColumns;
    this.relations = relations;
    this.editableColumns = editableColumns;
    this.callback = callback;
    this.editorContainer = editorContainer;
    this.containerElement = containerElement;
    this.notificationListener = notificationListener;

    this.editorBox = Document.get().createDivElement();
    editorBox.getStyle().setPaddingLeft(0.5, Unit.EM);
    editorBox.getStyle().setPaddingRight(0.5, Unit.EM);

    create();

    addStyleName(STYLE_ROW_EDITOR);
    sinkEvents(Event.ONCLICK + Event.ONKEYDOWN + Event.ONKEYPRESS);
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
        EventUtils.eatEvent(event);
        refocus();
        return;
      }

      int keyCode = event.getKeyCode();
      boolean hasModifiers = EventUtils.hasModifierKey(event);

      if (keyCode == KeyCodes.KEY_ESCAPE) {
        EventUtils.eatEvent(event);
        getCallback().onCancel();
      } else if (keyCode == KeyCodes.KEY_ENTER && hasModifiers) {
        EventUtils.eatEvent(event);
        getCallback().onConfirm(getRow());
      } else if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_DELETE) {
        EventUtils.eatEvent(event);
        startEdit(index, target, EditorFactory.getStartKey(keyCode));
      } else if (handleKeyboardNavigation(keyCode, hasModifiers)) {
        EventUtils.eatEvent(event);
      }

    } else if (EventUtils.isKeyPress(eventType)) {
      int charCode = event.getCharCode();
      EventUtils.eatEvent(event);
      startEdit(index, target, charCode);

    } else if (EventUtils.isClick(event)) {
      if (isCellActive(index)) {
        startEdit(index, target, EditorFactory.START_MOUSE_CLICK);
      } else {
        activateCell(index);
      }
    }
  }

  public void onEditEnd(EditEndEvent event) {
    Assert.notNull(event);
    setEditing(false);
    refocus();

    if (!BeeUtils.equalsTrimRight(event.getOldValue(), event.getNewValue())) {
      updateCell(event.getColumn().getId(), event.getNewValue());
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

  private void create() {
    BeeLabel label;
    Html cell;

    for (int i = 0; i < getSize(); i++) {
      label = new BeeLabel(getEditableColumn(i).getCaption());
      label.addStyleName(STYLE_LABEL);
      setWidget(i, 0, label);
      getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_TOP);

      cell = new Html();
      cell.setStyleName(STYLE_CELL);
      DomUtils.setDataColumn(cell, i);
      setCellSize(cell, i);
      if (ValueType.isNumeric(getEditableColumn(i).getDataType())) {
        StyleUtils.setTextAlign(cell, HasHorizontalAlignment.ALIGN_RIGHT);
      }
      cell.getElement().setTabIndex(0);
      setWidget(i, 1, cell);
    }

    BeeImage confirm = new BeeImage(Global.getImages().ok(), new ConfirmCommand());
    confirm.addStyleName(STYLE_CONFIRM);
    BeeImage cancel = new BeeImage(Global.getImages().cancel(), new CancelCommand());
    cancel.addStyleName(STYLE_CANCEL);

    int r = getSize();
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
      return getWidget(index, 1);
    } else {
      return null;
    }
  }

  private int getCellIndex(String columnId) {
    for (int i = 0; i < getSize(); i++) {
      if (BeeUtils.same(getEditableColumn(i).getColumnId(), columnId)) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private Element getContainerElement() {
    return containerElement;
  }

  private List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  private int getDataIndex(String columnId) {
    for (int i = 0; i < getDataColumns().size(); i++) {
      if (BeeUtils.same(getDataColumns().get(i).getId(), columnId)) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private EditableColumn getEditableColumn(int index) {
    return getEditableColumns().get(index);
  }

  private List<EditableColumn> getEditableColumns() {
    return editableColumns;
  }

  private Element getEditorBox() {
    return editorBox;
  }

  private HasWidgets getEditorContainer() {
    return editorContainer;
  }

  private Widget getLabel(int index) {
    if (isIndex(index)) {
      return getWidget(index, 0);
    } else {
      return null;
    }
  }

  private int getMaxCellWidth() {
    return BeeUtils.limit(getContainerElement().getOffsetWidth() - 100, defaultCellWidth, 400);
  }

  private NotificationListener getNotificationListener() {
    return notificationListener;
  }

  private Set<RelationInfo> getRelations() {
    return relations;
  }

  private IsRow getRow() {
    return row;
  }

  private int getSize() {
    return getEditableColumns().size();
  }

  private boolean isCellActive(int index) {
    return isIndex(index) && getActiveIndex() == index;
  }

  private boolean isIndex(int index) {
    return index >= 0 && index < getSize();
  }

  private boolean isRelSource(String columnId) {
    if (BeeUtils.isEmpty(columnId) || BeeUtils.isEmpty(getRelations())) {
      return false;
    }
    for (RelationInfo relationInfo : getRelations()) {
      if (BeeUtils.same(relationInfo.getRelSource(), columnId)) {
        return true;
      }
    }
    return false;
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
      getEditableColumn(index).getUiColumn().render(null, getRow(), builder);
      cell.getElement().setInnerHTML(builder.toSafeHtml().asString());
    }
  }

  private void renderRow() {
    for (int i = 0; i < getSize(); i++) {
      renderCell(i, getRow().getString(getEditableColumn(i).getColIndex()));
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
        if (precision > 100) {
          width = maxCellWidth;
          height = defaultCellHeight * 3;
        } else if (precision > 0) {
          width = (precision + 1) * charWidth;
        } else {
          width = defaultCellWidth;
        }
        break;
    }

    width = BeeUtils.limit(width, minCellWidth, maxCellWidth);
    height = BeeUtils.limit(height, defaultCellHeight, maxCellHeight);

    StyleUtils.setSize(cell, width, height);
  }

  private void setRow(IsRow row) {
    this.row = row;
  }
  
  private void startEdit(int index, Element sourceElement, int charCode) {
    EditableColumn editableColumn = getEditableColumn(index);

    if (charCode == EditorFactory.START_KEY_DELETE) {
      if (!editableColumn.isNullable()) {
        return;
      }
      String oldValue = editableColumn.getOldValueForUpdate(getRow());
      if (BeeUtils.isEmpty(oldValue)) {
        return;
      }

      updateCell(editableColumn.getColumnForUpdate().getId(), null);
      return;
    }

    if (ValueType.BOOLEAN.equals(editableColumn.getDataType())
        && BeeUtils.inList(charCode, EditorFactory.START_MOUSE_CLICK, EditorFactory.START_KEY_ENTER)
        && editableColumn.getRelationInfo() == null) {

      String oldValue = getRow().getString(editableColumn.getColIndex());
      Boolean b = !BeeUtils.toBoolean(oldValue);
      if (!b && editableColumn.isNullable()) {
        b = null;
      }
      String newValue = BooleanValue.pack(b);

      updateCell(editableColumn.getColumnId(), newValue);
      return;
    }

    setEditing(true);

    StyleUtils.copySize(sourceElement, getEditorBox());
    StyleUtils.setLeft(getEditorBox(),
        DomUtils.getRelativeLeft(getContainerElement(), sourceElement));
    StyleUtils.setTop(getEditorBox(), 
        DomUtils.getRelativeTop(getContainerElement(), sourceElement));
    
    sourceElement.blur();

    editableColumn.openEditor(getEditorContainer(), getEditorBox(), getContainerElement(),
        StyleUtils.getZIndex(this) + 1, getRow(), BeeUtils.toChar(charCode), this);
  }

  private void updateCell(String columnId, String value) {
    int index = getDataIndex(columnId);
    getRow().setValue(index, value);
    
    index = getCellIndex(columnId);
    if (isIndex(index)) {
      renderCell(index, value);
    }
    
    if (isRelSource(columnId)) {
      if (BeeUtils.isEmpty(value)) {
        for (RelationInfo relationInfo : getRelations()) {
          if (BeeUtils.same(relationInfo.getRelSource(), columnId)) {
            String source = relationInfo.getSource();
            index = getDataIndex(source);
            getRow().clearCell(index);

            index = getCellIndex(source);
            if (isIndex(index)) {
              renderCell(index, null);
            }
          }
        }

      } else {
        String viewName = null;
        final List<String> viewColumns = Lists.newArrayList();
        
        for (RelationInfo relationInfo : getRelations()) {
          if (BeeUtils.same(relationInfo.getRelSource(), columnId)) {
            if (viewName == null) {
              viewName = relationInfo.getRelView();
            } else if (!BeeUtils.same(viewName, relationInfo.getRelView())) {
              continue;
            }
            if (!BeeUtils.containsSame(viewColumns, relationInfo.getRelColumn())) {
              viewColumns.add(relationInfo.getRelColumn());
            }
          }
        }
        
        Assert.notEmpty(viewColumns, "related columns not available");
        Queries.getRow(viewName, BeeUtils.toLong(value), viewColumns, new Queries.RowCallback() {
          public void onFailure(String[] reason) {
            if (getNotificationListener() != null) {
              getNotificationListener().notifySevere(reason);
            }
          }
          
          public void onSuccess(BeeRow viewRow) {
            for (int viewIndex = 0; viewIndex < viewColumns.size(); viewIndex++) {
              for (RelationInfo relationInfo : getRelations()) {
                if (BeeUtils.same(relationInfo.getRelColumn(), viewColumns.get(viewIndex))) {
                  String sourceId = relationInfo.getSource();
                  String viewValue = viewRow.getString(viewIndex);
                  getRow().setValue(getDataIndex(sourceId), viewValue);

                  int cellIndex = getCellIndex(sourceId);
                  if (isIndex(cellIndex)) {
                    renderCell(cellIndex, viewValue);
                  }
                }
              }
            }
          }
        });
      }
    }
  }
}
