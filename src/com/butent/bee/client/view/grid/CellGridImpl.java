package com.butent.bee.client.view.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
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
import com.butent.bee.client.grid.SelectionColumn;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.AbstractPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.ConditionalStyle;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.RowEditor;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Creates cell grid elements, connecting view and presenter elements of them.
 */

public class CellGridImpl extends Absolute implements GridView, SearchView, EditStartEvent.Handler,
    EditEndEvent.Handler, EditFormEvent.Handler {

  private class FilterUpdater implements ValueUpdater<String> {
    public void update(String value) {
      if (getFilterChangeHandler() != null) {
        getFilterChangeHandler().onChange(null);
      }
    }
  }

  private class GridFormPresenter extends AbstractPresenter {
    private final HeaderView header;

    private GridFormPresenter(HeaderView header) {
      super();
      this.header = header;
    }

    public void handleAction(Action action) {
      if (action == null) {
        return;
      }
      
      switch (action) {
        case CLOSE:
          formCancel();
          break;
        case EDIT:
          getEditForm().setEnabled(true);
          hideAction(action);
          break;
        case SAVE:
          formConfirm();
          break;
        default:  
      }
    }
    
    private void hideAction(Action action) {
      header.showAction(action, false);
    }
    
    private void setCaption(String caption) {
      header.setCaption(caption);
    }

    private void setMessage(String message) {
      header.setMessage(message);
    }
    
    private void showAction(Action action) {
      header.showAction(action, true);
    }
    
    private void updateCaptionStyle(boolean edit) {
      header.removeCaptionStyle(getFormStyle(STYLE_FORM_CAPTION, !edit));
      header.addCaptionStyle(getFormStyle(STYLE_FORM_CAPTION, edit));
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

  private static final String STYLE_FORM_CONTAINER = "bee-GridFormContainer";
  private static final String STYLE_FORM_HEADER = "bee-GridFormHeader";
  private static final String STYLE_FORM_CAPTION = "bee-GridFormCaption";

  private static final String SUFFIX_EDIT = "-edit";
  private static final String SUFFIX_NEW_ROW = "-newRow";

  private static final String DEFAULT_NEW_ROW_CAPTION = "New Row";
  
  private GridPresenter viewPresenter = null;

  private ChangeHandler filterChangeHandler = null;
  private final FilterUpdater filterUpdater = new FilterUpdater();

  private final CellGrid grid = new CellGrid();

  private Evaluator rowEditable = null;
  private Evaluator rowValidation = null;

  private final Map<String, EditableColumn> editableColumns = Maps.newLinkedHashMap();

  private final Notification notification = new Notification();

  private boolean enabled = true;

  private List<BeeColumn> dataColumns = null;
  private final Set<RelationInfo> relations = Sets.newHashSet();

  private String relColumn = null;
  private long relId = BeeConst.UNDEF;

  private List<String> newRowColumns = null;
  private String newRowCaption = null;
  private RowEditor newRowWidget = null;
  private final NewRowCallback newRowCallback = new NewRowCallback();

  private FormView newRowForm = null;
  private String newRowFormContainerId = null;
  private boolean newRowFormInitialized = false;

  private FormView editForm = null;
  private boolean editMode = false;
  private Evaluator editMessage = null;
  private boolean editShowId = false;
  private final Set<String> editInPlace = Sets.newHashSet();

  private String editFormContainerId = null;
  private boolean editFormInitialized = false;

  private boolean singleForm = false;
  private boolean adding = false;

  private GridCallback gridCallback = null;
  
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

  public HandlerRegistration addEditFormHandler(EditFormEvent.Handler handler) {
    return addHandler(handler, EditFormEvent.getType());
  }

  public HandlerRegistration addReadyForInsertHandler(ReadyForInsertEvent.Handler handler) {
    return addHandler(handler, ReadyForInsertEvent.getType());
  }

  public HandlerRegistration addReadyForUpdateHandler(ReadyForUpdateEvent.Handler handler) {
    return addHandler(handler, ReadyForUpdateEvent.getType());
  }

  public HandlerRegistration addSaveChangesHandler(SaveChangesEvent.Handler handler) {
    return addHandler(handler, SaveChangesEvent.getType());
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
          getGrid().autoFit(true);
          msg = "autoFit";
        }

      } else if (cmd.startsWith("ps")) {
        if (xp[0] > 0) {
          getGrid().setPageSize(xp[0], true, true);
          msg = "updatePageSize " + xp[0];
        } else {
          int oldPageSize = getGrid().getPageSize();
          int newPageSize = getGrid().estimatePageSize();
          if (newPageSize > 0) {
            getGrid().setPageSize(newPageSize, true, true);
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
      getGrid().refresh(true);
    }
  }

  public void create(List<BeeColumn> dataCols, int rowCount, BeeRowSet rowSet,
      GridDescription gridDescr, GridCallback callback, boolean hasSearch) {
    Assert.notEmpty(dataCols);
    Assert.notNull(gridDescr);
    
    setGridCallback(callback);
    if (callback != null) {
      callback.beforeCreate(dataCols, rowCount, gridDescr, hasSearch);
    }

    setDataColumns(dataCols);

    boolean hasHeaders = !BeeUtils.isFalse(gridDescr.hasHeaders());
    boolean hasFooters = hasSearch;
    if (hasFooters && BeeUtils.isFalse(gridDescr.hasFooters())) {
      hasFooters = false;
    }

    boolean showColumnWidths = false;
    List<ColumnDescription> columnDescriptions = null;
    
    if (gridDescr.getStyleSheets() != null) {
      Global.addStyleSheets(gridDescr.getStyleSheets());
    }

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

    if (BeeUtils.isTrue(gridDescr.showColumnWidths())) {
      showColumnWidths = true;
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
    if (callback != null) {
      callback.beforeCreateColumns(dataCols, columnDescriptions);
    }

    String idName = gridDescr.getIdName();
    String versionName = gridDescr.getVersionName();

    AbstractColumn<?> column;
    ColumnHeader header = null;
    ColumnFooter footer = null;

    int dataIndex;
    RelationInfo relationInfo;

    for (ColumnDescription columnDescr : columnDescriptions) {
      String columnId = columnDescr.getName();
      if (callback != null &&
          !callback.beforeCreateColumn(columnId, dataCols, columnDescr)) {
        continue;
      }

      ColType colType = columnDescr.getColType();
      if (BeeUtils.isEmpty(columnId) || colType == null) {
        continue;
      }

      String caption = BeeUtils.ifString(columnDescr.getCaption(), columnId);

      String source = columnDescr.getSource();
      if (!BeeUtils.isEmpty(source)) {
        source = DataUtils.getColumnName(source, dataCols, idName, versionName);
        if (BeeUtils.isEmpty(source)) {
          BeeKeeper.getLog().warning("columnId:", columnId, "source:", columnDescr.getSource(),
              "source not found");
          continue;
        }
      }

      CellType cellType = columnDescr.getCellType();

      dataIndex = BeeConst.UNDEF;
      column = null;

      switch (colType) {
        case ID:
          column = new RowIdColumn();
          source = idName;
          break;

        case VERSION:
          column = new RowVersionColumn();
          source = versionName;
          break;

        case DATA:
        case RELATED:
          for (int i = 0; i < dataCols.size(); i++) {
            BeeColumn dataColumn = dataCols.get(i);
            if (BeeUtils.same(source, dataColumn.getId())) {
              column = GridFactory.createColumn(dataColumn, i, cellType);

              if (ColType.RELATED.equals(colType)) {
                relationInfo = RelationInfo.create(dataCols, columnDescr);
              } else {
                relationInfo = null;
              }

              EditableColumn editableColumn =
                  new EditableColumn(dataCols, i, relationInfo, column, caption, columnDescr);
              editableColumn.setNotificationListener(this);
              getEditableColumns().put(BeeUtils.normalize(columnId), editableColumn);

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
          if (calc != null) {
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
          
        case SELECTION:
          column = new SelectionColumn(getGrid());
          source = null;
          break;
      }

      if (column == null) {
        BeeKeeper.getLog().warning("cannot create column:", columnId, colType);
        continue;
      }

      if (!BeeUtils.isEmpty(columnDescr.getSearchBy())) {
        column.setSearchBy(DataUtils.parseColumns(columnDescr.getSearchBy(), dataCols,
            idName, versionName));
      } else if (!BeeUtils.isEmpty(source)) {
        column.setSearchBy(Lists.newArrayList(source));
      }

      if (!BeeUtils.isEmpty(columnDescr.getSortBy())) {
        column.setSortBy(DataUtils.parseColumns(columnDescr.getSortBy(), dataCols,
            idName, versionName));
      } else if (!BeeUtils.isEmpty(source)) {
        column.setSortBy(Lists.newArrayList(source));
      }

      if (BeeUtils.isTrue(columnDescr.isSortable()) && !BeeUtils.isEmpty(column.getSortBy())) {
        column.setSortable(true);
      }

      if (!BeeUtils.isEmpty(columnDescr.getFormat())) {
        Format.setFormat(column, column.getValueType(), columnDescr.getFormat());
      }
      if (!BeeUtils.isEmpty(columnDescr.getHorAlign())) {
        UiHelper.setHorizontalAlignment(column, columnDescr.getHorAlign());
      }

      if (hasHeaders) {
        boolean showWidth = showColumnWidths;
        if (columnDescr.showWidth() != null) {
          showWidth = columnDescr.showWidth();
        }
        header = new ColumnHeader(columnId, caption, showWidth);
      }

      if (hasFooters && BeeUtils.isTrue(columnDescr.hasFooter())
          && !BeeUtils.isEmpty(column.getSearchBy())) {
        footer = new ColumnFooter(column.getSearchBy(), getFilterUpdater());
      } else {
        footer = null;
      }

      if (callback != null &&
          !callback.afterCreateColumn(columnId, column, header, footer)) {
        continue;
      }

      getGrid().addColumn(columnId, dataIndex, source, column, header, footer);
      getGrid().setColumnInfo(columnId, columnDescr, dataCols);
    }

    if (callback != null) {
      callback.afterCreateColumns(getGrid());
    }

    initNewRowColumns(gridDescr.getNewRowColumns());
    setNewRowCaption(BeeUtils.ifString(gridDescr.getNewRowCaption(), DEFAULT_NEW_ROW_CAPTION));

    getGrid().setRowCount(rowCount, false);

    if (rowSet != null && !rowSet.isEmpty()) {
      getGrid().setRowData(rowSet.getRows().getList(), false);
      getGrid().estimateColumnWidths();
    }
    getGrid().estimateHeaderWidths(true);

    initOrder(gridDescr.getOrder());

    getGrid().addEditStartHandler(this);

    add(getGrid());
    add(getNotification());
    
    setEditMode(BeeUtils.unbox(gridDescr.getEditMode()));

    String editFormName = gridDescr.getEditForm();
    String newRowFormName = gridDescr.getNewRowForm();

    setSingleForm(!BeeUtils.isEmpty(editFormName) && BeeUtils.same(newRowFormName, editFormName));

    if (!BeeUtils.isEmpty(editFormName)) {
      FormFactory.createFormView(editFormName, dataCols, new FormFactory.FormViewCallback() {
        public void onFailure(String[] reason) {
          notifyWarning(reason);
        }

        public void onSuccess(FormView result) {
          if (result != null) {
            String containerId = createFormContainer(result, true, null, getGrid().isReadOnly(),
                hasEditMode());
            setEditFormContainerId(containerId);
            setEditForm(result);

            if (isSingleForm()) {
              setNewRowFormContainerId(containerId);
            }
            result.addEditFormHandler(CellGridImpl.this);
          }
        }
      });

      if (gridDescr.getEditMessage() != null) {
        setEditMessage(Evaluator.create(gridDescr.getEditMessage(), null, dataCols));
      }
      setEditShowId(BeeUtils.unbox(gridDescr.getEditShowId()));

      if (!BeeUtils.isEmpty(gridDescr.getEditInPlace())) {
        getEditInPlace().addAll(BeeUtils.toList(gridDescr.getEditInPlace()));
      }
    }

    if (!BeeUtils.isEmpty(newRowFormName) && !isSingleForm()) {
      FormFactory.createFormView(newRowFormName, dataCols, new FormFactory.FormViewCallback() {
        public void onFailure(String[] reason) {
          notifyWarning(reason);
        }

        public void onSuccess(FormView result) {
          if (result != null) {
            String containerId = createFormContainer(result, false, getNewRowCaption(),
                getGrid().isReadOnly(), hasEditMode());
            setNewRowFormContainerId(containerId);
            setNewRowForm(result);
          }
        }
      });
    }

    if (callback != null) {
      callback.afterCreate(getGrid());
    }
  }

  public int estimatePageSize(int containerWidth, int containerHeight) {
    return getGrid().estimatePageSize(containerWidth, containerHeight, true);
  }

  public void finishNewRow(IsRow row) {
    if (useFormForInsert()) {
      showForm(false, false);
      if (isSingleForm()) {
        getForm(false).showChildren(true);
      }
    } else {
      StyleUtils.hideDisplay(getNewRowWidget());
      StyleUtils.hideScroll(this, ScrollBars.BOTH);
    }

    fireEvent(new AddEndEvent());
    setAdding(false);

    showGrid(true);
    getGrid().setEditing(false);

    if (row == null) {
      getGrid().refocus();
    } else {
      getGrid().insertRow(row);
    }
  }

  public IsRow getActiveRowData() {
    return getGrid().getActiveRowData();
  }

  public Filter getFilter(List<? extends IsColumn> columns, String idColumnName,
      String versionColumnName) {
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

      List<String> sources = ((ColumnFooter) footer).getSources();
      if (BeeUtils.isEmpty(sources)) {
        continue;
      }

      Filter flt = null;
      for (String source : sources) {
        Filter f = DataUtils.parseExpression(source + " " + input, columns, idColumnName,
            versionColumnName);
        if (f == null) {
          continue;
        }

        if (flt == null) {
          flt = f;
        } else {
          flt = Filter.or(flt, f);
        }
      }

      if (flt == null) {
        continue;
      }
      if (filter == null) {
        filter = flt;
      } else {
        filter = Filter.and(filter, flt);
      }
    }
    return filter;
  }

  public CellGrid getGrid() {
    return grid;
  }

  public GridCallback getGridCallback() {
    return gridCallback;
  }

  public String getRelColumn() {
    return relColumn;
  }

  public long getRelId() {
    return relId;
  }

  public Collection<RowInfo> getSelectedRows() {
    return getGrid().getSelectedRows().values();
  }

  public GridPresenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
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

  public void onEditForm(EditFormEvent event) {
    Assert.notNull(event);
    if (event.isCanceled()) {
      formCancel();
    } else if (event.isPending()) {
      getViewPresenter().requery(true);
      closeEditForm();
    } else {
      closeEditForm();
    }
  }

  public void onEditStart(EditStartEvent event) {
    if (!isEnabled()) {
      return;
    }
    Assert.notNull(event);
    String columnId = event.getColumnId();

    boolean useForm = useFormForEdit(columnId);
    if (!useForm && event.isReadOnly()) {
      return;
    }

    EditableColumn editableColumn = getEditableColumn(columnId);
    if (editableColumn == null && !useForm) {
      return;
    }

    IsRow rowValue = event.getRowValue();
    if (!isRowEditable(rowValue, true)) {
      return;
    }
    if (editableColumn != null && !editableColumn.isCellEditable(rowValue, true)) {
      return;
    }

    if (useForm) {
      fireEvent(new EditFormEvent(State.OPEN));
      showGrid(false);
      showForm(true, true);

      GridFormPresenter presenter = (GridFormPresenter) getEditForm().getViewPresenter();
      if (isSingleForm()) {
        presenter.setCaption(getEditForm().getCaption());
        presenter.updateCaptionStyle(true);
      }
      updateEditFormMessage(presenter, rowValue);

      if (hasEditMode()) {
        presenter.showAction(Action.EDIT);
        getEditForm().setEnabled(false);
      }
      
      IsRow row = cloneRow(rowValue);
      getEditForm().updateRow(row, true);
      
      if (editableColumn != null && !hasEditMode()) {
        Widget widget = getEditForm().getWidgetBySource(editableColumn.getColumnId());
        if (widget instanceof Focusable && widget.isVisible()) {
          ((Focusable) widget).setFocus(true);
        }
      }
      
      if (getEditForm().getFormCallback() != null) {
        getEditForm().getFormCallback().onStartEdit(getEditForm(), row);
      }
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

  public void setRelColumn(String relColumn) {
    this.relColumn = relColumn;
  }

  public void setRelId(long relId) {
    this.relId = relId;
  }

  public void setViewPresenter(Presenter presenter) {
    if (presenter instanceof GridPresenter) {
      this.viewPresenter = (GridPresenter) presenter;
    } else if (presenter == null) {
      this.viewPresenter = null;
    }
  }

  public void startNewRow() {
    if (!isEnabled()) {
      return;
    }
    
    boolean useForm = useFormForInsert();

    if (!useForm && getNewRowColumns() == null) {
      List<String> columnList = Lists.newArrayList();
      boolean ok;
      for (Map.Entry<String, EditableColumn> entry : getEditableColumns().entrySet()) {
        String key = entry.getKey();
        EditableColumn editableColumn = entry.getValue();
        String columnId = editableColumn.getColumnId();

        if (!BeeUtils.isEmpty(getRelColumn()) && BeeUtils.same(getRelColumn(), columnId)) {
          ok = false;
        } else if (isRelSource(columnId)) {
          ok = !editableColumn.isNullable() && isForeign(columnId);
        } else if (isForeign(columnId) && isForeign(getRelSource(columnId))) {
          ok = false;
        } else {
          ok = !editableColumn.isNullable() || !getGrid().isColumnReadOnly(key);
        }

        if (ok) {
          columnList.add(key);
        }
      }
      setNewRowColumns(columnList);
    }
    if (!useForm && getNewRowColumns().isEmpty()) {
      notifyWarning("startNewRow:", "new row columns not available");
      return;
    }

    IsRow oldRow = null;
    IsRow newRow = createEmptyRow();

    for (EditableColumn editableColumn : getEditableColumns().values()) {
      if (!editableColumn.hasCarry()) {
        continue;
      }
      if (oldRow == null) {
        oldRow = getGrid().getActiveRowData();
        if (oldRow == null) {
          oldRow = createEmptyRow();
        }
      }

      String carry = editableColumn.getCarryValue(oldRow);
      if (!BeeUtils.isEmpty(carry)) {
        newRow.setValue(editableColumn.getColIndex(), carry);
      }
    }

    if (getGridCallback() != null && !getGridCallback().onStartNewRow(this, oldRow, newRow)) {
      return;
    }
    
    getGrid().setEditing(true);
    showGrid(false);
    StyleUtils.autoScroll(this, ScrollBars.BOTH);
    
    fireEvent(new AddStartEvent(null));

    setAdding(true);

    if (!useForm && getNewRowWidget() == null) {
      List<EditableColumn> columns = Lists.newArrayList();
      for (String columnId : getNewRowColumns()) {
        columns.add(getEditableColumn(columnId));
      }

      setNewRowWidget(new RowEditor(getNewRowCaption(), getDataColumns(), getRelations(), columns,
          getNewRowCallback(), this, getElement(), this));
      add(getNewRowWidget());
    }

    if (useForm) {
      showForm(false, true);
      FormView form = getForm(false);
      if (form.getFormCallback() != null) {
        form.getFormCallback().onStartNewRow(form, oldRow, newRow);
      }

      if (isSingleForm() && form.getViewPresenter() instanceof GridFormPresenter) {
        GridFormPresenter presenter = (GridFormPresenter) form.getViewPresenter();
        presenter.setCaption(getNewRowCaption());
        presenter.setMessage(null);
        presenter.updateCaptionStyle(false);
        if (hasEditMode()) {
          presenter.hideAction(Action.EDIT);
          form.setEnabled(true);
        }
      }

      form.showChildren(false);
      form.updateRow(newRow, false);
    } else {
      getNewRowWidget().start(newRow);
      StyleUtils.unhideDisplay(getNewRowWidget());
    }
  }
  
  private boolean checkNewRow(IsRow row) {
    boolean ok = true;
    int count = 0;

    if (!useFormForInsert() && getNewRowColumns() == null) {
      notifySevere("New Row", "columns not available");
      ok = false;
    }

    if (ok) {
      List<String> captions = Lists.newArrayList();
      List<String> columns = Lists.newArrayList();

      if (useFormForInsert()) {
        columns.addAll(getEditableColumns().keySet());
      } else {
        columns.addAll(getNewRowColumns());
      }

      for (String columnId : columns) {
        EditableColumn editableColumn = getEditableColumn(columnId);
        String value = row.getString(editableColumn.getColIndex());
        if (BeeUtils.isEmpty(value)) {
          if (!editableColumn.isNullable()) {
            captions.add(getGrid().getColumnCaption(columnId));
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

  private IsRow cloneRow(IsRow original) {
    String[] arr = new String[getDataColumns().size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = original.getString(i);
    }

    IsRow result = new BeeRow(original.getId(), arr);
    result.setVersion(original.getVersion());

    return result;
  }

  private void closeEditForm() {
    showForm(true, false);
    showGrid(true);
    fireEvent(new EditFormEvent(State.CLOSED));
    getGrid().refocus();
  }

  private IsRow createEmptyRow() {
    String[] arr = new String[getDataColumns().size()];
    return new BeeRow(0, arr);
  }

  private String createFormContainer(FormView formView, boolean edit, String defaultCaption,
      boolean readOnly, boolean editable) {
    HeaderView formHeader = new HeaderImpl();
    formHeader.asWidget().addStyleName(STYLE_FORM_HEADER);
    formHeader.asWidget().addStyleName(getFormStyle(STYLE_FORM_HEADER, edit));

    String caption = BeeUtils.ifString(formView.getCaption(), defaultCaption);
    EnumSet<Action> actions;
    if (!edit) {
      actions = EnumSet.of(Action.SAVE, Action.CLOSE);
    } else if (readOnly) {
      actions = EnumSet.of(Action.CLOSE);
    } else if (editable) {
      actions = EnumSet.of(Action.EDIT, Action.SAVE, Action.CLOSE);
    } else {
      actions = EnumSet.of(Action.SAVE, Action.CLOSE);
    }
    formHeader.create(caption, false, false, null, actions, null);
    formHeader.addCaptionStyle(STYLE_FORM_CAPTION);
    formHeader.addCaptionStyle(getFormStyle(STYLE_FORM_CAPTION , edit));

    Split container = new Split(0);
    StyleUtils.makeAbsolute(container);
    container.addStyleName(STYLE_FORM_CONTAINER);
    container.addStyleName(getFormStyle(STYLE_FORM_CONTAINER, edit));

    container.addNorth(formHeader.asWidget(), formHeader.getHeight());
    container.add(formView.asWidget());

    add(container);

    container.setVisible(false);
    formView.setEditing(true);

    GridFormPresenter presenter = new GridFormPresenter(formHeader);
    formHeader.setViewPresenter(presenter);
    formView.setViewPresenter(presenter);
    
    return container.getId();
  }

  private void formCancel() {
    if (isAdding()) {
      finishNewRow(null);
    } else {
      closeEditForm();
    }
  }

  private void formConfirm() {
    FormView form = getForm(!isAdding());
    IsRow row = form.getRow();

    if (isAdding()) {
      if (form.getFormCallback() != null && 
          !form.getFormCallback().onPrepareForInsert(form, this, row)) {
        return;
      }
      // TODO if (checkNewRow(row)) {
      prepareForInsert(row);
      // }
    } else {
      closeEditForm();
      saveChanges(row);
    }
  }

  private List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  private EditableColumn getEditableColumn(String columnId) {
    if (BeeUtils.isEmpty(columnId)) {
      return null;
    }
    return getEditableColumns().get(BeeUtils.normalize(columnId));
  }

  private Map<String, EditableColumn> getEditableColumns() {
    return editableColumns;
  }

  private FormView getEditForm() {
    return editForm;
  }

  private String getEditFormContainerId() {
    return editFormContainerId;
  }

  private Set<String> getEditInPlace() {
    return editInPlace;
  }

  private Evaluator getEditMessage() {
    return editMessage;
  }

  private boolean getEditShowId() {
    return editShowId;
  }

  private ChangeHandler getFilterChangeHandler() {
    return filterChangeHandler;
  }
  
  private FilterUpdater getFilterUpdater() {
    return filterUpdater;
  }

  private FormView getForm(boolean edit) {
    if (edit || isSingleForm()) {
      return getEditForm();
    } else {
      return getNewRowForm();
    }
  }

  private String getFormStyle(String base, boolean edit) {
    return base + (edit ? SUFFIX_EDIT : SUFFIX_NEW_ROW);
  }

  private NewRowCallback getNewRowCallback() {
    return newRowCallback;
  }

  private String getNewRowCaption() {
    return newRowCaption;
  }

  private List<String> getNewRowColumns() {
    return newRowColumns;
  }

  private FormView getNewRowForm() {
    return newRowForm;
  }

  private String getNewRowFormContainerId() {
    return newRowFormContainerId;
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

  private String getRelSource(String columnId) {
    if (BeeUtils.isEmpty(columnId) || BeeUtils.isEmpty(getRelations())) {
      return null;
    }

    for (RelationInfo relationInfo : getRelations()) {
      if (BeeUtils.same(relationInfo.getSource(), columnId)) {
        return relationInfo.getRelSource();
      }
    }
    return null;
  }

  private Evaluator getRowEditable() {
    return rowEditable;
  }

  private Evaluator getRowValidation() {
    return rowValidation;
  }

  private boolean hasEditMode() {
    return editMode;
  }

  private void initNewRowColumns(String columnNames) {
    if (getEditableColumns().isEmpty() || BeeUtils.isEmpty(columnNames)) {
      return;
    }

    List<String> columnList = Lists.newArrayList();
    for (String colName : BeeUtils.NAME_SPLITTER.split(columnNames)) {
      if (BeeUtils.isEmpty(colName)) {
        continue;
      }

      String id = null;
      if (getEditableColumns().containsKey(BeeUtils.normalize(colName))) {
        id = colName;
      }
      if (BeeUtils.isEmpty(id)) {
        BeeKeeper.getLog().warning("newRowColumn", colName, "is not editable");
        continue;
      }

      if (!BeeUtils.containsSame(columnList, id)) {
        columnList.add(id);
      }
    }

    if (!columnList.isEmpty()) {
      setNewRowColumns(columnList);
    }
  }

  private void initOrder(Order viewOrder) {
    if (viewOrder == null) {
      return;
    }

    Order gridOrder = getGrid().getSortOrder();
    if (!gridOrder.isEmpty()) {
      gridOrder.clear();
    }

    for (Order.Column oc : viewOrder.getColumns()) {
      String columnId = getGrid().getColumnIdBySource(oc.getName());
      if (!BeeUtils.isEmpty(columnId)) {
        gridOrder.add(columnId, oc.getSources(), oc.isAscending());
      }
    }
  }

  private boolean isAdding() {
    return adding;
  }

  private boolean isEditFormInitialized() {
    return editFormInitialized;
  }

  private boolean isForeign(String columnId) {
    return !BeeUtils.isEmpty(getRelSource(columnId));
  }

  private boolean isNewRowFormInitialized() {
    return newRowFormInitialized;
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

  private boolean isSingleForm() {
    return singleForm;
  }

  private void prepareForInsert(IsRow row) {
    List<BeeColumn> columns = Lists.newArrayList();
    List<String> values = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      if (!BeeUtils.isEmpty(getRelColumn())
          && BeeUtils.same(getRelColumn(), getDataColumns().get(i).getId())) {
        columns.add(getDataColumns().get(i));
        values.add(BeeUtils.toString(getRelId()));
        continue;
      }

      String value = row.getString(i);
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

  private void saveChanges(IsRow newRow) {
    long rowId = newRow.getId();
    IsRow oldRow = getGrid().getRowById(rowId);
    if (oldRow == null) {
      notifyWarning("Old row not found", "id = " + rowId);
      return;
    }

    String oldValue;
    String newValue;

    List<BeeColumn> columns = Lists.newArrayList();
    List<String> oldValues = Lists.newArrayList();
    List<String> newValues = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      oldValue = oldRow.getString(i);
      newValue = newRow.getString(i);

      if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
        getGrid().preliminaryUpdate(rowId, getDataColumns().get(i).getId(), newValue);
        columns.add(getDataColumns().get(i));
        oldValues.add(oldValue);
        newValues.add(newValue);
      }
    }

    if (columns.size() > 0) {
      fireEvent(new SaveChangesEvent(rowId, newRow.getVersion(), columns, oldValues, newValues));
    }
  }

  private void setAdding(boolean adding) {
    this.adding = adding;
  }

  private void setDataColumns(List<BeeColumn> dataColumns) {
    this.dataColumns = dataColumns;
  }

  private void setEditForm(FormView editForm) {
    this.editForm = editForm;
  }

  private void setEditFormContainerId(String editFormContainerId) {
    this.editFormContainerId = editFormContainerId;
  }

  private void setEditFormInitialized(boolean editFormInitialized) {
    this.editFormInitialized = editFormInitialized;
  }

  private void setEditMessage(Evaluator editMessage) {
    this.editMessage = editMessage;
  }

  private void setEditMode(boolean editMode) {
    this.editMode = editMode;
  }

  private void setEditShowId(boolean editShowId) {
    this.editShowId = editShowId;
  }

  private void setFilterChangeHandler(ChangeHandler filterChangeHandler) {
    this.filterChangeHandler = filterChangeHandler;
  }

  private void setGridCallback(GridCallback gridCallback) {
    this.gridCallback = gridCallback;
  }

  private void setNewRowCaption(String newRowCaption) {
    this.newRowCaption = newRowCaption;
  }

  private void setNewRowColumns(List<String> newRowColumns) {
    this.newRowColumns = newRowColumns;
  }

  private void setNewRowForm(FormView newRowForm) {
    this.newRowForm = newRowForm;
  }

  private void setNewRowFormContainerId(String newRowFormContainerId) {
    this.newRowFormContainerId = newRowFormContainerId;
  }

  private void setNewRowFormInitialized(boolean newRowFormInitialized) {
    this.newRowFormInitialized = newRowFormInitialized;
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

  private void setSingleForm(boolean singleForm) {
    this.singleForm = singleForm;
  }

  private void showForm(boolean edit, boolean show) {
    String containerId = edit ? getEditFormContainerId() : getNewRowFormContainerId();

    if (show) {
      StyleUtils.unhideDisplay(containerId);
      if (edit) {
        if (!isEditFormInitialized()) {
          setEditFormInitialized(true);
          if (isSingleForm()) {
            setNewRowFormInitialized(true);
          }
          getForm(edit).start(null);
        }
      } else {
        if (!isNewRowFormInitialized()) {
          setNewRowFormInitialized(true);
          getForm(edit).start(null);
        }
      }
    } else {
      StyleUtils.hideDisplay(containerId);
    }
  }

  private void showGrid(boolean show) {
    getGrid().setVisible(show);
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

  private void updateEditFormMessage(GridFormPresenter presenter, IsRow row) {
    if (getEditMessage() == null && !getEditShowId()) {
      return;
    }
    
    String message = null;
    if (getEditMessage() != null) {
      getEditMessage().update(row);
      message = getEditMessage().evaluate();
    }
    if (getEditShowId() && row != null) {
      message = BeeUtils.concat(1, message, BeeUtils.bracket(row.getId()));
    }
    
    presenter.setMessage(message);
  }

  private boolean useFormForEdit(String columnId) {
    if (getEditForm() == null) {
      return false;
    }
    if (BeeUtils.isEmpty(columnId) || getEditInPlace().isEmpty()) {
      return true;
    }
    return !BeeUtils.containsSame(getEditInPlace(), columnId);
  }
  
  private boolean useFormForInsert() {
    return getForm(false) != null;
  }
}
