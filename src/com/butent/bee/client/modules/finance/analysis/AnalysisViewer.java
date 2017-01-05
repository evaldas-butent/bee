package com.butent.bee.client.modules.finance.analysis;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.finance.analysis.AnalysisCellType;
import com.butent.bee.shared.modules.finance.analysis.AnalysisLabel;
import com.butent.bee.shared.modules.finance.analysis.AnalysisResults;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitType;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitValue;
import com.butent.bee.shared.modules.finance.analysis.AnalysisValue;
import com.butent.bee.shared.modules.finance.analysis.AnalysisValueType;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AnalysisViewer extends Flow implements HasCaption {

  private static BeeLogger logger = LogUtils.getLogger(AnalysisViewer.class);

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "fin-AnalysisViewer-";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";
  private static final String STYLE_HEADER = STYLE_PREFIX + "header";

  private static final String STYLE_WRAPPER = STYLE_PREFIX + "wrapper";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private static final String STYLE_COLUMN = STYLE_PREFIX + "column";
  private static final String STYLE_ROW = STYLE_PREFIX + "row";

  private static final String STYLE_LABEL = STYLE_PREFIX + "label";
  private static final String STYLE_LABEL_PREFIX = STYLE_LABEL + "-";

  private static final String STYLE_SPLIT = STYLE_PREFIX + "split";
  private static final String STYLE_SPLIT_PREFIX = STYLE_SPLIT + "-";
  private static final String STYLE_SPLIT_EMPTY = STYLE_SPLIT_PREFIX + "empty";

  private static final String STYLE_TYPE = STYLE_PREFIX + "type";
  private static final String STYLE_TYPE_PREFIX = STYLE_TYPE + "-";

  private static final String STYLE_VALUE = STYLE_PREFIX + "value";
  private static final String STYLE_VALUE_PREFIX = STYLE_VALUE + "-";

  private static final String STYLE_PERFORMANCE = STYLE_PREFIX + "performance";
  private static final String STYLE_TIME = STYLE_PREFIX + "time";
  private static final String STYLE_DURATION = STYLE_PREFIX + "duration";

  private static final String PERIOD_SEPARATOR = " - ";

  private final AnalysisResults results;

  private final List<Long> columnIds = new ArrayList<>();
  private final Map<Long, List<AnalysisLabel>> columnLabels = new HashMap<>();

  private final Map<Long, List<AnalysisSplitType>> columnSplitTypes = new HashMap<>();
  private final Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> columnSplitValues =
      new HashMap<>();

  private final Map<Long, List<AnalysisCellType>> columnCellTypes = new HashMap<>();

  private final Map<Long, Integer> columnSpan = new HashMap<>();
  private final Map<Long, Map<AnalysisSplitType, Integer>> columnSplitSpan = new HashMap<>();

  private final List<Long> rowIds = new ArrayList<>();
  private final Map<Long, List<AnalysisLabel>> rowLabels = new HashMap<>();

  private final Map<Long, List<AnalysisSplitType>> rowSplitTypes = new HashMap<>();
  private final Map<Long, Map<AnalysisSplitType, List<AnalysisSplitValue>>> rowSplitValues =
      new HashMap<>();

  private final Map<Long, List<AnalysisCellType>> rowCellTypes = new HashMap<>();

  private final Map<Long, Integer> rowSpan = new HashMap<>();
  private final Map<Long, Map<AnalysisSplitType, Integer>> rowSplitSpan = new HashMap<>();

  private final Table<Long, Long, List<AnalysisValue>> values = HashBasedTable.create();

  AnalysisViewer(AnalysisResults results) {
    super(STYLE_CONTAINER);

    this.results = results;

    layout();
    render();
  }

  @Override
  public String getCaption() {
    return results.getHeaderString(COL_ANALYSIS_NAME);
  }

  private void layout() {
    for (BeeRow column : results.getColumns()) {
      if (results.isColumnVisible(column)) {
        long id = column.getId();
        columnIds.add(id);

        String period = formatRange(results.getColumnRange(column));
        columnLabels.put(id, results.getColumnLabels(column, period));

        List<AnalysisSplitType> st = results.getColumnSplitTypes(id);
        if (!BeeUtils.isEmpty(st)) {
          columnSplitTypes.put(id, st);
        }

        Map<AnalysisSplitType, List<AnalysisSplitValue>> sv = results.getColumnSplitValues(id);
        if (!BeeUtils.isEmpty(sv)) {
          columnSplitValues.put(id, sv);
        }

        List<AnalysisCellType> cellTypes = results.getColumnCellTypes(column);
        this.columnCellTypes.put(id, cellTypes);

        columnSpan.put(id, getSpan(st, sv, cellTypes));

        Map<AnalysisSplitType, Integer> splitSpan = getSplitSpan(st, sv, cellTypes);
        if (!BeeUtils.isEmpty(splitSpan)) {
          columnSplitSpan.put(id, splitSpan);
        }
      }
    }

    logger.debug("columns", columnIds);

    for (BeeRow row : results.getRows()) {
      if (results.isRowVisible(row)) {
        long id = row.getId();
        rowIds.add(id);

        String period = formatRange(results.getRowRange(row));
        rowLabels.put(id, results.getRowLabels(row, period));

        List<AnalysisSplitType> st = results.getRowSplitTypes(id);
        if (!BeeUtils.isEmpty(st)) {
          rowSplitTypes.put(id, st);
        }

        Map<AnalysisSplitType, List<AnalysisSplitValue>> sv = results.getRowSplitValues(id);
        if (!BeeUtils.isEmpty(sv)) {
          rowSplitValues.put(id, sv);
        }

        List<AnalysisCellType> cellTypes = results.getRowCellTypes(row);
        this.rowCellTypes.put(id, cellTypes);

        rowSpan.put(id, getSpan(st, sv, cellTypes));

        Map<AnalysisSplitType, Integer> splitSpan = getSplitSpan(st, sv, cellTypes);
        if (!BeeUtils.isEmpty(splitSpan)) {
          rowSplitSpan.put(id, splitSpan);
        }
      }
    }

    logger.debug("rows", rowIds);

    for (AnalysisValue value : results.getValues()) {
      long rowId = value.getRowId();
      long columnId = value.getColumnId();

      if (rowIds.contains(rowId) && columnIds.contains(columnId)) {
        if (values.contains(rowId, columnId)) {
          values.get(rowId, columnId).add(value);

        } else {
          List<AnalysisValue> list = new ArrayList<>();
          list.add(value);
          values.put(rowId, columnId, list);
        }
      }
    }

    logger.debug("values", values.size(),
        values.values().stream().mapToInt(List::size).sum());
  }

  private static int getSpan(List<AnalysisSplitType> splitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValues,
      List<AnalysisCellType> cellTypes) {

    int span = 1;

    if (!BeeUtils.isEmpty(splitTypes) && !BeeUtils.isEmpty(splitValues)) {
      for (AnalysisSplitType type : splitTypes) {
        int size = BeeUtils.size(splitValues.get(type));

        if (size > 1) {
          span *= size;
        }
      }
    }

    if (!BeeUtils.isEmpty(cellTypes)) {
      span *= cellTypes.size();
    }

    return span;
  }

  private static Map<AnalysisSplitType, Integer> getSplitSpan(List<AnalysisSplitType> splitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValues,
      List<AnalysisCellType> cellTypes) {

    Map<AnalysisSplitType, Integer> result = new HashMap<>();

    int span = 1;
    if (!BeeUtils.isEmpty(cellTypes)) {
      span *= cellTypes.size();
    }

    if (!BeeUtils.isEmpty(splitTypes) && !BeeUtils.isEmpty(splitValues)) {
      for (int i = splitTypes.size() - 1; i >= 0; i--) {
        AnalysisSplitType type = splitTypes.get(i);
        result.put(type, span);

        int size = BeeUtils.size(splitValues.get(type));
        if (size > 1) {
          span *= size;
        }
      }
    }

    return result;
  }

  public static String formatRange(MonthRange range) {
    if (range == null) {
      return BeeConst.STRING_EMPTY;
    }

    YearMonth minYm = range.getMinMonth();
    YearMonth maxYm = range.getMaxMonth();

    if (minYm.equals(maxYm)) {
      return Format.renderYearMonth(minYm);

    } else if (minYm.getYear() == maxYm.getYear()) {
      if (minYm.getMonth() == 1 && maxYm.getMonth() == 12) {
        return BeeUtils.toString(minYm.getYear());

      } else if (minYm.getMonth() % 3 == 1 && maxYm.getMonth() == minYm.getMonth() + 2) {
        return BeeUtils.joinWords(minYm.getYear(), Format.quarterFull(minYm.getQuarter()));

      } else {
        return BeeUtils.join(PERIOD_SEPARATOR,
            Format.renderYearMonth(minYm), Format.renderMonthFullStandalone(maxYm).toLowerCase());
      }

    } else {
      String lower = BeeUtils.isMore(minYm, ANALYSIS_MIN_YEAR_MONTH)
          ? Format.renderYearMonth(minYm) : BeeConst.STRING_EMPTY;
      String upper = BeeUtils.isLess(maxYm, ANALYSIS_MAX_YEAR_MONTH)
          ? Format.renderYearMonth(maxYm) : BeeConst.STRING_EMPTY;

      if (BeeUtils.allEmpty(lower, upper)) {
        return BeeConst.STRING_EMPTY;
      } else {
        return lower + PERIOD_SEPARATOR + upper;
      }
    }
  }

  private void render() {
    Horizontal performance = new Horizontal(STYLE_PERFORMANCE);

    if (Global.isDebug()) {
      performance.add(renderMillis(results.getInitStart()));
      performance.add(renderDuration(results.getValidateStart() - results.getInitStart()));

      performance.add(renderMillis(results.getValidateStart()));
      performance.add(renderDuration(results.getComputeStart() - results.getValidateStart()));

      performance.add(renderMillis(results.getComputeStart()));
      performance.add(renderDuration(results.getComputeEnd() - results.getComputeStart()));

      performance.add(renderMillis(results.getComputeEnd()));
      performance.add(renderDuration(results.getComputeEnd() - results.getInitStart()));

    } else {
      Label timeLabel = new Label(TimeUtils.renderDateTime(results.getComputeEnd()));
      timeLabel.addStyleName(STYLE_TIME);
      performance.add(timeLabel);

      long duration = results.getComputeEnd() - results.getInitStart();
      Label durationLabel = new Label(BeeUtils.bracket(TimeUtils.renderMillis(duration)));
      durationLabel.addStyleName(STYLE_DURATION);
      performance.add(durationLabel);
    }

    add(performance);

    Flow header = new Flow(STYLE_HEADER);
    results.getHeaderLabels(formatRange(results.getHeaderRange())).forEach(analysisLabel ->
        header.add(render(analysisLabel)));
    add(header);

    Flow wrapper = new Flow(STYLE_WRAPPER);
    wrapper.add(renderTable());
    add(wrapper);
  }

  private Widget renderTable() {
    int maxColumnLabels = columnLabels.values().stream()
        .mapToInt(List::size)
        .max()
        .orElseGet(BeeConst.INT_ZERO_SUPPLIER);

    int maxColumnSplitTypes = columnSplitTypes.values().stream()
        .mapToInt(List::size)
        .max()
        .orElseGet(BeeConst.INT_ZERO_SUPPLIER);

    boolean columnsNeedBudget = columnCellTypes.values().stream()
        .anyMatch(AnalysisCellType::needsBudget);

    int maxRowLabels = rowLabels.values().stream()
        .mapToInt(List::size)
        .max()
        .orElseGet(BeeConst.INT_ZERO_SUPPLIER);

    int maxRowSplitTypes = rowSplitTypes.values().stream()
        .mapToInt(List::size)
        .max()
        .orElseGet(BeeConst.INT_ZERO_SUPPLIER);

    boolean rowsNeedBudget = rowCellTypes.values().stream()
        .anyMatch(AnalysisCellType::needsBudget);

    int rStartValues = maxColumnLabels + maxColumnSplitTypes + (columnsNeedBudget ? 1 : 0);
    int cStartValues = maxRowLabels + maxRowSplitTypes + (rowsNeedBudget ? 1 : 0);

    HtmlTable table = new HtmlTable(STYLE_TABLE);

    int r;
    int c;

    if (maxColumnLabels > 0) {
      r = 0;
      c = cStartValues;

      for (long columnId : columnIds) {
        List<AnalysisLabel> labels = columnLabels.get(columnId);

        if (!BeeUtils.isEmpty(labels)) {
          Integer span = columnSpan.get(columnId);

          for (int i = 0; i < labels.size(); i++) {
            table.setWidgetAndStyle(r + i, c, render(labels.get(i)), STYLE_COLUMN);

            if (BeeUtils.isMore(span, 1)) {
              table.getCellFormatter().setColSpan(r + i, c, span);
            }
          }
        }

        c++;
      }

      logger.debug("column labels", maxColumnLabels);
    }

    if (maxColumnSplitTypes > 0) {
      r = maxColumnLabels;
      c = cStartValues;

      for (long columnId : columnIds) {
        List<AnalysisSplitType> splitTypes = columnSplitTypes.get(columnId);

        if (!BeeUtils.isEmpty(splitTypes)) {
          Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValuesByType =
              columnSplitValues.get(columnId);
          Map<AnalysisSplitType, Integer> splitSpan = columnSplitSpan.get(columnId);

          if (!BeeUtils.isEmpty(splitValuesByType) && !BeeUtils.isEmpty(splitSpan)) {
            for (int i = 0; i < splitTypes.size(); i++) {
              AnalysisSplitType type = splitTypes.get(i);

              List<AnalysisSplitValue> splitValues = splitValuesByType.get(type);
              Integer span = splitSpan.get(type);

              if (!BeeUtils.isEmpty(splitValues) && BeeUtils.isPositive(span)) {
                for (int j = 0; j < splitValues.size(); j++) {
                  table.setWidgetAndStyle(r + i, c + j * span,
                      render(type, splitValues.get(j)), STYLE_COLUMN);
                }
              }
            }
          }
        }

        c += columnSpan.get(columnId);
      }

      logger.debug("column splits", maxColumnSplitTypes);
    }

    if (columnsNeedBudget) {
      r = maxColumnLabels + maxColumnSplitTypes;
      c = cStartValues;

      for (long columnId : columnIds) {
        List<AnalysisCellType> cellTypes = columnCellTypes.get(columnId);
        int span = columnSpan.get(columnId);

        if (AnalysisCellType.needsBudget(cellTypes)) {
          int size = cellTypes.size();

          for (int i = 0; i < span; i += size) {
            for (int j = 0; j < size; j++) {
              table.setWidgetAndStyle(r, c + i * size + j, render(cellTypes.get(j)),
                  STYLE_COLUMN);
            }
          }
        }

        c += span;
      }

      logger.debug("column cell types");
    }

    r = rStartValues;

    for (long rowId : rowIds) {
      logger.debug("row", rowId);

      if (maxRowLabels > 0) {
        List<AnalysisLabel> labels = rowLabels.get(rowId);

        if (!BeeUtils.isEmpty(labels)) {
          c = 0;

          for (int i = 0; i < labels.size(); i++) {
            table.setWidgetAndStyle(r, c + i, render(labels.get(i)), STYLE_ROW);
          }

          logger.debug("labels", labels.size());
        }
      }

      if (maxRowSplitTypes > 0) {
        List<AnalysisSplitType> splitTypes = rowSplitTypes.get(rowId);

        if (!BeeUtils.isEmpty(splitTypes)) {
          c = maxRowLabels;

          Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValuesByType =
              rowSplitValues.get(rowId);
          Map<AnalysisSplitType, Integer> splitSpan = rowSplitSpan.get(rowId);

          if (!BeeUtils.isEmpty(splitValuesByType) && !BeeUtils.isEmpty(splitSpan)) {
            for (int i = 0; i < splitTypes.size(); i++) {
              AnalysisSplitType type = splitTypes.get(i);

              List<AnalysisSplitValue> splitValues = splitValuesByType.get(type);
              Integer span = splitSpan.get(type);

              if (!BeeUtils.isEmpty(splitValues) && BeeUtils.isPositive(span)) {
                for (int j = 0; j < splitValues.size(); j++) {
                  table.setWidgetAndStyle(r + j * span, c + i,
                      render(type, splitValues.get(j)), STYLE_ROW);
                }
              }
            }
          }

          logger.debug("splits", splitTypes.size());
        }
      }

      if (rowsNeedBudget) {
        List<AnalysisCellType> cellTypes = rowCellTypes.get(rowId);

        if (AnalysisCellType.needsBudget(cellTypes)) {
          c = maxRowLabels + maxRowSplitTypes;

          int size = cellTypes.size();
          int span = rowSpan.get(rowId);

          for (int i = 0; i < span; i += size) {
            for (int j = 0; j < size; j++) {
              table.setWidgetAndStyle(r + i * size + j, c, render(cellTypes.get(j)),
                  STYLE_ROW);
            }
          }

          logger.debug("cell types", size);
        }
      }

      c = cStartValues;
      for (long columnId : columnIds) {
        if (values.contains(rowId, columnId)) {
          List<AnalysisValue> analysisValues = values.get(rowId, columnId);

          renderValues(analysisValues, table, r, c);
          logger.debug("column", columnId, "values", analysisValues.size());
        }

        c += columnSpan.get(columnId);
      }

      r += rowSpan.get(rowId);
    }

    return table;
  }

  private void renderValues(Collection<AnalysisValue> analysisValues,
      HtmlTable table, int r, int c) {

    for (AnalysisValue value : analysisValues) {
      int rowOffset = getRowOffset(value);
      int columnOffset = getColumnOffset(value);

      List<AnalysisCellType> rowTypes = rowCellTypes.get(value.getRowId());
      List<AnalysisCellType> columnTypes = columnCellTypes.get(value.getColumnId());

      AnalysisCellType cellType;

      for (int i = 0; i < rowTypes.size(); i++) {
        AnalysisCellType rowType = rowTypes.get(i);

        for (int j = 0; j < columnTypes.size(); j++) {
          AnalysisCellType columnType = columnTypes.get(j);

          if (i > 0) {
            cellType = rowType;
          } else if (j > 0) {
            cellType = columnType;
          } else if (columnType.isDefault()) {
            cellType = rowType;
          } else {
            cellType = columnType;
          }

          String text = cellType.render(value);
          if (!BeeUtils.isEmpty(text)) {
            String typeSuffix = cellType.getAnalysisValueType().name().toLowerCase();
            table.setText(r + rowOffset + i, c + columnOffset + j, text,
                STYLE_VALUE, STYLE_VALUE_PREFIX + typeSuffix);
          }
        }
      }
    }
  }

  private int getColumnOffset(AnalysisValue value) {
    int offset = 0;

    Integer splitTypeIndex = value.getColumnSplitTypeIndex();
    Integer splitValueIndex = value.getColumnSplitValueIndex();

    if (splitTypeIndex == null || splitValueIndex == null) {
      return offset;
    }

    long id = value.getColumnId();

    List<AnalysisSplitType> splitTypes = columnSplitTypes.get(id);
    if (!BeeUtils.isIndex(splitTypes, splitTypeIndex)) {
      return offset;
    }

    AnalysisSplitType splitType = splitTypes.get(splitTypeIndex);
    List<AnalysisSplitValue> splitValues = columnSplitValues.get(id).get(splitType);

    if (!BeeUtils.isIndex(splitValues, splitValueIndex)) {
      return offset;
    }

    Integer splitSpan = columnSplitSpan.get(id).get(splitType);
    if (BeeUtils.isPositive(splitSpan)) {
      offset += splitValueIndex * splitSpan;
    } else {
      offset += splitValueIndex;
    }

    return offset;
  }

  private int getRowOffset(AnalysisValue value) {
    int offset = 0;

    Integer splitTypeIndex = value.getRowSplitTypeIndex();
    Integer splitValueIndex = value.getRowSplitValueIndex();

    if (splitTypeIndex == null || splitValueIndex == null) {
      return offset;
    }

    long id = value.getRowId();

    List<AnalysisSplitType> splitTypes = rowSplitTypes.get(id);
    if (!BeeUtils.isIndex(splitTypes, splitTypeIndex)) {
      return offset;
    }

    AnalysisSplitType splitType = splitTypes.get(splitTypeIndex);
    List<AnalysisSplitValue> splitValues = rowSplitValues.get(id).get(splitType);

    if (!BeeUtils.isIndex(splitValues, splitValueIndex)) {
      return offset;
    }

    Integer splitSpan = rowSplitSpan.get(id).get(splitType);
    if (BeeUtils.isPositive(splitSpan)) {
      offset += splitValueIndex * splitSpan;
    } else {
      offset += splitValueIndex;
    }

    return offset;
  }

  private static Widget renderMillis(long millis) {
    return new Label(TimeUtils.renderDateTime(millis, true));
  }

  private static Widget renderDuration(long millis) {
    return new Label(TimeUtils.renderMillis(millis));
  }

  private static Widget render(AnalysisLabel label, String... styleNames) {
    Label widget = new Label(label.getText());

    if (styleNames != null) {
      for (String styleName : styleNames) {
        widget.addStyleName(styleName);
      }
    }

    widget.addStyleName(STYLE_LABEL);
    widget.addStyleName(STYLE_LABEL_PREFIX + label.getSource().toLowerCase());

    setColors(widget.getElement(), label.getBackground(), label.getForeground());

    return widget;
  }

  private static Widget render(AnalysisSplitType splitType, AnalysisSplitValue splitValue,
      String... styleNames) {

    String text = splitValue.isEmpty()
        ? BeeUtils.bracket(splitType.getCaption()) : splitValue.getValue();

    Label widget = new Label(text);

    if (styleNames != null) {
      for (String styleName : styleNames) {
        widget.addStyleName(styleName);
      }
    }

    widget.addStyleName(STYLE_SPLIT);
    widget.addStyleName(STYLE_SPLIT_PREFIX + splitType.name().toLowerCase());
    if (splitValue.isEmpty()) {
      widget.addStyleName(STYLE_SPLIT_EMPTY);
    }

    setColors(widget.getElement(), splitValue.getBackground(), splitValue.getForeground());

    return widget;
  }

  private static Widget render(AnalysisCellType cellType, String... styleNames) {
    AnalysisValueType valueType = cellType.getAnalysisValueType();
    Label widget = new Label(valueType.getCaption());

    if (styleNames != null) {
      for (String styleName : styleNames) {
        widget.addStyleName(styleName);
      }
    }

    widget.addStyleName(STYLE_TYPE);
    widget.addStyleName(STYLE_TYPE_PREFIX + valueType.name().toLowerCase());

    return widget;
  }

  private static void setColors(Element target, String bg, String fg) {
    if (!BeeUtils.same(bg, fg)) {
      if (!BeeUtils.isEmpty(bg)) {
        target.getStyle().setBackgroundColor(bg);
      }

      if (!BeeUtils.isEmpty(fg)) {
        target.getStyle().setColor(fg);
      }
    }
  }
}
