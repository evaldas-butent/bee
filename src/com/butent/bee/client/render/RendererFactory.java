package com.butent.bee.client.render;

import com.google.common.collect.Lists;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.RenderableToken;
import com.butent.bee.shared.ui.RendererDescription;
import com.butent.bee.shared.ui.RendererType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RendererFactory {

  private static final BeeLogger logger = LogUtils.getLogger(RendererFactory.class);
  
  public static AbstractCellRenderer createRenderer(String viewName, List<String> renderColumns) {
    return createRenderer(viewName, renderColumns, null);
  }
  
  public static AbstractCellRenderer getRenderer(RendererDescription description,
      Calculation calculation, List<RenderableToken> tokens, String itemKey,
      List<String> renderColumns, List<? extends IsColumn> dataColumns, int dataIndex) {

    if (description != null) {
      return createRenderer(description, itemKey, renderColumns, dataColumns, dataIndex);

    } else if (calculation != null) {
      return createRenderer(calculation, dataColumns, dataIndex);

    } else if (!BeeUtils.isEmpty(tokens)) {
      return createRenderer(tokens, dataColumns);
      
    } else if (!BeeUtils.isEmpty(itemKey) && BeeUtils.isIndex(dataColumns, dataIndex)) {
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
  
  public static AbstractCellRenderer getRenderer(RendererDescription description,
      Calculation calculation, List<RenderableToken> tokens, String itemKey,
      List<String> renderColumns, List<? extends IsColumn> dataColumns, int dataIndex,
      Relation relation) {
    
    List<? extends IsColumn> columns;
    int index;

    if (relation != null && relation.renderSource()) {
      columns = Data.getColumns(relation.getViewName());
      index = BeeConst.UNDEF;
    } else {
      columns = dataColumns;
      index = getDataIndex(description, calculation, itemKey, dataColumns, dataIndex, relation); 
    }

    return getRenderer(description, calculation, tokens, itemKey, renderColumns, columns, index);
  }

  private static AbstractCellRenderer createRenderer(Calculation calculation,
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
  
  private static AbstractCellRenderer createRenderer(RendererDescription description,
      String itemKey, List<String> renderColumns, List<? extends IsColumn> dataColumns,
      int dataIndex) {

    Assert.notNull(description);
    RendererType type = description.getType();
    Assert.notNull(type);

    IsColumn dataColumn = BeeUtils.getQuietly(dataColumns, dataIndex);
    if (dataColumn == null && type.requiresSource()) {
      logger.warning("renderer", type.getTypeCode(), "requires source");
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
          logger.warning("EnumRenderer: item key not specified");
        }
        break;

      case JOIN:
        renderer = new JoinRenderer(dataColumns, description.getSeparator(), renderColumns);
        break;
        
      case FLAG:
        renderer = new FlagRenderer(dataIndex);
        break;

      case STAR:
        renderer = new StarRenderer(dataIndex);
        break;
        
      default:
        logger.severe("renderer", type.name(), "not supported");
    }

    if (renderer instanceof HasValueStartIndex && description.getValueStartIndex() != null) {
      ((HasValueStartIndex) renderer).setValueStartIndex(description.getValueStartIndex());
    }

    if (renderer instanceof HasItems && description.getItems() != null) {
      ((HasItems) renderer).setItems(description.getItems());
    }
    return renderer;
  }

  private static AbstractCellRenderer createRenderer(List<RenderableToken> tokens,
      List<? extends IsColumn> dataColumns) {
    if (BeeUtils.isEmpty(tokens) || BeeUtils.isEmpty(dataColumns)) {
      return null;
    }
    
    List<ColumnToken> columnTokens = Lists.newArrayList();
    for (RenderableToken token : tokens) {
      String source = token.getSource();
      if (BeeUtils.isEmpty(source)) {
        continue;
      }
      
      int index = DataUtils.getColumnIndex(source, dataColumns);
      ValueType type = null;
      
      if (BeeConst.isUndef(index)) {
        if (BeeUtils.same(source, DataUtils.ID_TAG)) {
          index = DataUtils.ID_INDEX;
          type = DataUtils.ID_TYPE;
        } else if (BeeUtils.same(source, DataUtils.VERSION_TAG)) {
          index = DataUtils.VERSION_INDEX;
          type = ValueType.DATETIME;
        }
        
      } else {
        type = dataColumns.get(index).getType();
      }
      
      if (type == null) {
        logger.severe("token source not recognized:", source);
      } else {
        columnTokens.add(ColumnToken.create(index, type, token));
      }
    }
    
    if (columnTokens.isEmpty()) {
      logger.severe("cannot create TokenRenderer");
      return null;
    }
    return new TokenRenderer(columnTokens);
  }
  
  private static AbstractCellRenderer createRenderer(String viewName, List<String> renderColumns,
      String separator) {
    Assert.notEmpty(viewName);
    Assert.notEmpty(renderColumns);
    
    DataInfo dataInfo = Data.getDataInfo(viewName);
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

  private static int getDataIndex(RendererDescription description, Calculation calculation,
      String itemKey, List<? extends IsColumn> dataColumns, int dataIndex, Relation relation) {
    if (relation == null || BeeUtils.isEmpty(itemKey)) {
      return dataIndex;
    }
    if (description != null && !RendererType.ENUM.equals(description.getType())) {
      return dataIndex;
    }
    if (calculation != null) {
      return dataIndex;
    }

    int index = DataUtils.getColumnIndex(relation.getOriginalTarget(), dataColumns);
    if (!BeeConst.isUndef(index) && ValueType.isNumeric(dataColumns.get(index).getType())) {
      return index;
    } else {
      return dataIndex;
    }
  }
  
  private RendererFactory() {
  }
}
