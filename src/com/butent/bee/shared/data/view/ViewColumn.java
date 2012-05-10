package com.butent.bee.shared.data.view;

import com.google.common.base.Predicate;
import com.google.common.collect.Range;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class ViewColumn implements BeeSerializable, HasInfo {
  
  public static class Level implements Predicate<ViewColumn> {
    
    public static Level of(int level) {
      return new Level(level, null);
    }

    public static Level of(Range<Integer> range) {
      return (range == null) ? null : new Level(BeeConst.UNDEF, range);
    }
    
    private final int level;
    private final Range<Integer> range;

    private Level(int level, Range<Integer> range) {
      this.level = level;
      this.range = range;
    }

    @Override
    public boolean apply(ViewColumn input) {
      if (input == null) {
        return false;
      } else if (range == null) {
        return input.getLevel() == level;
      } else {
        return range.contains(input.getLevel());
      }
    }
  }

  private enum Serial {
    NAME, PARENT, TABLE, FIELD, RELATION, LEVEL, LOCALE, AGGREGATE, EXPRESSION, HIDDEN, READ_ONLY
  }

  public static Predicate<ViewColumn> VISIBLE = new Predicate<ViewColumn>() {
    @Override
    public boolean apply(ViewColumn input) {
      return (input == null) ? false : !input.isHidden();
    }
  };
  
  public static ViewColumn restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }

    ViewColumn vc = new ViewColumn();
    vc.deserialize(s);
    return vc;
  }
  
  private String name;
  private String parent;

  private String table;
  private String field;
  
  private String relation;
  private int level;
  
  private String locale;
  private String aggregate;
  private String expression;

  private boolean hidden;
  private boolean readOnly;

  public ViewColumn(String name, String parent, String table, String field, String relation,
      int level, String locale, String aggregate, String expression, boolean hidden,
      boolean readOnly) {
    super();
    this.name = name;
    this.parent = parent;
    this.table = table;
    this.field = field;
    this.relation = relation;
    this.level = level;
    this.locale = locale;
    this.aggregate = aggregate;
    this.expression = expression;
    this.hidden = hidden;
    this.readOnly = readOnly;
  }

  private ViewColumn() {
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case NAME:
          setName(value);
          break;
        case PARENT:
          setParent(value);
          break;
        case TABLE:
          setTable(value);
          break;
        case FIELD:
          setField(value);
          break;
        case RELATION:
          setRelation(value);
          break;
        case LEVEL:
          setLevel(BeeUtils.toInt(value));
          break;
        case LOCALE:
          setLocale(value);
          break;
        case AGGREGATE:
          setAggregate(value);
          break;
        case EXPRESSION:
          setExpression(value);
          break;
        case HIDDEN:
          setHidden(Codec.unpack(value));
          break;
        case READ_ONLY:
          setReadOnly(Codec.unpack(value));
          break;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ViewColumn)) {
      return false;
    }
    return BeeUtils.same(getName(), ((ViewColumn) obj).getName());
  }
  
  public String getAggregate() {
    return aggregate;
  }

  public String getExpression() {
    return expression;
  }

  public String getField() {
    return field;
  }

  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Name", getName(), "Parent", getParent(),
        "Table", getTable(), "Field", getField(),
        "Relation", getRelation(), "Level", getLevel(),
        "Locale", getLocale(), "Aggregate", getAggregate(), "Expression", getExpression(),
        "Hidden", isHidden(), "Read Only", isReadOnly());
  }

  public int getLevel() {
    return level;
  }

  public String getLocale() {
    return locale;
  }

  public String getName() {
    return name;
  }

  public String getParent() {
    return parent;
  }

  public String getRelation() {
    return relation;
  }

  public String getTable() {
    return table;
  }

  @Override
  public int hashCode() {
    return BeeUtils.normalize(getName()).hashCode();
  }
  
  public boolean isHidden() {
    return hidden;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case NAME:
          arr[i++] = getName();
          break;
        case PARENT:
          arr[i++] = getParent();
          break;
        case TABLE:
          arr[i++] = getTable();
          break;
        case FIELD:
          arr[i++] = getField();
          break;
        case RELATION:
          arr[i++] = getRelation();
          break;
        case LEVEL:
          arr[i++] = getLevel();
          break;
        case LOCALE:
          arr[i++] = getLocale();
          break;
        case AGGREGATE:
          arr[i++] = getAggregate();
          break;
        case EXPRESSION:
          arr[i++] = getExpression();
          break;
        case HIDDEN:
          arr[i++] = Codec.pack(isHidden());
          break;
        case READ_ONLY:
          arr[i++] = Codec.pack(isReadOnly());
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAggregate(String aggregate) {
    this.aggregate = aggregate;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public void setField(String field) {
    this.field = field;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  public void setTable(String table) {
    this.table = table;
  }
}
