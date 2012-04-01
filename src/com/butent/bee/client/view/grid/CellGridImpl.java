package com.butent.bee.client.view.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Stacking;
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
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.ui.ConditionalStyle;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.ActionEvent;
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
    EditEndEvent.Handler, ActionEvent.Handler {

  private class FilterUpdater implements ValueUpdater<String> {
    public void update(String value) {
      if (getFilterChangeHandler() != null) {
        getFilterChangeHandler().onChange(null);
      }
    }
  }

  private class ModalForm extends Popup {

    private ModalForm(Widget widget, HasDimensions content) {
      super(false);

      if (Stacking.getWidgetCount() <= 0) {
        enableGlass();
      }
      setAnimationEnabled(true);

      widget.addStyleName(getDefaultStyleName() + "-content");
      setWidget(widget);

      if (content != null) {
        setDimensions(content);
      }
    }

    @Override
    public String getIdPrefix() {
      return "modal-form";
    }

    @Override
    protected String getDefaultStyleName() {
      return "bee-ModalForm";
    }

    private void close() {
      hide();
    }

    private void open() {
      center();
    }

    private void setDimensions(HasDimensions content) {
      double v;
      Unit u;

      if (content.getWidthValue() != null) {
        v = content.getWidthValue();
        u = Dimensions.normalizeUnit(content.getWidthUnit());
        if (Unit.PCT.equals(u)) {
          v = Window.getClientWidth() * v / 100;
          u = Unit.PX;
        }
      } else {
        v = Window.getClientWidth() / 2;
        u = Unit.PX;
      }
      if (v > 0) {
        getElement().getStyle().setWidth(v, u);
      }

      if (content.getHeightValue() != null) {
        v = content.getHeightValue();
        u = Dimensions.normalizeUnit(content.getHeightUnit());
        if (Unit.PCT.equals(u)) {
          v = Window.getClientHeight() * v / 100;
          u = Unit.PX;
        }
      } else {
        v = Window.getClientHeight() / 2;
        u = Unit.PX;
      }
      if (v > 0) {
        getElement().getStyle().setHeight(v, u);
      }
    }
  }

  private class NewRowCallback implements RowEditor.Callback {
    public void onCancel() {
      finishNewRow(null);
    }

    public void onConfirm(IsRow row) {
      if (validateRow(row, true)) {
        prepareForInsert(row);
      }
    }
  }

  private static final String DEFAULT_NEW_ROW_CAPTION = "New Row";

  private final String gridName;

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
  private boolean editSave = false;
  private Evaluator editMessage = null;
  private boolean editShowId = false;
  private final Set<String> editInPlace = Sets.newHashSet();
  private boolean editNewRow = false;

  private String editFormContainerId = null;
  private boolean editFormInitialized = false;

  private boolean singleForm = false;
  private boolean adding = false;
  private String activeFormContainerId = null;

  private boolean showNewRowPopup = false;
  private boolean showEditPopup = false;

  private ModalForm newRowPopup = null;
  private ModalForm editPopup = null;

  private GridCallback gridCallback = null;

  public CellGridImpl(String gridName) {
    super();
    this.gridName = gridName;
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

    Set<String> footerEvents;
    if (hasFooters) {
      footerEvents = BeeUtils.toSet(gridDescr.getFooterEvents());
    } else {
      footerEvents = null;
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
          for (int i = 0; i < dataCols.size(); i++) {
            BeeColumn dataColumn = dataCols.get(i);
            if (BeeUtils.same(source, dataColumn.getId())) {
              AbstractCellRenderer renderer =
                  RendererFactory.getRenderer(columnDescr.getRendererDescription(),
                      columnDescr.getRender(), dataCols, i);
              
              if (renderer == null) {
                column = GridFactory.createColumn(dataColumn, i, cellType);
              } else {
                column = GridFactory.createRenderableColumn(renderer, dataColumn, i, cellType);
              }

              if (columnDescr.isForeign()) {
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
        footer = new ColumnFooter(column.getSearchBy(), footerEvents, getFilterUpdater());
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
    setEditSave(BeeUtils.unbox(gridDescr.getEditSave()));

    String editFormName = gridDescr.getEditForm();
    String newRowFormName = gridDescr.getNewRowForm();

    setShowEditPopup(BeeUtils.unbox(gridDescr.getEditPopup()));
    setShowNewRowPopup(BeeUtils.unbox(gridDescr.getNewRowPopup())
        && !BeeUtils.isEmpty(newRowFormName));

    setSingleForm(!BeeUtils.isEmpty(editFormName) && BeeUtils.same(newRowFormName, editFormName));

    if (!BeeUtils.isEmpty(editFormName)) {
      FormFactory.createFormView(editFormName, dataCols, new FormFactory.FormViewCallback() {
        public void onFailure(String[] reason) {
          notifyWarning(reason);
        }

        public void onSuccess(FormView result) {
          if (result != null) {
            String containerId = createFormContainer(result, true, null, showEditPopup());
            setEditFormContainerId(containerId);
            setEditForm(result);

            if (isSingleFormInstance()) {
              setNewRowFormContainerId(containerId);
            }
            result.addActionHandler(CellGridImpl.this);
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
      setEditNewRow(BeeUtils.unbox(gridDescr.getEditNewRow()));
    }

    if (!BeeUtils.isEmpty(newRowFormName) && !isSingleFormInstance()) {
      FormFactory.createFormView(newRowFormName, dataCols, new FormFactory.FormViewCallback() {
        public void onFailure(String[] reason) {
          notifyWarning(reason);
        }

        public void onSuccess(FormView result) {
          if (result != null) {
            String containerId = createFormContainer(result, false, getNewRowCaption(),
                showNewRowPopup());
            setNewRowFormContainerId(containerId);
            setNewRowForm(result);

            result.addActionHandler(CellGridImpl.this);
          }
        }
      });
    }

    if (callback != null) {
      callback.afterCreate(getGrid());
    }
  }

  public void editRow(IsRow row, boolean enable) {
    if (row != null && getEditForm() != null) {
      onEditStart(new EditStartEvent(row, null, null, BeeConst.UNDEF, false));
      if (enable && !getEditForm().isEnabled()) {
        getEditForm().getViewPresenter().handleAction(Action.EDIT);
      }
    }
  }

  public void ensureGridVisible() {
    if (!BeeUtils.isEmpty(getActiveFormContainerId())) {
      formCancel();
    }
  }

  public int estimatePageSize(int containerWidth, int containerHeight) {
    return getGrid().estimatePageSize(containerWidth, containerHeight, true);
  }

  public void finishNewRow(IsRow row) {
    if (useFormForInsert()) {
      showForm(false, false);
      if (isSingleFormInstance()) {
        getForm(false).showChildren(true);
      }

    } else {
      if (showNewRowPopup()) {
        getNewRowPopup().close();
      } else {
        StyleUtils.hideDisplay(getNewRowWidget());
        StyleUtils.hideScroll(this, ScrollBars.BOTH);
        showGrid(true);
      }
      setActiveFormContainerId(null);
    }

    fireEvent(new AddEndEvent(showNewRowPopup()));
    setAdding(false);

    getGrid().setEditing(false);

    if (row == null) {
      getGrid().refocus();
    } else {
      getGrid().insertRow(row);
      if (editNewRow()) {
        editRow(row, true);
      }
    }
  }

  public void formCancel() {
    if (isAdding()) {
      finishNewRow(null);
    } else {
      closeEditForm();
    }
  }

  public void formConfirm() {
    FormView form = getForm(!isAdding());
    IsRow row = form.getRow();

    if (isAdding()) {
      if (form.getFormCallback() != null &&
          !form.getFormCallback().onPrepareForInsert(form, this, row)) {
        return;
      }
      if (validateRow(row, form, true)) {
        prepareForInsert(row);
      }

    } else {
      if (validateRow(row, form, false)) {
        closeEditForm();
        saveChanges(row);
      }
    }
  }

  public IsRow getActiveRowData() {
    return getGrid().getActiveRowData();
  }

  public Filter getFilter(List<? extends IsColumn> columns, String idColumnName,
      String versionColumnName) {
    List<ColumnFooter> footers = getGrid().getFooters();

    if (footers == null || footers.isEmpty()) {
      return null;
    }
    Filter filter = null;

    for (ColumnFooter footer : footers) {
      if (footer == null) {
        continue;
      }
      String input = BeeUtils.trim(footer.getValue());
      if (BeeUtils.isEmpty(input)) {
        continue;
      }

      List<String> sources = footer.getSources();
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

  public FormView getForm(boolean edit) {
    if (edit || isSingleFormInstance()) {
      return getEditForm();
    } else {
      return getNewRowForm();
    }
  }

  public CellGrid getGrid() {
    return grid;
  }

  public GridCallback getGridCallback() {
    return gridCallback;
  }

  public String getGridName() {
    return gridName;
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

  public void onAction(ActionEvent event) {
    Assert.notNull(event);
    if (event.contains(Action.REQUERY)) {
      getViewPresenter().requery(true);
    } else if (event.contains(Action.REFRESH)) {
      getViewPresenter().refresh();
    }

    if (!BeeUtils.isEmpty(getActiveFormContainerId())) {
      if (event.contains(Action.SAVE)) {
        formConfirm();
      } else if (event.contains(Action.CLOSE)) {
        formCancel();
      }
    }
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
    if (!isEnabled()) {
      return;
    }
    Assert.notNull(event);

    IsRow rowValue = event.getRowValue();
    String columnId = event.getColumnId();
    EditableColumn editableColumn = getEditableColumn(columnId);

    boolean useForm = useFormForEdit(columnId);
    boolean editable = !isReadOnly();

    if (useForm) {
      if (editable) {
        editable = isRowEditable(rowValue, true);
      }
    } else {
      if (!editable || event.isReadOnly()) {
        return;
      }
      if (!isRowEditable(rowValue, true)) {
        return;
      }
      if (editableColumn == null) {
        return;
      }
      if (!editableColumn.isCellEditable(rowValue, true)) {
        return;
      }
    }

    if (useForm) {
      fireEvent(new EditFormEvent(State.OPEN, showEditPopup()));
      showForm(true, true);

      GridFormPresenter presenter = (GridFormPresenter) getEditForm().getViewPresenter();

      String caption = getRowCaption(rowValue, true);
      if (isSingleForm()) {
        presenter.setCaption(BeeUtils.ifString(caption, getEditForm().getCaption()));
        presenter.updateCaptionStyle(true);
      } else if (!BeeUtils.isEmpty(caption)) {
        presenter.setCaption(caption);
      }
      updateEditFormMessage(presenter, rowValue);

      boolean enableForm = editable;

      if (presenter.hasAction(Action.EDIT)) {
        presenter.showAction(Action.EDIT, editable && hasEditMode());
        if (presenter.hasAction(Action.SAVE)) {
          presenter.hideAction(Action.SAVE);
        }
        enableForm = false;

      } else if (presenter.hasAction(Action.SAVE)) {
        presenter.showAction(Action.SAVE, editable && hasEditSave());
      }

      getEditForm().setEnabled(enableForm);

      IsRow row = cloneRow(rowValue);
      getEditForm().updateRow(row, true);

      if (editableColumn != null && enableForm) {
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
            EditorFactory.START_KEY_ENTER) && !editableColumn.isForeign()) {

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
    if (!isEnabled() || isReadOnly()) {
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
        } else {
          ok = !getGrid().isColumnReadOnly(key);
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

    IsRow oldRow = getGrid().getActiveRowData();
    IsRow newRow = createEmptyRow();

    for (EditableColumn editableColumn : getEditableColumns().values()) {
      if (!editableColumn.hasCarry()) {
        continue;
      }
      if (oldRow == null) {
        oldRow = createEmptyRow();
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

    fireEvent(new AddStartEvent(null, showNewRowPopup()));

    setAdding(true);

    String caption = getRowCaption(newRow, false);

    if (!useForm && getNewRowWidget() == null) {
      List<EditableColumn> columns = Lists.newArrayList();
      for (String columnId : getNewRowColumns()) {
        columns.add(getEditableColumn(columnId));
      }

      setNewRowWidget(new RowEditor(BeeUtils.ifString(caption, getNewRowCaption()),
          getDataColumns(), getRelations(), columns, getNewRowCallback(), this, getElement(), this));

      if (showNewRowPopup()) {
        setNewRowPopup(new ModalForm(getNewRowWidget(), null));
      } else {
        add(getNewRowWidget());
      }
    }

    if (useForm) {
      showForm(false, true);
      FormView form = getForm(false);
      if (form.getFormCallback() != null) {
        form.getFormCallback().onStartNewRow(form, oldRow, newRow);
      }

      if (form.getViewPresenter() instanceof GridFormPresenter) {
        GridFormPresenter presenter = (GridFormPresenter) form.getViewPresenter();

        if (isSingleForm()) {
          presenter.setCaption(BeeUtils.ifString(caption, getNewRowCaption()));
          presenter.setMessage(null);
          presenter.updateCaptionStyle(false);

          if (presenter.hasAction(Action.EDIT)) {
            presenter.hideAction(Action.EDIT);
          }
          if (presenter.hasAction(Action.SAVE)) {
            presenter.showAction(Action.SAVE);
          }
          form.setEnabled(true);
        } else if (!BeeUtils.isEmpty(caption)) {
          presenter.setCaption(caption);
        }
      }

      form.showChildren(false);
      form.updateRow(newRow, false);

      UiHelper.focus(form.asWidget());

    } else {
      getNewRowWidget().start(newRow);

      if (showNewRowPopup()) {
        getNewRowPopup().open();
      } else {
        showGrid(false);
        StyleUtils.autoScroll(this, ScrollBars.BOTH);
        StyleUtils.unhideDisplay(getNewRowWidget());
      }

      setActiveFormContainerId(getNewRowWidget().getId());
    }
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
    fireEvent(new EditFormEvent(State.CLOSED, showEditPopup()));
    getGrid().refocus();
  }

  private IsRow createEmptyRow() {
    String[] arr = new String[getDataColumns().size()];
    return new BeeRow(0, arr);
  }

  private String createFormContainer(FormView formView, boolean edit, String defaultCaption,
      boolean asPopup) {
    String caption = BeeUtils.ifString(formView.getCaption(), defaultCaption);
    EnumSet<Action> actions = EnumSet.of(Action.CLOSE);
    if (!edit) {
      actions.add(Action.SAVE);
    } else if (!isReadOnly()) {
      if (hasEditMode()) {
        actions.add(Action.EDIT);
      }
      if (hasEditSave() || isSingleFormInstance()) {
        actions.add(Action.SAVE);
      }
    }

    GridFormPresenter gfp = new GridFormPresenter(this, formView, caption, actions, edit,
        hasEditSave());
    Widget container = gfp.getWidget();

    if (asPopup) {
      if (edit) {
        setEditPopup(new ModalForm(container, formView));
      } else {
        setNewRowPopup(new ModalForm(container, formView));
      }

    } else {
      add(container);
      container.setVisible(false);
    }

    formView.setEditing(true);
    formView.setViewPresenter(gfp);

    return DomUtils.getId(container);
  }

  private boolean editNewRow() {
    return editNewRow;
  }

  private String getActiveFormContainerId() {
    return activeFormContainerId;
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

  private ModalForm getEditPopup() {
    return editPopup;
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

  private ModalForm getNewRowPopup() {
    return newRowPopup;
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

  private String getRowCaption(IsRow row, boolean edit) {
    if (getGridCallback() == null) {
      return null;
    }
    return getGridCallback().getRowCaption(row, edit);
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

  private boolean hasEditSave() {
    return editSave;
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

  private boolean isNewRowFormInitialized() {
    return newRowFormInitialized;
  }

  private boolean isReadOnly() {
    return getGrid().isReadOnly();
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

  private boolean isSingleFormInstance() {
    return isSingleForm() && !showNewRowPopup() && !showEditPopup();
  }

  private void prepareForInsert(IsRow row) {
    List<BeeColumn> columns = Lists.newArrayList();
    List<String> values = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      BeeColumn dataColumn = getDataColumns().get(i);
      if (!BeeUtils.isEmpty(getRelColumn()) && BeeUtils.same(getRelColumn(), dataColumn.getId())) {
        columns.add(dataColumn);
        values.add(BeeUtils.toString(getRelId()));
        continue;
      }

      String value = row.getString(i);
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      if (!dataColumn.isReadOnly() && !dataColumn.isForeign()) {
        columns.add(getDataColumns().get(i));
        values.add(value);
      }
    }

    if (columns.isEmpty()) {
      notifySevere("New Row", "all columns cannot be empty");
      return;
    }

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

    if (!columns.isEmpty()) {
      fireEvent(new SaveChangesEvent(rowId, newRow.getVersion(), columns, oldValues, newValues));
    }
  }

  private void setActiveFormContainerId(String activeFormContainerId) {
    this.activeFormContainerId = activeFormContainerId;
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

  private void setEditNewRow(boolean editNewRow) {
    this.editNewRow = editNewRow;
  }

  private void setEditPopup(ModalForm editPopup) {
    this.editPopup = editPopup;
  }

  private void setEditSave(boolean editSave) {
    this.editSave = editSave;
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

  private void setNewRowPopup(ModalForm newRowPopup) {
    this.newRowPopup = newRowPopup;
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

  private void setShowEditPopup(boolean showEditPopup) {
    this.showEditPopup = showEditPopup;
  }

  private void setShowNewRowPopup(boolean showNewRowPopup) {
    this.showNewRowPopup = showNewRowPopup;
  }

  private void setSingleForm(boolean singleForm) {
    this.singleForm = singleForm;
  }

  private boolean showEditPopup() {
    return showEditPopup;
  }

  private void showForm(boolean edit, boolean show) {
    String containerId = edit ? getEditFormContainerId() : getNewRowFormContainerId();
    ModalForm popup = edit ? getEditPopup() : getNewRowPopup();

    if (show) {
      if (popup == null) {
        showGrid(false);
        StyleUtils.unhideDisplay(containerId);
      } else {
        popup.open();
      }

      if (edit) {
        if (!isEditFormInitialized()) {
          setEditFormInitialized(true);
          if (isSingleFormInstance()) {
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
      setActiveFormContainerId(containerId);

    } else {
      if (popup == null) {
        StyleUtils.hideDisplay(containerId);
        showGrid(true);
      } else {
        popup.close();
      }
      setActiveFormContainerId(null);
    }
  }

  private void showGrid(boolean show) {
    getGrid().setVisible(show);
  }

  private boolean showNewRowPopup() {
    return showNewRowPopup;
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

  private boolean validateRow(IsRow row, boolean force) {
    return validateRow(row, this, force);
  }

  private boolean validateRow(IsRow row, NotificationListener notificationListener, boolean force) {
    boolean ok = true;
    if (isReadOnly()) {
      return ok;
    }

    IsRow oldRow = getGrid().getActiveRowData();

    String oldValue = null;
    String newValue;
    int index;

    for (Map.Entry<String, EditableColumn> entry : getEditableColumns().entrySet()) {
      if (getGrid().isColumnReadOnly(entry.getKey())) {
        continue;
      }

      EditableColumn ec = entry.getValue();
      index = ec.getIndexForUpdate();
      if (oldRow != null) {
        oldValue = oldRow.getString(index);
      }
      newValue = row.getString(index);

      ok = UiHelper.validateCell(oldValue, newValue, ec.getValidation(), row, index,
          ec.getTypeForUpdate(), ec.isNullable(), ec.getMinValue(), ec.getMaxValue(),
          ec.getCaption(), notificationListener, force);
      if (!ok) {
        break;
      }
    }

    if (ok && getRowValidation() != null) {
      ok = UiHelper.validateRow(row, getRowValidation(), notificationListener);
    }
    return ok;
  }
}
