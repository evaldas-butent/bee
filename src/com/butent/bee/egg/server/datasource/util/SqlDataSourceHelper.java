package com.butent.bee.egg.server.datasource.util;

import com.google.common.collect.Lists;

import com.butent.bee.egg.server.datasource.base.DataSourceException;
import com.butent.bee.egg.server.datasource.base.ReasonType;
import com.butent.bee.egg.server.datasource.base.TypeMismatchException;
import com.butent.bee.egg.server.datasource.datatable.ColumnDescription;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableCell;
import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.datatable.value.BooleanValue;
import com.butent.bee.egg.server.datasource.datatable.value.DateTimeValue;
import com.butent.bee.egg.server.datasource.datatable.value.DateValue;
import com.butent.bee.egg.server.datasource.datatable.value.NumberValue;
import com.butent.bee.egg.server.datasource.datatable.value.TextValue;
import com.butent.bee.egg.server.datasource.datatable.value.TimeOfDayValue;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.butent.bee.egg.server.datasource.query.AbstractColumn;
import com.butent.bee.egg.server.datasource.query.AggregationColumn;
import com.butent.bee.egg.server.datasource.query.AggregationType;
import com.butent.bee.egg.server.datasource.query.ColumnColumnFilter;
import com.butent.bee.egg.server.datasource.query.ColumnIsNullFilter;
import com.butent.bee.egg.server.datasource.query.ColumnSort;
import com.butent.bee.egg.server.datasource.query.ColumnValueFilter;
import com.butent.bee.egg.server.datasource.query.ComparisonFilter;
import com.butent.bee.egg.server.datasource.query.CompoundFilter;
import com.butent.bee.egg.server.datasource.query.NegationFilter;
import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.server.datasource.query.QueryFilter;
import com.butent.bee.egg.server.datasource.query.QueryGroup;
import com.butent.bee.egg.server.datasource.query.QuerySelection;
import com.butent.bee.egg.server.datasource.query.QuerySort;
import com.butent.bee.egg.server.datasource.query.SimpleColumn;
import com.butent.bee.egg.server.datasource.query.SortOrder;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.logging.Logger;

public class SqlDataSourceHelper {
  private static final Logger logger = Logger.getLogger(SqlDataSourceHelper.class.getName());

  public static DataTable executeQuery(Query query, SqlDatabaseDescription databaseDescription)
      throws DataSourceException {
    Connection con = getDatabaseConnection(databaseDescription);
    String tableName = databaseDescription.getTableName();

    StringBuilder queryStringBuilder = new StringBuilder();
    buildSqlQuery(query, queryStringBuilder, tableName);
    List<String> columnIdsList = null;
    if (query.hasSelection()) {
      columnIdsList = getColumnIdsList(query.getSelection());
    }
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(queryStringBuilder.toString());

      DataTable table = buildColumns(rs, columnIdsList);

      buildRows(table, rs);
      return table;
    } catch (SQLException e) {
      String messageToUser = "Failed to execute SQL query. mySQL error message:"
          + " " + e.getMessage();
      throw new DataSourceException(ReasonType.INTERNAL_ERROR, messageToUser);
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
        }
      }
    }
  }

  static void appendFromClause(StringBuilder queryStringBuilder, String tableName)
      throws DataSourceException {
    if (BeeUtils.isEmpty(tableName)) {
      LogUtils.severe(logger, "No table name provided.");
      throw new DataSourceException(ReasonType.OTHER, "No table name provided.");
    }
    queryStringBuilder.append("FROM ");
    queryStringBuilder.append(tableName);
    queryStringBuilder.append(" ");
  }

  static void appendGroupByClause(Query query, StringBuilder queryStringBuilder) {
    if (!query.hasGroup()) {
      return;
    }
    queryStringBuilder.append("GROUP BY ");
    QueryGroup queryGroup = query.getGroup();
    List<String> groupColumnIds = queryGroup.getColumnIds();
    List<String> newColumnIds = Lists.newArrayList();
    for (String groupColumnId : groupColumnIds) {
      newColumnIds.add('`' + groupColumnId + '`');
    }
    BeeUtils.append(queryStringBuilder, newColumnIds, ", ");
    queryStringBuilder.append(" ");
  }

  static void appendLimitAndOffsetClause(Query query, StringBuilder queryStringBuilder) {
    if (query.hasRowLimit()) {
      queryStringBuilder.append("LIMIT ");
      queryStringBuilder.append(query.getRowLimit());
    }
    if (query.hasRowOffset()) {
      queryStringBuilder.append(" OFFSET ").append(query.getRowOffset());
    }
  }

  static void appendOrderByClause(Query query, StringBuilder queryStringBuilder) {
    if (!query.hasSort()) {
      return;
    }
    queryStringBuilder.append("ORDER BY ");
    QuerySort querySort = query.getSort();
    List<ColumnSort> sortColumns = querySort.getSortColumns();
    int numOfSortColumns = sortColumns.size();
    for (int col = 0; col < numOfSortColumns; col++) {
      ColumnSort columnSort = sortColumns.get(col);
      queryStringBuilder.append(getColumnId(columnSort.getColumn()));
      if (columnSort.getOrder() == SortOrder.DESCENDING) {
        queryStringBuilder.append(" DESC");
      }
      if (col < numOfSortColumns - 1) {
        queryStringBuilder.append(", ");
      }
    }
    queryStringBuilder.append(" ");
  }

  static void appendSelectClause(Query query, StringBuilder queryStringBuilder) {
    queryStringBuilder.append("SELECT ");

    if (!query.hasSelection()) {
      queryStringBuilder.append("* ");
      return;
    }

    List<AbstractColumn> columns = query.getSelection().getColumns();
    int numOfColsInQuery = columns.size();

    for (int col = 0; col < numOfColsInQuery; col++) {
      queryStringBuilder.append(getColumnId(columns.get(col)));
      if (col < numOfColsInQuery - 1) {
        queryStringBuilder.append(", ");
      }
    }
    queryStringBuilder.append(" ");
  }

  static void appendWhereClause(Query query, StringBuilder queryStringBuilder) {
    if (query.hasFilter()) {
      QueryFilter queryFilter = query.getFilter();
      queryStringBuilder.append("WHERE ")
          .append(buildWhereClauseRecursively(queryFilter)).append(" ");
    }
  }

  static DataTable buildColumns(ResultSet rs, List<String> columnIdsList) throws SQLException {
    DataTable result = new DataTable();
    ResultSetMetaData metaData = rs.getMetaData();
    int numOfCols = metaData.getColumnCount();

    for (int i = 1; i <= numOfCols; i++) {
      String id = (columnIdsList == null) ? metaData.getColumnLabel(i) : columnIdsList.get(i - 1);
      ColumnDescription columnDescription = new ColumnDescription(id,
          sqlTypeToValueType(metaData.getColumnType(i)), metaData.getColumnLabel(i));
      result.addColumn(columnDescription);
    }
    return result;
  }

  static void buildRows(DataTable dataTable, ResultSet rs) throws SQLException {
    List<ColumnDescription> columnsDescriptionList = dataTable.getColumnDescriptions();
    int numOfCols = dataTable.getNumberOfColumns();

    ValueType[] columnsTypeArray = new ValueType[numOfCols];
    for (int c = 0; c < numOfCols; c++) {
      columnsTypeArray[c] = columnsDescriptionList.get(c).getType();
    }

    while (rs.next()) {
      TableRow tableRow = new TableRow();
      for (int c = 0; c < numOfCols; c++) {
        tableRow.addCell(buildTableCell(rs, columnsTypeArray[c], c));
      }
      try {
        dataTable.addRow(tableRow);
      } catch (TypeMismatchException e) {
        Assert.untouchable();
      }
    }
  }

  private static void buildSqlQuery(Query query, StringBuilder queryStringBuilder, String tableName)
      throws DataSourceException {
    appendSelectClause(query, queryStringBuilder);
    appendFromClause(queryStringBuilder, tableName);
    appendWhereClause(query, queryStringBuilder);
    appendGroupByClause(query, queryStringBuilder);
    appendOrderByClause(query, queryStringBuilder);
    appendLimitAndOffsetClause(query, queryStringBuilder);
  }

  @SuppressWarnings("deprecation")
  private static TableCell buildTableCell(ResultSet rs, ValueType valueType,
      int column) throws SQLException {
    Value value = null;
    column = column + 1;

    switch (valueType) {
      case BOOLEAN:
        value = BooleanValue.getInstance(rs.getBoolean(column));
        break;
      case NUMBER:
        value = new NumberValue(rs.getDouble(column));
        break;
      case DATE:
        Date date = rs.getDate(column);
        if (date != null) {
          GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
          gc.set(date.getYear() + 1900, date.getMonth(), date.getDate());
          value = new DateValue(gc);
        }
        break;
      case DATETIME:
        Timestamp timestamp = rs.getTimestamp(column);
        if (timestamp != null) {
          GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
          gc.set(timestamp.getYear() + 1900, timestamp.getMonth(),
                 timestamp.getDate(), timestamp.getHours(), timestamp.getMinutes(),
                 timestamp.getSeconds());
          gc.set(Calendar.MILLISECOND, timestamp.getNanos() / 1000000);
          value = new DateTimeValue(gc);
        }
        break;
      case TIMEOFDAY:
        Time time = rs.getTime(column);
        if (time != null) {
          GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
          gc.set(1970, Calendar.JANUARY, 1, time.getHours(), time.getMinutes(),
                 time.getSeconds());
          gc.set(GregorianCalendar.MILLISECOND, 0);
          value = new TimeOfDayValue(gc);
        }
        break;
      default:
        String colValue = rs.getString(column);
        if (colValue == null) {
          value = TextValue.getNullValue();
        } else {
          value = new TextValue(rs.getString(column));
        }
        break;
    }

    if (rs.wasNull()) {
      return new TableCell(Value.getNullValueFromValueType(valueType));
    } else {
      return new TableCell(value);
    }
  }

  private static void buildWhereClauseForIsNullFilter(StringBuilder whereClause,
      QueryFilter queryFilter) {
    ColumnIsNullFilter filter = (ColumnIsNullFilter) queryFilter;

    whereClause.append("(").append(getColumnId(filter.getColumn())).append(" IS NULL)");
  }

  private static StringBuilder buildWhereClauseFromRightAndLeftParts(
      StringBuilder value1, StringBuilder value2, ComparisonFilter.Operator operator) {
    StringBuilder clause;
    switch (operator) {
      case EQ:
        clause = value1.append("=").append(value2);
        break;
      case NE:
        clause = value1.append("<>").append(value2);
        break;
      case LT:
        clause = value1.append("<").append(value2);
        break;
      case GT:
        clause = value1.append(">").append(value2);
        break;
      case LE:
        clause = value1.append("<=").append(value2);
        break;
      case GE:
        clause = value1.append(">=").append(value2);
        break;
      case CONTAINS:
        value2 = new StringBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"%").append(value2).append("%\"");
        break;
      case STARTS_WITH:
        value2 = new StringBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"").append(value2).append("%\"");
        break;
      case ENDS_WITH:
        value2 = new StringBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"%").append(value2).append("\"");
        break;
      case MATCHES:
        throw new RuntimeException("SQL does not support regular expression");
      case LIKE:
        value2 = new StringBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"").append(value2).append("\"");
        break;
      default:
        Assert.untouchable("Operator was not found: " + operator);
        return null;
    }
    clause.insert(0, "(").append(")");
    return clause;
  }

  private static StringBuilder buildWhereClauseRecursively(QueryFilter queryFilter) {
    StringBuilder whereClause = new StringBuilder();

    if (queryFilter instanceof ColumnIsNullFilter) {
      buildWhereClauseForIsNullFilter(whereClause, queryFilter);
    } else if (queryFilter instanceof ComparisonFilter) {
      buildWhereCluaseForComparisonFilter(whereClause, queryFilter);
    } else if (queryFilter instanceof NegationFilter) {
      whereClause.append("(NOT ");
      whereClause.append(buildWhereClauseRecursively(
          ((NegationFilter) queryFilter).getSubFilter()));
      whereClause.append(")");
    } else {
      CompoundFilter compoundFilter = (CompoundFilter) queryFilter;
      int numberOfSubFilters = compoundFilter.getSubFilters().size();

      if (numberOfSubFilters == 0) {
        if (compoundFilter.getOperator() == CompoundFilter.LogicalOperator.AND) {
          whereClause.append("true");
        } else {
          whereClause.append("false");
        }
      } else {
        List<String> filterComponents = Lists.newArrayList();
        for (QueryFilter filter : compoundFilter.getSubFilters()) {
          filterComponents.add(buildWhereClauseRecursively(filter).toString());
        }
        String logicalOperator = getSqlLogicalOperator(compoundFilter.getOperator());
        BeeUtils.append(whereClause.append("("), filterComponents, " " + logicalOperator + " ")
            .append(")");
      }
    }
    return whereClause;
  }

  private static void buildWhereCluaseForComparisonFilter(
      StringBuilder whereClause, QueryFilter queryFilter) {
    StringBuilder first = new StringBuilder();
    StringBuilder second = new StringBuilder();

    if (queryFilter instanceof ColumnColumnFilter) {
      ColumnColumnFilter filter = (ColumnColumnFilter) queryFilter;
      first.append(getColumnId(filter.getFirstColumn()));
      second.append(getColumnId(filter.getSecondColumn()));
    } else {
      ColumnValueFilter filter = (ColumnValueFilter) queryFilter;
      first.append(getColumnId(filter.getColumn()));
      second.append(filter.getValue().toString());
      if ((filter.getValue().getType() == ValueType.TEXT)
          || (filter.getValue().getType() == ValueType.DATE)
          || (filter.getValue().getType() == ValueType.DATETIME)
          || (filter.getValue().getType() == ValueType.TIMEOFDAY)) {
        second.insert(0, "\"");
        second.insert(second.length(), "\"");
      }
    }
    whereClause.append(buildWhereClauseFromRightAndLeftParts(
        first, second, ((ComparisonFilter) queryFilter).getOperator()));
  }

  private static String getAggregationFunction(AggregationType type) {
    return type.getCode();
  }

  private static StringBuilder getColumnId(AbstractColumn abstractColumn) {
    StringBuilder columnId = new StringBuilder();

    if (abstractColumn instanceof SimpleColumn) {
      columnId.append("`").append(abstractColumn.getId()).append("`");
    } else {
      AggregationColumn aggregationColumn = (AggregationColumn) abstractColumn;
      columnId.append(getAggregationFunction(aggregationColumn.getAggregationType())).append("(`").append(
          aggregationColumn.getAggregatedColumn()).append("`)");
    }
    return columnId;
  }

  private static List<String> getColumnIdsList(QuerySelection selection) {
    List<String> columnIds = Lists.newArrayListWithCapacity(selection.getColumns().size());
    for (AbstractColumn column : selection.getColumns()) {
      columnIds.add(column.getId());
    }
    return columnIds;
  }

  private static Connection getDatabaseConnection(
      SqlDatabaseDescription databaseDescription) throws DataSourceException {
    Connection con;
    String userName = databaseDescription.getUser();
    String password = databaseDescription.getPassword();
    String url = databaseDescription.getUrl();
    try {
      con = DriverManager.getConnection(url, userName, password);
    } catch (SQLException e) {
      LogUtils.severe(logger, e, "Failed to connect to database server.");
      throw new DataSourceException(ReasonType.INTERNAL_ERROR,
          "Failed to connect to database server.");
    }
    return con;
  }

  private static String getSqlLogicalOperator(CompoundFilter.LogicalOperator operator) {
    String stringOperator;
    switch (operator) {
      case AND:
        stringOperator = "AND";
        break;
      case OR:
        stringOperator = "OR";
        break;
      default:
        Assert.untouchable("Logical operator was not found: " + operator);
        stringOperator = null;
    }
    return stringOperator;
  }

  private static ValueType sqlTypeToValueType(int sqlType) {
    ValueType valueType;
    switch (sqlType) {
      case Types.BOOLEAN:
      case Types.BIT: {
        valueType = ValueType.BOOLEAN;
        break;
      }
      case Types.CHAR:
      case Types.VARCHAR:
        valueType = ValueType.TEXT;
        break;
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.BIGINT:
      case Types.TINYINT:
      case Types.REAL:
      case Types.NUMERIC:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.DECIMAL:
        valueType = ValueType.NUMBER;
        break;
      case Types.DATE:
        valueType = ValueType.DATE;
        break;
      case Types.TIME:
        valueType = ValueType.TIMEOFDAY;
        break;
      case Types.TIMESTAMP:
        valueType = ValueType.DATETIME;
        break;
      default:
        valueType = ValueType.TEXT;
        break;
    }
    return valueType;
  }

  private SqlDataSourceHelper() {
  }
}
