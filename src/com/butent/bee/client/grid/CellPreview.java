package com.butent.bee.client.grid;

import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.CellPreviewEvent;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Enables previewing of cell value by reacting to mouse action.
 */

public class CellPreview implements CellPreviewEvent.Handler<IsRow> {
  private final int listen;
  private final int ignore;

  public CellPreview() {
    this(Event.ONMOUSEMOVE | Event.ONMOUSEOUT | Event.ONMOUSEOVER, false);
  }

  public CellPreview(int events, boolean handle) {
    if (handle) {
      this.listen = events;
      this.ignore = 0;
    } else {
      this.listen = 0;
      this.ignore = events;
    }
  }

  public void onCellPreview(CellPreviewEvent<IsRow> event) {
    int type = EventUtils.getTypeInt(event.getNativeEvent());
    if (type != 0) {
      if (listen != 0 && (listen & type) == 0) {
        return;
      }
      if (ignore != 0 && (ignore & type) != 0) {
        return;
      }
    }

    for (Property p : getInfo(event)) {
      BeeKeeper.getLog().info(p.getName(), p.getValue());
    }
    BeeKeeper.getLog().addSeparator();
  }

  private List<Property> getInfo(CellPreviewEvent<IsRow> event) {
    List<Property> lst = PropertyUtils.createProperties(
        "Column", event.getColumn(),
        "Index", event.getIndex(),
        "Key", event.getContext().getKey(),
        "Row Id", event.getValue().getId(),
        "Native", EventUtils.transformEvent(event.getNativeEvent(), false));

    PropertyUtils.addNotEemptyProperties(lst,
        "Canceled", event.isCanceled(),
        "Cell Editing", event.isCellEditing(),
        "Selection Handled", event.isSelectionHandled());

    return lst;
  }
}
