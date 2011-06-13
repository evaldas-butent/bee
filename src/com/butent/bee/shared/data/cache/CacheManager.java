package com.butent.bee.shared.data.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enables operations within cache, adding and removing cached objects.
 */

public class CacheManager implements HandlesDeleteEvents, HandlesUpdateEvents {

  /**
   * Handles single cache entry, contains it's attributes and methods for changing them.
   */

  private class Entry implements HasExtendedInfo {
    private final String viewName;
    private final List<BeeColumn> columns;
    private final CachedData data = new CachedData();
    private final Set<CachedQuery> queries = Sets.newHashSet();

    private Entry(String viewName, List<BeeColumn> columns) {
      this.viewName = viewName;
      this.columns = columns;
    }

    public List<ExtendedProperty> getInfo() {
      List<ExtendedProperty> info = Lists.newArrayList();

      info.add(new ExtendedProperty("View Name", viewName));
      String pfx = viewName.trim();
      int idx;

      info.add(new ExtendedProperty(pfx, "Column Count", BeeUtils.toString(columns.size())));
      idx = 0;
      for (BeeColumn column : columns) {
        info.add(new ExtendedProperty(pfx + " column", BeeUtils.progress(++idx, columns.size()),
            column.getLabel()));
      }

      PropertyUtils.appendChildrenToExtended(info, pfx + " data", data.getInfo());
      info.add(new ExtendedProperty(pfx, "Cached Queries", BeeUtils.toString(queries.size())));

      idx = 0;
      for (CachedQuery query : queries) {
        PropertyUtils.appendChildrenToExtended(info,
            BeeUtils.concat(1, pfx, "query", BeeUtils.progress(++idx, queries.size())),
            query.getInfo());
      }
      return info;
    }

    private void addRows(List<BeeRow> rows, Filter filter, Order order, int offset) {
      Assert.notNull(rows);
      Assert.nonNegative(offset);

      CachedQuery query = getQuery(filter, order);
      if (query == null) {
        query = new CachedQuery(filter, order);
        queries.add(query);
      }

      int p = offset;
      for (BeeRow row : rows) {
        data.add(row.getId(), row);
        query.add(p++, row.getId());
      }
    }
    
    private void checkColumnUpdate(String columnId) {
    for (Iterator<CachedQuery> it = queries.iterator(); it.hasNext(); ) {
      CachedQuery query = it.next();
      if (query.containsColumn(columnId)) {
        BeeKeeper.getLog().info("Cache", viewName, "update column", columnId);
        BeeKeeper.getLog().info("invalidated query", query.getStrFilter(), query.getStrOrder());

        query.invalidate();
        it.remove();
      }
    }}
    
    private void clearHistory() {
      data.clearHistory();
      for (CachedQuery query : queries) {
        query.clearHistory();
      }
    }
    
    private boolean deleteRow(long id) {
      boolean ok = data.deleteKey(id);
      if (ok) {
        for (Iterator<CachedQuery> it = queries.iterator(); it.hasNext(); ) {
          CachedQuery query = it.next();
          query.deleteValue(id);
          if (query.isEmpty()) {
            it.remove();
          }
        }
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
      if (data.isEmpty()) {
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
      return data.getRows(rowIds);
    }

    private BeeRowSet getRowSet(Filter filter, Order order, int offset, int limit) {
      List<BeeRow> rows = getRows(filter, order, offset, limit);
      if (rows == null) {
        return null;
      }
      return new BeeRowSet(viewName, columns, rows);
    }

    private void invalidate() {
      data.invalidate();
      for (CachedQuery query : queries) {
        query.invalidate();
      }
    }
    
    private boolean isEmpty() {
      return data.isEmpty();
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
        CachedQuery query = new CachedQuery(filter, order);
        query.setRowCount(rowCount);
        queries.add(query);
      }
    }

    private boolean updateCell(long rowId, long version, String columnId, String value) {
      BeeRow row = data.get(rowId);
      if (row == null) {
        return false;
      }
      row.setVersion(version);
      
      boolean ok = false;
      for (int i = 0; i < columns.size(); i++) {
        if (BeeUtils.same(columns.get(i).getLabel(), columnId)) {
          row.setValue(i, value);
          ok = true;
        }
      }
      if (!ok) {
        BeeKeeper.getLog().warning("Cache", viewName, "column", columnId, "not found");
        return ok;
      }
      
      checkColumnUpdate(columnId);
      return ok;
    }

    private boolean updateRow(BeeRow newRow) {
      boolean ok = false;
      if (newRow == null) {
        return ok;
      }
      BeeRow oldRow = data.get(newRow.getId());
      if (oldRow == null) {
        return ok;
      }
      oldRow.setVersion(newRow.getVersion());
      
      for (int i = 0; i < columns.size(); i++) {
        if (BeeUtils.equalsTrimRight(oldRow.getString(i), newRow.getString(i))) {
          continue;
        }
        oldRow.setValue(i, newRow.getString(i));
        ok = true;

        checkColumnUpdate(columns.get(i).getLabel());
      }
      return ok;
    }
  }

  private final Map<String, Entry> entries = Maps.newHashMap();
  
  public CacheManager() {
    super();
  }

  public void add(BeeRowSet rowSet) {
    add(rowSet, null);
  }

  public void add(BeeRowSet rowSet, Filter filter) {
    add(rowSet, filter, null);
  }

  public void add(BeeRowSet rowSet, Filter filter, Order order) {
    add(rowSet, filter, order, 0);
  }

  public void add(BeeRowSet rowSet, Filter filter, Order order, int offset) {
    add(rowSet, filter, order, offset, -1);
  }

  public void add(BeeRowSet rowSet, Filter filter, Order order, int offset, int limit) {
    Assert.notNull(rowSet);
    Assert.nonNegative(offset);

    int rowCount = rowSet.getNumberOfRows();
    boolean isComplete = offset <= 0 && (limit <= 0 || limit > rowCount);

    if (rowCount > 0 || isComplete) {
      Entry entry = put(rowSet.getViewName(), rowSet.getColumns());

      if (rowCount > 0) {
        entry.addRows(rowSet.getRows().getList(), filter, order, offset);
      }
      if (isComplete) {
        entry.setRowCount(filter, order, rowCount);
      }
    }
  }

  public void clearAllHistory() {
    for (Entry entry : entries.values()) {
      entry.clearHistory();
    }
  }

  public void clearHistory(String key) {
    assertKey(key);
    get(key).clearHistory();
  }

  public boolean contains(String key) {
    if (BeeUtils.isEmpty(key)) {
      return false;
    }
    return entries.containsKey(normalizeKey(key));
  }
  
  public boolean deleteRow(String key, long rowId) {
    if (!contains(key)) {
      return false;
    }
    Entry entry = get(key);
    
    boolean ok = entry.deleteRow(rowId);
    if (ok && entry.isEmpty()) {
      remove(key);
    }
    
    return ok;
  }

  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();
    info.add(new ExtendedProperty("Cache", "Entries", BeeUtils.toString(entries.size())));

    int idx = 0;
    for (Entry entry : entries.values()) {
      PropertyUtils.appendWithPrefix(info, BeeUtils.progress(++idx, entries.size()),
          entry.getInfo());
    }
    return info;
  }

  public List<BeeRow> getRows(String viewName, Filter filter, Order order, int offset, int limit) {
    Assert.notEmpty(viewName);
    Entry entry = get(viewName);
    if (entry == null) {
      return null;
    }
    return entry.getRows(filter, order, offset, limit);
  }

  public BeeRowSet getRowSet(String viewName, Filter filter, Order order, int offset, int limit) {
    Assert.notEmpty(viewName);
    Entry entry = get(viewName);
    if (entry == null) {
      return null;
    }
    return entry.getRowSet(filter, order, offset, limit);
  }

  public void invalidate(String key) {
    assertKey(key);
    get(key).invalidate();
  }

  public void invalidateAll() {
    for (Entry entry : entries.values()) {
      entry.invalidate();
    }
  }

  public void invalidateQuietly(String key) {
    if (contains(key)) {
      get(key).invalidate();
    }
  }

  public void onCellUpdate(CellUpdateEvent event) {
    Assert.notNull(event);
    String key = event.getViewName();
    if (!contains(key)) {
      return;
    }

    Entry entry = get(key);
    long rowId = event.getRowId();
    long version = event.getVersion();
    String columnId = event.getColumnId();
    String value = event.getValue();
    
    if (entry.updateCell(rowId, version, columnId, value)) {
      BeeKeeper.getLog().info("Cache", key, "updated", rowId, columnId, value);
    }
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    Assert.notNull(event);
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
    Assert.notNull(event);
    if (deleteRow(event.getViewName(), event.getRowId())) {
      BeeKeeper.getLog().info("Cache", event.getViewName(), "deleted row id:", event.getRowId());
    }
  }

  public void onRowUpdate(RowUpdateEvent event) {
    Assert.notNull(event);
    String key = event.getViewName();
    if (!contains(key)) {
      return;
    }

    Entry entry = get(key);
    BeeRow row = event.getRow();
    
    if (entry.updateRow(row)) {
      BeeKeeper.getLog().info("Cache", key, "updated row", row.getId());
    }
  }
  
  public Entry put(String viewName, List<BeeColumn> columns) {
    Assert.notEmpty(viewName);

    Entry entry = get(viewName);
    if (entry == null) {
      Assert.notEmpty(columns);
      entry = new Entry(viewName, columns);
      entries.put(normalizeKey(viewName), entry);
    }
    return entry;
  }

  public void remove(String key) {
    invalidate(key);
    entries.remove(normalizeKey(key));
    BeeKeeper.getLog().info("Cache: removed", key);
  }

  public void removeAll() {
    invalidateAll();
    entries.clear();
  }
  
  public void removeQuietly(String key) {
    if (contains(key)) {
      invalidate(key);
      entries.remove(normalizeKey(key));
    }
  }

  private void assertKey(String key) {
    Assert.notEmpty(key);
    Assert.contains(entries, normalizeKey(key));
  }

  private Entry get(String key) {
    return entries.get(normalizeKey(key));
  }

  private String normalizeKey(String key) {
    return BeeUtils.normalize(key);
  }
}
