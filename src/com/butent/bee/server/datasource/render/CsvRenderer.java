package com.butent.bee.server.datasource.render;

import com.butent.bee.server.datasource.base.ResponseStatus;
import com.butent.bee.server.datasource.util.ValueFormatter;
import com.butent.bee.shared.data.IsCell;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import com.ibm.icu.util.ULocale;

import java.util.List;
import java.util.Map;

public class CsvRenderer {

  public static String renderCsvError(ResponseStatus responseStatus) {
    StringBuilder sb = new StringBuilder();
    sb.append("Error: ").append(responseStatus.getReasonType().getMessageForReasonType());
    sb.append(". ").append(responseStatus.getDescription());
    return escapeString(sb.toString());
  }

  public static CharSequence renderDataTable(IsTable dataTable, ULocale locale,
      String separator) {
    if (separator == null) {
      separator = ",";
    }

    if (dataTable.getColumns().isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    List<IsColumn> columns = dataTable.getColumns();
    for (IsColumn column : columns) {
      sb.append(escapeString(column.getLabel())).append(separator);
    }

    Map<ValueType, ValueFormatter> formatters = ValueFormatter.createDefaultFormatters(locale);

    int length = sb.length();
    sb.replace(length - 1, length, "\n");

    List<IsRow> rows = dataTable.getRows();
    for (IsRow row : rows) {
      List<IsCell> cells = row.getCells();
      for (IsCell cell : cells) {
        String formattedValue = cell.getFormattedValue();
        if (formattedValue == null) {
          formattedValue = formatters.get(cell.getType()).format(cell.getValue());
        }
        if (cell.isNull()) {
          sb.append("null");
        } else {
          ValueType type = cell.getType();
          if (formattedValue.indexOf(',') > -1 || type.equals(ValueType.TEXT)) {
            sb.append(escapeString(formattedValue));
          } else {
            sb.append(formattedValue);
          }
        }
        sb.append(separator);
      }

      length = sb.length();
      sb.replace(length - 1, length, "\n");
    }
    return sb.toString();
  }

  private static String escapeString(String input) {
    StringBuilder sb = new StringBuilder();
    sb.append("\"");
    sb.append(BeeUtils.replace(input, "\"", "\"\""));
    sb.append("\"");
    return sb.toString();
  }

  private CsvRenderer() {
  }
}
