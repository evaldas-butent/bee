package com.butent.bee.shared.data;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.logging.Logger;

public class Filter implements BeeSerializable {

  private enum SerializationMembers {
    TYPE, COLUMN, OPERATOR, VALUE, CONDITIONS
  }

  private static Logger logger = Logger.getLogger(Filter.class.getName());

  public static Filter and(Filter... filters) {
    return new Filter("AND", filters);
  }

  public static Filter condition(String colName, String op, Object value) {
    return new Filter(colName, op, value);
  }

  public static Filter or(Filter... filters) {
    return new Filter("OR", filters);
  }

  public static Filter restore(String s) {
    Filter filter = new Filter();
    filter.deserialize(s);
    return filter;
  }

  private boolean safe = true;
  private String nodeType;

  private String column;
  private String operator;
  private Value value;

  private List<Filter> conditions;

  private Filter() {
    this.safe = false;
  }

  private Filter(String join, Filter... filters) {
    Assert.notEmpty(join);
    this.nodeType = join;
    add(filters);
  }

  private Filter(String colName, String op, Object value) {
    Assert.notEmpty(colName);
    Assert.notEmpty(op);

    this.column = colName;
    this.operator = op;
    this.value = BeeUtils.objectToValue(value);
  }

  public Filter add(Filter... filters) {
    Assert.notEmpty(nodeType, "Not a composite filter");

    if (!BeeUtils.isEmpty(filters)) {
      if (BeeUtils.isEmpty(conditions)) {
        conditions = Lists.newArrayList();
      }
      for (Filter filter : filters) {
        if (!BeeUtils.isEmpty(filter)) {
          conditions.add(filter);
        }
      }
    }
    return this;
  }

  @Override
  public void deserialize(String s) {
    Assert.isFalse(safe);
    this.safe = true;

    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String expr = arr[i];

      switch (member) {
        case TYPE:
          this.nodeType = expr;
          break;

        case COLUMN:
          this.column = expr;
          break;

        case OPERATOR:
          this.operator = expr;
          break;

        case VALUE:
          if (!BeeUtils.isEmpty(expr)) {
            this.value = Value.restore(expr);
          }
          break;

        case CONDITIONS:
          if (!BeeUtils.isEmpty(expr)) {
            for (String xpr : Codec.beeDeserialize(expr)) {
              add(Filter.restore(xpr));
            }
          }
          break;

        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  public String getColumn() {
    return column;
  }

  public List<Filter> getConditions() {
    return conditions;
  }

  public String getNodeType() {
    return nodeType;
  }

  public String getOperator() {
    return operator;
  }

  public Value getValue() {
    return value;
  }

  public boolean isEmpty() {
    return !BeeUtils.isEmpty(nodeType) && BeeUtils.isEmpty(conditions);
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case TYPE:
          arr[i++] = getNodeType();
          break;

        case COLUMN:
          arr[i++] = getColumn();
          break;

        case OPERATOR:
          arr[i++] = getOperator();
          break;

        case VALUE:
          arr[i++] = getValue();
          break;

        case CONDITIONS:
          arr[i++] = getConditions();
          break;

        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }
}
