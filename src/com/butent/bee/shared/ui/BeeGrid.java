package com.butent.bee.shared.ui;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Implementation of a grid user interface component.
 */

public class BeeGrid implements BeeSerializable {

  private static Logger logger = Logger.getLogger(BeeGrid.class.getName());

  public static BeeGrid restore(String s) {
    BeeGrid grid = new BeeGrid();
    grid.deserialize(s);
    return grid;
  }

  /**
   * Contains a list of column parameters.
   */

  private enum ColumnMembers {
    TYPE, NAME, CAPTION, READONLY, WIDTH, SOURCE, RELSOURCE, RELATION, EXPRESSION
  }

  /**
   * Contains a list of grid parameters.
   */

  private enum GridMembers {
    NAME, VIEW, CAPTION, READONLY, COLUMNS
  }

  /**
   * Contains a list of available column types in the grid.
   */

  public enum ColType {
    DATA("BeeDataColumn"),
    RELATED("BeeRelColumn"),
    CALCULATED("BeeCalcColumn"),
    ID("BeeIdColumn"),
    VERSION("BeeVerColumn");

    public static ColType getColType(String tagName) {
      if (!BeeUtils.isEmpty(tagName)) {
        for (ColType type : ColType.values()) {
          if (BeeUtils.same(type.getTagName(), tagName)) {
            return type;
          }
        }
      }
      return null;
    }

    private final String tagName;

    private ColType(String tagName) {
      this.tagName = tagName;
    }

    public String getTagName() {
      return tagName;
    }
  }

  private class GridColumn implements BeeSerializable {
    private ColType type;
    private String colName;
    private String colCaption;
    private boolean colReadOnly;
    private int width;

    private String source;
    private String relSource;
    private String relation;
    private String expression;

    private GridColumn() {
    }

    public GridColumn(ColType type, String name, String caption, boolean readOnly, int width) {
      Assert.notEmpty(type);
      Assert.notEmpty(name);
      this.type = type;
      this.colName = name;
      this.colCaption = caption;
      this.colReadOnly = readOnly;
      this.width = width;
    }

    @Override
    public void deserialize(String s) {
      ColumnMembers[] members = ColumnMembers.values();
      String[] arr = Codec.beeDeserialize(s);
      Assert.lengthEquals(arr, members.length);

      for (int i = 0; i < members.length; i++) {
        ColumnMembers member = members[i];
        String value = arr[i];

        switch (member) {
          case TYPE:
            type = ColType.valueOf(value);
            break;
          case NAME:
            colName = value;
            break;
          case CAPTION:
            colCaption = value;
            break;
          case READONLY:
            colReadOnly = BeeUtils.toBoolean(value);
            break;
          case WIDTH:
            width = BeeUtils.toInt(value);
            break;
          case SOURCE:
            source = value;
            break;
          case RELSOURCE:
            relSource = value;
            break;
          case RELATION:
            relation = value;
            break;
          case EXPRESSION:
            expression = value;
            break;
          default:
            logger.severe("Unhandled serialization member: " + member);
            break;
        }
      }
    }

    @Override
    public String serialize() {
      ColumnMembers[] members = ColumnMembers.values();
      Object[] arr = new Object[members.length];
      int i = 0;

      for (ColumnMembers member : members) {
        switch (member) {
          case TYPE:
            arr[i++] = type;
            break;
          case NAME:
            arr[i++] = colName;
            break;
          case CAPTION:
            arr[i++] = colCaption;
            break;
          case READONLY:
            arr[i++] = colReadOnly;
            break;
          case WIDTH:
            arr[i++] = width;
            break;
          case SOURCE:
            arr[i++] = source;
            break;
          case RELSOURCE:
            arr[i++] = relSource;
            break;
          case RELATION:
            arr[i++] = relation;
            break;
          case EXPRESSION:
            arr[i++] = expression;
            break;
          default:
            logger.severe("Unhandled serialization member: " + member);
            break;
        }
      }
      return Codec.beeSerializeAll(arr);
    }

    private GridColumn setExpression(String expr) {
      this.expression = expr;
      return this;
    }

    private GridColumn setRelation(String relation) {
      this.relation = relation;
      return this;
    }

    private GridColumn setRelSource(String relSource) {
      this.relSource = relSource;
      return this;
    }

    private GridColumn setSource(String source) {
      this.source = source;
      return this;
    }
  }

  private String name;
  private String viewName;
  private String caption;
  private boolean readOnly;
  private Map<String, GridColumn> columns = Maps.newLinkedHashMap();

  public BeeGrid(String name, String viewName, String caption, boolean readOnly) {
    Assert.notEmpty(name);
    Assert.notEmpty(viewName);

    this.name = name;
    this.viewName = viewName;
    this.caption = caption;
    this.readOnly = readOnly;
  }

  private BeeGrid() {
  }

  public void addCalculatedColumn(String name, String caption, boolean readOnly, int width,
      String expr) {
    Assert.notEmpty(expr);
    addColumn(ColType.CALCULATED, name, caption, readOnly, width)
        .setExpression(expr);
  }

  public void addDataColumn(String name, String caption, boolean readOnly, int width,
      String source) {
    Assert.notEmpty(source);
    addColumn(ColType.DATA, name, caption, readOnly, width)
        .setSource(source);
  }

  public void addIdColumn(String name, String caption, int width) {
    addColumn(ColType.ID, name, caption, true, width);
  }

  public void addRelatedColumn(String name, String caption, boolean readOnly, int width,
      String source, String relSource, String relation) {
    Assert.notEmpty(source);
    Assert.notEmpty(relation);
    addColumn(ColType.RELATED, name, caption, readOnly, width)
        .setSource(source)
        .setRelSource(relSource)
        .setRelation(relation);
  }

  public void addVersionColumn(String name, String caption, int width) {
    addColumn(ColType.VERSION, name, caption, true, width);
  }

  @Override
  public void deserialize(String s) {
    Assert.isTrue(isEmpty());

    GridMembers[] members = GridMembers.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      GridMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case NAME:
          name = value;
          break;

        case VIEW:
          viewName = value;
          break;

        case CAPTION:
          caption = value;
          break;

        case READONLY:
          readOnly = BeeUtils.toBoolean(value);
          break;

        case COLUMNS:
          if (!BeeUtils.isEmpty(value)) {
            String[] data = Codec.beeDeserialize(value);

            for (String str : data) {
              GridColumn col = new GridColumn();
              col.deserialize(str);
              addColumn(col);
            }
          }
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  public String getCaption() {
    return caption;
  }

  public String getCaption(String colName) {
    return getColumn(colName).colCaption;
  }

  public int getColumnCount() {
    return columns.size();
  }

  public String[] getColumns() {
    int i = 0;
    int cnt = getColumnCount();
    String[] cols = new String[cnt];

    for (GridColumn col : columns.values()) {
      cols[i++] = col.colName;
    }
    return cols;
  }

  public String getExpression(String colName) {
    return getColumn(colName).expression;
  }

  public String getName() {
    return name;
  }

  public String getRelation(String colName) {
    return getColumn(colName).relation;
  }

  public String getRelSource(String colName) {
    return getColumn(colName).relSource;
  }

  public String getSource(String colName) {
    return getColumn(colName).source;
  }

  public ColType getType(String colName) {
    return getColumn(colName).type;
  }

  public String getViewName() {
    return viewName;
  }

  public int getWidth(String colName) {
    return getColumn(colName).width;
  }

  public boolean hasColumn(String name) {
    Assert.notEmpty(name);
    return columns.containsKey(name.toLowerCase());
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getColumnCount());
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean isReadOnly(String colName) {
    return getColumn(colName).colReadOnly;
  }

  @Override
  public String serialize() {
    GridMembers[] members = GridMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (GridMembers member : members) {
      switch (member) {
        case NAME:
          arr[i++] = getName();
          break;
        case VIEW:
          arr[i++] = getViewName();
          break;
        case CAPTION:
          arr[i++] = getCaption();
          break;
        case READONLY:
          arr[i++] = isReadOnly();
          break;
        case COLUMNS:
          arr[i++] = columns.values();
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }

  private void addColumn(GridColumn column) {
    Assert.notNull(column);
    Assert.state(!hasColumn(column.colName),
        BeeUtils.concat(1, "Dublicate column name:", getName(), column.colName));

    columns.put(column.colName.toLowerCase(), column);
  }

  private GridColumn addColumn(ColType type, String name, String caption, boolean readOnly,
      int width) {
    GridColumn col = new GridColumn(type, name, caption, readOnly, width);
    addColumn(col);
    return col;
  }

  private GridColumn getColumn(String name) {
    Assert.state(hasColumn(name), "Column not found: " + name);
    return columns.get(name.toLowerCase());
  }
}
