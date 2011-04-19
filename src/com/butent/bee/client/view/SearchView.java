package com.butent.bee.client.view;

import com.google.gwt.event.dom.client.HasChangeHandlers;

import com.butent.bee.shared.data.view.Filter;

public interface SearchView extends View, HasChangeHandlers {
  Filter getFilter();
}
