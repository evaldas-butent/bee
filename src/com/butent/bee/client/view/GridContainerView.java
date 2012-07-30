package com.butent.bee.client.view;

import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;

import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.List;

/**
 * Requires for implementing classes to be able to create a grid container with specified parameters
 * and get it's content.
 */

public interface GridContainerView extends View, HasAllDragAndDropHandlers, HasGridView, Printable {

  void bind();

  void create(GridDescription gridDescription, List<BeeColumn> dataColumns, int rowCount,
      BeeRowSet rowSet, Order order, GridCallback gridCallback, Collection<UiOption> uiOptions,
      GridFactory.GridOptions gridOptions);

  List<String> getFavorite();
  
  HeaderView getHeader();
}
