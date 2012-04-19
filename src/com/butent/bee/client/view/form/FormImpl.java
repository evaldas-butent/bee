package com.butent.bee.client.view.form;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.WidgetCreationCallback;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.utils.EvalHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.validation.CellValidateEvent.Handler;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.view.ActionEvent;
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
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.data.event.ActiveWidgetChangeEvent;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataRequestEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.ParentRowEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.event.ScopeChangeEvent;
import com.butent.bee.shared.data.event.SelectionCountChangeEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Handles such form events like warnings, deletions, visibility of elements etc.
 */

public class FormImpl extends Absolute implements FormView, EditEndEvent.Handler,
    NativePreviewHandler {

  private class CreationCallback extends WidgetCreationCallback {
    
    private CreationCallback() {
      super();
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

      if (result.isDisablable() && !BeeUtils.isEmpty(id)) {
        getDisablableWidgets().add(id);
      }

      if (type.isDisplay()) {
        String source = result.getSource();
        int index = BeeConst.UNDEF;

        boolean ok = true;
        if (!BeeUtils.isEmpty(source) && hasData()) {
          index = getDataIndex(source);
          if (index < 0) {
            onFailure(new String[] {"display source not found", source, id});
            ok = false;
          }
        }

        if (ok) {
          AbstractCellRenderer renderer =
              RendererFactory.getRenderer(result.getRendererDescription(), result.getRender(),
                  result.getItemKey(), getDataColumns(), index);
          ok = (index >= 0 || renderer != null);
          if (ok) {
            getDisplayWidgets().add(new DisplayWidget(index, renderer, result));
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
      
      super.onSuccess(result);
    }
  }

  private class DisplayWidget {
    private final int dataIndex;
    private final AbstractCellRenderer renderer;
    private final WidgetDescription widgetDescription;

    private DisplayWidget(int dataIndex, AbstractCellRenderer renderer,
        WidgetDescription widgetDescription) {
      this.dataIndex = dataIndex;
      this.renderer = renderer;
      this.widgetDescription = widgetDescription;
    }

    private String getValue(IsRow rowValue) {
      if (renderer != null) {
        return renderer.render(rowValue);
      } else if (rowValue != null) {
        return rowValue.getString(dataIndex);
      } else {
        return BeeConst.STRING_EMPTY;
      }
    }

    private String getWidgetId() {
      return widgetDescription.getWidgetId();
    }

    private FormWidget getWidgetType() {
      return widgetDescription.getWidgetType();
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

  private static final String STYLE_FORM = "bee-Form";
  private static final String STYLE_DISABLED = "bee-Form-disabled";

  private static final String NEW_ROW_CAPTION = "Create New";

  private final String formName;

  private Presenter viewPresenter = null;

  private Widget rootWidget = null;

  private Evaluator rowEditable = null;
  private Evaluator rowValidation = null;

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
  private final List<String> disablableWidgets = Lists.newArrayList();

  private final Set<DisplayWidget> displayWidgets = Sets.newHashSet();
  private final List<EditableWidget> editableWidgets = Lists.newArrayList();

  private final List<TabEntry> tabOrder = Lists.newArrayList();

  private HandlerRegistration previewReg = null;
  private String previewId = null;

  private int activeEditableIndex = BeeConst.UNDEF;

  private Dimensions dimensions = null;

  public FormImpl(String formName) {
    this(formName, Position.RELATIVE);
  }

  public FormImpl(String formName, Position position) {
    super(position, Overflow.AUTO);
    this.formName = formName;
  }

  public HandlerRegistration addActionHandler(ActionEvent.Handler handler) {
    return addHandler(handler, ActionEvent.getType());
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

  public HandlerRegistration addCellValidationHandler(String columnId, Handler handler) {
    EditableWidget editableWidget = getEditableWidgetByColumn(columnId, true);
    if (editableWidget == null) {
      return null;
    } else {
      return editableWidget.addCellValidationHandler(handler);
    }
  }

  public HandlerRegistration addDataRequestHandler(DataRequestEvent.Handler handler) {
    return addHandler(handler, DataRequestEvent.getType());
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
      FormCallback callback, boolean addStyle) {
    Assert.notNull(formDescription);
    setDataColumns(dataCols);
    setHasData(!BeeUtils.isEmpty(dataCols));
    setFormCallback(callback);

    if (hasData()) {
      Calculation calc = formDescription.getRowEditable();
      if (calc != null) {
        setRowEditable(Evaluator.create(calc, null, dataCols));
      }

      calc = formDescription.getRowValidation();
      if (calc != null) {
        setRowValidation(Evaluator.create(calc, null, dataCols));
      }
    }

    setReadOnly(formDescription.isReadOnly());
    setCaption(formDescription.getCaption());

    setDimensions(formDescription.getDimensions());

    Widget root = FormFactory.createForm(formDescription, dataCols, creationCallback, callback);
    if (root == null) {
      return;
    }

    if (addStyle) {
      StyleUtils.makeAbsolute(root);
      root.addStyleName(STYLE_FORM);
    }
    setRootWidget(root);

    add(root);
    add(getNotification());
    
    creationCallback.bind(this, getId());
  }

  public void finishNewRow(IsRow rowValue) {
    fireEvent(new AddEndEvent(false));

    if (rowValue != null) {
      setRow(rowValue);
    } else {
      setRow(getRowBuffer());
    }

    refreshData(true, true);

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

  public List<EditableWidget> getEditableWidgets() {
    return editableWidgets;
  }

  public FormCallback getFormCallback() {
    return formCallback;
  }

  public String getFormName() {
    return formName;
  }

  public Unit getHeightUnit() {
    return (getDimensions() == null) ? null : getDimensions().getHeightUnit();
  }

  public Double getHeightValue() {
    return (getDimensions() == null) ? null : getDimensions().getHeightValue();
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
    EditableWidget editableWidget = getEditableWidgetByColumn(source, false);
    if (editableWidget == null) {
      return null;
    } else {
      return getWidgetById(editableWidget.getWidgetId());
    }
  }

  public String getWidgetId() {
    return getId();
  }

  public Unit getWidthUnit() {
    return (getDimensions() == null) ? null : getDimensions().getWidthUnit();
  }

  public Double getWidthValue() {
    return (getDimensions() == null) ? null : getDimensions().getWidthValue();
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

  public void onActiveWidgetChange(ActiveWidgetChangeEvent event) {
    if (event.isActive()) {
      for (int i = 0; i < getEditableWidgets().size(); i++) {
        if (BeeUtils.same(event.getWidgetId(), getEditableWidgets().get(i).getWidgetId())) {
          setActiveEditableIndex(i);
          break;
        }
      }
    } else {
      setActiveEditableIndex(BeeConst.UNDEF);
    }
  }

  public void onCellUpdate(CellUpdateEvent event) {
    Assert.notNull(event);
    long version = event.getVersion();
    String source = event.getColumnName();
    String value = event.getValue();
    
    IsRow rowValue = getRow();
    boolean wasRowEnabled = isRowEnabled(rowValue); 

    rowValue.setVersion(version);
    int dataIndex = getDataIndex(source);
    rowValue.setValue(dataIndex, value);

    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (BeeUtils.same(source, editableWidget.getColumnId())) {
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

      if (rowEnabled != wasRowEnabled) {
        DomUtils.enableChildren(this, rowEnabled);
        refreshChildWidgets(rowValue);
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
      BeeKeeper.getLog().debug(column.getId(), "old:", oldValue, "new:", newValue);
      if (isAdding() || isEditing()) {
        rowValue.setValue(index, newValue);
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

  public void onPreviewNativeEvent(NativePreviewEvent event) {
    if (EventUtils.isClick(event.getNativeEvent().getType())) {
      if (!BeeUtils.isEmpty(getPreviewId())) {
        setPreviewId(null);
        event.cancel();
      }
    } else if (EventUtils.isMouseDown(event.getNativeEvent().getType())) {
      if (!BeeConst.isUndef(getActiveEditableIndex())) {
        EditableWidget editableWidget = getEditableWidgets().get(getActiveEditableIndex());
        if (!DomUtils.isOrHasChild(editableWidget.getWidgetId(),
            EventUtils.getEventTargetElement(event))) {
          if (!editableWidget.checkForUpdate(true)) {
            setPreviewId(editableWidget.getWidgetId());
            event.cancel();
          }
        }
      }
    }
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
    if (!validate(true)) {
      return;
    }

    List<BeeColumn> columns = Lists.newArrayList();
    List<String> values = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      String value = getRow().getString(i);
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      BeeColumn column = getDataColumns().get(i);
      if (!column.isReadOnly() && !column.isForeign()) {
        columns.add(column);
        values.add(value);
      }
    }

    if (columns.isEmpty()) {
      notifySevere("New Row", "all columns cannot be empty");
      return;
    }

    fireEvent(new ReadyForInsertEvent(columns, values));
  }

  public void refresh(boolean refreshChildren) {
    refreshData(refreshChildren, getRow() != null);
  }

  public void refreshCellContent(String columnSource) {
  }

  public void reset() {
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void setEnabled(boolean enabled) {
    if (this.enabled == enabled) {
      return;
    }
    this.enabled = enabled;

    for (String id : getDisablableWidgets()) {
      Widget widget = getWidgetById(id);
      if (widget instanceof HasEnabled && enabled != ((HasEnabled) widget).isEnabled()) {
        ((HasEnabled) widget).setEnabled(enabled);
      }
    }

    getRootWidget().setStyleName(STYLE_DISABLED, !enabled);
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

  public void start(Integer count) {
    if (hasData()) {
      if (!getTabOrder().isEmpty()) {
        getTabOrder().clear();
      }

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
    fireEvent(new AddStartEvent(NEW_ROW_CAPTION, false));

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
    refreshData(true, true);
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
        BeeColumn column = getDataColumns().get(index);
        fireEvent(new ReadyForUpdateEvent(rowValue, column, oldValue, newValue,
            column.isForeign()));
      }
    }
  }

  public void updateRow(IsRow rowValue, boolean refreshChildren) {
    setRow(rowValue);
    render(refreshChildren);
  }

  public boolean validate(boolean force) {
    boolean ok = true;

    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (!editableWidget.validate(force)) {
        ok = false;
        break;
      }
    }

    if (ok && getRow() != null) {
      ok = ValidationHelper.validateRow(getRow(), getRowValidation(), this);
    }

    return ok;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    closePreview();
    setPreviewReg(Event.addNativePreviewHandler(this));
  }

  @Override
  protected void onUnload() {
    closePreview();
    super.onUnload();
  }

  private void closePreview() {
    if (getPreviewReg() != null) {
      getPreviewReg().removeHandler();
      setPreviewReg(null);
    }
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

    Widget widget = getWidgetById(getTabOrder().get(index).getWidgetId());
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

  private int getActiveEditableIndex() {
    return activeEditableIndex;
  }

  private Dimensions getDimensions() {
    return dimensions;
  }

  private List<String> getDisablableWidgets() {
    return disablableWidgets;
  }

  private Set<DisplayWidget> getDisplayWidgets() {
    return displayWidgets;
  }

  private EditableWidget getEditableWidgetByColumn(String columnId, boolean warn) {
    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (BeeUtils.same(columnId, editableWidget.getColumnId())) {
        return editableWidget;
      }
    }

    if (warn) {
      BeeKeeper.getLog().warning("editable widget not found:", columnId);
    }
    return null;
  }

  private Notification getNotification() {
    return notification;
  }

  private String getPreviewId() {
    return previewId;
  }

  private HandlerRegistration getPreviewReg() {
    return previewReg;
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

  private Evaluator getRowValidation() {
    return rowValidation;
  }

  private List<TabEntry> getTabOrder() {
    return tabOrder;
  }

  private Widget getWidgetById(String id) {
    return DomUtils.getChildQuietly(this, id);
  }

  private boolean hasData() {
    return hasData;
  }

  private boolean isAdding() {
    return adding;
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

  private boolean isRowEnabled(IsRow rowValue) {
    return !isReadOnly() && isEnabled() && isRowEditable(rowValue, false);
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
    BeeKeeper.getBus().fireEventFromSource(new ParentRowEvent(rowValue, isRowEnabled(rowValue)),
        getId());
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

    fireEvent(new ActiveRowChangeEvent(getRow()));

    if (getFormCallback() != null) {
      getFormCallback().afterRefresh(this, getRow());
    }
  }

  private void setActiveEditableIndex(int activeEditableIndex) {
    this.activeEditableIndex = activeEditableIndex;
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

  private void setDimensions(Dimensions dimensions) {
    this.dimensions = dimensions;
  }

  private void setFormCallback(FormCallback formCallback) {
    this.formCallback = formCallback;
  }

  private void setHasData(boolean hasData) {
    this.hasData = hasData;
  }

  private void setPreviewId(String previewId) {
    this.previewId = previewId;
  }

  private void setPreviewReg(HandlerRegistration previewReg) {
    this.previewReg = previewReg;
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

  private void setRowValidation(Evaluator rowValidation) {
    this.rowValidation = rowValidation;
  }

  private void showNote(Level level, String... messages) {
    StyleUtils.setZIndex(getNotification(), StyleUtils.getZIndex(getRootWidget()) + 1);
    getNotification().show(level, messages);
  }
}
