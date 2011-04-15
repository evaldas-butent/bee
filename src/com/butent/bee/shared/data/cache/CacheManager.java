package com.butent.bee.shared.data.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.view.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CacheManager {

  private static class Entry implements HasExtendedInfo {
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
    
    private void assertRange(int offset, int limit) {
      Assert.nonNegative(offset);
      Assert.isPositive(limit);
    }
    
    private void clearHistory() {
      data.clearHistory();
      for (CachedQuery query : queries) {
        query.clearHistory();
      }
    }

    private CachedQuery getQuery(Filter filter, Order order) {
      for (CachedQuery query : queries) {
        if (query.same(filter, order)) {
          return query;
        }
      }
      return null;
    }
    
    private int getRowCount(Filter filter) {
      int rowCount = BeeConst.SIZE_UNKNOWN;
      int qrc;
      
      for (CachedQuery query : queries) {
        if (query.sameFilter(filter)) {
          qrc = query.getRowCount();
          if (qrc >= 0) {
            rowCount = qrc;
            break;
          }
        }
      }
      return rowCount;
    }

    private List<BeeRow> getRows(Filter filter, Order order, int offset, int limit) {
      assertRange(offset, limit);

      if (data.isEmpty()) {
        return null;
      }
      CachedQuery query = getQuery(filter, order);
      if (query == null) {
        return null;
      }
      
      List<Long> rowIds = query.getRowIds(offset, offset + limit);
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
  }
  
  private static final Map<String, Entry> ENTRIES = Maps.newHashMap();
  
  public static void add(BeeRowSet rowSet) {
    add(rowSet, null);
  }
  
  public static void add(BeeRowSet rowSet, Filter filter) {
    add(rowSet, filter, null);
  }

  public static void add(BeeRowSet rowSet, Filter filter, Order order) {
    add(rowSet, filter, order, 0);
  }
  
  public static void add(BeeRowSet rowSet, Filter filter, Order order, int offset) {
    add(rowSet, filter, order, offset, -1);
  }
  
  public static void add(BeeRowSet rowSet, Filter filter, Order order, int offset, int limit) {
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

  public static void clearAllHistory() {
    for (Entry entry : ENTRIES.values()) {
      entry.clearHistory();
    }
  }
  
  public static void clearHistory(String key) {
    assertKey(key);
    get(key).clearHistory();
  }

  public static boolean contains(String key) {
    if (BeeUtils.isEmpty(key)) {
      return false;
    }
    return ENTRIES.containsKey(normalizeKey(key));
  }

  public static List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();
    info.add(new ExtendedProperty("Cache", "Entries", BeeUtils.toString(ENTRIES.size())));

    int idx = 0;
    for (Entry entry : ENTRIES.values()) {
      PropertyUtils.appendWithPrefix(info, BeeUtils.progress(++idx, ENTRIES.size()),
          entry.getInfo());
    }
    return info;
  }
  
  public static int getRowCount(String viewName, Filter filter) {
    Assert.notEmpty(viewName);
    Entry entry = get(viewName);
    if (entry == null) {
      return BeeConst.SIZE_UNKNOWN;
    }
    return entry.getRowCount(filter);
  }

  public static List<BeeRow> getRows(String viewName, Filter filter, Order order,
      int offset, int limit) {
    Assert.notEmpty(viewName);
    Entry entry = get(viewName);
    if (entry == null) {
      return null;
    }
    return entry.getRows(filter, order, offset, limit);
  }
  
  public static BeeRowSet getRowSet(String viewName, Filter filter, Order order,
      int offset, int limit) {
    Assert.notEmpty(viewName);
    Entry entry = get(viewName);
    if (entry == null) {
      return null;
    }
    return entry.getRowSet(filter, order, offset, limit);
  }
  
  public static void invalidate(String key) {
    assertKey(key);
    get(key).invalidate();
  }

  public static void invalidateAll() {
    for (Entry entry : ENTRIES.values()) {
      entry.invalidate();
    }
  }
  
  public static Entry put(String viewName, List<BeeColumn> columns) {
    Assert.notEmpty(viewName);
    
    Entry entry = get(viewName);
    if (entry == null) {
      Assert.notEmpty(columns);
      entry = new Entry(viewName, columns);
      ENTRIES.put(normalizeKey(viewName), entry);
    }
    return entry; 
  }
  
  public static void remove(String key) {
    invalidate(key);
    ENTRIES.remove(normalizeKey(key));
  }
  
  public static void removeAll() {
    invalidateAll();
    ENTRIES.clear();
  }
  
  public static boolean setRowCount(String viewName, Filter filter, Order order, int rowCount) {
    Assert.notEmpty(viewName);
    Entry entry = get(viewName);
    if (entry == null) {
      return false;
    }

    entry.setRowCount(filter, order, rowCount);
    return true;
  }

  private static void assertKey(String key) {
    Assert.notEmpty(key);
    Assert.contains(ENTRIES, normalizeKey(key));
  }
  
  private static Entry get(String key) {
    return ENTRIES.get(normalizeKey(key));
  }

  private static String normalizeKey(String key) {
    return BeeUtils.normalize(key);
  }

  private CacheManager() {
  }
}
