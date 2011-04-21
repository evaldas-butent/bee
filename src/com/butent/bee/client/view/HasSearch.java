package com.butent.bee.client.view;

import com.butent.bee.client.view.search.SearchView;

import java.util.Collection;

public interface HasSearch {
  Collection<SearchView> getSearchers();
}
