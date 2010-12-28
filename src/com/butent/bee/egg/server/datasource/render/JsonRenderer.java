package com.butent.bee.egg.server.datasource.render;

import com.google.common.collect.Lists;

import com.butent.bee.egg.server.datasource.base.DataSourceParameters;
import com.butent.bee.egg.server.datasource.base.ReasonType;
import com.butent.bee.egg.server.datasource.base.ResponseStatus;
import com.butent.bee.egg.server.datasource.base.StatusType;
import com.butent.bee.egg.server.datasource.base.Warning;
import com.butent.bee.egg.server.datasource.datatable.ColumnDescription;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableCell;
import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.datatable.value.BooleanValue;
import com.butent.bee.egg.server.datasource.datatable.value.DateTimeValue;
import com.butent.bee.egg.server.datasource.datatable.value.DateValue;
import com.butent.bee.egg.server.datasource.datatable.value.NumberValue;
import com.butent.bee.egg.server.datasource.datatable.value.TimeOfDayValue;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.ibm.icu.util.GregorianCalendar;

import java.util.List;
import java.util.Map;

public class JsonRenderer {

  public static StringBuilder appendCellJson(TableCell cell, 
      StringBuilder sb, boolean includeFormatting, boolean isLastColumn) {
    Value value = cell.getValue();
    ValueType type = cell.getType();
    StringBuilder valueJson = new StringBuilder();
    GregorianCalendar calendar;
    String escapedFormattedString = "";
    boolean isJsonNull = false;

    DateValue dateValue;
    TimeOfDayValue timeOfDayValue;
    if ((value == null) || (value.isNull())) {
      valueJson.append("null");
      isJsonNull = true;
    } else {
      switch (type) {
        case BOOLEAN:
          valueJson.append(((BooleanValue) value).getValue());
          break;
        case DATE:
          valueJson.append("new Date(");
          dateValue = (DateValue) value;
          valueJson.append(dateValue.getYear()).append(",");
          valueJson.append(dateValue.getMonth()).append(",");
          valueJson.append(dateValue.getDayOfMonth());
          valueJson.append(")");
          break;
        case NUMBER:
          valueJson.append(((NumberValue) value).getValue());
          break;
        case TEXT:
          valueJson.append("'");
          valueJson.append(EscapeUtil.jsonEscape(value.toString()));
          valueJson.append("'");
          break;
        case TIMEOFDAY:
          valueJson.append("[");
          timeOfDayValue = (TimeOfDayValue) value;
          valueJson.append(timeOfDayValue.getHours()).append(",");
          valueJson.append(timeOfDayValue.getMinutes()).append(",");
          valueJson.append(timeOfDayValue.getSeconds()).append(",");
          valueJson.append(timeOfDayValue.getMilliseconds());
          valueJson.append("]");
          break;
        case DATETIME:
          calendar = ((DateTimeValue) value).getCalendar();
          valueJson.append("new Date(");
          valueJson.append(calendar.get(GregorianCalendar.YEAR)).append(",");
          valueJson.append(calendar.get(GregorianCalendar.MONTH)).append(",");
          valueJson.append(calendar.get(GregorianCalendar.DAY_OF_MONTH));
          valueJson.append(",");
          valueJson.append(calendar.get(GregorianCalendar.HOUR_OF_DAY));
          valueJson.append(",");
          valueJson.append(calendar.get(GregorianCalendar.MINUTE)).append(",");
          valueJson.append(calendar.get(GregorianCalendar.SECOND));
          valueJson.append(")");
          break;
        default:
          throw new IllegalArgumentException("Illegal value Type " + type);
      }
    }

    String formattedValue = cell.getFormattedValue();
    if ((value != null) && !value.isNull() && (formattedValue != null)) {
      escapedFormattedString = EscapeUtil.jsonEscape(formattedValue);
      if ((type == ValueType.TEXT) && value.toString().equals(formattedValue)) {
        escapedFormattedString = "";
      }
    }

    if ((isLastColumn) || (!isJsonNull)) {
      sb.append("{");
      sb.append("v:").append(valueJson);
      if ((includeFormatting) && (!escapedFormattedString.equals(""))) {
        sb.append(",f:'").append(escapedFormattedString).append("'");
      }
      String customPropertiesString = getPropertiesMapString(cell.getCustomProperties());
      if (customPropertiesString != null) {
        sb.append(",p:").append(customPropertiesString);
      }
      sb.append("}");
    }
    return sb;
  }

  public static StringBuilder appendColumnDescriptionJson(ColumnDescription col,
      StringBuilder sb) {
    sb.append("{");
    sb.append("id:'").append(EscapeUtil.jsonEscape(col.getId())).append("',");
    sb.append("label:'").append(EscapeUtil.jsonEscape(col.getLabel())).append("',");
    sb.append("type:'").append(col.getType().getTypeCodeLowerCase()).append("',");
    sb.append("pattern:'").append(EscapeUtil.jsonEscape(col.getPattern())).append("'");

    String customPropertiesString = getPropertiesMapString(col.getCustomProperties());
    if (customPropertiesString != null) {
      sb.append(",p:").append(customPropertiesString);
    }

    sb.append("}");
    return sb;
  }

  public static String getSignature(DataTable data) {
    String tableAsString = renderDataTable(data, true, false).toString();
    long longHashCode = tableAsString.hashCode();
    return String.valueOf(Math.abs(longHashCode));
  }

  public static CharSequence renderDataTable(DataTable dataTable, boolean includeValues, 
      boolean includeFormatting) {
    if (dataTable.getColumnDescriptions().isEmpty()) {
      return "";
    }

    List<ColumnDescription> columnDescriptions = dataTable.getColumnDescriptions();

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("cols:[");

    ColumnDescription col;
    for (int colId = 0; colId < columnDescriptions.size(); colId++) {
      col = columnDescriptions.get(colId);
      appendColumnDescriptionJson(col, sb);
      if (colId != (columnDescriptions.size() - 1)) {
        sb.append(",");
      }
    }
    sb.append("]");

    if (includeValues) {
      sb.append(",rows:[");
      List<TableCell> cells;
      TableCell cell;

      List<TableRow> rows = dataTable.getRows();
      for (int rowId = 0; rowId < rows.size(); rowId++) {
        TableRow tableRow = rows.get(rowId);
        cells = tableRow.getCells();
        sb.append("{c:[");
        for (int cellId = 0; cellId < cells.size(); cellId++) {
          cell = cells.get(cellId);
          if (cellId < (cells.size() - 1)) {
            appendCellJson(cell, sb, includeFormatting, false);
            sb.append(",");
          } else {
            appendCellJson(cell, sb, includeFormatting, true);
          }
        }
        sb.append("]");

        String customPropertiesString = getPropertiesMapString(tableRow.getCustomProperties());
        if (customPropertiesString != null) {
          sb.append(",p:").append(customPropertiesString);
        }

        sb.append("}");
        if ((rows.size() - 1) > rowId) {
          sb.append(",");
        }
      }

      sb.append("]");
    }

    String customPropertiesString = getPropertiesMapString(dataTable.getCustomProperties());
    if (customPropertiesString != null) {
      sb.append(",p:").append(customPropertiesString);
    }

    sb.append("}");
    return sb;
  }

  public static CharSequence renderJsonResponse(DataSourceParameters dsParams,
      ResponseStatus responseStatus, DataTable data, boolean isJsonp) {
    StringBuilder sb = new StringBuilder();
    if (isJsonp) {
      sb.append(dsParams.getResponseHandler()).append("(");
    }
    sb.append("{version:'0.6'");

    String requestId = dsParams.getRequestId();
    if (requestId != null) {
      sb.append(",reqId:'").append(EscapeUtil.jsonEscape(requestId)).append("'");
    }

    String previousSignature = dsParams.getSignature();
    if (responseStatus == null) {
      if (!BeeUtils.isEmpty(previousSignature) && (data != null)
          && (JsonRenderer.getSignature(data).equals(previousSignature))) {
        responseStatus = new ResponseStatus(StatusType.ERROR, ReasonType.NOT_MODIFIED, null);
      } else {
        responseStatus = new ResponseStatus(StatusType.OK, null, null);
      }
    }

    StatusType statusType = responseStatus.getStatusType();
    sb.append(",status:'").append(statusType.lowerCaseString()).append("'");

    if (statusType != StatusType.OK) {
      if (statusType == StatusType.WARNING) {
        List<Warning> warnings = data.getWarnings();
        List<String> warningJsonStrings = Lists.newArrayList();
        if (warnings != null) {
          for (Warning warning : warnings) {
            warningJsonStrings.add(getFaultString(warning.getReasonType(), warning.getMessage()));
          }
        }
        BeeUtils.append(sb.append(",warnings:["), warningJsonStrings, ",").append("]");

      } else {
        sb.append(",errors:[");
        sb.append(getFaultString(responseStatus.getReasonType(), responseStatus.getDescription()));
        sb.append("]");
      }
    }
    
    if ((statusType != StatusType.ERROR) && (data != null)) {
      sb.append(",sig:'").append(JsonRenderer.getSignature(data)).append("'");
      sb.append(",table:").append(JsonRenderer.renderDataTable(data, true, true));
    }
    
    sb.append("}");
    if (isJsonp) {
      sb.append(");");
    }
    
    return sb.toString();
  }

  private static String getFaultString(ReasonType reasonType, String description) {
    List<String> objectParts = Lists.newArrayList();
    if (reasonType != null) {
      objectParts.add("reason:'" + reasonType.lowerCaseString() + "'");
      objectParts.add("message:'" + EscapeUtil.jsonEscape(
              reasonType.getMessageForReasonType(null)) + "'");
    }

    if (description != null) {
      objectParts.add("detailed_message:'" + EscapeUtil.jsonEscape(description) + "'");
    }
    return BeeUtils.append(new StringBuilder("{"), objectParts, ",").append("}").toString();
  }

  private static String getPropertiesMapString(Map<String, String> propertiesMap) {
    String customPropertiesString = null;
    if ((propertiesMap != null) && (!propertiesMap.isEmpty())) {
      List<String> customPropertiesStrings = Lists.newArrayList();
      for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
        customPropertiesStrings.add("'"
            + EscapeUtil.jsonEscape(entry.getKey()) + "':'"
            + EscapeUtil.jsonEscape(entry.getValue()) + "'");
      }
      customPropertiesString = BeeUtils.append(new StringBuilder("{"),
          customPropertiesStrings, ",").append("}").toString();
    }
    return customPropertiesString;
  }

  private JsonRenderer() {
  }
}
