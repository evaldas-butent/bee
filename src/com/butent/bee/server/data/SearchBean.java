package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class SearchBean {
  
  private static Multimap<String, String> searchableColumns = null;

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  ModuleHolderBean mh;

  public ResponseObject processQuery(String query) {
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.error("search query not specified");
    }
    
    if (searchableColumns == null) {
      searchableColumns = mh.getSearchableColumns();
    }

    List<SearchResult> results = Lists.newArrayList();
    
    for (String viewName : searchableColumns.keySet()) {
      Collection<String> columns = searchableColumns.get(viewName);
      if (columns.isEmpty()) {
        continue;
      }
      
      CompoundFilter filter = Filter.or();
      for (String column : columns) {
        filter.add(ComparisonFilter.contains(column, query));
      }
      
      BeeRowSet rowSet = qs.getViewData(viewName, filter);
      if (rowSet == null || rowSet.isEmpty()) {
        continue;
      }
      
      for (BeeRow row : rowSet.getRows()) {
        results.add(new SearchResult(viewName, row));
      }
    }
    return ResponseObject.response(results);
  }
}
