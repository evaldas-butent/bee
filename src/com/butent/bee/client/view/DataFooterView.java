package com.butent.bee.client.view;

/**
 * Requires to have {@code create} method with given row count, page size, paging and search
 * options.
 */

public interface DataFooterView extends View {
  void create(int rowCount, int pageSize, boolean addPaging, boolean addSearch);
}
