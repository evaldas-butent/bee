package com.butent.bee.client.view.grid;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.CalculatedCell;
import com.butent.bee.client.grid.CalculatedColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.RowIdColumn;
import com.butent.bee.client.grid.RowVersionColumn;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.ConditionalStyle;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.RowEditor;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.ColumnDescription.CellType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Creates cell grid elements, connecting view and presenter elements of them.
 */

public class CellGridImpl extends Absolute implements GridView, SearchView, EditStartEvent.Handler,
    EditEndEvent.Handler {

  private class FilterUpdater implements ValueUpdater<String> {
    public void update(String value) {
      if (getFilterChangeHandler() != null) {
        getFilterChangeHandler().onChange(null);
      }
    }
  }

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

  private ChangeHandler filterChangeHandler = null;
  private final FilterUpdater filterUpdater = new FilterUpdater();

  private final CellGrid grid = new CellGrid();

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

  public CellGridImpl() {
    super();
  }

  public HandlerRegistration addAddEndHandler(AddEndEvent.Handler handler) {
    return addHandler(handler, AddEndEvent.getType());
  }

  public HandlerRegistration addAddStartHandler(AddStartEvent.Handler handler) {
    return addHandler(handler, AddStartEvent.getType());
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    setFilterChangeHandler(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        setFilterChangeHandler(null);
      }
    };
  }

  public HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler) {
    return addHandler(handler, ReadyForInsertEvent.getType());
  }

  public HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler) {
    return addHandler(handler, ReadyForUpdateEvent.getType());
  }

  public void applyOptions(String options) {
    if (BeeUtils.isEmpty(options)) {
      return;
    }

    boolean redraw = false;
    String[] opt = BeeUtils.split(options, ";");

    for (int i = 0; i < opt.length; i++) {
      String[] arr = BeeUtils.split(opt[i], " ");
      int len = arr.length;
      if (len <= 1) {
        continue;
      }
      String cmd = arr[0].trim().toLowerCase();
      String args = opt[i].trim().substring(cmd.length() + 1).trim();

      int[] xp = new int[len - 1];
      String[] sp = new String[len - 1];

      for (int j = 1; j < len; j++) {
        sp[j - 1] = arr[j].trim();
        if (BeeUtils.isDigit(arr[j])) {
          xp[j - 1] = BeeUtils.toInt(arr[j]);
        } else {
          xp[j - 1] = 0;
        }
      }

      Edges edges = null;
      switch (len - 1) {
        case 1:
          edges = new Edges(xp[0]);
          break;
        case 2:
          edges = new Edges(xp[0], xp[1]);
          break;
        case 3:
          edges = new Edges(xp[0], xp[1], xp[2]);
          break;
        default:
          edges = new Edges(xp[0], xp[1], xp[2], xp[3]);
      }

      int cc = getGrid().getColumnCount();
      String colId = sp[0];
      if (BeeUtils.isDigit(colId) && xp[0] < cc) {
        colId = getGrid().getColumnId(xp[0]);
      }

      String msg = null;

      if (cmd.startsWith("bh")) {
        msg = "setBodyCellHeight " + xp[0];
        getGrid().setBodyCellHeight(xp[0]);
        redraw = true;
      } else if (cmd.startsWith("bp")) {
        msg = "setBodyCellPadding " + edges.getCssValue();
        getGrid().setBodyCellPadding(edges);
        redraw = true;
      } else if (cmd.startsWith("bw")) {
        msg = "setBodyBorderWidth " + edges.getCssValue();
        getGrid().setBodyBorderWidth(edges);
        redraw = true;
      } else if (cmd.startsWith("bm")) {
        msg = "setBodyCellMargin " + edges.getCssValue();
        getGrid().setBodyCellMargin(edges);
        redraw = true;
      } else if (cmd.startsWith("bf")) {
        msg = "setColumnBodyFont " + args;
        getGrid().setBodyFont(args);
        redraw = true;

      } else if (cmd.startsWith("hh")) {
        msg = "setHeaderCellHeight " + xp[0];
        getGrid().setHeaderCellHeight(xp[0]);
        redraw = true;
      } else if (cmd.startsWith("hp")) {
        msg = "setHeaderCellPadding " + edges.getCssValue();
        getGrid().setHeaderCellPadding(edges);
        redraw = true;
      } else if (cmd.startsWith("hw")) {
        msg = "setHeaderBorderWidth " + edges.getCssValue();
        getGrid().setHeaderBorderWidth(edges);
        redraw = true;
      } else if (cmd.startsWith("hm")) {
        msg = "setHeaderCellMargin " + edges.getCssValue();
        getGrid().setHeaderCellMargin(edges);
        redraw = true;
      } else if (cmd.startsWith("hf")) {
        msg = "setColumnHeaderFont " + args;
        getGrid().setHeaderFont(args);
        redraw = true;

      } else if (cmd.startsWith("fh")) {
        msg = "setFooterCellHeight " + xp[0];
        getGrid().setFooterCellHeight(xp[0]);
        redraw = true;
      } else if (cmd.startsWith("fp")) {
        msg = "setFooterCellPadding " + edges.getCssValue();
        getGrid().setFooterCellPadding(edges);
        redraw = true;
      } else if (cmd.startsWith("fw")) {
        msg = "setFooterBorderWidth " + edges.getCssValue();
        getGrid().setFooterBorderWidth(edges);
        redraw = true;
      } else if (cmd.startsWith("fm")) {
        msg = "setFooterCellMargin " + edges.getCssValue();
        getGrid().setFooterCellMargin(edges);
        redraw = true;
      } else if (cmd.startsWith("ff")) {
        msg = "setColumnFooterFont " + args;
        getGrid().setFooterFont(args);
        redraw = true;

      } else if (cmd.startsWith("chw") && len > 2) {
        msg = "setColumnHeaderWidth " + colId + " " + xp[1];
        getGrid().setColumnHeaderWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("chf") && len > 2) {
        String font = ArrayUtils.join(sp, " ", 1);
        msg = "setColumnHeaderFont " + colId + " " + font;
        getGrid().setColumnHeaderFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cbw") && len > 2) {
        msg = "setColumnBodyWidth " + colId + " " + xp[1];
        getGrid().setColumnBodyWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("cbf") && len > 2) {
        String font = ArrayUtils.join(sp, " ", 1);
        msg = "setColumnBodyFont " + colId + " " + font;
        getGrid().setColumnBodyFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cfw") && len > 2) {
        msg = "setColumnFooterWidth " + colId + " " + xp[1];
        getGrid().setColumnFooterWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("cff") && len > 2) {
        String font = ArrayUtils.join(sp, " ", 1);
        msg = "setColumnFooterFont " + colId + " " + font;
        getGrid().setColumnFooterFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cw") && len > 2) {
        if (len <= 3) {
          msg = "setColumnWidth " + colId + " " + xp[1];
          getGrid().setColumnWidth(colId, xp[1]);
          redraw = true;
        } else {
          msg = "setColumnWidth " + colId + " " + xp[1] + " " + StyleUtils.parseUnit(sp[2]);
          getGrid().setColumnWidth(colId, xp[1], StyleUtils.parseUnit(sp[2]));
          redraw = true;
        }

      } else if (cmd.startsWith("minw")) {
        msg = "setMinCellWidth " + xp[0];
        getGrid().setMinCellWidth(xp[0]);
      } else if (cmd.startsWith("maxw")) {
        msg = "setMaxCellWidth " + xp[0];
        getGrid().setMaxCellWidth(xp[0]);
      } else if (cmd.startsWith("minh")) {
        msg = "setMinCellHeight " + xp[0];
        getGrid().setMinBodyCellHeight(xp[0]);
      } else if (cmd.startsWith("maxh")) {
        msg = "setMaxCellHeight " + xp[0];
        getGrid().setMaxBodyCellHeight(xp[0]);

      } else if (cmd.startsWith("zm")) {
        msg = "setResizerMoveSensitivityMillis " + xp[0];
        getGrid().setResizerMoveSensitivityMillis(xp[0]);
      } else if (cmd.startsWith("zs")) {
        msg = "setResizerShowSensitivityMillis " + xp[0];
        getGrid().setResizerShowSensitivityMillis(xp[0]);

      } else if (cmd.startsWith("fit")) {
        if (getGrid().contains(colId)) {
          getGrid().autoFitColumn(colId);
          msg = "autoFitColumn " + colId;
        } else {
          getGrid().autoFit();
          msg = "autoFit";
        }

      } else if (cmd.startsWith("ps")) {
        if (xp[0] > 0) {
          updatePageSize(xp[0], false);
          msg = "updatePageSize " + xp[0];
        } else {
          int oldPageSize = getGrid().getPageSize();
          int newPageSize = getGrid().estimatePageSize();
          if (newPageSize > 0) {
            updatePageSize(newPageSize, false);
          }
          msg = "page size: old " + oldPageSize + " new " + newPageSize;
        }
      }

      if (msg == null) {
        BeeKeeper.getLog().warning("unrecognized command", opt[i]);
      } else {
        BeeKeeper.getLog().info(msg);
      }
    }

    if (redraw) {
      getGrid().redraw();
    }
  }

  public void create(List<BeeColumn> dataCols, int rowCount, BeeRowSet rowSet,
      GridDescription gridDescr, boolean hasSearch) {
    Assert.notEmpty(dataCols);
    setDataColumns(dataCols);

    boolean hasHeaders = (gridDescr == null) ? true : !BeeUtils.isFalse(gridDescr.hasHeaders());
    boolean hasFooters = hasSearch;
    if (hasFooters && gridDescr != null && BeeUtils.isFalse(gridDescr.hasFooters())) {
      hasFooters = false;
    }

    boolean showColumnWidths = true;
    List<ColumnDescription> columnDescriptions = null;

    if (gridDescr != null) {
      if (hasHeaders && gridDescr.getHeader() != null) {
        getGrid().setHeaderComponent(gridDescr.getHeader());
      }
      if (gridDescr.getBody() != null) {
        getGrid().setBodyComponent(gridDescr.getBody());
      }
      if (hasFooters && gridDescr.getFooter() != null) {
        getGrid().setFooterComponent(gridDescr.getFooter());
      }

      if (BeeUtils.isTrue(gridDescr.isReadOnly())) {
        getGrid().setReadOnly(true);
      }

      if (gridDescr.getMinColumnWidth() != null) {
        getGrid().setMinCellWidth(gridDescr.getMinColumnWidth());
      }
      if (gridDescr.getMaxColumnWidth() != null) {
        getGrid().setMaxCellWidth(gridDescr.getMaxColumnWidth());
      }

      if (BeeUtils.isFalse(gridDescr.showColumnWidths())) {
        showColumnWidths = false;
      }

      if (gridDescr.getRowStyles() != null) {
        ConditionalStyle rowStyles = ConditionalStyle.create(gridDescr.getRowStyles(), null,
            dataCols);
        if (rowStyles != null) {
          getGrid().setRowStyles(rowStyles);
        }
      }

      if (gridDescr.getRowEditable() != null) {
        setRowEditable(Evaluator.create(gridDescr.getRowEditable(), null, dataCols));
      }
      if (gridDescr.getRowValidation() != null) {
        setRowValidation(Evaluator.create(gridDescr.getRowValidation(), null, dataCols));
      }

      columnDescriptions = gridDescr.getVisibleColumns();
    }

    if (columnDescriptions == null) {
      columnDescriptions = Lists.newArrayList();
    }
    if (columnDescriptions.isEmpty()) {
      ColumnDescription idCol = new ColumnDescription(ColType.ID, "rowId");
      idCol.setCaption("Id");
      idCol.setReadOnly(true);
      idCol.setSortable(false);
      idCol.setVisible(true);
      idCol.setShowWidth(false);
      idCol.setHasFooter(hasFooters);
      columnDescriptions.add(idCol);

      for (int i = 0; i < dataCols.size(); i++) {
        ColumnDescription dataCol = new ColumnDescription(ColType.DATA, dataCols.get(i).getId());
        dataCol.setReadOnly(getGrid().isReadOnly());
        dataCol.setSortable(true);
        dataCol.setVisible(true);
        dataCol.setShowWidth(showColumnWidths);
        dataCol.setHasFooter(hasFooters);
        dataCol.setSource(dataCols.get(i).getId());
        columnDescriptions.add(dataCol);
      }

      ColumnDescription verCol = new ColumnDescription(ColType.VERSION, "rowVersion");
      verCol.setCaption("Version");
      verCol.setReadOnly(true);
      verCol.setSortable(false);
      verCol.setVisible(true);
      verCol.setShowWidth(showColumnWidths);
      verCol.setHasFooter(hasFooters);
      columnDescriptions.add(verCol);
    }

    AbstractColumn<?> column;
    ColumnHeader header = null;
    ColumnFooter footer = null;
    int dataIndex;

    for (ColumnDescription columnDescr : columnDescriptions) {
      String columnId = columnDescr.getName();
      ColType colType = columnDescr.getColType();
      if (BeeUtils.isEmpty(columnId) || colType == null) {
        continue;
      }

      String caption = BeeUtils.ifString(columnDescr.getCaption(), columnId);
      String source = columnDescr.getSource();
      dataIndex = BeeConst.UNDEF;

      CellType cellType = columnDescr.getCellType();
      column = null;

      switch (colType) {
        case ID:
          column = new RowIdColumn();
          break;

        case VERSION:
          column = new RowVersionColumn();
          break;

        case DATA:
        case RELATED:
          for (int i = 0; i < dataCols.size(); i++) {
            BeeColumn dataColumn = dataCols.get(i);
            if (BeeUtils.same(source, dataColumn.getId())) {
              column = GridFactory.createColumn(dataColumn, i, cellType);
              if (columnDescr.isSortable()) {
                column.setSortable(true);
              }

              RelationInfo relationInfo;
              if (ColType.RELATED.equals(colType)) {
                relationInfo = RelationInfo.create(dataCols, columnDescr);
              } else {
                relationInfo = null;
              }

              EditableColumn editableColumn =
                  new EditableColumn(dataCols, i, relationInfo, column, caption, columnDescr);
              editableColumn.setNotificationListener(this);
              getEditableColumns().put(columnId, editableColumn);

              if (relationInfo != null) {
                getRelations().add(relationInfo);
              }

              dataIndex = i;
              break;
            }
          }
          break;

        case CALCULATED:
          Calculation calc = columnDescr.getCalc();
          if (calc != null && !calc.isEmpty()) {
            Cell<String> cell =
                (cellType == null) ? new CalculatedCell() : GridFactory.createCell(cellType);
            CalculatedColumn calcColumn = new CalculatedColumn(cell, columnDescr.getValueType(),
                Evaluator.create(calc, columnId, dataCols));

            if (columnDescr.getPrecision() != null) {
              calcColumn.setPrecision(columnDescr.getPrecision());
            }
            if (columnDescr.getScale() != null) {
              calcColumn.setScale(columnDescr.getScale());
              if (ValueType.DECIMAL.equals(columnDescr.getValueType())) {
                calcColumn.setNumberFormat(Format.getDecimalFormat(columnDescr.getScale()));
              }
            }
            column = calcColumn;
          }
          break;
      }

      if (column == null) {
        BeeKeeper.getLog().warning("cannot create column:", columnId, colType, source);
        continue;
      }

      if (!BeeUtils.isEmpty(columnDescr.getFormat())) {
        Format.setFormat(column, column.getValueType(), columnDescr.getFormat());
      }
      if (!BeeUtils.isEmpty(columnDescr.getHorAlign())) {
        UiHelper.setHorizontalAlignment(column, columnDescr.getHorAlign());
      }

      if (hasHeaders) {
        header = new ColumnHeader(columnId, caption,
            showColumnWidths && !BeeUtils.isFalse(columnDescr.showWidth()));
      }
      if (hasFooters && BeeUtils.isTrue(columnDescr.hasFooter())
          && !BeeUtils.isEmpty(source)) {
        footer = new ColumnFooter(source, getFilterUpdater());
      } else {
        footer = null;
      }

      getGrid().addColumn(columnId, dataIndex, column, header, footer);
      getGrid().setColumnInfo(columnId, columnDescr, dataCols);
    }

    initNewRowColumns((gridDescr == null) ? null : gridDescr.getNewRowColumns());

    getGrid().setRowCount(rowCount);

    if (rowSet != null) {
      getGrid().estimateColumnWidths(rowSet.getRows().getList());
    }
    getGrid().estimateHeaderWidths(true);

    getGrid().addEditStartHandler(this);

    add(getGrid());
    add(getNotification());
  }

  public int estimatePageSize(int containerWidth, int containerHeight) {
    return getGrid().estimatePageSize(containerWidth, containerHeight, true);
  }

  public void finishNewRow(IsRow row) {
    StyleUtils.hideDisplay(getNewRowWidget());
    fireEvent(new AddEndEvent());

    StyleUtils.hideScroll(this, ScrollBars.BOTH);
    StyleUtils.unhideDisplay(getGrid());
    getGrid().setEditing(false);

    if (row != null) {
      getGrid().insertRow(row);
    }
  }

  public RowInfo getActiveRowInfo() {
    return getGrid().getActiveRowInfo();
  }

  public Filter getFilter(List<? extends IsColumn> columns) {
    List<ColumnFooter> footers = getGrid().getFooters();

    if (footers == null || footers.size() <= 0) {
      return null;
    }
    Filter filter = null;

    for (Header<?> footer : footers) {
      if (!(footer instanceof ColumnFooter)) {
        continue;
      }
      String input = BeeUtils.trim(((ColumnFooter) footer).getValue());
      if (BeeUtils.isEmpty(input)) {
        continue;
      }
      String source = ((ColumnFooter) footer).getSource();
      if (BeeUtils.isEmpty(source)) {
        continue;
      }
      Filter flt = DataUtils.parseExpression(source + " " + input, columns, true);

      if (flt == null) {
        continue;
      }
      if (filter == null) {
        filter = flt;
      } else {
        filter = CompoundFilter.and(filter, flt);
      }
    }
    return filter;
  }

  public CellGrid getGrid() {
    return grid;
  }

  public Collection<RowInfo> getSelectedRows() {
    return getGrid().getSelectedRows().values();
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
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

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isRowEditable(long rowId, boolean warn) {
    IsRow rowValue = getGrid().getRowById(rowId);
    if (rowValue == null) {
      return false;
    }
    return isRowEditable(rowValue, warn);
  }

  public boolean isRowSelected(long rowId) {
    return getGrid().isRowSelected(rowId);
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

  public void onEditEnd(EditEndEvent event) {
    Assert.notNull(event);
    getGrid().setEditing(false);
    getGrid().refocus();

    if (!BeeUtils.equalsTrimRight(event.getOldValue(), event.getNewValue())) {
      updateCell(event.getRowValue(), event.getColumn(), event.getOldValue(), event.getNewValue(),
          event.isRowMode());
    }

    if (event.getKeyCode() != null) {
      int keyCode = BeeUtils.unbox(event.getKeyCode());
      if (BeeUtils.inList(keyCode, KeyCodes.KEY_TAB, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN)) {
        getGrid().handleKeyboardNavigation(keyCode, event.hasModifiers());
      }
    }
  }

  public void onEditStart(EditStartEvent event) {
    Assert.notNull(event);
    String columnId = event.getColumnId();
    EditableColumn editableColumn = getEditableColumn(columnId);
    if (editableColumn == null) {
      return;
    }

    IsRow rowValue = event.getRowValue();
    if (!isRowEditable(rowValue, true)) {
      return;
    }
    if (!editableColumn.isCellEditable(rowValue, true)) {
      return;
    }

    if (event.getCharCode() == EditorFactory.START_KEY_DELETE) {
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
        && BeeUtils.inList(event.getCharCode(), EditorFactory.START_MOUSE_CLICK,
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

    getGrid().setEditing(true);
    if (event.getSourceElement() != null) {
      event.getSourceElement().blur();
    }

    editableColumn.openEditor(this, event.getSourceElement(), getGrid().getElement(),
        getGrid().getZIndex() + 1, rowValue, BeeUtils.toChar(event.getCharCode()), this);
  }

  public void refreshCellContent(long rowId, String columnSource) {
    getGrid().refreshCellContent(rowId, columnSource);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setViewPresenter(Presenter presenter) {
    this.viewPresenter = presenter;
  }

  public void setVisibleRange(int start, int length) {
    getGrid().setVisibleRange(start, length);
  }

  public void startNewRow() {
    if (getNewRowColumns().isEmpty()) {
      return;
    }

    getGrid().setEditing(true);
    StyleUtils.hideDisplay(getGrid());
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
        if (getGrid().getActiveRow() >= 0
            && getGrid().getActiveRow() < getGrid().getVisibleItemCount()) {
          oldRow = getGrid().getVisibleItem(getGrid().getActiveRow());
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

  public void updatePageSize(int pageSize, boolean init) {
    Assert.isPositive(pageSize);
    int oldSize = getGrid().getPageSize();

    if (oldSize == pageSize) {
      if (init) {
        getGrid().setVisibleRangeAndClearData(getGrid().getVisibleRange(), true);
      }
    } else {
      getGrid().setVisibleRange(getGrid().getPageStart(), pageSize);
    }
  }

  private boolean checkNewRow(IsRow row) {
    boolean ok = true;
    int count = 0;

    for (String columnId : getNewRowColumns()) {
      EditableColumn editableColumn = getEditableColumn(columnId);
      String value = row.getString(editableColumn.getColIndex());
      if (BeeUtils.isEmpty(value)) {
        if (!editableColumn.isNullable()) {
          notifySevere(getGrid().getColumnCaption(columnId), "Value required");
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

  private ChangeHandler getFilterChangeHandler() {
    return filterChangeHandler;
  }

  private FilterUpdater getFilterUpdater() {
    return filterUpdater;
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

  private Set<RelationInfo> getRelations() {
    return relations;
  }

  private Evaluator getRowEditable() {
    return rowEditable;
  }

  private Evaluator getRowValidation() {
    return rowValidation;
  }

  private void initNewRowColumns(String columnNames) {
    getNewRowColumns().clear();
    if (getGrid().isReadOnly() || getEditableColumns().isEmpty()) {
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
        if (!entry.getValue().isNullable() || !getGrid().isColumnReadOnly(id)) {
          getNewRowColumns().add(id);
        }
      }
    }
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

  private void setFilterChangeHandler(ChangeHandler filterChangeHandler) {
    this.filterChangeHandler = filterChangeHandler;
  }

  private void setNewRowWidget(RowEditor newRowWidget) {
    this.newRowWidget = newRowWidget;
  }

  private void setRowEditable(Evaluator rowEditable) {
    this.rowEditable = rowEditable;
  }

  private void setRowValidation(Evaluator rowValidation) {
    this.rowValidation = rowValidation;
  }

  private void showNote(Level level, String... messages) {
    StyleUtils.setZIndex(getNotification(), getGrid().getZIndex() + 1);
    getNotification().show(level, messages);
  }

  private void updateCell(IsRow rowValue, IsColumn dataColumn, String oldValue, String newValue,
      boolean rowMode) {
    getGrid().preliminaryUpdate(rowValue.getId(), dataColumn.getId(), newValue);
    fireEvent(new ReadyForUpdateEvent(rowValue, dataColumn, oldValue, newValue, rowMode));
  }
}
