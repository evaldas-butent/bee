package com.butent.bee.server.datasource.render;

import com.google.common.collect.Lists;

import com.butent.bee.server.datasource.base.DataSourceParameters;
import com.butent.bee.server.datasource.base.ResponseStatus;
import com.butent.bee.server.datasource.base.StatusType;
import com.butent.bee.shared.BeeDate;
import com.butent.bee.shared.data.CustomProperties;
import com.butent.bee.shared.data.DataWarning;
import com.butent.bee.shared.data.IsCell;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.Reasons;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TimeOfDayValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class JsonRenderer {

  public static StringBuilder appendCellJson(IsCell cell,
      StringBuilder sb, boolean includeFormatting, boolean isLastColumn) {
    Value value = cell.getValue();
    ValueType type = cell.getType();
    StringBuilder valueJson = new StringBuilder();
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
          BeeDate date = ((DateTimeValue) value).getDateTime();
          valueJson.append("new Date(");
          valueJson.append(date.getYear()).append(",");
          valueJson.append(date.getMonth() - 1).append(",");
          valueJson.append(date.getDom()).append(",");
          valueJson.append(date.getHour()).append(",");
          valueJson.append(date.getMinute()).append(",");
          valueJson.append(date.getSecond()).append(")");
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
      String propertiesString = getPropertiesMapString(cell.getProperties());
      if (propertiesString != null) {
        sb.append(",p:").append(propertiesString);
      }
      sb.append("}");
    }
    return sb;
  }

  public static StringBuilder appendColumnDescriptionJson(IsColumn col, StringBuilder sb) {
    sb.append("{");
    sb.append("id:'").append(EscapeUtil.jsonEscape(col.getId())).append("',");
    sb.append("label:'").append(EscapeUtil.jsonEscape(col.getLabel())).append("',");
    sb.append("type:'").append(col.getType().getTypeCodeLowerCase()).append("',");
    sb.append("pattern:'").append(EscapeUtil.jsonEscape(col.getPattern())).append("'");

    String propertiesString = getPropertiesMapString(col.getProperties());
    if (propertiesString != null) {
      sb.append(",p:").append(propertiesString);
    }

    sb.append("}");
    return sb;
  }

  public static String getSignature(IsTable data) {
    String tableAsString = renderDataTable(data, true, false).toString();
    long longHashCode = tableAsString.hashCode();
    return String.valueOf(Math.abs(longHashCode));
  }

  public static CharSequence renderDataTable(IsTable dataTable, boolean includeValues,
      boolean includeFormatting) {
    if (dataTable.getColumns().isEmpty()) {
      return "";
    }

    List<IsColumn> columns = dataTable.getColumns();

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("cols:[");

    IsColumn col;
    for (int colId = 0; colId < columns.size(); colId++) {
      col = columns.get(colId);
      appendColumnDescriptionJson(col, sb);
      if (colId != (columns.size() - 1)) {
        sb.append(",");
      }
    }
    sb.append("]");

    if (includeValues) {
      sb.append(",rows:[");
      List<IsCell> cells;
      IsCell cell;

      List<IsRow> rows = dataTable.getRows();
      for (int rowId = 0; rowId < rows.size(); rowId++) {
        IsRow tableRow = rows.get(rowId);
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

        String propertiesString = getPropertiesMapString(tableRow.getProperties());
        if (propertiesString != null) {
          sb.append(",p:").append(propertiesString);
        }

        sb.append("}");
        if ((rows.size() - 1) > rowId) {
          sb.append(",");
        }
      }

      sb.append("]");
    }

    String propertiesString = getPropertiesMapString(dataTable.getTableProperties());
    if (propertiesString != null) {
      sb.append(",p:").append(propertiesString);
    }

    sb.append("}");
    return sb;
  }

  public static CharSequence renderJsonResponse(DataSourceParameters dsParams,
      ResponseStatus responseStatus, IsTable data, boolean isJsonp) {
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
        responseStatus = new ResponseStatus(StatusType.ERROR, Reasons.NOT_MODIFIED, null);
      } else {
        responseStatus = new ResponseStatus(StatusType.OK, null, null);
      }
    }

    StatusType statusType = responseStatus.getStatusType();
    sb.append(",status:'").append(statusType.lowerCaseString()).append("'");

    if (statusType != StatusType.OK) {
      if (statusType == StatusType.WARNING) {
        List<DataWarning> warnings = data.getWarnings();
        List<String> warningJsonStrings = Lists.newArrayList();
        if (warnings != null) {
          for (DataWarning warning : warnings) {
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

  private static String getFaultString(Reasons reasonType, String description) {
    List<String> objectParts = Lists.newArrayList();
    if (reasonType != null) {
      objectParts.add("reason:'" + reasonType.lowerCaseString() + "'");
      objectParts.add("message:'" + EscapeUtil.jsonEscape(reasonType.getMessageForReasonType())
          + "'");
    }

    if (description != null) {
      objectParts.add("detailed_message:'" + EscapeUtil.jsonEscape(description) + "'");
    }
    return BeeUtils.append(new StringBuilder("{"), objectParts, ",").append("}").toString();
  }

  private static String getPropertiesMapString(CustomProperties propertiesMap) {
    String customPropertiesString = null;
    if ((propertiesMap != null) && (!propertiesMap.isEmpty())) {
      List<String> customPropertiesStrings = Lists.newArrayList();
      for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
        customPropertiesStrings.add("'"
            + EscapeUtil.jsonEscape(entry.getKey()) + "':'"
            + EscapeUtil.jsonEscape(BeeUtils.transform(entry.getValue())) + "'");
      }
      customPropertiesString = BeeUtils.append(new StringBuilder("{"),
          customPropertiesStrings, ",").append("}").toString();
    }
    return customPropertiesString;
  }

  private JsonRenderer() {
  }
}
