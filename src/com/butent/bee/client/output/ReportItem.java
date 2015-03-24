package com.butent.bee.client.output;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

public abstract class ReportItem implements BeeSerializable {

  protected static final String STYLE_PREFIX = "bee-rep-";

  public static final String STYLE_BOOLEAN = STYLE_PREFIX + "boolean";
  public static final String STYLE_DATE = STYLE_PREFIX + "date";
  public static final String STYLE_DATETIME = STYLE_PREFIX + "datetime";
  public static final String STYLE_ENUM = STYLE_PREFIX + "enum";
  public static final String STYLE_NUM = STYLE_PREFIX + "num";
  public static final String STYLE_TEXT = STYLE_PREFIX + "text";

  private enum Serial {
    CLAZZ, NAME, CAPTION, EXPRESSION, FUNCTION, COL_SUMMARY, ROW_SUMMARY, RELATION, DATA
  }

  public enum Function implements HasLocalizedCaption {
    MIN() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.least();
      }
    },
    MAX() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.greatest();
      }
    },
    SUM() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.amount();
      }
    },
    COUNT() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.quantity();
      }
    },
    LIST() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.list();
      }
    };

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }
  }

  private final String name;
  private final String caption;
  private String expression;
  private Function function;
  private boolean colSummary;
  private boolean rowSummary;
  private String relation;

  protected ReportItem(String name, String caption) {
    this.name = Assert.notEmpty(name);
    this.caption = BeeUtils.notEmpty(caption, this.name);
    setExpression(name);
  }

  @SuppressWarnings("unchecked")
  public Object calculate(Object total, ReportValue value) {
    if (value != null && !BeeUtils.isEmpty(value.getValue())) {
      switch (getFunction()) {
        case COUNT:
          if (total == null) {
            return 1;
          }
          return (int) total + 1;
        case LIST:
          if (total == null) {
            return new TreeSet<>(Arrays.asList(value));
          }
          ((Collection<ReportValue>) total).add(value);
          break;
        case MAX:
          if (total == null || value.compareTo((ReportValue) total) > 0) {
            return value;
          }
          break;
        case MIN:
          if (total == null || value.compareTo((ReportValue) total) < 0) {
            return value;
          }
          break;
        case SUM:
          Assert.unsupported();
          break;
      }
    }
    return total;
  }

  public void clearFilter() {
  }

  @Override
  public void deserialize(String data) {
  }

  public ReportItem enableCalculation() {
    if (getFunction() == null) {
      setFunction(Function.MAX);
    }
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ReportItem)) {
      return false;
    }
    return Objects.equals(name, ((ReportItem) obj).name);
  }

  public abstract ReportValue evaluate(SimpleRow row);

  public EnumSet<Function> getAvailableFunctions() {
    return EnumSet.of(Function.MIN, Function.MAX, Function.COUNT, Function.LIST);
  }

  public String getCaption() {
    return caption;
  }

  public String getExpression() {
    return expression;
  }

  public Widget getFilterWidget() {
    return null;
  }

  public String getFormatedCaption() {
    return getCaption();
  }

  public Function getFunction() {
    return function;
  }

  public String getName() {
    return name;
  }

  public String getOptionsCaption() {
    return null;
  }

  public Widget getOptionsWidget() {
    return null;
  }

  public String getRelation() {
    return relation;
  }

  public abstract String getStyle();

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  public boolean isColSummary() {
    return getFunction() != null && colSummary;
  }

  public boolean isRowSummary() {
    return getFunction() != null && rowSummary;
  }

  public static ReportItem restore(String data) {
    if (BeeUtils.isEmpty(data)) {
      return null;
    }
    Map<String, String> map = Codec.deserializeMap(data);
    String clazz = map.get(Serial.CLAZZ.name());
    String name = map.get(Serial.NAME.name());
    String caption = map.get(Serial.CAPTION.name());

    ReportItem item = null;

    if (NameUtils.getClassName(ReportBooleanItem.class).equals(clazz)) {
      item = new ReportBooleanItem(name, caption);

    } else if (NameUtils.getClassName(ReportDateItem.class).equals(clazz)) {
      item = new ReportDateItem(name, caption);

    } else if (NameUtils.getClassName(ReportDateTimeItem.class).equals(clazz)) {
      item = new ReportDateTimeItem(name, caption);

    } else if (NameUtils.getClassName(ReportEnumItem.class).equals(clazz)) {
      item = new ReportEnumItem(name, caption);

    } else if (NameUtils.getClassName(ReportNumericItem.class).equals(clazz)) {
      item = new ReportNumericItem(name, caption);

    } else if (NameUtils.getClassName(ReportTextItem.class).equals(clazz)) {
      item = new ReportTextItem(name, caption);

    } else {
      Assert.unsupported("Unsupported class name: " + clazz);
    }
    item.setExpression(map.get(Serial.EXPRESSION.name()));
    item.setFunction(EnumUtils.getEnumByName(Function.class, map.get(Serial.FUNCTION.name())));
    item.setRowSummary(BeeUtils.toBoolean(map.get(Serial.ROW_SUMMARY.name())));
    item.setColSummary(BeeUtils.toBoolean(map.get(Serial.COL_SUMMARY.name())));
    item.setRelation(map.get(Serial.RELATION.name()));
    item.deserialize(map.get(Serial.DATA.name()));

    return item;
  }

  public ReportItem saveOptions() {
    return this;
  }

  @Override
  public String serialize() {
    return serialize(null);
  }

  public String serializeFilter() {
    return null;
  }

  public ReportItem setColSummary(boolean isSummary) {
    this.colSummary = isSummary;
    return this;
  }

  public ReportItem setExpression(String xpr) {
    this.expression = xpr;
    return this;
  }

  @SuppressWarnings("unused")
  public ReportItem setFilter(String value) {
    return this;
  }

  public ReportItem setFunction(Function fnc) {
    this.function = fnc;
    return this;
  }

  public ReportItem setRelation(String rel) {
    this.relation = rel;
    return this;
  }

  public ReportItem setRowSummary(boolean isSummary) {
    this.rowSummary = isSummary;
    return this;
  }

  @SuppressWarnings("unchecked")
  public Object summarize(Object total, Object value) {
    if (value != null) {
      if (total == null) {
        if (getFunction() == Function.LIST) {
          return new TreeSet<>((Collection<ReportValue>) value);
        }
        return value;
      }
      switch (getFunction()) {
        case COUNT:
          return (int) total + (int) value;
        case LIST:
          ((Collection<ReportValue>) total).addAll((Collection<ReportValue>) value);
          break;
        default:
          return calculate(total, (ReportValue) value);
      }
    }
    return total;
  }

  public boolean validate(SimpleRow row) {
    return row != null;
  }

  protected String serialize(String data) {
    Map<String, Object> map = new HashMap<>();

    for (Serial key : Serial.values()) {
      Object value = null;

      switch (key) {
        case CAPTION:
          value = getCaption();
          break;
        case CLAZZ:
          value = NameUtils.getClassName(this.getClass());
          break;
        case COL_SUMMARY:
          value = isColSummary();
          break;
        case DATA:
          value = data;
          break;
        case EXPRESSION:
          value = getExpression();
          break;
        case FUNCTION:
          value = getFunction();
          break;
        case NAME:
          value = getName();
          break;
        case RELATION:
          value = getRelation();
          break;
        case ROW_SUMMARY:
          value = isRowSummary();
          break;
      }
      map.put(key.name(), value);
    }
    return Codec.beeSerialize(map);
  }
}
