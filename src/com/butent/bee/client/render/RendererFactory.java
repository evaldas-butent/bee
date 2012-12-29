package com.butent.bee.client.render;

import com.google.common.collect.Lists;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.data.CellSource;
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
      List<String> renderColumns, List<? extends IsColumn> dataColumns, CellSource source) {

    if (description != null) {
      return createRenderer(description, itemKey, renderColumns, dataColumns, source);

    } else if (calculation != null) {
      return createRenderer(calculation, dataColumns, source);

    } else if (!BeeUtils.isEmpty(tokens)) {
      return createRenderer(tokens, dataColumns);
      
    } else if (!BeeUtils.isEmpty(itemKey) && source != null) {
      return new EnumRenderer(source, itemKey);
    
    } else if (!BeeUtils.isEmpty(renderColumns)) {
      if (renderColumns.size() == 1) {
        int index = DataUtils.getColumnIndex(renderColumns.get(0), dataColumns);
        return new SimpleRenderer(CellSource.forColumn(dataColumns.get(index), index));
      } else {
        return new JoinRenderer(dataColumns, null, renderColumns);
      }

    } else {
      return null;
    }
  }
  
  public static AbstractCellRenderer getRenderer(RendererDescription description,
      Calculation calculation, List<RenderableToken> tokens, String itemKey,
      List<String> renderColumns, List<? extends IsColumn> dataColumns, CellSource cellSource,
      Relation relation) {
    
    List<? extends IsColumn> columns;
    CellSource source;

    if (relation != null && relation.renderSource()) {
      columns = Data.getColumns(relation.getViewName());
      source = null;
    } else {
      columns = dataColumns;
      source = getDataSource(description, calculation, itemKey, dataColumns, cellSource, relation); 
    }

    return getRenderer(description, calculation, tokens, itemKey, renderColumns, columns, source);
  }

  private static AbstractCellRenderer createRenderer(Calculation calculation,
      List<? extends IsColumn> dataColumns, CellSource source) {
    Assert.notNull(calculation);

    String columnId = (source == null) ? null : source.getName();

    Evaluator evaluator = Evaluator.create(calculation, columnId, dataColumns);
    if (source != null) {
      Integer index = source.getIndex();
      if (index != null) {
        evaluator.setColIndex(index);
      }
    }

    return new EvalRenderer(source, evaluator);
  }
  
  private static AbstractCellRenderer createRenderer(RendererDescription description,
      String itemKey, List<String> renderColumns, List<? extends IsColumn> dataColumns,
      CellSource source) {

    Assert.notNull(description);
    RendererType type = description.getType();
    Assert.notNull(type);

    if (source == null && type.requiresSource()) {
      logger.warning("renderer", type.getTypeCode(), "requires source");
      return null;
    }

    AbstractCellRenderer renderer = null;
    switch (type) {
      case LIST:
        renderer = new ListRenderer(source);
        break;

      case MAP:
        renderer = new MapRenderer(source, description.getSeparator());
        break;

      case RANGE:
        renderer = new RangeRenderer(source, description.getSeparator(), description.getOptions());
        break;

      case ENUM:
        if (!BeeUtils.isEmpty(itemKey)) {
          renderer = new EnumRenderer(source, itemKey);
        } else {
          logger.warning("EnumRenderer: item key not specified");
        }
        break;

      case JOIN:
        renderer = new JoinRenderer(dataColumns, description.getSeparator(), renderColumns);
        break;
        
      case FLAG:
        renderer = new FlagRenderer(source);
        break;

      case STAR:
        renderer = new StarRenderer(source);
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
          type = ValueType.DATE_TIME;
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
      return new SimpleRenderer(CellSource.forColumn(dataInfo.getColumns().get(index), index));
    }
  }

  private static CellSource getDataSource(RendererDescription description, Calculation calculation,
      String itemKey, List<? extends IsColumn> dataColumns, CellSource source, Relation relation) {
    if (relation == null || BeeUtils.isEmpty(itemKey)) {
      return source;
    }
    if (description != null && !RendererType.ENUM.equals(description.getType())) {
      return source;
    }
    if (calculation != null) {
      return source;
    }

    int index = DataUtils.getColumnIndex(relation.getOriginalTarget(), dataColumns);
    if (!BeeConst.isUndef(index) && ValueType.isNumeric(dataColumns.get(index).getType())) {
      return CellSource.forColumn(dataColumns.get(index), index);
    } else {
      return source;
    }
  }
  
  private RendererFactory() {
  }
}
