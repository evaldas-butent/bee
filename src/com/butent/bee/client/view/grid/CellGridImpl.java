package com.butent.bee.client.view.grid;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.DOM;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.CalculatedCell;
import com.butent.bee.client.grid.CalculatedColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.RowIdColumn;
import com.butent.bee.client.grid.RowVersionColumn;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.ConditionalStyle;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.edit.AdjustmentListener;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasNumberBounds;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.ColumnDescription.CellType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Creates cell grid elements, connecting view and presenter elements of them.
 */

public class CellGridImpl extends Absolute implements GridView, SearchView, EditStartEvent.Handler {

  private class CancelCommand extends BeeCommand {
    @Override
    public void execute() {
      restoreGrid();
    }
  }

  private class ConfirmationCommand extends BeeCommand {
    @Override
    public void execute() {
      restoreGrid();
    }
  }
  
  private class EditableColumn implements KeyDownHandler, BlurHandler, EditStopEvent.Handler {
    private final int colIndex;
    private final BeeColumn dataColumn;
    private final RelationInfo relationInfo;

    private final Evaluator editable;
    private final Evaluator validation;

    private final String minValue;
    private final String maxValue;

    private final EditorDescription editorDescription;

    private Editor editor = null;
    private IsRow rowValue = null;

    private State state = State.PENDING;
    
    private String captionElementId = null;

    private EditableColumn(List<BeeColumn> dataColumns, int colIndex,
        ColumnDescription columnDescr) {
      this.colIndex = colIndex;
      this.dataColumn = dataColumns.get(colIndex);

      if (columnDescr == null) {
        this.relationInfo = null;
        this.editable = null;
        this.validation = null;
        this.minValue = null;
        this.maxValue = null;
        this.editorDescription = null;
      } else {
        if (ColType.RELATED.equals(columnDescr.getColType())) {
          this.relationInfo = RelationInfo.create(dataColumns, columnDescr);
        } else {
          this.relationInfo = null;
        }
        String source = this.dataColumn.getLabel();
        this.editable = Evaluator.create(columnDescr.getEditable(), source, dataColumns);
        this.validation = Evaluator.create(columnDescr.getValidation(), source, dataColumns);
        this.minValue = columnDescr.getMinValue();
        this.maxValue = columnDescr.getMaxValue();
        this.editorDescription = columnDescr.getEditor();
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof EditableColumn)) {
        return false;
      }
      return getColIndex() == ((EditableColumn) obj).getColIndex();
    }
    
    @Override
    public int hashCode() {
      return getColIndex();
    }

    public void onBlur(BlurEvent event) {
      if (State.OPEN.equals(getState())) {
        closeEditor();
      }
    }

    public void onEditStop(EditStopEvent event) {
      if (event.isFinished()) {
        endEdit();
      } else if (event.isError()) {
        notifySevere(event.getMessage());
      } else {
        closeEditor();
      }
    }

    public void onKeyDown(KeyDownEvent event) {
      int keyCode = event.getNativeKeyCode();
      if (getEditor() == null || getEditor().handlesKey(keyCode)) {
        return;
      }
      NativeEvent nativeEvent = event.getNativeEvent();

      switch (keyCode) {
        case KeyCodes.KEY_ESCAPE:
          EventUtils.eatEvent(nativeEvent);
          closeEditor();
          break;

        case KeyCodes.KEY_ENTER:
          EventUtils.eatEvent(nativeEvent);
          endEdit();
          break;

        case KeyCodes.KEY_TAB:
        case KeyCodes.KEY_UP:
        case KeyCodes.KEY_DOWN:
          EventUtils.eatEvent(event.getNativeEvent());
          if (endEdit()) {
            getGrid().handleKeyboardNavigation(keyCode, EventUtils.hasModifierKey(nativeEvent));
          }
          break;
      }
    }

    private void closeEditor() {
      setState(State.CLOSED);
      getEditor().setEditing(false);
      StyleUtils.hideDisplay(getEditor().asWidget());

      getGrid().setEditing(false);
      getGrid().refocus();
    }

    private boolean endEdit() {
      if (State.OPEN.equals(getState())) {
        String oldValue = getOldValueForUpdate(getRowValue());
        String editorValue = getEditor().getValue();

        if (BeeUtils.equalsTrimRight(oldValue, editorValue)) {
          closeEditor();
          return true;
        }

        String errorMessage = getEditor().validate();
        if (!BeeUtils.isEmpty(errorMessage)) {
          notifySevere(editorValue, errorMessage);
          return false;
        }

        String newValue = getEditor().getNormalizedValue();
        if (!validate(oldValue, newValue)) {
          return false;
        }

        closeEditor();
        if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
          updateCell(getRowValue(), getColumnForUpdate(), oldValue, newValue, getRowModeForUpdate());
        }
        return true;
      }
      return false;
    }
    
    private Editor ensureEditor(String columnId) {
      if (getEditor() != null) {
        return getEditor();
      }

      String format = null;
      if (getEditorDescription() != null) {
        setEditor(EditorFactory.getEditor(getEditorDescription(), isNullable(), getRelationInfo()));
        format = getEditorDescription().getFormat();
      } else {
        setEditor(EditorFactory.createEditor(getDataColumn(), isNullable()));
      }

      getEditor().asWidget().addStyleName(STYLE_EDITOR);

      if (BeeUtils.isEmpty(format)) {
        AbstractColumn<?> gridColumn = getGrid().getColumn(columnId);
        LocaleUtils.copyDateTimeFormat(gridColumn, getEditor());
        LocaleUtils.copyNumberFormat(gridColumn, getEditor());
      } else {
        Format.setFormat(getEditor(), getDataColumn().getType(), format);
      }

      initEditor();
      add(getEditor());
      return getEditor();
    }

    private String getCaptionElementId() {
      return captionElementId;
    }

    private int getColIndex() {
      return colIndex;
    }

    private BeeColumn getColumnForUpdate() {
      return (getRelationInfo() == null) ? getDataColumn() : getRelationInfo().getDataColumn();
    }

    private BeeColumn getDataColumn() {
      return dataColumn;
    }

    private Evaluator getEditable() {
      return editable;
    }

    private Editor getEditor() {
      return editor;
    }

    private EditorDescription getEditorDescription() {
      return editorDescription;
    }

    private String getMaxValue() {
      return maxValue;
    }

    private String getMinValue() {
      return minValue;
    }

    private String getOldValueForUpdate(IsRow row) {
      int index = (getRelationInfo() == null) ? getColIndex() : getRelationInfo().getDataIndex();
      return row.getString(index);
    }

    private RelationInfo getRelationInfo() {
      return relationInfo;
    }

    private boolean getRowModeForUpdate() {
      return getRelationInfo() != null;
    }

    private IsRow getRowValue() {
      return rowValue;
    }

    private State getState() {
      return state;
    }

    private Evaluator getValidation() {
      return validation;
    }

    private void initEditor() {
      if (getEditor() == null) {
        return;
      }
      getEditor().addKeyDownHandler(this);
      getEditor().addBlurHandler(this);
      getEditor().addEditStopHandler(this);

      if (getEditor() instanceof HasNumberBounds) {
        if (BeeUtils.isDouble(getMinValue())) {
          ((HasNumberBounds) getEditor()).setMinValue(BeeUtils.toDoubleOrNull(getMinValue()));
        }
        if (BeeUtils.isDouble(getMaxValue())) {
          ((HasNumberBounds) getEditor()).setMaxValue(BeeUtils.toDoubleOrNull(getMaxValue()));
        }
      }
    }

    private boolean isCellEditable(IsRow row, boolean warn) {
      if (row == null) {
        return false;
      }
      if (getEditable() == null) {
        return true;
      }

      getEditable().update(row, BeeConst.UNDEF, getColIndex(), dataColumn.getType(),
          row.getString(getColIndex()));
      boolean ok = BeeUtils.toBoolean(getEditable().evaluate());

      if (!ok && warn) {
        notifyWarning("Cell is read only:", getEditable().transform());
      }
      return ok;
    }

    private boolean isNullable() {
      if (getRelationInfo() != null) {
        return getRelationInfo().isNullable();
      } else if (getDataColumn() != null) {
        return getDataColumn().isNullable();
      } else {
        return true;
      }
    }

    private void setCaptionElementId(String captionElementId) {
      this.captionElementId = captionElementId;
    }

    private void setEditor(Editor editor) {
      this.editor = editor;
    }

    private void setRowValue(IsRow rowValue) {
      this.rowValue = rowValue;
    }

    private void setState(State state) {
      this.state = state;
    }

    private boolean validate(String oldValue, String newValue) {
      if (BeeUtils.equalsTrimRight(oldValue, newValue)) {
        return true;
      }
      String errorMessage = null;

      if (getValidation() != null) {
        getValidation().update(getRowValue(), BeeConst.UNDEF, getColIndex(),
            getDataColumn().getType(), oldValue, newValue);
        String msg = getValidation().evaluate();
        if (!BeeUtils.isEmpty(msg)) {
          errorMessage = msg;
        }
      }

      if (errorMessage == null
          && (!BeeUtils.isEmpty(getMinValue()) || !BeeUtils.isEmpty(getMaxValue()))) {
        ValueType type = getDataColumn().getType();
        Value value = Value.parseValue(type, newValue, false);

        if (!BeeUtils.isEmpty(getMinValue())
            && value.compareTo(Value.parseValue(type, getMinValue(), true)) < 0) {
          errorMessage = BeeUtils.concat(1, errorMessage, "Min value:", getMinValue());
        }
        if (!BeeUtils.isEmpty(getMaxValue())
            && value.compareTo(Value.parseValue(type, getMaxValue(), true)) > 0) {
          errorMessage = BeeUtils.concat(1, errorMessage, "Max value:", getMaxValue());
        }
      }

      if (errorMessage == null) {
        return true;
      } else {
        notifySevere(errorMessage);
        return false;
      }
    }
  }

  private class FilterUpdater implements ValueUpdater<String> {
    public void update(String value) {
      if (filterChangeHandler != null) {
        filterChangeHandler.onChange(null);
      }
    }
  }
  
  private static final String STYLE_EDITOR = "bee-CellGridEditor";

  private Presenter viewPresenter = null;

  private ChangeHandler filterChangeHandler = null;
  private final FilterUpdater filterUpdater = new FilterUpdater();

  private final CellGrid grid = new CellGrid();

  private Evaluator rowEditable = null;

  private final Map<String, EditableColumn> editableColumns = Maps.newLinkedHashMap();

  private final Notification notification = new Notification();
  
  private final List<String> newRowColumns = Lists.newArrayList();
  
  private String confirmWidgetId = null;
  private String cancelWidgetId = null;

  public CellGridImpl() {
    super();
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    filterChangeHandler = handler;
    return new HandlerRegistration() {
      public void removeHandler() {
        filterChangeHandler = null;
      }
    };
  }

  public HandlerRegistration addEditEndHandler(EditEndEvent.Handler handler) {
    return addHandler(handler, EditEndEvent.getType());
  }

  public void addRow() {
    if (getNewRowColumns().isEmpty()) {
      return;
    }
    
    getGrid().setEditing(true);
    StyleUtils.hideDisplay(getGrid());
    
    int y = 20;
    int xCap = 20;
    int xEd = 120;
    
    for (String columnId : getNewRowColumns()) {
      EditableColumn editableColumn = getEditableColumn(columnId);
      if (editableColumn == null) {
        continue;
      }
      
      Element captionElement = null;
      if (editableColumn.getCaptionElementId() == null) {
        String caption = getGrid().getColumnCaption(columnId);
        BeeLabel label = new BeeLabel(caption);
        StyleUtils.makeAbsolute(label);
        add(label);
        editableColumn.setCaptionElementId(label.getId());
        captionElement = label.getElement();
      } else {
        captionElement = DOM.getElementById(editableColumn.getCaptionElementId());
      }
      
      if (captionElement != null) {
        StyleUtils.setLeft(captionElement, xCap);
        StyleUtils.setTop(captionElement, y);
        StyleUtils.unhideDisplay(captionElement);
      }
      
      Editor editor = editableColumn.ensureEditor(columnId);
      StyleUtils.setLeft(editor.asWidget(), xEd);
      StyleUtils.setTop(editor.asWidget(), y);
      StyleUtils.unhideDisplay(editor.asWidget());
      
      y += 30;
    }
    
    String widgetId = ensureConfirmWidget();
    StyleUtils.setLeft(widgetId, xCap);
    StyleUtils.setTop(widgetId, y);
    StyleUtils.unhideDisplay(widgetId);    

    widgetId = ensureCancelWidget();
    StyleUtils.setLeft(widgetId, xEd);
    StyleUtils.setTop(widgetId, y);
    StyleUtils.unhideDisplay(widgetId);    
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
        ColumnDescription dataCol = new ColumnDescription(ColType.DATA, dataCols.get(i).getLabel());
        dataCol.setReadOnly(getGrid().isReadOnly());
        dataCol.setSortable(true);
        dataCol.setVisible(true);
        dataCol.setShowWidth(showColumnWidths);
        dataCol.setHasFooter(hasFooters);
        dataCol.setSource(dataCols.get(i).getLabel());
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
            if (BeeUtils.same(source, dataColumn.getLabel())) {
              column = GridFactory.createColumn(dataColumn, i, cellType);
              getEditableColumns().put(columnId, new EditableColumn(dataCols, i, columnDescr));
              if (columnDescr.isSortable()) {
                column.setSortable(true);
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
        String caption = BeeUtils.ifString(columnDescr.getCaption(), columnId);
        header = new ColumnHeader(columnId, caption,
            showColumnWidths && !BeeUtils.isFalse(columnDescr.showWidth()));
      }
      if (hasFooters && BeeUtils.isTrue(columnDescr.hasFooter())
          && !BeeUtils.isEmpty(source)) {
        footer = new ColumnFooter(source, filterUpdater);
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

      BeeColumn dataColumn = editableColumn.getColumnForUpdate();
      String oldValue = editableColumn.getOldValueForUpdate(rowValue);

      ValueType valueType = dataColumn.getType();
      String newValue = Value.getNullString(valueType);
      if (BeeUtils.isEmpty(oldValue) || BeeUtils.same(oldValue, newValue)) {
        return;
      }

      updateCell(rowValue, dataColumn, oldValue, newValue, editableColumn.getRowModeForUpdate());
      return;
    }

    if (ValueType.BOOLEAN.equals(editableColumn.getDataColumn().getType())
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

    Editor editor = editableColumn.ensureEditor(columnId);
    EditorDescription editorDescription = editableColumn.getEditorDescription();

    editableColumn.setRowValue(rowValue);
    editableColumn.setState(State.OPEN);

    Element sourceElement = event.getSourceElement();
    Element editorElement = editor.asWidget().getElement();
    adjustEditor(sourceElement, editor, editorElement, editorDescription);

    if (sourceElement != null) {
      sourceElement.blur();
    }

    StyleUtils.setZIndex(editorElement, getGrid().getZIndex() + 1);
    StyleUtils.unhideDisplay(editorElement);
    editor.setFocus(true);

    editor.setEditing(true);
    editor.startEdit(rowValue.getString(editableColumn.getColIndex()),
        BeeUtils.toChar(event.getCharCode()),
        editorDescription == null ? null : editorDescription.getOnEntry());
  }

  public void refreshCellContent(long rowId, String columnSource) {
    getGrid().refreshCellContent(rowId, columnSource);
  }

  public void setViewPresenter(Presenter presenter) {
    this.viewPresenter = presenter;
  }

  public void setVisibleRange(int start, int length) {
    getGrid().setVisibleRange(start, length);
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

  private void adjustEditor(Element sourceElement, Editor editor, Element editorElement,
      EditorDescription editorDescription) {
    if (sourceElement != null) {
      if (editor instanceof AdjustmentListener) {
        ((AdjustmentListener) editor).adjust(sourceElement);
      } else {
        StyleUtils.copyBox(sourceElement, editorElement);
        StyleUtils.copyFont(sourceElement, editorElement);
      }
    }

    int left = StyleUtils.getLeft(editorElement);
    int width = StyleUtils.getWidth(editorElement);

    int top = StyleUtils.getTop(editorElement);
    int height = StyleUtils.getHeight(editorElement);

    int horMargins = 10;
    int vertMargins = 10;

    if (editorDescription != null) {
      int editorWidth = BeeConst.UNDEF;
      int editorHeight = BeeConst.UNDEF;
      int editorMinWidth = BeeConst.UNDEF;
      int editorMinHeight = BeeConst.UNDEF;

      EditorType editorType = editorDescription.getType();
      if (editorType != null) {
        if (BeeUtils.isPositive(editorType.getDefaultWidth())) {
          editorWidth = editorType.getDefaultWidth();
        }
        if (BeeUtils.isPositive(editorType.getDefaultHeight())) {
          editorHeight = editorType.getDefaultHeight();
        }
        if (BeeUtils.isPositive(editorType.getMinWidth())) {
          editorMinWidth = editorType.getMinWidth();
        }
        if (BeeUtils.isPositive(editorType.getMinHeight())) {
          editorMinHeight = editorType.getMinHeight();
        }
      }

      if (BeeUtils.isPositive(editorDescription.getWidth())) {
        editorWidth = editorDescription.getWidth();
      }
      if (BeeUtils.isPositive(editorDescription.getHeight())) {
        editorHeight = editorDescription.getHeight();
      }
      if (BeeUtils.isPositive(editorDescription.getMinWidth())) {
        editorMinWidth = editorDescription.getMinWidth();
      }
      if (BeeUtils.isPositive(editorDescription.getMinHeight())) {
        editorMinHeight = editorDescription.getMinHeight();
      }

      if (editorWidth > width) {
        StyleUtils.setWidth(editorElement, editorWidth);
        width = editorWidth;
      } else if (editorMinWidth > width) {
        StyleUtils.setWidth(editorElement, editorMinWidth);
        width = editorMinWidth;
      }

      if (editorHeight > height) {
        StyleUtils.setHeight(editorElement, editorHeight);
        height = editorHeight;
      } else if (editorMinHeight > height) {
        StyleUtils.setHeight(editorElement, editorMinHeight);
        height = editorMinHeight;
      }
    }

    int x = getGrid().getElement().getScrollLeft();
    int maxWidth = getGrid().getElement().getClientWidth();

    if (x > 0 || left + width + horMargins > maxWidth) {
      left -= x;
      int newWidth = width;
      if (left < 0) {
        newWidth += left;
        left = 0;
      }
      if (left + newWidth + horMargins > maxWidth) {
        if (left > 0) {
          left = Math.max(0, maxWidth - newWidth - horMargins);
        }
        if (left + newWidth + horMargins > maxWidth) {
          newWidth = maxWidth - left - horMargins;
        }
      }
      StyleUtils.setLeft(editorElement, left);
      if (newWidth > 0 && newWidth != width) {
        StyleUtils.setWidth(editorElement, newWidth);
      }
    }

    int y = getGrid().getElement().getScrollTop();
    int maxHeight = getGrid().getElement().getClientHeight();

    if (y > 0 || top + height + vertMargins > maxHeight) {
      top -= y;
      int newHeight = height;
      if (top < 0) {
        newHeight += top;
        top = 0;
      }
      if (top + newHeight + vertMargins > maxHeight) {
        if (top > 0) {
          top = Math.max(0, maxHeight - newHeight - vertMargins);
        }
        if (top + newHeight + vertMargins > maxHeight) {
          newHeight = maxHeight - top - vertMargins;
        }
      }
      StyleUtils.setTop(editorElement, top);
      if (newHeight > 0 && newHeight != height) {
        StyleUtils.setHeight(editorElement, newHeight);
      }
    }
  }

  private String ensureCancelWidget() {
    if (getCancelWidgetId() == null) {
      BeeImage cancel = new BeeImage(Global.getImages().cancel(), new CancelCommand());
      setCancelWidgetId(cancel.getId());
      StyleUtils.makeAbsolute(cancel);
      add(cancel);
    }
    return getCancelWidgetId();
  }

  private String ensureConfirmWidget() {
    if (getConfirmWidgetId() == null) {
      BeeImage confirm = new BeeImage(Global.getImages().ok(), new ConfirmationCommand());
      setConfirmWidgetId(confirm.getId());
      StyleUtils.makeAbsolute(confirm);
      add(confirm);
    }
    return getConfirmWidgetId();
  }

  private String getCancelWidgetId() {
    return cancelWidgetId;
  }

  private String getConfirmWidgetId() {
    return confirmWidgetId;
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

  private List<String> getNewRowColumns() {
    return newRowColumns;
  }

  private Notification getNotification() {
    return notification;
  }

  private Evaluator getRowEditable() {
    return rowEditable;
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
        
        if (!getNewRowColumns().contains(id)) {
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
    } else {
      for (Map.Entry<String, EditableColumn> entry : getEditableColumns().entrySet()) {
        if (!entry.getValue().isNullable()) {
          getNewRowColumns().add(entry.getKey());
        }
      }
    }
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

  private void restoreGrid() {
    for (EditableColumn column : getEditableColumns().values()) {
      if (column.getEditor() != null) {
        StyleUtils.hideDisplay(column.getEditor().asWidget());
      }
      if (column.getCaptionElementId() != null) {
        StyleUtils.hideDisplay(column.getCaptionElementId());
      }
    }
    
    if (getConfirmWidgetId() != null) {
      StyleUtils.hideDisplay(getConfirmWidgetId());
    }
    if (getCancelWidgetId() != null) {
      StyleUtils.hideDisplay(getCancelWidgetId());
    }
    
    getGrid().setEditing(false);
    StyleUtils.unhideDisplay(getGrid());
  }

  private void setCancelWidgetId(String cancelWidgetId) {
    this.cancelWidgetId = cancelWidgetId;
  }

  private void setConfirmWidgetId(String confirmWidgetId) {
    this.confirmWidgetId = confirmWidgetId;
  }
  
  private void setRowEditable(Evaluator rowEditable) {
    this.rowEditable = rowEditable;
  }

  private void showNote(Level level, String... messages) {
    StyleUtils.setZIndex(getNotification(), getGrid().getZIndex() + 1);
    getNotification().show(level, messages);
  }
  
  private void updateCell(IsRow rowValue, IsColumn dataColumn, String oldValue, String newValue,
      boolean rowMode) {
    getGrid().preliminaryUpdate(rowValue.getId(), dataColumn.getLabel(), newValue);
    fireEvent(new EditEndEvent(rowValue, dataColumn, oldValue, newValue, rowMode));
  }
}
