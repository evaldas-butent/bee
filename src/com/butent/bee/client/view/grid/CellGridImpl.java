package com.butent.bee.client.view.grid;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RelationUtils;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ModalForm;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.logical.ActionEvent;
import com.butent.bee.client.event.logical.DataRequestEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.cell.ActionCell;
import com.butent.bee.client.grid.cell.CalculatedCell;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.grid.column.ActionColumn;
import com.butent.bee.client.grid.column.CalculatedColumn;
import com.butent.bee.client.grid.column.RowIdColumn;
import com.butent.bee.client.grid.column.RowVersionColumn;
import com.butent.bee.client.grid.column.SelectionColumn;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.ConditionalStyle;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.validation.CellValidateEvent.Handler;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.validation.ValidationOrigin;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.RenderableToken;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Wildcards;
import com.butent.bee.shared.utils.Wildcards.Pattern;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates cell grid elements, connecting view and presenter elements of them.
 */

public class CellGridImpl extends Absolute implements GridView, SearchView, EditStartEvent.Handler,
    EditEndEvent.Handler, ActionEvent.Handler {

  private class FilterUpdater implements ValueUpdater<String> {
    public void update(String value) {
      if (getFilterChangeHandler() != null) {
        getFilterChangeHandler().onValueChange(null);
      }
    }
  }
  
  private class SaveChangesCallback extends Callback<IsRow> {
    @Override
    public void onFailure(String... reason) {
      getEditForm().notifySevere(reason);
    }

    @Override
    public void onSuccess(IsRow result) {
      closeEditForm();
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(CellGridImpl.class);
  
  private final String gridName;
  private final DataInfo dataInfo;

  private GridPresenter viewPresenter = null;

  private ValueChangeHandler<String> filterChangeHandler = null;
  private final FilterUpdater filterUpdater = new FilterUpdater();

  private final CellGrid grid = new CellGrid();

  private Evaluator rowEditable = null;
  private Evaluator rowValidation = null;

  private final Map<String, EditableColumn> editableColumns = Maps.newLinkedHashMap();

  private final Notification notification = new Notification();

  private List<BeeColumn> dataColumns = null;

  private final String relColumn;
  private long relId = BeeConst.UNDEF;

  private final List<String> newRowDefaults = Lists.newArrayList();
  private String newRowCaption = null;

  private FormView newRowForm = null;
  private String newRowFormContainerId = null;
  private boolean newRowFormInitialized = false;
  private boolean newRowFormGenerated = false;

  private FormView editForm = null;
  private boolean editMode = false;
  private boolean editSave = false;
  private Evaluator editMessage = null;
  private boolean editShowId = false;
  private final Set<String> editInPlace = Sets.newHashSet();

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
  
  private SaveChangesCallback saveChangesCallback = null;

  public CellGridImpl(String gridName, String viewName, String relColumn) {
    super();
    this.gridName = gridName;
    this.dataInfo = BeeUtils.isEmpty(viewName) ? null : Data.getDataInfo(viewName);
    this.relColumn = relColumn;
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
    EditableColumn editableColumn = getEditableColumn(columnId, true);
    if (editableColumn == null) {
      return null;
    } else {
      return editableColumn.addCellValidationHandler(handler);
    }
  }

  @Override
  public HandlerRegistration addEditFormHandler(EditFormEvent.Handler handler) {
    return addHandler(handler, EditFormEvent.getType());
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
  public HandlerRegistration addSaveChangesHandler(SaveChangesEvent.Handler handler) {
    return addHandler(handler, SaveChangesEvent.getType());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    setFilterChangeHandler(handler);
    return new HandlerRegistration() {
      public void removeHandler() {
        setFilterChangeHandler(null);
      }
    };
  }

  @Override
  public void applyOptions(String options) {
    if (BeeUtils.isEmpty(options)) {
      return;
    }

    boolean redraw = false;
    String[] opt = BeeUtils.split(options, BeeConst.CHAR_SEMICOLON);

    for (int i = 0; i < opt.length; i++) {
      String[] arr = BeeUtils.split(opt[i], BeeConst.CHAR_SPACE);
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
        String font = ArrayUtils.join(BeeConst.STRING_SPACE, sp, 1);
        msg = "setColumnHeaderFont " + colId + " " + font;
        getGrid().setColumnHeaderFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cbw") && len > 2) {
        msg = "setColumnBodyWidth " + colId + " " + xp[1];
        getGrid().setColumnBodyWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("cbf") && len > 2) {
        String font = ArrayUtils.join(BeeConst.STRING_SPACE, sp, 1);
        msg = "setColumnBodyFont " + colId + " " + font;
        getGrid().setColumnBodyFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cfw") && len > 2) {
        msg = "setColumnFooterWidth " + colId + " " + xp[1];
        getGrid().setColumnFooterWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("cff") && len > 2) {
        String font = ArrayUtils.join(BeeConst.STRING_SPACE, sp, 1);
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
        int oldPageSize = getGrid().getPageSize();
        int newPageSize = BeeUtils.positive(xp[0], getGrid().estimatePageSize());
        if (newPageSize > 0 && newPageSize != oldPageSize) {
          getGrid().setPageSize(newPageSize, true);
          DataRequestEvent.fire(getGrid(), NavigationOrigin.SYSTEM);
        }
        msg = "page size: old " + oldPageSize + " new " + newPageSize;
      }

      if (msg == null) {
        logger.warning("unrecognized command", opt[i]);
      } else {
        logger.info(msg);
      }
    }

    if (redraw) {
      getGrid().refresh();
    }
  }

  @Override
  public void create(final List<BeeColumn> dataCols, int rowCount, BeeRowSet rowSet,
      GridDescription gridDescr, GridCallback callback, boolean hasSearch, Order order) {
    Assert.notEmpty(dataCols);
    Assert.notNull(gridDescr);

    setGridCallback(callback);
    if (callback != null) {
      callback.beforeCreate(dataCols, rowCount, gridDescr, hasSearch);
    }

    setDataColumns(dataCols);

    boolean hasHeaders = gridDescr.hasColumnHeaders();
    boolean hasFooters = hasSearch;
    if (hasFooters && BeeUtils.isFalse(gridDescr.hasFooters())) {
      hasFooters = false;
    }

    Set<String> footerEvents;
    if (hasFooters) {
      footerEvents = NameUtils.toSet(gridDescr.getFooterEvents());
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
    EditableColumn editableColumn = null;

    final String viewName = gridDescr.getViewName();

    for (ColumnDescription columnDescr : columnDescriptions) {
      String columnName = columnDescr.getName();
      if (callback != null && !callback.beforeCreateColumn(columnName, dataCols, columnDescr)) {
        continue;
      }

      ColType colType = columnDescr.getColType();
      if (BeeUtils.isEmpty(columnName) || colType == null) {
        continue;
      }

      List<String> renderColumns = columnDescr.getRenderColumns();
      if (BeeUtils.isEmpty(renderColumns) && !BeeUtils.isEmpty(columnDescr.getRenderTokens())) {
        if (renderColumns == null) {
          renderColumns = Lists.newArrayList();
        }
        for (RenderableToken renderableToken : columnDescr.getRenderTokens()) {
          if (DataUtils.contains(dataCols, renderableToken.getSource())) {
            renderColumns.add(renderableToken.getSource());
          }
        }
      }

      String source = columnDescr.getSource();
      String originalSource;

      if (colType == ColType.RELATED && columnDescr.getRelation() != null) {
        if (!columnDescr.isRelationInitialized()) {
          Holder<String> sourceHolder = Holder.of(source);
          Holder<List<String>> listHolder = Holder.of(renderColumns);

          columnDescr.getRelation().initialize(Data.getDataInfoProvider(), viewName,
              sourceHolder, listHolder, Relation.RenderMode.TARGET);

          source = sourceHolder.get();
          renderColumns = listHolder.get();

          columnDescr.setSource(source);
          columnDescr.setRenderColumns(renderColumns);
          columnDescr.setRelationInitialized(true);
        }
        originalSource = columnDescr.getRelation().getOriginalTarget();
      } else {
        originalSource = null;
      }

      dataIndex = BeeConst.UNDEF;
      if (!BeeUtils.isEmpty(source)) {
        String normalized = DataUtils.getColumnName(source, dataCols, idName, versionName);
        if (BeeUtils.isEmpty(normalized)) {
          logger.warning("columnName:", columnName, "source:", source, "not found");
          continue;
        } else {
          if (!source.equals(normalized)) {
            source = normalized;
          }
          dataIndex = DataUtils.getColumnIndex(source, dataCols);
        }
      }

      String caption = LocaleUtils.maybeLocalize(columnDescr.getCaption());
      if (BeeUtils.isEmpty(caption)) {
        if (!BeeUtils.isEmpty(originalSource) && !originalSource.equals(source)) {
          caption = DataUtils.getColumnLabel(originalSource, dataCols);
        } else if (!BeeConst.isUndef(dataIndex)) {
          caption = dataCols.get(dataIndex).getLabel();
        }

        if (BeeUtils.isEmpty(caption)) {
          caption = columnName;
        } else {
          caption = LocaleUtils.maybeLocalize(caption);
        }
      }

      CellType cellType = columnDescr.getCellType();
      column = null;

      AbstractCellRenderer renderer = null;
      if (callback != null) {
        renderer = callback.getRenderer(columnName, dataCols, columnDescr);
      }
      if (renderer == null) {
        renderer = RendererFactory.getRenderer(columnDescr.getRendererDescription(),
            columnDescr.getRender(), columnDescr.getRenderTokens(), columnDescr.getItemKey(),
            renderColumns, dataCols, dataIndex, columnDescr.getRelation());
      }

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
          if (dataIndex >= 0) {
            BeeColumn dataColumn = dataCols.get(dataIndex);

            if (renderer == null) {
              column = GridFactory.createColumn(dataColumn, dataIndex, cellType);
            } else {
              column = GridFactory.createRenderableColumn(renderer, dataColumn, dataIndex,
                  cellType);
            }

            editableColumn = new EditableColumn(viewName, dataCols, dataIndex, column, caption,
                columnDescr);
            editableColumn.setNotificationListener(this);
            getEditableColumns().put(BeeUtils.normalize(columnName), editableColumn);
          }
          break;

        case CALCULATED:
          Cell<String> cell =
              (cellType == null) ? new CalculatedCell() : GridFactory.createCell(cellType);
          CalculatedColumn calcColumn = new CalculatedColumn(cell, columnDescr.getValueType(),
              renderer);

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
          break;

        case SELECTION:
          column = new SelectionColumn(getGrid());
          source = null;
          break;

        case ACTION:
          column = new ActionColumn(ActionCell.create(viewName, columnDescr), dataIndex, renderer);
          break;
      }

      if (column == null) {
        logger.warning("cannot create column:", columnName, colType);
        continue;
      }

      if (!BeeUtils.isEmpty(columnDescr.getSortBy())) {
        column.setSortBy(DataUtils.parseColumns(columnDescr.getSortBy(), dataCols,
            idName, versionName));
      } else if (!BeeUtils.isEmpty(renderColumns)) {
        column.setSortBy(Lists.newArrayList(renderColumns));
      } else if (!BeeUtils.isEmpty(source)) {
        column.setSortBy(Lists.newArrayList(source));
      }

      if (!BeeUtils.isEmpty(columnDescr.getSearchBy())) {
        column.setSearchBy(DataUtils.parseColumns(columnDescr.getSearchBy(), dataCols,
            idName, versionName));
      } else if (!BeeUtils.isEmpty(column.getSortBy())) {
        column.setSearchBy(Lists.newArrayList(column.getSortBy()));
      } else if (!BeeUtils.isEmpty(source)) {
        column.setSearchBy(Lists.newArrayList(source));
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

      if (!BeeUtils.isEmpty(columnDescr.getOptions())) {
        column.setOptions(columnDescr.getOptions());
      }

      if (hasHeaders) {
        boolean showWidth = showColumnWidths;
        if (columnDescr.showWidth() != null) {
          showWidth = columnDescr.showWidth();
        }
        header = new ColumnHeader(columnName, caption, showWidth);
      }

      if (hasFooters && BeeUtils.isTrue(columnDescr.hasFooter())
          && !BeeUtils.isEmpty(column.getSearchBy())) {
        footer = new ColumnFooter(column.getSearchBy(), footerEvents, getFilterUpdater());
      } else {
        footer = null;
      }

      if (callback != null && !callback.afterCreateColumn(columnName, dataCols, column, header,
          footer, editableColumn)) {
        continue;
      }

      getGrid().addColumn(columnName, dataIndex, source, column, header, footer);
      getGrid().setColumnInfo(columnName, columnDescr, gridDescr, dataCols);
    }

    if (callback != null) {
      callback.afterCreateColumns(this);
    }

    getGrid().initRenderMode(gridDescr.getRenderMode());

    initNewRowDefaults(gridDescr.getNewRowDefaults(), dataCols);
    setNewRowCaption(BeeUtils.notEmpty(gridDescr.getNewRowCaption(),
        (getDataInfo() == null) ? null : getDataInfo().getNewRowCaption()));

    getGrid().estimateHeaderWidths(true);

    getGrid().setRowCount(rowCount, false);
    if (rowSet != null && !rowSet.isEmpty()) {
      getGrid().setRowData(rowSet.getRows().getList(), false);
    }

    initOrder(order);

    getGrid().addEditStartHandler(this);

    add(getGrid());
    add(getNotification());

    setEditMode(BeeUtils.unbox(gridDescr.getEditMode()));
    setEditSave(BeeUtils.unbox(gridDescr.getEditSave()));

    String editFormName = BeeUtils.notEmpty(gridDescr.getEditForm(),
        (getDataInfo() == null) ? null : getDataInfo().getEditForm());
    final String newRowFormName = BeeUtils.notEmpty(gridDescr.getNewRowForm(),
        (getDataInfo() == null) ? null : getDataInfo().getNewRowForm());

    setShowEditPopup(BeeUtils.nvl(gridDescr.getEditPopup(), isChild()));
    setShowNewRowPopup(BeeUtils.nvl(gridDescr.getNewRowPopup(), isChild()));

    setSingleForm(!BeeUtils.isEmpty(editFormName) && BeeUtils.same(newRowFormName, editFormName));

    if (!BeeUtils.isEmpty(editFormName)) {
      FormFactory.createFormView(editFormName, viewName, dataCols, true,
          new FormFactory.FormViewCallback() {
            @Override
            public void onSuccess(FormDescription formDescription, FormView result) {
              if (result != null) {
                String containerId = createFormContainer(result, true, null, showEditPopup());
                setEditFormContainerId(containerId);
                setEditForm(result);

                if (isSingleFormInstance()) {
                  setNewRowFormContainerId(containerId);
                } else if (isSingleForm()) {
                  FormView newRowFormView = new FormImpl(newRowFormName);
                  newRowFormView.create(formDescription, viewName, dataCols, true,
                      FormFactory.getFormCallback(newRowFormName));
                  embraceNewRowForm(newRowFormView);
                }
              }
            }
          });

      if (gridDescr.getEditMessage() != null) {
        setEditMessage(Evaluator.create(gridDescr.getEditMessage(), null, dataCols));
      }
      setEditShowId(BeeUtils.unbox(gridDescr.getEditShowId()));

      if (!BeeUtils.isEmpty(gridDescr.getEditInPlace())) {
        getEditInPlace().addAll(NameUtils.toList(gridDescr.getEditInPlace()));
      }
    }

    if (!BeeUtils.isEmpty(newRowFormName) && !isSingleForm()) {
      FormFactory.createFormView(newRowFormName, viewName, dataCols, true,
          new FormFactory.FormViewCallback() {
            @Override
            public void onSuccess(FormDescription formDescription, FormView result) {
              embraceNewRowForm(result);
            }
          });
    }

    if (BeeUtils.isEmpty(newRowFormName) && !BeeUtils.isEmpty(viewName) && !isReadOnly()) {
      generateNewRowForm(gridDescr);
      setNewRowFormGenerated(true);
    }

    if (callback != null) {
      callback.afterCreate(this);
    }
  }

  @Override
  public boolean createParentRow(final NotificationListener notificationListener,
      final Callback<IsRow> callback) {

    if (!isAdding()) {
      return false;
    }
    final FormView form = getForm(false);
    if (form == null) {
      return false;
    }
    
    if (!form.validate(notificationListener, false)) {
      return false;
    }
    IsRow row = form.getActiveRow();
    if (!DataUtils.isNewRow(row)) {
      return true;
    }
      
    if (!validateFormData(form, notificationListener, false)) {
      return false;
    }

    prepareForInsert(row, form, new Callback<IsRow>() {
      @Override
      public void onFailure(String... reason) {
        if (callback != null) {
          callback.onFailure(reason);
        }
      }

      @Override
      public void onSuccess(IsRow result) {
        form.updateRow(result, true);
        
        IsRow copy = DataUtils.cloneRow(result);
        getGrid().insertRow(copy, false);

        if (callback != null) {
          callback.onSuccess(result);
        }
      }
    });
    
    return true;
  }
  
  @Override
  public int estimatePageSize(int containerWidth, int containerHeight) {
    return getGrid().estimatePageSize(containerWidth, containerHeight, true);
  }

  @Override
  public void finishNewRow(IsRow row) {
    showForm(false, false);

    fireEvent(new AddEndEvent(showNewRowPopup()));
    setAdding(false);

    getGrid().setEditing(false);

    if (row == null) {
      getGrid().refocus();
    } else {
      getGrid().insertRow(row, true);
    }
  }

  @Override
  public void formCancel() {
    if (isAdding()) {
      finishNewRow(null);
    } else {
      closeEditForm();
    }
  }

  @Override
  public void formConfirm() {
    final FormView form = getForm(!isAdding());
    Assert.notNull(form, "formConfirm: active form is null");

    IsRow oldRow = form.getOldRow();
    IsRow newRow = form.getActiveRow();
    Assert.notNull(newRow, "formConfirm: active row is null");

    if (!validateFormData(form, form, true)) {
      return;
    }

    if (isAdding()) {
      if (DataUtils.isNewRow(newRow)) {
        prepareForInsert(newRow, form, new Callback<IsRow>() {
          @Override
          public void onFailure(String... reason) {
            form.notifySevere(reason);
          }

          @Override
          public void onSuccess(IsRow result) {
            finishNewRow(result);
          }
        });

      } else {
        boolean changed = saveChanges(oldRow, newRow, new Callback<IsRow>() {
          @Override
          public void onFailure(String... reason) {
            form.notifySevere(reason);
          }

          @Override
          public void onSuccess(IsRow result) {
            finishNewRow(null);
          }
        });

        if (!changed) {
          finishNewRow(null);
        }
      }

    } else {
      boolean changed = saveChanges(oldRow, newRow, getSaveChangesCallback());
      if (!changed) {
        closeEditForm();
      }
    }
  }

  @Override
  public IsRow getActiveRow() {
    if (isAdding() && getNewRowForm() != null) {
      return getNewRowForm().getActiveRow();
    } else {
      return getGrid().getActiveRow();
    }
  }

  @Override
  public List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  @Override
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

  @Override
  public FormView getForm(boolean edit) {
    if (edit || isSingleFormInstance()) {
      return getEditForm();
    } else {
      return getNewRowForm();
    }
  }

  @Override
  public CellGrid getGrid() {
    return grid;
  }

  @Override
  public GridCallback getGridCallback() {
    return gridCallback;
  }

  @Override
  public String getGridName() {
    return gridName;
  }

  @Override
  public long getRelId() {
    return relId;
  }

  @Override
  public List<? extends IsRow> getRowData() {
    return getGrid().getRowData();
  }

  @Override
  public Collection<RowInfo> getSelectedRows() {
    return getGrid().getSelectedRows().values();
  }

  @Override
  public String getViewName() {
    return (getDataInfo() == null) ? null : getDataInfo().getViewName();
  }

  @Override
  public GridPresenter getViewPresenter() {
    return viewPresenter;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public boolean isAdding() {
    return adding;
  }

  @Override
  public boolean isEnabled() {
    return getGrid().isEnabled();
  }

  @Override
  public boolean isReadOnly() {
    return getGrid().isReadOnly();
  }

  @Override
  public boolean isRowEditable(IsRow rowValue, boolean warn) {
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

  @Override
  public boolean isRowSelected(long rowId) {
    return getGrid().isRowSelected(rowId);
  }

  public boolean likeAMotherlessChild() {
    return isChild() && !DataUtils.isId(getRelId());
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
  public void onAction(ActionEvent event) {
    Assert.notNull(event);
    if (event.contains(Action.REFRESH)) {
      getViewPresenter().refresh(true);
    }

    if (!BeeUtils.isEmpty(getActiveFormContainerId())) {
      if (event.contains(Action.SAVE)) {
        formConfirm();
      } else if (event.contains(Action.CLOSE)) {
        formCancel();
      }
    }
  }

  @Override
  public void onEditEnd(EditEndEvent event, EditEndEvent.HasEditEndHandler source) {
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

  @Override
  public void onEditStart(EditStartEvent event) {
    if (!isEnabled()) {
      return;
    }
    Assert.notNull(event);

    IsRow rowValue = event.getRowValue();
    String columnId = event.getColumnId();
    final EditableColumn editableColumn = getEditableColumn(columnId, false);

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
        presenter.setCaption(BeeUtils.notEmpty(caption, getEditForm().getCaption()));
        presenter.updateStyle(true);
      } else if (!BeeUtils.isEmpty(caption)) {
        presenter.setCaption(caption);
      }
      updateEditFormMessage(presenter, rowValue);

      final boolean enableForm;

      if (presenter.hasAction(Action.EDIT)) {
        presenter.showAction(Action.EDIT, editable && hasEditMode());
        if (presenter.hasAction(Action.SAVE)) {
          presenter.hideAction(Action.SAVE);
        }
        enableForm = false;

      } else {
        if (presenter.hasAction(Action.SAVE)) {
          presenter.showAction(Action.SAVE, editable && hasEditSave());
        }
        enableForm = editable;        
      }

      getEditForm().setEnabled(enableForm);

      ScheduledCommand focusCommand = new ScheduledCommand() {
        @Override
        public void execute() {
          if (editableColumn != null && enableForm) {
            String source = editableColumn.getColumnId();
            Widget widget = getEditForm().getWidgetBySource(source);

            if (widget == null && getDataInfo() != null) {
              String relSource = getDataInfo().getRelationSource(source);
              if (!BeeUtils.isEmpty(relSource) && !BeeUtils.same(source, relSource)) {
                widget = getEditForm().getWidgetBySource(relSource);
              }
            }

            if (widget instanceof Focusable && widget.isVisible()) {
              ((Focusable) widget).setFocus(true);
            } else {
              UiHelper.focus(getEditForm().asWidget());
            }
          }
        }
      };
      
      IsRow row = DataUtils.cloneRow(rowValue);
      
      boolean start;
      if (getEditForm().getFormCallback() != null) {
        start = getEditForm().getFormCallback().onStartEdit(getEditForm(), row, focusCommand);
      } else {
        start = true;
      }
      
      if (start) {
        getEditForm().updateRow(row, true);
        focusCommand.execute();
      }
      return;
    }

    if (event.getCharCode() == EditorFactory.START_KEY_DELETE) {
      if (!editableColumn.isNullable()) {
        return;
      }

      String oldValue = editableColumn.getOldValue(rowValue);
      if (BeeUtils.isEmpty(oldValue)) {
        return;
      }

      validateAndUpdate(editableColumn, rowValue, oldValue, null, false);
      return;
    }

    if (ValueType.BOOLEAN.equals(editableColumn.getDataType())
        && BeeUtils.inList(event.getCharCode(), EditorFactory.START_MOUSE_CLICK,
            EditorFactory.START_KEY_ENTER)) {

      String oldValue = editableColumn.getOldValue(rowValue);
      Boolean b = !BeeUtils.toBoolean(oldValue);
      if (!b && editableColumn.isNullable()) {
        b = null;
      }
      String newValue = BooleanValue.pack(b);

      validateAndUpdate(editableColumn, rowValue, oldValue, newValue, true);
      return;
    }

    getGrid().setEditing(true);
    if (event.getSourceElement() != null) {
      event.getSourceElement().blur();
    }

    editableColumn.openEditor(this, event.getSourceElement(), getGrid().getElement(),
        getGrid().getZIndex() + 1, rowValue, BeeUtils.toChar(event.getCharCode()), this);
  }

  @Override
  public void refresh(boolean refreshChildren) {
    getGrid().refresh();
  }

  @Override
  public void refreshCellContent(long rowId, String columnSource) {
    getGrid().refreshCellContent(rowId, columnSource);
  }

  @Override
  public void setEnabled(boolean enabled) {
    getGrid().setEnabled(enabled);
  }

  @Override
  public void setRelId(long relId) {
    this.relId = relId;
  }

  @Override
  public void setViewPresenter(Presenter presenter) {
    if (presenter instanceof GridPresenter) {
      this.viewPresenter = (GridPresenter) presenter;
    } else if (presenter == null) {
      this.viewPresenter = null;
    }
  }

  @Override
  public void startNewRow() {
    if (!isEnabled() || isReadOnly()) {
      return;
    }

    IsRow oldRow = getGrid().getActiveRow();
    IsRow newRow = DataUtils.createEmptyRow(getDataColumns().size());

    if (!getNewRowDefaults().isEmpty()) {
      DataUtils.setDefaults(newRow, getNewRowDefaults(), getDataColumns(), Global.getDefaults());
      RelationUtils.setDefaults(getViewName(), newRow, getNewRowDefaults(), getDataColumns());
    }

    for (EditableColumn editableColumn : getEditableColumns().values()) {
      if (!editableColumn.hasCarry()) {
        continue;
      }
      if (oldRow == null) {
        oldRow = DataUtils.createEmptyRow(getDataColumns().size());
      }

      String carry = editableColumn.getCarryValue(oldRow);
      if (!BeeUtils.isEmpty(carry)) {
        int index = editableColumn.getColIndex();
        newRow.setValue(index, carry);

        if (editableColumn.hasRelation() && BeeUtils.equalsTrim(carry, oldRow.getString(index))) {
          RelationUtils.setRelatedValues(getViewName(), editableColumn.getColumnId(),
              newRow, oldRow);
        }
      }
    }

    if (getGridCallback() != null && !getGridCallback().onStartNewRow(this, oldRow, newRow)) {
      return;
    }

    getGrid().setEditing(true);

    fireEvent(new AddStartEvent(null, showNewRowPopup()));

    setAdding(true);

    String caption = getRowCaption(newRow, false);

    showForm(false, true);
    FormView form = getForm(false);
    if (form.getFormCallback() != null) {
      form.getFormCallback().onStartNewRow(form, oldRow, newRow);
    }

    if (form.getViewPresenter() instanceof GridFormPresenter) {
      GridFormPresenter presenter = (GridFormPresenter) form.getViewPresenter();

      if (isSingleForm()) {
        presenter.setCaption(BeeUtils.notEmpty(caption, getNewRowCaption()));
        presenter.setMessage(null);
        presenter.updateStyle(false);

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

    form.updateRow(newRow, true);
    UiHelper.focus(form.asWidget());
  }

  @Override
  public boolean validateFormData(FormView form, NotificationListener notificationListener,
      boolean focusOnError) {

    boolean ok = true;
    if (isReadOnly()) {
      return ok;
    }

    IsRow oldRow = getGrid().getActiveRow();
    IsRow newRow = form.getActiveRow();

    String oldValue = null;
    String newValue;
    int index;

    for (Map.Entry<String, EditableColumn> entry : getEditableColumns().entrySet()) {
      if (getGrid().isColumnReadOnly(entry.getKey())) {
        continue;
      }

      EditableColumn ec = entry.getValue();
      if (!ec.isWritable()) {
        continue;
      }

      index = ec.getColIndex();
      if (oldRow != null) {
        oldValue = oldRow.getString(index);
      }
      newValue = newRow.getString(index);

      CellValidation cv = new CellValidation(oldValue, newValue, ec.getValidation(), newRow,
          ec.getDataColumn(), index, ec.getDataType(), ec.isNullable(), ec.getMinValue(),
          ec.getMaxValue(), ec.getCaption(), notificationListener);

      ok = BeeUtils.isTrue(ValidationHelper.validateCell(cv, ec, ValidationOrigin.GRID));
      if (!ok) {
        if (form != null && focusOnError) {
          form.focus(ec.getColumnId());
        }
        break;
      }
    }

    if (ok && getRowValidation() != null) {
      ok = ValidationHelper.validateRow(newRow, getRowValidation(), notificationListener);
    }
    return ok;
  }

  @Override
  protected void onUnload() {
    if (!Global.isTemporaryDetach()) {
      if (getNewRowPopup() != null) {
        getNewRowPopup().unload();
      }
      if (getEditPopup() != null) {
        getEditPopup().unload();
      }
    }

    super.onUnload();
  }

  private void amendGeneratedSize(ModalForm popup, FormView form) {
    boolean animationEnabled = popup.isAnimationEnabled();
    popup.setAnimationEnabled(false);
    popup.setVisible(false);
    popup.show();
    
    int width = DomUtils.getOuterWidth(form.getRootWidget().getElement());
    int height = DomUtils.getOuterHeight(form.getRootWidget().getElement())
        + form.getViewPresenter().getHeader().getHeight() + 1;
    
    popup.hide();
    popup.setAnimationEnabled(animationEnabled);
    popup.setVisible(true);
    
    if (width > BeeUtils.toInt(form.getWidthValue())) {
      StyleUtils.setWidth(popup, width);
    }
    StyleUtils.setHeight(popup, height);
  }

  private void closeEditForm() {
    showForm(true, false);
    fireEvent(new EditFormEvent(State.CLOSED, showEditPopup()));
    getGrid().refocus();
  }

  private String createFormContainer(final FormView formView, boolean edit, String caption,
      boolean asPopup) {
    String formCaption = BeeUtils.notEmpty(caption, formView.getCaption());

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

    final GridFormPresenter gfp = new GridFormPresenter(this, formView, formCaption, actions, edit,
        hasEditSave());
    Widget container = gfp.getWidget();

    if (asPopup) {
      ModalForm popup = new ModalForm(container, formView, true, true);

      popup.setOnSave(new ScheduledCommand() {
        @Override
        public void execute() {
          if (gfp.isActionEnabled(Action.SAVE) && formView.checkOnSave()) {
            gfp.handleAction(Action.SAVE);
          }
        }
      });

      popup.setOnEscape(new ScheduledCommand() {
        @Override
        public void execute() {
          if (formView.checkOnClose()) {
            gfp.handleAction(Action.CLOSE);
          }
        }
      });

      if (edit) {
        setEditPopup(popup);
      } else {
        setNewRowPopup(popup);
      }

    } else {
      add(container);
      container.setVisible(false);
    }

    formView.setEditing(true);
    formView.setViewPresenter(gfp);

    formView.addActionHandler(this);

    return DomUtils.getId(container);
  }

  private void createNewRowWidgets(FlexTable container, List<String> columnNames,
      WidgetDescriptionCallback callback) {

    int r = 0;

    for (String columnName : columnNames) {
      EditableColumn editableColumn = getEditableColumn(columnName, true);
      if (editableColumn == null) {
        continue;
      }

      BeeLabel label = new BeeLabel(editableColumn.getCaption());
      label.addStyleName(RowFactory.STYLE_NEW_ROW_LABEL);

      if (editableColumn.hasDefaults()) {
        label.addStyleName(StyleUtils.NAME_HAS_DEFAULTS);
      } else if (!editableColumn.isNullable()) {
        label.addStyleName(StyleUtils.NAME_REQUIRED);
      }

      container.setWidget(r, 0, label);
      container.getCellFormatter().setStyleName(r, 0, RowFactory.STYLE_NEW_ROW_LABEL_CELL);

      Editor editor = editableColumn.createEditor(true);
      editor.asWidget().addStyleName(RowFactory.STYLE_NEW_ROW_INPUT);

      container.setWidget(r, 1, editor.asWidget());
      container.getCellFormatter().setStyleName(r, 1, RowFactory.STYLE_NEW_ROW_INPUT_CELL);

      if (editor.getWidgetType() != null) {
        WidgetDescription widgetDescription = new WidgetDescription(editor.getWidgetType(),
            editor.getId(), columnName);

        widgetDescription.updateFrom(editableColumn);
        callback.onSuccess(widgetDescription, editor.asWidget());
      }
      r++;
    }
  }

  private void embraceNewRowForm(FormView formView) {
    if (formView != null) {
      String id = createFormContainer(formView, false, getNewRowCaption(), showNewRowPopup());

      setNewRowFormContainerId(id);
      setNewRowForm(formView);
    }
  }

  private void generateNewRowForm(GridDescription gridDescription) {
    String newRowColumns = BeeUtils.notEmpty(gridDescription.getNewRowColumns(),
        (getDataInfo() == null) ? null : getDataInfo().getNewRowColumns());
    final List<String> columnNames = getNewRowColumnNames(newRowColumns);

    if (columnNames.isEmpty()) {
      logger.severe("grid", gridDescription.getName(), "new row columns not available");
      return;
    }

    String formName = "grid-" + gridDescription.getName() + "-new-row";
    final String rootName = "root";

    FormDescription formDescription = FormFactory.createFormDescription(formName,
        ImmutableMap.of(UiConstants.ATTR_VIEW_NAME, gridDescription.getViewName()),
        FormWidget.FLEX_TABLE, ImmutableMap.of(UiConstants.ATTR_NAME, rootName));

    FormView form = new FormImpl(formName);
    form.create(formDescription, gridDescription.getViewName(), getDataColumns(), true,
        new AbstractFormCallback() {
          @Override
          public void afterCreateEditableWidget(EditableWidget editableWidget) {
            EditableColumn ec = getEditableColumn(editableWidget.getWidgetName(), true);
            editableWidget.setValidationDelegate(ec);
          }

          @Override
          public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback wdc) {
            if (BeeUtils.same(name, rootName) && widget instanceof FlexTable) {
              widget.addStyleName(RowFactory.STYLE_NEW_ROW_TABLE);
              createNewRowWidgets((FlexTable) widget, columnNames, wdc);
            }
          }

          @Override
          public FormCallback getInstance() {
            return null;
          }

          @Override
          public AbstractCellRenderer getRenderer(WidgetDescription widgetDescription) {
            EditableColumn ec = getEditableColumn(widgetDescription.getWidgetName(), true);
            AbstractColumn<?> uiColumn = (ec == null) ? null : ec.getUiColumn();

            if (uiColumn instanceof HasCellRenderer) {
              return ((HasCellRenderer) uiColumn).getRenderer();
            } else {
              return null;
            }
          }
        });
    
    double initialWidth = RowFactory.GENERATED_FORM_WIDTH;
    double initialHeight = RowFactory.GENERATED_HEADER_HEIGHT + RowFactory.GENERATED_HEIGHT_MARGIN
        + columnNames.size() * RowFactory.GENERATED_ROW_HEIGHT;

    form.setWidthValue(initialWidth);
    form.setHeightValue(initialHeight);

    embraceNewRowForm(form);
  }

  private String getActiveFormContainerId() {
    return activeFormContainerId;
  }

  private DataInfo getDataInfo() {
    return dataInfo;
  }

  private EditableColumn getEditableColumn(String columnId, boolean warn) {
    if (BeeUtils.isEmpty(columnId)) {
      if (warn) {
        logger.warning("editable column id not specified");
      }
      return null;
    }

    EditableColumn editableColumn = getEditableColumns().get(BeeUtils.normalize(columnId));
    if (editableColumn == null && warn) {
      logger.warning("editable column not found:", columnId);
    }
    return editableColumn;
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

  private ValueChangeHandler<String> getFilterChangeHandler() {
    return filterChangeHandler;
  }

  private FilterUpdater getFilterUpdater() {
    return filterUpdater;
  }

  private String getNewRowCaption() {
    return newRowCaption;
  }

  private List<String> getNewRowColumnNames(String columnNames) {
    List<String> result = Lists.newArrayList();

    if (!BeeUtils.isEmpty(columnNames)) {
      for (String colName : NameUtils.NAME_SPLITTER.split(columnNames)) {
        if (BeeUtils.isEmpty(colName)) {
          continue;
        }

        String name = BeeUtils.normalize(colName);
        if (!getEditableColumns().containsKey(name)) {
          logger.warning("newRowColumn", colName, "is not editable");
          continue;
        }

        if (!result.contains(name)) {
          result.add(name);
        }
      }
    }

    if (result.isEmpty()) {
      for (Map.Entry<String, EditableColumn> entry : getEditableColumns().entrySet()) {
        String name = entry.getKey();
        if (!getGrid().isColumnReadOnly(name) && !result.contains(name)) {
          result.add(name);
        }
      }
    }
    return result;
  }

  private List<String> getNewRowDefaults() {
    return newRowDefaults;
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

  private Notification getNotification() {
    return notification;
  }

  private String getRelColumn() {
    return relColumn;
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

  private SaveChangesCallback getSaveChangesCallback() {
    if (saveChangesCallback == null) {
      saveChangesCallback = new SaveChangesCallback();
    }
    return saveChangesCallback;
  }

  private boolean hasEditMode() {
    return editMode;
  }

  private boolean hasEditSave() {
    return editSave;
  }

  private void initNewRowDefaults(String input, List<BeeColumn> columns) {
    if (!getNewRowDefaults().isEmpty()) {
      getNewRowDefaults().clear();
    }

    if (BeeUtils.same(input, BeeConst.STRING_MINUS) || BeeUtils.isEmpty(columns)) {
      return;
    }

    if (BeeUtils.isEmpty(input) || Wildcards.isDefaultAny(input)) {
      for (BeeColumn column : columns) {
        if (column.hasDefaults()) {
          getNewRowDefaults().add(column.getId());
        }
      }
      return;
    }

    Set<Pattern> patterns = Sets.newHashSet();
    for (String s : NameUtils.NAME_SPLITTER.split(input)) {
      patterns.add(Wildcards.getDefaultPattern(s, false));
    }

    for (BeeColumn column : columns) {
      if (column.hasDefaults() && Wildcards.contains(patterns, column.getId())) {
        getNewRowDefaults().add(column.getId());
      }
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

  private boolean isChild() {
    return !BeeUtils.isEmpty(getRelColumn());
  }

  private boolean isEditFormInitialized() {
    return editFormInitialized;
  }

  private boolean isNewRowFormGenerated() {
    return newRowFormGenerated;
  }

  private boolean isNewRowFormInitialized() {
    return newRowFormInitialized;
  }

  private boolean isSingleForm() {
    return singleForm;
  }

  private boolean isSingleFormInstance() {
    return isSingleForm() && !showNewRowPopup() && !showEditPopup();
  }

  private void prepareForInsert(IsRow row, FormView form, Callback<IsRow> callback) {
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

      if (dataColumn.isWritable()) {
        columns.add(dataColumn);
        values.add(value);
      }
    }

    if (columns.isEmpty()) {
      callback.onFailure(getViewName(), "New Row", "all columns cannot be empty");
      return;
    }
    
    ReadyForInsertEvent event = new ReadyForInsertEvent(columns, values, callback);

    if (form.getFormCallback() != null && !form.getFormCallback().onReadyForInsert(event)) {
      return;
    }
    if (getGridCallback() != null && !getGridCallback().onReadyForInsert(this, event)) {
      return;
    }
    
    fireEvent(event);
  }

  private boolean saveChanges(IsRow oldRow, IsRow newRow, Callback<IsRow> callback) {
    List<BeeColumn> columns = Lists.newArrayList();
    List<String> oldValues = Lists.newArrayList();
    List<String> newValues = Lists.newArrayList();

    for (int i = 0; i < getDataColumns().size(); i++) {
      BeeColumn dataColumn = getDataColumns().get(i);
      if (!dataColumn.isWritable()) {
        continue;
      }

      String oldValue = oldRow.getString(i);
      String newValue = newRow.getString(i);

      if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
        columns.add(dataColumn);
        oldValues.add(oldValue);
        newValues.add(newValue);
      }
    }

    SaveChangesEvent event = new SaveChangesEvent(oldRow, newRow, columns, oldValues, newValues,
        callback);
    if (getGridCallback() != null && !getGridCallback().onSaveChanges(this, event)) {
      return false;
    }

    if (columns.isEmpty()) {
      return false;
    } else {
      fireEvent(event);
      return true;
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

  private void setEditPopup(ModalForm editPopup) {
    this.editPopup = editPopup;
  }

  private void setEditSave(boolean editSave) {
    this.editSave = editSave;
  }

  private void setEditShowId(boolean editShowId) {
    this.editShowId = editShowId;
  }

  private void setFilterChangeHandler(ValueChangeHandler<String> filterChangeHandler) {
    this.filterChangeHandler = filterChangeHandler;
  }

  private void setGridCallback(GridCallback gridCallback) {
    this.gridCallback = gridCallback;
  }

  private void setNewRowCaption(String newRowCaption) {
    this.newRowCaption = newRowCaption;
  }

  private void setNewRowForm(FormView newRowForm) {
    this.newRowForm = newRowForm;
  }

  private void setNewRowFormContainerId(String newRowFormContainerId) {
    this.newRowFormContainerId = newRowFormContainerId;
  }

  private void setNewRowFormGenerated(boolean newRowFormGenerated) {
    this.newRowFormGenerated = newRowFormGenerated;
  }

  private void setNewRowFormInitialized(boolean newRowFormInitialized) {
    this.newRowFormInitialized = newRowFormInitialized;
  }

  private void setNewRowPopup(ModalForm newRowPopup) {
    this.newRowPopup = newRowPopup;
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
    FormView form = getForm(edit);

    if (show) {
      if (popup == null) {
        showGrid(false);
        StyleUtils.unhideDisplay(containerId);
      } else {
        if (isChild() && isNewRowFormGenerated()) {
          if (!isNewRowFormInitialized()) {
            amendGeneratedSize(popup, form);
          }
          popup.showAt(getAbsoluteLeft(), getAbsoluteTop(), DomUtils.getScrollBarHeight() + 1);
        } else {
          popup.center();
        }
      }

      if (edit) {
        if (!isEditFormInitialized()) {
          setEditFormInitialized(true);
          if (isSingleFormInstance()) {
            setNewRowFormInitialized(true);
          }
          form.start(null);
        }
      } else {
        if (!isNewRowFormInitialized()) {
          setNewRowFormInitialized(true);
          form.start(null);
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

    form.setState(show ? State.OPEN : State.CLOSED);
  }

  private void showGrid(boolean show) {
    getGrid().setVisible(show);
  }

  private boolean showNewRowPopup() {
    return showNewRowPopup;
  }

  private void showNote(LogLevel level, String... messages) {
    StyleUtils.setZIndex(getNotification(), getGrid().getZIndex() + 1);
    getNotification().show(level, messages);
  }

  private void updateCell(final IsRow rowValue, final IsColumn dataColumn,
      String oldValue, final String newValue, boolean rowMode) {

    getGrid().preliminaryUpdate(rowValue.getId(), dataColumn.getId(), newValue);
    
    Callback<IsRow> callback = new Callback<IsRow>() {
      @Override
      public void onFailure(String... reason) {
        refreshCellContent(rowValue.getId(), dataColumn.getId());
        notifySevere(reason);
      }

      @Override
      public void onSuccess(IsRow result) {
        if (result != null) {
          logger.info("updated:", result.getId(), dataColumn.getId(), newValue);
        }
      }
    };

    ReadyForUpdateEvent event = 
        new ReadyForUpdateEvent(rowValue, dataColumn, oldValue, newValue, rowMode, callback);
    
    if (getGridCallback() != null && !getGridCallback().onReadyForUpdate(this, event)) {
      return;
    }
    fireEvent(event);
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
      message = BeeUtils.joinWords(message, BeeUtils.bracket(BeeUtils.toString(row.getId())));
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

  private boolean validateAndUpdate(EditableColumn editableColumn, IsRow row, String oldValue,
      String newValue, boolean tab) {
    Boolean ok = editableColumn.validate(oldValue, newValue, row, ValidationOrigin.CELL);
    if (!BeeUtils.isTrue(ok)) {
      return false;
    }

    updateCell(row, editableColumn.getDataColumn(), oldValue, newValue,
        editableColumn.getRowModeForUpdate());
    if (tab) {
      getGrid().handleKeyboardNavigation(KeyCodes.KEY_TAB, false);
    }
    return true;
  }
}
