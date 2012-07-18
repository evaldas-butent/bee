package com.butent.bee.shared.data.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CacheManager implements HandlesAllDataEvents {

  private class Entry implements HasExtendedInfo, HasViewName {

    private final DataInfo dataInfo;
    
    private final int maximumSize;
    private final ReplacementPolicy replacementPolicy;

    private final CachedData dataRows;
    private final Set<CachedQuery> queries = Sets.newHashSet();

    private Entry(DataInfo dataInfo) {
      this.dataInfo = dataInfo;
      
      this.maximumSize = BeeUtils.nvl(dataInfo.getCacheMaximumSize(), DEFAULT_MAXIMUM_SIZE);
      this.replacementPolicy = BeeUtils.nvl(dataInfo.getCacheReplacementPolicy(),
          DEFAULT_REPLACEMENT_POLICY);
      
      this.dataRows = new CachedData(maximumSize, replacementPolicy);
    }

    public List<ExtendedProperty> getExtendedInfo() {
      List<ExtendedProperty> info = Lists.newArrayList();

      info.add(new ExtendedProperty("View Name", getViewName()));

      String pfx = getViewName();
      info.add(new ExtendedProperty(pfx, "Column Count",
          BeeUtils.toString(dataInfo.getColumnCount())));

      PropertyUtils.appendChildrenToExtended(info, pfx + " data", dataRows.getInfo());
      info.add(new ExtendedProperty(pfx, "Cached Queries", BeeUtils.toString(queries.size())));

      int idx = 0;
      for (CachedQuery query : queries) {
        PropertyUtils.appendChildrenToExtended(info,
            BeeUtils.concat(1, pfx, "query", BeeUtils.progress(++idx, queries.size())),
            query.getInfo());
      }
      return info;
    }

    @Override
    public String getViewName() {
      return dataInfo.getViewName();
    }

    private void addRows(List<BeeRow> rows, Filter filter, Order order, int offset) {
      Assert.notNull(rows);

      CachedQuery query = getQuery(filter, order);
      if (query == null) {
        query = new CachedQuery(filter, order, maximumSize, replacementPolicy);
        queries.add(query);
      }

      int p = Math.max(offset, 0);
      for (BeeRow row : rows) {
        dataRows.add(row.getId(), row);
        query.add(p++, row.getId());
      }
    }

    private void checkColumnUpdate(String columnId) {
      for (Iterator<CachedQuery> it = queries.iterator(); it.hasNext();) {
        CachedQuery query = it.next();
        if (query.containsColumn(columnId)) {
          BeeKeeper.getLog().info("Cache", getViewName(), "update column", columnId);
          BeeKeeper.getLog().info("invalidated query", query.getStrFilter(), query.getStrOrder());

          query.invalidate();
          it.remove();
        }
      }
    }

    private boolean containsRange(Filter filter, Order order, int offset, int limit) {
      if (dataRows.isEmpty() || offset < 0 || limit <= 0) {
        return false;
      }

      CachedQuery query = getQuery(filter, order);
      if (query == null) {
        return false;

      } else if (!dataRows.isFull()) {
        return query.containsRange(offset, limit);

      } else {
        for (int i = 0; i < limit; i++) {
          Long rowId = query.get(offset + i);
          if (rowId == null) {
            return false;
          }
          if (!dataRows.containsKey(rowId)) {
            return false;
          }
        }
        return true;
      }
    }

    private boolean deleteRow(long id) {
      long millis = System.currentTimeMillis();

      boolean ok = dataRows.deleteKey(id);
      if (ok) {
        for (Iterator<CachedQuery> it = queries.iterator(); it.hasNext();) {
          CachedQuery query = it.next();
          query.deleteValue(id);
          if (query.isEmpty()) {
            it.remove();
          }
        }
        BeeKeeper.getLog().info("Cache", getViewName(), "deleted row", id, "in",
            System.currentTimeMillis() - millis);
      }
      return ok;
    }

    private CachedQuery getQuery(Filter filter, Order order) {
      for (CachedQuery query : queries) {
        if (query.same(filter, order)) {
          return query;
        }
      }
      return null;
    }

    private List<BeeRow> getRows(Filter filter, Order order, int offset, int limit) {
      if (dataRows.isEmpty()) {
        return null;
      }

      CachedQuery query = getQuery(filter, order);
      if (query == null) {
        return null;
      }

      int start = Math.max(offset, 0);
      int length = (limit > 0) ? limit : query.getRowCount() - start;
      if (length <= 0) {
        return null;
      }

      List<Long> rowIds = query.getRowIds(start, length);
      if (rowIds == null) {
        return null;
      }
      return dataRows.getRows(rowIds);
    }

    private BeeRowSet getRowSet(Filter filter, Order order, int offset, int limit) {
      List<BeeRow> rows = getRows(filter, order, offset, limit);
      if (rows == null) {
        return null;
      }
      return new BeeRowSet(getViewName(), dataInfo.getColumns(), rows);
    }

    private void insertRow(BeeRow row) {
      dataRows.add(row.getId(), row);

      for (CachedQuery query : queries) {
        query.invalidate();
      }
      queries.clear();

      BeeKeeper.getLog().info("Cache", getViewName(), "inserted row", row.getId());
    }

    private void invalidate() {
      dataRows.invalidate();
      for (CachedQuery query : queries) {
        query.invalidate();
      }
    }

    private void setRowCount(Filter filter, Order order, int rowCount) {
      boolean found = false;

      for (CachedQuery query : queries) {
        if (query.sameFilter(filter)) {
          query.setRowCount(rowCount);
          found = true;
        }
      }

      if (!found) {
        CachedQuery query = new CachedQuery(filter, order, maximumSize, replacementPolicy);
        query.setRowCount(rowCount);
        queries.add(query);
      }
    }

    private boolean updateCell(long rowId, long version, String columnId, String value) {
      BeeRow row = dataRows.get(rowId);
      if (row == null) {
        return false;
      }
      row.setVersion(version);

      boolean ok = false;
      for (int i = 0; i < dataInfo.getColumnCount(); i++) {
        if (BeeUtils.same(dataInfo.getColumnId(i), columnId)) {
          row.setValue(i, value);
          ok = true;
        }
      }

      if (ok) {
        checkColumnUpdate(columnId);
        BeeKeeper.getLog().info("Cache", getViewName(), "updated", rowId, columnId, value);
      } else {
        BeeKeeper.getLog().warning("Cache", getViewName(), "column", columnId, "not found");
      }
      return ok;
    }

    private boolean updateRow(IsRow newRow) {
      boolean ok = false;
      if (newRow == null) {
        return ok;
      }
      BeeRow oldRow = dataRows.get(newRow.getId());
      if (oldRow == null) {
        return ok;
      }
      oldRow.setVersion(newRow.getVersion());

      for (int i = 0; i < dataInfo.getColumnCount(); i++) {
        if (BeeUtils.equalsTrimRight(oldRow.getString(i), newRow.getString(i))) {
          continue;
        }
        oldRow.setValue(i, newRow.getString(i));
        ok = true;

        checkColumnUpdate(dataInfo.getColumnId(i));
      }

      if (ok) {
        BeeKeeper.getLog().info("Cache", getViewName(), "updated row", newRow.getId());
      }
      return ok;
    }
  }

  private static final int DEFAULT_MAXIMUM_SIZE = 0x3fff;
  private static final ReplacementPolicy DEFAULT_REPLACEMENT_POLICY =
      ReplacementPolicy.FIRST_IN_FIRST_OUT;
  
  private final Map<String, Entry> entries = Maps.newHashMap();

  public CacheManager() {
    super();
  }

  public void add(DataInfo dataInfo, BeeRowSet rowSet, Filter filter, Order order, int offset,
      int limit) {
    if (dataInfo == null || rowSet == null) {
      return;
    }

    int rowCount = rowSet.getNumberOfRows();
    boolean isComplete = offset <= 0 && (limit <= 0 || limit > rowCount);

    if (rowCount > 0 || isComplete) {
      String key = normalizeKey(dataInfo.getViewName());
      Entry entry = get(key);

      if (entry == null) {
        if (BeeUtils.isZero(dataInfo.getCacheMaximumSize())) {
          return;
        }
        entry = new Entry(dataInfo);
        entries.put(key, entry);
      }

      if (rowCount > 0) {
        entry.addRows(rowSet.getRows().getList(), filter, order, offset);
      }
      if (isComplete) {
        entry.setRowCount(filter, order, rowCount);
      }
    }
  }

  public void clear() {
    invalidateAll();
    entries.clear();
  }

  public boolean containsRange(String viewName, Filter filter, Order order, int offset, int limit) {
    if (BeeUtils.isEmpty(viewName)) {
      return false;
    }
    Entry entry = get(viewName);
    if (entry == null) {
      return false;
    }
    return entry.containsRange(filter, order, offset, limit);
  }
  
  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();
    info.add(new ExtendedProperty("Cache", "Entries", BeeUtils.toString(entries.size())));

    int idx = 0;
    for (Entry entry : entries.values()) {
      PropertyUtils.appendWithPrefix(info, BeeUtils.progress(++idx, entries.size()),
          entry.getExtendedInfo());
    }
    return info;
  }

  public BeeRowSet getRowSet(String viewName, Filter filter, Order order, int offset, int limit) {
    Assert.notEmpty(viewName);
    Entry entry = get(viewName);
    if (entry == null) {
      return null;
    }
    return entry.getRowSet(filter, order, offset, limit);
  }

  public void onCellUpdate(CellUpdateEvent event) {
    String key = event.getViewName();
    if (contains(key)) {
      get(key).updateCell(event.getRowId(), event.getVersion(), event.getColumnName(),
          event.getValue());
    }
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    String key = event.getViewName();
    if (!contains(key)) {
      return;
    }

    int cnt = 0;
    for (RowInfo rowInfo : event.getRows()) {
      if (deleteRow(key, rowInfo.getId())) {
        cnt++;
      }
    }
    BeeKeeper.getLog().info("Cache", key, "deleted", cnt, "rows", "of", event.getRows().size());
  }

  public void onRowDelete(RowDeleteEvent event) {
    deleteRow(event.getViewName(), event.getRowId());
  }

  public void onRowInsert(RowInsertEvent event) {
    insertRow(event.getViewName(), event.getRow());
  }

  public void onRowUpdate(RowUpdateEvent event) {
    String key = event.getViewName();
    if (contains(key)) {
      get(key).updateRow(event.getRow());
    }
  }

  public void remove(String key) {
    if (contains(key)) {
      get(key).invalidate();
      entries.remove(normalizeKey(key));
      BeeKeeper.getLog().info("Cache", key, "removed");
    }
  }

  private boolean contains(String key) {
    if (BeeUtils.isEmpty(key)) {
      return false;
    }
    return entries.containsKey(normalizeKey(key));
  }

  private boolean deleteRow(String key, long rowId) {
    if (contains(key)) {
      return get(key).deleteRow(rowId);
    } else {
      return false;
    }
  }

  private Entry get(String key) {
    return entries.get(normalizeKey(key));
  }

  private boolean insertRow(String key, BeeRow row) {
    Assert.notNull(row);
    if (!contains(key)) {
      return false;
    }
    get(key).insertRow(row);
    return true;
  }

  private void invalidateAll() {
    for (Entry entry : entries.values()) {
      entry.invalidate();
    }
  }

  private String normalizeKey(String key) {
    return BeeUtils.normalize(key);
  }
}
