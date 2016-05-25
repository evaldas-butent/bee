package com.butent.bee.client.i18n;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class DictionaryGrid extends AbstractGridInterceptor {

  public DictionaryGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new DictionaryGrid();
  }
}
