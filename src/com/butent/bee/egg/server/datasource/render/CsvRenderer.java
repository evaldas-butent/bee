package com.butent.bee.egg.server.datasource.render;

import com.butent.bee.egg.server.datasource.base.ResponseStatus;
import com.butent.bee.egg.server.datasource.datatable.ColumnDescription;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableCell;
import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.datatable.ValueFormatter;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.ibm.icu.util.ULocale;

import java.util.List;
import java.util.Map;

public class CsvRenderer {

  public static String renderCsvError(ResponseStatus responseStatus) {
    StringBuilder sb = new StringBuilder();
    sb.append("Error: ").append(responseStatus.getReasonType().getMessageForReasonType(null));
    sb.append(". ").append(responseStatus.getDescription());
    return escapeString(sb.toString());
  }

  public static CharSequence renderDataTable(DataTable dataTable, ULocale locale,
      String separator) {
    if (separator == null) {
      separator = ",";
    }

    if (dataTable.getColumnDescriptions().isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    List<ColumnDescription> columns = dataTable.getColumnDescriptions();
    for (ColumnDescription column : columns) {
      sb.append(escapeString(column.getLabel())).append(separator);
    }

    Map<ValueType, ValueFormatter> formatters = ValueFormatter.createDefaultFormatters(locale);

    int length = sb.length();
    sb.replace(length - 1, length, "\n");

    List<TableRow> rows = dataTable.getRows();
    for (TableRow row : rows) {
      List<TableCell> cells = row.getCells();
      for (TableCell cell : cells) {
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
