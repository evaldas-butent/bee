package com.butent.bee.egg.server.datasource;

import com.google.common.collect.Lists;

import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.server.datasource.query.QueryFormat;
import com.butent.bee.egg.server.datasource.query.QueryGroup;
import com.butent.bee.egg.server.datasource.query.QueryLabels;
import com.butent.bee.egg.server.datasource.query.QuerySelection;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.Aggregation;
import com.butent.bee.egg.shared.data.DataException;
import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.Reasons;
import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.SimpleColumn;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.util.List;
import java.util.logging.Logger;

public final class QuerySplitter {
  private static final Logger logger = Logger.getLogger(QuerySplitter.class.getName());

  public static QueryPair splitQuery(Query query, Capabilities capabilities)
      throws DataException {
    switch (capabilities) {
      case ALL:
        return splitAll(query);
      case NONE:
        return splitNone(query);
      case SQL:
        return splitSQL(query);
      case SORT_AND_PAGINATION:
        return splitSortAndPagination(query);
      case SELECT:
        return splitSelect(query);
    }
    LogUtils.severe(logger, "Capabilities not supported.");
    throw new DataException(Reasons.NOT_SUPPORTED, "Capabilities not supported.");
  }

  private static QueryPair splitAll(Query query) {
    Query dataSourceQuery = new Query();
    dataSourceQuery.copyFrom(query);
    Query completionQuery = new Query();
    return new QueryPair(dataSourceQuery, completionQuery);
  }

  private static QueryPair splitNone(Query query) {
    Query completionQuery = new Query();
    completionQuery.copyFrom(query);
    return new QueryPair(null, completionQuery);
  }

  private static QueryPair splitSelect(Query query) {
    Query dataSourceQuery = new Query();
    Query completionQuery = new Query();
    if (query.getSelection() != null) {
      QuerySelection selection = new QuerySelection();
      for (String simpleColumnId : query.getAllColumnIds()) {
        selection.addColumn(new SimpleColumn(simpleColumnId));
      }
      dataSourceQuery.setSelection(selection);
    }

    completionQuery.copyFrom(query);
    return new QueryPair(dataSourceQuery, completionQuery);
  }

  private static QueryPair splitSortAndPagination(Query query) {
    if (!query.getAllScalarFunctionsColumns().isEmpty()) {
      Query completionQuery = new Query();
      completionQuery.copyFrom(query);
      return new QueryPair(new Query(), completionQuery);
    }

    Query dataSourceQuery = new Query();
    Query completionQuery = new Query();
    if (query.hasFilter() || query.hasGroup() || query.hasPivot()) {
      completionQuery.copyFrom(query);
    } else {
      dataSourceQuery.setSort(query.getSort());     
      if (query.hasRowSkipping()) {
        completionQuery.copyRowSkipping(query);
        completionQuery.copyRowLimit(query);
        completionQuery.copyRowOffset(query);
      } else {
        dataSourceQuery.copyRowLimit(query);
        dataSourceQuery.copyRowOffset(query);
      }

      completionQuery.setSelection(query.getSelection());
      completionQuery.setOptions(query.getOptions());
      completionQuery.setLabels(query.getLabels());
      completionQuery.setUserFormatOptions(query.getUserFormatOptions());
    }
    return new QueryPair(dataSourceQuery, completionQuery);
  }

  private static QueryPair splitSQL(Query query) {
    if (!query.getAllScalarFunctionsColumns().isEmpty() 
        || (query.hasPivot()
            && ((query.hasUserFormatOptions() &&
                !query.getUserFormatOptions().getAggregationColumns().isEmpty())
             || (query.hasLabels() && !query.getLabels().getAggregationColumns().isEmpty())))) {
      Query completionQuery = new Query();
      completionQuery.copyFrom(query);
      return new QueryPair(new Query(), completionQuery);
    }

    Query dataSourceQuery = new Query();
    Query completionQuery = new Query();

    if (query.hasPivot()) {
      List<AbstractColumn> pivotColumns = query.getPivot().getColumns();

      dataSourceQuery.copyFrom(query);
      dataSourceQuery.setPivot(null);
      dataSourceQuery.setSort(null);
      dataSourceQuery.setOptions(null);
      dataSourceQuery.setLabels(null);
      dataSourceQuery.setUserFormatOptions(null);

      try {
        dataSourceQuery.setRowSkipping(0);
        dataSourceQuery.setRowLimit(-1);
        dataSourceQuery.setRowOffset(0);
      } catch (InvalidQueryException e) {
        Assert.untouchable();
      }

      List<AbstractColumn> newGroupColumns = Lists.newArrayList();
      List<AbstractColumn> newSelectionColumns = Lists.newArrayList();
      if (dataSourceQuery.hasGroup()) {
        newGroupColumns.addAll(dataSourceQuery.getGroup().getColumns());
      }
      newGroupColumns.addAll(pivotColumns);
      if (dataSourceQuery.hasSelection()) {
        newSelectionColumns.addAll(dataSourceQuery.getSelection().getColumns());
      }
      newSelectionColumns.addAll(pivotColumns);
      QueryGroup group = new QueryGroup();
      for (AbstractColumn col : newGroupColumns) {
        group.addColumn(col);
      }
      dataSourceQuery.setGroup(group);
      QuerySelection selection = new QuerySelection();
      for (AbstractColumn col : newSelectionColumns) {
        selection.addColumn(col);
      }
      dataSourceQuery.setSelection(selection);

      completionQuery.copyFrom(query);
      completionQuery.setFilter(null);

      QuerySelection completionSelection = new QuerySelection();
      List<AbstractColumn> originalSelectedColumns = query.getSelection().getColumns();
      for (int i = 0; i < originalSelectedColumns.size(); i++) {
        AbstractColumn column = originalSelectedColumns.get(i);
        if (query.getGroup().getColumns().contains(column)) {
          completionSelection.addColumn(column);
        } else { 
          String id = column.getId();
          completionSelection.addColumn(
              new AggregationColumn(new SimpleColumn(id), Aggregation.MIN));
        }
      }

      completionQuery.setSelection(completionSelection);
    } else {
      dataSourceQuery.copyFrom(query);
      dataSourceQuery.setOptions(null);
      completionQuery.setOptions(query.getOptions());
      try {
        if (query.hasRowSkipping()) {
          dataSourceQuery.setRowSkipping(0);
          dataSourceQuery.setRowLimit(-1);
          dataSourceQuery.setRowOffset(0);
          
          completionQuery.copyRowSkipping(query);
          completionQuery.copyRowLimit(query);
          completionQuery.copyRowOffset(query); 
        }
        if (query.hasLabels()) {
          dataSourceQuery.setLabels(null);
          QueryLabels labels = query.getLabels();
          QueryLabels newLabels = new QueryLabels();
          for (AbstractColumn column : labels.getColumns()) {
            newLabels.addLabel(new SimpleColumn(column.getId()), labels.getLabel(column));
          }
          completionQuery.setLabels(newLabels);
        }
        if (query.hasUserFormatOptions()) {
          dataSourceQuery.setUserFormatOptions(null);
          QueryFormat formats = query.getUserFormatOptions();
          QueryFormat newFormats = new QueryFormat();
          for (AbstractColumn column : formats.getColumns()) {
            newFormats.addPattern(new SimpleColumn(column.getId()), formats.getPattern(column));
          }
          completionQuery.setUserFormatOptions(newFormats);
        }
      } catch (InvalidQueryException e) {
        Assert.untouchable();
      }
    }
    return new QueryPair(dataSourceQuery, completionQuery);
  }

  private QuerySplitter() {
  }
}
