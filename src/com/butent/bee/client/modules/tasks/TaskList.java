package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;

import com.butent.bee.client.Global;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskType;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.utils.BeeUtils;

final class TaskList {

  static Consumer<GridOptions> getFeedFilterHandler(Feed feed) {
    final TaskType type = TaskType.getByFeed(feed);
    Assert.notNull(type);

    Consumer<GridOptions> consumer = new Consumer<GridFactory.GridOptions>() {
      @Override
      public void accept(GridOptions input) {
        String caption = BeeUtils.notEmpty(input.getCaption(), type.getCaption());
        GridFactory.openGrid(TaskConstants.GRID_TASKS, new TasksGrid(type, caption), input);
      }
    };

    return consumer;
  }

  static void open(String args) {
    TaskType type = TaskType.getByPrefix(args);

    if (type == null) {
      Global.showError(Lists.newArrayList(TaskConstants.GRID_TASKS, "Type not recognized:", args));
    } else {
      GridFactory.openGrid(TaskConstants.GRID_TASKS, new TasksGrid(type, type.getCaption()));
    }
  }

  private TaskList() {
    super();
  }
}
