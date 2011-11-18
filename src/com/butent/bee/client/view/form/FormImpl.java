package com.butent.bee.client.view.form;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetCallback;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.utils.EvalHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataRequestEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.event.ScopeChangeEvent;
import com.butent.bee.shared.data.event.SelectionCountChangeEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Handles such form events like warnings, deletions, visibility of elements etc.
 */

public class FormImpl extends Absolute implements FormView, EditEndEvent.Handler {

  private class CreationCallback implements WidgetCallback {
    public void onFailure(String[] reason) {
      BeeKeeper.getLog().severe(ArrayUtils.join(reason, 1));
    }

    public void onSuccess(WidgetDescription result) {
      if (result == null) {
        onFailure(new String[] {"widget description is null"});
        return;
      }

      String id = result.getWidgetId();
      FormWidget type = result.getWidgetType();
      if (type == null) {
        onFailure(new String[] {"widget type is null", id});
        return;
      }

      if (type.isDisplay()) {
        String source = result.getSource();
        int index = BeeConst.UNDEF;
        ValueType valueType = null;
        Evaluator evaluator = null;

        boolean ok = true;

        if (!BeeUtils.isEmpty(source) && hasData()) {
          index = getDataIndex(source);
          if (index >= 0) {
            valueType = getDataColumns().get(index).getType();
          } else {
            onFailure(new String[] {"display source not found", source, id});
            ok = false;
          }
        }

        if (ok) {
          Calculation calc = result.getCalculation();
          if (calc != null) {
            evaluator = Evaluator.create(calc, source, getDataColumns());
          }

          ok = (index >= 0 || evaluator != null);
          if (ok) {
            getDisplayWidgets().add(new DisplayWidget(index, valueType, evaluator, result));
          }
        }
      }

      if (type.isEditable() && hasData() && !BeeUtils.isEmpty(result.getSource())) {
        String source = result.getSource();
        int index = getDataIndex(source);

        if (index >= 0) {
          RelationInfo relationInfo = null;
          if (type.isSelector()) {
            relationInfo = RelationInfo.create(getDataColumns(), source, result.getRelSource(),
                result.getRelView(), result.getRelColumn());
          }
          getEditableWidgets().add(new EditableWidget(getDataColumns(), index,
              relationInfo, result));
        } else {
          onFailure(new String[] {"editable source not found", source, id});
        }
      }

      if (type.isChild() && hasData()) {
        getChildWidgets().add(result);
      }
    }
  }

  private class DisplayWidget {
    private final int dataIndex;
    private final ValueType valueType;

    private final Evaluator evaluator;

    private final WidgetDescription widgetDescription;

    private DisplayWidget(int dataIndex, ValueType valueType, Evaluator evaluator,
        WidgetDescription widgetDescription) {
      this.dataIndex = dataIndex;
      this.valueType = valueType;
      this.evaluator = evaluator;
      this.widgetDescription = widgetDescription;
    }

    private int getDataIndex() {
      return dataIndex;
    }

    private Evaluator getEvaluator() {
      return evaluator;
    }

    private String getValue(IsRow rowValue) {
      if (getEvaluator() != null) {
        if (rowValue != null && getDataIndex() >= 0 && getValueType() != null) {
          getEvaluator().update(rowValue, getPageStart(), getDataIndex(), getValueType(),
              rowValue.getString(getDataIndex()));
        } else {
          getEvaluator().update(rowValue);
        }
        return getEvaluator().evaluate();
      } else if (rowValue != null) {
        return rowValue.getString(getDataIndex());
      } else {
        return BeeConst.STRING_EMPTY;
      }
    }

    private ValueType getValueType() {
      return valueType;
    }

    private WidgetDescription getWidgetDescription() {
      return widgetDescription;
    }

    private String getWidgetId() {
      return getWidgetDescription().getWidgetId();
    }

    private FormWidget getWidgetType() {
      return getWidgetDescription().getWidgetType();
    }
  }

  private class TabEntry implements Comparable<TabEntry> {
    private final int tabIndex;
    private final int order;
    private final String widgetId;

    private TabEntry(int tabIndex, int order, String widgetId) {
      this.tabIndex = tabIndex;
      this.order = order;
      this.widgetId = widgetId;
    }

    public int compareTo(TabEntry o) {
      Assert.notNull(o);

      int res = Integer.valueOf(getTabIndex()).compareTo(o.getTabIndex());
      if (res == BeeConst.COMPARE_EQUAL) {
        res = Integer.valueOf(getOrder()).compareTo(o.getOrder());
      }
      return res;
    }

    private int getOrder() {
      return order;
    }

    private int getTabIndex() {
      return tabIndex;
    }

    private String getWidgetId() {
      return widgetId;
    }
  }

  private static final String NEW_ROW_CAPTION = "Create New";
  
  private Presenter viewPresenter = null;

  private Widget rootWidget = null;

  private Evaluator rowEditable = null;

  private final Notification notification = new Notification();

  private boolean enabled = true;

  private boolean hasData = false;
  private List<BeeColumn> dataColumns = null;

  private boolean editing = false;
  private boolean adding = false;

  private int pageStart = 0;
  private int rowCount = BeeConst.UNDEF;

  private IsRow row = null;
  private IsRow rowBuffer = null;
  private JavaScriptObject rowJso = null;

  private boolean readOnly = false;
  
  private String caption = null;

  private FormCallback formCallback = null;

  private final CreationCallback creationCallback = new CreationCallback();

  private final Set<DisplayWidget> displayWidgets = Sets.newHashSet();
  private final List<EditableWidget> editableWidgets = Lists.newArrayList();
  private final Set<WidgetDescription> childWidgets = Sets.newHashSet();

  private final List<TabEntry> tabOrder = Lists.newArrayList();

  public FormImpl() {
    super();
  }

  public HandlerRegistration addActiveRowChangeHandler(ActiveRowChangeEvent.Handler handler) {
    return addHandler(handler, ActiveRowChangeEvent.getType());
  }

  public HandlerRegistration addAddEndHandler(AddEndEvent.Handler handler) {
    return addHandler(handler, AddEndEvent.getType());
  }

  public HandlerRegistration addAddStartHandler(AddStartEvent.Handler handler) {
    return addHandler(handler, AddStartEvent.getType());
  }

  public HandlerRegistration addDataRequestHandler(DataRequestEvent.Handler handler) {
    return addHandler(handler, DataRequestEvent.getType());
  }

  public HandlerRegistration addEditFormHandler(EditFormEvent.Handler handler) {
    return addHandler(handler, EditFormEvent.getType());
  }
  
  public HandlerRegistration addLoadingStateChangeHandler(LoadingStateChangeEvent.Handler handler) {
    return addHandler(handler, LoadingStateChangeEvent.TYPE);
  }

  public HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler) {
    return addHandler(handler, ReadyForInsertEvent.getType());
  }

  public HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler) {
    return addHandler(handler, ReadyForUpdateEvent.getType());
  }

  public HandlerRegistration addScopeChangeHandler(ScopeChangeEvent.Handler handler) {
    return addHandler(handler, ScopeChangeEvent.getType());
  }

  public HandlerRegistration addSelectionCountChangeHandler(
      SelectionCountChangeEvent.Handler handler) {
    return null;
  }

  public HandlerRegistration addSortHandler(SortEvent.Handler handler) {
    return null;
  }

  public void applyOptions(String options) {
  }

  public void create(FormDescription formDescription, List<BeeColumn> dataCols,
      FormCallback callback) {
    Assert.notNull(formDescription);
    setDataColumns(dataCols);
    setHasData(!BeeUtils.isEmpty(dataCols));
    setFormCallback(callback);

    if (hasData()) {
      Calculation rec = formDescription.getRowEditable();
      if (rec != null) {
        setRowEditable(Evaluator.create(rec, null, dataCols));
      }
    }

    setReadOnly(formDescription.isReadOnly());
    setCaption(formDescription.getCaption());

    Widget root = FormFactory.createForm(formDescription, dataCols, getCreationCallback(),
        callback);
    if (root == null) {
      return;
    }

    StyleUtils.makeAbsolute(root);
    root.addStyleName("bee-Form");
    setRootWidget(root);

    add(root);
    add(getNotification());
  }

  public void finishNewRow(IsRow rowValue) {
    fireEvent(new AddEndEvent());

    if (rowValue != null) {
      setRow(rowValue);
    } else {
      setRow(getRowBuffer());
    }

    refreshData(true, true);
    showChildren(true);

    if (rowValue != null) {
      int rc = getRowCount();
      setPageStart(rc, false, false);
      setRowCount(rc + 1, false);
      fireScopeChange();
    }

    setAdding(false);
  }

  public void fireLoadingStateChange(LoadingStateChangeEvent.LoadingState loadingState) {
    if (loadingState != null) {
      fireEvent(new LoadingStateChangeEvent(loadingState));
    }
  }

  public RowInfo getActiveRowInfo() {
    if (getRow() == null) {
      return null;
    }
    return new RowInfo(getRow());
  }

  public String getCaption() {
    return caption;
  }

  public List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  public int getDataIndex(String source) {
    return DataUtils.getColumnIndex(source, getDataColumns());
  }

  public HasDataTable getDisplay() {
    return this;
  }

  public FormCallback getFormCallback() {
    return formCallback;
  }

  public int getPageSize() {
    return 1;
  }

  public int getPageStart() {
    return pageStart;
  }

  public IsRow getRow() {
    return row;
  }

  public int getRowCount() {
    return rowCount;
  }

  public List<? extends IsRow> getRowData() {
    List<IsRow> data = Lists.newArrayList();
    if (getRow() != null) {
      data.add(getRow());
    }
    return data;
  }

  public JavaScriptObject getRowJso() {
    if (!hasData() || getRow() == null) {
      return null;
    }

    if (rowJso == null) {
      setRowJso(EvalHelper.createJso(getDataColumns()));
    }
    EvalHelper.toJso(getDataColumns(), getRow(), rowJso);

    return rowJso;
  }

  public Order getSortOrder() {
    return null;
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public Widget getWidgetBySource(String source) {
    Assert.notEmpty(source);
    EditableWidget editableWidget = getEditableWidget(source);
    if (editableWidget == null) {
      return null;
    } else {
      return getWidget(editableWidget.getWidgetId());
    }
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

  public boolean isRowEditable(boolean warn) {
    if (getRow() == null || isReadOnly() || !isEnabled()) {
      return false;
    }
    return isRowEditable(getRow(), warn);
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
    Assert.notNull(event);
    long version = event.getVersion();
    String source = event.getColumnName();
    String value = event.getValue();

    IsRow rowValue = getRow();
    rowValue.setVersion(version);
    int dataIndex = getDataIndex(source);
    rowValue.setValue(dataIndex, value);

    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (!BeeUtils.same(source, editableWidget.getColumnId())) {
        continue;
      }

      Editor editor = editableWidget.getEditor();
      if (editor != null && !BeeUtils.equalsTrimRight(value, editor.getNormalizedValue())) {
        editableWidget.setValue(rowValue);
      }
    }

    if (!isReadOnly() && isEnabled()) {
      boolean rowEnabled = isRowEditable(rowValue, false);

      for (EditableWidget editableWidget : getEditableWidgets()) {
        if (editableWidget.isReadOnly()) {
          continue;
        }
        Editor editor = editableWidget.getEditor();
        if (editor == null) {
          continue;
        }

        boolean editable = rowEnabled && editableWidget.isEditable(rowValue);
        if (editable != editor.isEnabled()) {
          editor.setEnabled(editable);
        }
      }

      if (getRowEditable() != null && hasChildren()) {
        for (WidgetDescription widgetDescription : getChildWidgets()) {
          Widget widget = getWidget(widgetDescription.getWidgetId());
          if (widget instanceof HasEnabled && rowEnabled != ((HasEnabled) widget).isEnabled()) {
            ((HasEnabled) widget).setEnabled(rowEnabled);
          }
        }
      }
    }

    refreshDisplayWidgets();
  }

  public void onEditEnd(EditEndEvent event) {
    Assert.notNull(event);

    IsRow rowValue = getRow();
    IsColumn column = event.getColumn();

    int index = getDataIndex(column.getId());
    String oldValue = rowValue.getString(index);
    String newValue = event.getNewValue();

    if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
      if (isAdding() || isEditing()) {
        rowValue.setValue(index, newValue);
        BeeKeeper.getLog().info(column.getId(), index, "old:", oldValue, "new:", newValue);
      } else {
        fireEvent(new ReadyForUpdateEvent(rowValue, column, oldValue, newValue, event.isRowMode()));
      }
    }

    Integer keyCode = event.getKeyCode();
    String widgetId = event.getWidgetId();

    if (keyCode != null && !BeeUtils.isEmpty(widgetId) && getTabOrder().size() > 1) {
      switch (BeeUtils.unbox(keyCode)) {
        case KeyCodes.KEY_ENTER:
        case KeyCodes.KEY_DOWN:
          navigate(widgetId, true);
          break;

        case KeyCodes.KEY_TAB:
          navigate(widgetId, !event.hasModifiers());
          break;

        case KeyCodes.KEY_UP:
          navigate(widgetId, false);
          break;
      }
    }
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    setRowCount(getRowCount() - event.getRows().size(), true);
  }

  public void onRowDelete(RowDeleteEvent event) {
    setRowCount(getRowCount() - 1, true);
  }

  public void onRowUpdate(RowUpdateEvent event) {
    Assert.notNull(event);
    IsRow newRow = event.getRow();
    Assert.notNull(newRow);

    setRow(newRow);
    refreshData(false, false);
  }

  public void prepareForInsert() {
    if (getFormCallback() != null && !getFormCallback().onPrepareForInsert(this, this, getRow())) {
      return;
    }
    if (!checkNewRow(getRow())) {
      return;
    }

    List<BeeColumn> columns = Lists.newArrayList();
    List<String> values = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      String value = getRow().getString(i);
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      if (!isForeign(getDataColumns().get(i).getId())) {
        columns.add(getDataColumns().get(i));
        values.add(value);
      }
    }

    Assert.notEmpty(columns);
    fireEvent(new ReadyForInsertEvent(columns, values));
  }

  public void refresh(boolean refreshChildren) {
    refreshData(refreshChildren, getRow() != null);
  }

  public void refreshCellContent(String columnSource) {
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    for (EditableWidget editableWidget : getEditableWidgets()) {
      Editor editor = editableWidget.getEditor();
      if (editor != null && editor.isEnabled() != enabled) {
        editor.setEnabled(enabled);
      }
    }

    for (WidgetDescription widgetDescription : getChildWidgets()) {
      Widget widget = getWidget(widgetDescription.getWidgetId());
      if (widget instanceof HasEnabled && enabled != ((HasEnabled) widget).isEnabled()) {
        ((HasEnabled) widget).setEnabled(enabled);
      }
    }
  }

  public void setPageSize(int size, boolean fireScopeChange, boolean fireDataRequest) {
  }

  public void setPageStart(int start, boolean fireScopeChange, boolean fireDataRequest) {
    Assert.nonNegative(start);
    if (start == getPageStart()) {
      return;
    }
    
    this.pageStart = start;
    
    if (fireScopeChange) {
      fireScopeChange();
    }
    if (fireDataRequest) {
      fireDataRequest();
    }
  }

  public void setRow(IsRow row) {
    this.row = row;
  }

  public void setRowCount(int count, boolean fireScopeChange) {
    Assert.nonNegative(count);
    if (count == getRowCount()) {
      return;
    }

    this.rowCount = count;

    if (getPageStart() >= count) {
      setPageStart(Math.max(count - 1, 0), true, true);
    } else if (fireScopeChange) {
      fireScopeChange();
    }
  }

  public void setRowData(List<? extends IsRow> values, boolean refresh) {
    if (BeeUtils.isEmpty(values)) {
      setRow(null);
    } else {
      setRow(values.get(0));
    }
    
    if (refresh) {
      refresh(true);
    }
  }

  public void setViewPresenter(Presenter presenter) {
    this.viewPresenter = presenter;
  }

  public void showChildren(boolean show) {
    for (WidgetDescription widgetDescription : getChildWidgets()) {
      getWidget(widgetDescription.getWidgetId()).setVisible(show);
    }
  }

  public void start(Integer count) {
    if (hasData()) {
      for (EditableWidget editableWidget : getEditableWidgets()) {
        editableWidget.bind(this, this, this);

        if (editableWidget.isFocusable() && editableWidget.getEditor() != null) {
          int tabIndex = editableWidget.getEditor().getTabIndex();
          if (tabIndex >= 0) {
            getTabOrder().add(new TabEntry(tabIndex, getTabOrder().size(),
                editableWidget.getWidgetId()));
          }
        }
      }

      if (getTabOrder().size() > 1) {
        Collections.sort(getTabOrder());
      }

      if (count != null) {
        setRowCount(count, true);
        if (count > 0) {
          fireDataRequest();
        } else {
          setRow(null);
        }
      }
    }
  }

  public void startNewRow() {
    setAdding(true);
    fireEvent(new AddStartEvent(NEW_ROW_CAPTION));

    IsRow oldRow = getRow();
    setRowBuffer(oldRow);
    if (oldRow == null) {
      oldRow = createEmptyRow();
    }
    IsRow newRow = createEmptyRow();

    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (!editableWidget.hasCarry()) {
        continue;
      }

      String carry = editableWidget.getCarryValue(oldRow);
      if (!BeeUtils.isEmpty(carry)) {
        newRow.setValue(editableWidget.getDataIndex(), carry);
      }
    }

    if (getFormCallback() != null) {
      getFormCallback().onStartNewRow(this, oldRow, newRow);
    }

    setRow(newRow);
    refreshData(false, true);
    showChildren(false);
  }

  public void updateActiveRow(List<? extends IsRow> values) {
  }

  public void updateCell(String columnId, String newValue) {
    Assert.notEmpty(columnId);

    IsRow rowValue = getRow();
    if (rowValue == null) {
      notifySevere("update cell:", columnId, newValue, "form has no data");
      return;
    }

    int index = getDataIndex(columnId);
    if (BeeConst.isUndef(index)) {
      notifySevere("update cell:", columnId, newValue, "column not found");
      return;
    }

    String oldValue = rowValue.getString(index);

    if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
      if (isAdding() || isEditing()) {
        rowValue.setValue(index, newValue);
        refreshEditableWidget(index);
        refreshDisplayWidgets();
      } else {
        fireEvent(new ReadyForUpdateEvent(rowValue, getDataColumns().get(index),
            oldValue, newValue, isForeign(columnId)));
      }
    }
  }

  public void updateRow(IsRow rowValue, boolean refreshChildren) {
    setRow(rowValue);
    render(refreshChildren);
  }

  private boolean checkNewRow(IsRow rowValue) {
    boolean ok = true;
    int count = 0;

    if (getEditableWidgets().isEmpty()) {
      notifySevere("New Row", "columns not available");
      ok = false;
    }

    if (ok) {
      List<String> captions = Lists.newArrayList();
      for (EditableWidget editableWidget : getEditableWidgets()) {
        String value = rowValue.getString(editableWidget.getIndexForUpdate());
        if (BeeUtils.isEmpty(value)) {
          if (!editableWidget.isNullable()) {
            captions.add(editableWidget.getCaption());
            ok = false;
          }
        } else {
          count++;
        }
      }
      if (!ok) {
        notifySevere(BeeUtils.transformCollection(captions), "Value required");
      }
    }

    if (ok && count <= 0) {
      notifySevere("New Row", "all columns cannot be empty");
      ok = false;
    }
    return ok;
  }

  private IsRow createEmptyRow() {
    String[] arr = new String[getDataColumns().size()];
    return new BeeRow(0, arr);
  }
  
  private void fireDataRequest() {
    fireEvent(new DataRequestEvent());
  }

  private void fireScopeChange() {
    fireEvent(new ScopeChangeEvent(getPageStart(), getPageSize(), getRowCount()));
  }

  private void focus(int index, boolean forward, boolean cycle) {
    if (!BeeUtils.isIndex(getTabOrder(), index)) {
      return;
    }
    if (!isRowEditable(false)) {
      return;
    }

    Widget widget = getWidget(getTabOrder().get(index).getWidgetId());
    boolean ok = widget instanceof Focusable;
    if (ok && widget instanceof HasEnabled) {
      ok = ((HasEnabled) widget).isEnabled();
    }
    if (ok) {
      ((Focusable) widget).setFocus(true);
      return;
    }

    int size = getTabOrder().size();
    if (size <= 1) {
      return;
    }

    int i;
    boolean md;

    if (forward) {
      if (index < size - 1) {
        i = index + 1;
        md = true;
      } else {
        i = 0;
        md = false;
      }
    } else {
      if (index > 0) {
        i = index - 1;
        md = true;
      } else {
        i = size - 1;
        md = false;
      }
    }

    if (cycle || md) {
      focus(i, forward, cycle && md);
    }
  }

  private Set<WidgetDescription> getChildWidgets() {
    return childWidgets;
  }

  private CreationCallback getCreationCallback() {
    return creationCallback;
  }
  
  private Set<DisplayWidget> getDisplayWidgets() {
    return displayWidgets;
  }

  private EditableWidget getEditableWidget(String columnId) {
    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (BeeUtils.same(columnId, editableWidget.getColumnId())) {
        return editableWidget;
      }
    }
    return null;
  }

  private List<EditableWidget> getEditableWidgets() {
    return editableWidgets;
  }

  private Notification getNotification() {
    return notification;
  }

  private Widget getRootWidget() {
    return rootWidget;
  }

  private IsRow getRowBuffer() {
    return rowBuffer;
  }

  private Evaluator getRowEditable() {
    return rowEditable;
  }

  private List<TabEntry> getTabOrder() {
    return tabOrder;
  }

  private Widget getWidget(String id) {
    if (isAttached()) {
      return DomUtils.getWidgetQuietly(this, id);
    } else {
      return null;
    }
  }

  private boolean hasChildren() {
    return !getChildWidgets().isEmpty();
  }

  private boolean hasData() {
    return hasData;
  }

  private boolean isAdding() {
    return adding;
  }

  private boolean isForeign(String columnId) {
    if (BeeUtils.isEmpty(columnId)) {
      return false;
    }
    EditableWidget editableWidget = getEditableWidget(columnId);
    if (editableWidget == null) {
      return false;
    } else {
      return editableWidget.isForeign();
    }
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

  private void navigate(String widgetId, boolean forward) {
    if (BeeUtils.isEmpty(widgetId)) {
      return;
    }
    int cnt = getTabOrder().size();
    if (cnt <= 1) {
      return;
    }

    int idx = BeeConst.UNDEF;
    for (int i = 0; i < cnt; i++) {
      if (BeeUtils.same(getTabOrder().get(i).getWidgetId(), widgetId)) {
        idx = i;
        break;
      }
    }
    if (BeeConst.isUndef(idx)) {
      return;
    }

    boolean cycle;
    if (forward) {
      if (idx < cnt - 1) {
        idx++;
        cycle = true;
      } else {
        idx = 0;
        cycle = false;
      }
    } else {
      if (idx > 0) {
        idx--;
        cycle = true;
      } else {
        idx = cnt - 1;
        cycle = false;
      }
    }

    focus(idx, forward, cycle);
  }

  private void refreshChildWidgets(IsRow rowValue) {
    if (!hasChildren()) {
      return;
    }
    boolean rowEnabled = !isReadOnly() && isEnabled() && isRowEditable(rowValue, false);

    for (WidgetDescription widgetDescription : getChildWidgets()) {
      Widget widget = getWidget(widgetDescription.getWidgetId());
      if (widget instanceof ChildGrid) {
        ((ChildGrid) widget).refresh(rowValue, rowEnabled);
      }
    }
  }

  private void refreshData(boolean refreshChildren, boolean focus) {
    fireLoadingStateChange(LoadingStateChangeEvent.LoadingState.PARTIALLY_LOADED);
    render(refreshChildren);
    fireLoadingStateChange(LoadingStateChangeEvent.LoadingState.LOADED);

    if (focus) {
      focus(0, true, false);
    }
  }

  private void refreshDisplayWidgets() {
    for (DisplayWidget displayWidget : getDisplayWidgets()) {
      displayWidget.getWidgetType().updateDisplay(this, displayWidget.getWidgetId(),
          displayWidget.getValue(getRow()));
    }
  }

  private void refreshEditableWidget(int dataIndex) {
    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (editableWidget.getIndexForUpdate() == dataIndex) {
        editableWidget.setValue(getRow());
      }
    }
  }

  private void refreshEditableWidgets() {
    boolean rowEnabled = isRowEditable(false);

    for (EditableWidget editableWidget : getEditableWidgets()) {
      Editor editor = editableWidget.getEditor();
      if (editor == null) {
        continue;
      }

      boolean editable;

      if (getRow() == null) {
        editable = false;
      } else {
        editable = rowEnabled && !editableWidget.isReadOnly();
        if (editable) {
          editable = editableWidget.isEditable(getRow());
        }
      }

      editableWidget.setValue(getRow());
      if (editable != editor.isEnabled()) {
        editor.setEnabled(editable);
      }
    }
  }

  private void render(boolean refreshChildren) {
    if (getFormCallback() != null) {
      getFormCallback().beforeRefresh(this, getRow());
    }

    refreshEditableWidgets();
    refreshDisplayWidgets();

    if (refreshChildren) {
      refreshChildWidgets(getRow());
    }
  
    if (getFormCallback() != null) {
      getFormCallback().afterRefresh(this, getRow());
    }
  }
  
  private void setAdding(boolean adding) {
    this.adding = adding;
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setDataColumns(List<BeeColumn> dataColumns) {
    this.dataColumns = dataColumns;
  }

  private void setFormCallback(FormCallback formCallback) {
    this.formCallback = formCallback;
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

  private void setRowBuffer(IsRow rowBuffer) {
    this.rowBuffer = rowBuffer;
  }

  private void setRowEditable(Evaluator rowEditable) {
    this.rowEditable = rowEditable;
  }

  private void setRowJso(JavaScriptObject rowJso) {
    this.rowJso = rowJso;
  }

  private void showNote(Level level, String... messages) {
    StyleUtils.setZIndex(getNotification(), StyleUtils.getZIndex(getRootWidget()) + 1);
    getNotification().show(level, messages);
  }
}
