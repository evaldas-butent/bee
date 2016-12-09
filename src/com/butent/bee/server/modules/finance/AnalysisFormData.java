package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AnalysisFormData {

  private final BeeRow header;
  private final Map<String, Integer> headerIndexes;

  private final Map<String, Integer> columnIndexes;
  private final List<BeeRow> selectedColumns = new ArrayList<>();
  private final List<BeeRow> deselectedColumns = new ArrayList<>();

  private final Map<String, Integer> rowIndexes;
  private final List<BeeRow> selectedRows = new ArrayList<>();
  private final List<BeeRow> deselectedRows = new ArrayList<>();

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
      int index = columnIndexes.get(COL_ANALYSIS_COLUMN_SELECTED);

      for (BeeRow row : columnData) {
        if (row.isTrue(index)) {
          selectedColumns.add(row);
        } else {
          deselectedColumns.add(row);
        }
      }
    }

    this.rowIndexes = AnalysisUtils.getIndexes(rowData);
    if (!DataUtils.isEmpty(rowData)) {
      int index = rowIndexes.get(COL_ANALYSIS_ROW_SELECTED);

      for (BeeRow row : rowData) {
        if (row.isTrue(index)) {
          selectedRows.add(row);
        } else {
          deselectedRows.add(row);
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
      messages.add(BeeUtils.isEmpty(deselectedColumns)
          ? "columns not available" : "columns not selected");
    }
    if (BeeUtils.isEmpty(selectedRows)) {
      messages.add(BeeUtils.isEmpty(deselectedRows)
          ? "rows not available" : "rows not selected");
    }

    Integer yearFrom = header.getInteger(headerIndexes.get(COL_ANALYSIS_HEADER_YEAR_FROM));
    Integer monthFrom = header.getInteger(headerIndexes.get(COL_ANALYSIS_HEADER_MONTH_FROM));
    Integer yearUntil = header.getInteger(headerIndexes.get(COL_ANALYSIS_HEADER_YEAR_UNTIL));
    Integer monthUntil = header.getInteger(headerIndexes.get(COL_ANALYSIS_HEADER_MONTH_UNTIL));

    MonthRange headerRange;

    if (AnalysisUtils.isValidRange(yearFrom, monthFrom, yearUntil, monthUntil)) {
      headerRange = AnalysisUtils.getRange(yearFrom, monthFrom, yearUntil, monthUntil);
    } else {
      headerRange = null;
      messages.add(dictionary.invalidPeriod(AnalysisUtils.formatYearMonth(yearFrom, monthFrom),
          AnalysisUtils.formatYearMonth(yearUntil, monthUntil)));
    }

    if (!BeeUtils.isEmpty(selectedColumns)) {
      for (BeeRow column : selectedColumns) {
        String columnLabel = getColumnLabel(column);

        String abbreviation = column.getString(columnIndexes.get(COL_ANALYSIS_COLUMN_ABBREVIATION));
        if (!BeeUtils.isEmpty(abbreviation) && !AnalysisUtils.isValidAbbreviation(abbreviation)) {
          messages.add(BeeUtils.joinWords(columnLabel,
              Localized.dictionary().finAnalysisInvalidAbbreviation(), abbreviation));
        }

        Integer y1 = column.getInteger(columnIndexes.get(COL_ANALYSIS_COLUMN_YEAR_FROM));
        Integer m1 = column.getInteger(columnIndexes.get(COL_ANALYSIS_COLUMN_MONTH_FROM));
        Integer y2 = column.getInteger(columnIndexes.get(COL_ANALYSIS_COLUMN_YEAR_UNTIL));
        Integer m2 = column.getInteger(columnIndexes.get(COL_ANALYSIS_COLUMN_MONTH_UNTIL));

        if (!AnalysisUtils.isValidRange(y1, m1, y2, m2)) {
          messages.add(BeeUtils.joinWords(columnLabel,
              dictionary.invalidPeriod(AnalysisUtils.formatYearMonth(y1, m1),
                  AnalysisUtils.formatYearMonth(y2, m2))));

        } else if (headerRange != null
            && !headerRange.intersects(AnalysisUtils.getRange(y1, m1, y2, m2))) {

          messages.add(BeeUtils.joinWords(columnLabel,
              "column and header periods do not intersect"));
        }
      }
    }

    if (!BeeUtils.isEmpty(selectedRows)) {
      for (BeeRow row : selectedRows) {
        String rowLabel = getRowLabel(row);

        String abbreviation = row.getString(rowIndexes.get(COL_ANALYSIS_ROW_ABBREVIATION));
        if (!BeeUtils.isEmpty(abbreviation) && !AnalysisUtils.isValidAbbreviation(abbreviation)) {
          messages.add(BeeUtils.joinWords(rowLabel,
              Localized.dictionary().finAnalysisInvalidAbbreviation(), abbreviation));
        }
      }
    }

    return messages;
  }

  private String getColumnLabel(BeeRow column) {
    String label = BeeUtils.joinWords(
        column.getString(columnIndexes.get(COL_ANALYSIS_COLUMN_ORDINAL)),
        BeeUtils.notEmpty(column.getString(columnIndexes.get(COL_ANALYSIS_COLUMN_NAME)),
            column.getString(columnIndexes.get(COL_ANALYSIS_COLUMN_ABBREVIATION))));

    if (BeeUtils.isEmpty(label)) {
      return BeeUtils.joinWords(DataUtils.ID_TAG, column.getId());
    } else {
      return label;
    }
  }

  private String getRowLabel(BeeRow row) {
    String label = BeeUtils.joinWords(
        row.getString(rowIndexes.get(COL_ANALYSIS_ROW_ORDINAL)),
        BeeUtils.notEmpty(row.getString(rowIndexes.get(COL_ANALYSIS_ROW_NAME)),
            row.getString(rowIndexes.get(COL_ANALYSIS_ROW_ABBREVIATION))));

    if (BeeUtils.isEmpty(label)) {
      return BeeUtils.joinWords(DataUtils.ID_TAG, row.getId());
    } else {
      return label;
    }
  }
}
