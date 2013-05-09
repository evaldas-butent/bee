package com.butent.bee.client.view.grid;

import com.google.common.collect.Lists;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterDescription;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AbstractGridInterceptor implements GridInterceptor {

  public static final List<String> DELETE_ROW_MESSAGE = Lists.newArrayList("Išmesti eilutę ?");

  public static Pair<String, String> deleteRowsMessage(int selectedRows) {
    String m1 = "Išmesti aktyvią eilutę";

    String m2 = (selectedRows == 1)
        ? "Išmesti pažymėtą eilutę"
        : BeeUtils.joinWords("Išmesti", selectedRows, "pažymėtas eilutes");

    return Pair.of(m1, m2);
  }

  private GridPresenter gridPresenter = null;

  @Override
  public void afterAction(Action action, GridPresenter presenter) {
  }

  @Override
  public void afterCreate(GridView gridView) {
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {
    return true;
  }

  @Override
  public void afterCreateColumns(GridView gridView) {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
  }

  @Override
  public void afterDeleteRow(long rowId) {
  }

  @Override
  public void afterInsertRow(IsRow result) {
  }

  @Override
  public void afterUpdateCell(IsColumn column, IsRow result, boolean rowMode) {
  }

  @Override
  public void afterUpdateRow(IsRow result) {
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    return true;
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter) {
    return true;
  }

  @Override
  public void beforeCreate(List<? extends IsColumn> dataColumns, GridDescription gridDescription) {
  }

  @Override
  public boolean beforeCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {
    return true;
  }

  @Override
  public void beforeCreateColumns(List<? extends IsColumn> dataColumns,
      List<ColumnDescription> columnDescriptions) {
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    return true;
  }

  @Override
  public DeleteMode beforeDeleteRow(GridPresenter presenter, IsRow row) {
    return DeleteMode.DEFAULT;
  }

  @Override
  public DeleteMode beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows) {
    return DeleteMode.DEFAULT;
  }

  @Override
  public void beforeRefresh(GridPresenter presenter) {
  }

  @Override
  public IdentifiableWidget createCustomWidget(String name, Element description) {
    return null;
  }

  @Override
  public String getCaption() {
    return null;
  }

  @Override
  public String getColumnCaption(String columnName) {
    return null;
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {
    return defMode;
  }

  @Override
  public List<String> getDeleteRowMessage(IsRow row) {
    return DELETE_ROW_MESSAGE;
  }

  @Override
  public Pair<String, String> getDeleteRowsMessage(int selectedRows) {
    return deleteRowsMessage(selectedRows);
  }

  @Override
  public AbstractFilterSupplier getFilterSupplier(String columnName,
      ColumnDescription columnDescription) {
    return null;
  }

  @Override
  public GridPresenter getGridPresenter() {
    return gridPresenter;
  }

  @Override
  public Map<String, Filter> getInitialParentFilters() {
    return null;
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return null;
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public List<FilterDescription> getPredefinedFilters(List<FilterDescription> defaultFilters) {
    return defaultFilters;
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {
    return null;
  }

  @Override
  public String getRowCaption(IsRow row, boolean edit) {
    return null;
  }

  @Override
  public String getSupplierKey() {
    return null;
  }

  @Override
  public boolean onClose(GridPresenter presenter) {
    return true;
  }

  @Override
  public void onEditStart(EditStartEvent event) {
  }

  @Override
  public boolean onLoad(GridDescription gridDescription) {
    return true;
  }

  @Override
  public boolean onLoadExtWidget(Element root) {
    return true;
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
  }

  @Override
  public void onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {
  }

  @Override
  public void onReadyForUpdate(GridView gridView, ReadyForUpdateEvent event) {
  }

  @Override
  public void onSaveChanges(GridView gridView, SaveChangesEvent event) {
  }

  @Override
  public void onShow(GridPresenter presenter) {
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    return true;
  }

  @Override
  public void setGridPresenter(GridPresenter gridPresenter) {
    this.gridPresenter = gridPresenter;
  }
}
