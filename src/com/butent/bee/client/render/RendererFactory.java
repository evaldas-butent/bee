package com.butent.bee.client.render;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.ui.PotentialRenderer;
import com.butent.bee.shared.ui.RendererDescription;
import com.butent.bee.shared.ui.RendererType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RendererFactory {

  public static AbstractCellRenderer createRenderer(String viewName, List<String> renderColumns) {
    return createRenderer(viewName, renderColumns, null);
  }
  
  public static AbstractCellRenderer createRenderer(String viewName, List<String> renderColumns,
      String separator) {
    Assert.notEmpty(viewName);
    Assert.notEmpty(renderColumns);
    
    DataInfo dataInfo = Global.getDataInfo(viewName, true);
    if (dataInfo == null) {
      return null;
    }
    
    if (renderColumns.size() > 1) {
      return new JoinRenderer(dataInfo.getColumns(), separator, renderColumns);
    } else {
      int index = dataInfo.getColumnIndex(renderColumns.get(0));
      return new SimpleRenderer(index, dataInfo.getColumns().get(index));
    }
  }
  
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
      String itemKey, List<String> renderColumns, List<? extends IsColumn> dataColumns,
      int dataIndex) {
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

      case JOIN:
        renderer = new JoinRenderer(dataColumns, description.getSeparator(), renderColumns);
        break;
    }

    if (renderer instanceof HasValueStartIndex && description.getValueStartIndex() != null) {
      ((HasValueStartIndex) renderer).setValueStartIndex(description.getValueStartIndex());
    }

    if (renderer instanceof HasItems && description.getItems() != null) {
      ((HasItems) renderer).setItems(description.getItems());
    }
    return renderer;
  }

  public static AbstractCellRenderer getRenderer(PotentialRenderer potentialRenderer,
      String itemKey, List<String> renderColumns, List<? extends IsColumn> dataColumns,
      int dataIndex) {
    RendererDescription description = (potentialRenderer instanceof RendererDescription)
        ? (RendererDescription) potentialRenderer : null;
    Calculation calculation = (potentialRenderer instanceof Calculation)
        ? (Calculation) potentialRenderer : null;
    
    return getRenderer(description, calculation, itemKey, renderColumns, dataColumns, dataIndex);
  }
  
  public static AbstractCellRenderer getRenderer(RendererDescription description,
      Calculation calculation, String itemKey, List<String> renderColumns,
      List<? extends IsColumn> dataColumns, int dataIndex) {
    if (description != null) {
      return createRenderer(description, itemKey, renderColumns, dataColumns, dataIndex);

    } else if (calculation != null) {
      return createRenderer(calculation, dataColumns, dataIndex);

    } else if (!BeeUtils.isEmpty(itemKey)) {
      Assert.isIndex(dataColumns, dataIndex);
      return new EnumRenderer(dataIndex, dataColumns.get(dataIndex), itemKey);
    
    } else if (!BeeUtils.isEmpty(renderColumns)) {
      if (renderColumns.size() == 1) {
        int index = DataUtils.getColumnIndex(renderColumns.get(0), dataColumns);
        if (BeeConst.isUndef(index)) {
          index = dataIndex;
        }
        return new SimpleRenderer(index, dataColumns.get(index));
      } else {
        return new JoinRenderer(dataColumns, null, renderColumns);
      }

    } else {
      return null;
    }
  }

  private RendererFactory() {
  }
}
