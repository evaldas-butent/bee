package com.butent.bee.client.view;

import com.butent.bee.client.view.search.SearchView;

import java.util.Collection;

/**
 * Requires any implementing classes to be able to get their searchers.
 */

public interface HasSearch {
  Collection<SearchView> getSearchers();
}
