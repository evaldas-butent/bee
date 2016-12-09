package com.butent.bee.server.modules.finance;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplit;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class AnalysisFormData {

  private final BeeRow header;
  private final Map<String, Integer> headerIndexes;

  private final Map<String, Integer> columnIndexes;
  private final List<BeeRow> columns = new ArrayList<>();
  private final List<Integer> selectedColumns = new ArrayList<>();

  private final Map<String, Integer> rowIndexes;
  private final List<BeeRow> rows = new ArrayList<>();
  private final List<Integer> selectedRows = new ArrayList<>();

  private final List<BeeRow> headerFilters = new ArrayList<>();
  private final Map<Long, BeeRow> columnFilters = new HashMap<>();
  private final Map<Long, BeeRow> rowFilters = new HashMap<>();
  private final Map<String, Integer> filterIndexes;

  AnalysisFormData(BeeRowSet headerData, BeeRowSet columnData, BeeRowSet rowData,
      BeeRowSet filterData) {

    this.header = DataUtils.isEmpty(headerData) ? null : headerData.getRow(0);
    this.headerIndexes = AnalysisUtils.getIndexes(headerData);

    this.columnIndexes = AnalysisUtils.getIndexes(columnData);

    if (!DataUtils.isEmpty(columnData)) {
      this.columns.addAll(columnData.getRows());

      int index = columnIndexes.get(COL_ANALYSIS_COLUMN_SELECTED);
      for (int i = 0; i < columns.size(); i++) {
        if (columns.get(i).isTrue(index)) {
          selectedColumns.add(i);
        }
      }
    }

    this.rowIndexes = AnalysisUtils.getIndexes(rowData);

    if (!DataUtils.isEmpty(rowData)) {
      this.rows.addAll(rowData.getRows());

      int index = rowIndexes.get(COL_ANALYSIS_ROW_SELECTED);
      for (int i = 0; i < rows.size(); i++) {
        if (rows.get(i).isTrue(index)) {
          selectedRows.add(i);
        }
      }
    }

    this.filterIndexes = AnalysisUtils.getIndexes(filterData);

    if (!DataUtils.isEmpty(filterData)) {
      for (BeeRow row : filterData) {
        Long parent = row.getLong(filterIndexes.get(COL_ANALYSIS_HEADER));

        if (DataUtils.isId(parent)) {
          headerFilters.add(row);

        } else {
          parent = row.getLong(filterIndexes.get(COL_ANALYSIS_COLUMN));
          if (DataUtils.isId(parent)) {
            columnFilters.put(parent, row);

          } else {
            parent = row.getLong(filterIndexes.get(COL_ANALYSIS_ROW));
            if (DataUtils.isId(parent)) {
              rowFilters.put(parent, row);
            }
          }
        }
      }
    }
  }

  List<String> validate(Dictionary dictionary) {
    List<String> messages = new ArrayList<>();

    if (header == null) {
      messages.add("header not available");
      return messages;
    }
    if (BeeUtils.isEmpty(selectedColumns)) {
      messages.add(BeeUtils.isEmpty(columns) ? "columns not available" : "columns not selected");
    }
    if (BeeUtils.isEmpty(selectedRows)) {
      messages.add(BeeUtils.isEmpty(rows) ? "rows not available" : "rows not selected");
    }

    Integer yearFrom = getHeaderInteger(COL_ANALYSIS_HEADER_YEAR_FROM);
    Integer monthFrom = getHeaderInteger(COL_ANALYSIS_HEADER_MONTH_FROM);
    Integer yearUntil = getHeaderInteger(COL_ANALYSIS_HEADER_YEAR_UNTIL);
    Integer monthUntil = getHeaderInteger(COL_ANALYSIS_HEADER_MONTH_UNTIL);

    MonthRange headerRange;

    if (AnalysisUtils.isValidRange(yearFrom, monthFrom, yearUntil, monthUntil)) {
      headerRange = AnalysisUtils.getRange(yearFrom, monthFrom, yearUntil, monthUntil);
    } else {
      headerRange = null;
      messages.add(dictionary.invalidPeriod(AnalysisUtils.formatYearMonth(yearFrom, monthFrom),
          AnalysisUtils.formatYearMonth(yearUntil, monthUntil)));
    }

    if (!BeeUtils.isEmpty(selectedColumns)) {
      Integer splitLevels = getHeaderInteger(COL_ANALYSIS_COLUMN_SPLIT_LEVELS);

      for (int position : selectedColumns) {
        BeeRow column = columns.get(position);
        String columnLabel = getColumnLabel(dictionary, column);

        Integer y1 = getColumnInteger(column, COL_ANALYSIS_COLUMN_YEAR_FROM);
        Integer m1 = getColumnInteger(column, COL_ANALYSIS_COLUMN_MONTH_FROM);
        Integer y2 = getColumnInteger(column, COL_ANALYSIS_COLUMN_YEAR_UNTIL);
        Integer m2 = getColumnInteger(column, COL_ANALYSIS_COLUMN_MONTH_UNTIL);

        if (!AnalysisUtils.isValidRange(y1, m1, y2, m2)) {
          messages.add(BeeUtils.joinWords(columnLabel,
              dictionary.invalidPeriod(AnalysisUtils.formatYearMonth(y1, m1),
                  AnalysisUtils.formatYearMonth(y2, m2))));

        } else if (headerRange != null
            && !headerRange.intersects(AnalysisUtils.getRange(y1, m1, y2, m2))) {

          messages.add(BeeUtils.joinWords(columnLabel,
              "column and header periods do not intersect"));
        }

        List<AnalysisSplit> splits = getColumnSplits(column, splitLevels);
        if (!splits.isEmpty()) {
          if (AnalysisSplit.validateSplits(splits)) {
            for (AnalysisSplit split : splits) {
              if (!split.visibleForColumns()) {
                messages.add(BeeUtils.joinWords(columnLabel,
                    Localized.dictionary().finAnalysisInvalidSplit(), split));
              }
            }

          } else {
            messages.add(BeeUtils.joinWords(columnLabel,
                Localized.dictionary().finAnalysisInvalidSplit(), splits));
          }
        }
      }

      messages.addAll(validateAbbreviations(columns,
          columnIndexes.get(COL_ANALYSIS_COLUMN_ABBREVIATION),
          column -> getColumnLabel(dictionary, column)));
    }

    if (!BeeUtils.isEmpty(selectedRows)) {
      Integer splitLevels = getHeaderInteger(COL_ANALYSIS_ROW_SPLIT_LEVELS);

      for (int position : selectedRows) {
        BeeRow row = rows.get(position);
        String rowLabel = getRowLabel(dictionary, row);

        List<AnalysisSplit> splits = getRowSplits(row, splitLevels);
        if (!splits.isEmpty()) {
          if (AnalysisSplit.validateSplits(splits)) {
            for (AnalysisSplit split : splits) {
              if (!split.visibleForRows()) {
                messages.add(BeeUtils.joinWords(rowLabel,
                    Localized.dictionary().finAnalysisInvalidSplit(), split));
              }
            }

          } else {
            messages.add(BeeUtils.joinWords(rowLabel,
                Localized.dictionary().finAnalysisInvalidSplit(), splits));
          }
        }
      }

      messages.addAll(validateAbbreviations(rows, rowIndexes.get(COL_ANALYSIS_ROW_ABBREVIATION),
          row -> getRowLabel(dictionary, row)));
    }

    return messages;
  }

  private Integer getHeaderInteger(String key) {
    return header.getInteger(headerIndexes.get(key));
  }

  private Long getHeaderLong(String key) {
    return header.getLong(headerIndexes.get(key));
  }

  private String getHeaderString(String key) {
    return header.getString(headerIndexes.get(key));
  }

  private boolean isHeaderTrue(String key) {
    return header.isTrue(headerIndexes.get(key));
  }

  private String getColumnLabel(Dictionary dictionary, BeeRow column) {
    String label = BeeUtils.joinWords(getColumnString(column, COL_ANALYSIS_COLUMN_ORDINAL),
        BeeUtils.notEmpty(getColumnString(column, COL_ANALYSIS_COLUMN_NAME),
            getColumnString(column, COL_ANALYSIS_COLUMN_ABBREVIATION)));

    if (BeeUtils.isEmpty(label)) {
      return BeeUtils.joinWords(dictionary.column(), DataUtils.ID_TAG, column.getId());
    } else {
      return BeeUtils.joinWords(dictionary.column(), label);
    }
  }

  private Integer getColumnInteger(BeeRow column, String key) {
    return column.getInteger(columnIndexes.get(key));
  }

  private Long getColumnLong(BeeRow column, String key) {
    return column.getLong(columnIndexes.get(key));
  }

  private String getColumnString(BeeRow column, String key) {
    return column.getString(columnIndexes.get(key));
  }

  private List<AnalysisSplit> getColumnSplits(BeeRow column, Integer levels) {
    List<AnalysisSplit> splits = new ArrayList<>();

    if (BeeUtils.isPositive(levels)) {
      int max = Math.min(levels, COL_ANALYSIS_COLUMN_SPLIT.length);

      for (int i = 0; i < max; i++) {
        String value = getColumnString(column, COL_ANALYSIS_COLUMN_SPLIT[i]);
        AnalysisSplit split = EnumUtils.getEnumByName(AnalysisSplit.class, value);

        if (split != null) {
          splits.add(split);
        }
      }
    }

    return splits;
  }

  private String getRowLabel(Dictionary dictionary, BeeRow row) {
    String label = BeeUtils.joinWords(getRowString(row, COL_ANALYSIS_ROW_ORDINAL),
        BeeUtils.notEmpty(getRowString(row, COL_ANALYSIS_ROW_NAME),
            getRowString(row, COL_ANALYSIS_ROW_ABBREVIATION)));

    if (BeeUtils.isEmpty(label)) {
      return BeeUtils.joinWords(dictionary.row(), DataUtils.ID_TAG, row.getId());
    } else {
      return BeeUtils.joinWords(dictionary.row(), label);
    }
  }

  private Integer getRowInteger(BeeRow row, String key) {
    return row.getInteger(rowIndexes.get(key));
  }

  private Long getRowLong(BeeRow row, String key) {
    return row.getLong(rowIndexes.get(key));
  }

  private String getRowString(BeeRow row, String key) {
    return row.getString(rowIndexes.get(key));
  }

  private List<AnalysisSplit> getRowSplits(BeeRow row, Integer levels) {
    List<AnalysisSplit> splits = new ArrayList<>();

    if (BeeUtils.isPositive(levels)) {
      int max = Math.min(levels, COL_ANALYSIS_ROW_SPLIT.length);

      for (int i = 0; i < max; i++) {
        String value = getRowString(row, COL_ANALYSIS_ROW_SPLIT[i]);
        AnalysisSplit split = EnumUtils.getEnumByName(AnalysisSplit.class, value);

        if (split != null) {
          splits.add(split);
        }
      }
    }

    return splits;
  }

  private static List<String> validateAbbreviations(Collection<BeeRow> input,
      int abbreviationIndex, Function<BeeRow, String> labelFunction) {

    List<String> messages = new ArrayList<>();
    Multiset<String> abbreviations = HashMultiset.create();

    if (!BeeUtils.isEmpty(input)) {
      for (BeeRow row : input) {
        String abbreviation = row.getString(abbreviationIndex);

        if (!BeeUtils.isEmpty(abbreviation)) {
          if (AnalysisUtils.isValidAbbreviation(abbreviation)) {
            abbreviations.add(abbreviation);
          } else {
            messages.add(BeeUtils.joinWords(labelFunction.apply(row),
                Localized.dictionary().finAnalysisInvalidAbbreviation(), abbreviation));
          }
        }
      }

      if (abbreviations.size() > abbreviations.entrySet().size()) {
        for (BeeRow row : input) {
          String abbreviation = row.getString(abbreviationIndex);

          if (!BeeUtils.isEmpty(abbreviation) && abbreviations.count(abbreviation) > 1) {
            messages.add(BeeUtils.joinWords(labelFunction.apply(row),
                Localized.dictionary().valueNotUnique(abbreviation)));
          }
        }
      }
    }

    return messages;
  }
}
