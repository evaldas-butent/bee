package com.butent.bee.client.view;

import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.ui.GridDescription;

import java.util.List;

/**
 * Requires for implementing classes to be able to create a grid container with specified parameters
 * and get it's content.
 */

public interface GridContainerView extends View {

  void bind();

  void create(String caption, List<BeeColumn> dataColumns, int rowCount, BeeRowSet rowSet,
      GridDescription gridDescription, boolean isChild);

  GridView getContent();
}
