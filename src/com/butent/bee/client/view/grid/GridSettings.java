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
import com.butent.bee.client.view.grid.CellGrid.ColumnInfo;
import com.butent.bee.client.widget.BooleanWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.SimpleBoolean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.Flexibility;
import com.butent.bee.shared.ui.GridComponentDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.StyleDeclaration;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Map;

public class GridSettings {

  private static class ColumnConfig {

    private static List<BeeColumn> dataColumns = Lists.newArrayList();

    private static int gridIndex;
    private static int nameIndex;
    private static int captionIndex;

    private static int widthIndex;
    private static int minWidthIndex;
    private static int maxWidthIndex;

    private static int autoFitIndex;

    private static int flexGrowIndex;
    private static int flexShrinkIndex;
    private static int flexBasisIndex;
    private static int flexUnitIndex;

    private static int formatIndex;

    private static int headerStyleIndex;
    private static int headerFontIndex;

    private static int bodyStyleIndex;
    private static int bodyFontIndex;

    private static int footerStyleIndex;
    private static int footerFontIndex;

    private static void ensureIndexes(List<BeeColumn> columns) {
      if (dataColumns.isEmpty()) {
        dataColumns.addAll(columns);

        List<String> names = DataUtils.getColumnNames(columns);

        gridIndex = getIndex(names, "GridSetting");
        nameIndex = getIndex(names, "Name");
        captionIndex = getIndex(names, "Caption");

        widthIndex = getIndex(names, "Width");
        minWidthIndex = getIndex(names, "MinWidth");
        maxWidthIndex = getIndex(names, "MaxWidth");

        autoFitIndex = getIndex(names, "AutoFit");

        flexGrowIndex = getIndex(names, "FlexGrow");
        flexShrinkIndex = getIndex(names, "FlexShrink");
        flexBasisIndex = getIndex(names, "FlexBasis");
        flexUnitIndex = getIndex(names, "FlexBasisUnit");

        formatIndex = getIndex(names, "Format");

        headerStyleIndex = getIndex(names, "HeaderStyle");
        headerFontIndex = getIndex(names, "HeaderFont");

        bodyStyleIndex = getIndex(names, "BodyStyle");
        bodyFontIndex = getIndex(names, "BodyFont");

        footerStyleIndex = getIndex(names, "FooterStyle");
        footerFontIndex = getIndex(names, "FooterFont");
      }
    }

    private final BeeRow row;

    private ColumnConfig(BeeRow row) {
      this.row = row;
    }

    private void applyTo(ColumnDescription columnDescription) {
      String caption = getCaption();
      if (!BeeUtils.isEmpty(caption)) {
        columnDescription.setCaption(caption);
      }

      Integer width = getWidth();
      if (BeeUtils.isPositive(width)) {
        columnDescription.setWidth(width);
      }

      Integer minWidth = getMinWidth();
      if (BeeUtils.isPositive(minWidth)) {
        columnDescription.setMinWidth(minWidth);
      }

      Integer maxWidth = getMaxWidth();
      if (BeeUtils.isPositive(maxWidth)) {
        columnDescription.setMaxWidth(maxWidth);
      }

      Boolean autoFit = getAutoFit();
      if (BeeUtils.isTrue(autoFit)) {
        columnDescription.setAutoFit(BeeConst.STRING_TRUE);
      }

      Flexibility flexibility = Flexibility.maybeCreate(getFlexGrow(), getFlexShrink(),
          getFlexBasis(), getFlexBasisUnit());
      if (flexibility != null) {
        if (columnDescription.getFlexibility() == null) {
          columnDescription.setFlexibility(flexibility);
        } else {
          columnDescription.getFlexibility().merge(flexibility);
        }
      }

      String format = getFormat();
      if (!BeeUtils.isEmpty(format)) {
        columnDescription.setFormat(format);
      }

      columnDescription.setHeaderStyle(StyleDeclaration.fuse(columnDescription.getHeaderStyle(),
          null, getHeaderStyle(), getHeaderFont()));
      columnDescription.setBodyStyle(StyleDeclaration.fuse(columnDescription.getBodyStyle(),
          null, getBodyStyle(), getBodyFont()));
      columnDescription.setFooterStyle(StyleDeclaration.fuse(columnDescription.getFooterStyle(),
          null, getFooterStyle(), getFooterFont()));
    }

    private Boolean getAutoFit() {
      return row.getBoolean(autoFitIndex);
    }

    private String getBodyFont() {
      return row.getString(bodyFontIndex);
    }

    private String getBodyStyle() {
      return row.getString(bodyStyleIndex);
    }

    private String getCaption() {
      return row.getString(captionIndex);
    }

    private Integer getFlexBasis() {
      return row.getInteger(flexBasisIndex);
    }

    private String getFlexBasisUnit() {
      return row.getString(flexUnitIndex);
    }

    private Integer getFlexGrow() {
      return row.getInteger(flexGrowIndex);
    }

    private Integer getFlexShrink() {
      return row.getInteger(flexShrinkIndex);
    }

    private String getFooterFont() {
      return row.getString(footerFontIndex);
    }

    private String getFooterStyle() {
      return row.getString(footerStyleIndex);
    }

    private String getFormat() {
      return row.getString(formatIndex);
    }

    private String getHeaderFont() {
      return row.getString(headerFontIndex);
    }

    private String getHeaderStyle() {
      return row.getString(headerStyleIndex);
    }

    private Integer getMaxWidth() {
      return row.getInteger(maxWidthIndex);
    }

    private Integer getMinWidth() {
      return row.getInteger(minWidthIndex);
    }

    private String getName() {
      return row.getString(nameIndex);
    }

    private Integer getWidth() {
      return row.getInteger(widthIndex);
    }

    private boolean isEmpty() {
      return getCaption() == null
          && getWidth() == null && getMinWidth() == null && getMaxWidth() == null
          && getAutoFit() == null
          && getFlexGrow() == null && getFlexShrink() == null
          && getFlexBasis() == null && getFlexBasisUnit() == null
          && getFormat() == null
          && getHeaderStyle() == null && getHeaderFont() == null
          && getBodyStyle() == null && getBodyFont() == null
          && getFooterStyle() == null && getFooterFont() == null;
    }
  }

  private static class GridConfig {

    private static List<BeeColumn> dataColumns = Lists.newArrayList();

    private static int userIndex;
    private static int keyIndex;
    private static int captionIndex;
    private static int columnsIndex;
    private static int orderIndex;

    private static int headerHeightIndex;
    private static int headerStyleIndex;
    private static int headerFontIndex;
    private static int headerPaddingIndex;
    private static int headerBorderIndex;
    private static int headerMarginIndex;

    private static int rowHeightIndex;
    private static int bodyStyleIndex;
    private static int bodyFontIndex;
    private static int bodyPaddingIndex;
    private static int bodyBorderIndex;
    private static int bodyMarginIndex;

    private static int footerHeightIndex;
    private static int footerStyleIndex;
    private static int footerFontIndex;
    private static int footerPaddingIndex;
    private static int footerBorderIndex;
    private static int footerMarginIndex;

    private static int autoFitIndex;
    private static int minColumnWidthIndex;
    private static int maxColumnWidthIndex;

    private static int flexGrowIndex;
    private static int flexShrinkIndex;
    private static int flexBasisIndex;
    private static int flexUnitIndex;

    private static void ensureIndexes(List<BeeColumn> columns) {
      if (dataColumns.isEmpty()) {
        dataColumns.addAll(columns);

        List<String> names = DataUtils.getColumnNames(columns);

        userIndex = getIndex(names, GridDescription.COL_GRID_SETTING_USER);
        keyIndex = getIndex(names, "Key");
        captionIndex = getIndex(names, "Caption");
        columnsIndex = getIndex(names, "Columns");
        orderIndex = getIndex(names, "Order");

        headerHeightIndex = getIndex(names, "HeaderHeight");
        headerStyleIndex = getIndex(names, "HeaderStyle");
        headerFontIndex = getIndex(names, "HeaderFont");
        headerPaddingIndex = getIndex(names, "HeaderPadding");
        headerBorderIndex = getIndex(names, "HeaderBorderWidth");
        headerMarginIndex = getIndex(names, "HeaderMargin");

        rowHeightIndex = getIndex(names, "RowHeight");
        bodyStyleIndex = getIndex(names, "BodyStyle");
        bodyFontIndex = getIndex(names, "BodyFont");
        bodyPaddingIndex = getIndex(names, "BodyPadding");
        bodyBorderIndex = getIndex(names, "BodyBorderWidth");
        bodyMarginIndex = getIndex(names, "BodyMargin");

        footerHeightIndex = getIndex(names, "FooterHeight");
        footerStyleIndex = getIndex(names, "FooterStyle");
        footerPaddingIndex = getIndex(names, "FooterPadding");
        footerFontIndex = getIndex(names, "FooterFont");
        footerBorderIndex = getIndex(names, "FooterBorderWidth");
        footerMarginIndex = getIndex(names, "FooterMargin");

        autoFitIndex = getIndex(names, "AutoFit");
        minColumnWidthIndex = getIndex(names, "MinColumnWidth");
        maxColumnWidthIndex = getIndex(names, "MaxColumnWidth");

        flexGrowIndex = getIndex(names, "FlexGrow");
        flexShrinkIndex = getIndex(names, "FlexShrink");
        flexBasisIndex = getIndex(names, "FlexBasis");
        flexUnitIndex = getIndex(names, "FlexBasisUnit");
      }
    }

    private final BeeRow row;

    private final Map<String, ColumnConfig> columnSettings = Maps.newHashMap();

    private GridConfig(BeeRow row) {
      this.row = row;
    }

    private void applyTo(GridDescription gridDescription) {
      String caption = getCaption();
      if (!BeeUtils.isEmpty(caption)) {
        gridDescription.setCaption(caption);
      }

      String order = getOrder();
      if (!BeeUtils.isEmpty(order)) {
        gridDescription.setOrder(Order.restore(order));
      }

      gridDescription.setHeader(fuseCompnent(gridDescription.getHeader(), getHeaderHeight(),
          getHeaderStyle(), getHeaderFont(), getHeaderPadding(), getHeaderBorderWidth(),
          getHeaderMargin()));

      gridDescription.setBody(fuseCompnent(gridDescription.getBody(), getRowHeight(),
          getBodyStyle(), getBodyFont(), getBodyPadding(), getBodyBorderWidth(), getBodyMargin()));

      gridDescription.setFooter(fuseCompnent(gridDescription.getFooter(), getFooterHeight(),
          getFooterStyle(), getFooterFont(), getFooterPadding(), getFooterBorderWidth(),
          getFooterMargin()));

      Boolean autoFit = getAutoFit();
      if (BeeUtils.isTrue(autoFit)) {
        gridDescription.setAutoFit(BeeConst.STRING_TRUE);
      }

      Integer minColumnWidth = getMinColumnWidth();
      if (BeeUtils.isPositive(minColumnWidth)) {
        gridDescription.setMinColumnWidth(minColumnWidth);
      }

      Integer maxColumnWidth = getMaxColumnWidth();
      if (BeeUtils.isPositive(maxColumnWidth)) {
        gridDescription.setMaxColumnWidth(maxColumnWidth);
      }

      Flexibility flexibility = Flexibility.maybeCreate(getFlexGrow(), getFlexShrink(),
          getFlexBasis(), getFlexBasisUnit());
      if (flexibility != null) {
        if (gridDescription.getFlexibility() == null) {
          gridDescription.setFlexibility(flexibility);
        } else {
          gridDescription.getFlexibility().merge(flexibility);
        }
      }

      List<String> visibleColumnNames = getColumns();

      if (!visibleColumnNames.isEmpty() || !columnSettings.isEmpty()) {
        List<String> names = Lists.newArrayList();
        for (ColumnDescription columnDescription : gridDescription.getColumns()) {
          names.add(columnDescription.getName());
        }

        for (Map.Entry<String, ColumnConfig> entry : columnSettings.entrySet()) {
          int index = names.indexOf(entry.getKey());
          if (index >= 0) {
            entry.getValue().applyTo(gridDescription.getColumns().get(index));
          } else {
            logger.warning("settings column not found:", entry.getKey());
          }
        }

        if (!visibleColumnNames.isEmpty()) {
          List<Integer> indexes = Lists.newArrayList();

          for (String name : visibleColumnNames) {
            int index = names.indexOf(name);

            if (index >= 0) {
              indexes.add(index);
            } else {
              logger.warning("visible column not found:", name);
            }
          }

          if (!indexes.isEmpty()) {
            List<ColumnDescription> columnDescriptions = Lists.newArrayList();

            for (int index : indexes) {
              ColumnDescription columnDescription = gridDescription.getColumns().get(index);
              if (BeeUtils.isFalse(columnDescription.getVisible())) {
                columnDescription.setVisible(null);
              }

              columnDescriptions.add(columnDescription);
            }

            int cc = names.size();
            if (columnDescriptions.size() < cc) {
              for (int i = 0; i < cc; i++) {
                if (!indexes.contains(i)) {
                  ColumnDescription columnDescription = gridDescription.getColumns().get(i);
                  if (columnDescription.getVisible() == null) {
                    columnDescription.setVisible(false);
                  }

                  columnDescriptions.add(columnDescription);
                }
              }
            }

            gridDescription.getColumns().clear();
            gridDescription.getColumns().addAll(columnDescriptions);
          }
        }

        for (ColumnDescription columnDescription : gridDescription.getColumns()) {
          ColumnConfig columnConfig = columnSettings.get(columnDescription.getName());
          if (columnConfig != null) {
            columnConfig.applyTo(columnDescription);
          }
        }
      }
    }

    private GridComponentDescription fuseCompnent(GridComponentDescription component,
        Integer height, String style, String font, String padding, String border, String margin) {

      if (!BeeUtils.isPositive(height) && BeeUtils.allEmpty(style, font, padding, border, margin)) {
        return component;
      }

      GridComponentDescription result;
      if (component == null) {
        result = new GridComponentDescription(height);
      } else {
        result = component.copy();
        if (BeeUtils.isPositive(height)) {
          result.setHeight(height);
        }
      }

      if (!BeeUtils.allEmpty(style, font)) {
        result.setStyle(StyleDeclaration.fuse(result.getStyle(), null, style, font));
      }

      if (!BeeUtils.isEmpty(padding)) {
        result.setPadding(padding);
      }
      if (!BeeUtils.isEmpty(border)) {
        result.setBorderWidth(border);
      }
      if (!BeeUtils.isEmpty(margin)) {
        result.setMargin(margin);
      }

      return result;
    }

    private Boolean getAutoFit() {
      return row.getBoolean(autoFitIndex);
    }

    private String getBodyBorderWidth() {
      return row.getString(bodyBorderIndex);
    }

    private String getBodyFont() {
      return row.getString(bodyFontIndex);
    }

    private String getBodyMargin() {
      return row.getString(bodyMarginIndex);
    }

    private String getBodyPadding() {
      return row.getString(bodyPaddingIndex);
    }

    private String getBodyStyle() {
      return row.getString(bodyStyleIndex);
    }

    private String getCaption() {
      return row.getString(captionIndex);
    }

    private List<String> getColumns() {
      return NameUtils.toList(row.getString(columnsIndex));
    }

    private Integer getFlexBasis() {
      return row.getInteger(flexBasisIndex);
    }

    private String getFlexBasisUnit() {
      return row.getString(flexUnitIndex);
    }

    private Integer getFlexGrow() {
      return row.getInteger(flexGrowIndex);
    }

    private Integer getFlexShrink() {
      return row.getInteger(flexShrinkIndex);
    }

    private String getFooterBorderWidth() {
      return row.getString(footerBorderIndex);
    }

    private String getFooterFont() {
      return row.getString(footerFontIndex);
    }

    private Integer getFooterHeight() {
      return row.getInteger(footerHeightIndex);
    }

    private String getFooterMargin() {
      return row.getString(footerMarginIndex);
    }

    private String getFooterPadding() {
      return row.getString(footerPaddingIndex);
    }

    private String getFooterStyle() {
      return row.getString(footerStyleIndex);
    }

    private String getHeaderBorderWidth() {
      return row.getString(headerBorderIndex);
    }

    private String getHeaderFont() {
      return row.getString(headerFontIndex);
    }

    private Integer getHeaderHeight() {
      return row.getInteger(headerHeightIndex);
    }

    private String getHeaderMargin() {
      return row.getString(headerMarginIndex);
    }

    private String getHeaderPadding() {
      return row.getString(headerPaddingIndex);
    }

    private String getHeaderStyle() {
      return row.getString(headerStyleIndex);
    }

    private Integer getMaxColumnWidth() {
      return row.getInteger(maxColumnWidthIndex);
    }

    private Integer getMinColumnWidth() {
      return row.getInteger(minColumnWidthIndex);
    }

    private String getOrder() {
      return row.getString(orderIndex);
    }

    private Integer getRowHeight() {
      return row.getInteger(rowHeightIndex);
    }

    private boolean isEmpty() {
      if (!columnSettings.isEmpty()) {
        for (ColumnConfig columnConfig : columnSettings.values()) {
          if (!columnConfig.isEmpty()) {
            return false;
          }
        }
      }

      return getCaption() == null
          && getColumns() == null && getOrder() == null
          && getHeaderHeight() == null && getHeaderStyle() == null 
          && getHeaderFont() == null && getHeaderPadding() == null 
          && getHeaderBorderWidth() == null && getHeaderMargin() == null
          && getRowHeight() == null && getBodyStyle() == null
          && getBodyFont() == null && getBodyPadding() == null
          && getBodyBorderWidth() == null && getBodyMargin() == null
          && getFooterHeight() == null && getFooterStyle() == null
          && getFooterFont() == null && getFooterPadding() == null
          && getFooterBorderWidth() == null && getFooterMargin() == null
          && getAutoFit() == null
          && getMinColumnWidth() == null && getMaxColumnWidth() == null
          && getFlexGrow() == null && getFlexShrink() == null
          && getFlexBasis() == null && getFlexBasisUnit() == null;
    }
  }

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

  public static void handle(final CellGrid grid, UIObject target) {
    Assert.notNull(grid);
    if (grid.getRowData().isEmpty()) {
      return;
    }

    List<ColumnInfo> predefinedColumns = grid.getPredefinedColumns();
    List<Integer> visibleColumns = grid.getVisibleColumns();

    final HtmlTable table = new HtmlTable();
    table.addStyleName(STYLE_TABLE);

    int row = 0;

    for (int index : visibleColumns) {
      table.setWidget(row, 0, createCheckBox(true));
      table.setWidget(row, 1, createLabel(predefinedColumns.get(index), index));

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

    Global.inputWidget("Stulpeliai", table, new InputCallback() {
      @Override
      public void onSuccess() {
        List<Integer> selectedColumns = Lists.newArrayList();

        for (int i = 0; i < table.getRowCount(); i++) {
          Widget checkBox = table.getWidget(i, 0);
          if (checkBox instanceof BooleanWidget
              && BeeUtils.isTrue(((BooleanWidget) checkBox).getValue())) {
            int index = DomUtils.getDataIndex(table.getWidget(i, 1).getElement());
            if (!BeeConst.isUndef(index)) {
              selectedColumns.add(index);
            }
          }
        }

        grid.updateVisibleColumns(selectedColumns);
      }
    }, STYLE_DIALOG, target);
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

  private static int getIndex(List<String> names, String name) {
    int index = names.indexOf(name);
    if (index < 0) {
      logger.warning("settings name not found:", name);
    }
    return index;
  }

  private static String getLabel(ColumnInfo columnInfo) {
    return BeeUtils.notEmpty(columnInfo.getCaption(), columnInfo.getColumnId());
  }

  private static String normalizeValue(String value) {
    return BeeUtils.isEmpty(value) ? null : value.trim();
  }

  private static void saveGridSetting(final String key, int index, String value) {
    Assert.notEmpty(key);
    Assert.isIndex(GridConfig.dataColumns, index);

    final BeeColumn dataColumn = GridConfig.dataColumns.get(index);
    final String newValue = normalizeValue(value);

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
