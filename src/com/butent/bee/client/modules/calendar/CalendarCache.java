package com.butent.bee.client.modules.calendar;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class CalendarCache implements HandlesAllDataEvents {

  interface Callback {
    void onSuccess(BeeRowSet result);
  }

  interface MultiCallback {
    void onSuccess(Integer result);
  }

  private static final BeeLogger logger = LogUtils.getLogger(CalendarCache.class);
  
  private final Map<String, BeeRowSet> data = Maps.newHashMap();

  private final Map<String, State> states = Maps.newHashMap();

  private final Map<String, List<Callback>> callbacks = Maps.newHashMap();

  private DataInfo appointmentViewInfo = null;

  CalendarCache() {
    super();
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (isEventRelevant(event)) {
      BeeRowSet rowSet = data.get(event.getViewName());
      if (rowSet != null) {
        rowSet.updateCell(event.getRowId(), event.getColumnIndex(), event.getValue());
      }
    }
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (isEventRelevant(event)) {
      BeeRowSet rowSet = data.get(event.getViewName());
      if (rowSet != null) {
        for (RowInfo rowInfo : event.getRows()) {
          rowSet.removeRowById(rowInfo.getId());
        }
      }
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (isEventRelevant(event)) {
      BeeRowSet rowSet = data.get(event.getViewName());
      if (rowSet != null) {
        rowSet.removeRowById(event.getRowId());
      }
    }
  }
  
  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (isEventRelevant(event)) {
      BeeRowSet rowSet = data.get(event.getViewName());
      if (rowSet != null && !rowSet.containsRow(event.getRowId())) {
        rowSet.addRow(event.getRow());
      }
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (isEventRelevant(event)) {
      BeeRowSet rowSet = data.get(event.getViewName());
      if (rowSet != null) {
        rowSet.updateRow(event.getRow());
      }
    }
  }

  void ensureData(String viewName) {
    getData(viewName, null);
  }
  
  List<BeeColumn> getAppointmentViewColumns() {
    return getAppointmentViewInfo().getColumns();
  }

  DataInfo getAppointmentViewInfo() {
    if (appointmentViewInfo == null) {
      appointmentViewInfo = Data.getDataInfo(CalendarConstants.VIEW_APPOINTMENTS);
    }
    return appointmentViewInfo;
  }

  void getData(String viewName, Callback callback) {
    if (State.LOADED.equals(states.get(viewName))) {
      if (callback != null) {
        callback.onSuccess(data.get(viewName));
      }

    } else {
      if (callback != null) {
        List<Callback> cbs = callbacks.get(viewName);
        if (cbs == null) {
          callbacks.put(viewName, Lists.newArrayList(callback));
        } else {
          cbs.add(callback);
        }
      }
      loadData(viewName);
    }
  }

  void getData(final Collection<String> viewNames, final MultiCallback multiCallback) {
    if (multiCallback == null) {
      for (String viewName : viewNames) {
        ensureData(viewName);
      }

    } else {
      Callback callback = new Callback() {
        private boolean consumed = false;

        @Override
        public void onSuccess(BeeRowSet result) {
          if (this.consumed) {
            return;
          }

          boolean ok = true;
          for (String viewName : viewNames) {
            if (!isLoaded(viewName)) {
              ok = false;
              break;
            }
          }

          if (ok) {
            this.consumed = true;
            multiCallback.onSuccess(viewNames.size());
          }
        }
      };

      for (String viewName : viewNames) {
        getData(viewName, callback);
      }
    }
  }

  Integer getInteger(String viewName, long rowId, String columnId) {
    BeeRow row = getRow(viewName, rowId);
    if (row == null) {
      return null;
    } else {
      return Data.getInteger(viewName, row, columnId);
    }
  }
  
  Long getLong(String viewName, long rowId, String columnId) {
    BeeRow row = getRow(viewName, rowId);
    if (row == null) {
      return null;
    } else {
      return Data.getLong(viewName, row, columnId);
    }
  }
  
  BeeRow getRow(String viewName, long rowId) {
    BeeRowSet rowSet = getRowSet(viewName);
    if (rowSet == null) {
      return null;
    } else {
      return rowSet.getRowById(rowId);
    }
  }
  
  BeeRowSet getRowSet(String viewName) {
    BeeRowSet rowSet = data.get(viewName);
    if (rowSet == null) {
      logger.warning(NameUtils.getName(this), viewName, "data not available");
    }
    return rowSet;
  }

  String getString(String viewName, long rowId, String columnId) {
    BeeRow row = getRow(viewName, rowId);
    if (row == null) {
      return null;
    } else {
      return Data.getString(viewName, row, columnId);
    }
  }
  
  boolean isLoaded(String viewName) {
    return State.LOADED.equals(states.get(viewName));
  }

  void refresh() {
    if (data.isEmpty()) {
      return;
    }
    states.clear();

    for (String viewName : ImmutableSet.copyOf(data.keySet())) {
      loadData(viewName);
    }
  }

  private boolean isEventRelevant(DataEvent event) {
    return event != null && data.containsKey(event.getViewName());
  }
  
  private void loadData(final String viewName) {
    if (State.PENDING.equals(states.get(viewName))) {
      return;
    }
    states.put(viewName, State.PENDING);

    Queries.getRowSet(viewName, null, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        data.put(viewName, result.clone());
        states.put(viewName, State.LOADED);

        List<Callback> cbs = callbacks.get(viewName);
        if (cbs != null) {
          for (Callback callback : cbs) {
            callback.onSuccess(result);
          }
          callbacks.remove(viewName);
        }
      }
    });
  }
}
