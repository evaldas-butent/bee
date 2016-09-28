package com.butent.bee.client.render;

import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.grid.cell.AbstractCell;

public class RenderableCell extends AbstractCell<String> {

  public RenderableCell() {
    super();
  }

  @Override
  public String render(CellContext context, String value) {
    return value;
  }
}
