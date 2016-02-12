package com.butent.bee.client.communication;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class ChatGrid extends AbstractGridInterceptor {

  public ChatGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new ChatGrid();
  }
}
