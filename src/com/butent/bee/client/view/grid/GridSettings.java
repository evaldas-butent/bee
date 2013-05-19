package com.butent.bee.client.view.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.view.grid.CellGrid.ColumnInfo;
import com.butent.bee.client.widget.BooleanWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.SimpleBoolean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Map;

public class GridSettings {

  private static final BeeLogger logger = LogUtils.getLogger(GridSettings.class);

  private static final String STYLE_PREFIX = "bee-GridSettings-";
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_CHECK = STYLE_PREFIX + "check";
  private static final String STYLE_LABEL = STYLE_PREFIX + "label";

  private static final Map<String, GridConfig> grids = Maps.newHashMap();

  public static GridDescription apply(String key, GridDescription input) {
    GridConfig gridConfig = grids.get(key);

    if (gridConfig != null && !gridConfig.isEmpty()) {
      GridDescription gridDescription = input.copy();
      gridConfig.applyTo(gridDescription);
      return gridDescription;

    } else {
      return input;
    }
  }
  
  public static void handle(final String key, final CellGrid grid, UIObject target) {
    Assert.notNull(grid);
    if (grid.getRowData().isEmpty()) {
      return;
    }

    final List<ColumnInfo> predefinedColumns = grid.getPredefinedColumns();
    List<Integer> visibleColumns = grid.getVisibleColumns();

    final HtmlTable table = new HtmlTable();
    table.addStyleName(STYLE_TABLE);

    int row = 0;

    for (int index : visibleColumns) {
      ColumnInfo columnInfo = predefinedColumns.get(index);
      
      if (columnInfo.isHidable()) {
        table.setWidget(row, 0, createCheckBox(true));
      }
      table.setWidget(row, 1, createLabel(columnInfo, index));

      row++;
    }

    if (predefinedColumns.size() > visibleColumns.size()) {
      for (int i = 0; i < predefinedColumns.size(); i++) {
        if (!visibleColumns.contains(i)) {
          table.setWidget(row, 0, createCheckBox(false));
          table.setWidget(row, 1, createLabel(predefinedColumns.get(i), i));

          row++;
        }
      }
    }

    Global.inputWidget(Localized.constants.settings(), table, new InputCallback() {
      @Override
      public void onSuccess() {
        List<Integer> selectedColumns = Lists.newArrayList();

        for (int i = 0; i < table.getRowCount(); i++) {
          int index = DomUtils.getDataIndex(table.getWidget(i, 1).getElement());
          ColumnInfo columnInfo = predefinedColumns.get(index);
          
          boolean visible;
          
          if (columnInfo.isHidable()) {
            Widget checkBox = table.getWidget(i, 0);
            visible = checkBox instanceof BooleanWidget
                && BeeUtils.isTrue(((BooleanWidget) checkBox).getValue());
          } else {
            visible = true;
          }
          
          if (visible) {
            selectedColumns.add(index);
          }
        }

        if (grid.updateVisibleColumns(selectedColumns)) {
          List<String> names = Lists.newArrayList();

          List<ColumnInfo> columns = grid.getColumns();
          for (ColumnInfo columnInfo : columns) {
            names.add(columnInfo.getColumnId());
          }
          
          saveGridSetting(key, GridConfig.columnsIndex, NameUtils.join(names));
        }
      }
    }, STYLE_DIALOG, target);
  }

  public static boolean hasVisibleColumns(String key) {
    GridConfig gridConfig = grids.get(key);
    return gridConfig != null && gridConfig.hasVisibleColumns();
  }

  public static void load(String serializedGridSettings, String serializedColumnSettings) {
    grids.clear();

    if (!BeeUtils.isEmpty(serializedGridSettings)) {
      BeeRowSet gridRowSet = BeeRowSet.restore(serializedGridSettings);
      GridConfig.ensureIndexes(gridRowSet.getColumns());

      for (BeeRow gridRow : gridRowSet.getRows()) {
        grids.put(gridRow.getString(GridConfig.keyIndex), new GridConfig(gridRow));
      }

      logger.info("grid settings", grids.size());

      if (!BeeUtils.isEmpty(serializedColumnSettings)) {
        BeeRowSet columnRowSet = BeeRowSet.restore(serializedColumnSettings);
        ColumnConfig.ensureIndexes(columnRowSet.getColumns());

        GridConfig gridConfig = null;
        int cc = 0;

        for (BeeRow columnRow : columnRowSet.getRows()) {
          Long gridId = columnRow.getLong(ColumnConfig.gridIndex);
          if (gridConfig == null || !gridId.equals(gridConfig.row.getId())) {
            gridConfig = findGridByRowId(BeeUtils.unbox(gridId));
          }

          ColumnConfig columnConfig = new ColumnConfig(columnRow);
          gridConfig.columnSettings.put(columnConfig.getName(), columnConfig);

          cc++;
        }

        logger.info("column settings", cc);
      }
    }
  }

  public static void onSettingsChange(String key, SettingsChangeEvent event) {
    if (HasDimensions.ATTR_HEIGHT.equals(event.getAttribute())) {
      ensureGridIndexes();

      int index = BeeConst.UNDEF;

      if (event.getComponentType() != null) {
        switch (event.getComponentType()) {
          case HEADER:
            index = GridConfig.headerHeightIndex;
            break;
          case BODY:
            index = GridConfig.rowHeightIndex;
            break;
          case FOOTER:
            index = GridConfig.footerHeightIndex;
            break;
        }
      }

      if (!BeeConst.isUndef(index)) {
        saveGridSetting(key, index, event.getValue());
      }

    } else if (HasDimensions.ATTR_WIDTH.equals(event.getAttribute())) {
      ensureColumnIndexes();
      if (!BeeUtils.isEmpty(event.getColumnName())) {
        saveColumnSetting(key, event.getColumnName(), ColumnConfig.widthIndex, event.getValue());
      }
      
    } else {
      logger.warning(key, event.getComponentType(), event.getColumnName(), event.getAttribute(),
          event.getValue(), "not persisted");
    }
  }

  public static void saveSortOrder(String key, Order order) {
    ensureGridIndexes();
    saveGridSetting(key, GridConfig.orderIndex,
        (order == null || order.isEmpty()) ? null : order.serialize());
  }

  private static Widget createCheckBox(boolean value) {
    SimpleBoolean widget = new SimpleBoolean(value);
    widget.addStyleName(STYLE_CHECK);
    return widget;
  }

  private static Widget createLabel(ColumnInfo columnInfo, int index) {
    CustomDiv widget = new CustomDiv(STYLE_LABEL);

    widget.setHTML(getLabel(columnInfo));
    DomUtils.setDataIndex(widget.getElement(), index);

    return widget;
  }

  private static void ensureColumnIndexes() {
    ensureGridIndexes();
    if (ColumnConfig.dataColumns.isEmpty()) {
      ColumnConfig.ensureIndexes(Data.getColumns(ColumnDescription.VIEW_COLUMN_SETTINGS));
    }
  }

  private static void ensureGridIndexes() {
    if (GridConfig.dataColumns.isEmpty()) {
      GridConfig.ensureIndexes(Data.getColumns(GridDescription.VIEW_GRID_SETTINGS));
    }
  }

  private static GridConfig findGridByRowId(long id) {
    for (GridConfig gridConfig : grids.values()) {
      if (gridConfig.row.getId() == id) {
        return gridConfig;
      }
    }
    return null;
  }

  private static String getLabel(ColumnInfo columnInfo) {
    return BeeUtils.notEmpty(columnInfo.getCaption(), columnInfo.getColumnId());
  }

  private static void saveColumnSetting(final String key, final String name, final int index,
      final String value) {

    Assert.notEmpty(key);
    GridConfig gridConfig = grids.get(key);

    if (gridConfig != null) {
      gridConfig.saveColumnSetting(name, index, value);

    } else if (!BeeUtils.isEmpty(value)) {
      List<BeeColumn> columns = Lists.newArrayList();
      columns.add(GridConfig.dataColumns.get(GridConfig.userIndex));
      columns.add(GridConfig.dataColumns.get(GridConfig.keyIndex));

      List<String> values = Queries.asList(BeeKeeper.getUser().getUserId(), key);

      Queries.insert(GridDescription.VIEW_GRID_SETTINGS, columns, values, null,
          new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              GridConfig gcfg = new GridConfig(result);
              grids.put(key, gcfg);

              gcfg.saveColumnSetting(name, index, value);
            }
          });
    }
  }

  private static void saveGridSetting(final String key, int index, String value) {
    Assert.notEmpty(key);
    Assert.isIndex(GridConfig.dataColumns, index);

    final BeeColumn dataColumn = GridConfig.dataColumns.get(index);
    final String newValue = GridUtils.normalizeValue(value);

    GridConfig gridConfig = grids.get(key);
    if (gridConfig == null) {
      if (newValue != null) {
        List<BeeColumn> columns = Lists.newArrayList();
        columns.add(GridConfig.dataColumns.get(GridConfig.userIndex));
        columns.add(GridConfig.dataColumns.get(GridConfig.keyIndex));
        columns.add(dataColumn);

        List<String> values = Queries.asList(BeeKeeper.getUser().getUserId(), key, newValue);

        Queries.insert(GridDescription.VIEW_GRID_SETTINGS, columns, values, null,
            new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                grids.put(key, new GridConfig(result));
                logger.debug("created grid settings:", key, dataColumn.getId(), newValue);
              }
            });
      }

    } else if (!BeeUtils.equalsTrim(gridConfig.row.getString(index), newValue)) {
      gridConfig.row.setValue(index, newValue);

      Queries.update(GridDescription.VIEW_GRID_SETTINGS,
          ComparisonFilter.compareId(gridConfig.row.getId()), dataColumn.getId(),
          new TextValue(newValue), new Queries.IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              if (BeeUtils.unbox(result) == 1) {
                logger.debug("updated grid settings:", key, dataColumn.getId(), newValue);
              } else {
                logger.warning("could not update grid settings:", result);
                logger.warning(key, dataColumn.getId(), newValue);
              }
            }
          });
    }
  }

  private GridSettings() {
  }
}
