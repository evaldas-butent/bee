package com.butent.bee.client.view;

import com.butent.bee.shared.data.event.SelectionCountChangeEvent;

/**
 * Requires to have {@code create} method with given row count, page size, paging and search
 * options.
 */

public interface DataFooterView extends View, SelectionCountChangeEvent.Handler {
  void create(int rowCount, boolean addPaging, boolean showPageSize, boolean addSearch);
}
