package com.butent.bee.client.view.form;

import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.ui.FormFactory.WidgetCallback;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.event.SelectionCountChangeEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class FormImpl extends Absolute implements FormView, EditEndEvent.Handler, HasDataTable {

  private class CreationCallback implements WidgetCallback {
    public void onFailure(String[] reason) {
      BeeKeeper.getLog().severe(ArrayUtils.join(reason, 1));
      BeeKeeper.getScreen().notifySevere(reason);
    }

    public void onSuccess(WidgetDescription result) {
      if (result == null) {
        onFailure(new String[] {"widget description is null"});
        return;
      }
      FormWidget type = result.getWidgetType();
      if (type == null) {
        onFailure(new String[] {"widget type is null"});
        return;
      }
      
      if (type.isDisplay()) {
        getDisplayWidgets().add(result);
      }

      if (type.isEditable()) {
        String source = result.getSource();
        int index = getDataIndex(source);
        
        if (index >= 0) {
          getEditableWidgets().add(new EditableWidget(getDataColumns(), index, null, result));
        } else {
          onFailure(new String[] {"source column not found", source, result.getWidgetId()});
        }
      }
    }
  }

  private Presenter viewPresenter = null;
  
  private Widget rootWidget = null;

  private Evaluator rowEditable = null;

  private final Notification notification = new Notification();

  private boolean enabled = true;
  
  private boolean hasData = false;
  private List<BeeColumn> dataColumns = null;
  
  private boolean editing = false;

  private int pageStart = 0;
  private int rowCount = BeeConst.UNDEF;
  
  private IsRow rowData = null;
  
  private boolean readOnly = false;
  
  private final CreationCallback creationCallback = new CreationCallback();
  
  private final Set<WidgetDescription> displayWidgets = Sets.newHashSet();
  private final Set<EditableWidget> editableWidgets = Sets.newHashSet();

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
    return addHandler(handler, LoadingStateChangeEvent.TYPE);
  }

  public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
    return addHandler(handler, RangeChangeEvent.getType());
  }

  public HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler) {
    return addHandler(handler, ReadyForInsertEvent.getType());
  }

  public HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler) {
    return addHandler(handler, ReadyForUpdateEvent.getType());
  }

  public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler) {
    return addHandler(handler, RowCountChangeEvent.getType());
  }

  public HandlerRegistration addSelectionCountChangeHandler(SelectionCountChangeEvent.Handler handler) {
    return null;
  }

  public HandlerRegistration addSortHandler(SortEvent.Handler handler) {
    return null;
  }

  public void applyOptions(String options) {
  }

  public void create(FormDescription formDescription, List<BeeColumn> dataCols) {
    Assert.notNull(formDescription);
    setDataColumns(dataCols);
    setHasData(!BeeUtils.isEmpty(dataCols));

    if (hasData()) {
      Calculation rec = formDescription.getRowEditable();
      if (rec != null) {
        setRowEditable(Evaluator.create(rec, null, dataCols));
      }
    }

    setReadOnly(formDescription.isReadOnly());
    
    Widget root = FormFactory.createForm(formDescription, getCreationCallback());
    if (root == null) {
      return;
    }

    StyleUtils.makeAbsolute(root);
    root.addStyleName("bee-Form");
    setRootWidget(root);

    add(root);
    add(getNotification());
  }

  public void finishNewRow(IsRow row) {
    fireEvent(new AddEndEvent());
    setEditing(false);

    if (row != null) {
      setRowData(row);
    }
  }

  public void fireLoadingStateChange(LoadingStateChangeEvent.LoadingState loadingState) {
    if (loadingState != null) {
      fireEvent(new LoadingStateChangeEvent(loadingState));
    }
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
    if (getRowData() == null || isReadOnly()) {
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
    setRowCount(count, isRowCountExact());
  }

  public void setRowCount(int count, boolean isExact) {
    Assert.nonNegative(count);
    if (count == getRowCount()) {
      return;
    }
    this.rowCount = count;

    if (getPageStart() >= count) {
      setPageStart(Math.max(count - 1, 0));
    }
    RowCountChangeEvent.fire(this, count, isExact);
  }

  public void setRowData(int start, List<? extends IsRow> values) {
    if (!BeeUtils.isEmpty(values)) {
      setRowData(values.get(0));
      refreshData();
    }  
  }
  
  public void setSelectionModel(SelectionModel<? super IsRow> selectionModel) {
  }
  
  public void setViewPresenter(Presenter presenter) {
    this.viewPresenter = presenter;
  }

  public void setVisibleRange(int start, int length) {
    setVisibleRow(start, false);
  }

  public void setVisibleRange(Range range) {
    setVisibleRow(range.getStart(), false);
  }

  public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
    setVisibleRow(range.getStart(), forceRangeChangeEvent);
  }

  public void start(int count) {
    if (hasData()) {
      setRowCount(count);
      if (count > 0) {
        RangeChangeEvent.fire(this, getVisibleRange());
      }
    }
  }

  public void startNewRow() {
    setEditing(true);
    fireEvent(new AddStartEvent());

    IsRow oldRow = null;
    IsRow newRow = createEmptyRow();

    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (!editableWidget.hasCarry()) {
        continue;
      }
      if (oldRow == null) {
        if (getRowData() != null) {
          oldRow = getRowData();
        } else {
          oldRow = createEmptyRow();
        }
      }

      String carry = editableWidget.getCarryValue(oldRow);
      if (!BeeUtils.isEmpty(carry)) {
        newRow.setValue(editableWidget.getDataIndex(), carry);
      }
    }
    
    setRowData(newRow);
    refreshData();
  }
  
  public void updateActiveRow(List<? extends IsRow> values) {
  }

  private IsRow createEmptyRow() {
    String[] arr = new String[getDataColumns().size()];
    return new BeeRow(0, arr);
  }
  
  private CreationCallback getCreationCallback() {
    return creationCallback;
  }

  private List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  private int getDataIndex(String source) {
    int index = BeeConst.UNDEF;
    if (BeeUtils.isEmpty(source) || getDataColumns() == null) {
      return index;
    }
    
    for (int i = 0; i < getDataColumns().size(); i++) {
      if (BeeUtils.same(source, getDataColumns().get(i).getId())) {
        index = i;
        break;
      }
    }
    return index;
  }

  private Set<WidgetDescription> getDisplayWidgets() {
    return displayWidgets;
  }

  private Set<EditableWidget> getEditableWidgets() {
    return editableWidgets;
  }

  private Notification getNotification() {
    return notification;
  }

  private int getPageStart() {
    return pageStart;
  }

  private Widget getRootWidget() {
    return rootWidget;
  }

  private IsRow getRowData() {
    return rowData;
  }

  private Evaluator getRowEditable() {
    return rowEditable;
  }

  private Widget getWidget(String id) {
    if (isAttached()) {
      return DomUtils.getWidget(this, id);
    } else {
      return null;
    }
  }

  private boolean hasData() {
    return hasData;
  }

  private boolean isReadOnly() {
    return readOnly;
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

  private void refreshData() {
    fireLoadingStateChange(LoadingStateChangeEvent.LoadingState.PARTIALLY_LOADED);
    
    for (EditableWidget editableWidget : getEditableWidgets()) {
      Widget widget = getWidget(editableWidget.getWidgetId());
      String value = getRowData().getString(editableWidget.getDataIndex());
      
      if (widget instanceof Editor) {
        ((Editor) widget).setValue(BeeUtils.trimRight(value));
      }
    }
    
    fireLoadingStateChange(LoadingStateChangeEvent.LoadingState.LOADED);
  }

  private void setDataColumns(List<BeeColumn> dataColumns) {
    this.dataColumns = dataColumns;
  }

  private void setHasData(boolean hasData) {
    this.hasData = hasData;
  }

  private void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  private void setRootWidget(Widget rootWidget) {
    this.rootWidget = rootWidget;
  }

  private void setRowData(IsRow rowData) {
    this.rowData = rowData;
  }

  private void setRowEditable(Evaluator rowEditable) {
    this.rowEditable = rowEditable;
  }

  private void setVisibleRow(int index, boolean forceRangeChangeEvent) {
    Assert.nonNegative(index);

    boolean changed = (index != getPageStart());
    if (changed) {
      setPageStart(index);
    }
    if (changed || forceRangeChangeEvent) {
      RangeChangeEvent.fire(this, getVisibleRange());
    }
  }

  private void showNote(Level level, String... messages) {
    StyleUtils.setZIndex(getNotification(), StyleUtils.getZIndex(getRootWidget()) + 1);
    getNotification().show(level, messages);
  }
  
  private void updateCell(IsRow rowValue, IsColumn dataColumn, String oldValue, String newValue,
      boolean rowMode) {
//    preliminaryUpdate(rowValue.getId(), dataColumn.getId(), newValue);
    fireEvent(new ReadyForUpdateEvent(rowValue, dataColumn, oldValue, newValue, rowMode));
  }
}
