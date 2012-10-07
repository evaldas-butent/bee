package com.butent.bee.client.view.form;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
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
import com.butent.bee.client.validation.ValidationOrigin;
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
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
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
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Handles such form events like warnings, deletions, visibility of elements etc.
 */

public class FormImpl extends Absolute implements FormView, EditEndEvent.Handler,
    NativePreviewHandler {

  private class CreationCallback extends WidgetCreationCallback {

    private CreationCallback() {
      super();
    }
    
    @Override
    public void onSuccess(WidgetDescription result) {
      if (result == null) {
        onFailure("widget description is null");
        return;
      }

      String id = result.getWidgetId();
      FormWidget type = result.getWidgetType();
      if (type == null) {
        onFailure("widget type is null", id);
        return;
      }

      if (result.isDisablable() && !BeeUtils.isEmpty(id)) {
        getDisablableWidgets().add(id);
      }
      
      DisplayWidget displayWidget = null;

      if (type.isDisplay()) {
        String source = result.getSource();
        int index = BeeConst.UNDEF;

        boolean ok = true;
        if (!BeeUtils.isEmpty(source) && hasData()) {
          index = getDataIndex(source);
          if (index < 0) {
            onFailure("display source not found", source, id);
            ok = false;
          }
        }

        if (ok) {
          AbstractCellRenderer renderer = null;
          if (getFormCallback() != null) {
            renderer = getFormCallback().getRenderer(result);
          }
          
          if (renderer == null) {
            renderer = RendererFactory.getRenderer(result.getRendererDescription(),
                result.getRender(), result.getRenderTokens(), result.getItemKey(),
                NameUtils.toList(result.getRenderColumns()), getDataColumns(), index,
                result.getRelation());
          }

          ok = (index >= 0 || renderer != null);
          if (ok) {
            displayWidget = new DisplayWidget(index, renderer, result);
            getDisplayWidgets().add(displayWidget);
          }
        }
      }

      if (type.isEditable() && hasData() && !BeeUtils.isEmpty(result.getSource())) {
        String source = result.getSource();
        int index = getDataIndex(source);

        if (index >= 0) {
          EditableWidget editableWidget = new EditableWidget(getDataColumns(), index, result,
              displayWidget);
          getEditableWidgets().add(editableWidget);

          result.setNullable(editableWidget.isNullable());
          result.setHasDefaults(editableWidget.hasDefaults());
          
          if (getFormCallback() != null) {
            getFormCallback().afterCreateEditableWidget(editableWidget);
          }
        } else {
          onFailure("editable source not found", source, id);
        }
      }

      super.onSuccess(result);
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

  private static final BeeLogger logger = LogUtils.getLogger(FormImpl.class);
  
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
  private String viewName = null;  
  private List<BeeColumn> dataColumns = null;

  private boolean editing = false;
  private boolean adding = false;

  private int pageStart = 0;
  private int rowCount = BeeConst.UNDEF;

  private IsRow activeRow = null;
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
  
  private State state = null;

  public FormImpl(String formName) {
    this(formName, Position.RELATIVE);
  }

  public FormImpl(String formName, Position position) {
    super(position, Overflow.AUTO);
    this.formName = formName;
  }

  @Override
  public HandlerRegistration addActionHandler(ActionEvent.Handler handler) {
    return addHandler(handler, ActionEvent.getType());
  }

  @Override
  public HandlerRegistration addActiveRowChangeHandler(ActiveRowChangeEvent.Handler handler) {
    return addHandler(handler, ActiveRowChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addAddEndHandler(AddEndEvent.Handler handler) {
    return addHandler(handler, AddEndEvent.getType());
  }

  @Override
  public HandlerRegistration addAddStartHandler(AddStartEvent.Handler handler) {
    return addHandler(handler, AddStartEvent.getType());
  }

  @Override
  public HandlerRegistration addCellValidationHandler(String columnId, Handler handler) {
    EditableWidget editableWidget = getEditableWidgetByColumn(columnId, true);
    if (editableWidget == null) {
      return null;
    } else {
      return editableWidget.addCellValidationHandler(handler);
    }
  }

  @Override
  public HandlerRegistration addDataRequestHandler(DataRequestEvent.Handler handler) {
    return addHandler(handler, DataRequestEvent.getType());
  }

  @Override
  public HandlerRegistration addLoadingStateChangeHandler(LoadingStateChangeEvent.Handler handler) {
    return addHandler(handler, LoadingStateChangeEvent.TYPE);
  }

  @Override
  public HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler) {
    return addHandler(handler, ReadyForInsertEvent.getType());
  }

  @Override
  public HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler) {
    return addHandler(handler, ReadyForUpdateEvent.getType());
  }

  @Override
  public HandlerRegistration addScopeChangeHandler(ScopeChangeEvent.Handler handler) {
    return addHandler(handler, ScopeChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addSelectionCountChangeHandler(
      SelectionCountChangeEvent.Handler handler) {
    return null;
  }

  @Override
  public HandlerRegistration addSortHandler(SortEvent.Handler handler) {
    return null;
  }

  @Override
  public void applyOptions(String options) {
  }

  @Override
  public boolean checkForUpdate(boolean reset) {
    if (BeeConst.isUndef(getActiveEditableIndex())) {
      return true;
    }  

    EditableWidget editableWidget = getEditableWidgets().get(getActiveEditableIndex());
    if (editableWidget == null) {
      return true;
    } else {
      return editableWidget.checkForUpdate(reset);
    }
  }

  @Override
  public void create(FormDescription formDescription, String view, List<BeeColumn> dataCols,
      boolean addStyle, FormCallback callback) {
    Assert.notNull(formDescription);
    
    setViewName(BeeUtils.notEmpty(view, formDescription.getViewName()));
    setDataColumns(dataCols);
    setHasData(!BeeUtils.isEmpty(dataCols));

    setFormCallback(callback);
    if (callback != null) {
      callback.setFormView(this);
    }

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

    Widget root = FormFactory.createForm(formDescription, getViewName(), dataCols,
        creationCallback, callback);
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
    
    if (callback != null) {
      callback.afterCreate(this);
    }
  }

  @Override
  public void finishNewRow(IsRow rowValue) {
    fireEvent(new AddEndEvent(false));

    if (rowValue != null) {
      setActiveRow(rowValue);
    } else {
      setActiveRow(getRowBuffer());
    }

    refreshData(true, true);

    if (rowValue != null) {
      int rc = getRowCount();
      setPageStart(rc, false, false, NavigationOrigin.SYSTEM);
      setRowCount(rc + 1, false);
      fireScopeChange();
    }

    setAdding(false);
  }

  @Override
  public void fireLoadingStateChange(LoadingStateChangeEvent.LoadingState loadingState) {
    if (loadingState != null) {
      fireEvent(new LoadingStateChangeEvent(loadingState));
    }
  }

  @Override
  public boolean focus(String source) {
    if (BeeUtils.isEmpty(source)) {
      return false;
    }
    
    Widget widget = getWidgetBySource(source);
    if (widget == null) {
      return false;
    } else {
      DomUtils.setFocus(widget, true);
      return true;
    }
  }
  
  @Override
  public IsRow getActiveRow() {
    return activeRow;
  }

  @Override
  public RowInfo getActiveRowInfo() {
    if (getActiveRow() == null) {
      return null;
    }
    return new RowInfo(getActiveRow());
  }

  @Override
  public String getCaption() {
    return caption;
  }

  @Override
  public List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  @Override
  public int getDataIndex(String source) {
    return DataUtils.getColumnIndex(source, getDataColumns());
  }

  @Override
  public HasDataTable getDisplay() {
    return this;
  }

  @Override
  public FormCallback getFormCallback() {
    return formCallback;
  }

  @Override
  public String getFormName() {
    return formName;
  }

  @Override
  public Unit getHeightUnit() {
    return (getDimensions() == null) ? null : getDimensions().getHeightUnit();
  }

  @Override
  public Double getHeightValue() {
    return (getDimensions() == null) ? null : getDimensions().getHeightValue();
  }

  @Override
  public int getPageSize() {
    return 1;
  }

  @Override
  public int getPageStart() {
    return pageStart;
  }

  @Override
  public Widget getRootWidget() {
    return rootWidget;
  }

  @Override
  public int getRowCount() {
    return rowCount;
  }

  @Override
  public List<? extends IsRow> getRowData() {
    List<IsRow> data = Lists.newArrayList();
    if (getActiveRow() != null) {
      data.add(getActiveRow());
    }
    return data;
  }

  @Override
  public JavaScriptObject getRowJso() {
    if (!hasData() || getActiveRow() == null) {
      return null;
    }

    if (rowJso == null) {
      setRowJso(EvalHelper.createJso(getDataColumns()));
    }
    EvalHelper.toJso(getDataColumns(), getActiveRow(), rowJso);

    return rowJso;
  }

  @Override
  public Order getSortOrder() {
    return null;
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public String getViewName() {
    return viewName;
  }

  @Override
  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  @Override
  public Widget getWidgetBySource(String source) {
    Assert.notEmpty(source);
    EditableWidget editableWidget = getEditableWidgetByColumn(source, false);
    if (editableWidget == null) {
      return null;
    } else {
      return getWidgetById(editableWidget.getWidgetId());
    }
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public Unit getWidthUnit() {
    return (getDimensions() == null) ? null : getDimensions().getWidthUnit();
  }

  @Override
  public Double getWidthValue() {
    return (getDimensions() == null) ? null : getDimensions().getWidthValue();
  }

  @Override
  public boolean isEditing() {
    return editing;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isRowEditable(boolean warn) {
    if (getActiveRow() == null || isReadOnly() || !isEnabled()) {
      return false;
    }
    return isRowEditable(getActiveRow(), warn);
  }

  @Override
  public void notifyInfo(String... messages) {
    showNote(LogLevel.INFO, messages);
  }

  @Override
  public void notifySevere(String... messages) {
    showNote(LogLevel.ERROR, messages);
  }

  @Override
  public void notifyWarning(String... messages) {
    showNote(LogLevel.WARNING, messages);
  }

  @Override
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

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    Assert.notNull(event);

    IsRow rowValue = getActiveRow();
    long rowId = event.getRowId();
    if (rowValue == null || rowValue.getId() != rowId) {
      return;
    }

    long version = event.getVersion();
    String source = event.getColumnName();
    String value = event.getValue();

    boolean wasRowEnabled = isRowEnabled(rowValue);

    rowValue.setVersion(version);
    int dataIndex = getDataIndex(source);
    rowValue.setValue(dataIndex, value);

    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (BeeUtils.same(source, editableWidget.getColumnId())) {
        editableWidget.refresh(rowValue);
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
  
  @Override
  public void onEditEnd(EditEndEvent event, EditEndEvent.HasEditEndHandler source) {
    Assert.notNull(event);

    IsRow rowValue = getActiveRow();
    IsColumn column = event.getColumn();

    Integer keyCode = event.getKeyCode();
    String widgetId = event.getWidgetId();
    boolean hasModifiers = event.hasModifiers();
    
    if (column == null) {
      navigate(keyCode, hasModifiers, widgetId);
      return;
    }
    
    int index = getDataIndex(column.getId());
    String oldValue = rowValue.getString(index);
    String newValue = event.getNewValue();

    if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
      logger.debug(column.getId(), "old:", oldValue, "new:", newValue);

      if (isAdding() || isEditing()) {
        rowValue.setValue(index, newValue);

        if (event.hasRelation() && source instanceof EditableWidget) {
          ((EditableWidget) source).maybeUpdateRelation(getViewName(), rowValue, false);
          refreshEditableWidget(index);
          refreshDisplayWidgets();
        } else if (event.isRowMode()) {
          refreshEditableWidgets();
          refreshDisplayWidgets();
        }

      } else {
        fireEvent(new ReadyForUpdateEvent(rowValue, column, oldValue, newValue, event.isRowMode()));
      }
    }

    navigate(keyCode, hasModifiers, widgetId);
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
  }

  @Override
  public void onPreviewNativeEvent(NativePreviewEvent event) {
    String type = event.getNativeEvent().getType();

    if (EventUtils.isClick(type)) {
      if (!BeeUtils.isEmpty(getPreviewId())) {
        setPreviewId(null);
        event.cancel();
      }

    } else if (EventUtils.isMouseDown(type)) {
      if (!BeeConst.isUndef(getActiveEditableIndex())) {
        Element targetElement = EventUtils.getEventTargetElement(event);
        EditableWidget editableWidget = getEditableWidgets().get(getActiveEditableIndex());

        if (!editableWidget.getEditor().isOrHasPartner(targetElement)) {
          if (!editableWidget.checkForUpdate(true)) {
            setPreviewId(editableWidget.getWidgetId());
            event.cancel();
          }
        }
      }
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    Assert.notNull(event);
    IsRow newRow = event.getRow();
    
    if (DataUtils.sameId(getActiveRow(), newRow)) {
      setActiveRow(newRow);
      refreshData(false, false);
    }
  }

  @Override
  public void prepareForInsert() {
    if (getFormCallback() != null 
        && !getFormCallback().onPrepareForInsert(this, this, getActiveRow())) {
      return;
    }
    if (!validate()) {
      return;
    }

    List<BeeColumn> columns = Lists.newArrayList();
    List<String> values = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      String value = getActiveRow().getString(i);
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      BeeColumn column = getDataColumns().get(i);
      if (column.isWritable()) {
        columns.add(column);
        values.add(value);
      }
    }

    if (columns.isEmpty()) {
      notifySevere("New Row", "all columns cannot be empty");
      return;
    }

    fireEvent(new ReadyForInsertEvent(columns, values, null));
  }

  @Override
  public void refresh(boolean refreshChildren) {
    refreshData(refreshChildren, getActiveRow() != null);
  }

  @Override
  public void refreshCellContent(String columnSource) {
  }

  @Override
  public void refreshChildWidgets(IsRow rowValue) {
    BeeKeeper.getBus().fireEventFromSource(new ParentRowEvent(getViewName(), rowValue,
        isRowEnabled(rowValue)), getId());
  }

  @Override
  public void reset() {
  }

  @Override
  public void setActiveRow(IsRow activeRow) {
    if (getFormCallback() != null) {
      getFormCallback().onSetActiveRow(activeRow);
    }
    this.activeRow = activeRow;
  }

  @Override
  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  @Override
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

  @Override
  public void setPageSize(int size, boolean fireScopeChange) {
  }

  @Override
  public void setPageStart(int start, boolean fireScopeChange, boolean fireDataRequest,
      NavigationOrigin origin) {
    Assert.nonNegative(start);
    if (start == getPageStart()) {
      return;
    }

    this.pageStart = start;

    if (fireScopeChange) {
      fireScopeChange();
    }
    if (fireDataRequest) {
      fireDataRequest(origin);
    }
  }

  @Override
  public void setRowCount(int count, boolean fireScopeChange) {
    Assert.nonNegative(count);
    if (count == getRowCount()) {
      return;
    }

    this.rowCount = count;

    if (getPageStart() >= count) {
      setPageStart(Math.max(count - 1, 0), true, false, NavigationOrigin.SYSTEM);
    } else if (fireScopeChange) {
      fireScopeChange();
    }
  }

  @Override
  public void setRowData(List<? extends IsRow> values, boolean refresh) {
    if (BeeUtils.isEmpty(values)) {
      setActiveRow(null);
    } else {
      setActiveRow(values.get(0));
    }

    if (refresh) {
      refresh(true);
    }
  }

  @Override
  public void setState(State state) {
    this.state = state;
  }

  @Override
  public void setViewPresenter(Presenter presenter) {
    this.viewPresenter = presenter;
  }

  @Override
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
          fireDataRequest(NavigationOrigin.SYSTEM);
        } else {
          setActiveRow(null);
          fireLoadingStateChange(LoadingStateChangeEvent.LoadingState.LOADED);
        }
      }
    }
    
    if (getFormCallback() != null) {
      getFormCallback().onStart(this);
    }
  }

  @Override
  public void startNewRow() {
    setAdding(true);
    fireEvent(new AddStartEvent(NEW_ROW_CAPTION, false));

    IsRow oldRow = getActiveRow();
    setRowBuffer(oldRow);
    if (oldRow == null) {
      oldRow = DataUtils.createEmptyRow(getDataColumns().size());
    }
    IsRow newRow = DataUtils.createEmptyRow(getDataColumns().size());

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

    setActiveRow(newRow);
    refreshData(true, true);
  }

  @Override
  public void updateActiveRow(List<? extends IsRow> values) {
  }

  @Override
  public void updateCell(String columnId, String newValue) {
    Assert.notEmpty(columnId);

    IsRow rowValue = getActiveRow();
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

  @Override
  public void updateRow(IsRow rowValue, boolean refreshChildren) {
    setActiveRow(rowValue);
    render(refreshChildren);
  }

  @Override
  public boolean validate() {
    boolean ok = true;

    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (!editableWidget.validate(ValidationOrigin.FORM)) {
        Widget widget = getWidgetById(editableWidget.getWidgetId());
        if (widget != null) {
          DomUtils.setFocus(widget, true);
        }

        ok = false;
        break;
      }
    }

    if (ok && getActiveRow() != null) {
      ok = ValidationHelper.validateRow(getActiveRow(), getRowValidation(), this);
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

  private void fireDataRequest(NavigationOrigin origin) {
    fireEvent(new DataRequestEvent(origin));
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
      logger.warning("editable widget not found:", columnId);
    }
    return null;
  }

  private List<EditableWidget> getEditableWidgets() {
    return editableWidgets;
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
      notifyWarning("Row is read only:", getRowEditable().toString());
    }
    return ok;
  }

  private boolean isRowEnabled(IsRow rowValue) {
    return !isReadOnly() && isEnabled() && isRowEditable(rowValue, false);
  }

  private void navigate(Integer keyCode, boolean hasModifiers, String widgetId) {
    if (keyCode != null && !BeeUtils.isEmpty(widgetId) && getTabOrder().size() > 1
        && !State.CLOSED.equals(getState())) {
      switch (BeeUtils.unbox(keyCode)) {
        case KeyCodes.KEY_ENTER:
        case KeyCodes.KEY_DOWN:
          navigate(widgetId, true);
          break;

        case KeyCodes.KEY_TAB:
          navigate(widgetId, !hasModifiers);
          break;

        case KeyCodes.KEY_UP:
          navigate(widgetId, false);
          break;
      }
    }
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
      String id = displayWidget.getWidgetId();
      Widget widget = DomUtils.getChildQuietly(this, id);

      if (widget == null) {
        logger.warning("refresh display:", id, "widget not found");
      } else {
        displayWidget.refresh(widget, getActiveRow());
      }
    }
  }

  private void refreshEditableWidget(int dataIndex) {
    for (EditableWidget editableWidget : getEditableWidgets()) {
      if (editableWidget.getDataIndex() == dataIndex) {
        editableWidget.refresh(getActiveRow());
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

      if (getActiveRow() == null) {
        editable = false;
      } else {
        editable = rowEnabled && !editableWidget.isReadOnly();
        if (editable) {
          editable = editableWidget.isEditable(getActiveRow());
        }
      }

      editableWidget.refresh(getActiveRow());
      if (editable != editor.isEnabled()) {
        editor.setEnabled(editable);
      }
    }
  }

  private void render(boolean refreshChildren) {
    if (getFormCallback() != null) {
      getFormCallback().beforeRefresh(this, getActiveRow());
    }

    refreshEditableWidgets();
    refreshDisplayWidgets();

    if (refreshChildren) {
      refreshChildWidgets(getActiveRow());
    }

    fireEvent(new ActiveRowChangeEvent(getActiveRow()));

    if (getFormCallback() != null) {
      getFormCallback().afterRefresh(this, getActiveRow());
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

  private void setViewName(String viewName) {
    this.viewName = viewName;
  }

  private void showNote(LogLevel level, String... messages) {
    StyleUtils.setZIndex(getNotification(), StyleUtils.getZIndex(getRootWidget()) + 1);
    getNotification().show(level, messages);
  }
}
