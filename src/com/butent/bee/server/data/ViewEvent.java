package com.butent.bee.server.data;

import com.google.common.collect.Lists;

import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public abstract class ViewEvent {

  private final String viewName;
  private List<String> errors;
  private boolean afterStage = false;

  public abstract static class ViewModifyEvent extends ViewEvent {
    ViewModifyEvent(String viewName) {
      super(viewName);
    }
  }

  public static class ViewDeleteEvent extends ViewModifyEvent {
    private final List<Long> ids;

    ViewDeleteEvent(String viewName, List<Long> ids) {
      super(viewName);
      Assert.notEmpty(ids);

      this.ids = ids;
    }

    public List<Long> getIds() {
      return ids;
    }
  }

  public static class ViewInsertEvent extends ViewModifyEvent {
    private List<BeeColumn> columns;
    private BeeRow row;

    ViewInsertEvent(String viewName, List<BeeColumn> columns, BeeRow row) {
      super(viewName);
      Assert.notEmpty(columns);
      Assert.notNull(row);

      this.columns = columns;
      this.row = row;

      setAfter();
    }

    public List<BeeColumn> getColumns() {
      return columns;
    }

    public BeeRow getRow() {
      return row;
    }
  }

  public static class ViewUpdateEvent extends ViewModifyEvent {
    private List<BeeColumn> columns;
    private BeeRow row;
  
    ViewUpdateEvent(String viewName, List<BeeColumn> columns, BeeRow row) {
      super(viewName);
      Assert.notEmpty(columns);
      Assert.notNull(row);
  
      this.columns = columns;
      this.row = row;
  
      setAfter();
    }
  
    public List<BeeColumn> getColumns() {
      return columns;
    }
  
    public BeeRow getRow() {
      return row;
    }
  }

  public static class ViewQueryEvent extends ViewEvent {
    private final SqlSelect query;
    private final BeeRowSet rowset;

    ViewQueryEvent(String viewName, SqlSelect query, BeeRowSet rowset) {
      super(viewName);
      Assert.noNulls(query, rowset);

      this.query = query;
      this.rowset = rowset;

      setAfter();
    }

    public SqlSelect getQuery() {
      return query;
    }

    public BeeRowSet getRowset() {
      return rowset;
    }
  }

  private ViewEvent(String viewName) {
    Assert.notEmpty(viewName);
    this.viewName = viewName;
  }

  public void addErrorMessage(String message) {
    Assert.notEmpty(message);

    if (errors == null) {
      errors = Lists.newArrayList();
    }
    errors.add(message);
  }

  public String getViewName() {
    return viewName;
  }

  public boolean hasErrors() {
    return !BeeUtils.isEmpty(errors);
  }

  public boolean isAfter() {
    return afterStage;
  }

  public boolean isBefore() {
    return !afterStage;
  }

  List<String> getErrorMessages() {
    return errors;
  }

  void setAfter() {
    this.afterStage = true;
  }
}
