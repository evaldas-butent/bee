package com.butent.bee.client.render;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.utils.JreEmulation;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.ui.RendererDescription;
import com.butent.bee.shared.ui.RendererType;

import java.util.List;

public class RendererFactory {

  public static AbstractCellRenderer createRenderer(Calculation calculation,
      List<? extends IsColumn> dataColumns, int dataIndex) {
    Assert.notNull(calculation);
    Assert.isIndex(dataColumns, dataIndex);

    IsColumn column = dataColumns.get(dataIndex);
    return new EvalRenderer(dataIndex, column, Evaluator.create(calculation, column.getId(),
        dataColumns));
  }

  public static AbstractCellRenderer createRenderer(RendererDescription description,
      int dataIndex, IsColumn dataColumn) {
    Assert.notNull(description);
    RendererType type = description.getType();
    Assert.notNull(type);

    AbstractCellRenderer renderer = null;

    switch (type) {
      case LIST:
        renderer = new ListRenderer(dataIndex, dataColumn);
        break;
      case MAP:
        renderer = new MapRenderer(dataIndex, dataColumn, description.getSeparator());
        break;
      case RANGE:
        renderer = new RangeRenderer(dataIndex, dataColumn, description.getSeparator(),
            description.getOptions());
        break;
    }
    Assert.notNull(renderer);

    if (renderer instanceof HasValueStartIndex && description.getValueStartIndex() != null) {
      ((HasValueStartIndex) renderer).setValueStartIndex(description.getValueStartIndex());
    }

    if (renderer instanceof HasItems) {
      if (description.getItems() != null) {
        ((HasItems) renderer).setItems(description.getItems());
      } else {
        BeeKeeper.getLog().warning(JreEmulation.getSimpleName(renderer), "no items initialized");
      }
    }
    return renderer;
  }

  public static AbstractCellRenderer getRenderer(RendererDescription description,
      Calculation calculation, List<? extends IsColumn> dataColumns, int dataIndex) {
    if (description != null) {
      Assert.isIndex(dataColumns, dataIndex);
      return createRenderer(description, dataIndex, dataColumns.get(dataIndex));
    } else if (calculation != null) {
      return createRenderer(calculation, dataColumns, dataIndex);
    } else {
      return null;
    }
  }

  private RendererFactory() {
  }
}
