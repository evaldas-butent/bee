package com.butent.bee.client.view.search;

import com.google.gwt.event.dom.client.HasChangeHandlers;

import com.butent.bee.client.view.View;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

/**
 * Requires implementing classes to have {@code getFilter} method.
 */

public interface SearchView extends View, HasChangeHandlers {
  Filter getFilter(List<? extends IsColumn> columns);
}
