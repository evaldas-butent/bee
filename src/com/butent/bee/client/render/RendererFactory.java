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
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RendererFactory {

  public static AbstractCellRenderer createRenderer(Calculation calculation,
      List<? extends IsColumn> dataColumns, int dataIndex) {
    Assert.notNull(calculation);

    IsColumn column = BeeUtils.getQuietly(dataColumns, dataIndex);
    String columnId = (column == null) ? null : column.getId();
    
    Evaluator evaluator = Evaluator.create(calculation, columnId, dataColumns);
    if (column != null) {
      evaluator.setColIndex(dataIndex);
    }

    return new EvalRenderer(dataIndex, column, evaluator);
  }

  public static AbstractCellRenderer createRenderer(RendererDescription description,
      String itemKey, List<? extends IsColumn> dataColumns, int dataIndex) {
    Assert.notNull(description);
    RendererType type = description.getType();
    Assert.notNull(type);

    IsColumn dataColumn = BeeUtils.getQuietly(dataColumns, dataIndex);
    if (dataColumn == null && type.requiresSource()) {
      BeeKeeper.getLog().warning("renderer", type.getTypeCode(), "requires source");
      return null;
    }

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

      case ENUM:
        if (!BeeUtils.isEmpty(itemKey)) {
          renderer = new EnumRenderer(dataIndex, dataColumn, itemKey);
        } else {
          BeeKeeper.getLog().warning("EnumRenderer: item key not specified");
        }
        break;
    }

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
      Calculation calculation, String itemKey, List<? extends IsColumn> dataColumns, int dataIndex) {
    if (description != null) {
      return createRenderer(description, itemKey, dataColumns, dataIndex);

    } else if (calculation != null) {
      return createRenderer(calculation, dataColumns, dataIndex);
    
    } else if (!BeeUtils.isEmpty(itemKey)) {
      Assert.isIndex(dataColumns, dataIndex);
      return new EnumRenderer(dataIndex, dataColumns.get(dataIndex), itemKey);

    } else {
      return null;
    }
  }

  private RendererFactory() {
  }
}
