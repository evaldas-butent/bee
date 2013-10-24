package com.butent.bee.client.modules.discussions;

import com.butent.bee.client.modules.discussions.DiscussionsList.ListType;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;

class DiscussionsGridHandler extends AbstractGridInterceptor {

  private final ListType type;

  public DiscussionsGridHandler(ListType type) {
    this.type = type;
  }

}
