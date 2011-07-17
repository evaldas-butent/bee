package com.butent.bee.client.view.form;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.RowEditor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.event.SelectionCountChangeEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class FormImpl extends Absolute implements FormView, EditEndEvent.Handler, HasDataTable {

  private class NewRowCallback implements RowEditor.Callback {
    public void onCancel() {
      finishNewRow(null);
    }

    public void onConfirm(IsRow row) {
      if (checkNewRow(row)) {
        prepareForInsert(row);
      }
    }
  }

  private Presenter viewPresenter = null;
  
  private Widget formWidget = null;

  private Evaluator rowEditable = null;
  private Evaluator rowValidation = null;

  private final Map<String, EditableColumn> editableColumns = Maps.newLinkedHashMap();

  private final Notification notification = new Notification();

  private final List<String> newRowColumns = Lists.newArrayList();

  private RowEditor newRowWidget = null;
  private final NewRowCallback newRowCallback = new NewRowCallback();

  private boolean enabled = true;

  private List<BeeColumn> dataColumns = null;
  private final Set<RelationInfo> relations = Sets.newHashSet();
  
  private boolean editing = false;

  private int pageStart = 0;
  private int rowCount = BeeConst.UNDEF;
  
  private IsRow rowData = null;
  
  private boolean readOnly = false;

  public FormImpl() {
    super();
  }

  public HandlerRegistration addActiveRowChangeHandler(ActiveRowChangeEvent.Handler handler) {
    return null;
  }

  public HandlerRegistration addAddEndHandler(AddEndEvent.Handler handler) {
    return addHandler(handler, AddEndEvent.getType());
  }

  public HandlerRegistration addAddStartHandler(AddStartEvent.Handler handler) {
    return addHandler(handler, AddStartEvent.getType());
  }

  public HandlerRegistration addCellPreviewHandler(CellPreviewEvent.Handler<IsRow> handler) {
    return null;
  }

  public HandlerRegistration addLoadingStateChangeHandler(LoadingStateChangeEvent.Handler handler) {
    return null;
  }

  public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
    return null;
  }

  public HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler) {
    return addHandler(handler, ReadyForInsertEvent.getType());
  }

  public HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler) {
    return addHandler(handler, ReadyForUpdateEvent.getType());
  }

  public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler) {
    return null;
  }

  public HandlerRegistration addSelectionCountChangeHandler(SelectionCountChangeEvent.Handler handler) {
    return null;
  }

  public HandlerRegistration addSortHandler(SortEvent.Handler handler) {
    return null;
  }

  public void applyOptions(String options) {
  }

  public void create(FormDescription formDescription, List<BeeColumn> dataCols, int rc,
      BeeRowSet rowSet) {
    Assert.notNull(formDescription);
    setDataColumns(dataCols);
    
    boolean hasView = !BeeUtils.isEmpty(dataCols);
    if (hasView) {
      Calculation rec = formDescription.getRowEditable();
      if (rec != null) {
        setRowEditable(Evaluator.create(rec, null, dataCols));
      }
      Calculation rvc = formDescription.getRowValidation();
      if (rvc != null) {
        setRowValidation(Evaluator.create(rvc, null, dataCols));
      }
      initNewRowColumns(formDescription.getNewRowColumns());
    }

    setRowCount(rc);
    setReadOnly(formDescription.isReadOnly());
    
    setFormWidget(FormFactory.createForm(formDescription));
    StyleUtils.makeAbsolute(getFormWidget());
    getFormWidget().addStyleName("bee-Form");

    add(getFormWidget());
    add(getNotification());
  }

  public void finishNewRow(IsRow row) {
    StyleUtils.hideDisplay(getNewRowWidget());
    fireEvent(new AddEndEvent());

    StyleUtils.hideScroll(this, ScrollBars.BOTH);
    StyleUtils.unhideDisplay(getFormWidget());
    setEditing(false);

    if (row != null) {
      setRowData(row);
    }
  }

  public void fireLoadingStateChange(LoadingState loadingState) {
  }

  public RowInfo getActiveRowInfo() {
    if (getRowData() == null) {
      return null;
    }
    return new RowInfo(getRowData());
  }

  public HasDataTable getDisplay() {
    return this;
  }

  public int getRowCount() {
    return rowCount;
  }

  public SelectionModel<? super IsRow> getSelectionModel() {
    return null;
  }

  public Order getSortOrder() {
    return null;
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public IsRow getVisibleItem(int indexOnPage) {
    return null;
  }

  public int getVisibleItemCount() {
    return 1;
  }

  public Iterable<IsRow> getVisibleItems() {
    return null;
  }

  public Range getVisibleRange() {
    return new Range(getPageStart(), 1);
  }

  public String getWidgetId() {
    return getId();
  }

  public boolean isColumnEditable(String columnId) {
    if (BeeUtils.isEmpty(columnId)) {
      return false;
    }
    return getEditableColumns().containsKey(columnId);
  }

  public boolean isEditing() {
    return editing;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isRowCountExact() {
    return true;
  }

  public boolean isRowEditable(boolean warn) {
    if (getRowData() == null) {
      return false;
    }
    return isRowEditable(getRowData(), warn);
  }

  public void notifyInfo(String... messages) {
    showNote(Level.INFO, messages);
  }

  public void notifySevere(String... messages) {
    showNote(Level.SEVERE, messages);
  }

  public void notifyWarning(String... messages) {
    showNote(Level.WARNING, messages);
  }

  public void onCellUpdate(CellUpdateEvent event) {
  }

  public void onEditEnd(EditEndEvent event) {
    Assert.notNull(event);
    setEditing(false);
//  refocus();

    if (!BeeUtils.equalsTrimRight(event.getOldValue(), event.getNewValue())) {
      updateCell(event.getRowValue(), event.getColumn(), event.getOldValue(), event.getNewValue(),
          event.isRowMode());
    }

    if (event.getKeyCode() != null) {
      int keyCode = BeeUtils.unbox(event.getKeyCode());
      if (BeeUtils.inList(keyCode, KeyCodes.KEY_TAB, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN)) {
//      handleKeyboardNavigation(keyCode, event.hasModifiers());
      }
    }
  }

  public void onMultiDelete(MultiDeleteEvent event) {
  }

  public void onRowDelete(RowDeleteEvent event) {
  }

  public void onRowUpdate(RowUpdateEvent event) {
  }

  public void refreshCellContent(String columnSource) {
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setPageSize(int pageSize) {
  }

  public void setPageStart(int pageStart) {
    this.pageStart = pageStart;
  }

  public void setRowCount(int count) {
    this.rowCount = count;
  }

  public void setRowCount(int count, boolean isExact) {
    setRowCount(count);
  }

  public void setRowData(int start, List<? extends IsRow> values) {
    setRowData(values.get(0));
  }

  public void setSelectionModel(SelectionModel<? super IsRow> selectionModel) {
  }

  public void setViewPresenter(Presenter presenter) {
    this.viewPresenter = presenter;
  }

  public void setVisibleRange(int start, int length) {
    setPageStart(start);
  }

  public void setVisibleRange(Range range) {
    setPageStart(range.getStart());
  }

  public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
  }

  public void startNewRow() {
    if (getNewRowColumns().isEmpty()) {
      return;
    }

    setEditing(true);
    StyleUtils.hideDisplay(getFormWidget());
    StyleUtils.autoScroll(this, ScrollBars.BOTH);

    fireEvent(new AddStartEvent());

    if (getNewRowWidget() == null) {
      List<EditableColumn> columns = Lists.newArrayList();
      for (String columnId : getNewRowColumns()) {
        columns.add(getEditableColumn(columnId));
      }

      setNewRowWidget(new RowEditor(getDataColumns(), getRelations(), columns,
          getNewRowCallback(), this, getElement(), this));
      add(getNewRowWidget());
    }

    IsRow oldRow = null;
    IsRow newRow = createEmptyRow();

    for (EditableColumn editableColumn : getEditableColumns().values()) {
      if (!editableColumn.hasCarry()) {
        continue;
      }
      if (oldRow == null) {
        if (getRowData() != null) {
          oldRow = getRowData();
        } else {
          oldRow = createEmptyRow();
        }
      }

      String carry = editableColumn.getCarryValue(oldRow);
      if (!BeeUtils.isEmpty(carry)) {
        newRow.setValue(editableColumn.getColIndex(), carry);
      }
    }

    getNewRowWidget().start(newRow);
    StyleUtils.unhideDisplay(getNewRowWidget());
  }

  public void updateActiveRow(List<? extends IsRow> values) {
  }

  private boolean checkNewRow(IsRow row) {
    boolean ok = true;
    int count = 0;

    for (String columnId : getNewRowColumns()) {
      EditableColumn editableColumn = getEditableColumn(columnId);
      String value = row.getString(editableColumn.getColIndex());
      if (BeeUtils.isEmpty(value)) {
        if (!editableColumn.isNullable()) {
//          notifySevere(getColumnCaption(columnId), "Value required");
          ok = false;
        }
      } else {
        count++;
      }
    }

    if (ok && count <= 0) {
      notifySevere("New Row", "all columns cannot be empty");
      ok = false;
    }
    if (ok && getRowValidation() != null) {
      getRowValidation().update(row);
      String message = getRowValidation().evaluate();
      if (!BeeUtils.isEmpty(message)) {
        notifySevere(message);
        ok = false;
      }
    }
    return ok;
  }

  private IsRow createEmptyRow() {
    String[] arr = new String[getDataColumns().size()];
    return new BeeRow(0, arr);
  }

  private List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  private EditableColumn getEditableColumn(String columnId) {
    if (BeeUtils.isEmpty(columnId)) {
      return null;
    }
    return getEditableColumns().get(columnId);
  }

  private Map<String, EditableColumn> getEditableColumns() {
    return editableColumns;
  }

  private Widget getFormWidget() {
    return formWidget;
  }

  private NewRowCallback getNewRowCallback() {
    return newRowCallback;
  }

  private List<String> getNewRowColumns() {
    return newRowColumns;
  }

  private RowEditor getNewRowWidget() {
    return newRowWidget;
  }

  private Notification getNotification() {
    return notification;
  }

  private int getPageStart() {
    return pageStart;
  }

  private Set<RelationInfo> getRelations() {
    return relations;
  }

  private IsRow getRowData() {
    return rowData;
  }

  private Evaluator getRowEditable() {
    return rowEditable;
  }

  private Evaluator getRowValidation() {
    return rowValidation;
  }

  private void initNewRowColumns(String columnNames) {
    getNewRowColumns().clear();
    if (isReadOnly() || getEditableColumns().isEmpty()) {
      return;
    }

    if (!BeeUtils.isEmpty(columnNames)) {
      Splitter splitter = Splitter.on(CharMatcher.anyOf(" ,;")).trimResults().omitEmptyStrings();
      for (String colName : splitter.split(columnNames)) {
        if (BeeUtils.isEmpty(colName)) {
          continue;
        }

        String id = null;
        if (getEditableColumns().containsKey(colName)) {
          id = colName;
        } else {
          for (String columnId : getEditableColumns().keySet()) {
            if (BeeUtils.same(columnId, colName)) {
              id = columnId;
              break;
            }
          }
        }
        if (BeeUtils.isEmpty(id)) {
          BeeKeeper.getLog().warning("newRowColumn", colName, "is not editable");
          continue;
        }

        if (!BeeUtils.containsSame(getNewRowColumns(), id)) {
          getNewRowColumns().add(id);
        }
      }
    }

    if (getNewRowColumns().isEmpty()) {
      for (Map.Entry<String, EditableColumn> entry : getEditableColumns().entrySet()) {
        String id = entry.getKey();
//        if (!entry.getValue().isNullable() || !isColumnReadOnly(id)) {
          getNewRowColumns().add(id);
//        }
      }
    }
  }

  private boolean isReadOnly() {
    return readOnly;
  }

  private boolean isRelated(String columnId) {
    if (BeeUtils.isEmpty(columnId) || BeeUtils.isEmpty(getRelations())) {
      return false;
    }

    for (RelationInfo relationInfo : getRelations()) {
      if (BeeUtils.same(relationInfo.getSource(), columnId)) {
        return true;
      }
    }
    return false;
  }

  private boolean isRowEditable(IsRow rowValue, boolean warn) {
    if (rowValue == null) {
      return false;
    }
    if (getRowEditable() == null) {
      return true;
    }
    getRowEditable().update(rowValue);
    boolean ok = BeeUtils.toBoolean(getRowEditable().evaluate());

    if (!ok && warn) {
      notifyWarning("Row is read only:", getRowEditable().transform());
    }
    return ok;
  }

  private void prepareForInsert(IsRow row) {
    List<BeeColumn> columns = Lists.newArrayList();
    List<String> values = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      String value = row.getString(i);
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      if (!isRelated(getDataColumns().get(i).getId())) {
        columns.add(getDataColumns().get(i));
        values.add(value);
      }
    }

    Assert.notEmpty(columns);
    fireEvent(new ReadyForInsertEvent(columns, values));
  }

  private void setDataColumns(List<BeeColumn> dataColumns) {
    this.dataColumns = dataColumns;
  }

  private void setFormWidget(Widget formWidget) {
    this.formWidget = formWidget;
  }

  private void setNewRowWidget(RowEditor newRowWidget) {
    this.newRowWidget = newRowWidget;
  }

  private void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  private void setRowData(IsRow rowData) {
    this.rowData = rowData;
  }

  private void setRowEditable(Evaluator rowEditable) {
    this.rowEditable = rowEditable;
  }

  private void setRowValidation(Evaluator rowValidation) {
    this.rowValidation = rowValidation;
  }

  private void showNote(Level level, String... messages) {
    StyleUtils.setZIndex(getNotification(), StyleUtils.getZIndex(getFormWidget()) + 1);
    getNotification().show(level, messages);
  }

  private void startEdit(String columnId, int charCode, Element sourceElement) {
    EditableColumn editableColumn = getEditableColumn(columnId);
    if (editableColumn == null) {
      return;
    }

    IsRow rowValue = getRowData();
    if (!isRowEditable(rowValue, true)) {
      return;
    }
    if (!editableColumn.isCellEditable(rowValue, true)) {
      return;
    }

    if (charCode == EditorFactory.START_KEY_DELETE) {
      if (!editableColumn.isNullable()) {
        return;
      }

      String oldValue = editableColumn.getOldValueForUpdate(rowValue);
      if (BeeUtils.isEmpty(oldValue)) {
        return;
      }

      updateCell(rowValue, editableColumn.getColumnForUpdate(), oldValue, null,
          editableColumn.getRowModeForUpdate());
      return;
    }

    if (ValueType.BOOLEAN.equals(editableColumn.getDataType())
        && BeeUtils.inList(charCode, EditorFactory.START_MOUSE_CLICK,
            EditorFactory.START_KEY_ENTER) && editableColumn.getRelationInfo() == null) {

      String oldValue = rowValue.getString(editableColumn.getColIndex());
      Boolean b = !BeeUtils.toBoolean(oldValue);
      if (!b && editableColumn.isNullable()) {
        b = null;
      }
      String newValue = BooleanValue.pack(b);

      updateCell(rowValue, editableColumn.getDataColumn(), oldValue, newValue, false);
      return;
    }

    setEditing(true);
    if (sourceElement != null) {
      sourceElement.blur();
    }

    editableColumn.openEditor(this, sourceElement, getFormWidget().getElement(),
        StyleUtils.getZIndex(getFormWidget()) + 1, rowValue, BeeUtils.toChar(charCode), this);
  }

  private void updateCell(IsRow rowValue, IsColumn dataColumn, String oldValue, String newValue,
      boolean rowMode) {
//    preliminaryUpdate(rowValue.getId(), dataColumn.getId(), newValue);
    fireEvent(new ReadyForUpdateEvent(rowValue, dataColumn, oldValue, newValue, rowMode));
  }
}
