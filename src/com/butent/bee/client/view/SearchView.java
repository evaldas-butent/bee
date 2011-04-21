package com.butent.bee.client.view;

import com.google.gwt.event.dom.client.HasChangeHandlers;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

public interface SearchView extends View, HasChangeHandlers {

  Filter getFilter(List<? extends IsColumn> columns);
}
