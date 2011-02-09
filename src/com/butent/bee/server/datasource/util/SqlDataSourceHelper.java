package com.butent.bee.server.datasource.util;

import com.google.common.collect.Lists;

import com.butent.bee.server.datasource.query.Query;
import com.butent.bee.server.datasource.query.QueryGroup;
import com.butent.bee.server.datasource.query.QuerySelection;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.Aggregation;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataException;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.Reasons;
import com.butent.bee.shared.data.TableCell;
import com.butent.bee.shared.data.column.AbstractColumn;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.SimpleColumn;
import com.butent.bee.shared.data.filter.ColumnColumnFilter;
import com.butent.bee.shared.data.filter.ColumnIsNullFilter;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.NegationFilter;
import com.butent.bee.shared.data.filter.RowFilter;
import com.butent.bee.shared.data.sort.SortQuery;
import com.butent.bee.shared.data.sort.SortColumn;
import com.butent.bee.shared.data.sort.SortOrder;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.TimeOfDayValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

public class SqlDataSourceHelper {
  private static final Logger logger = Logger.getLogger(SqlDataSourceHelper.class.getName());

  public static IsTable<?, ?> executeQuery(Query query, SqlDatabaseDescription databaseDescription)
      throws DataException {
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

      IsTable<?, ?> table = buildColumns(rs, columnIdsList);

      buildRows(table, rs);
      return table;
    } catch (SQLException e) {
      String messageToUser = "Failed to execute SQL query. mySQL error message:"
          + " " + e.getMessage();
      throw new DataException(Reasons.INTERNAL_ERROR, messageToUser);
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
      throws DataException {
    if (BeeUtils.isEmpty(tableName)) {
      LogUtils.severe(logger, "No table name provided.");
      throw new DataException(Reasons.OTHER, "No table name provided.");
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
    SortQuery querySort = query.getSort();
    List<SortColumn> sortColumns = querySort.getSortColumns();
    int numOfSortColumns = sortColumns.size();
    for (int col = 0; col < numOfSortColumns; col++) {
      SortColumn columnSort = sortColumns.get(col);
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
      RowFilter queryFilter = query.getFilter();
      queryStringBuilder.append("WHERE ")
          .append(buildWhereClauseRecursively(queryFilter)).append(" ");
    }
  }

  static BeeRowSet buildColumns(ResultSet rs, List<String> columnIdsList) throws SQLException {
    BeeRowSet result = new BeeRowSet();
    ResultSetMetaData metaData = rs.getMetaData();
    int numOfCols = metaData.getColumnCount();

    for (int i = 1; i <= numOfCols; i++) {
      BeeColumn column = new BeeColumn();
      JdbcUtils.setColumnInfo(metaData, i, column);
      if (columnIdsList != null) {
        String id = columnIdsList.get(i - 1);
        if (!BeeUtils.isEmpty(id)) {
          column.setId(id);
        }
      }
      result.addColumn(column);
    }
    return result;
  }

  static <R extends IsRow, C extends IsColumn> void buildRows(IsTable<R, C> dataTable, ResultSet rs)
      throws SQLException {
    List<C> columns = dataTable.getColumns();
    int numOfCols = dataTable.getNumberOfColumns();

    ValueType[] columnsTypeArray = new ValueType[numOfCols];
    for (int c = 0; c < numOfCols; c++) {
      columnsTypeArray[c] = columns.get(c).getType();
    }

    while (rs.next()) {
      R row = dataTable.createRow();
      for (int c = 0; c < numOfCols; c++) {
        row.addCell(buildTableCell(rs, columnsTypeArray[c], c));
      }
      dataTable.addRow(row);
    }
  }

  private static void buildSqlQuery(Query query, StringBuilder queryStringBuilder, String tableName)
      throws DataException {
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
          value = new DateValue(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
        }
        break;
      case DATETIME:
        Timestamp timestamp = rs.getTimestamp(column);
        if (timestamp != null) {
          value = new DateTimeValue(timestamp.getYear() + 1900, timestamp.getMonth() + 1,
              timestamp.getDate(), timestamp.getHours(), timestamp.getMinutes(),
              timestamp.getSeconds(), timestamp.getNanos() / 1000000);
        }
        break;
      case TIMEOFDAY:
        Time time = rs.getTime(column);
        if (time != null) {
          value = new TimeOfDayValue(time.getHours(), time.getMinutes(), time.getSeconds());
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
      RowFilter queryFilter) {
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

  private static StringBuilder buildWhereClauseRecursively(RowFilter queryFilter) {
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
        for (RowFilter filter : compoundFilter.getSubFilters()) {
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
      StringBuilder whereClause, RowFilter queryFilter) {
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

  private static String getAggregationFunction(Aggregation type) {
    return type.getCode();
  }

  private static StringBuilder getColumnId(AbstractColumn abstractColumn) {
    StringBuilder columnId = new StringBuilder();

    if (abstractColumn instanceof SimpleColumn) {
      columnId.append("`").append(abstractColumn.getId()).append("`");
    } else {
      AggregationColumn aggregationColumn = (AggregationColumn) abstractColumn;
      columnId.append(getAggregationFunction(aggregationColumn.getAggregationType())).append("(`")
        .append(aggregationColumn.getAggregatedColumn()).append("`)");
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
      SqlDatabaseDescription databaseDescription) throws DataException {
    Connection con;
    String userName = databaseDescription.getUser();
    String password = databaseDescription.getPassword();
    String url = databaseDescription.getUrl();
    try {
      con = DriverManager.getConnection(url, userName, password);
    } catch (SQLException e) {
      LogUtils.severe(logger, e, "Failed to connect to database server.");
      throw new DataException(Reasons.INTERNAL_ERROR,
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

  private SqlDataSourceHelper() {
  }
}
