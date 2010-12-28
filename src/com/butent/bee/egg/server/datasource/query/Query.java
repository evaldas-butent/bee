package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.base.MessagesEnum;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import com.ibm.icu.util.ULocale;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Query {

  private static final Logger logger = Logger.getLogger(Query.class.getName());

  static String columnListToQueryString(List<AbstractColumn> l) {
    StringBuilder builder = new StringBuilder();
    List<String> stringList = Lists.newArrayList();
    for (AbstractColumn col : l) {
      stringList.add(col.toQueryString());
    }
    BeeUtils.append(builder, stringList, ", ");
    return builder.toString();
  }

  static String stringToQueryStringLiteral(String s) {
    if (s.contains("\"")) {
      if (s.contains("'")) {
        throw new RuntimeException("Cannot represent string that contains both double-quotes (\") "
            + " and single quotes (').");
      } else {
        return "'" + s + "'";
      }
    } else {
      return "\"" + s + "\"";
    }
  }

  private static <T> void checkForDuplicates(List<T>
      selectionColumns, String clauseName, ULocale userLocale) throws InvalidQueryException {
    for (int i = 0; i < selectionColumns.size(); i++) {
      T col = selectionColumns.get(i);
      for (int j = i + 1; j < selectionColumns.size(); j++) {
        if (col.equals(selectionColumns.get(j))) {
          String[] args = {col.toString(), clauseName};
          String messageToLogAndUser = MessagesEnum.COLUMN_ONLY_ONCE.getMessageWithArgs(userLocale,
              args);
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
  }

  private QuerySort sort = null;

  protected QuerySelection selection = null;

  private QueryFilter filter = null;

  private QueryGroup group = null;

  private QueryPivot pivot = null;

  private int rowSkipping = 0;

  private int rowLimit = -1;

  private int rowOffset = 0;

  private QueryOptions options = null;

  private QueryLabels labels = null;

  private QueryFormat userFormatOptions = null;

  private ULocale localeForUserMessages = null;

  public Query() {
  }

  public void copyFrom(Query query) {
    setSort(query.getSort());
    setSelection(query.getSelection());
    setFilter(query.getFilter());
    setGroup(query.getGroup());
    setPivot(query.getPivot());
    copyRowSkipping(query);
    copyRowLimit(query);
    copyRowOffset(query);
    setUserFormatOptions(query.getUserFormatOptions());
    setLabels(query.getLabels());
    setOptions(query.getOptions());
  }

  public void copyRowLimit(Query originalQuery) {
    rowLimit = originalQuery.getRowLimit();
  }

  public void copyRowOffset(Query originalQuery) {
    rowOffset = originalQuery.getRowOffset();
  }

  public void copyRowSkipping(Query originalQuery) {
    rowSkipping = originalQuery.getRowSkipping();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Query other = (Query) obj;
    if (filter == null) {
      if (other.filter != null) {
        return false;
      }
    } else if (!filter.equals(other.filter)) {
      return false;
    }
    if (group == null) {
      if (other.group != null) {
        return false;
      }
    } else if (!group.equals(other.group)) {
      return false;
    }
    if (labels == null) {
      if (other.labels != null) {
        return false;
      }
    } else if (!labels.equals(other.labels)) {
      return false;
    }
    if (options == null) {
      if (other.options != null) {
        return false;
      }
    } else if (!options.equals(other.options)) {
      return false;
    }
    if (pivot == null) {
      if (other.pivot != null) {
        return false;
      }
    } else if (!pivot.equals(other.pivot)) {
      return false;
    }
    if (rowSkipping != other.rowSkipping) {
      return false;
    }
    if (rowLimit != other.rowLimit) {
      return false;
    }
    if (rowOffset != other.rowOffset) {
      return false;
    }
    if (selection == null) {
      if (other.selection != null) {
        return false;
      }
    } else if (!selection.equals(other.selection)) {
      return false;
    }
    if (sort == null) {
      if (other.sort != null) {
        return false;
      }
    } else if (!sort.equals(other.sort)) {
      return false;
    }
    if (userFormatOptions == null) {
      if (other.userFormatOptions != null) {
        return false;
      }
    } else if (!userFormatOptions.equals(other.userFormatOptions)) {
      return false;
    }
    return true;
  }

  public Set<AggregationColumn> getAllAggregations() {
    Set<AggregationColumn> result = Sets.newHashSet();
    if (hasSelection()) {
      result.addAll(selection.getAggregationColumns());
    }
    if (hasSort()) {
      for (AbstractColumn col : sort.getColumns()) {
        if (col instanceof AggregationColumn) {
          result.add((AggregationColumn) col);
        }
      }
    }

    if (hasLabels()) {
      for (AbstractColumn col : labels.getColumns()) {
        if (col instanceof AggregationColumn) {
          result.add((AggregationColumn) col);
        }
      }
    }
    if (hasUserFormatOptions()) {
      for (AbstractColumn col : userFormatOptions.getColumns()) {
        if (col instanceof AggregationColumn) {
          result.add((AggregationColumn) col);
        }
      }
    }
    return result;
  }

  public Set<String> getAllColumnIds() {
    Set<String> result = Sets.newHashSet();
    if (hasSelection()) {
      for (AbstractColumn col : selection.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }
    if (hasSort()) {
      for (AbstractColumn col : sort.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }
    if (hasGroup()) {
      result.addAll(getGroup().getSimpleColumnIds());
    }
    if (hasPivot()) {
      result.addAll(getPivot().getSimpleColumnIds());
    }
    if (hasFilter()) {
      result.addAll(getFilter().getAllColumnIds());
    }
    if (hasLabels()) {
      for (AbstractColumn col : labels.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }
    if (hasUserFormatOptions()) {
      for (AbstractColumn col : userFormatOptions.getColumns()) {
        result.addAll(col.getAllSimpleColumnIds());
      }
    }

    return result;
  }

  public Set<ScalarFunctionColumn> getAllScalarFunctionsColumns() {
    Set<ScalarFunctionColumn> mentionedScalarFunctionColumns = Sets.newHashSet();
    if (hasSelection()) {
      mentionedScalarFunctionColumns.addAll(selection.getScalarFunctionColumns());
    }
    if (hasFilter()) {
      mentionedScalarFunctionColumns.addAll(filter.getScalarFunctionColumns());
    }
    if (hasGroup()) {
      mentionedScalarFunctionColumns.addAll(group.getScalarFunctionColumns());
    }
    if (hasPivot()) {
      mentionedScalarFunctionColumns.addAll(pivot.getScalarFunctionColumns());
    }
    if (hasSort()) {
      mentionedScalarFunctionColumns.addAll(sort.getScalarFunctionColumns());
    }
    if (hasLabels()) {
      mentionedScalarFunctionColumns.addAll(labels.getScalarFunctionColumns());
    }
    if (hasUserFormatOptions()) {
      mentionedScalarFunctionColumns.addAll(userFormatOptions.getScalarFunctionColumns());
    }
    return mentionedScalarFunctionColumns;
  }

  public QueryFilter getFilter() {
    return filter;
  }

  public QueryGroup getGroup() {
    return group;
  }

  public QueryLabels getLabels() {
    return labels;
  }

  public QueryOptions getOptions() {
    return options;
  }

  public QueryPivot getPivot() {
    return pivot;
  }

  public int getRowLimit() {
    return rowLimit;
  }

  public int getRowOffset() {
    return rowOffset;
  }

  public int getRowSkipping() {
    return rowSkipping;
  }

  public QuerySelection getSelection() {
    return selection;
  }

  public QuerySort getSort() {
    return sort;
  }

  public QueryFormat getUserFormatOptions() {
    return userFormatOptions;
  }

  public boolean hasFilter() {
    return (filter != null);
  }

  public boolean hasGroup() {
    return group != null && !group.getColumnIds().isEmpty();
  }

  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    result = prime * result + ((filter == null) ? 0 : filter.hashCode());
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + ((labels == null) ? 0 : labels.hashCode());
    result = prime * result + ((options == null) ? 0 : options.hashCode());
    result = prime * result + ((pivot == null) ? 0 : pivot.hashCode());
    result = prime * result + rowSkipping;
    result = prime * result + rowLimit;
    result = prime * result + rowOffset;
    result = prime * result + ((selection == null) ? 0 : selection.hashCode());
    result = prime * result + ((sort == null) ? 0 : sort.hashCode());
    result = prime * result + ((userFormatOptions == null) ? 0 : userFormatOptions.hashCode());
    return result;
  }

  public boolean hasLabels() {
    return (labels != null) && (!labels.getColumns().isEmpty());
  }

  public boolean hasOptions() {
    return (options != null) && (!options.isDefault());
  }

  public boolean hasPivot() {
    return pivot != null && !pivot.getColumnIds().isEmpty();
  }

  public boolean hasRowLimit() {
    return rowLimit > -1;
  }

  public boolean hasRowOffset() {
    return rowOffset > 0;
  }

  public boolean hasRowSkipping() {
    return rowSkipping > 0;
  }

  public boolean hasSelection() {
    return (selection != null) && (!selection.isEmpty());
  }

  public boolean hasSort() {
    return (sort != null) && (!sort.isEmpty());
  }

  public boolean hasUserFormatOptions() {
    return (userFormatOptions != null) && (!userFormatOptions.getColumns().isEmpty());
  }

  public boolean isEmpty() {
    return (!hasSort() && !hasSelection() && !hasFilter() && !hasGroup() && !hasPivot()
            && !hasRowSkipping() && !hasRowLimit() && !hasRowOffset()
            && !hasUserFormatOptions() && !hasLabels() && !hasOptions());
  }

  public void setFilter(QueryFilter filter) {
    this.filter = filter;
  }

  public void setGroup(QueryGroup group) {
    this.group = group;
  }

  public void setLabels(QueryLabels labels) {
    this.labels = labels;
  }

  public void setLocaleForUserMessages(ULocale localeForUserMessges) {
    this.localeForUserMessages = localeForUserMessges;
  }

  public void setOptions(QueryOptions options) {
    this.options = options;
  }

  public void setPivot(QueryPivot pivot) {
    this.pivot = pivot;
  }

  public void setRowLimit(int rowLimit) throws InvalidQueryException {
    if (rowLimit < -1) {
      String messageToLogAndUser = "Invalid value for row limit: " + rowLimit;
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    this.rowLimit = rowLimit;
  }

  public void setRowOffset(int rowOffset) throws InvalidQueryException {
    if (rowOffset < 0) {
      String messageToLogAndUser = MessagesEnum.INVALID_OFFSET.getMessageWithArgs(
          localeForUserMessages, Integer.toString(rowOffset));
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    this.rowOffset = rowOffset;
  }

  public void setRowSkipping(int rowSkipping) throws InvalidQueryException {
    if (rowSkipping < 0) {
      String messageToLogAndUser = MessagesEnum.INVALID_SKIPPING.getMessageWithArgs(
          localeForUserMessages, Integer.toString(rowSkipping));
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    this.rowSkipping = rowSkipping;
  }

  public void setSelection(QuerySelection selection) {
    this.selection = selection;
  }

  public void setSort(QuerySort sort) {
    this.sort = sort;
  }

  public void setUserFormatOptions(QueryFormat userFormatOptions) {
    this.userFormatOptions = userFormatOptions;
  }

  public String toQueryString() {
    List<String> clauses = Lists.newArrayList();
    if (hasSelection()) {
      clauses.add("SELECT " + selection.toQueryString());
    }
    if (hasFilter()) {
      clauses.add("WHERE " + filter.toQueryString());
    }
    if (hasGroup()) {
      clauses.add("GROUP BY " + group.toQueryString());
    }
    if (hasPivot()) {
      clauses.add("PIVOT " + pivot.toQueryString());
    }
    if (hasSort()) {
      clauses.add("ORDER BY " + sort.toQueryString());
    }
    if (hasRowSkipping()) {
      clauses.add("SKIPPING " + rowSkipping);
    }
    if (hasRowLimit()) {
      clauses.add("LIMIT " + rowLimit);
    }
    if (hasRowOffset()) {
      clauses.add("OFFSET " + rowOffset);
    }
    if (hasLabels()) {
      clauses.add("LABEL " + labels.toQueryString());
    }
    if (hasUserFormatOptions()) {
      clauses.add("FORMAT " + userFormatOptions.toQueryString());
    }
    if (hasOptions()) {
      clauses.add("OPTIONS " + options.toQueryString());
    }
    StringBuilder result = new StringBuilder();
    BeeUtils.append(result, clauses, " ");
    return result.toString();
  }

  public void validate() throws InvalidQueryException {
    List<String> groupColumnIds =
        hasGroup() ? group.getColumnIds() : Lists.<String> newArrayList();
    List<AbstractColumn> groupColumns = hasGroup() ? group.getColumns() :
        Lists.<AbstractColumn> newArrayList();
    List<String> pivotColumnIds =
        hasPivot() ? pivot.getColumnIds() : Lists.<String> newArrayList();
    List<AbstractColumn> selectionColumns = hasSelection()
        ? selection.getColumns() : Lists.<AbstractColumn> newArrayList();
    List<AggregationColumn> selectionAggregated =
        hasSelection() ? selection.getAggregationColumns() :
            Lists.<AggregationColumn> newArrayList();
    List<SimpleColumn> selectionSimple = hasSelection()
        ? selection.getSimpleColumns() : Lists.<SimpleColumn> newArrayList();
    List<ScalarFunctionColumn> selectedScalarFunctionColumns = hasSelection()
        ? selection.getScalarFunctionColumns()
        : Lists.<ScalarFunctionColumn> newArrayList();
    selectedScalarFunctionColumns.addAll(selectedScalarFunctionColumns);
    List<AbstractColumn> sortColumns = hasSort() ? sort.getColumns() :
        Lists.<AbstractColumn> newArrayList();
    List<AggregationColumn> sortAggregated = hasSort()
        ? sort.getAggregationColumns()
        : Lists.<AggregationColumn> newArrayList();

    checkForDuplicates(selectionColumns, "SELECT", localeForUserMessages);
    checkForDuplicates(sortColumns, "ORDER BY", localeForUserMessages);
    checkForDuplicates(groupColumnIds, "GROUP BY", localeForUserMessages);
    checkForDuplicates(pivotColumnIds, "PIVOT", localeForUserMessages);

    if (hasGroup()) {
      for (AbstractColumn column : group.getColumns()) {
        if (!column.getAllAggregationColumns().isEmpty()) {
          String messageToLogAndUser = MessagesEnum.CANNOT_BE_IN_GROUP_BY.getMessageWithArgs(
              localeForUserMessages, column.toQueryString());
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
    if (hasPivot()) {
      for (AbstractColumn column : pivot.getColumns()) {
        if (!column.getAllAggregationColumns().isEmpty()) {
          String messageToLogAndUser = MessagesEnum.CANNOT_BE_IN_PIVOT.getMessageWithArgs(
              localeForUserMessages, column.toQueryString());
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
    if (hasFilter()) {
      List<AggregationColumn> filterAggregations = filter.getAggregationColumns();
      if (!filterAggregations.isEmpty()) {
        String messageToLogAndUser = MessagesEnum.CANNOT_BE_IN_WHERE.getMessageWithArgs(
            localeForUserMessages, filterAggregations.get(0).toQueryString());
        LogUtils.severe(logger, messageToLogAndUser);
        throw new InvalidQueryException(messageToLogAndUser);
      }
    }

    for (SimpleColumn column1 : selectionSimple) {
      String id = column1.getColumnId();
      for (AggregationColumn column2 : selectionAggregated) {
        if (id.equals(column2.getAggregatedColumn().getId())) {
          String messageToLogAndUser = MessagesEnum.SELECT_WITH_AND_WITHOUT_AGG.getMessageWithArgs(
              localeForUserMessages, id);
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    if (!selectionAggregated.isEmpty()) {
      for (AbstractColumn col : selectionColumns) {
        checkSelectedColumnWithGrouping(groupColumns, col);
      }
    }

    if (hasSelection() && hasGroup()) {
      for (AggregationColumn column : selectionAggregated) {
        String id = column.getAggregatedColumn().getId();
        if (groupColumnIds.contains(id)) {
          String messageToLogAndUser = MessagesEnum.COL_AGG_NOT_IN_SELECT.getMessageWithArgs(
              localeForUserMessages, id);
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    if (hasGroup() && selectionAggregated.isEmpty()) {
      String messageToLogAndUser = MessagesEnum.CANNOT_GROUP_WITHOUT_AGG.getMessage(
          localeForUserMessages);
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    if (hasPivot() && selectionAggregated.isEmpty()) {
      String messageToLogAndUser = MessagesEnum.CANNOT_PIVOT_WITHOUT_AGG.getMessage(
          localeForUserMessages);
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }

    if (hasSort() && !selectionAggregated.isEmpty()) {
      for (AbstractColumn column : sort.getColumns()) {
        String messageToLogAndUser = MessagesEnum.COL_IN_ORDER_MUST_BE_IN_SELECT.getMessageWithArgs(
            localeForUserMessages, column.toQueryString());
        checkColumnInList(selection.getColumns(), column, messageToLogAndUser);
      }
    }

    if (hasPivot()) {
      for (AggregationColumn column : selectionAggregated) {
        String id = column.getAggregatedColumn().getId();
        if (pivotColumnIds.contains(id)) {
          String messageToLogAndUser = MessagesEnum.AGG_IN_SELECT_NO_PIVOT.getMessageWithArgs(
              localeForUserMessages, id);
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    if (hasGroup() && hasPivot()) {
      for (String id : groupColumnIds) {
        if (pivotColumnIds.contains(id)) {
          String messageToLogAndUser = MessagesEnum.NO_COL_IN_GROUP_AND_PIVOT.getMessageWithArgs(
              localeForUserMessages, id);
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }

    if (hasPivot() && !sortAggregated.isEmpty()) {
      AggregationColumn column = sortAggregated.get(0);
      String messageToLogAndUser = MessagesEnum.NO_AGG_IN_ORDER_WHEN_PIVOT.getMessageWithArgs(
          localeForUserMessages, column.getAggregatedColumn().getId());
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }

    for (AggregationColumn column : sortAggregated) {
      String messageToLogAndUser = MessagesEnum.AGG_IN_ORDER_NOT_IN_SELECT.getMessageWithArgs(
          localeForUserMessages, column.toQueryString());
      checkColumnInList(selectionAggregated, column, messageToLogAndUser);
    }

    Set<AbstractColumn> labelColumns = (hasLabels()
        ? labels.getColumns() : Sets.<AbstractColumn> newHashSet());
    Set<AbstractColumn> formatColumns = (hasUserFormatOptions()
        ? userFormatOptions.getColumns() : Sets.<AbstractColumn> newHashSet());

    if (hasSelection()) {
      for (AbstractColumn col : labelColumns) {
        if (!selectionColumns.contains(col)) {
          String messageToLogAndUser = MessagesEnum.LABEL_COL_NOT_IN_SELECT.getMessageWithArgs(
              localeForUserMessages, col.toQueryString());
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
      for (AbstractColumn col : formatColumns) {
        if (!selectionColumns.contains(col)) {
          String messageToLogAndUser = MessagesEnum.FORMAT_COL_NOT_IN_SELECT.getMessageWithArgs(
              localeForUserMessages, col.toQueryString());
          LogUtils.severe(logger, messageToLogAndUser);
          throw new InvalidQueryException(messageToLogAndUser);
        }
      }
    }
  }

  private void checkColumnInList(List<? extends AbstractColumn> columns,
      AbstractColumn column, String messageToLogAndUser) throws InvalidQueryException {
    if (columns.contains(column)) {
      return;
    } else if (column instanceof ScalarFunctionColumn) {
      List<AbstractColumn> innerColumns = ((ScalarFunctionColumn) column).getColumns();
      for (AbstractColumn innerColumn : innerColumns) {
        checkColumnInList(columns, innerColumn, messageToLogAndUser);
      }
    } else {
      LogUtils.severe(logger, messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
  }

  private void checkSelectedColumnWithGrouping(List<AbstractColumn> groupColumns,
      AbstractColumn col) throws InvalidQueryException {
    if (col instanceof SimpleColumn) {
      if (!groupColumns.contains(col)) {
        String messageToLogAndUser = MessagesEnum.ADD_COL_TO_GROUP_BY_OR_AGG.getMessageWithArgs(
            localeForUserMessages, col.getId());
        LogUtils.severe(logger, messageToLogAndUser);
        throw new InvalidQueryException(messageToLogAndUser);
      }
    } else if (col instanceof ScalarFunctionColumn) {
      if (!groupColumns.contains(col)) {
        List<AbstractColumn> innerColumns = ((ScalarFunctionColumn) col).getColumns();
        for (AbstractColumn innerColumn : innerColumns) {
          checkSelectedColumnWithGrouping(groupColumns, innerColumn);
        }
      }
    }
  }
}
